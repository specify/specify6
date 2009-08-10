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
package edu.ku.brc.specify.ui;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 9, 2009
 *
 */
public class LatLonPoint implements LatLonPointIFace
{

    protected Double latitude;
    protected Double longitude;
    protected Double altitude;
    protected String title;
    
    
    /**
     * @param latitude
     * @param longitude
     */
    public LatLonPoint(Double latitude, Double longitude)
    {
        this(latitude, longitude, null, null);
    }

    /**
     * @param latitude
     * @param longitude
     * @param title
     */
    public LatLonPoint(Double latitude, Double longitude, String title)
    {
        this(latitude, longitude, null, title);
    }

    /**
     * @param latitude
     * @param longitude
     * @param altitude
     * @param title
     */
    public LatLonPoint(Double latitude, Double longitude, Double altitude, String title)
    {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.LatLonPointIFace#getAltitude()
     */
    @Override
    public Double getAltitude()
    {
        return altitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.LatLonPointIFace#getLatitude()
     */
    @Override
    public Double getLatitude()
    {
        return latitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.LatLonPointIFace#getLongitude()
     */
    @Override
    public Double getLongitude()
    {
        return longitude;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.LatLonPointIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(Double altitude)
    {
        this.altitude = altitude;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

}
