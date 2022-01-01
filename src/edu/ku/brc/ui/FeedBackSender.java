/* Copyright (C) 2022, Specify Collections Consortium
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
package edu.ku.brc.ui;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.ProxyHelper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Vector;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;


/**
 * A generic 'base' dialog that can be used to collect information and send to a web service.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public abstract class FeedBackSender
{
    private static final Logger log = Logger.getLogger(FeedBackSender.class);
    
    /**
     * 
     */
    public FeedBackSender()
    {
    }
    
    /**
     * @param cls
     * @param exception
     */
    public void capture(final Class<?> cls, 
                        final Exception exception)
    {
        connectToServerNow(getFeedBackSenderItem(cls, exception));
    }
    
    /**
     * @param cls
     * @param exception
     */
    public void capture(final Class<?> cls, 
                        final String message,
                        final Exception exception)
    {
        connectToServerNow(getFeedBackSenderItem(cls, message, exception));
    }
    
    /**
     * @param cls
     * @param exception
     */
    public void sendMsg(final Class<?> cls, 
                        final String message,
                        final Exception exception)
    {
        FeedBackSenderItem item = getFeedBackSenderItem(cls, message, exception);
        item.setIncludeEmail(false);
        connectToServerNow(item);
    }
    
    /**
     * 
     */
    public void sendFeedback()
    {
        connectToServerNow(getFeedBackSenderItem(null, null));
    }
    
    /**
     * @param cls
     * @param exception
     * @return
     */
    protected FeedBackSenderItem getFeedBackSenderItem(final Class<?> cls, final Exception exception)
    {
        return getFeedBackSenderItem(cls, null, exception);
    }
    
    /**
     * @param cls
     * @param exception
     * @return
     */
    protected FeedBackSenderItem getFeedBackSenderItem(final Class<?> cls, final String comments, final Exception exception)
    {
        FeedBackSenderItem item = new FeedBackSenderItem();
        item.setStackTrace(exception != null ? exception.getMessage() : null);
        item.setClassName(cls != null ? cls.getName() : null);
        item.setComments(comments);
        return item;
    }
    
    /**
     * @return the url that the info should be sent to
     */
    protected abstract String getSenderURL();
    
    /**
     * @param item
     * @throws Exception
     */
    protected void send(final FeedBackSenderItem item) throws Exception
    {
        if (item != null)
        {
            // check the website for the info about the latest version
            CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent(getClass().getName())
                .build();
            //httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$

            HttpPost postMethod = new HttpPost(getSenderURL());
            
            // get the POST parameters (which includes usage stats, if we're allowed to send them)
            //https://stackoverflow.com/questions/9362427/adding-parameter-to-httppost-on-apaches-httpclient
            NameValuePair[] postParams = createPostParameters(item);
            postMethod.setEntity(new UrlEncodedFormEntity(Arrays.asList(postParams), StandardCharsets.UTF_8));
            ProxyHelper.applyProxySettings(postMethod, null);

            // connect to the server
            try
            {
                CloseableHttpResponse response = httpClient.execute(postMethod);
                String responseString = EntityUtils.toString(response.getEntity());

                if (StringUtils.isNotEmpty(responseString))
                {
                    System.err.println(responseString);
                }
    
            }
            catch (java.net.UnknownHostException uex)
            {
                log.error(uex.getMessage());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param item the item to fill
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters(final FeedBackSenderItem item)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        try
        {
            postParams.add(new BasicNameValuePair("bug",         item.getBug())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("class_name",  item.getClassName())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("comments",    item.getComments())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("stack_trace", item.getStackTrace())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("task_name",   item.getTaskName())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("title",       item.getTitle())); //$NON-NLS-1$
            
            // get the install ID
            String installID = UsageTracker.getInstallId();
            postParams.add(new BasicNameValuePair("id", installID)); //$NON-NLS-1$
    
            Runtime runtime    = Runtime.getRuntime();
            Long    usedMemory = runtime.maxMemory() - (runtime.totalMemory () + runtime.freeMemory ());
            Long    maxMemory = runtime.maxMemory();
            
            // get the OS name and version
            postParams.add(new BasicNameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new BasicNameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new BasicNameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new BasicNameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new BasicNameValuePair("max_memory",   maxMemory.toString())); //$NON-NLS-1$
            postParams.add(new BasicNameValuePair("used_memory",  usedMemory.toString())); //$NON-NLS-1$
            
            Properties props = item.getProps();
            if (props != null)
            {
                for (Object key : props.keySet())
                {
                    postParams.add(new BasicNameValuePair(key.toString(),  props.getProperty(key.toString()))); //$NON-NLS-1$
                }
            }
            
            //if (!UIRegistry.isRelease()) // For Testing Only
            {
                postParams.add(new BasicNameValuePair("user_name", System.getProperty("user.name"))); //$NON-NLS-1$
                try 
                {
                    postParams.add(new BasicNameValuePair("ip", InetAddress.getLocalHost().getHostAddress())); //$NON-NLS-1$
                } catch (UnknownHostException e) {}
            }
            
            String resAppVersion = UIRegistry.getAppVersion();
            if (StringUtils.isEmpty(resAppVersion))
            {
                resAppVersion = "Unknown"; 
            }
            postParams.add(new BasicNameValuePair("app_version", resAppVersion)); //$NON-NLS-1$
            
            Vector<NameValuePair> extraStats = collectionSecondaryInfo(item);
            if (extraStats != null)
            {
                postParams.addAll(extraStats);
            }
            
            // create an array from the params
            NameValuePair[] paramArray = new BasicNameValuePair[postParams.size()];
            for (int i = 0; i < paramArray.length; ++i)
            {
                paramArray[i] = postParams.get(i);
            }
            
            return paramArray;
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Collection Statistics about the Collection (synchronously).
     * @param item feedback item
     * @return list of http named value pairs
     */
    protected Vector<NameValuePair> collectionSecondaryInfo(@SuppressWarnings("unused") final FeedBackSenderItem item)
    {
        return null;
    }
    
    /**
     * Connect to the update/usage tracking server now.  If this method is called based on the action of a user
     * on a UI widget (menu item, button, etc) a popup will ALWAYS be displayed as a result, showing updates available
     * (even if none are) or an error message.  If this method is called as part of an automatic update check or 
     * automatic usage stats connection, the error message will never be displayed.  Also, in that case, the list
     * of updates will only be displayed if at least one update is available AND the user has enabled auto update checking.
     */
    protected void connectToServerNow(final FeedBackSenderItem item)
    {
        if (item != null)
        {
            // Create a SwingWorker to connect to the server in the background, then show results on the Swing thread
            SwingWorker workerThread = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    // connect to the server, sending usage stats if allowed, and gathering the latest modules version info
                    try
                    {
                        send(item);
                        return null;
                    }
                    catch (Exception e)
                    {
                        //UsageTracker.incrHandledUsageCount();
                        //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FeedBackSender.class, e);
                        // if any exceptions occur, return them so the finished() method can have them
                        e.printStackTrace();
                        return e;
                    }
                }
                
                @Override
                public void finished()
                {
                }
            };
            
            // start the background task
            workerThread.start();
        }
    }
}
