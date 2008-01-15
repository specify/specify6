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
public interface GeoCoordDataIFace
{

    //--------------------------------------------
    //-- This portion is used for getting the data
    //--------------------------------------------
    
    public abstract String getLocalityString();
    
    public abstract String getLatitude();
    
    public abstract String getLongitude();
    
    public abstract Integer getId();
    
    public abstract String getTitle();
    
    public abstract String getCountry();
    
    public abstract String getState();
    
    public abstract String getCounty();
    
    //--------------------------------------------
    //-- This portion is used for setting the data
    //--------------------------------------------

    public abstract void set(final String latitude, String longitude);
    
    public abstract void setXML(String xml);
    
    public abstract String getXML();
}
