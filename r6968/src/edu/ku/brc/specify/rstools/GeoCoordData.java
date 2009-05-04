/* Copyright (C) 2009, University of Kansas Center for Research
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
    
    /**
     * @param id
     * @param country
     * @param state
     * @param county
     * @param localityStr
     */
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
    public Integer getGeoCoordId()
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
