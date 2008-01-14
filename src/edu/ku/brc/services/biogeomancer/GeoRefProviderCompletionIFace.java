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
public interface GeoRefProviderCompletionIFace
{
    /**
     * @param items
     */
    public void complete(final List<GeoRefDataIFace> items);
}
