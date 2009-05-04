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

import javax.swing.ImageIcon;

import edu.ku.brc.util.Pair;

/**
 * This interface defines the minimum requirements for an object that is to
 * be displayed in Google Earth as a placemark.
 * 
 * @author jstewart
 * @code_status Complete
 */
public interface GoogleEarthPlacemarkIFace
{
    /**
     * Returns the title of the placemark.
     * 
     * @return the placemark title
     */
    public abstract String getTitle();
    
    /**
     * Returns the HTML content to be displayed in the placemark popup bubble.
     * @param textColor the text color for the HTML generation
     * @return the HTML content
     */
    public abstract String getHtmlContent(String textColor);
    
    /**
     * Returns the geolocation of the placemark (latitude, longitude).
     * 
     * @return the lat and lon
     */
    public abstract Pair<Double,Double> getLatLon();
    
    /**
     * @return an URL to the image to use or null
     */
    public abstract ImageIcon getImageIcon();
    
    /**
     * Provides an opportunity for the object to cleanup any internal data.
     */
    public abstract void cleanup();
}
