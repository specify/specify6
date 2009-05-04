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
    
    public abstract Integer getGeoCoordId();
    
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
