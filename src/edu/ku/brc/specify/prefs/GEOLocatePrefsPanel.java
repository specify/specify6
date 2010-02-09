/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.prefs;

import java.util.Properties;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Discipline;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 24, 2008
 *
 */
public class GEOLocatePrefsPanel extends GenericPrefsPanel
{
    private static final String GL_HYWX    = "GEOLocate.HYWX";
    private static final String GL_WTRBODY = "GEOLocate.WATERBODY";
    
    protected ValCheckBox  hywXCBX;
    protected ValCheckBox  waterBodyCBX;

    /**
     * 
     */
    public GEOLocatePrefsPanel()
    {
        super();
        
        createForm("Preferences", "GEOLocatePrefs");
        
        //this.hContext = "GeoLocatePref";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#createForm(java.lang.String, java.lang.String)
     */
    @Override
    public void createForm(String viewSetName, String viewName)
    {
        super.createForm(viewSetName, viewName);
        
        hywXCBX      = form.getCompById(GL_HYWX);
        waterBodyCBX = form.getCompById(GL_WTRBODY);
        
        boolean isFish = Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish);
        
        hywXCBX.setValue(AppPreferences.getLocalPrefs().getBoolean(GL_HYWX, isFish), "");
        waterBodyCBX.setValue(AppPreferences.getLocalPrefs().getBoolean(GL_WTRBODY, isFish), "");
    }

    
  //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getChangedFields(java.util.Properties)
     */
    @Override
    public void getChangedFields(final Properties changeHash)
    {
        checkChanged(hywXCBX, GL_HYWX, changeHash);
        checkChanged(waterBodyCBX, GL_WTRBODY, changeHash);
        
    }
    
    /**
     * @param cbx
     * @param name
     * @param changeHash
     */
    private void checkChanged(final ValCheckBox cbx, final String name, final Properties changeHash)
    {
        if (cbx != null && cbx.isChanged())
        {
            Object value = cbx.getValue();
            if (value != null)
            {
                changeHash.put(name, value);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelIFace#isFormValid()
     */
    public boolean isFormValid()
    {
        return true;
    }
    
    /**
     * @param cbx
     * @param name
     */
    private void saveCBX(final ValCheckBox cbx, final String name)
    {
        if (cbx.isChanged())
        {
            Object value = cbx.getValue();
            if (value != null)
            {
                AppPreferences.getLocalPrefs().putBoolean(name, cbx.isSelected());
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        saveCBX(hywXCBX, GL_HYWX);
        saveCBX(waterBodyCBX, GL_WTRBODY);
    }
}
