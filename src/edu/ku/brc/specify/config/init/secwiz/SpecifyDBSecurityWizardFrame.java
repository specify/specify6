/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.config.init.secwiz;

import static edu.ku.brc.af.ui.ProcessListUtil.getProcessIdWithText;
import static edu.ku.brc.af.ui.ProcessListUtil.killProcess;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
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
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.SpVersion;
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
@SuppressWarnings("serial")
public class SpecifyDBSecurityWizardFrame extends JFrame implements FrameworkAppIFace
{
    //private static final Logger  log = Logger.getLogger(SpecifyDBSecurityWizardFrame.class);
    public enum PROC_STATUS {None, FoundAndKilled, FoundNotKilled}
    
    private String               appVersion          = "6.0"; //$NON-NLS-1$
    private String               appBuildVersion     = "(Unknown)"; //$NON-NLS-1$
 
    /**
     * @throws HeadlessException
     */
    public SpecifyDBSecurityWizardFrame() throws HeadlessException
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
        
        ImageIcon helpIcon = IconManager.getIcon(SpecifyDBSecurityWizard.getIconName(), IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$
        
        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            setJMenuBar(menuBar);
        }
        UIRegistry.register(UIRegistry.MENUBAR, menuBar);
        
        setIconImage(IconManager.getIcon(SpecifyDBSecurityWizard.getIconName(), IconManager.IconSize.NonStd).getImage());
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        HelpMgr.setAppDefHelpId("security_wiz1");


        SpecifyDBSecurityWizard wizPanel = new SpecifyDBSecurityWizard(new SpecifyDBSecurityWizard.WizardListener() {
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
    
    /**
     * @return a list of process IDs for user spawned MySQL processes.
     */
    private static List<Integer> checkForMySQLPrc()
    {
    	return UIHelper.isWindows() ? getProcessIdWithText("_data/bin/mysqld") : getProcessIdWithText("3337");
    }
    
    /**
     * Check for and kills and existing embedded MySQl processes.
     * @return a status as to whether any were found and whether they were killed.
     */
    public static PROC_STATUS checkForMySQLProcesses()
    {
    	PROC_STATUS status = PROC_STATUS.None;
        List<Integer> ids = checkForMySQLPrc();
        if (ids.size() > 0)
        {
        	status = PROC_STATUS.FoundNotKilled;
            if (UIHelper.promptForAction("CONTINUE", "CANCEL", "WARNING", getResourceString("Specify.EMBD_KILL_PROCS")))
            {
                for (Integer id : ids)
                {
                    killProcess(id);
                }
                status = PROC_STATUS.FoundAndKilled;
            }
            
            /*try
            {
                boolean cont = true;
                int cnt = 0;
                while (cont)
                {
                    Thread.sleep(2000);
                    
                    ids = checkForMySQLPrc();
                    cont = ids.size() > 0 && cnt < 5;
                    cnt++;
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }*/
        }
    	return status;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    public boolean doExit(boolean doAppExit)
    {
        // Create Version Record and copy if there is a connection
        if (DBConnection.getInstance().getConnection() != null)
        {
            String resAppVersion = UIRegistry.getAppVersion();
            String dbVersion     = SchemaUpdateService.getInstance().getDBSchemaVersionFromXML();
            SpVersion.createInitialRecord(DBConnection.getInstance().getConnection(), resAppVersion, dbVersion);
    
            if (UIRegistry.isMobile())
            {
                DBConnection.setCopiedToMachineDisk(true);
            }
        }
        
        DBConnection.shutdown();
        HibernateUtil.shutdown();
        
        SwingUtilities.invokeLater(new Runnable() {

            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
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
        
        JMenu helpMenu = UIHelper.createLocalizedMenu(mb, "Specify.HELP_MENU", "Specify.HELP_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
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
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSecurityWizard.class, e);
            e.printStackTrace();
        }
        
        AppBase.processArgs(args);
        AppBase.setupTeeForStdErrStdOut(true, false);
        
        System.setProperty("appdatadir", "..");
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
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
                String VERSION_CHECK = "version_check.auto";
                if (localPrefs.getBoolean(VERSION_CHECK, null) == null)
                {
                    localPrefs.putBoolean(VERSION_CHECK, true);
                }

                String EXTRA_CHECK = "extra.check";
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
                    checkForMySQLProcesses();
                }
                
                Specify.setUpSystemProperties();
                
                final SpecifyDBSecurityWizardFrame wizardFrame = new SpecifyDBSecurityWizardFrame();

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
        });
    }

}
