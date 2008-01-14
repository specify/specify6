/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.services.biogeomancer;

import java.util.List;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 14, 2008
 *
 */
public interface GeoRefServiceProviderIFace
{

    /**
     * @param items
     * @param listener
     */
    public abstract void processGeoRefData(List<GeoRefDataIFace> items, 
                                           GeoRefProviderCompletionIFace listener);
    
}
