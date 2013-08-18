/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.awt.Component;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class LocalityBusRules extends AttachmentOwnerBaseBusRules implements ListSelectionListener
{
    protected ValComboBoxFromQuery geographyCBX = null;
    /**
     * 
     */
    public LocalityBusRules()
    {
        super(Locality.class);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            Component comp = formViewObj.getCompById("4");
            if (comp != null && comp instanceof ValComboBoxFromQuery)
            {
                geographyCBX = (ValComboBoxFromQuery)comp;
                geographyCBX.addListSelectionListener(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        if (geographyCBX != null)
        {
            geographyCBX.removeListSelectionListener(this);
            geographyCBX = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        // TODO Auto-generated method stub
        return super.processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (viewable instanceof FormViewObj)
        {
            Locality locality = (Locality)dataObj;
            if (locality  != null)
            {
                boolean   enable   = locality.getGeography() != null && StringUtils.isNotEmpty(locality.getLocalityName());
                Component bgmComp  = formViewObj.getCompById("23");
                if (bgmComp != null)
                {
                    bgmComp.setEnabled(enable);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if (formViewObj != null && formViewObj.getDataObj() != null)
        {
            afterFillForm(formViewObj.getDataObj());
            /*Geography geography = (Geography)geographyCBX.getValue();
            Locality  locality  = (Locality)formViewObj.getDataObj();
            if (locality  != null)
            {
                locality.setGeography(geography);
            }*/
        }
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            Locality locality = (Locality)dataObj;
            
            Integer id = locality.getId();
            if (id == null)
            {
                isOK = true;
                
            } else
            {
                isOK = okToDelete(0, new String[] {"collectingevent", "LocalityID"}, locality.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        return super.afterSaveCommit(dataObj, session);
    }
    
//    /* (non-Javadoc)
//     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
//     */
//    @Override
//    public void addChildrenToNewDataObjects(final Object newDataObj)
//    {
//        super.addChildrenToNewDataObjects(newDataObj);
//        
//        if (newDataObj instanceof GeoCoordDetail)
//        {
//            if ()
//        } else if (newDataObj instanceof LocalityDetail)
//        {
//            
//        }
//    }
}
