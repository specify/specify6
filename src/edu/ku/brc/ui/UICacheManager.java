/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.exceptions.UIException;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.FileCache;

/**
 * This class manages all things UI. It is the central clearing house for UI components.
 * Meaning you can register various UI components by name and then get them later.<br><br>
 * <br>
 * Using the <i>getMostRecentFrame</i> method<br>
 * You <i>SHOULD</i> set the RECENTFRAME ui component when you are in a dialog and then any error dialogs
 * can be parented to your dialog. But if you set it you MUST set it back to null. (NOte if you forget it will always return
 * the TOPFRAME, but don't rely on this)

 * @code_status Complete
 **
 * @author rods
 *
 */
public class UICacheManager
{
    // Static Data Members
    protected static final String MISSING_FACTORY_MSG = "The object has not been set for the ViewBasedDialogFactoryIFace. This class can be used without first setting a factory implementing this interface.";

    public static final String FRAME     = "frame";
    public static final String MENUBAR   = "menubar";
    public static final String TOOLBAR   = "toolbar";
    public static final String STATUSBAR = "statusbar";
    public static final String TOPFRAME  = "topframe";
    public static final String GLASSPANE = "glasspane";
    public static final String MAINPANE  = "mainpane";
    public static final String RECENTFRAME  = "recentframe";

    public static final String LONGTERM_CACHE_MAP = "longterm-cache-map.xml";

    private static final Logger           log      = Logger.getLogger(UICacheManager.class);
    protected static final UICacheManager instance = new UICacheManager();

    // Data Members
    protected Hashtable<String, Component> components = new Hashtable<String, Component>();

    protected Hashtable<String, Hashtable<String, JComponent>> uiItems = new Hashtable<String, Hashtable<String, JComponent>>();

    protected Font           baseFont           = null;
    
    protected ResourceBundle resourceBundle     = null;
    protected String         resourceName       = "resources";

    protected FileCache      longTermCache      = null;
    protected FileCache      shortTermCache     = null;

    protected String         defaultWorkingPath = null;
    protected String         appName            = null;

    protected ViewBasedDialogFactoryIFace viewbasedFactory = null;
    

