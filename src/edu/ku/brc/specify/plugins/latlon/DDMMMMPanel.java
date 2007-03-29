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
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.ui.validation.ValTextField;
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
    
    protected ValTextField   latitudeMM;
    protected ValTextField   longitudeMM;
    
    
    /**
     * Constructor. 
     */
    public DDMMMMPanel()
    {
        // nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#init()
     */
    @Override
    public void init()
    {
        createUI("p, p, p, p, p, 2px, p", 3, 3, 7);
    }
    
    /* (non-Javadoc)
     * @see DDDDPanel#createUI(java.lang.String, int, int, int)
     */
    @Override
    protected PanelBuilder createUI(final String colDef, 
                                    final int latCols,
                                    final int lonCols,
                                    final int cbxIndex)
    {
        PanelBuilder    builder = super.createUI(colDef, latCols, lonCols, cbxIndex);
        CellConstraints cc      = new CellConstraints();

        latitudeMM   = createTextField(8);
        longitudeMM  = createTextField(8);

        builder.add(new JLabel(" "), cc.xy(4,1));
        builder.add(latitudeMM, cc.xy(5,1));

        builder.add(new JLabel(" "), cc.xy(4,3));
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
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMMM(latitude.abs()));
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
            String[] parts = StringUtils.split(LatLonConverter.convertToDDMMMM(longitude.abs()));
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
            String str = latitudeDD.getText() + " " + latitudeMM.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                latitude = LatLonConverter.convertDDMMMMToDDDD(str, NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            }
        } else
        {
            String str = longitudeDD.getText() + " " + longitudeMM.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                longitude =  LatLonConverter.convertDDMMMMToDDDD(str, EAST_WEST[longitudeDir.getSelectedIndex()]);
            }
        }
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
    
}
