/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.exceptions.UIException;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.ui.tmanfe.SearchReplacePanel;
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
public class UIRegistry
{
    // Static Data Members
    protected static final String MISSING_FACTORY_MSG = "The object has not been set for the ViewBasedDialogFactoryIFace. This class can be used without first setting a factory implementing this interface.";
    protected static final String EMBEDDED_DB_PATH        = "embedded.dbpath";
    protected static final String MOBILE_EMBEDDED_DB_PATH = "mobile.embedded.dbpath";
    protected static final String EMBEDDED_DB_DIR         = "SPECIFY_DATA";
    
    protected static final boolean debugPaths  = false;

    
    public static final String FRAME        = "frame";
    public static final String MENUBAR      = "menubar";
    public static final String TOOLBAR      = "toolbar";
    public static final String GLASSPANE    = "glasspane";
    public static final String MAINPANE     = "mainpane";
    public static final String RECENTFRAME  = "recentframe";
    
    // Standard Actions
    public static final String UNDO         = "Undo";
    public static final String REDO         = "Redo";
    public static final String CUT          = "Cut";
    public static final String COPY         = "Copy";
    public static final String PASTE        = "Paste";
    public static final String FIND         = "Find";
    public static final String INSERT       = "Insert";
    public static final String ADD          = "Add";
    public static final String DELETE       = "Delete";
    public static final String Clear        = "Clear";
    
    public static final String LONGTERM_CACHE_MAP = "longterm-cache-map.xml";

    private static final Logger       log              = Logger.getLogger(UIRegistry.class);
    protected static final UIRegistry instance         = new UIRegistry();
    
    protected static Rectangle        frameRect        = null;
    protected static GhostGlassPane   oldGlassPane     = null;    
    protected static boolean          showingGlassPane = false;
    protected static boolean          isRelease        = false;
    protected static boolean          isTesting        = false;
    protected static int              STD_WAIT_TIME    = 2000; // 2 Seconds
    public    static int              STD_FONT_SIZE    = 20;   // 20 point size
    
    protected static Boolean          isEmbedded       = null;
    protected static Boolean          isMobile         = null;
    

    // Data Members
    protected HashMap<String, Component> components  = new HashMap<String, Component>();
    protected Window                       topWindow   = null;
    protected Stack<Window>                windowStack = new Stack<Window>();

    protected HashMap<String, HashMap<String, JComponent>> uiItems = new HashMap<String, HashMap<String, JComponent>>();

    protected Font           baseFont           = null;
    protected Font           defaultFont        = null;

    protected FileCache      longTermCache      = null;
    protected FileCache      shortTermCache     = null;
    protected FileCache      formsCache         = null;
    protected JStatusBar     statusBar          = null;

    protected String         defaultWorkingPath = null;
    protected String         userDataDir        = null;
    protected String         appDataDir         = null;
    protected String         appName            = null;
    
    // Resource Management
    protected ResourceBundle       resourceBundle = null;
    protected Stack<ResBundleInfo> resBundleStack = new Stack<ResBundleInfo>();
    protected String               resourceName   = "resources";
    protected Locale               resourceLocale = Locale.getDefault();
    protected Locale               platformLocale = Locale.getDefault();
    protected static boolean       doShowAllResStrErors = false;


    protected ViewBasedDialogFactoryIFace viewbasedFactory = null;
    
    protected HashMap<String, Action> actionMap = new HashMap<String, Action>();
    
    //------------------------------------------------
    // Undo / Redo Helpers
    //------------------------------------------------
    protected static UndoAction       undoAction; // these three are special
    protected static RedoAction       redoAction;
    protected static LaunchFindReplaceAction launchFindReplaceAction;
    
    protected HashMap<Object, Action> actions = null;
    
    // XXX Doing the whole permanentFocusOwner owner thing is a kludge until 
    // we really understand the right way to do Cut Copy Paste
    protected static Component permanentFocusOwner = null;
    
    static 
    {
        // Insurance for non-English Locales
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
        instance.baseFont = new JLabel("").getFont();
        instance.baseFont = instance.baseFont.deriveFont(Font.PLAIN);
        
        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager(); 
        focusManager.addPropertyChangeListener( 
            new PropertyChangeListener() { 
                public void propertyChange(PropertyChangeEvent e) 
                {
                    String propName = e.getPropertyName(); 
                    if (propName.equals("permanentFocusOwner"))
                    {
                        permanentFocusOwner = focusManager.getFocusOwner();
                    }
                    /*
                    System.out.println(propName+"  "+
                            ( focusManager.getFocusOwner() != null ? focusManager.getFocusOwner().hashCode():"")+
                            "  FO: ["+(focusManager.getFocusOwner() != null ? focusManager.getFocusOwner().getClass().getSimpleName() : "NULL")+"]"+
                            " PERM: ["+(permanentFocusOwner != null ? permanentFocusOwner.getClass().getSimpleName() : "NULL")+"]");//+" perm: "+permanentFocusOwner);
                    
                    if (focusManager.getFocusOwner() instanceof GetSetValueIFace)
                    {
                        System.out.println("-> "+((GetSetValueIFace)focusManager.getFocusOwner()).getValue());
                    }
                    */
                    if (("focusOwner".equals(propName)) && undoAction != null && redoAction != null) 
                    { 
                        if (focusManager.getFocusOwner() instanceof UndoableTextIFace)
                        {
                            //System.out.println("Owner");

                            UndoableTextIFace undoableText = (UndoableTextIFace)focusManager.getFocusOwner();
                            if (undoableText != null)
                            {
                                //System.err.println("Hooking up undo manager for "+undoableText);
                                undoAction.setUndoManager(undoableText.getUndoManager());
                                redoAction.setUndoManager(undoableText.getUndoManager());
                            }
                        } else if (focusManager.getFocusOwner() instanceof JTextComponent)
                        {
                            undoAction.setUndoManager(null);
                            redoAction.setUndoManager(null);
                        }
                        undoAction.updateUndoState();
                        redoAction.updateRedoState();
                    }
                } 
            } 
        );
    }
    

    /**
     * Default private constructor for singleton.
     *
     */
    private UIRegistry()
    {
        if (resourceBundle == null)
        {
            resourceBundle = getResourceBundle(resourceName);
        }
    }

    /**
     * Returns the singleton.
     * @return the singleton instance
     */
    public static UIRegistry getInstance()
    {
        return instance;
    }
    
    /**
     * @return the string for the current version number from the resources file (e.g. 6.2.10)
     */
    public static String getAppVersion()
    {
        return getResourceString("SPECIFY_VERSION");
    }
    
    /**
     * @return true if the install version number matches the jar version number.
     */
    public static boolean doesAppversionsMatch()
    {
        String install4J     = UIHelper.getInstall4JInstallString();
        String resAppVersion = getAppVersion();
        
        if (StringUtils.isEmpty(install4J) ||
            StringUtils.isEmpty(resAppVersion) ||
            !resAppVersion.equals(install4J))
        {
            showLocalizedError("APPVER_MISMATCH", install4J, resAppVersion);
            return false;
        }
        return true;
    }

    /**
     * @return the platformLocale
     */
    public static Locale getPlatformLocale()
    {
        return instance.platformLocale;
    }

    
    
    /*
     * If, to handle I18N issues with java loading of properties files, we load the properties files 'manually',
     * the following 2 methods could be used.
    */
    /**
     * 
     * @param name
     * @param locale
     * @return
     */
    /*private static String getResourcePropertiesFileName(final String name, final Locale locale)
    {
    	return "C:\\workspace\\XSpTrnk\\src\\" + name + "_" + locale.getLanguage() + ".properties";
    }*/
    
    /**
     * @param name
     * @param locale
     * @return
     */
    /*public static ResourceBundle getPropertyResourceBundleFromFileinConfigDir(final String name, final Locale locale)
    {
    	try
    	{
    		String resFileName = getResourcePropertiesFileName(name, locale);
    		return new PropertyResourceBundle(new InputStreamReader(new FileInputStream(new File(resFileName)), "UTF-8"));
    	} catch (Exception ex)
    	{
    		ex.printStackTrace();
    		return ResourceBundle.getBundle(name);
    	}
    }*/
    
