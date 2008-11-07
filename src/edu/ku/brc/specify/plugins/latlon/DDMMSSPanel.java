/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.util.LatLonConverter.parseLatLonStr;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.Part;

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
        super();
        this.defaultFormat = FORMAT.DDDDDD;
        this.decimalFmtLen = 3;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDMMMMPanel#init()
     */
    @Override
    public void init()
    {
        PanelBuilder builder = createUI("p, p, p, p, p, p, p, 2px, p", 3, 3, 9, true, true);
        
        latitudeSS   = createTextField(Double.class, 6, 0.0, 59.99999999, latTFs);
        longitudeSS  = createTextField(Double.class, 6, 0.0, 59.99999999, lonTFs);
        
        latitudeMM.setColumns(3);
        longitudeMM.setColumns(3);
        
        CellConstraints cc = new CellConstraints();
        builder.add(createLabel(" : "), cc.xy(6,1));
        builder.add(latitudeSS, cc.xy(7,1));

        builder.add(createLabel(" : "), cc.xy(6,3));
        builder.add(longitudeSS, cc.xy(7,3));
        
        textFields.clear();
        textFields.addAll(latTFs);
        textFields.addAll(lonTFs);
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
            
            Part[] parts = parseLatLonStr(latitudeStr);
            latitudeDD.setText(parts[0].getPart());
            latitudeMM.setText(parts[1].getPart());
            latitudeSS.setText(parts[2].getPart());
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latitudeDir.getSelectedItem().toString());
            }
            latitudeTF.setText(latitudeStr);
        } else
        {
            latitudeDD.setText("");
            latitudeMM.setText("");
            latitudeSS.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
            latitudeTF.setText("");
        }
        
        if (longitude != null)
        {
            Part[] parts = parseLatLonStr(longitudeStr);
            longitudeDir.setSelectedIndex(longitude.doubleValue() >= 0 ? 0 : 1);
            longitudeDD.setText(parts[0].getPart());
            longitudeMM.setText(parts[1].getPart());
            longitudeSS.setText(parts[2].getPart());
            
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(longitudeDir.getSelectedItem().toString());
            }
            longitudeTF.setText(longitudeStr);
        } else
        {
            longitudeDD.setText("");
            longitudeMM.setText("");
            longitudeSS.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }    
            longitudeTF.setText("");
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
    /*public ErrorType validateState(final boolean includeEmptyCheck)
    {
        ErrorType state = validateStateTexFields(includeEmptyCheck);
        if (state == ErrorType.Valid)
        {
            ValState valStateLat = evalState(latitudeDD, latitudeMM, latitudeSS);
            ValState valStateLon = evalState(longitudeDD, longitudeMM, longitudeSS);
            state = valStateLat != ValState.Error && valStateLon != ValState.Error ? ErrorType.Valid : ErrorType.Error;
            
            if (state == ErrorType.Valid)
            {
                latitudeTF.setText(getLatitudeStr());
                longitudeTF.setText(getLongitudeStr());
            }
        }
        return state;
    }*/
    
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

