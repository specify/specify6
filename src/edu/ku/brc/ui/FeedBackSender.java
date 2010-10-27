/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.getInstall4JInstallString;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;


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
            HttpClient httpClient = new HttpClient();
            httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
            
            PostMethod postMethod = new PostMethod(getSenderURL());
            
            // get the POST parameters (which includes usage stats, if we're allowed to send them)
            NameValuePair[] postParams = createPostParameters(item);
            postMethod.setRequestBody(postParams);
            
            // connect to the server
            try
            {
                httpClient.executeMethod(postMethod);
                
                // get the server response
                String responseString = postMethod.getResponseBodyAsString();
                
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
            postParams.add(new NameValuePair("bug",         item.getBug())); //$NON-NLS-1$
            postParams.add(new NameValuePair("class_name",  item.getClassName())); //$NON-NLS-1$
            postParams.add(new NameValuePair("comments",    item.getComments())); //$NON-NLS-1$
            postParams.add(new NameValuePair("stack_trace", item.getStackTrace())); //$NON-NLS-1$
            postParams.add(new NameValuePair("task_name",   item.getTaskName())); //$NON-NLS-1$
            postParams.add(new NameValuePair("title",       item.getTitle())); //$NON-NLS-1$
            
            // get the install ID
            String installID = UsageTracker.getInstallId();
            postParams.add(new NameValuePair("id", installID)); //$NON-NLS-1$
    
            Runtime runtime    = Runtime.getRuntime();
            Long    usedMemory = runtime.maxMemory() - (runtime.totalMemory () + runtime.freeMemory ());
            Long    maxMemory = runtime.maxMemory();
            
            // get the OS name and version
            postParams.add(new NameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new NameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new NameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new NameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$ //$NON-NLS-2$
            postParams.add(new NameValuePair("max_memory",   maxMemory.toString())); //$NON-NLS-1$
            postParams.add(new NameValuePair("used_memory",  usedMemory.toString())); //$NON-NLS-1$
            
            Properties props = item.getProps();
            if (props != null)
            {
                for (Object key : props.keySet())
                {
                    postParams.add(new NameValuePair(key.toString(),  props.getProperty(key.toString()))); //$NON-NLS-1$
                }
            }
            
            //if (!UIRegistry.isRelease()) // For Testing Only
            {
                postParams.add(new NameValuePair("user_name", System.getProperty("user.name"))); //$NON-NLS-1$
                try 
                {
                    postParams.add(new NameValuePair("ip", InetAddress.getLocalHost().getHostAddress())); //$NON-NLS-1$
                } catch (UnknownHostException e) {}
            }
            
            String install4JStr = getInstall4JInstallString();
            if (StringUtils.isEmpty(install4JStr))
            {
                install4JStr = "Unknown"; 
            }
            postParams.add(new NameValuePair("app_version", install4JStr)); //$NON-NLS-1$
            
            Vector<NameValuePair> extraStats = collectionSecondaryInfo();
            if (extraStats != null)
            {
                postParams.addAll(extraStats);
            }
            
            // create an array from the params
            NameValuePair[] paramArray = new NameValuePair[postParams.size()];
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
     * @return list of http named value pairs
     */
    protected Vector<NameValuePair> collectionSecondaryInfo()
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