    /**
     * Default private constructor for singleton.
     *
     */
    private UICacheManager()
    {
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
     * Returns the singleton.
     * @return the singleton instance
     */
    public static UICacheManager getInstance()
    {
        return instance;
    }

    /**
     *  Return the current ResourceBundle.
     * @return the current ResourceBundle
     */
    protected static ResourceBundle getResourceBundleInternal()
    {
        return instance.getResourceBundle();
    }


    /**
     * Returns the "working" directory which is platform specific. It will create one if one is not created
     * <b>NOTE: The application name must be set first.</b><br>
     * One Windows it is ...\<i>&lt;user name&gt;</i>\Application Data\&lt;application name&gt;<br>
     * On Unix based platforms it will create a directory of the "application name" with a "." in front. 
     * For example: <code>/home/john/.specify</code>
     * @return Returns the "working" directory which is platform specific.
     */
    public static String getDefaultWorkingPath()
    {
        if (instance.defaultWorkingPath == null)
        {
            instance.defaultWorkingPath = getUserDataDir();
        }
        return instance.defaultWorkingPath;
    }

    /**
     * Set the working directory. It is not recommended to use this because the working directory will automatically be created.
     * @param defaultWorkingPath the new and different working directory.
     */
    public static void setDefaultWorkingPath(String defaultWorkingPath)
    {
        instance.defaultWorkingPath = defaultWorkingPath;
    }
    
    /**
     * Get the "user" based working directory that is platform specific and requires the "application name" be set first. 
     * @return the string to a platform specify user data directory for the application name.
     */
    public static String getUserDataDir()
    {
        if (instance.appName == null)
        {
            throw new RuntimeException("The AppName has not been set into the UICacheManger!");
        }
        
        String base;
        if (System.getProperty("os.name").indexOf("Windows") > -1)
        {
            base = System.getenv("APPDATA") + File.separator + instance.appName;
        } else
        {
            base = System.getProperty("user.home") + File.separator + "." + instance.appName;
        }
        File baseDir = new File(base);
        if (!baseDir.exists())
        {
            if (!baseDir.mkdir())
            {
                throw new RuntimeException("Couldn't create data directory for "+instance.appName+" ["+baseDir.getAbsolutePath()+"]");
            }
        }
        return base;
    }

    /**
     * Returns the current application name.
     * @return the current application name.
     */
    public static String getAppName()
    {
        return instance.appName;
    }

    /**
     * Sets the application name and this name cannot be changed (meaning it can only be set once).
     * @param appName the application name (it is best if it doesn't have a space in the middle)
     */
    public static void setAppName(final String appName)
    {
        if (StringUtils.isNotEmpty(appName) && StringUtils.isEmpty(instance.appName))
        {
            instance.appName = appName;
        } else
        {
            throw new RuntimeException("You cannot set the app name twice or with an empty string!");
        }
    }

    /**
     * Registers a uicomp.
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
     * Unregisters a uicomp.
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
     * Returns a UI component by name.
     * @param name the name of the component to be retrieved
     * @return a UI component by name
     */
    public static Component get(final String name)
    {
        return instance.components.get(name);
    }

    /**
     * Returns the main ResourceBundle.
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
     * Sets the resource name.
     * @param resourceName The reourceName to set.
     */
    public void setResourceName(final String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    protected String getResourceStringInternal(final String key)
    {
        try 
        {
            return resourceBundle.getString(key);
            
        } catch (MissingResourceException ex) 
        {
            log.error("Couldn't find key["+key+"] in resource bundle.");
            return key;
        }
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    public static String getResourceString(final String key)
    {
        return instance.getResourceStringInternal(key);
    } 
    
    /**
     * Formats an Internationalized string with a variable argument list.
     * @param key the I18N key
     * @param args the list args
     * @return a formatted string
     */
    public static String getLocalizedMessage(final String key, final Object ... args)
    {
        return String.format(getResourceString(key), args);
    }

    /**
     * Returns the ViewBasedFacory for the application.
     * @return the ViewBasedFacory for the application
     */
    public static ViewBasedDialogFactoryIFace getViewbasedFactory()
    {
        if (instance.viewbasedFactory == null)
        {
            String className = System.getProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", null);
            if (StringUtils.isNotEmpty(className))
            {
                try 
                {
                    instance.viewbasedFactory = (ViewBasedDialogFactoryIFace)Class.forName(className).newInstance();
                   
                } catch (Exception e) 
                {
                    InternalError error = new InternalError("Can't instantiate ViewBasedDialogFactoryIFace factory " + className);
                    error.initCause(e);
                    throw error;
                }
                
            } else
            {
                throw new InternalError(MISSING_FACTORY_MSG);
            }
        }

        return instance.viewbasedFactory;
    }

    /**
     * Sets the ViewBasedFacory for the application.
     * @param viewbasedFactory the factory
     */
    /*public static void setViewbasedFactory(ViewBasedDialogFactoryIFace viewbasedFactory)
    {
        instance.viewbasedFactory = viewbasedFactory;
    }*/

    /**
     * Registers a uiComp into the applications.
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
     * Unregisters a uiComp from the application.
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
     * Displays a message in the status bar.
     * @param text the text to be displayed
     */
    public static void displayStatusBarText(final String text)
    {
        JStatusBar statusBar = ((JStatusBar)instance.components.get(STATUSBAR));
        if (statusBar != null)
        {
            statusBar.setText(text == null ? "" : text);
        }
    }

    /**
     * Displays a message in the status bar.
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
     * Repaints the top most frame.
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
     * Returns the glass pane from the mgr.
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
     * 
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
      * Display an Error dialog.
     * @param msg the message to be displayed
     */
    public static void displayErrorDlg(final String msg)
    {
         JOptionPane.showMessageDialog(getMostRecentFrame(), msg, getResourceString("error"), JOptionPane.ERROR_MESSAGE);
    }

    //----------------------------------------------------------------------------------
    // File Cache Section
    //----------------------------------------------------------------------------------

    /**
     * Returns the longTermCache.
	 * @return the longTermCache.
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
     * Sets the longTermCache.
	 * @param longTermCache The longTermCache to set.
	 */
	public static void setLongTermFileCache(FileCache longTermCache)
	{
		instance.longTermCache = longTermCache;
	}

	/**
     * Gets the shortTermCache.
	 * @return the shortTermCache.
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
     * Sets the shortTermCache.
	 * @param shortTermCache The shortTermCache to set.
	 */
	public static void setShortTermFileCache(FileCache shortTermCache)
	{
		instance.shortTermCache = shortTermCache;
	}

    /**
     * Creates the initial font mapping from the base font size to the other sizes.
     * @param clazz the class of the component
     * @param baseFontArg the base font size
     */
    protected static void adjustAllFonts(final Font oldBaseFont, final Font baseFontArg)
    {
        int    fontSize    = baseFontArg.getSize();
        int    oldFontSize = oldBaseFont.getSize();
        String family   = baseFontArg.getFamily();
        
        UIDefaults uiDefaults = UIManager.getDefaults();
        Enumeration<Object> e = uiDefaults.keys();
        while (e.hasMoreElements())
        {
            Object key = e.nextElement();
            if (key.toString().endsWith(".font"))
            {
                FontUIResource fontUIRes = (FontUIResource)uiDefaults.get(key);
                if (fontSize != fontUIRes.getSize() || !family.equals(fontUIRes.getFamily()))
                {
                    UIManager.put(key, new FontUIResource(new Font(family, fontUIRes.getStyle(), fontSize + (fontUIRes.getSize() - oldFontSize))));
                }
            }
        }
    }
    
    /**
     * Return the base font for the UI component.
     * @return the base font for the UI.
     */
    public static Font getBaseFont()
    {
        return instance.baseFont;
    }
    
    /**
     * Sets the base font and builds all the control's fonts.
     * @param newBaseFont the new font
     */
    public static void setBaseFont(final Font newBaseFont)
    {
        if (instance.baseFont != null && instance.baseFont != newBaseFont)
        {
            adjustAllFonts(instance.baseFont, newBaseFont);
        }
        instance.baseFont = newBaseFont;
    }

}

