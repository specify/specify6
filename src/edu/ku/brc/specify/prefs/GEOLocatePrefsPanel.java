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
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
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
    private static final String GL_HYWX    		= "GEOLocate.HYWX";
    private static final String GL_WTRBODY 		= "GEOLocate.WATERBODY";
    private static final String GL_RESTRICT 	= "GEOLocate.RESTRICTTOLOWESTADM";
    private static final String GL_DOUNCERT 	= "GEOLocate.DOUNCERT";
    private static final String GL_DOPOLY 		= "GEOLocate.DOPOLY";
    private static final String GL_DISPLACEPOLY = "GEOLocate.DISPLACEPOLY";
    private static final String GL_LANGKEY      = "GEOLocate.LANGUAGEKEY";
    private static final String GL_USEGL_MAPS   = "GEOLocate.USEGL_MAPS";
    
    protected ValCheckBox  hywXCBX;
    protected ValCheckBox  waterBodyCBX;
    protected ValCheckBox  restrictToLowestAdmCBX;
    protected ValCheckBox  doUncertCBX;
    protected ValCheckBox  doPolyCBX;
    protected ValCheckBox  displacePolyCBX;
    protected ValCheckBox  geoLocMapServerCBX;
    protected ValComboBox  languageKeyCoBX;

    /**
     * 
     */
    public GEOLocatePrefsPanel()
    {
        super();
        createUI();
    }
    
    /**
     * Create the UI for the panel
     */
    protected void createUI()
    {
    	createForm("Preferences", "GEOLocatePrefs");
    	
    	AppPreferences prefs = AppPreferences.getRemote();
    	String  languageKey   = prefs.get(GL_LANGKEY, "English");
    	
    	FormViewObj fvo = (FormViewObj)form;
    	languageKeyCoBX = fvo.getCompById("GEOLocate.LANGUAGEKEY");
        if (languageKeyCoBX != null)
        {
        	languageKeyCoBX.setValue(languageKey, null);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#createForm(java.lang.String, java.lang.String)
     */
    @Override
    public void createForm(final String viewSetName, final String viewName)
    {
        super.createForm(viewSetName, viewName);
        
        hywXCBX            = form.getCompById(GL_HYWX);
        waterBodyCBX       = form.getCompById(GL_WTRBODY);
        restrictToLowestAdmCBX = form.getCompById(GL_RESTRICT);
        doUncertCBX        = form.getCompById(GL_DOUNCERT);
        doPolyCBX          = form.getCompById(GL_DOPOLY);
        displacePolyCBX    = form.getCompById(GL_DISPLACEPOLY);
        languageKeyCoBX    = form.getCompById(GL_LANGKEY);
        geoLocMapServerCBX = form.getCompById(GL_USEGL_MAPS);
        boolean isFish = Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish);
        
        AppPreferences locPrefs = AppPreferences.getLocalPrefs();
        hywXCBX.setValue(locPrefs.getBoolean(GL_HYWX, isFish), null);
        waterBodyCBX.setValue(locPrefs.getBoolean(GL_WTRBODY, isFish), null);
        restrictToLowestAdmCBX.setValue(locPrefs.getBoolean(GL_RESTRICT, !isFish), null);
        doUncertCBX.setValue(locPrefs.getBoolean(GL_DOUNCERT, isFish), null);
        doPolyCBX.setValue(locPrefs.getBoolean(GL_DOPOLY, isFish), null);
        displacePolyCBX.setValue(locPrefs.getBoolean(GL_DISPLACEPOLY, isFish), null);
        geoLocMapServerCBX.setValue(locPrefs.getBoolean(GL_USEGL_MAPS, true), null);
        //languageKeyCoBX.setValue(locPrefs.get(GL_LANGKEY, null), null);
    }
    
    /**
     * @return
     */
    public Viewable getForm()
    {
    	return form;
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
     * @param cmpName
     * @param changeHash
     */
    private void checkChanged(final ValCheckBox cbx, final String cmpName, final Properties changeHash)
    {
        if (cbx != null && cbx.isChanged())
        {
            Object value = cbx.getValue();
            if (value != null)
            {
                changeHash.put(cmpName, value);
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
     * @param prefName
     */
    private void saveCBX(final ValCheckBox cbx, final String prefName)
    {
        if (cbx.isChanged())
        {
            Object value = cbx.getValue();
            if (value != null)
            {
                AppPreferences.getLocalPrefs().putBoolean(prefName, cbx.isSelected());
            }
        }
    }
    
    /**
     * @param cbx
     * @param prefName
     */
    private void saveCoBX (final ValComboBox cbx, final String prefName)
    {
    	if (cbx.isChanged())
        {
            String method = (String)cbx.getValue();
            if (method != null)
            {
            	AppPreferences.getRemote().put(prefName, method);
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
        saveCBX(restrictToLowestAdmCBX, GL_RESTRICT);
        saveCBX(doUncertCBX, GL_DOUNCERT);
        saveCBX(doPolyCBX, GL_DOPOLY);
        saveCBX(displacePolyCBX, GL_DISPLACEPOLY);
        saveCBX(geoLocMapServerCBX, GL_USEGL_MAPS);
        saveCoBX(languageKeyCoBX, GL_LANGKEY);
    }
}
