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
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.util.LatLonConverter;

/**
 * Used for entering lat/lon data in the Degrees and Decimal Minutes.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 8, 2007
 *
 */
public class DDMMMMPanel extends DDDDPanel
{
    protected static final Logger log = Logger.getLogger(DDMMMMPanel.class);
    
    protected ValFormattedTextFieldSingle latitudeMM;
    protected ValFormattedTextFieldSingle longitudeMM;
    
    
    /**
     * Constructor. 
     */
    public DDMMMMPanel()
    {
        decimalFmtLen = 5;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#init()
     */
    @Override
    public void init()
    {
        createUI("p, p, p, p, p, 2px, p", 3, 3, 7, true, false);
    }
    
    /**
     * @param colDef
     * @param latCols
     * @param lonCols
     * @param cbxIndex
     * @param asDDIntegers
     * @param asMMIntegers
     * @return
     */
    protected PanelBuilder createUI(final String  colDef, 
                                    final int     latCols,
                                    final int     lonCols,
                                    final int     cbxIndex,
                                    final boolean asDDIntegers,
                                    final boolean asMMIntegers)
    {
        PanelBuilder    builder = super.createUI(colDef, latCols, lonCols, cbxIndex, asDDIntegers);
        CellConstraints cc      = new CellConstraints();

        latitudeMM   = asMMIntegers ? createTextField(Integer.class, 8, 0, 59) : createTextField(Double.class, 8, 0.0, 59.99999999);
        longitudeMM  = asMMIntegers ? createTextField(Integer.class, 8, 0, 59) : createTextField(Double.class, 8, 0.0, 59.99999999);

        builder.add(createLabel(" "), cc.xy(4,1));
        builder.add(latitudeMM, cc.xy(5,1));

        builder.add(createLabel(" "), cc.xy(4,3));
        builder.add(longitudeMM, cc.xy(5,3));

        return builder;
    }
    
    /* (non-Javadoc)
     * @see DDDDPanel#setDataIntoUI()
     */
    @Override
    protected void setDataIntoUI()
    {
        if (latitude != null)
        {
            //System.out.println("BD:["+latitude.abs()+"] text["+LatLonConverter.convertToDDMMMM(latitude.abs())+"]");
            latitudeDir.setSelectedIndex(latitude.doubleValue() >= 0 ? 0 : 1);
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMMM(latitude.abs(), decimalFmtLen));
            latitudeDD.setText(stripZeroes(parts[0]));
            latitudeMM.setText(stripZeroes(parts[1]));
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latitudeDir.getSelectedItem().toString());
            }

        } else
        {
            latitudeDD.setText("");
            latitudeMM.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
        }
        
        if (longitude != null)
        {
            //System.out.println("BD:["+longitude+"] text["+LatLonConverter.convertToDDMMMM(longitude.abs())+"]");
            longitudeDir.setSelectedIndex(longitude.doubleValue() >= 0 ? 0 : 1);
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMMM(longitude.abs(), decimalFmtLen));
            longitudeDD.setText(stripZeroes(parts[0]));
            longitudeMM.setText(stripZeroes(parts[1]));
            
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(longitudeDir.getSelectedItem().toString());
            }
        } else
        {
            longitudeDD.setText("");
            longitudeMM.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }    
        }
    }
    
    /* (non-Javadoc)
     * @see DDDDPanel#getDataFromUI(boolean)
     */
    @Override
    protected void getDataFromUI(final boolean doLatitude)
    {
        if (doLatitude)
        {
            if (evalState(latitudeDD, latitudeMM) == ValState.Valid)
            {
                latitude = LatLonConverter.convertDDMMMMToDDDD(getStringFromFields(latitudeDD, latitudeMM), NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            } else
            {
                latitude = null;
            }
        } else
        {
            
            if (evalState(longitudeDD, longitudeMM) == ValState.Valid)
            {
                longitude =  LatLonConverter.convertDDMMMMToDDDD(getStringFromFields(longitudeDD, longitudeMM), EAST_WEST[longitudeDir.getSelectedIndex()]);
            } else
            {
                longitude = null;
            }
        }
    }
    
    /**
     * @return
     */
    public String getLatitudeStr()
    {
        return getStringFromFields(true, latitudeDD, latitudeMM) + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /**
     * @return
     */
    public String getLongitudeStr()
    {
        return getStringFromFields(true, longitudeDD, longitudeMM) + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
    }

    /* (non-Javadoc)
     * @see DDDDPanel#getDataFromUI()
     */
    @Override
    public void getDataFromUI()
    {
        getDataFromUI(true);
        getDataFromUI(false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#validateState(boolean)
     */
    public ErrorType validateState(final boolean includeEmptyCheck)
    {
        ErrorType state = validateStateTexFields(includeEmptyCheck);
        if (state == ErrorType.Valid)
        {
            ValState valStateLat = evalState(latitudeDD, latitudeMM);
            ValState valStateLon = evalState(longitudeDD, longitudeMM);
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
        latitudeMM.setText("");
        longitudeMM.setText("");
    }
    
}
