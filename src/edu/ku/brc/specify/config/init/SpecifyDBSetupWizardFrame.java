/* Copyright (C) 2009, University of Kansas Center for Research
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

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.ProcessListUtil;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

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
        
        ImageIcon helpIcon = IconManager.getIcon("WizardIcon", IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$
        
        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            setJMenuBar(menuBar);
        }
        UIRegistry.register(UIRegistry.MENUBAR, menuBar);
        
        setIconImage(IconManager.getIcon("WizardIcon", IconManager.IconSize.Std16).getImage());
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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
                       setTitle(title);
                    }
        });
        
        setTitle(getResourceString("MAIN_TITLE"));
        
        setContentPane(wizPanel);
        
        pack();
    }
    
    /**
     * Check for and kills and existing embedded MySQl processes.
     */
    public static void checkForMySQLProcesses()
    {
        List<Integer> ids = ProcessListUtil.getProcessIdWithText("3337");
        if (ids.size() > 0)
        {
            if (UIHelper.promptForAction("CONTINUE", "CANCEL", "WARNING", getResourceString("Specify.EMBD_KILL_PROCS")))
            {
                for (Integer id : ids)
                {
                    ProcessListUtil.killProcess(id);
                }
            }
            
            
            try
            {
                boolean cont = true;
                while (cont)
                {
                    Thread.sleep(2000);
                    
                    ids = ProcessListUtil.getProcessIdWithText("3337");
                    cont = ids.size()> 0;
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    public boolean doExit(boolean doAppExit)
    {
        DBConnection.setCopiedToMachineDisk(true);
        DBConnection.shutdown();
        HibernateUtil.shutdown();
        
        System.exit(0);
        
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
     * Setup all the System properties. This names all the needed factories. 
     */
    public static void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(DataProviderFactory.factoryName,             "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");         // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(RecordSetFactory.factoryName,                "edu.ku.brc.specify.config.SpecifyRecordSetFactory");          // Needed for Searching //$NON-NLS-1$
        System.setProperty(DBTableIdMgr.factoryName,                    "edu.ku.brc.specify.config.SpecifyDBTableIdMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(BackupServiceFactory.factoryName,            "edu.ku.brc.af.core.db.MySQLBackupService");                   // Needed for Backup and Restore //$NON-NLS-1$
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
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                System.setProperty(symbol, symbol);
            }
        }
        
        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        // For Debugging Only 
        //System.setProperty("mobile", "true");
        
        String mobile = System.getProperty("mobile");
        if (StringUtils.isNotEmpty(mobile))
        {
            UIRegistry.setMobile(true);
        }
        
        String embeddeddbdir = System.getProperty("embeddeddbdir");
        if (StringUtils.isNotEmpty(embeddeddbdir))
        {
            UIRegistry.setEmbeddedDBDir(embeddeddbdir);
        } else
        {
            UIRegistry.setEmbeddedDBDir(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        }
        
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
                
                setUpSystemProperties();
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
        });
    }

}
