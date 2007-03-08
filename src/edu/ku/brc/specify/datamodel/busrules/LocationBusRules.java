/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class LocationBusRules extends BaseBusRules
{
    protected static final Logger log = Logger.getLogger(LocationBusRules.class);
    
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

    @Override
    public void afterSave(Object dataObj)
    {
        System.err.println("afterSave() on Location object");
    }

    @Override
    public void beforeSave(Object dataObj)
    {
        System.err.println("beforeSave() on Location object");
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Location)
        {
            return okToDeleteLocation((Location)dataObj);
        }
        
        if (dataObj instanceof LocationTreeDefItem)
        {
            return okToDeleteLocDefItem((LocationTreeDefItem)dataObj);
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    public boolean okToDeleteLocation(Location loc)
    {
        if (!okToDelete("preparation", "LocationID", loc.getId()))
        {
            return false;
        }
        
        if (!okToDelete("container", "LocationID", loc.getId()))
        {
            return false;
        }

        if (!okToDelete("location", "ParentID", loc.getId()))
        {
            return false;
        }
        
        return true;
    }
    
    public boolean okToDeleteLocDefItem(LocationTreeDefItem defItem)
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
