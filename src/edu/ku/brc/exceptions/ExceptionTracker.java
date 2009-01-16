/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.exceptions;

import static edu.ku.brc.ui.UIHelper.getInstall4JInstallString;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.ui.UIRegistry;


/**
 * Used to tracker and send Handled Exceptions.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class ExceptionTracker
{
    public static final String factoryName = "edu.ku.brc.exceptions.ExceptionTracker"; //$NON-NLS-1$

    protected static ExceptionTracker instance = null;
    
    /**
     * 
     */
    public ExceptionTracker()
    {
    }
    
    /**
     * @param cls
     * @param exception
     */
    public void capture(final Class<?> cls, 
                        final Exception exception)
    {
        
        connectToServerNow(getExceptionItem(cls, exception));
    }
    
    /**
     * @param cls
     * @param exception
     * @return
     */
    protected ExceptionItem getExceptionItem(final Class<?> cls, final Exception exception)
    {
        ExceptionItem item = new ExceptionItem();
        item.setStackTrace(exception.getMessage());
        item.setClassName(cls.getName());
        return item;
    }
    
    /**
     * @param item
     * @throws Exception
     */
    protected void send(final ExceptionItem item) throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        //PostMethod postMethod = new PostMethod("http://localhost/cgi-bin/exception.php");
        PostMethod postMethod = new PostMethod("http://specify6-test.nhm.ku.edu/exception.php");
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters(item);
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
            
            // get the server response
            /*String responseString = postMethod.getResponseBodyAsString();
            
            if (StringUtils.isNotEmpty(responseString))
            {
                System.err.println(responseString);
            }*/

        }
        catch (Exception e)
        {
            e.printStackTrace();
            //UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExceptionTracker.class, e);
            //throw new ConnectionException(e);
        }
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param doSendSecondaryStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters(final ExceptionItem item)
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
    
            // get the OS name and version
            postParams.add(new NameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$
            
            if (!UIRegistry.isRelease()) // For Testing Only
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
            
            Vector<NameValuePair> extraStats = collectSecondaryStats();
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
    protected Vector<NameValuePair> collectSecondaryStats()
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
    protected void connectToServerNow(final ExceptionItem item)
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
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExceptionTracker.class, e);
                    // if any exceptions occur, return them so the finished() method can have them
                    return e;
                }
            }
            
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            @Override
            public void finished()
            {
            }
        };
        
        // start the background task
        workerThread.start();
    }

    /**
     * Returns the instance of the ExceptionTracker.
     * @return the instance of the ExceptionTracker.
     */
    public static ExceptionTracker getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = (ExceptionTracker)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate ExceptionTracker factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new ExceptionTracker();
    }

}
