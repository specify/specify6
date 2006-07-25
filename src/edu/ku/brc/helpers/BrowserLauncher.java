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
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

/**
 * Taken from the Web (Bare Bones Launcher). This is a cross platform approach for launching browsers from java Apps
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class BrowserLauncher
{

    private static final String errMsg = "Error attempting to launch web browser";

    /**
     * Opens an URL in a browser
     * @param url the url to open
     */
    public static void openURL(String url)
    {
        String osName = System.getProperty("os.name");
        try
        {
            if (osName.startsWith("Mac OS"))
            {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
                openURL.invoke(null, new Object[] { url });

            } else if (osName.startsWith("Windows"))
            {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);

            } else
            { // assume Unix or Linux
                String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                {
                    if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                    {
                        browser = browsers[count];
                    }
                }
                if (browser == null)
                {
                    throw new Exception("Could not find web browser");
                } else
                {
                    Runtime.getRuntime().exec(new String[] { browser, url });
                }
            }
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }

}
