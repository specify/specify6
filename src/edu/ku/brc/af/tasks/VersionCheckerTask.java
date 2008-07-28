package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * This class provides the basic capability to compare an installed piece of software with the
 * latest available version and provide some feedback to the user.  It also provides the capability
 * to send usage stats that were collected by the {@link UsageTracker} class.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class VersionCheckerTask extends BaseTask
{
    /** A {@link Logger} for all messages emitted from this class. */
    private static final Logger log = Logger.getLogger(VersionCheckerTask.class);
    
    public static final String VERSION_CHECK = "VersionChecker"; //$NON-NLS-1$
    
    /**
     * Constructor.
     */
    public VersionCheckerTask()
    {
        super(VERSION_CHECK, getResourceString(VERSION_CHECK));
    }
    
    /**
     * Cosntructor.
     * 
     * @param name the name of the task
     * @param title the user-viewable name of the task
     */
    public VersionCheckerTask(String name, String title)
    {
        super(name, title);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        
        // setup a background task to check for udpates at startup and/or send usage stats at startup
        Timer timer = new Timer("VersionCheckingThread", true); //$NON-NLS-1$
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                // check for updates
                //    show popup if updates available
                //    don't show error popup if we can't contact the server
                //    send usage stats if allowed
                connectToServerNow(false);
            }
        };
        
        // see if either update checking or usage tracking is enabled
        boolean checkForUpdates = autoUpdateCheckIsEnabled();
        boolean sendUsageStats  = usageTrackingIsAllowed();
        
        // if either feature is enabled, schedule the background task
        if (checkForUpdates || sendUsageStats)
        {
            timer.schedule(task, 5000);
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        
        /*String label    = getResourceString("VersionCheckerTask.CHECK_NOW"); //$NON-NLS-1$
        
        JMenuItem checkNow = new JMenuItem(label);
        checkNow.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.incrUsageCount("UpdateNow"); //$NON-NLS-1$
                
                // then perform the check
                connectToServerNow(true);
            }
        });
        
        MenuItemDesc  miDesc = new MenuItemDesc(checkNow, getResourceString("VersionCheckerTask.HELP")); //$NON-NLS-1$
        
        miDesc.setPosition(MenuItemDesc.Position.Before, getResourceString("VersionCheckerTask.ABOUT")); //$NON-NLS-1$
        miDesc.setSepPosition(MenuItemDesc.Position.After);
        */
        List<MenuItemDesc> menuItems = new Vector<MenuItemDesc>();

        return menuItems;
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
     * Checks to see if usage tracking is allowed.
     * 
     * @return true if usage tracking is allowed.
     */
    protected boolean usageTrackingIsAllowed()
    {
        // see if the user is allowing us to send back usage data
        AppPreferences appPrefs  = AppPreferences.getRemote();
        Boolean        sendStats = appPrefs.getBoolean("usage_tracking.send_stats", null); //$NON-NLS-1$
        if (sendStats == null)
        {
            sendStats = true;
            appPrefs.putBoolean("usage_tracking.send_stats", sendStats); //$NON-NLS-1$
        }
        
        return sendStats;
    }

    /**
     * Checks to see if auto update checking is enabled.
     * 
     * @return true if auto update checking is turned on
     */
    protected boolean autoUpdateCheckIsEnabled()
    {
        // see if the user is allowing us to check for updates
        AppPreferences appPrefs        = AppPreferences.getRemote();
        Boolean        checkForUpdates = appPrefs.getBoolean("version_check.auto", null); //$NON-NLS-1$
        if (checkForUpdates == null)
        {
            checkForUpdates = true;
            appPrefs.putBoolean("version_check.auto", checkForUpdates); //$NON-NLS-1$
        }
        
        return checkForUpdates;
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
                    List<String> availUpdates = sendStatsAndGetUpdates();
                    return availUpdates;
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
                Object retVal = getValue();
                if (retVal != null)
                {
                    // if an exception occurred during update check...
                    if (retVal instanceof Exception)
                    {
                        // show an error popup if the user requested this update check through the menu item
                        if (isUserInitiatedUpdateCheck)
                        {
                            // there are only three types of errors that can occur
                            // 1. a connection error
                            // 2. an error parsing the reply
                            // 3. an error parsing or finding the local install_info.xml file
                            // We treat #2 and #3 as the same type of error
                            String errorString = null;
                            if (retVal instanceof ConnectionException)
                            {
                                errorString = getResourceString("VersionCheckerTask.CONNECTION_ERROR"); //$NON-NLS-1$
                            }
                            else
                            {
                                errorString = getResourceString("VersionCheckerTask.UPDATE_CHECK_PARSE_ERROR"); //$NON-NLS-1$
                            }
                            showErrorPopup(errorString);
                        }
                        
                        return;
                    }
                    // else (no errors occurred)
                   
                    // the update check worked, review the results
                    List<String> availUpdates = (List<String>)retVal;
                    boolean autoUpdatesOn     = autoUpdateCheckIsEnabled();
                    // if there are updates AND auto checking is enabled, OR the user initiated this update check, display a popup message
                    if ((availUpdates.size() > 0 && autoUpdatesOn) || isUserInitiatedUpdateCheck)
                    {
                        displayUpdatesAvailablePopup(availUpdates);
                    }
                }
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
    protected List<String> sendStatsAndGetUpdates() throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getVersionCheckURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        boolean sendUsageStats = usageTrackingIsAllowed();
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters(sendUsageStats);
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
        
        // TODO: bypass this code if this connection was ONLY for sending usage stats
        // get the server response
        String responseString = postMethod.getResponseBodyAsString();
        List<String> moduleUpdatesAvailable = getAvailableUpdates(responseString);
        return moduleUpdatesAvailable;
    }
    
    /**
     * Gets the URL of the version checking / usage tracking server.
     * 
     * @return the URL string
     */
    protected String getVersionCheckURL()
    {
        String baseURL = getResourceString("VersionCheckerTask.VERSION_CHECK_URL"); //$NON-NLS-1$
        return baseURL;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param sendUsageStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters(boolean sendUsageStats)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();

        // get the install ID
        String installID = UsageTracker.getInstallId();
        postParams.add(new NameValuePair("id",installID)); //$NON-NLS-1$

        // get the OS name and version
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        os = StringUtils.deleteWhitespace(os);
        postParams.add(new NameValuePair("os",os)); //$NON-NLS-1$
        String version = System.getProperty("os.version"); //$NON-NLS-1$
        version = StringUtils.deleteWhitespace(version);
        postParams.add(new NameValuePair("version",version)); //$NON-NLS-1$

        if (sendUsageStats)
        {
            // get all of the usage tracking stats
            List<Pair<String,Integer>> stats = UsageTracker.getUsageStats();
            for (Pair<String,Integer> stat: stats)
            {
                postParams.add(new NameValuePair(stat.first, Integer.toString(stat.second)));
            }
        }
        
        // create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }
        return paramArray;
    }
    
    /**
     * Shows a {@link JOptionPane} containing an message stating that an error occurred while checking for updates.
     */
    protected void showErrorPopup(String localizedErrorString)
    {
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(),
                                      localizedErrorString,
                                      getResourceString("VersionCheckerTask.ERROR"), //$NON-NLS-1$
                                      JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    /**
     * Compares the information in the version info grabbed from the server to the module versions of the currently installed software.
     * A list of installed modules that are not the latest version is returned.
     * 
     * @param latestVersionInfo an XML string holding information about the latest available module versions (retrieved from the server)
     * @return a list of currently installed modules that have a different version than the latest available (according to the server)
     * @throws Exception an XML parsing error occurred
     */
    protected List<String> getAvailableUpdates(String latestVersionInfo) throws Exception
    {
        List<String> availUpdates = new Vector<String>();
        
        Element installInfoDOM = XMLHelper.readDOMFromConfigDir("install_info.xml"); //$NON-NLS-1$
        Element latestInfoDOM  = XMLHelper.readStrToDOM4J(latestVersionInfo);
        
        List<Pair<String,String>> installedVersions = new Vector<Pair<String, String>>();
        
        // list out the versions of each of the installed modules
        for (Object instModInfo: installInfoDOM.selectNodes("Module")) //$NON-NLS-1$
        {
            Element instModInfoNode = (Element)instModInfo;
            String modName    = instModInfoNode.attributeValue("name"); //$NON-NLS-1$
            String modVersion = instModInfoNode.attributeValue("version"); //$NON-NLS-1$
            installedVersions.add(new Pair<String, String>(modName,modVersion));
        }
        
        for (Pair<String,String> instModVersion: installedVersions)
        {
            String instModName = instModVersion.first;
            String installedVersion = instModVersion.second;
            
            // find the latest version string for the installed module
            String xpathToNode = "./Module[@name='" + instModName + "']"; //$NON-NLS-1$ //$NON-NLS-2$
            Node latestModVersionNode = latestInfoDOM.selectSingleNode(xpathToNode);
            if (latestModVersionNode == null)
            {
                // we couldn't find the latest version info for this module
                // ignore it
                continue;
            }
            
            String latestVersion = ((Element)latestModVersionNode).attributeValue("version"); //$NON-NLS-1$
            
            if (!installedVersion.equalsIgnoreCase(latestVersion))
            {
                // there is a new version available
                availUpdates.add(instModName);
            }
        }
        
        return availUpdates;
    }
    
    /**
     * @param availableUpdates a list of currently installed modules that have a different 'latest' version available
     */
    protected void displayUpdatesAvailablePopup(List<String> availableUpdates)
    {
        UpdatesAvailableDialog updatesPopup = new UpdatesAvailableDialog();
        updatesPopup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        updatesPopup.setAvailableUpdates(availableUpdates);
        updatesPopup.setAlwaysOnTop(true);
        if (availableUpdates == null || availableUpdates.size() == 0)
        {
            updatesPopup.setSize(300,125);
        }
        else
        {
            int height = 150 + availableUpdates.size()*20;
            updatesPopup.setSize(450,height);
        }
        //updatesPopup.pack();
        updatesPopup.setVisible(true);
    }
    
    /**
     * This class is a UI widget to present the viewer with a list of available updates. 
     * 
     * @author jstewart
     * @code_status Beta
     */
    static class UpdatesAvailableDialog extends CustomDialog
    {
        /** A JTextPane for displaying the text of the updates list. */
        protected JTextPane content = new JTextPane();
        
        /**
         * Constructor.
         */
        public UpdatesAvailableDialog()
        {
            super((JFrame)UIRegistry.getTopWindow(), getResourceString("VersionCheckerTask.VER_CHK_TITLE"), false, CustomDialog.OK_BTN, null); //$NON-NLS-1$
            content.setContentType("text/html"); //$NON-NLS-1$
            content.setEditable(false);
            content.setOpaque(false);
            JScrollPane scroller = new JScrollPane(content);
            scroller.setBorder(null);
            setContentPanel(scroller);

            content.addHyperlinkListener(new HyperlinkListener()
            {
                @SuppressWarnings("synthetic-access") //$NON-NLS-1$
                public void hyperlinkUpdate(HyperlinkEvent e)
                {
                    if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
                    {
                        return;
                    }
                    
                    URL targetURL = e.getURL();
                    try
                    {
                        URI targetURI = targetURL.toURI();
                        AttachmentUtils.openURI(targetURI);
                    }
                    catch (Exception ex)
                    {
                        log.error("Exception occurred while opening update website", ex); //$NON-NLS-1$
                    }
                }
            });
            
            // force the creation of the widgets, so I can set the size
            super.createUI();
        }
        
        /**
         * Sets the displayed text (as HTML) based on the provided list of available updates.
         * 
         * @param availableUpdates a list of available updates
         */
        public void setAvailableUpdates(List<String> availableUpdates)
        {
            StringBuilder popupMessage = new StringBuilder("<html><div style=\"font-family: sans-serif; font-size: 12pt\"><table width=\"100%\" border=\"0\">"); //$NON-NLS-1$
            if (availableUpdates.size() == 0)
            {
                // no updates available
                popupMessage.append("<tr><td align=\"center\"><br>" + getResourceString("VersionCheckerTask.NO_UPDATES_AVAILABLE") + "<br></td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            else
            {
                popupMessage.append("<tr><td>Update versions are available for the following modules:<ul>"); //$NON-NLS-1$
                for (String update: availableUpdates)
                {
                    popupMessage.append("<li>" + update + "</li>");; //$NON-NLS-1$ //$NON-NLS-2$
                }
                popupMessage.append("</ul><br>"); //$NON-NLS-1$
                String downloadSiteURL = getResourceString("VersionCheckerTask.DOWNLOAD_SITE_URL"); //$NON-NLS-1$
                popupMessage.append("<a href=\"" + downloadSiteURL + "\">" + downloadSiteURL + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                popupMessage.append("<br></td</tr>"); //$NON-NLS-1$
            }
            popupMessage.append("</table></div></html>"); //$NON-NLS-1$
            content.setText(popupMessage.toString());
        } 
    }
    
    public static class ConnectionException extends IOException
    {
        public ConnectionException(Throwable e)
        {
            super();
        }
    }
}
