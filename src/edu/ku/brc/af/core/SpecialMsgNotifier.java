/* Copyright (C) 2021, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.ProxyHelper;
import edu.ku.brc.specify.Specify;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 12, 2010
 *
 */
public class SpecialMsgNotifier
{
    private static final Logger log = Logger.getLogger(SpecialMsgNotifier.class);
    private static AtomicBoolean blockMsg = new AtomicBoolean(false);
    
    /**
     * 
     */
    public SpecialMsgNotifier()
    {
        super();
    }

    
    public void checkForMessages()
    {
        if (AppPreferences.getLocalPrefs().getBoolean("DisableMessageCheckAtStartUp", false)) {
            log.info("DisableMessageCheckAtStartUp is true. Skipping message check.");
            return;
        }
        if (blockMsg.get())
        {
            return;
        }
        
        SwingWorker<Integer, Integer> msgCheckWorker = new SwingWorker<Integer, Integer>()
        {
            protected String msg = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                if (!blockMsg.get())
                {
                    try
                    {
                        Thread.sleep(15000); // 15 seconds
                        
                        String url       = UIRegistry.getResourceString("CGI_BASE_URL") + "/getmsg2.php";
                        String installID = UsageTracker.getInstallId();
                        
                        msg = send(url, installID);
                        msg = StringUtils.deleteWhitespace(msg);
                        
                    } catch (Exception ex)
                    {
                        // die silently
                    }      
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                if (msg != null)
                {
                    //System.out.println("["+msg+"]");
                    
                    if (StringUtils.isNotEmpty(msg) && !StringUtils.contains(msg, "NOMSG"))
                    {
                        String header = msg.length() > 6 ? msg.substring(0, 7).toUpperCase() : "";
                        if (header.startsWith("<HTML>"))
                        {
                            log.warn("Unable to connect to specify msg server");
                        } else
                        {
                            UIRegistry.showError(JOptionPane.WARNING_MESSAGE, msg);
                        }
                    }
                }
                
            }
        };
        
        msgCheckWorker.execute();
    }
    
    /**
     * @param item
     * @throws Exception
     */
    protected String send(final String url, final String id) throws Exception
    {
        // check the website for the info about the latest version
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        //ProxyHelper.applyProxySettings(httpClient);
        //httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);

        HttpPost postMethod = new HttpPost(url);
        RequestConfig.Builder requestConfig = RequestConfig.custom();
        requestConfig.setConnectTimeout(15000);
        ProxyHelper.applyProxySettings(postMethod, requestConfig);
        postMethod.setConfig(requestConfig.build());

        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        
        postParams.add(new BasicNameValuePair("id", id)); //$NON-NLS-1$
        
        String resAppVersion = UIRegistry.getAppVersion();
        resAppVersion = StringUtils.isEmpty(resAppVersion) ? "Unknown" : resAppVersion;

        // get the OS name and version
        postParams.add(new BasicNameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$
        postParams.add(new BasicNameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$
        postParams.add(new BasicNameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$
        postParams.add(new BasicNameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$
        postParams.add(new BasicNameValuePair("app_version",  UIRegistry.getAppVersion())); //$NON-NLS-1$
        
        //postMethod.setRequestBody(paramArray);
        postMethod.setEntity(new UrlEncodedFormEntity(postParams, StandardCharsets.UTF_8));
        // connect to the server
        try
        {
            CloseableHttpResponse response = httpClient.execute(postMethod);
            
            int status = response.getStatusLine().getStatusCode();
            if (status == 200)
            {
                // get the server response
                String responseString = EntityUtils.toString(response.getEntity());
                
                if (StringUtils.isNotEmpty(responseString))
                {
                    return responseString;
                }
            }
        } catch (java.net.UnknownHostException ex)
        {
            log.debug("Couldn't reach host.");
            
        } catch (Exception e)
        {
            e.printStackTrace();
            // die silently
        }
        return null;
    }

    /**
     * @param blockMsg the blockMsg to set
     */
    public static void setBlockMsg(boolean blockMsg)
    {
        SpecialMsgNotifier.blockMsg.set(blockMsg);
    }
}
