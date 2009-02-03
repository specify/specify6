/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */package edu.ku.brc.specify.datamodel.busrules;

import javax.swing.JButton;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
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

}
