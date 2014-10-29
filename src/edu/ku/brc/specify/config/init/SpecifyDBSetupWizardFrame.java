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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;

import com.install4j.api.launcher.ApplicationLauncher;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.ProcessListUtil;
import edu.ku.brc.af.ui.ProcessListUtil.PROC_STATUS;
import edu.ku.brc.af.ui.ProcessListUtil.ProcessListener;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2009
 *
 */
public class SpecifyDBSetupWizardFrame extends JFrame implements FrameworkAppIFace
{
    //private static final Logger  log = Logger.getLogger(SpecifyDBSetupWizardFrame.class);
    private final static String VERSION_CHECK = "version_check.auto";
    private final static String EXTRA_CHECK   = "extra.check";

    private String               appVersion          = "6.0"; //$NON-NLS-1$
    private String               appBuildVersion     = "(Unknown)"; //$NON-NLS-1$
 
    private JMenu helpMenu;
    
    /**
     * @throws HeadlessException
     */
    public SpecifyDBSetupWizardFrame() throws HeadlessException
    {
        super();
        
        UIRegistry.loadAndPushResourceBundle("specifydbsetupwiz");
        
        new MacOSAppHandler(this);
        
        UIRegistry.setTopWindow(this);
        
        // Now initialize
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        AppPrefsCache.setUseLocalOnly(true);
        SpecifyAppPrefs.setSkipRemotePrefs(true);
        SpecifyAppPrefs.initialPrefs();
        
        Specify.adjustLocaleFromPrefs();
        
        ImageIcon helpIcon = IconManager.getIcon(SpecifyDBSetupWizard.getIconName(), IconSize.Std32); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$
        
        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            setJMenuBar(menuBar);
        }
        UIRegistry.register(UIRegistry.MENUBAR, menuBar);
        
