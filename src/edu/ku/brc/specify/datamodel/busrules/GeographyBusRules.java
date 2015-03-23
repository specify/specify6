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
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.tasks.TreeTaskMgr;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Geography} or {@link GeographyTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class GeographyBusRules extends BaseTreeBusRules<Geography, GeographyTreeDef, GeographyTreeDefItem>
{
    protected static final String PARENT = "parent";
    protected static final String RANK = "definitionItem";
    
    /**
     * Constructor.
     */
    public GeographyBusRules()
    {
        super(Geography.class,GeographyTreeDefItem.class);
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
        return getLocalizedMessage("GEOGRAPHY_DELETED", ((Geography)dataObj).getName());
    }

    @Override
    public String[] getRelatedTableAndColumnNames()
    {
        String[] relationships = 
        {
                "locality",  "GeographyID",
                "geography", "AcceptedID"
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
        
        if (dataObj instanceof Geography)
        {
            return super.okToDeleteNode((Geography)dataObj);
        }
        if (dataObj instanceof GeographyTreeDefItem)
        {
            return okToDeleteDefItem((GeographyTreeDefItem)dataObj);
        }
        
        return false;
    }

    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link GeographyTreeDefItem}.
     * 
     * @param defItem the {@link GeographyTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(GeographyTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("geography", "GeographyTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSave(java.lang.Object)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Geography)
        {
            Geography geo = (Geography)dataObj;
            beforeSaveGeography(geo);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(geo, session);

            return;
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
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Geography}.  The real work of this method is to
     * update the 'fullname' field of all {@link Geography} objects effected by the changes
     * to the passed in {@link Geography}.
     * 
     * @param geo the {@link Geography} being saved
     */
    protected void beforeSaveGeography(Geography geo)
    {
        // nothing specific to Geography
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
		return Geography.class;
	}
    
    
}
