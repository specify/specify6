/* Filename:    $RCSfile: UICacheManager.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.2 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import java.awt.Component;
import java.io.File;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.exceptions.UIException;
import edu.ku.brc.specify.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.FileCache;

/**
 * This class manages all things UI. It is the central clearing house for UI components.
 * Meaning you can register various UI components by name and then get them later.<br><br>
 * <br>
 * Using the <i>getMostRecentFrame</i> method<br>
 * You <i>SHOULD</i> set the RECENTFRAME ui component when you are in a dialog and then any error dialogs
 * can be parented to your dialog. But if you set it you MUST set it back to null. (NOte if you forget it will always return
 * the TOPFRAME, but don't rely on this)
 *
 * @author rods
 *
 */
public class UICacheManager
{
    // Static Data Members
    public static final String FRAME     = "frame";
    public static final String MENUBAR   = "menubar";
    public static final String TOOLBAR   = "toolbar";
    public static final String STATUSBAR = "statusbar";
    public static final String TOPFRAME  = "topframe";
    public static final String GLASSPANE = "glasspane";
    public static final String MAINPANE  = "mainpane";
    public static final String RECENTFRAME  = "recentframe";

    public static final String LONGTERM_CACHE_MAP = "sp6-cache-map.xml";

    private static final Log            log      = LogFactory.getLog(UICacheManager.class);
    private static final UICacheManager instance = new UICacheManager();

    // Data Members
    protected Hashtable<String, Component> components = new Hashtable<String, Component>();

    protected Hashtable<String, Hashtable<String, JComponent>> uiItems = new Hashtable<String, Hashtable<String, JComponent>>();

    protected ResourceBundle resourceBundle = null;
    protected String         resourceName   = "resources";

    protected SubPaneMgr     subPaneMgr     = null;
    protected Class          rootPrefClass   = null;

    protected FileCache      longTermCache = null;
    protected FileCache      shortTermCache = null;

    protected String         defaultWorkingPath = System.getProperty("user.home") + File.separator + "Specify";

    /**
     * Default private constructor for singleton
     *
     */
    private UICacheManager()
    {
        File path = new File(defaultWorkingPath);
        if (!path.exists())
        {
            if (!path.mkdirs())
            {
                String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }

        if (resourceBundle == null)
        {
            try {
                // Get the resource bundle for the default locale
                resourceBundle = ResourceBundle.getBundle(resourceName);

            } catch (MissingResourceException ex) {
                log.error("Couldn't find Resource Bundle Name["+resourceName+"]", ex);
            }
        }
    }

    /**
     *
     * @return the singleton instance
     */
    public static UICacheManager getInstance()
    {
        return instance;
    }

    /**
     *
     * @return the current ResourceBundle
     */
    public static ResourceBundle getResourceBundleInternal()
    {
        return instance.getResourceBundle();
    }


    public String getDefaultWorkingPath()
    {
        return defaultWorkingPath;
    }

    public void setDefaultWorkingPath(String defaultWorkingPath)
    {
        this.defaultWorkingPath = defaultWorkingPath;
    }

    /**
     * Registers a uicomp
     * @param category the category to be registered
     * @param name the name
     * @param uiComp the ui component
     * @throws UIException throws exception if it is already registered
     */
    public static void registerUI(final String category, final String name, final JComponent uiComp) throws UIException
    {
        Hashtable<String, JComponent> compsHash = instance.uiItems.get(category);
        if (compsHash == null)
        {
            compsHash = new Hashtable<String, JComponent>();
            instance.uiItems.put(category, compsHash);
        }
        if (compsHash.containsKey(name))
        {
           throw new UIException("UI component with Name["+name+"] has already been registered to ["+category+"].");
        }
        compsHash.put(name, uiComp);
    }

    /**
     * Unregisters a uicomp
     * @param category the category to be registered
     * @param name the name
     * @throws UIException throws exception if it is not registered
     */
    public static void unregisterUI(final String category, final String name) throws UIException
    {
        Hashtable<String, JComponent> compsHash = instance.uiItems.get(category);
        if (compsHash == null)
        {
            throw new UIException("Couldn't find UI Category with Name["+category+"].");
        }
        JComponent comp = compsHash.get(name);
        if (comp == null)
        {
           throw new UIException("Couldn't find UI component with Name["+name+"].");
        }
        compsHash.remove(comp);
    }

    /**
     * Returns a UI component by name
     * @param name the name of the component to be retrieved
     * @return a UI component by name
     */
    public static Component get(final String name)
    {
        return instance.components.get(name);
    }

    /**
     * Returns the main ResourceBundle
     * @return Returns the main ResourceBundle
     */
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    /**
     * Returns the reourceName.
     * @return Returns the reourceName.
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * Sets the resource name
     * @param resourceName The reourceName to set.
     */
    public void setResourceName(final String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption)
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    protected String getResourceStringInternal(final String key)
    {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException ex) {
            log.error("Couldn't find key["+key+"] in resource bundle.");
            return key;
        }
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption)
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    public static String getResourceString(final String key)
    {
        return instance.getResourceStringInternal(key);
    }