        setIconImage(IconManager.getIcon(SpecifyDBSetupWizard.getIconName(), IconManager.IconSize.NonStd).getImage());
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        SpecifyDBSetupWizard wizPanel = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Institution, 
                new SpecifyDBSetupWizard.WizardListener() {
                    @Override
                    public void cancelled()
                    {
                        setVisible(false);
                        //dispose();
                        doExit(true);
                    }
                    @Override
                    public void hide()
                    {
                        setVisible(false);
                    }
                    @Override
                    public void finished()
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                dispose();
                                doExit(true);
                            }
                        });
                    }
                    @Override
                    public void panelChanged(String title)
                    {
                       setTitle(getAppTitle(title));
                    }
                    @Override 
                    public void helpContextChanged(String helpTarget) {
                    	if (getHelpMenuItem() != null) {
                    		HelpMgr.registerComponent(getHelpMenuItem(), helpTarget);
                    	}
                    }
                    
        });
        
        setTitle(getAppTitle(getResourceString("MAIN_TITLE")));
        
        setContentPane(wizPanel);
        
        pack();
    }
    
    /**
     * (To be replaced by method in AppBase)
     */
    protected String getAppTitle(final String titleStr)
    {
        String resAppVersion = UIRegistry.getAppVersion();
        if (StringUtils.isNotEmpty(resAppVersion))
        {
            appVersion = resAppVersion;
        }
        
        return AppBase.getTitle(appVersion, appBuildVersion, titleStr);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    public boolean doExit(boolean doAppExit)
    {
        if (UIRegistry.isMobile())
        {
            DBConnection.setCopiedToMachineDisk(true);
        }
        
        DBConnection.shutdown();
        HibernateUtil.shutdown();
        
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run()
            {
                if (UIRegistry.isEmbedded() || UIRegistry.isMobile())
                {
                    DBConnection.shutdownFinalConnection(true, false); // true means System.exit
                } else
                {
                    System.exit(0);
                }
            }
        });
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doAbout()
     */
    public void doAbout()
    {
        Specify specify = new Specify();
        specify.doAbout();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doPreferences()
     */
    public void doPreferences()
    {
        
    }
    
    public JMenuItem getHelpMenuItem() {
    	JMenuItem result = null;
    	if (helpMenu != null && helpMenu.getItemCount() > 0) { 
    		result = helpMenu.getItem(0);
    	}
    	return result;
    }
    /**
     * @return
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;

        //--------------------------------------------------------------------
        //-- File Menu
        //--------------------------------------------------------------------

        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            JMenu menu = UIHelper.createLocalizedMenu(mb, "Specify.FILE_MENU", "Specify.FILE_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
            
            menu.addSeparator();
            String title = "Specify.EXIT"; //$NON-NLS-1$
            String mnu = "Specify.Exit_MNEU"; //$NON-NLS-1$
            mi = UIHelper.createLocalizedMenuItem(menu, title, mnu, title, true, null); 
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            doExit(true);
                        }
                    });
        }
        
        helpMenu = UIHelper.createLocalizedMenu(mb, "Specify.HELP_MENU", "Specify.HELP_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
        HelpMgr.createHelpMenuItem(helpMenu, "Specify"); //$NON-NLS-1$
        helpMenu.addSeparator();
        
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            String ttle = "Specify.ABOUT";//$NON-NLS-1$ 
            String mneu = "Specify.ABOUTMNEU";//$NON-NLS-1$ 
            String desc = "Specify.ABOUT";//$NON-NLS-1$ 
            mi = UIHelper.createLocalizedMenuItem(helpMenu,ttle , mneu, desc,  true, null); 
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                           doAbout();
                        }
                    });
        }
        return mb;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
        try
        {
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
            }
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, e);
            e.printStackTrace();
        }
        
        AppBase.processArgs(args);
        AppBase.setupTeeForStdErrStdOut(true, false);
        
        System.setProperty("appdatadir", "..");
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        //try {
        //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+(new File(UIRegistry.getAppDataDir()).getCanonicalPath())+"]");
        //} catch (IOException ex) {}
        
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        // Check to see if we should check for a new version
        if (localPrefs.getBoolean(VERSION_CHECK, null) == null)
        {
            localPrefs.putBoolean(VERSION_CHECK, true);
        }

        if (localPrefs.getBoolean(EXTRA_CHECK, null) == null)
        {
            localPrefs.putBoolean(EXTRA_CHECK, true);
        }
        
        if (UIHelper.isLinux())
        {
            Specify.checkForSpecifyAppsRunning();
        }
        
        if (UIRegistry.isEmbedded())
        {
            ProcessListUtil.checkForMySQLProcesses(new ProcessListener()
            {
                @Override
                public void done(PROC_STATUS status) // called on the UI thread
                {
                    if (status == PROC_STATUS.eOK || status == PROC_STATUS.eFoundAndKilled)
                    {
                        startupContinuing();
                    }
                }
            });
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    startupContinuing();
                }
            });
        }
    }

    /**
     * 
     */
    private static void startupContinuing() // needs to be called on the UI Thread
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        Specify.setUpSystemProperties();
        final SpecifyDBSetupWizardFrame wizardFrame = new SpecifyDBSetupWizardFrame();

        if (localPrefs.getBoolean(VERSION_CHECK, true) && localPrefs.getBoolean(EXTRA_CHECK, true))
        {
            try
            {
               com.install4j.api.launcher.SplashScreen.hide();
               ApplicationLauncher.Callback callback = new ApplicationLauncher.Callback()
               {
                   public void exited(int exitValue)
                   {
                       UIHelper.centerAndShow(wizardFrame);
                   }
                   public void prepareShutdown()
                   {
                       
                   }
                };
                ApplicationLauncher.launchApplication("100", null, true, callback);
                
            } catch (Exception ex)
            {
                UIHelper.centerAndShow(wizardFrame);
            }
        } else
        {
            UIHelper.centerAndShow(wizardFrame);
        }
    }

}
