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
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PermitAttachment;

/**
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class PermitBusRules extends AttachmentOwnerBaseBusRules
{  
    /**
     * Constructor.
     */
    public PermitBusRules()
    {
        super(Permit.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#processBusiessRules(java.lang.Object)
     */
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Permit))
        {
            return STATUS.Error;
        }
        
        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("permitNumber", 
                                                                (FormDataObjIFace)dataObj, 
                                                                Permit.class, 
                                                                "permitId");

        return duplicateNumberStatus;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj instanceof Permit)
        {
            Permit permit = (Permit)dataObj;
            if (permit.getPermitId() == null || permit.getAccessionAuthorizations().size() == 0)
            {
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#deleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(final Object dataObj)
    {
        if (dataObj instanceof Permit)
        {
            return getLocalizedMessage("PERMIT_DELETED", ((Permit)dataObj).getPermitNumber());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }
    
    /**
     * Add the Attachment Owners and Attachment Holders to MV to be processed.
     * @param attOwner the owner being processed.
     */
    protected void addExtraObjectForProcessing(final Permit attOwner)
    {
        
        if (viewable != null && viewable.getMVParent() != null && viewable.getMVParent().getTopLevel() != null)
        {
            MultiView topMV = viewable.getMVParent().getTopLevel();
            topMV.addBusRuleItem(attOwner);
            
            for (PermitAttachment att : attOwner.getAttachmentReferences())
            {
                topMV.addBusRuleItem(att);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        addExtraObjectForProcessing((Permit)dataObj);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        addExtraObjectForProcessing((Permit)dataObj);
    }
}
