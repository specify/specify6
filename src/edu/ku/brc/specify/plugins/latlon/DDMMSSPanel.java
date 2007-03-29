/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;
import static edu.ku.brc.util.LatLonConverter.stripZeroes;

import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.ui.validation.ValTextField;
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
    protected ValTextField latitudeSS;
    protected ValTextField longitudeSS;

    /**
     * Constructor. 
     */
    public DDMMSSPanel()
    {
        // nothing to do
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDMMMMPanel#init()
     */
    @Override
    public void init()
    {
        PanelBuilder builder = createUI("p, p, p, p, p, p, p, 2px, p", 3, 3, 9);
        
        latitudeSS   = createTextField(6);
        longitudeSS  = createTextField(6);
        
        latitudeMM.setColumns(3);
        longitudeMM.setColumns(3);
        
        CellConstraints cc = new CellConstraints();
        builder.add(new JLabel(" : "), cc.xy(6,1));
        builder.add(latitudeSS, cc.xy(7,1));

        builder.add(new JLabel(" : "), cc.xy(6,3));
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
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(latitude));
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
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(longitude));
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
            String str = latitudeDD.getText() + " " + latitudeMM.getText() + " " + latitudeSS.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                latitude =  LatLonConverter.convertDDMMSSToDDDD(str, NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            }

        } else
        {
            String str = longitudeDD.getText() + " " + longitudeMM.getText() + " " + longitudeSS.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                longitude = LatLonConverter.convertDDMMSSToDDDD(str, EAST_WEST[longitudeDir.getSelectedIndex()]);
            }
        }
    }
}