    /**
     * Registers a uiComp into the applications
     * @param name the name of the UI component to be registered
     * @param uiComp the UI component to be registered
     */
    public static void register(final String name, final Component uiComp)
    {
        if (uiComp != null)
        {
            if (instance.components.get(name) == null)
            {
                instance.components.put(name, uiComp);
            } else
            {
                throw new RuntimeException("Registering a uiComp with an existing name["+ name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to register a null UI Component!");
        }
    }

    /**
     * Unregisters a uiComp from the application
     * @param name the name of the UI component to be unregistered
     */
    public static void unregister(final String name)
    {
        if (name != null)
        {
            if (instance.components.get(name) != null)
            {
                instance.components.remove(name);
            } else
            {
                throw new RuntimeException("Unregistering a uiComp that has been registered ["+name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to unregister with a null name!");
        }
    }

    /**
     * Displays a message in the status bar
     * @param text the text to be displayed
     */
    public static void displayStatusBarText(final String text)
    {
        JTextField txtField = ((JTextField)instance.components.get(STATUSBAR));
        if (txtField != null)
        {
            txtField.setText(text == null ? "" : text);
        }
    }

    /**
     * Displays a message in the status bar
     * @param key the key of the string that is to appear in the status bar. The resource string will be looked up
     */
    public static void displayLocalizedStatusBarText(final String key)
    {
        if (key == null) throw new NullPointerException("Call to displayLocalizedStatusBarText cannot be null!");

        String localizedStr = instance.getResourceStringInternal(key);
        assert localizedStr != null : "Localized String for key["+key+"]";

        displayStatusBarText(localizedStr);


    }

    /**
     * Convience method for adding a subpane to the subpane manager
     * @param subPane the sub pane to be added
     */
    public static void addSubPane(SubPaneIFace subPane)
    {
        getSubPaneMgr().addPane(subPane);
    }

    /**
     *
     * @return the sub pane manager
     */
    public static SubPaneMgr getSubPaneMgr()
    {
        return instance.subPaneMgr;
    }

    /**
     *
     * @param subPaneMgr
     */
    public static void setSubPaneMgr(SubPaneMgr subPaneMgr)
    {
        instance.subPaneMgr = subPaneMgr;
    }

    /**
     * repaints the top most frame
     *
     */
    public static void forceTopFrameRepaint()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                JFrame frame = ((JFrame)instance.components.get(TOPFRAME));
                assert frame != null : "The top frame has not been registered";
                frame.repaint();
            }
          });
    }

    /**
     * Returns the glass pane from the mgr
     * @return Returns the glass pane from the mgr
     */
    public static GhostGlassPane getGlassPane()
    {
        return ((GhostGlassPane)instance.components.get(GLASSPANE));
    }

