/*
 * Filename:    $RCSfile: MacOSAppHandler.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2006/05/25 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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


import java.lang.ref.WeakReference;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Needed for Mac Integration
 *
 * @author rods
 *
 */
public class MacOSAppHandler extends Application
{
    protected WeakReference app;

    public MacOSAppHandler(final Specify app)
    {
        this.app = new WeakReference<Specify>(app);

        addApplicationListener(new AppHandler());

        setEnabledPreferencesMenu(true);
    }

    class AppHandler extends ApplicationAdapter
    {
        public void handleAbout(ApplicationEvent event)
        {
            ((Specify)app.get()).doAbout();
            event.setHandled(true);
        }

        public void handlePreferences(ApplicationEvent event)
        {
            ((Specify)app.get()).preferences();
            event.setHandled(true);
        }

        public void handleQuit(ApplicationEvent event)
        {
            ((Specify)app.get()).doExit();
            event.setHandled(true);
         }
    }
    
}
