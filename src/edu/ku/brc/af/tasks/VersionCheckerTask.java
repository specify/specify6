package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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
                
                // see if the user is allowing us to send back usage data
                AppPreferences appPrefs = AppPreferences.getLocalPrefs();
                boolean sendStats = appPrefs.getBoolean("sendUsageStats", true);
                
                // then perform the check
                checkForUpdatesNow(true,sendStats);
            }
        });
        MenuItemDesc miDesc = new MenuItemDesc(checkNow, getResourceString("Help"));
        
        List<MenuItemDesc> menuItems = new Vector<MenuItemDesc>();
        menuItems.add(miDesc);

        return menuItems;
    }

    @Override
    public SubPaneIFace getStarterPane()
    {
        return null;
    }

    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        return null;
    }
    
    protected void checkForUpdatesNow(final boolean showResultsPopup, final boolean sendUsageDataIfAllowed)
    {
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                // check the website for the info about the latest version
                HttpClient httpClient = new HttpClient();
                
                // get the URL of the website to check, with usage info appended, if allowed
                String versionCheckURL = getVersionCheckURL(sendUsageDataIfAllowed);
                
                GetMethod get = new GetMethod(versionCheckURL);
                try
                {
                    httpClient.executeMethod(get);
                }
                catch (Exception e)
                {
                    return e;
                }

                try
                {
                    return get.getResponseBodyAsString();
                }
                catch (IOException e)
                {
                    return e;
                }
            }
            
            @Override
            public void finished()
            {
                Object retVal = getValue();
                
                // if an exception occurred during update check...
                if (retVal instanceof Exception)
                {
                    showError((Exception)retVal, showResultsPopup);
                    return;
                }
                // else
               
                // the update check worked, review the results
                String responseXML = (String)retVal;
                try
                {
                    String[] moduleUpdatesAvailable = getAvailableUpdates(responseXML);
                    if (moduleUpdatesAvailable.length > 0)
                    {
                        displayUpdatesAvailablePopup(moduleUpdatesAvailable);
                    }
                }
                catch (Exception e)
                {
                    showError(e, showResultsPopup);
                    return;
                }
            }
        };
        workerThread.start();
    }
    
    protected String getVersionCheckURL(boolean sendUsageDataIfAllowed)
    {
        String baseURL = getResourceString("VERSION_CHECK_URL");
        //baseURL = "http://redbud.nhm.ku.edu/Specify/versionCheck/check.php";
        if (sendUsageDataIfAllowed)
        {
            // append the usage stats (if we are allowed) as query parameters to the GET request
            // TODO: get a UUID from somewhere that is installation unique
            String UUID = "0x1010";
            baseURL += "?id=" + UUID;
            
            // append the OS of the host along with the stats
            String os = System.getProperty("os.name");
            os = StringUtils.deleteWhitespace(os);
            baseURL += "&os=" + os;
            String version = System.getProperty("os.version");
            version = StringUtils.deleteWhitespace(version);
            baseURL += "&ver=" + version;
            
            List<Pair<String,Integer>> stats = UsageTracker.getUsageStats();
            for (Pair<String,Integer> stat: stats)
            {
                baseURL += "&" + stat.first + "=" + stat.second;
            }
        }
        return baseURL;
    }
    
    protected void showError(Exception e, boolean showResultsPopup)
    {
        String updateError = getResourceString("UpdateError");
        if (showResultsPopup)
        {
            JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.TOPFRAME),
                                          updateError,
                                          getResourceString("Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        
        UIRegistry.getStatusBar().setWarningMessage(updateError,e);
        return;
    }
    
    protected String[] getAvailableUpdates(String latestVersionInfo) throws Exception
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
        
        String[] updates = new String[availUpdates.size()];
        for (int i = 0; i < updates.length; ++i)
        {
            updates[i] = availUpdates.get(i);
        }
        return updates;
    }
    
    protected void displayUpdatesAvailablePopup(String[] availableUpdates)
    {
        // TODO: implement this with production code
        
        String popupMessage = "<html>";
        
        if (availableUpdates.length == 0)
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