    /**
     * Loads and returns a resource Bundle.
     * @param name the name of the Bundle
     * @return the resource bundle object
     */
    public static ResourceBundle getResourceBundle(final String name)
    {
        ResourceBundle resBundle = null;
        try 
        {
            // I know this seems like an odd think to check,
            // but I want it to initialize itself at start up and also be able to set a new on later.
            // this was the only way.
            if (instance == null || instance.resourceLocale == null)
            {
                resBundle = ResourceBundle.getBundle(name, new UTF8Control());
            	/* If properties files are stored in config dir and not loaded as resources:
            	resBundle =  getPropertyResourceBundleFromFileinConfigDir(name, Locale.getDefault());         	
            	 }*/
            } else
            {
                try
                {
                    resBundle = ResourceBundle.getBundle(name, instance.resourceLocale, new UTF8Control());
                    /* if properties are stored in config dir and not loaded as resources:
                	resBundle =  getPropertyResourceBundleFromFileinConfigDir(name, instance.resourceLocale);
                	*/         	
                } catch (MissingResourceException ex) 
                {
                	resBundle = ResourceBundle.getBundle(name, Locale.ENGLISH, new UTF8Control());
                }            
            }
        } catch (MissingResourceException ex) 
        {
            log.error("Couldn't find Resource Bundle Name["+name+"]", ex);
        }
        return resBundle;
    }
    
    /**
     * @return the resourceLocale
     */
    public Locale getResourceLocale()
    {
        return resourceLocale;
    }

    /**
     * @param resourceLocale the resourceLocale to set
     */
    public static void setResourceLocale(Locale resourceLocale)
    {
        if (!instance.resourceLocale.equals(resourceLocale))
        {
            instance.resourceLocale = resourceLocale;
            instance.resourceBundle = getResourceBundle(instance.resourceName);
        }
    }

    /**
     * Pushes the Resource Info onto the stack (internal because of the 'new')
     * @param name the name of the resource
     * @param rb the resource bundle
     * @return the same res bundle
     */
    protected ResourceBundle pushInternal(final String name, final ResourceBundle rb)
    {
        resBundleStack.push(new ResBundleInfo(name, rb));
        return rb;
    }
    
    /**
     * Pushes the Resource Info onto the stack.
     * @param name the name of the resource
     * @param rb the resource bundle
     * @return the same res bundle
     */
    public static ResourceBundle push(final String name, final ResourceBundle rb)
    {
        instance.pushInternal(instance.resourceName, instance.resourceBundle);
        instance.resourceBundle = rb;
        instance.resourceName   = name;
        return rb;
    }

    /**
     * Loads a Resource Bundle by name and pushes it onto the Res bundle stack.
     * @param resName the name of the res bundle to load.
     * @return the loaded resource bundle or null if not found
     */
    public static ResourceBundle loadAndPushResourceBundle(final String resName)
    {
        ResourceBundle rb = getResourceBundle(resName);
        if (rb != null)
        {
            push(resName, rb);
        } else
        {
            log.error("Unable to load ResourceBundle["+resName+"]");
        }
        return rb;
    }

    /**
     * Pops a Resource Bundle off the stack.
     * @return the res bundle
     */
    public static ResourceBundle popResourceBundle()
    {
        if (instance.resBundleStack.size() > 0)
        {
            ResBundleInfo rbi = instance.resBundleStack.pop();
            instance.resourceBundle = rbi.getResBundle();
            instance.resourceName   = rbi.getName();
        } else
        {
            log.error("Tried to pop empty ResourceBundle Stack");
        }
        return instance.resourceBundle;
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
     * Returns a localized string from the resource bundle (masks the thrown exception).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    protected String getResourceStringInternal(final String key)
    {
        try 
        {
            //log.error("["+key+"]["+resourceBundle.getString(key)+"]");
            if (StringUtils.isEmpty(key))
            {
                log.warn("The key ["+key+"] was null/empty for localization");
                return "";
            }
            return resourceBundle.getString(key);
            
        } catch (MissingResourceException ex) 
        {
            if (resBundleStack.size() == 0 || doShowAllResStrErors)
            {
                log.warn("Couldn't find key["+key+"] in resource bundle ["+resourceName+"]");
            }
            
            for (int i=resBundleStack.size()-1;i>-1;i--)
            {
                ResBundleInfo ri = resBundleStack.elementAt(i);
                try
                {
                    return ri.getResBundle().getString(key);
                    
                } catch (MissingResourceException mre) 
                {
                    if (i == 0 || doShowAllResStrErors)
                    {
                        //log.error("Couldn't find key["+key+"] in resource bundle ["+ri.getName()+"]");
                    }
                }
            }
            return key;
        }
    }

    /**
     * This will enable the showing of all getResourceString ResourceBundle look up errors no matter
     * the level. Otherwise it only shows the error when it is not found at all.
     * @param doShowAllResStrErors the doShowAllResStrErors to set
     */
    public static void setDoShowAllResStrErors(boolean doShowAllResStrErors)
    {
        UIRegistry.doShowAllResStrErors = doShowAllResStrErors;
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown exception).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    public static String getResourceString(final String key)
    {
        return instance.getResourceStringInternal(key);
    } 
    
    /**
     * Returns a localized string from the resource bundle that has a format.
     * @param key the key to look up
     * @param params the object to be inserted into the string
     * @return  Returns a localized string from the resource bundle
     */
    public static String getFormattedResStr(final String key, final Object... params)
    {
        return String.format(instance.getResourceStringInternal(key), params);
    } 
    
    /**
     * @return the getPermanentFocusOwner
     */
    public static Component getPermanentFocusOwner()
    {
        return permanentFocusOwner;
    }

    /**
     * Returns whether this is a release.
     * @return whether this is a release.
     */
    public static boolean isRelease()
    {
        return isRelease;
    }

    /**
     * Sets whether this is a release.
     * @param isRelease the isRelease to set
     */
    public static void setRelease(boolean isRelease)
    {
        UIRegistry.isRelease = isRelease;
    }

    /**
     * @return the isTesting
     */
    public static boolean isTesting()
    {
        return isTesting;
    }

    /**
     * @param isTesting the isTesting to set
     */
    public static void setTesting(boolean isTesting)
    {
        UIRegistry.isTesting = isTesting;
    }

    /**
     * @return the statusBar
     */
    public static JStatusBar getStatusBar()
    {
        return instance.statusBar;
    }

    /**
     * @param statusBar the statusBar to set
     */
    public static void setStatusBar(JStatusBar statusBar)
    {
        instance.statusBar = statusBar;
    }

    /**
     * Set the working directory. It is not recommended to use this because the working directory will automatically be created.
     * @param defaultWorkingPath the new and different working directory.
     */
    public static void setDefaultWorkingPath(final String defaultWorkingPath)
    {
        File path = new File(defaultWorkingPath);
        String defPath = defaultWorkingPath;
        try
        {
            defPath = path.getCanonicalPath();
        } catch (IOException e) {}
        
        dumpCanonicalPath("setDefaultWorkingPath", defPath);
        instance.defaultWorkingPath = defPath;
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
        	File file = new File(".");
        	instance.defaultWorkingPath = UIHelper.stripSubDirs(file.getAbsolutePath(), 1);
        	log.debug("Working Path not set, setting it to["+instance.defaultWorkingPath+"]");
        }
        //log.debug("Def Working Path["+instance.defaultWorkingPath+"]");
        
        if (debugPaths)
        {
        	try {
        		log.debug("************************ getDefaultWorkingPath: Canonical["+(new File(instance.defaultWorkingPath).getCanonicalPath())+"]");
        	} catch (Exception ex) {}
        }
        return instance.defaultWorkingPath;
    }

    /**
     * Creates a File object for a Sub-Directory inside the Default Working Path Directory.
     * If the directory doesn't exist you can have it created.  
     * @param dirName the new or existing directory name
     * @param createIt true to create it if it doesn't exist, false doesn't create it
     * @return a File object to the the directory
     */
    public static File getAppDataSubDir(final String dirName, final boolean createIt)
    {
        File newDir = new File(getAppDataDir() + File.separator + dirName);
        if (!newDir.exists() && createIt)
        {
            if (!newDir.mkdirs())
            {
                return null;
            }
        }
        return newDir;
    }

