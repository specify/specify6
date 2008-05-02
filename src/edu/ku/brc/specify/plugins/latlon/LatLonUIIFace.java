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
    public void init();
    
    /**
     * Sets whether it is in "view" mode or "edit" mode.
     * @param isViewMode t/f
     */
    public void setViewMode(boolean isViewMode);
    
    /**
     * Sest the Latitude and Longitude.
     * @param latitude the lat
     * @param longitude the lon
     */
    public void set(final BigDecimal latitude, final BigDecimal longitude);
    
    /**
     * Gets the data from the controls. 
     */
    public void getDataFromUI();
    
    /**
     * @return the latitude
     */
    public BigDecimal getLatitude();
    
    /**
     * @return
     */
    public String getLatitudeDir();
    
    /**
     * @return the longitude
     */
    public BigDecimal getLongitude();
    
    /**
     * @return the char for the direction
     */
    public String getLongitudeDir();
    
    /**
     * @return
     */
    public boolean hasChanged();
    
    /**
     * @param hasChanged
     */
    public void setHasChanged(boolean hasChanged);
    
    /**
     * @param isRequired
     */
    public void setIsRequired(boolean isRequired);
    
    /**
     * @param changeListener
     */
    public void setChangeListener(final ChangeListener changeListener);
    
    /**
     * @return
     */
    public ErrorType validateState();
    
    /**
     * 
     */
    public void cleanUp();
    
  
}
