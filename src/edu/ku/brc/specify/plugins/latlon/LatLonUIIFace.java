/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

import java.math.BigDecimal;

import javax.swing.event.ChangeListener;

import edu.ku.brc.ui.validation.UIValidatable.ErrorType;


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
    
    public void set(final BigDecimal latitude, 
                    final BigDecimal longitude);
    
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
     * @return
     */
    public String getLongitudeDir();
    
    public boolean hasChanged();
    
    public void setHasChanged(boolean hasChanged);
    
    public void setIsRequired(boolean isRequired);
    
    public void setChangeListener(final ChangeListener changeListener);
    
    public ErrorType validateState();
    
    public void cleanUp();
    
  
}
