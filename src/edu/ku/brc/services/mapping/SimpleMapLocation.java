/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.mapping;

import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;

/**
 * A simple implementation of {@link MapLocationIFace}.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class SimpleMapLocation implements MapLocationIFace
{
    protected Double lat1;
    protected Double long1;
    protected Double lat2;
    protected Double long2;
    
    /**
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     */
    public SimpleMapLocation(Double lat1, Double long1, Double lat2, Double long2)
    {
        super();
        this.lat1 = lat1;
        this.long1 = long1;
        this.lat2 = lat2;
        this.long2 = long2;
    }

    public Double getLat1()
    {
        return lat1;
    }

    public Double getLat2()
    {
        return lat2;
    }

    public Double getLong1()
    {
        return long1;
    }

    public Double getLong2()
    {
        return long2;
    }
}