    /**
     * @param appDataDir
     */
    public static void setBaseAppDataDir(final String appDataDir) 
    {
        dumpCanonicalPath("setBaseAppDataDir", appDataDir);
        instance.appDataDir = appDataDir;
    }

    /**
     * This method will create the "working" directory for the application.
     * @return the the working directory where local preferences and other files are saved.
     */
    public static String getAppDataDir()
    {
        File dir;
        //log.debug("1 AppDataDir["+instance.appDataDir+"]");
        if (instance.appDataDir == null)
        {
            dir = new File(getUserHomeAppDir());
        } else
        {
            //log.debug("2 AppDataDir["+instance.appDataDir+"]");
            if (instance.appDataDir.equals("."))
            {
                //log.debug("************* dot");
                dir = new File(UIHelper.stripSubDirs((new File(".").getAbsolutePath()), 1) + File.separator + instance.appName);
            } else
            {
                //log.debug("3 AppDataDir["+instance.appDataDir+"]");
                dir = new File(instance.appDataDir + File.separator + instance.appName);
            }
        }
        //log.debug("AppDataDir["+dir.getAbsolutePath()+"]");
        
        if (!dir.exists())
        {
            if (!dir.mkdir())
            {
                throw new RuntimeException("Couldn't create data directory for "+instance.appName+" ["+dir.getAbsolutePath()+"]");
            }
        }
        if (debugPaths)
        {
            try {
                log.debug("************************ setDefaultWorkingPath: ["+dir.getCanonicalPath()+"]");
            } catch (Exception ex) {}
        }
        try
        {
            return dir.getCanonicalPath();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return dir.getAbsolutePath();
    }

    /**
     * Gets the location of Application directory by calling {@link getUserHomeDir()} that is platform specific and requires the "application name" be set first. 
     * @return the string to a platform specify user data directory for the application name.
     */
    public static String getUserHomeAppDir()
    {
        assert(instance.appName == null);
        
        return getUserHomeDir() + File.separator + instance.appName;
    }

    /**
     * Get the "user" based working directory that is platform specific. 
     * @return the string to a platform specify user data directory
     */
    /**
     * Get the "user" based working directory that is platform specific. When in Mobile 'mode'
     * it returns the DefaultWorkingPath, when in Standard app mode it uses the 'Default User Home Dir"
     * which is the platform specific true home directory.
     * @return the string to a platform specify user data directory
     */
    public static String getUserHomeDir()
    {
        //log.error("isMobile() "+isMobile()+"["+UIRegistry.getDefaultWorkingPath()+"]");
        
        return isMobile() ? UIRegistry.getDefaultWorkingPath() : getDefaultUserHomeDir();
    }

    /**
     * @return
     */
    public static String getDefaultUserHomeDir()
    {
        String homeDir = System.getProperty("user.home");
        
        UIHelper.OSTYPE osType = UIHelper.getOSType();
        if (osType == UIHelper.OSTYPE.Windows)
        {
            String appDataLoc = System.getenv("LOCALAPPDATA");
            
            return StringUtils.isNotEmpty(appDataLoc) ? appDataLoc : homeDir;
            
        } else if (osType == UIHelper.OSTYPE.MacOSX)
        {
            String docPath = homeDir + File.separator + "Documents"; // Not Localized
            
            if (new File(docPath).exists())
            {
                return docPath;
            }
        }
        // else
        return homeDir;
    }
    
    /**
     * 
     */
    public static void dumpPaths()
    {
        String mobile = "";
        try
        {
            if (StringUtils.isNotEmpty(getMobileEmbeddedDBPath()))
            {
                mobile = (new File(getMobileEmbeddedDBPath())).getCanonicalPath();
            }
        } catch (IOException ex) {}
        
        if (debugPaths)
        {
            log.debug("AppDataDir:                  "+getAppDataDir());
            log.debug("UserHomeAppDir:              "+getUserHomeAppDir());
            log.debug("UserHomeDir:                 "+getUserHomeDir());
            
            log.debug("DefaultEmbeddedDBPath:       "+getDefaultEmbeddedDBPath());
            log.debug("DefaultMobileEmbeddedDBPath: "+getEmbeddedDBPath());
            log.debug("MobileEmbeddedDBPath:        "+mobile);
            log.debug("DefaultWorkingPath:          "+getDefaultWorkingPath());
            //log.debug("MobileMachineDir:            "+DBConnection.getMobileMachineDir("<database name>"));
        }
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
        HashMap<String, JComponent> compsHash = instance.uiItems.get(category);
        if (compsHash == null)
        {
            compsHash = new HashMap<String, JComponent>();
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
        HashMap<String, JComponent> compsHash = instance.uiItems.get(category);
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
     * Returns a UI component by name.
     * @param altName the name of the component to be retrieved
     * @return a UI component by name
     */
    public static Window getTopWindow()
    {
        return instance.topWindow;
    }
    
    public static void setTopWindow(final Window window)
    {
        instance.topWindow = window;
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
    public static Window getMostRecentWindow()
    {
        Window recent = instance.windowStack.size() > 0 ? instance.windowStack.peek() : null;
        return recent == null || !recent.isVisible() ? instance.topWindow : recent;
    }
    
    /**
     * Pushes Window onto the Stack.
     * @param window pushed onto the Window stack
     */
    public static void pushWindow(final Window window)
    {
        instance.windowStack.push(window);
    }
    
    /**
     * Pops Window off the Stack.
     * @param window the window
     * @return pops window off the window stack
     */
    public static Window popWindow(final Window requester)
    {
        if (instance.windowStack.size() > 0)
        {
            int index = instance.windowStack.indexOf(requester);
            if (index == -1)
            {
                log.error("Trying to pop Window not on the stack ");
                
            } else if (index < instance.windowStack.size()-1)
            {
                log.error("Popping Window lower on Window stack than the Top ["+(instance.windowStack.size()-1)+"] poping index["+index+"]");
                // Now Pop other windows so it does get out of wack
                while (instance.windowStack.peek() != requester)
                {
                    instance.windowStack.pop();
                }
            } else
            {
                return instance.windowStack.pop();
            }
        } else
        {
            log.error("Trying to pop Window off an empty stack.");
        }
        return null;
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
     * Displays a dialog dialog with a parameterized localized message.
     * @param iconType the type of icon to use (Question, Error, etc)
     * @param titleKey the title localize key
     * @param msgKey the message localize key
     * @param args any args
     */
    public static void showLocalizedMsg(final int iconType, final String titleKey, final String msgKey, final Object ... args)
    {
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                (args.length == 0 ? getResourceString(msgKey) : String.format(getResourceString(msgKey), args)), 
                getResourceString(StringUtils.isNotEmpty(titleKey) ? titleKey : "WARNING"), iconType);
    }

    /**
     * Displays a Warning dialog with a non-localized error message.
     * @param titleKey the title localize key
     * @param msgKey the message localize key
     * @param args any args
     */
    public static void showLocalizedMsg(final String msgKey)
    {
        showLocalizedMsg(JOptionPane.WARNING_MESSAGE, "WARNING", msgKey);
    }

    /**
     * Displays a Warning dialog with a non-localized error message.
     * @param titleKey the title localize key
     * @param msgKey the message localize key
     * @param args any args
     */
    public static void showLocalizedMsg(final String titleKey, final String msgKey, final Object ... args)
    {
        showLocalizedMsg(JOptionPane.WARNING_MESSAGE, titleKey, msgKey, args);
    }
    
    /**
     * Asks Yes or No question using a JOptionPane
     * @param yesKey the resource key for the Yes button
     * @param noKey the resource key for the No button
     * @param nonL10NMsg the message or question NOT Localized
     * @param titleKey the resource key for the Dialog Title
     * @return JOptionPane.NO_OPTION or JOptionPane.YES_OPTION
     */
    public static int askYesNoLocalized(final String yesKey, final String noKey, final String nonL10NMsg, final String titleKey)
    {
        int userChoice = JOptionPane.NO_OPTION;
        Object[] options = { getResourceString(yesKey), getResourceString(noKey) };

        userChoice = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                                                  nonL10NMsg,
                                                     getResourceString(titleKey),
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return userChoice;
    }

    /**
     * Asks Yes, No, Cancel question using a JOptionPane
     * @param yesKey the resource key for the Yes button
     * @param noKey the resource key for the No button
     * @param cancelKey the resource key for the Cancel button
     * @param nonL10NMsg the message or question NOT Localized
     * @param titleKey the resource key for the Dialog Title
     * @return JOptionPane.NO_OPTION or JOptionPane.YES_OPTION
     */
    public static int askYesNoLocalized(final String yesKey, 
                                        final String noKey, 
                                        final String cancelKey, 
                                        final String nonL10NMsg, 
                                        final String titleKey)
    {
        int userChoice = JOptionPane.CANCEL_OPTION;
        Object[] options = { getResourceString(yesKey), getResourceString(noKey), getResourceString(cancelKey) };

        userChoice = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                                                  nonL10NMsg,
                                                     getResourceString(titleKey),
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return userChoice;
    }
    
    /**
     * Displays a dialog with a text field. 
     * @param lblKey the L10N key for the text field label
     * @param titleKey the L10N key for the title
     * @param msgKey optional message to be displayed, can be null or empty string
     * @param doMustHaveValue indicates that the dialog will only have an OK and will no close until a value is entered.
     * @return the string entered into the dialog.
     */
    public static String askForString(final String lblKey, 
                                      final String titleKey,
                                      final String msgKey,
                                      final boolean doMustHaveValue)
    {
        CellConstraints    cc = new CellConstraints();
        PanelBuilder       pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p" + (StringUtils.isNotEmpty(msgKey) ? ",4px,p:g" : "")));
        final ValTextField vt = new ValTextField(32);
        
        vt.setRequired(doMustHaveValue);
        pb.add(UIHelper.createI18NFormLabel(lblKey), cc.xy(1, 1));
        pb.add(vt, cc.xy(3, 1));
        if (StringUtils.isNotEmpty(msgKey))
        {
            pb.add(UIHelper.createI18NLabel(msgKey), cc.xyw(1, 3, 3));

        }
        pb.setDefaultDialogBorder();
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString(titleKey), true, 
                                                  doMustHaveValue ? CustomDialog.OK_BTN : CustomDialog.OKCANCEL, pb.getPanel());
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        vt.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                dlg.getOkBtn().setEnabled(StringUtils.isNotEmpty(vt.getText()));
            }
        });
        
        UIHelper.centerAndShow(dlg);
        
        return vt.getText();
    }

