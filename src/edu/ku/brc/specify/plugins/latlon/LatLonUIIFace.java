/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

import java.math.BigDecimal;

import javax.swing.event.ChangeListener;

import edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType;


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
     */
    public abstract void set(final BigDecimal latitude, final BigDecimal longitude);
    
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
     * @return
     */
    public abstract ErrorType validateState();
    
    /**
     * Enables or Disabled the UI.
     * @param enabled true to enable the UI
     */
    public abstract void setEnabled(boolean enabled);
    
    /**
     * 
     */
    public abstract void cleanUp();
    
  
}
