package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class provides the basic capability to compare an installed piece of software with the
 * latest available version and provide some feedback to the user.
 * 
 * @author jstewart
 * @code_status Alpha
 */
public class VersionCheckerTask extends BaseTask
{
    public static final String VERSION_CHECK = "VersionChecker";
    
    public VersionCheckerTask()
    {
        super(VERSION_CHECK, getResourceString(VERSION_CHECK));
    }
    
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
                checkForUpdatesNow(true);
            }
        };
        
        timer.schedule(task, 5000);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        String label    = getResourceString("CheckNow");
        
        JMenuItem checkNow = new JMenuItem(label);
        checkNow.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.incrUsageCount("UpdateNow");
                
                // then perform the check
                checkForUpdatesNow(false);
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
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        return null;
    }
    
    /**
     * @param showResultsPopup
     * @param sendUsageDataIfAllowed
     */
    protected void checkForUpdatesNow(final boolean silent)
    {
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                try
                {
                    List<String> availUpdates = getUpdatesList();
                    return availUpdates;
                }
                catch (Exception e)
                {
                    return e;
                }
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void finished()
            {
                Object retVal = getValue();
                
                // if an exception occurred during update check...
                if (retVal instanceof Exception)
                {
                    if (!silent)
                    {
                        showErrorPopup();
                    }
                    return;
                }
                // else
               
                // the update check worked, review the results
                List<String> availUpdates = (List<String>)retVal;
                if (availUpdates.size() > 0 || !silent)
                {
                    displayUpdatesAvailablePopup(availUpdates);
                }
            }
        };
        workerThread.start();
    }
    
    protected List<String> getUpdatesList() throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = getVersionCheckURL();
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // see if the user is allowing us to send back usage data
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        boolean sendStats = appPrefs.getBoolean("sendUsageStats", true);
        
        NameValuePair[] postParams = createPostParameters(sendStats);
        postMethod.setRequestBody(postParams);
        httpClient.executeMethod(postMethod);
        String responseString = postMethod.getResponseBodyAsString();
        List<String> moduleUpdatesAvailable = getAvailableUpdates(responseString);
        return moduleUpdatesAvailable;
    }
    
    /**
     * @param sendUsageDataIfAllowed
     * @return
     */
    protected String getVersionCheckURL()
    {
        String baseURL = getResourceString("VERSION_CHECK_URL");
        return baseURL;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking request.
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
     * @param e
     * @param showResultsPopup
     */
    protected void showErrorPopup()
    {
        String updateError = getResourceString("UpdateError");
        JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.TOPFRAME),
                                      updateError,
                                      getResourceString("Error"),
                                      JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    /**
     * @param latestVersionInfo
     * @return
     * @throws Exception
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
     * @param availableUpdates
     */
    protected void displayUpdatesAvailablePopup(List<String> availableUpdates)
    {
        // TODO: implement this with production code
        
        String popupMessage = "<html>";
        
        if (availableUpdates.size() == 0)
        {
            // no updates available
            popupMessage += "No updates available";
        }
        else
        {
            popupMessage += "Update versions are available for the following modules:<ul>";
            for (String update: availableUpdates)
            {
                popupMessage += "<li>" + update + "</li>";
            }
            popupMessage += "</ul>";
        }
        
        JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.TOPFRAME), popupMessage, getResourceString("VER_CHK_TITLE"), JOptionPane.INFORMATION_MESSAGE);
    }
}