    /**
     * Displays an error dialog with a non-localized error message.
     * @param msg the message
     */
    public static void showError(final String msg)
    {
        showError(null, msg);
    }

    /**
     * Displays an error dialog with a non-localized error message.
     * @param dlgType Dialog type error or warning
     * @param msg the message
     */
    public static void showError(final Integer dlgType, final String msg)
    {
        log.error(msg);
        
        String titleKey = dlgType != null && dlgType == JOptionPane.WARNING_MESSAGE ? "WARNING" : "UIRegistry.UNRECOVERABLE_ERROR_TITLE";
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                msg, 
                getResourceString(titleKey), dlgType == null ? JOptionPane.ERROR_MESSAGE : dlgType);
    }

    /**
     * Displays an error dialog with a non-localized error message in a non-modal dialog.
     * @param msg the message
     */
    public static void showErrorNonModal(final String msg)
    {
        log.error(msg);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(UIHelper.createLabel(msg), cc.xy(1,1));
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                                   getResourceString("UIRegistry.UNRECOVERABLE_ERROR_TITLE"),
                                   false,
                                   CustomDialog.OK_BTN,
                                   pb.getPanel());
        
        dlg.setVisible(true);
    }

    /**
     * Displays an error dialog with a localized error message.
     * @param key the bundle key for the message
     */
    public static void showLocalizedError(final String key)
    {
        showError(null, getResourceString(key));
    }
    
    /**
     * Displays an error dialog with a localized error message.
     * @param dlgType Dialog type error or warning
     * @param key the bundle key for the message
     */
    public static void showLocalizedError(final Integer dlgType, final String key)
    {
        showError(dlgType, getResourceString(key));
    }
    
    /**
     * Displays an error dialog with a localized error message.
     * @param key the bundle key for the message
     * @param args args for the message
     */
    public static void showLocalizedError(final String key, final Object ... args)
    {
        showError(String.format(getResourceString(key), args));
    }
    
    /**
     * Displays an error dialog with a localized error message.
     * @param dlgType Dialog type error or warning
     * @param key the bundle key for the message
     * @param args args for the message
     */
    public static void showLocalizedError(final Integer dlgType, final String key, final Object ... args)
    {
        showError(dlgType, String.format(getResourceString(key), args));
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
     * Displays a message in the status bar (Note: this updates on the SwingThread via SwingUtilities)
     * @param text the text to be displayed
     */
    public static void displayStatusBarText(final String text)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                if (instance.statusBar != null)
                {
                    instance.statusBar.setText(text == null ? "" : text);
                }                
            }
        });
    }

    /**
     * Displays a message in the status bar (Note: this updates on the SwingThread via SwingUtilities)
     * @param text the text to be displayed
     */
    public static void displayStatusBarErrMsg(final String text)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                if (instance.statusBar != null && text != null)
                {
                    instance.statusBar.setErrorMessage(text);
                }                
            }
        });
    }

    /**
     * Displays a message in the status bar. (Note: this updates on the SwingThread via SwingUtilities)
     * @param key the key of the string that is to appear in the status bar. The resource string will be looked up
     * @param args for the message
     */
    public static void displayLocalizedStatusBarText(final String key, final Object... args)
    {
        if (key == null) throw new NullPointerException("Call to displayLocalizedStatusBarText cannot be null!");

        String localizedStr = instance.getResourceStringInternal(key);
        assert localizedStr != null : "Localized String for key["+key+"]";

        displayStatusBarText(String.format(localizedStr, args));


    }

    /**
     * Displays an error message in the status bar. (Note: this updates on the SwingThread via SwingUtilities)
     * @param key the key of the string that is to appear in the status bar. The resource string will be looked up
     * @param args for the message
     */
    public static void displayLocalizedStatusBarError(final String key, final Object... args)
    {
        if (key == null) throw new NullPointerException("Call to displayLocalizedStatusBarText cannot be null!");

        String localizedStr = instance.getResourceStringInternal(key);
        assert localizedStr != null : "Localized String for key["+key+"]";

        displayStatusBarErrMsg(String.format(localizedStr, args));


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
                JFrame frame = ((JFrame)instance.topWindow);
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
     * Display an Error dialog.
     * @param msg the message to be displayed
     */
    public static void displayErrorDlg(final String msg)
    {
         JOptionPane.showMessageDialog(getMostRecentWindow(), msg, getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an Error dialog that gets its string from the resource bundle.
     * @param msg the message to be displayed
     */
    public static void displayErrorDlgLocalized(final String key, Object... args)
    {
         JOptionPane.showMessageDialog(getMostRecentWindow(), String.format(getResourceString(key), args), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an Information dialog that gets its string from the resource bundle.
     * @param key the message to be displayed
     * @param args for formatting msg
     */
    public static void displayInfoMsgDlgLocalized(final String key, Object... args)
    {
         JOptionPane.showMessageDialog(getMostRecentWindow(), String.format(getResourceString(key), args), getResourceString("INFORMATION"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Display an Information dialog that gets its string from the resource bundle.
     * @param msg the already localized message to be displayed
     */
    public static void displayInfoMsgDlg(final String msg)
    {
         JOptionPane.showMessageDialog(getMostRecentWindow(), msg, getResourceString("INFORMATION"), JOptionPane.INFORMATION_MESSAGE);
    }

    
    /**
     * Display an Confirmation Dialog where everything comes from the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param iconOption the icon to show
     * @return true is YES false if NO
     */
    public static boolean displayConfirm(final String title, 
                                         final String msg,
                                         final String keyBtn1, // Yes
                                         final String keyBtn2, // No
                                         final int    iconOption)
    {
        // Custom button text
        // NOTE: clicking the 'X' returns a -1
        Object[] options = { keyBtn1, keyBtn2 };
        
        return JOptionPane.showOptionDialog(getMostRecentWindow(), msg, 
                title, JOptionPane.YES_NO_OPTION,
                iconOption, null, options, options[1]) == JOptionPane.YES_OPTION;
    }

    /**
     * Display an Confirmation Dialog where everything comes from the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param iconOption the icon to show
     * @return true is YES false if NO
     */
    public static boolean displayConfirmLocalized(final String titleKey, 
                                                  final String msgKey,
                                                  final String keyBtn1,
                                                  final String keyBtn2,
                                                  final int    iconOption)
    {
        return displayConfirm(getResourceString(titleKey), getResourceString(msgKey), getResourceString(keyBtn1), getResourceString(keyBtn2), iconOption);
    }

    /**
     * Display an Confirmation Dialog where everything comes from the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param keyBtn3 the key to the third button
     * @param iconOption the icon to show
     * @return YES_OPTION, NO_OPTION, CANCEL_OPTION
     */
    public static int displayConfirm(final String title, 
                                     final String msg,
                                     final String keyBtn1, // Yes
                                     final String keyBtn2, // No
                                     final String keyBtn3, // Cancel
                                     final int    iconOption)
    {
        // Custom button text
        Object[] options = { keyBtn1, keyBtn2, keyBtn3 };
        
        return JOptionPane.showOptionDialog(getMostRecentWindow(), msg, 
                title, JOptionPane.YES_NO_CANCEL_OPTION,
                iconOption, null, options, options[2]);
    }

    /**
     * Display an Confirmation Dialog where everything comes from the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param keyBtn3 the key to the third button
     * @param iconOption the icon to show
     * @return YES_OPTION, NO_OPTION, CANCEL_OPTION
     */
    public static int displayConfirmLocalized(final String titleKey, 
                                              final String msgKey,
                                              final String keyBtn1,
                                              final String keyBtn2,
                                              final String keyBtn3,
                                              final int    iconOption)
    {
        return displayConfirm(getResourceString(titleKey), getResourceString(msgKey), getResourceString(keyBtn1), getResourceString(keyBtn2), getResourceString(keyBtn3), iconOption);
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
        synchronized(instance)
        {
            if (instance.longTermCache == null)
            {
                try
                {
                    instance.longTermCache = new FileCache("longTerm.Cache");
                    
                    // set the cache size to 20 MB
                    instance.longTermCache.setMaxCacheSize(20); // 20 megabytes
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIRegistry.class, ex);
                    log.error(ex);
                }
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
        synchronized(instance)
        {
            if (instance.shortTermCache == null)
            {
                try
                {
                    instance.shortTermCache = new FileCache();
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIRegistry.class, ex);
                    ex.printStackTrace();
                    log.error(ex);
                }
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
     * Returns the forms cache.
     * @return the forms cache.
     */
    public static FileCache getFormsCache()
    {
        synchronized(instance)
        {
            if (instance.formsCache == null)
            {
                try
                {
                    instance.formsCache = new FileCache("forms.Cache");
                    // turn off enforcement of the max cache size
                    instance.formsCache.setEnforceMaxCacheSize(false);
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIRegistry.class, ex);
                    ex.printStackTrace();
                    log.error(ex);
                }
            }
        }
        return instance.formsCache;
    }

    /**
     * Sets the forms cache.
     * @param formsCache The forms cache to set.
     */
    public static void setFormsCache(FileCache formsCache)
    {
        instance.formsCache = formsCache;
    }

    /**
     * Creates the initial font mapping from the base font size to the other sizes.
     * @param clazz the class of the component
     * @param baseFontArg the base font size
     */
    protected static void adjustAllFonts(final Font oldBaseFont, final Font baseFontArg)
    {
        if (oldBaseFont != null && baseFontArg != null)
        {
            int    fontSize    = baseFontArg.getSize();
            int    oldFontSize = oldBaseFont.getSize();
            String family      = baseFontArg.getFamily();
            
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
    }
    
    /**
     * Check the font against the System base font.
     * @param font the new font.
     * @return the original System Base font if the family name and size matches. For some OSs the actual
     * System Base Font is different than creating it.
     */
    public static Font adjustPerDefaultFont(final Font font)
    {
        return font.getFamily().equals(instance.defaultFont.getFamily()) && instance.defaultFont.getSize() == font.getSize() ? instance.defaultFont : font;
    }
    
    /**
     * Return the base font for the UI component.
     * @return the base font for the UI.
     */
    public static Font getBaseFont()
    {
        return instance.baseFont != null ? instance.baseFont : UIHelper.createLabel("").getFont();
    }
    
    /**
     * Sets the base font and builds all the control's fonts.
     * @param newBaseFont the new font
     */
    public static void setBaseFont(final Font newBaseFont)
    {
        if (instance.baseFont != newBaseFont && instance.baseFont != null)
        {
            adjustAllFonts(instance.baseFont, newBaseFont);
        }
        instance.baseFont = newBaseFont;
    }
    
    /**
     * @param defaultFont the default font
     */
    public static void setDefaultFont(Font defaultFont) 
    {
        instance.defaultFont = defaultFont;
    }

    /**
     * @return the default font
     */
    public static Font getDefaultFont() 
    {
        return instance.defaultFont;
    }

    //---------------------------------------------------------------------------------
    //-- Glass Pane Buffered Image
    //---------------------------------------------------------------------------------
    protected static SoftReference<BufferedImage> glassPaneBufferedImageSR;
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     * @return Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static BufferedImage getGlassPaneBufferedImage(final int width, final int height)
    {
        BufferedImage bufImg = null;
        
        if (glassPaneBufferedImageSR != null)
        {
            bufImg = glassPaneBufferedImageSR.get();
        }
        
        if (bufImg != null)
        {
            if (bufImg.getWidth() != width && bufImg.getHeight() != height)
            {
                bufImg = null;   
            }
        }
        
        if (bufImg == null)
        {
            glassPaneBufferedImageSR = new SoftReference<BufferedImage>(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
        
        return glassPaneBufferedImageSR.get();
    }

    
    /**
     * Writes a string message into the BufferedImage on GlassPane and sets the main component's visibility to false and
     * shows the GlassPane.
     * @param msg the message
     * @param pointSize the Font point size for the message to be writen in
     */
    public static GhostGlassPane writeGlassPaneMsg(final String msg, final int pointSize)
    {
        GhostGlassPane glassPane = getGlassPane();
        if (glassPane != null)
        {
            glassPane.finishDnD();
        }
        
        glassPane.setMaskingEvents(true);
        
        Component mainComp = get(MAINPANE);
        if (mainComp != null && glassPane != null)
        {
            JFrame frame = (JFrame)get(FRAME);
            frameRect = frame.getBounds();
            
            int      y        = 0;
            JMenuBar menuBar  = null;
            Dimension size    = mainComp.getSize();
            if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
            {
                menuBar = frame.getJMenuBar();
                size.height += menuBar.getSize().height;
                y += menuBar.getSize().height;
            }
            BufferedImage buffer = getGlassPaneBufferedImage(size.width, size.height);
            Graphics2D    g2     = buffer.createGraphics();
            if (menuBar != null)
            {
                menuBar.paint(g2);
            }
            g2.translate(0, y);
            mainComp.paint(g2);
            g2.translate(0, -y);
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 128));
            g2.fillRect(0, 0, size.width, size.height);
            
            
            g2.setFont(new Font((new JLabel()).getFont().getName(), Font.BOLD, pointSize));
            FontMetrics fm = g2.getFontMetrics();
            
            int tw = fm.stringWidth(msg);
            int th = fm.getHeight();
            int tx = (size.width - tw) / 2;
            int ty = (size.height - th) / 2;
            
            int expand = 20;
            int arc    = expand * 2;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
            
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(tx-(expand / 2), ty-fm.getAscent()-(expand / 2), tw+expand, th+expand, arc, arc);
            
            g2.setColor(Color.BLACK);
            g2.drawString(msg, tx, ty);
            g2.dispose();
            
            glassPane.setImage(buffer);
            glassPane.setPoint(new Point(0,0), GhostGlassPane.ImagePaintMode.ABSOLUTE);
            glassPane.setOffset(new Point(0,0));
            
            glassPane.setVisible(true);
            mainComp.setVisible(false);
            
            //Using paintImmediately fixes problems with glass pane not showing, such as for workbench saves initialed
            //during workbench or app shutdown. Don't know if there is a better way to fix it.
            //glassPane.repaint();
            glassPane.paintImmediately(glassPane.getBounds());
            showingGlassPane = true;
        }
        
        return glassPane;
    }
    
    /**
     * Clears the GlassPane, meaning it sets the mainComp visible again and sets the GlassPane to be hidden.
     */
    public synchronized static void clearGlassPaneMsg()
    {
        Component mainComp = UIRegistry.get(UIRegistry.MAINPANE);
        if (mainComp != null && getGlassPane() != null && getGlassPane().isVisible())
        {
            getGlassPane().setMaskingEvents(false);
            getGlassPane().setVisible(false);
            mainComp.setVisible(true);
            mainComp.repaint();
            
            Frame frame = (JFrame)get(FRAME);
            frame.setBounds(frameRect);
        }  
        showingGlassPane = false;
    }
    
    /**
     * @return
     */
    public static boolean isShowingGlassPane()
    {
        return showingGlassPane;
    }
    
    /**
     * @param msg
     * @param pointSize
     */
    public static SimpleGlassPane writeSimpleGlassPaneMsg(final String msg, final int pointSize)
    {
    	return writeSimpleGlassPaneMsg(msg, pointSize, false);
    }
    
    /**
     * @param msg
     * @param pointSize
     * @param isDblClickProgBar
     * @return
     */
    public static SimpleGlassPane writeSimpleGlassPaneMsg(final String msg, final int pointSize, final boolean isDblClickProgBar)
    {
        SimpleGlassPane glassPane = new SimpleGlassPane(msg, pointSize, false, isDblClickProgBar);
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        if (statusBar != null)
        {
            glassPane.setMargin(new Insets(0, 0, statusBar.getSize().height, 0));
        }
        
        oldGlassPane = UIRegistry.getGlassPane();
        if (oldGlassPane != null)
        {
            oldGlassPane.finishedWithDragAndDrop();
        }
        
        if (glassPane != null && UIRegistry.getTopWindow() != null)
        {
            ((JFrame)UIRegistry.getTopWindow()).setGlassPane(glassPane);
            glassPane.setVisible(true);
            showingGlassPane = true;
            
        } else
        {
            oldGlassPane     = null;
            showingGlassPane = false;
        }
        
        return glassPane;
    }
    
    /**
     * Fades screen and writes message to screen
     * @param localizedMsg the already localized message
     */
    public static void writeTimedSimpleGlassPaneMsg(final String  localizedMsg)
    {
        writeTimedSimpleGlassPaneMsg(localizedMsg, null, null, null, false);
    }
    
    /**
     * Fades screen and writes message to screen
     * @param localizedMsg the already localized message
     * @param textColor the color of the text
     */
    public static void writeTimedSimpleGlassPaneMsg(final String  localizedMsg,
                                                    final Color   textColor)
    {
        writeTimedSimpleGlassPaneMsg(localizedMsg, null, textColor, null, false);
    }
    
    /**
     * Fades screen and writes message to screen
     * @param localizedMsg the already localized message
     * @param milliseconds the number of milliseconds to pause showing the message
     * @param doHideOnClick true to hide message on user click
     */
    public static void writeTimedSimpleGlassPaneMsg(final String  localizedMsg,
                                                    final Integer milliseconds,
                                                    final boolean doHideOnClick)
    {
        writeTimedSimpleGlassPaneMsg(localizedMsg, milliseconds, null, null, doHideOnClick);
    }
    
    /**
     * Fades screen and writes message to screen
     * @param localizedMsg the already localized message
     * @param milliseconds the number of milliseconds to pause showing the message
     * @param textColor the color of the text
     * @param pointSize the point size to draw the text
     * @param doHideOnClick true to hide message on user click
     */
    public static void writeTimedSimpleGlassPaneMsg(final String  localizedMsg,
                                                    final Integer milliseconds, 
                                                    final Color   textColor,
                                                    final Integer pointSize,
                                                    final boolean doHideOnClick)
    {
        writeTimedSimpleGlassPaneMsg(localizedMsg, milliseconds, textColor, pointSize, doHideOnClick, null);
    }
    
    /**
     * Fades screen and writes message to screen
     * @param localizedMsg the already localized message
     * @param milliseconds the number of milliseconds to pause showing the message
     * @param textColor the color of the text
     * @param pointSize the point size to draw the text
     * @param yTextPos set the the 'y' position of the text
     */
    public static void writeTimedSimpleGlassPaneMsg(final String  localizedMsg,
                                                    final Integer milliseconds, 
                                                    final Color   textColor,
                                                    final Integer pointSize,
                                                    final boolean doHideOnClick,
                                                    final Integer yTextPos)
    {
        final SimpleGlassPane sgp = UIRegistry.writeSimpleGlassPaneMsg(localizedMsg, pointSize == null ? STD_FONT_SIZE : pointSize);
        if (sgp != null)
        {
            sgp.setTextColor(textColor);
            sgp.setHideOnClick(doHideOnClick);
            sgp.setTextYPos(yTextPos);
        }

        SwingWorker<Integer, Integer> msgWorker = new SwingWorker<Integer, Integer>()
        {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    Thread.sleep(milliseconds != null ? milliseconds : STD_WAIT_TIME);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                if (sgp != null)
                {
                    sgp.setTextColor(null);
                }
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (textColor == Color.RED)
                {
                    getStatusBar().setErrorMessage(localizedMsg);
                } else
                {
                    displayStatusBarText(localizedMsg);
                }
            }
        };
          
        msgWorker.execute();
    }
    
    /**
     * 
     */
    public synchronized static void clearSimpleGlassPaneMsg()
    {
        if (oldGlassPane != null)
        {
            ((JFrame)UIRegistry.getTopWindow()).setGlassPane(oldGlassPane);
            oldGlassPane.setVisible(false);
        } else
        {
            Component glassPane = ((JFrame)UIRegistry.getTopWindow()).getGlassPane();
            if (glassPane != null)
            {
                glassPane.setVisible(false);
            }
        }
        oldGlassPane     = null;
        showingGlassPane = false;
    }
    
    /**
     * @param isMobile
     */
    public static void setMobile(final boolean isMobile)
    {
        UIRegistry.isMobile = isMobile;
    }
    
    /**
     * @return
     */
    public static boolean isMobile()
    {
        return isMobile != null && isMobile;
    }
    
    /**
     * @return the isEmbedded
     */
    public static Boolean isEmbedded()
    {
        return isEmbedded != null && isEmbedded;
    }

    /**
     * @param isEmbedded the isEmbedded to set
     */
    public static void setEmbedded(final Boolean isEmbedded)
    {
        UIRegistry.isEmbedded = isEmbedded;
    }

    /**
     * Sets the path to the embedded DB.
     * @param path the path.
     */
    public static void setEmbeddedDBPath(final String path)
    {
        dumpCanonicalPath("setEmbeddedDBDir", path);

        if (StringUtils.isNotEmpty(path))
        {
            System.setProperty(EMBEDDED_DB_PATH, path);
        }
    }
    
    /**
     * @return the path to the embedded DB
     */
    public static String getEmbeddedDBPath()
    {
        return System.getProperty(EMBEDDED_DB_PATH);
    }
    
    /**
     * @param desc
     * @param path
     */
    private static void dumpCanonicalPath(final String desc, final String path)
    {
        dumpCanonicalPath(desc, new File(path));
    }
    
    /**
     * @param desc
     * @param path
     */
    private static void dumpCanonicalPath(final String desc, final File path)
    {
        if (debugPaths)
        {
            try {
                log.debug("***** dumpCanonicalPath: "+desc+": ["+path.getCanonicalPath()+"]");
            } catch (Exception ex) {}
        }
    }
    
    /**
     * @return a default path to the embedded DB when it is suppose to be on the local machine.
     */
    public static String getDefaultEmbeddedDBPath()
    {
        dumpCanonicalPath("getDefaultEmbeddedDBPath", getAppDataDir() + File.separator + EMBEDDED_DB_DIR);
        return UIRegistry.getAppDataDir() + File.separator + EMBEDDED_DB_DIR;
    }
    
    /**
     * @return a default path to the embedded DB when it is suppose to be on removable media. 
     * Sos it is placed relative to the executable.
     */
    public static String getDefaultMobileEmbeddedDBPath()
    {
        dumpCanonicalPath("getDefaultMobileEmbeddedDBPath", UIRegistry.getDefaultWorkingPath() + File.separator + EMBEDDED_DB_DIR);
        return UIRegistry.getDefaultWorkingPath() + File.separator + EMBEDDED_DB_DIR;
    }
    
    /**
     * @return a default path to the embedded DB when it is suppose to be on removable media. 
     * Sos it is placed relative to the executable.
     */
    public static String getDefaultMobileEmbeddedDBPath(final String dbName)
    {
        dumpCanonicalPath("getDefaultMobileEmbeddedDBPath", UIRegistry.getDefaultWorkingPath() + File.separator + dbName);
        
        String path = UIRegistry.getDefaultWorkingPath();
        
        String mobileRelativePath = System.getProperty("mobilesrcdir");
        if (StringUtils.isNotEmpty(mobileRelativePath))
        {
            if (!path.endsWith(File.separator) && !mobileRelativePath.startsWith(File.separator))
            {
                path += File.separator;
            }
            path += mobileRelativePath;
        }
        
        return path + File.separator + dbName;
    }
    
    /**
     * Sets the path to the embedded DB.
     * @param path the path.
     */
    public static void setMobileEmbeddedDBPath(final String path)
    {
        dumpCanonicalPath("setMobileEmbeddedDBPath", path);

        if (StringUtils.isNotEmpty(path))
        {
            System.setProperty(MOBILE_EMBEDDED_DB_PATH, path);
        }
    }
    
    /**
     * @return the path to the embedded DB
     */
    public static String getMobileEmbeddedDBPath()
    {
        return System.getProperty(MOBILE_EMBEDDED_DB_PATH);
    }
    
    //---------------------------------------------------------
    //-- Undo / Redo and Other Action Stuff
    //---------------------------------------------------------

    /**
     * Returns an Action by name.
     * @param name the name of the action
     */
    public static Action getAction(final String name)
    {
        return instance.actionMap.get(name);
    }
    
    /**
     * Register's an action.
     * @param name thee name of the action
     * @param action the action 
     * @return the action passed in
     */
    public static Action registerAction(final String name, final Action action)
    {
        if (instance.actionMap.get(name) == null)
        {
            instance.actionMap.put(name, action);
            
        } else
        {
            log.error("Action with name["+name+"] is already registered.");
        }
        return action;
    }
    
    /**
     * Register's an action.
     * @param name thee name of the action
     * @param action the action 
     * @return the action passed in
     */
    public static void unregisterAction(final String name)
    {
        if (instance.actionMap.get(name) != null)
        {
            instance.actionMap.remove(name);
            
        } else
        {
            log.error("Couldn't find Action with name["+name+"].");
        }
    }
    
    /**
     * Adds Key navigation bindings to a component. (Is this needed?)
     * @param comp the component
     */
    public void addNavBindings(JComponent comp) 
    {
        InputMap inputMap = comp.getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.downAction);
    }
    
    // Create the edit menu
    public JMenu createEditMenu()
    {
       JMenu menu = new JMenu(getResourceString("EDIT"));
       menu.setMnemonic(KeyEvent.VK_E);
       // Undo and redo are actions of our own creation.
       undoAction = (UndoAction) makeAction(UndoAction.class,
                                            this,
                                            "Undo",
                                            null,
                                            null,
                                            new Integer(KeyEvent.VK_Z),
                                            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       register(UNDO, menu.add(undoAction));
       actionMap.put(UNDO, undoAction);
       redoAction = (RedoAction) makeAction(RedoAction.class,
                                            this,
                                            "Redo",
                                            null,
                                            null,
                                            new Integer(KeyEvent.VK_Y),
                                            KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       register(REDO, menu.add(redoAction));
       actionMap.put(REDO, redoAction);
       
       menu.addSeparator();
       // These actions come from the default editor kit.  Get the ones we want
       // and stick them in the menu.
       Action cutAction = makeAction(DefaultEditorKit.CutAction.class,
                                     null,
                                     "Cut",
                                     null,
                                     "Cut selection to clipboard", // I18N ????
                                     new Integer(KeyEvent.VK_X),
                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       register(CUT, menu.add(cutAction));
       cutAction.setEnabled(false);
       actionMap.put(CUT, cutAction);
       
       Action copyAction = makeAction(DefaultEditorKit.CopyAction.class,
                                      null,
                                      "Copy",
                                      null,
                                      "Copy selection to clipboard",
                                      new Integer(KeyEvent.VK_C),
                                      KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       register(COPY, menu.add(copyAction));
       copyAction.setEnabled(false);
       actionMap.put(COPY, copyAction);
       
       Action pasteAction = makeAction(DefaultEditorKit.PasteAction.class,
                                       null,
                                       "Paste",
                                       null,
                                       "Paste contents of clipboard",
                                       new Integer(KeyEvent.VK_V),
                                       KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       pasteAction.setEnabled(false);
       register(PASTE, menu.add(pasteAction));
       actionMap.put(PASTE, pasteAction);
       
       /*
       menu.addSeparator();
       Action selectAllAction = makeAction(SelectAllAction.class,
                                           this,
                                           "Select All",
                                           null,
                                           "Select all text",
                                           new Integer(KeyEvent.VK_A),
                                           KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(selectAllAction);
       */
       launchFindReplaceAction = (LaunchFindReplaceAction) makeAction(LaunchFindReplaceAction.class,
               this,
               "Find",
               null,
               null,
               new Integer(KeyEvent.VK_F),
               KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        //menu.add(launchFindReplaceAction);
//        launchFindReplaceAction.setEnabled(false);
//        register(FIND, menu.add(launchFindReplaceAction));
//        actionMap.put(FIND, launchFindReplaceAction);
        
        launchFindReplaceAction.setEnabled(false);
        register(FIND, menu.add(launchFindReplaceAction));
        actionMap.put(FIND, launchFindReplaceAction);
        
       return menu;
    }
    
    
    /**
     * Enables/Disables Cut/Copy/Paste.
     * @param enable true/false
     */
    public static void enableCutCopyPaste(final boolean enable)
    {
        instance.actionMap.get(CUT).setEnabled(enable);
        instance.actionMap.get(COPY).setEnabled(enable);
        instance.actionMap.get(PASTE).setEnabled(enable);
    }

    /**
     * @param actionClass
     * @param owner
     * @param name
     * @param icon
     * @param toolTip
     * @param mnemonicKeyCode
     * @param acceleratorKey
     * @return
     */
     public Action makeAction(Class<?> actionClass,
                             Object owner,
                             String name,
                             ImageIcon icon,
                             String toolTip,
                             Integer mnemonicKeyCode,
                             KeyStroke acceleratorKey)
    {
        Action a = null;
        try
        {
            Constructor<?> c;
            if (owner != null)
            {
                // If the class is an inner class, its constuctor takes a hidden
                // parameter, an object of it enclosing class.
                c = actionClass.getConstructor(new Class<?>[] { owner.getClass() });
                a = (Action) c.newInstance(new Object[] { owner });
            } else
            {
                c = actionClass.getConstructor((Class<?>[]) null);
                a = (Action) c.newInstance((Object[])null);
            }
            return makeAction(a, name, icon, toolTip, mnemonicKeyCode, acceleratorKey);
            
        } catch (ClassCastException e)
        {
            System.err.println("actionClass argument " + actionClass + " does not implement Action");
            
        } catch (Exception e)
        {
            System.err.println(e + " -- while trying to make an instance of " + actionClass);
            try
            {
                // Output a list of constructors available for this class.
                Constructor<?>[] cc = actionClass.getConstructors();
                for (int i = 0; i < cc.length; i++)
                {
                    System.err.println(cc[i].toString());
                }
            } catch (Exception ee)
            {
                log.error(ee);
            }
        }
        return a;
    }

    /**
     * @param action
     * @param name
     * @param icon
     * @param toolTip
     * @param mnemonicKeyCode
     * @param acceleratorKey
     * @return
     */
    public Action makeAction(Action    action,
                             String    name,
                             ImageIcon icon,
                             String    toolTip,
                             Integer   mnemonicKeyCode,
                             KeyStroke acceleratorKey)
    {
        if (name != null)
            action.putValue(Action.NAME, name);
        
        if (icon != null)
            action.putValue(Action.SMALL_ICON, icon);
        
        if (toolTip != null)
            action.putValue(Action.SHORT_DESCRIPTION, toolTip);
        
        if (mnemonicKeyCode != null)
            action.putValue(Action.MNEMONIC_KEY, mnemonicKeyCode);
        
        if (acceleratorKey != null)
            action.putValue(Action.ACCELERATOR_KEY, acceleratorKey);
            
        return action;
    }

    // This nested class is the child of desperation. If SelectAllAction was a
    // public static nested class of DefaultEditorKit as CopyAction is, this
    // hack wouldn't be needed.
    /*public class SelectAllAction extends AbstractAction
    {
        private final Action realAction = getActionByName(DefaultEditorKit.selectAllAction);

        public SelectAllAction()
        {
            super();
        }

        public SelectAllAction(final UIRegistry uic)
        {
            super();
        }

        public void actionPerformed(ActionEvent e)
        {
            realAction.actionPerformed(e);
        }

    }*/
    
    // The following two methods allow us to find an
    // action provided by the editor kit by its name.
    /*public HashMap<Object, Action> createActionTable(JTextComponent textComponent) 
    {
        actions = new HashMap<Object, Action>();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) 
        {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
        return actions;
    }*/


    /**
     * Returns an action name.
     * @param altName
     * @return
     */
    /*private Action getActionByName(String name) 
    {
        return actions.get(name);
    }*/
    
    //------------------------------------------------------
    //-- An interface for all those wanting to play nice with
    //-- the undo mechanism
    //------------------------------------------------------
    public interface UndoableTextIFace
    {
        /**
         * @return the UndoManager
         */
        public UndoManager getUndoManager();
        
        /**
         * @return the JTextComponent that does undo
         */
        public JTextComponent getTextComponent();
    }

    //------------------------------------------------------
    //-- The UndoAction
    //------------------------------------------------------
    public class UndoAction extends AbstractAction
    {
        protected UndoManager undoManager = null;

        public UndoAction()
        {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (undoManager.canUndo())
                {
                    undoManager.undo();
                }
                
            } catch (CannotUndoException ex)
            {
                ex.printStackTrace();
            }
            updateUndoState();
            undoAction.updateUndoState();
        }

        protected void updateUndoState()
        {
            if (undoManager != null && undoManager.canUndo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getUndoPresentationName());
            } else
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }

        /**
         * @return the undo
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }

        /**
         * @param undo the undo to set
         */
        public void setUndoManager(UndoManager undo)
        {
            this.undoManager = undo;
            setEnabled(undo != null);
        }
    }

    //------------------------------------------------------
    //-- The RedoAction
    //------------------------------------------------------
    public class RedoAction extends AbstractAction
    {
        protected UndoManager undoManager = null;

        public RedoAction()
        {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (undoManager.canRedo())
                {
                    undoManager.redo();
                }
            } catch (CannotRedoException ex)
            {
                log.error("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState()
        {
            if (undoManager != null && undoManager.canRedo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getRedoPresentationName());
            } else
            {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }

        /**
         * @return the undo
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }

        /**
         * @param undo
         *            the undo to set
         */
        public void setUndoManager(UndoManager undo)
        {
            this.undoManager = undo;
            setEnabled(undo != null);
        }
    }
    
    //------------------------------------------------------
    //-- The LaunchFindAction  Action
    //------------------------------------------------------
    public class LaunchFindReplaceAction extends AbstractAction
    {
        protected SearchReplacePanel searchReplacePanel = null;
        
        public LaunchFindReplaceAction()
        {
            super("Find");
            setEnabled(false);
        }       

        @SuppressWarnings("synthetic-access")
        public void actionPerformed(ActionEvent e)
        {
            log.debug("Ctrl-f hit from with UIRegistry - passing action onto the SearchReplacePanel");
            if (this.isEnabled())
            {
                if (searchReplacePanel != null)
                {
                    searchReplacePanel.getLaunchFindAction().actionPerformed(e);
                } else 
                {
                    log.error("search panel is null");
                }
            }
        }

        public void removeSearchPanel()
        {
            this.searchReplacePanel = null;
            setEnabled(false);
        }

        /**
         * @return the undo
         */
        public SearchReplacePanel getSearchReplacePanel()
        {
            return searchReplacePanel;
        }

        /**
         */
        @SuppressWarnings("synthetic-access")
        public void setSearchReplacePanel(SearchReplacePanel panel)
        {
            this.searchReplacePanel = panel;
            if (panel == null)
            {
                 setEnabled(true);
            }
        }
    }
    //------------------------------------------------------
    //--  Listens for edits that can be undone.
    //------------------------------------------------------
    public class UICUndoableEditListener implements UndoableEditListener 
    {
        protected UndoManager undoManager;
        
        public UICUndoableEditListener(final UndoManager undoManager)
        {
            this.undoManager = undoManager;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
         */
        public void undoableEditHappened(UndoableEditEvent e) 
        {
            //Remember the edit and update the menus.
            /*if (undoManager != null)
            {
                undoManager.addEdit(e.getEdit());
            }
            if (undoAction != null)
            {
                undoAction.updateUndoState();
            }
            if (redoAction != null)
            {
                redoAction.updateRedoState();
            }*/
        }
    }
    
    /**
     * @return the redoAction
     */
    public static RedoAction getRedoAction()
    {
        return redoAction;
    }

    /**
     * @return the undoAction
     */
    public static UndoAction getUndoAction()
    {
        return undoAction;
    }

    /**
     * @return the launchFindReplaceAction
     */
    public static LaunchFindReplaceAction getLaunchFindReplaceAction()
    {
        return launchFindReplaceAction;
    }
    
    public static void disableFindFromEditMenu()
    {
        getLaunchFindReplaceAction().setEnabled(false);
        instance.actionMap.get(FIND).setEnabled(false);   
    }
    
    public static void enableFind(final SearchReplacePanel findPanel, final boolean enable)
    {
        getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);
        instance.actionMap.get(FIND).setEnabled(enable);   
    }
    /**
     * @param undoableText
     */
    public void hookUpUndoableEditListener(final UndoableTextIFace undoableText)
    {
        undoableText.getTextComponent().getDocument().addUndoableEditListener(new UICUndoableEditListener(undoableText.getUndoManager()));
    }
    
    /**
     * @param nameStr
     * @param enable
     * @param selected
     */
    public static void enableActionAndMenu(final String nameStr, final boolean enable, final Boolean selected)
    {
        Action action = UIRegistry.getAction(nameStr);
        if (action != null)
        {
            action.setEnabled(enable);
        }
        
        Component comp = UIRegistry.get(nameStr);
        if (comp instanceof JCheckBoxMenuItem)
        {
            ((JCheckBoxMenuItem)comp).setEnabled(enable);
            if (selected != null)
            {
                ((JCheckBoxMenuItem)comp).setSelected(selected);
            }
            
        } else if (comp instanceof JMenuItem)
        {
            ((JMenuItem)comp).setEnabled(enable);
        }
    }


    
    //-----------------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------------
    class ResBundleInfo
    {
        protected String         name;
        protected ResourceBundle resBundle;
        public ResBundleInfo(String name, ResourceBundle resBundle)
        {
            super();
            this.name = name;
            this.resBundle = resBundle;
        }
        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }
        /**
         * @return the resBundle
         */
        public ResourceBundle getResBundle()
        {
            return resBundle;
        }
        
    }
}

