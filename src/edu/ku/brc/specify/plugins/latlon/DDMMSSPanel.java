/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.util.LatLonConverter.stripZeroes;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.util.LatLonConverter;

/**
 * Used for entering lat/lon data in the Degrees, Minutes, and Decimal Seconds.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 8, 2007
 *
 */
public class DDMMSSPanel extends DDMMMMPanel
{
    protected ValFormattedTextFieldSingle latitudeSS;
    protected ValFormattedTextFieldSingle longitudeSS;

    /**
     * Constructor. 
     */
    public DDMMSSPanel()
    {
        decimalFmtLen = 3;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDMMMMPanel#init()
     */
    @Override
    public void init()
    {
        PanelBuilder builder = createUI("p, p, p, p, p, p, p, 2px, p", 3, 3, 9, true, true);
        
        latitudeSS   = createTextField(Double.class, 6, 0.0, 59.99999999);
        longitudeSS  = createTextField(Double.class, 6, 0.0, 59.99999999);
        
        latitudeMM.setColumns(3);
        longitudeMM.setColumns(3);
        
        CellConstraints cc = new CellConstraints();
        builder.add(createLabel(" : "), cc.xy(6,1));
        builder.add(latitudeSS, cc.xy(7,1));

        builder.add(createLabel(" : "), cc.xy(6,3));
        builder.add(longitudeSS, cc.xy(7,3));
    }

    
    /* (non-Javadoc)
     * @see DDMMMMPanel#setDataIntoUI()
     */
    @Override
    protected void setDataIntoUI()
    {
        if (latitude != null)
        {
            latitudeDir.setSelectedIndex(latitude.doubleValue() >= 0 ? 0 : 1);
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(latitude, decimalFmtLen));
            latitudeDD.setText(stripZeroes(parts[0]));
            latitudeMM.setText(stripZeroes(parts[1]));
            latitudeSS.setText(stripZeroes(parts[2]));
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latitudeDir.getSelectedItem().toString());
            }
        } else
        {
            latitudeDD.setText("");
            latitudeMM.setText("");
            latitudeSS.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
        }
        
        if (longitude != null)
        {
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(longitude, decimalFmtLen));
            longitudeDir.setSelectedIndex(longitude.doubleValue() >= 0 ? 0 : 1);
            longitudeDD.setText(stripZeroes(parts[0]));
            longitudeMM.setText(stripZeroes(parts[1]));  
            longitudeSS.setText(stripZeroes(parts[2]));
            
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(longitudeDir.getSelectedItem().toString());
            }
        } else
        {
            longitudeDD.setText("");
            longitudeMM.setText("");
            longitudeSS.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }    
        }
            
    }
    
    /* (non-Javadoc)
     * @see DDMMMMPanel#getDataFromUI(boolean)
     */
    @Override
    public void getDataFromUI(final boolean doLatitude)
    {
        if (doLatitude)
        {
            if (evalState(latitudeDD, latitudeMM, latitudeSS) == ValState.Valid)
            {
                latitude =  LatLonConverter.convertDDMMSSToDDDD(getStringFromFields(latitudeDD, latitudeMM, latitudeSS), NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            } else
            {
                latitude = null;
            }

        } else if (evalState(longitudeDD, longitudeMM, longitudeSS) == ValState.Valid)
        {
            longitude = LatLonConverter.convertDDMMSSToDDDD(getStringFromFields(longitudeDD, longitudeMM, longitudeSS), EAST_WEST[longitudeDir.getSelectedIndex()]);
        } else
        {
            longitude = null;
        }
    }
    
    /**
     * @return
     */
    public String getLatitudeStr()
    {
        return getStringFromFields(true, latitudeDD, latitudeMM, latitudeSS) + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /**
     * @return
     */
    public String getLongitudeStr()
    {
        return getStringFromFields(true, longitudeDD, longitudeMM, longitudeSS) + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDMMMMPanel#validateState(boolean)
     */
    public ErrorType validateState(final boolean includeEmptyCheck)
    {
        ErrorType state = validateStateTexFields(includeEmptyCheck);
        if (state == ErrorType.Valid)
        {
            ValState valStateLat = evalState(latitudeDD, latitudeMM, latitudeSS);
            ValState valStateLon = evalState(longitudeDD, longitudeMM, longitudeSS);
            state = valStateLat != ValState.Error && valStateLon != ValState.Error ? ErrorType.Valid : ErrorType.Error;
        }
        return state;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#clear()
     */
    @Override
    public void clear()
    {
        super.clear();
        latitudeSS.setText("");
        longitudeSS.setText("");
    }

}

