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
package edu.ku.brc.af.core;


import java.lang.ref.WeakReference;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;


/**
 * Needed for Mac Integration

 * @code_status Complete
 **
 * @author rods
 *
 */
public class MacOSAppHandler extends Application
{
    protected WeakReference<FrameworkAppIFace> app;

    public MacOSAppHandler(final FrameworkAppIFace app)
    {
        this.app = new WeakReference<FrameworkAppIFace>(app);

        addApplicationListener(new AppHandler());

        setEnabledPreferencesMenu(true);
    }

    class AppHandler extends ApplicationAdapter
    {
        public void handleAbout(ApplicationEvent event)
        {
            app.get().doAbout();
            event.setHandled(true);
        }

        public void handleAppPrefsMgr(ApplicationEvent event)
        {
            app.get().doPreferences();
            event.setHandled(true);
        }
        
        public void handlePreferences(ApplicationEvent event) 
        {
            app.get().doPreferences();
            event.setHandled(true);
        }

        public void handleQuit(ApplicationEvent event)
        {
            app.get().doExit(true);
            event.setHandled(false);  // This is so bizarre that this needs to be set to false
                                      // It seems to work backwards compared to the other calls
         }
    }

}
