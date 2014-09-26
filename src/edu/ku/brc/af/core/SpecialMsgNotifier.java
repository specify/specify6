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

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.lang.StringUtils;
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
                            UIRegistry.showLocalizedError("NO_INTERNET");
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
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);

        PostMethod postMethod = new PostMethod(url);
        
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        
        postParams.add(new NameValuePair("id", id)); //$NON-NLS-1$
        
        String resAppVersion = UIRegistry.getAppVersion();
        resAppVersion = StringUtils.isEmpty(resAppVersion) ? "Unknown" : resAppVersion;

        // get the OS name and version
        postParams.add(new NameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$
        postParams.add(new NameValuePair("app_version",  UIRegistry.getAppVersion())); //$NON-NLS-1$
        
        // create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }

         postMethod.setRequestBody(paramArray);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
            
            int status = postMethod.getStatusCode();
            if (status == 200)
            {
                // get the server response
                String responseString = postMethod.getResponseBodyAsString();
                
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
