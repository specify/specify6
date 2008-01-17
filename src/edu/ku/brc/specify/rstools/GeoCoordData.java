/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.rstools;

import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 15, 2008
 *
 */
public class GeoCoordData implements GeoCoordDataIFace
{
    private int    id;
    private String country;
    private String state;
    private String county;
    private String localityStr;
    private String latitude;
    private String longitude;
    private String xml;
    
    public GeoCoordData(final int id, final String country, final String state, final String county, final String localityStr)
    {
        super();
        this.id = id;
        this.country = country;
        this.state = state;
        this.county = county;
        this.localityStr = localityStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCountry()
     */
    public String getCountry()
    {
        return country;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCounty()
     */
    public String getCounty()
    {
        return county;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getId()
     */
    public Integer getId()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLatitude()
     */
    public String getLatitude()
    {
        return latitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLocalityString()
     */
    public String getLocalityString()
    {
        return localityStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLongitude()
     */
    public String getLongitude()
    {
        return longitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getState()
     */
    public String getState()
    {
        return state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getTitle()
     */
    public String getTitle()
    {
        return "???";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getXML()
     */
    public String getXML()
    {
        return xml;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#set(java.lang.Double, java.lang.Double)
     */
    public void set(final String latitudeArg, final String longitudeArg)
    {
        this.latitude  = latitudeArg;
        this.longitude = longitudeArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#setXML(java.lang.String)
     */
    public void setXML(String xmlArg)
    {
        this.xml = xmlArg;
    }
    
}
