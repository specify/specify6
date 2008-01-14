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

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 14, 2008
 *
 */
public interface GeoRefDataIFace
{

    //--------------------------------------------
    //-- This portion is used for getting the data
    //--------------------------------------------
    
    public abstract String getLocalityString();
    
    public abstract Double getLatitude();
    
    public abstract Double getLongitude();
    
    public abstract Integer getId();
    
    public abstract String getTitle();
    
    public abstract String getCountry();
    
    public abstract String getState();
    
    public abstract String getCounty();
    
    //--------------------------------------------
    //-- This portion is used for setting the data
    //--------------------------------------------

    public abstract void set(final Double latitude, Double longitude);
    
    public abstract void setXML(String xml);
    
    public abstract String getXML();
}