    /**
     * Returns the most recent frame to be used, but note that there is no magic here. You
     * must set the the most recent frame in order for it to be used by someone else. The one
     * thing it does do for you, is that if you forgot to set it and someone else uses it it does
     * check to make sure it is not null AND visible. If either of these are true then it returns the TOPFRAME.
     * @return Returns the most recent frame to be used, but note that there is no magic here. You
     * must set the the most recent frame in order for it to be used by someone else. The one
     * thing it does do for you, is that if you forgot to set it and someone else uses it it does
     * check to make sure it is not null AND visible. If either of these are true then it returns the TOPFRAME.
     */
    public static Component getMostRecentFrame()
    {
        Component recent = instance.components.get(RECENTFRAME);
        return recent == null || !recent.isVisible() ? instance.components.get(TOPFRAME) : recent;
    }

     /**
      * Display an Error dialog
     * @param msg the message to be displayed
     */
    public static void displayErrorDlg(final String msg)
    {
         JOptionPane.showMessageDialog(getMostRecentFrame(), msg, getResourceString("error"), JOptionPane.ERROR_MESSAGE);
    }


    //----------------------------------------------------------------------------------
    // Prefs Section
    //----------------------------------------------------------------------------------

    /**
     * Returns the name of the root preference for this application
     * @return Returns the name of the root preference for this application
     */
    public static Class getRootPrefClass()
    {
        if (instance.rootPrefClass == null)
        {
            throw new RuntimeException("Root Pref Name is null and nees to be set!");
        }
        return instance.rootPrefClass;
    }

    /**
     * Sets the name of the root preference for this application
     * @param rootPrefClass the root pref name (defaults to "Specify")
     */
    public static void setRootPrefClass(final Class rootPrefClass)
    {
        instance.rootPrefClass = rootPrefClass;
    }

    /**
     * Helper method to assist in building pref node names.
     * It takes the parent name and then appends a slash and then the child's name
     * @param parentName the parent name
     * @param childName the new child name to be appended
     * @return returnturn [parentName]/[childName]
     */
    public static String appendChildPrefName(String parentName, String childName)
    {
        return parentName + "/" + childName;
    }

    /**
     * Helper method to assist in building pref node names.
     * It takes the parent name and then appends a slash and then the child's name
     * @param parentName the parent name
     * @param childName the new child name to be appended
     * @return returnturn [parentName]/[childName]
     */
    public static String appendChildPrefName(String parentName, String childName, String subChildName)
    {
        return parentName + "/" + childName+ "/" + subChildName;
    }

    /**
     * Return the Preferences node for the application
     * @return Return the Preferences node for the application
     */
    public static Preferences getAppPrefs()
    {
        return Preferences.userNodeForPackage(getRootPrefClass());
    }


    //----------------------------------------------------------------------------------
    // File Cache Section
    //----------------------------------------------------------------------------------

    /**
	 * @return Returns the longTermCache.
	 */
	public static FileCache getLongTermFileCache()
	{
        if (instance.longTermCache == null)
        {
            try
            {
                instance.longTermCache = new FileCache("longTerm.Cache");
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
		return instance.longTermCache;
	}

	/**
	 * @param longTermCache The longTermCache to set.
	 */
	public static void setLongTermFileCache(FileCache longTermCache)
	{
		instance.longTermCache = longTermCache;
	}

	/**
	 * @return Returns the shortTermCache.
	 */
	public static FileCache getShortTermFileCache()
	{
        if (instance.shortTermCache == null)
        {
            try
            {
                instance.shortTermCache = new FileCache("shortTermCache");
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
		return instance.shortTermCache;
	}

	/**
	 * @param shortTermCache The shortTermCache to set.
	 */
	public static void setShortTermFileCache(FileCache shortTermCache)
	{
		instance.shortTermCache = shortTermCache;
	}
}
