/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.rstools;

import java.math.BigDecimal;

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
    private String errorPolygon;
    private BigDecimal errorEstimate;
    
    /**
     * @param id
     * @param country
     * @param state
     * @param county
     * @param localityStr
     */
    public GeoCoordData(final int id, 
                        final String country, 
                        final String state, 
                        final String county, 
                        final String localityStr,
                        final String errorPolygon,
                        final BigDecimal errorRadius)
    {
        super();
        this.id = id;
        this.country = country;
        this.state = state;
        this.county = county;
        this.localityStr = localityStr;
        this.errorPolygon = errorPolygon;
        this.errorEstimate = errorRadius;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCountry()
     */
    @Override
    public String getCountry()
    {
        return country;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCounty()
     */
    @Override
    public String getCounty()
    {
        return county;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getId()
     */
    @Override
    public Integer getGeoCoordId()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLatitude()
     */
    @Override
    public String getLatitude()
    {
        return latitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLocalityString()
     */
    @Override
    public String getLocalityString()
    {
        return localityStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLongitude()
     */
    @Override
    public String getLongitude()
    {
        return longitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getState()
     */
    @Override
    public String getState()
    {
        return state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "???";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getXML()
     */
    @Override
    public String getXML()
    {
        return xml;
    }

    /**
     * @return the errorPolygon
     */
    @Override
    public String getErrorPolygon()
    {
        return errorPolygon;
    }

    /**
     * @return the errorRadius
     */
    @Override
    public BigDecimal getErrorEstimate()
    {
        return errorEstimate;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#setErrorEstimate(java.lang.String)
     */
    @Override
    public void setErrorEstimate(BigDecimal errorEstimate)
    {
        this.errorEstimate = errorEstimate;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#setErrorPolygon(java.lang.String)
     */
    @Override
    public void setErrorPolygon(String errorPolygon)
    {
        this.errorPolygon = errorPolygon;
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
