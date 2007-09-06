/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Location} or
 * {@link LocationTreeDefItem} objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class LocationBusRules extends BaseTreeBusRules<Location, LocationTreeDef, LocationTreeDefItem>
{
    /**
     * Constructor.
     */
    public LocationBusRules()
    {
        super(Location.class, LocationTreeDefItem.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        if (dataObj instanceof Location)
        {
            return getLocalizedMessage("LOCATION_DELETED", ((Location)dataObj).getName());
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
        
        if (dataObj instanceof Location)
        {
            Location loc = (Location)dataObj;
            beforeSaveLocation(loc);
            
            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(loc, session);

            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Location}.  The real work of this method is to
     * update the 'fullname' field of all {@link Location} objects effected by the changes
     * to the passed in {@link Location}.
     * 
     * @param loc the {@link Location} being saved
     */
    protected void beforeSaveLocation(@SuppressWarnings("unused") Location loc)
    {
        // nothing specific to Location
    }
    
    @Override
    public boolean hasNoConnections(Location loc)
    {
        Integer id = loc.getTreeId();
        if (id == null)
        {
            return true;
        }
        
        boolean noPreps       = super.okToDelete("preparation", "LocationID", id);
        boolean noContainers  = super.okToDelete("container",   "LocationID", id);
        boolean noSyns        = super.okToDelete("location",    "AcceptedID", id);

        boolean noConns = noPreps && noContainers && noSyns;
        
        return noConns;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Location)
        {
            return super.okToDeleteNode((Location)dataObj);
        }
        if (dataObj instanceof LocationTreeDefItem)
        {
            return okToDeleteDefItem((LocationTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link LocationTreeDefItem}.
     * 
     * @param defItem the {@link LocationTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(LocationTreeDefItem defItem)
    {
        // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("location", "LocationTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }
}
