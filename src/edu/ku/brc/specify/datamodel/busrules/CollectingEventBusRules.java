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

import javax.swing.JButton;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Locality;

/**
 * @author rod 
 *
 * (original author was JDS)
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class CollectingEventBusRules extends AttachmentOwnerBaseBusRules
{
    /**
     * 
     */
    public CollectingEventBusRules()
    {
        super(CollectingEvent.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            JButton newBtn = getNewBtn();
            if (newBtn != null)
            {
                newBtn.setVisible(false);
            }
            JButton delBtn = getDelBtn();
            if (delBtn != null)
            {
                delBtn.setVisible(false);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        if (false)
        {
            CollectingEvent ce = (CollectingEvent)newDataObj;
            
            if (ce.getLocality() == null)
            {
                Locality locality = new Locality();
                locality.initialize();
                
                ce.addReference(locality, "locality");
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object                     dataObj,
                           final DataProviderSessionIFace   session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            CollectingEvent ce = (CollectingEvent)dataObj;
            
            Integer id = ce.getId();
            if (id == null)
            {
                isOK = true;
                
            } else
            {
                Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                int        count      = collection.getIsEmbeddedCollectingEvent() ? 1 : 0;
                isOK = okToDelete(count, new String[] {"collectionobject", "CollectingEventID"}, ce.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#shouldCloneField(java.lang.String)
     */
    @Override
    public boolean shouldCloneField(String fieldName)
    {
        if (fieldName.equals("collectingEventAttribute"))
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * Add the Attachment Owners and Attachment Holders to MV to be processed.
     * @param  attOwner the owner being processed.
     */
    protected void addExtraObjectForProcessing(final CollectingEvent attOwner)
    {
        
        if (viewable != null && viewable.getMVParent() != null && viewable.getMVParent().getTopLevel() != null)
        {
            MultiView topMV = viewable.getMVParent().getTopLevel();
            topMV.addBusRuleItem(attOwner);
            
            for (CollectingEventAttachment att : attOwner.getAttachmentReferences())
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
        
        addExtraObjectForProcessing((CollectingEvent)dataObj);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        addExtraObjectForProcessing((CollectingEvent)dataObj);

    }
}
