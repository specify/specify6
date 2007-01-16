/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.ui.validation.ValTextField;

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
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(latitude));
            latitudeDD.setText(parts[0]);
            latitudeMM.setText(parts[1]);
            latitudeSS.setText(parts[2]);
        }
        
        if (longitude != null)
        {
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMSS(longitude));
            longitudeDD.setText(parts[0]);
            longitudeMM.setText(parts[1]);  
            longitudeSS.setText(parts[2]);
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
                latitude =  LatLonConverter.convertDDMMSSToDDDD(str);
            }

        } else
        {
            String str = longitudeDD.getText() + " " + longitudeMM.getText() + " " + longitudeSS.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                longitude = LatLonConverter.convertDDMMSSToDDDD(str);
            }
        }
    }
}

