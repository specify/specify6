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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonConverter.FORMAT;

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
        this.defaultFormat = FORMAT.DDMMSS;
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
        if (latitudeStr != null && latInfoCnvrt != null)
        {
            latitudeDir.removeItemListener(this);
            latitudeDir.setSelectedIndex(latInfoCnvrt.isDirPositive() ? 0 : 1);
            latitudeDir.addItemListener(this);
            
            latitudeDD.setText(latInfoCnvrt.getPart(0));
            latitudeMM.setText(latInfoCnvrt.getPart(1));
            latitudeSS.setText(latInfoCnvrt.getPart(2));
            
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
            latitudeSS.setText("");
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
            longitudeSS.setText(lonInfoCnvrt.getPart(2));
            
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
                latitude = LatLonConverter.convertDDMMSSStrToDDDDBD(getStringFromFields(latitudeDD, latitudeMM, latitudeSS), NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
            } else
            {
                latitude = null;
            }

        } else if (evalState(longitudeDD, longitudeMM, longitudeSS) == ValState.Valid)
        {
            longitude = LatLonConverter.convertDDMMSSStrToDDDDBD(getStringFromFields(longitudeDD, longitudeMM, longitudeSS), EAST_WEST[longitudeDir.getSelectedIndex()]);
        } else
        {
            longitude = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.DDDDPanel#getLatitudeStr(boolean)
     */
    @Override
    public String getLatitudeStr(final boolean inclZeroes)
    {
        return getStringFromFields(true, inclZeroes, latitudeDD, latitudeMM, latitudeSS) + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /**
     * @return
     */
    @Override
    public String getLongitudeStr(final boolean inclZeroes)
    {
        return getStringFromFields(true, inclZeroes, longitudeDD, longitudeMM, longitudeSS) + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
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

