/* Copyright (C) 2017, University of Kansas Center for Research
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
package edu.ku.brc.helpers;

import edu.ku.brc.af.prefs.AppPreferences;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 1, 2010
 *
 */
public class ProxyHelper
{
    private static final Logger log                = Logger.getLogger(ProxyHelper.class);

    public static final String PROXY_HOST          = "PROXY_HOST";
    public static final String PROXY_PORT          = "PROXY_PORT";
    public static final String PROXY_HOST_HTTPS    = "PROXY_HOST_HTTPS";
    public static final String PROXY_PORT_HTTPS    = "PROXY_PORT_HTTPS";
    public static final String PROXY_TYPE          = "PROXY_TYPE";

    /**
     * @param proxyHost
     * @param proxyPort
     * @param proxyHostHttps
     * @param proxyPortHttps
     * @param doPrefsAlso
     */
    public static void registerProxy(final String proxyHost,
                                     final String proxyPort,
                                     final String proxyHostHttps,
                                     final String proxyPortHttps,
                                     final boolean doPrefsAlso)
    {
        setSysProp("proxySet", "true");
        setSysProp("proxyHost", proxyHost);
        setSysProp("proxyPort", proxyPort);

        setSysProp("http.proxyHost", proxyHost);
        setSysProp("http.proxyPort", proxyPort);
        
        setSysProp("https.proxyHost", proxyHostHttps); 
        setSysProp("https.proxyPort", proxyPortHttps);
        
        if (doPrefsAlso)
        {
            setProp(PROXY_HOST, proxyHost);
            setProp(PROXY_PORT, proxyPort);
            setProp(PROXY_HOST_HTTPS, proxyHostHttps);
            setProp(PROXY_PORT_HTTPS, proxyPortHttps);
        }
    }
    
    
    /**
     * @param name
     * @param value
     */
    private static void setSysProp(final String name, final String value)
    {
        if (value != null)
        {
            System.setProperty(name, value);
        } else
        {
            System.clearProperty(name);
        }
    }

    
    /**
     * @param name
     * @param value
     */
    private static void setProp(final String name, final String value)
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        if (value != null)
        {
            localPrefs.put(name, value);
        } else
        {
            localPrefs.remove(name);
        }
    }
    
    /**
     * @param doPrefsAlso
     */
    public static void clearProxySettings(final boolean doPrefsAlso)
    {
        registerProxy(null, null, null, null, true);
    }
    
    /**
     * 
     */
    public static void setProxySettingsFromPrefs()
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        registerProxy(localPrefs.get(PROXY_HOST, null), 
                      localPrefs.get(PROXY_PORT, null), 
                      localPrefs.get(PROXY_HOST_HTTPS, null),  
                      localPrefs.get(PROXY_PORT_HTTPS, null), 
                      false);
    }

    public static void applyProxySettings(HttpClient httpClient) {
        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost != null) {
            Integer proxyPort = null;
            try {
                proxyPort = Integer.valueOf(System.getProperty("http.proxyPort"));
            } catch (Exception e) {
                //disregard stupid port
                log.warn("invalid proxy port. defaulting to 3128.");
                proxyPort = 3128;
            }
            HostConfiguration hc = new HostConfiguration();
            hc.setProxy(proxyHost, proxyPort);
            httpClient.setHostConfiguration(hc);
        }
    }

}
