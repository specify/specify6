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
package edu.ku.brc.helpers;

/////////////////////////////////////////////////////////

//
// Version 1.5
//
// December 10, 2005
//
// Supports: Mac OS X, GNU/Linux, Unix, Windows XP
// // Example Usage:
//
// String url = "http://www.centerkey.com/";
//
// BrowserLauncher.openURL(url);
//
// Public Domain Software -- Free to Use as You Like
//
/////////////////////////////////////////////////////////
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import edu.ku.brc.ui.UIHelper;

/**
 * Taken from the Web (Bare Bones Launcher). This is a cross platform approach for launching browsers from java Apps
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class BrowserLauncher
{

    private static final String errMsg = getResourceString("BrowserLauncher.ERROR_LAUNCH_BROWSER");  //$NON-NLS-1$

    /**
     * Opens an URL in a browser.
     * @param url the url to open
     */
    public static void openURL(final String url)
    {
        UIHelper.OSTYPE osType = UIHelper.getOSType();
        try
        {
            if (osType == UIHelper.OSTYPE.MacOSX)
            {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager"); //$NON-NLS-1$
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class<?>[] { String.class }); //$NON-NLS-1$
                openURL.invoke(null, new Object[] { url });

            } else if (osType == UIHelper.OSTYPE.Windows)
            {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url); //$NON-NLS-1$

            } else
            { // assume Unix or Linux
                String[] browsers = { "firefox", "mozilla", "epiphany", "opera", "konqueror", "netscape" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                {
                    if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0) //$NON-NLS-1$
                    {
                        browser = browsers[count];
                    }
                }
                if (browser == null)
                {
                    throw new Exception("Could not find web browser"); //$NON-NLS-1$
                }
                Runtime.getRuntime().exec(new String[] { browser, url });
            }
        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BrowserLauncher.class, e);
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage()); //$NON-NLS-1$
        }
    }

}
