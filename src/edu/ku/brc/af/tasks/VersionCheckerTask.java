package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    
    public static final String VERSION_CHECK = "VersionChecker";
    
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
        Timer timer = new Timer("VersionCheckingThread", true);
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
        // create a menu item for "Check for updates now"
        
        String label    = getResourceString("CheckNow");
        
        JMenuItem checkNow = new JMenuItem(label);
        checkNow.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.incrUsageCount("UpdateNow");
                
                // then perform the check
                connectToServerNow(true);
            }
        });
        
        MenuItemDesc  miDesc = new MenuItemDesc(checkNow, getResourceString("Help"));
        
        miDesc.setPosition(MenuItemDesc.Position.Before, getResourceString("About"));
        miDesc.setSepPosition(MenuItemDesc.Position.After);
        
        List<MenuItemDesc> menuItems = new Vector<MenuItemDesc>();
        menuItems.add(miDesc);

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
        Boolean        sendStats = appPrefs.getBoolean("usage_tracking.send_stats", null);
        if (sendStats == null)
        {
            sendStats = true;
            appPrefs.putBoolean("usage_tracking.send_stats", sendStats);
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
        Boolean        checkForUpdates = appPrefs.getBoolean("version_check.auto", null);
        if (checkForUpdates == null)
        {
            checkForUpdates = true;
            appPrefs.putBoolean("version_check.auto", checkForUpdates);
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
            
            @SuppressWarnings("unchecked")
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
                            showErrorPopup();
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
        httpClient.getParams().setParameter("http.useragent", getClass().getName());
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getVersionCheckURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        boolean sendUsageStats = usageTrackingIsAllowed();
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters(sendUsageStats);
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        httpClient.executeMethod(postMethod);
        
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
        String baseURL = getResourceString("VERSION_CHECK_URL");
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
        postParams.add(new NameValuePair("id",installID));

        // get the OS name and version
        String os = System.getProperty("os.name");
        os = StringUtils.deleteWhitespace(os);
        postParams.add(new NameValuePair("os",os));
        String version = System.getProperty("os.version");
        version = StringUtils.deleteWhitespace(version);
        postParams.add(new NameValuePair("version",version));

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
    protected void showErrorPopup()
    {
        String updateError = getResourceString("UpdateError");
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(),
                                      updateError,
                                      getResourceString("Error"),
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
        
        Element installInfoDOM = XMLHelper.readDOMFromConfigDir("install_info.xml");
        Element latestInfoDOM  = XMLHelper.readStrToDOM4J(latestVersionInfo);
        
        List<Pair<String,String>> installedVersions = new Vector<Pair<String, String>>();
        
        // list out the versions of each of the installed modules
        for (Object instModInfo: installInfoDOM.selectNodes("Module"))
        {
            Element instModInfoNode = (Element)instModInfo;
            String modName    = instModInfoNode.attributeValue("name");
            String modVersion = instModInfoNode.attributeValue("version");
            installedVersions.add(new Pair<String, String>(modName,modVersion));
        }
        
        for (Pair<String,String> instModVersion: installedVersions)
        {
            String instModName = instModVersion.first;
            String installedVersion = instModVersion.second;
            
            // find the latest version string for the installed module
            String xpathToNode = "./Module[@name='" + instModName + "']";
            Node latestModVersionNode = latestInfoDOM.selectSingleNode(xpathToNode);
            if (latestModVersionNode == null)
            {
                // we couldn't find the latest version info for this module
                // ignore it
                continue;
            }
            
            String latestVersion = ((Element)latestModVersionNode).attributeValue("version");
            
            if (!installedVersion.toLowerCase().equals(latestVersion))
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
        updatesPopup.setSize(300,100);
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
            super((JFrame)UIRegistry.getTopWindow(), getResourceString("VER_CHK_TITLE"), false, CustomDialog.OK_BTN, null);
            content.setContentType("text/html");
            content.setEditable(false);
            content.setOpaque(false);
            setContentPanel(content);
            
            content.addHyperlinkListener(new HyperlinkListener()
            {
                @SuppressWarnings("synthetic-access")
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
                        log.error("Exception occurred while opening update website", ex);
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
            String popupMessage = new String("<html>");
            
            if (availableUpdates.size() == 0)
            {
                // no updates available
                popupMessage += "<center>" + getResourceString("NO_UPDATES_AVAILABLE") + "</center>";
            }
            else
            {
                popupMessage += "Update versions are available for the following modules:<ul>";
                for (String update: availableUpdates)
                {
                    popupMessage += "<li>" + update + "</li>";
                }
                popupMessage += "</ul><br>";
                
                String downloadSiteURL = getResourceString("DOWNLOAD_SITE_URL");
                popupMessage += "<a href=\"" + downloadSiteURL + "\">" + downloadSiteURL + "</a>";
                
                popupMessage += "</html>";
            }
            
            content.setText(popupMessage);
        }
    }
}
