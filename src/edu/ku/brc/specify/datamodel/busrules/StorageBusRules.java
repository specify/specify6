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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.tasks.TreeTaskMgr;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Storage} or
 * {@link StorageTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class StorageBusRules extends BaseTreeBusRules<Storage, StorageTreeDef, StorageTreeDefItem>
{
    /**
     * Constructor.
     */
    public StorageBusRules()
    {
        super(Storage.class, StorageTreeDefItem.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        TreeTaskMgr.checkLocks(); // TreeTaskMgr needs to Watch for Data_Entry Commands instead of calling it directly
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        if (dataObj instanceof Storage)
        {
            return getLocalizedMessage("STORAGE_DELETED", ((Storage)dataObj).getName());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Storage)
        {
            Storage stor = (Storage)dataObj;
            beforeSaveStorage(stor);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(stor, session);

            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Storage}.  The real work of this method is to
     * update the 'fullname' field of all {@link Storage} objects effected by the changes
     * to the passed in {@link Storage}.
     * 
     * @param stor the {@link Storage} being saved
     */
    protected void beforeSaveStorage(Storage stor)
    {
        // nothing specific to Storage
    }
    
    @Override
    public String[] getRelatedTableAndColumnNames()
    {
        String[] relationships = 
        {
                "preparation", "StorageID",
                "container",   "StorageID",
                "storage",    "AcceptedID"
        };

        return relationships;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
        
        if (dataObj instanceof Storage)
        {
            return super.okToDeleteNode((Storage)dataObj);
        }
        
        if (dataObj instanceof StorageTreeDefItem)
        {
            return okToDeleteDefItem((StorageTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link StorageTreeDefItem}.
     * 
     * @param defItem the {@link StorageTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(StorageTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("storage", "StorageTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        TreeTaskMgr.checkLocks();
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#getNodeClass()
	 */
	@Override
	protected Class<?> getNodeClass()
	{
		return Storage.class;
	}
    
    
}
