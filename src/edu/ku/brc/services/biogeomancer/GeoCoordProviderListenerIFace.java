/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.services.biogeomancer;

import java.util.List;

/**
 * Listener interface for knowing when the GeoRef processing has completed.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 14, 2008
 *
 */
public interface GeoCoordProviderListenerIFace
{
    
    /**
     * Tells the listener that it is about to display some UI.
     */
    public abstract void aboutToDisplayResults();
    
    
    /**
     * Tells the user it has completed and returns the items.
     * @param items the list of items to be GeoRef'ed (the complete list).
     * @param itemsUpdated the number of items updated
     */
    public abstract void complete(final List<GeoCoordDataIFace> items, int itemsUpdated);
}
