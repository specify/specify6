/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.config;

import java.awt.Color;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.ColorWrapper;

/**
 * One stop shopping for prefs, this is the one place that initializes all the prefs for any Specify application
 * 
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class SpecifyAppPrefs
{
    private static final String GL_HYWX    = "GEOLocate.HYWX";
    private static final String GL_WTRBODY = "GEOLocate.WATERBODY";
    
    private static boolean isInited        = false;
    private static boolean skipRemotePrefs = false;
    
    /**
     * Singleton Constructor.
     */
    protected SpecifyAppPrefs()
    {
    }
    
    /**
     * @param skipRemotePrefs the skipRemotePrefs to set
     */
    public static void setSkipRemotePrefs(boolean skipRemotePrefs)
    {
        SpecifyAppPrefs.skipRemotePrefs = skipRemotePrefs;
    }

    /**
     * Reloads the AppPrefsCache after loading a new set of remote preferences. 
     */
    public static void reloadPrefs()
    {
        isInited = false;
        initialPrefs();
    }
    
    /**
     * Initialize the prefs.
     */
    public static void initialPrefs()
    {
        if (!isInited)
        {
            AppPrefsCache.reset();
            
            if (!SpecifyAppPrefs.skipRemotePrefs)
            {
                AppPreferences.getRemote().load(); // Loads prefs from the database
            }
            
            if (AppContextMgr.getInstance().hasContext())
            {
                boolean isFish = Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish);
                if (isFish)
                {
                    if (AppPreferences.getRemote().getBoolean(GL_HYWX, null) == null)
                    {
                        AppPreferences.getRemote().putBoolean(GL_HYWX, true);
                    }
                
                    if (AppPreferences.getRemote().getBoolean(GL_WTRBODY, null) == null)
                    {
                        AppPreferences.getRemote().putBoolean(GL_WTRBODY, true);
                    }
                }
            }
            loadColorAndFormatPrefs();
            isInited = true;
        }
    }
    
    /**
     * Loads Default preferences into the AppPrefsCache.
     */
    public static void loadColorAndFormatPrefs()
    {
        //FastDateFormat fastDateFormat = FastDateFormat.getDateInstance(FastDateFormat.SHORT);      
        AppPrefsCache.register(AppPrefsCache.getDefaultDatePattern(), "ui", "formatting", "scrdateformat");
        
        ColorWrapper valtextcolor = new ColorWrapper(Color.RED);
        AppPrefsCache.register(valtextcolor, "ui", "formatting", "valtextcolor");
        
        ColorWrapper requiredFieldColor = new ColorWrapper(215, 230, 253);
        AppPrefsCache.register(requiredFieldColor, "ui", "formatting", "requiredfieldcolor");
       
        ColorWrapper viewFieldColor = new ColorWrapper(250, 250, 250);
        AppPrefsCache.register(viewFieldColor, "ui", "formatting", "viewfieldcolor");
    }
}
