/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.rstools;

import java.net.URL;

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
     * 
     * @return the HTML content
     */
    public abstract String getHtmlContent();
    
    /**
     * Returns the geolocation of the placemark (latitude, longitude).
     * 
     * @return the lat and lon
     */
    public abstract Pair<Double,Double> getLatLon();
    
    /**
     * @return an URL to the image to use or null
     */
    public abstract URL getIconURL();
    
    /**
     * Provides an opportunity for the object to cleanup any internal data.
     */
    public abstract void cleanup();
}
