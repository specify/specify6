/* Copyright (C) 2009, University of Kansas Center for Research
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
import java.util.Set;

import javax.swing.JTextField;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.DraggableRecordIdentifier;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Shipment;

/**
 * Business rules for validating a Loan.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ShipmentBusRules extends BaseBusRules
{
    /**
     * Constructor.
     */
    public ShipmentBusRules()
    {
        super(Shipment.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        if (formViewObj != null && formViewObj.getDataObj() instanceof Shipment)
        {
            MultiView mvParent = formViewObj.getMVParent();
            boolean   isNewObj = MultiView.isOptionOn(mvParent.getOptions(), MultiView.IS_NEW_OBJECT);
            if (isNewObj)
            {              
                Loan        loan     = (Loan)mvParent.getMultiViewParent().getData();
                Component   shipComp = formViewObj.getControlByName("shipmentNumber");
                if (shipComp instanceof JTextField)
                {
                    JTextField shipTxt = (JTextField)shipComp;
                    shipTxt.setText(loan.getLoanNumber());
                    //shipTxt.setChanged(true);
                }
            }
        }
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj == null || !(dataObj instanceof Shipment))
        {
            return STATUS.Error;
        }
        
        Shipment shipment = (Shipment)dataObj;
        
        //Agent agent = shipment.getAgentByShippedTo();
        Agent agent = shipment.getShippedTo();
        if (agent != null)
        {
            Set<Address> addr = agent.getAddresses();
            if (addr.size() == 0)
            {
                reasonList.add(getLocalizedMessage("LOAN_MISSING_AGENT_ADDR")); // This shouldn't ever happen
                return STATUS.Error;
            }
            
        } else
        {
            reasonList.add(getLocalizedMessage("Loan Agent is missing an Agent")); // This shouldn't ever happen
            return STATUS.Error;
        }     

        
        return STATUS.OK;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Accession)
        {
            return getLocalizedMessage("SHIPMENT_DELETED", ((Shipment)dataObj).getShipmentNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#setObjectIdentity(java.lang.Object, edu.ku.brc.ui.DraggableIcon)
     */
    public void setObjectIdentity(final Object dataObj, final DraggableRecordIdentifier draggableIcon)
    {
        if (dataObj == null)
        {
            draggableIcon.setLabel("");
        }
        
        if (dataObj instanceof Shipment)
        {
            Shipment shipment = (Shipment)dataObj;
            
            draggableIcon.setLabel(shipment.getShipmentNumber());
            
            Object data = draggableIcon.getData();
            if (data == null)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.addItem(shipment.getId());
                data = rs;
                draggableIcon.setData(data);
                
            } else if (data instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)data;
                rs.clearItems();
                rs.addItem(shipment.getId());
            }
        }
     }


}
