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
package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class sends usage stats.
 * 
 * @author jstewart, rods
 * 
 * @code_status Complete
 */
public class StatsTrackerTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(StatsTrackerTask.class);
    
    public  static final String           STATS_TRACKER   = "StatsTracker"; //$NON-NLS-1$
    
    protected StatsSwingWorker<?, ?> worker;
    protected boolean                isSendSecondaryStatsAllowed  = false;

    /**
     * Constructor.
     */
    public StatsTrackerTask()
    {
        super(STATS_TRACKER, getResourceString(STATS_TRACKER));
        
        if (UIRegistry.isTesting())
        {
            CommandDispatcher.register(DB_CMD_TYPE, this);
        }
    }
    
    /**
     * Cosntructor.
     * 
     * @param name the name of the task
     * @param title the user-viewable name of the task
     */
    public StatsTrackerTask(final String name, final String title)
    {
        super(name, title);
    }

    /**
     * @param isSendSecondaryStatsAllowed the isSendSecondaryStatsAllowed to set
     */
    public void setSendSecondaryStatsAllowed(boolean isSendSecondaryStatsAllowed)
    {
        this.isSendSecondaryStatsAllowed = isSendSecondaryStatsAllowed;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
    }
    
    /**
     * @param localizedMsg
     * @return
     */
    protected void showClosingFrame()
    {
        // no op
    }
    
    /**
     * @return
     */
    protected PropertyChangeListener getPCLForWorker()
    {
        return null;
    }
    
    /**
     * 
     */
    protected boolean starting()
    {
        return true;
    }
    
    /**
     * 
     */
    protected void completed()
    {
     // no op
    }
    
    /**
     * When it is done sending the statistics: if doExit is true it will shutdown the Hibernate and the DBConnections
     * and call System.exit(0), if doSendDoneEvent is true then it send a CommandAction(APP_CMD_TYPE, "STATS_SEND_DONE", null)
     * for someone else to shut everything down.
     * @param doExit call exit
     * @param doSilent don't show any UI while sending stats
     * @param doSendDoneEvent send a STATS_SEND_DONE command on the UI thread.
     */
    public void sendStats(final boolean doExit, final boolean doSilent, final boolean doSendDoneEvent)
    {
        if (!doSilent)
        {
            showClosingFrame();
        }
        
        if (starting())
        {
            worker = new StatsSwingWorker<Object, Object>()
            {
                @Override
                protected Object doInBackground() throws Exception
                {
                    sendStats();
                    
                    return null;
                }
    
                @Override
                protected void done()
                {
                    super.done();
                    
                    completed();
                    
                    if (doExit)
                    {
                        AppPreferences.shutdownAllPrefs();
                        DataProviderFactory.getInstance().shutdown();
                        DBConnection.shutdown();
                        System.exit(0);
                        
                    } else if (doSendDoneEvent)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(500);
                                } catch (Exception ex) {}
                                CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, "STATS_SEND_DONE", null));
                            }
                        });
                    }
                }
                
            };
            
            if (!doSilent)
            {
                PropertyChangeListener pcl = getPCLForWorker();
                if (pcl != null)
                {
                    worker.addPropertyChangeListener(pcl);
                }
            }
            worker.execute();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        // this task has no visible panes
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        // this task has no toolbar items
        return null;
    }


    /**
     * Connect to the update/usage tracking server now.  If this method is called based on the action of a user
     * on a UI widget (menu item, button, etc) a popup will ALWAYS be displayed as a result, showing updates available
     * (even if none are) or an error message.  If this method is called as part of an automatic update check or 
     * automatic usage stats connection, the error message will never be displayed.  Also, in that case, the list
     * of updates will only be displayed if at least one update is available AND the user has enabled auto update checking.
     * 
     * @param isUserInitiatedUpdateCheck if true, the user initiated this process through a UI option
     */
    protected void connectToServerNow(final boolean isUserInitiatedUpdateCheck)
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
                    sendStats();
                    return null;
                }
                catch (Exception e)
                {
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, e);
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
     * Connects to the update/usage tracking server to get the latest version info and/or send the usage stats.
     * 
     * @throws Exception if an IO error occured or the response couldn't be parsed
     */
    protected void sendStats() throws Exception
    {
        sendStats(getVersionCheckURL(), createPostParameters(isSendSecondaryStatsAllowed), getClass().getName());
    }
    
    /**
     * Connects to the update/usage tracking server to get the latest version info and/or send the usage stats.
     * @param url
     * @param postParams
     * @param userAgentName
     * @throws Exception if an IO error occurred or the response couldn't be parsed
     */
    public static void sendStats(final String url, 
                                 final Vector<NameValuePair> postParams, 
                                 final String userAgentName) throws Exception
    {
//        System.out.println("--------------------");
//        for (NameValuePair nvp : postParams)
//        {
//            System.out.println(String.format("[%s][%s]", nvp.getName(), nvp.getValue()));
//        }
//        System.out.println("--------------------");
        // If the user changes collection before it gets a chance to send the stats
        //if (!AppContextMgr.getInstance().hasContext()) return;
        
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", userAgentName); //$NON-NLS-1$
        
        PostMethod postMethod = new PostMethod(url);
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        postMethod.setRequestBody(buildNamePairArray(postParams));
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
            
            // get the server response
            @SuppressWarnings("unused")
            String responseString = postMethod.getResponseBodyAsString();
            
            //if (StringUtils.isNotEmpty(responseString))
            //{
            //    System.err.println(responseString);
            //}

        } catch (java.net.UnknownHostException ex)
        {
            log.debug("Couldn't reach host.");
            
        } catch (Exception e)
        {
            //e.printStackTrace();
            //UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, e);
            throw new ConnectionException(e);
        }
    }

    /**
     * Gets the URL of the version checking / usage tracking server.
     * 
     * @return the URL string
     */
    protected String getVersionCheckURL()
    {
        String baseURL = getResourceString("StatsTrackerTask.URL"); //$NON-NLS-1$
        return baseURL;
    }
    
    /**
     * Adds the string value or an empty string.
     * @param value the value to be added.
     * @return the new string
     */
    protected static String fixParam(final String value)
    {
        return value == null ? "" : value;
    }
    
    /**
     * Adds the string value or an empty string.
     * @param value the value to be added.
     * @return the new string
     */
    protected static String fixParam(final Object value)
    {
        return value == null ? "" : value.toString();
    }
    
    /**
     * Executes a Query that returns a count and adds to the return parameters of the http request
     * @param statName the name of the http param
     * @param statsList the list the stat is to be added to
     * @param sql the SQL statement to be executed
     * @return the value of the count query
     */
    protected int addStat(final String statName, final Vector<NameValuePair> statsList, final String sql)
    {
        Integer count = 0;
        if (sql != null)
        {
            count = BasicSQLUtils.getCount(sql);
            if (count != null)
            {
                statsList.add(new NameValuePair(statName, Integer.toString(count)));
            } else
            {
                return 0;
            }
        }
        return count;
    }
    
    /**
     * Collection Statistics about the Collection (synchronously).
     * @return list of http named value pairs
     */
    protected Vector<NameValuePair> collectSecondaryStats(@SuppressWarnings("unused") final boolean doSendSecondaryStats)
    {
        return null;
    }
    
    /**
     * Builds NamePair array from list.
     * @param postParams the list 
     * @return the array
     */
    public static NameValuePair[] buildNamePairArray(final Vector<NameValuePair> postParams)
    {
        // create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }
        return paramArray;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param doSendSecondaryStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected Vector<NameValuePair> createPostParameters(final boolean doSendSecondaryStats)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        try
        {
            // get the install ID
            String installID = UsageTracker.getInstallId();
            postParams.add(new NameValuePair("id", installID)); //$NON-NLS-1$
    
            //get ISA number
            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            try 
            {
            	postParams.add(new NameValuePair("ISA_number",   collection.getIsaNumber())); //$NON-NLS-1$
            } catch (NullPointerException e) {}
            
            // get the OS name and version
            postParams.add(new NameValuePair("os_name",      System.getProperty("os.name"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("os_version",   System.getProperty("os.version"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("java_version", System.getProperty("java.version"))); //$NON-NLS-1$
            postParams.add(new NameValuePair("java_vendor",  System.getProperty("java.vendor"))); //$NON-NLS-1$
            
            //if (!UIRegistry.isRelease()) // For Testing Only
            {
                postParams.add(new NameValuePair("user_name", System.getProperty("user.name"))); //$NON-NLS-1$
                try 
                {
                    postParams.add(new NameValuePair("ip", InetAddress.getLocalHost().getHostAddress())); //$NON-NLS-1$
                } catch (UnknownHostException e) {}
            }
            
            postParams.add(new NameValuePair("tester", AppPreferences.getLocalPrefs().getBoolean("tester", false) ? "true" : "false")); //$NON-NLS-1$
            
            String resAppVersion = UIRegistry.getAppVersion();
            if (StringUtils.isEmpty(resAppVersion))
            {
                resAppVersion = "Unknown"; 
            }
            postParams.add(new NameValuePair("app_version", resAppVersion)); //$NON-NLS-1$
            
            Vector<NameValuePair> extraStats = collectSecondaryStats(doSendSecondaryStats);
            if (extraStats != null)
            {
                postParams.addAll(extraStats);
            }
            
            // get all of the usage tracking stats
            List<Pair<String, Integer>> statistics = UsageTracker.getUsageStats();
            for (Pair<String, Integer> stat : statistics)
            {
                postParams.add(new NameValuePair(stat.first, Integer.toString(stat.second)));
            }
            
            return postParams;
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, ex);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (UIRegistry.isTesting() && cmdAction.isType(DB_CMD_TYPE))
        {
            Object dataObj = cmdAction.getData();
            if (dataObj != null)
            {
                UsageTracker.incrUsageCount("DB."+cmdAction.getAction()+"."+dataObj.getClass().getSimpleName());
            }
        }
        super.doCommand(cmdAction);
    }
    
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }
    
    //--------------------------------------------------------------------------
    //-- 
    //--------------------------------------------------------------------------
    public abstract class StatsSwingWorker<T, V> extends javax.swing.SwingWorker<T, V>
    {
        public void setProgressValue(final int value)
        {
            setProgress(value);
        }
    }
    
    //--------------------------------------------------------------------------
    //-- 
    //--------------------------------------------------------------------------
    public static class ConnectionException extends IOException
    {
        public ConnectionException(@SuppressWarnings("unused") Throwable e)
        {
            super();
        }
    }
}
