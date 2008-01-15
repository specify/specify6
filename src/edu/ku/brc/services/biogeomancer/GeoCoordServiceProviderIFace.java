/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.services.biogeomancer;

import java.util.List;

/**
 * An interface that provides Geo-Referencing capability.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 14, 2008
 *
 */
public interface GeoCoordServiceProviderIFace
{
    /**
     * Use the BioGeomancer web service to lookup georeferences foe the given items.
     * @param items the list of items to be GeoRef'ed
     * @param listener a listener for progress
     * @param helpContext provides a help context for any UI that may be presented durng the GeoRef process
     */
    public abstract void processGeoRefData(List<GeoCoordDataIFace> items, 
                                           GeoCoordProviderListenerIFace listener,
                                           String helpContext);
    
}
