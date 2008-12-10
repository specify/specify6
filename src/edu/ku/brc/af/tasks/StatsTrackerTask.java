package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIHelper;
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
     * @param doExit
     * @param doSilent
     */
    public void sendStats(final boolean doExit, final boolean doSilent)
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
                        System.exit(0);
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
     * @return a list of modules that have a different version number than the currently installed software
     * @throws Exception if an IO error occured or the response couldn't be parsed
     */
    protected void sendStats() throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getVersionCheckURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters(isSendSecondaryStatsAllowed);
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            httpClient.executeMethod(postMethod);
        }
        catch (Exception e)
        {
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
     * Executes a Query that returns a count and adds to the return parameters of the http request
     * @param statName the name of the http param
     * @param statsList the list the stat is to be added to
     * @param sql the SQL statement to be executed
     * @return the value of the count query
     */
    protected int addStat(final String statName, final Vector<NameValuePair> statsList, final String sql)
    {
        int count = 0;
        if (sql != null)
        {
            count = BasicSQLUtils.getCount(sql);
            statsList.add(new NameValuePair(statName, Integer.toString(count)));
        }
        return count;
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
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param doSendSecondaryStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters(final boolean doSendSecondaryStats)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        try
        {
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
                //try 
                //{
                //    postParams.add(new NameValuePair("ip", InetAddress.getLocalHost().getHostAddress())); //$NON-NLS-1$
                //} catch (UnknownHostException e) {}
            }
            
            String install4JStr = UIHelper.getInstall4JInstallString();
            if (StringUtils.isEmpty(install4JStr))
            {
                install4JStr = "Unknown"; 
            }
            postParams.add(new NameValuePair("app_version", install4JStr)); //$NON-NLS-1$
            
            if (doSendSecondaryStats)
            {
                Vector<NameValuePair> extraStats = collectSecondaryStats();
                if (extraStats != null)
                {
                    postParams.addAll(extraStats);
                }
            }
            
            // get all of the usage tracking stats
            List<Pair<String, Integer>> statistics = UsageTracker.getUsageStats();
            for (Pair<String, Integer> stat : statistics)
            {
                postParams.add(new NameValuePair(stat.first, Integer.toString(stat.second)));
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
