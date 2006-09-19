/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify;

import java.awt.Color;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.FastDateFormat;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.ColorWrapper;

/**
 * One stop shopping for prefs, this is the one place that initializes all the prefs for any Specify application
 * 
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class SpecifyAppPrefs
{

    
    /**
     * Singleton Constructor.
     */
    protected SpecifyAppPrefs()
    {
    }
    
    /**
     * Initialize the prefs.
     */
    public static void initialPrefs()
    {
        AppPrefsCache.reset();
        
        AppPreferences.getRemote().load(); // Loads prefs from the database
        
        FastDateFormat fastDateFormat = FastDateFormat.getDateInstance(FastDateFormat.SHORT);      
        SimpleDateFormat screenDateFormat = new SimpleDateFormat(fastDateFormat.getPattern());
        AppPrefsCache.register(screenDateFormat, "ui", "formatting", "scrdateformat");
        
        ColorWrapper valtextcolor = new ColorWrapper(Color.RED);
        AppPrefsCache.register(valtextcolor, "ui", "formatting", "valtextcolor");
        
        ColorWrapper requiredFieldColor = new ColorWrapper(215, 230, 253);
        AppPrefsCache.register(requiredFieldColor, "ui", "formatting", "requiredfieldcolor");
       
        ColorWrapper viewFieldColor = new ColorWrapper(250, 250, 250);
        AppPrefsCache.register(viewFieldColor, "ui", "formatting", "viewfieldcolor");
       
    }
    

}
