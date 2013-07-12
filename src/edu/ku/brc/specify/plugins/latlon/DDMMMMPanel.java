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

import static edu.ku.brc.ui.UIHelper.createLabel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonConverter.FORMAT;

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
        super();
        this.defaultFormat = FORMAT.DDMMMM;
        this.decimalFmtLen = 5;
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

        latitudeMM   = asMMIntegers ? createTextField(Integer.class, 8, 0, 59, latTFs) : createTextField(Double.class, 8, 0.0, 59.99999999, latTFs);
        longitudeMM  = asMMIntegers ? createTextField(Integer.class, 8, 0, 59, lonTFs) : createTextField(Double.class, 8, 0.0, 59.99999999, lonTFs);

        builder.add(createLabel(" "), cc.xy(4,1));
        builder.add(latitudeMM, cc.xy(5,1));

        builder.add(createLabel(" "), cc.xy(4,3));
        builder.add(longitudeMM, cc.xy(5,3));

        textFields.clear();
        textFields.addAll(latTFs);
        textFields.addAll(lonTFs);
        
        return builder;
    }

    /* (non-Javadoc)
     * @see DDDDPanel#setDataIntoUI()
     */
    @Override
    protected void setDataIntoUI()
    {
        if (latitudeStr != null && latInfoCnvrt != null)
        {
            latitudeDir.removeItemListener(this);
            latitudeDir.setSelectedIndex(latInfoCnvrt.isDirPositive() ? 0 : 1);
            latitudeDir.addItemListener(this);
            
            latitudeDD.setText(latInfoCnvrt.getPart(0));
            latitudeMM.setText(latInfoCnvrt.getPart(1));
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latInfoCnvrt.getDirStr());
            }
            
        } else
        {
            latitudeDir.removeItemListener(this);
            boolean isDefNorth = AppPreferences.getRemote().getBoolean(LatLonUI.LAT_PREF, true);
            latitudeDir.setSelectedIndex(isDefNorth ? 0 : 1);
            latitudeDir.addItemListener(this);
            
            latitudeDD.setText("");
            latitudeMM.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
            latitudeTF.setText("");
        }
        
        if (longitudeStr != null && lonInfoCnvrt != null)
        {
            longitudeDir.removeItemListener(this);
            longitudeDir.setSelectedIndex(lonInfoCnvrt.isDirPositive() ? 0 : 1);
            longitudeDir.addItemListener(this);
            
            longitudeDD.setText(lonInfoCnvrt.getPart(0));
            longitudeMM.setText(lonInfoCnvrt.getPart(1));
            
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(lonInfoCnvrt.getDirStr());
            }
            
        } else
        {
            longitudeDir.removeItemListener(this);
            boolean isDefWest = AppPreferences.getRemote().getBoolean(LatLonUI.LON_PREF, true);
            longitudeDir.setSelectedIndex(isDefWest ? 1 : 0);
            longitudeDir.addItemListener(this);

            longitudeDD.setText("");
            longitudeMM.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }
            longitudeTF.setText("");
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
                latitude = LatLonConverter.convertDDMMMMStrToDDDDBD(getStringFromFields(latitudeDD, latitudeMM), NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            } else
            {
                latitude = null;
            }
        } else
        {
            
            if (evalState(longitudeDD, longitudeMM) == ValState.Valid)
            {
                longitude =  LatLonConverter.convertDDMMMMStrToDDDDBD(getStringFromFields(longitudeDD, longitudeMM), EAST_WEST[longitudeDir.getSelectedIndex()]);
            } else
            {
                longitude = null;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#getLatitudeStr(boolean)
     */
    @Override
    public String getLatitudeStr(final boolean inclZeroes)
    {
        return getStringFromFields(true, true, latitudeDD, latitudeMM) + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#getLongitudeStr()
     */
    @Override
    public String getLongitudeStr(final boolean inclZeroes)
    {
        return getStringFromFields(true, inclZeroes, longitudeDD, longitudeMM) + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
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
