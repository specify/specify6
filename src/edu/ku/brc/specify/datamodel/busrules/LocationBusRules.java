/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UICacheManager.getLocalizedMessage;
import edu.ku.brc.specify.datamodel.Location;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class LocationBusRules extends BaseBusRules
{
    public LocationBusRules()
    {
        super(Location.class);
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        Location loc = (Location)dataObj;
        
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

        // for now, you can only delete Locations that have no children
        // we'll have to figure out a good way to check all the children later
        
//        // check all children
//        for (Location locChild: loc.getChildren())
//        {
//            if (!okToDelete(locChild))
//            {
//                return false;
//            }
//        }
        
        return true;
    }
}
