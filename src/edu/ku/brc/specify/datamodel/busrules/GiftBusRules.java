/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Component;

import javax.swing.JCheckBox;

import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 2, 2008
 *
 */
public class GiftBusRules extends AttachmentOwnerBaseBusRules
{
    public static final String CMDTYPE  = "Interactions";
    public final String NEW_GIFT = "NEW_GIFT";

    /**
     * @param dataClasses
     */
    public GiftBusRules()
    {
        super(GiftBusRules.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        if (formViewObj != null && formViewObj.getDataObj() instanceof Gift)
        {
            formViewObj.setSkippingAttach(true);

            MultiView mvParent = formViewObj.getMVParent();
            boolean   isEdit   = mvParent.isEditable();

            Component comp     = formViewObj.getControlByName("generateInvoice");
            if (comp instanceof JCheckBox)
            {
                ((JCheckBox)comp).setVisible(isEdit);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(Object dataObj, DataProviderSessionIFace session)
    {
        /*reasonList.clear();
        
        Gift gift = (Gift)dataObj;
        
        for (GiftPreparation giftPrep : gift.getGiftPreparations())
        {
            int availCnt = LoanBusRules.getUsedPrepCount(giftPrep.getPreparation());
            if (availCnt < 1)
            {
                reasonList.add("Not enough Preps to Gift "+availCnt); // I18N
                return false;
            }
        }*/
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);

         Gift gift = (Gift)dataObj;
        
        //System.out.println("beforeSaveCommit giftNum: "+gift.getGiftNumber());
        
        for (Shipment shipment : gift.getShipments())
        {
            if (shipment.getShipmentId() == null)
            {
                //shipment.setShipmentNumber(gift.getGiftNumber());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Gift))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("giftNumber", 
                                                                (FormDataObjIFace)dataObj, 
                                                                Gift.class, 
                                                                "giftId");

        return duplicateNumberStatus;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return getLocalizedMessage("LOAN_DELETED", ((Gift)dataObj).getGiftNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.DraggableIcon)
     */
    public void setObjectIdentity(final Object dataObj, 
                                  final DraggableRecordIdentifier draggableIcon)
    {
        if (dataObj == null)
        {
            draggableIcon.setLabel("");
        }
        
        if (dataObj instanceof Gift)
        {
            Gift gift = (Gift)dataObj;
            
            draggableIcon.setLabel(gift.getGiftNumber());
            
            Object data = draggableIcon.getData();
            if (data == null)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.addItem(gift.getGiftId());
                data = rs;
                draggableIcon.setData(data);
                
            } else if (data instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)data;
                rs.clearItems();
                rs.addItem(gift.getGiftId());
            }
        }
     }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#canCreateNewDataObject()
     */
    @Override
    public boolean canCreateNewDataObject()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#createNewObj(boolean, java.lang.Object)
     */
    @Override
    public void createNewObj(boolean doSetIntoAndValidateArg, Object oldDataObj)
    {
        CommandAction cmdAction = new CommandAction(CMDTYPE, NEW_GIFT, viewable);
        CommandDispatcher.dispatch(cmdAction);
    }
}
