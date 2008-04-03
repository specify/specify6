/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Geography} or {@link GeographyTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class GeographyBusRules extends BaseTreeBusRules<Geography, GeographyTreeDef, GeographyTreeDefItem>
{
    /**
     * Constructor.
     */
    public GeographyBusRules()
    {
        super(Geography.class,GeographyTreeDefItem.class);
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
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Geography}.  The real work of this method is to
     * update the 'fullname' field of all {@link Geography} objects effected by the changes
     * to the passed in {@link Geography}.
     * 
     * @param geo the {@link Geography} being saved
     */
    protected void beforeSaveGeography(@SuppressWarnings("unused") Geography geo)
    {
        // nothing specific to Geography
    }
}
