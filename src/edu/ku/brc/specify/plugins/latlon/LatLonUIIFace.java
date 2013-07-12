/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.latlon;

import java.math.BigDecimal;

import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;


/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 8, 2007
 *
 */
public interface LatLonUIIFace
{
    public enum LatLonType {LLPoint, LLLine, LLRect}
    
    /**
     * Creates and initializes the UI.
     */
    public abstract void init();
    
    /**
     * Sets whether it is in "view" mode or "edit" mode.
     * @param isViewMode t/f
     */
    public abstract void setViewMode(boolean isViewMode);
    
    /**
     * Sest the Latitude and Longitude.
     * @param latitude the lat
     * @param longitude the lon
     * @param latitudeStr the string representation of the latitude value
     * @param longitudeStr the string representation of the longitude value
     */
    public abstract void set(BigDecimal latitude, 
                             BigDecimal longitude,
                             String     latitudeStr, 
                             String     longitudeStr);
    
    /**
     * Gets the data from the controls. 
     */
    public abstract void getDataFromUI();
    
    /**
     * @return the latitude
     */
    public abstract BigDecimal getLatitude();
    
    /**
     * @return
     */
    public abstract String getLatitudeDir();
    
    /**
     * @return the longitude
     */
    public abstract BigDecimal getLongitude();
    
    /**
     * @return the char for the direction
     */
    public abstract String getLongitudeDir();
    
    /**
     * @return the Latitude as a string
     */
    public abstract String getLatitudeStr();
    
    /**
     * @return the Longitude as a string
     */
    public abstract String getLongitudeStr();

    /**
     * @return
     */
    public abstract boolean hasChanged();
    
    /**
     * @param hasChanged
     */
    public abstract void setHasChanged(boolean hasChanged);
    
    /**
     * @param isRequired
     */
    public abstract void setIsRequired(boolean isRequired);
    
    /**
     * @param changeListener
     */
    public abstract void setChangeListener(final ChangeListener changeListener);
    
    /**
     * @param includeEmptyCheck indicates it should also check for empty as part of
     * the validation process. It is the same a required, but they can't be set to required.
     * @return whether the fields are valid
     */
    public abstract ErrorType validateState(boolean includeEmptyCheck);
    
    /**
     * @return a reason for being in Error or Incomplete or it can return null.
     */
    public abstract String getReason();
    
    /**
     * Enables or Disabled the UI.
     * @param enabled true to enable the UI
     */
    public abstract void setEnabled(boolean enabled);
    
    /**
     * Clears (resets) all the values);
     */
    public abstract void clear();
    
    /**
     * Clean up any internal unneeded data structures.
     */
    public abstract void cleanUp();
    
  
}
