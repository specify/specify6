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

package edu.ku.brc.specify;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MainPanel;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsEditor;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.LoggerDialog;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.JasperReportsCache;
import edu.ku.brc.specify.tests.SpecifyAppPrefs;
import edu.ku.brc.specify.ui.CollectorActionListener;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DefaultClassActionHandler;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.db.DatabaseLoginPanel;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.CacheManager;
import edu.ku.brc.util.FileCache;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.MemoryWarningSystem;
import edu.ku.brc.util.thumbnails.Thumbnailer;
/**
 * Specify Main Application Class
 *
 * @code_status Beta
 * 
 * @author rods
 */
@SuppressWarnings("serial")
public class Specify extends JPanel implements DatabaseLoginListener
{
    private static final boolean isRelease          = true;
    private static final Logger  log                = Logger.getLogger(Specify.class);
    
    public static final boolean IS_DEVELOPMENT     = true;
    
    // The preferred size of the demo
    private static final int    PREFERRED_WIDTH    = 900;
    private static final int    PREFERRED_HEIGHT   = 800;

    private static Specify      specifyApp         = null; // needed for ActionListeners etc.

    // Status Bar
    private JStatusBar          statusField        = null;
    private JMenuBar            menuBar            = null;
    private JFrame              topFrame           = null;
    private MainPanel           mainPanel          = null;
    private JMenuItem           changeCatSeriesBtn = null;

    protected boolean           hasChanged         = false;

    protected String             currentDatabaseName = null;
    protected DatabaseLoginPanel dbLoginPanel        = null;
    protected String             databaseName        = null;
    protected String             userName            = null;

    protected GhostGlassPane     glassPane;

    private boolean              isWorkbenchOnly     = true;
    
    private String               appName             = "Specify";
    private String               appVersion          = "6.0";
    private String               appBuildVersion     = "200705091402";
    
    protected static CacheManager cacheManager        = new CacheManager();

    /**
     * Constructor.
     */
    public Specify()
    {
        UIRegistry.setRelease(isRelease);
        
    }
    
    /**
     * The very very first step in initializing Specify. 
     */
    protected void preStartUp()
    {
        //UIHelper.attachUnhandledException();
        
        // we simply need to create this class, not use it
        //@SuppressWarnings("unused") MacOSAppHandler macoshandler = new MacOSAppHandler(this);
        new MacOSAppHandler(this);

        // Name factories
        setUpSystemProperties();
    }
    
    /**
     * Starts up Specify with the initializer that enables the user to create a new empty database. 
     */
    public void startWithInitializer(final boolean doLoginOnly, final boolean assumeDerby)
    {
        preStartUp();
        
        if (true)
        {
            SpecifyInitializer specifyInitializer = new SpecifyInitializer(doLoginOnly, assumeDerby);
            specifyInitializer.setup(this);
            
        } else
        {
            startUp();
        }
    }
    
    /**
     * Start up without the initializer, assumes there is at least one database to connect to.
     */
    public void startUp()
    {
    	log.debug("StartUp");
        // Insurance
        if (StringUtils.isEmpty(UIRegistry.getJavaDBPath()))
        {
        	File userDataDir = new File(UIRegistry.getAppDataDir() + File.separator + "DerbyDatabases");
            UIRegistry.setJavaDBDir(userDataDir.getAbsolutePath());
        }
        log.debug(UIRegistry.getJavaDBPath());
        
        // Attachment related helpers
        Thumbnailer thumb = new Thumbnailer();
        File thumbnailDir = null;
        try
        {
            thumbnailDir = XMLHelper.getConfigDir("thumbnail_generators.xml");
            thumb.registerThumbnailers(thumbnailDir.getAbsolutePath());
        }
        catch (Exception e1)
        {
            throw new RuntimeException("Couldn't find thumbnailer xml ["+(thumbnailDir != null ? thumbnailDir.getAbsolutePath() : "")+"]");
        }
        thumb.setQuality(.5f);
        thumb.setMaxHeight(128);
        thumb.setMaxWidth(128);

        AttachmentManagerIface attachMgr = null;
        
        File location = UIRegistry.getAppDataSubDir("AttachmentStorage", true);
        try
        {
            attachMgr = new FileStoreAttachmentManager(location);
        }
        catch (IOException e1)
        {
            log.warn("Problems setting the FileStoreAttachmentManager at ["+location+"]");
            // TODO RELEASE -  Instead of exiting we need to disable Attchements
            throw new RuntimeException("Problems setting the FileStoreAttachmentManager at ["+location+"]");
        }
        
        AttachmentUtils.setAttachmentManager(attachMgr);
        AttachmentUtils.setThumbnailer(thumb);
        ActionListener attachmentDisplayer = AttachmentUtils.getAttachmentDisplayer();
        DefaultClassActionHandler.getInstance().registerActionHandler(Attachment.class, attachmentDisplayer);
        DefaultClassActionHandler.getInstance().registerActionHandler(Collector.class, new CollectorActionListener());
        
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        //localPrefs.load(); moved to end for not-null constraint
        /*String         derbyPath  = localPrefs.get("javadb.location", null);
        if (StringUtils.isNotEmpty(derbyPath))
        {
            UIRegistry.setJavaDBDir(derbyPath);
            log.debug("JavaDB Path: "+UIRegistry.getJavaDBPath());
        }*/
        
        UsageTracker.incrUsageCount("RunCount");
        
        UIHelper.attachUnhandledException();

        FileCache.setDefaultPath(UIRegistry.getAppDataDir()+ File.separator + "cache");

        cacheManager.registerCache(UIRegistry.getLongTermFileCache());
        cacheManager.registerCache(UIRegistry.getShortTermFileCache());
        cacheManager.registerCache(JasperReportsCache.getInstance());
        
        
        UIRegistry.register(UIRegistry.MAINPANE, this); // important to be done immediately
 
        specifyApp = this;
        
//        SystemTray sysTray = SystemTray.getSystemTray();
//        PopupMenu popup = new PopupMenu("Sp6");
//        MenuItem exitItem = new MenuItem("Exit");
//        exitItem.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent ae)
//            {
//                doExit();
//            }
//        });
//        popup.add(exitItem);
//        TrayIcon sp6icon = new TrayIcon(IconManager.getIcon("Specify16").getImage(),"Sepcify 6",popup);
//        try
//        {
//            sysTray.add(sp6icon);
//        }
//        catch (AWTException e1)
//        {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        
        // Setup base font AFTER setting Look and Feel
        UIRegistry.setBaseFont((new JLabel()).getFont());

        log.info("Creating Database configuration ");

        if (!isWorkbenchOnly)
        {
            HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener());
            HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener());
            // SInce Update get called when deleting an object there is no need to register this class.
            // The update deletes becuase first it removes the Lucene document and then goes to add it back in, but since the
            // the record is deleted it doesn't get added.
            HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener());
            //HibernateUtil.setListener("delete", new edu.ku.brc.specify.dbsupport.DeleteEventListener());
        }
        dbLoginPanel = UIHelper.doLogin(true, false, false, this); // true means do auto login if it can, second bool means use dialog instead of frame
        localPrefs.load();
    }
    
    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    protected void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI
        System.setProperty("edu.ku.brc.ui.db.TreeFinderFactory",        "edu.ku.brc.specify.treeutils.TreeFinderFactoryImpl");  // needed for treequerycbx components
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory");
    }

    /**
     * Creates the initial panels that will be shown at start up and sets up the Application Context
     * @param databaseNameArg the database name
     * @param userNameArg the user name
     */
    protected void initStartUpPanels(final String databaseNameArg, final String userNameArg)
    {

        if( !SwingUtilities.isEventDispatchThread() )
        {
            SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        initStartUpPanels(databaseNameArg, userNameArg);
                    }
                });
            return;
        }
        
 
        TaskMgr.readRegistry();
        
        TaskMgr.initializePlugins();

        validate();

        add(mainPanel, BorderLayout.CENTER);
        doLayout();

        mainPanel.setBackground(Color.WHITE);

        JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
        if (toolBar != null && toolBar.getComponentCount() < 2)
        {
            toolBar.setVisible(false);
        }
        
        TaskMgr.requestInitalContext();

        showApp();
    }

    /**
     * Determines if this is an applet or application
     */
    public boolean isApplet()
    {
        return false;
    }


    /**
     * General Method for initializing the class
     *
     */
    private void initialize(GraphicsConfiguration gc)
    {
        setLayout(new BorderLayout());

        // set the preferred size of the demo
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setPreferredSize(new Dimension(1024, 768)); // For demo

        topFrame = new JFrame(gc);
        topFrame.setIconImage(IconManager.getImage("AppIcon").getImage());
        //topFrame.setAlwaysOnTop(true);
        
        topFrame.setGlassPane(glassPane = new GhostGlassPane());
        topFrame.setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UIRegistry.register(UIRegistry.GLASSPANE, glassPane);

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        UIRegistry.register(UIRegistry.TOPFRAME, topFrame);

        menuBar = createMenus();
        if (menuBar != null)
        {
            //top.add(menuBar, BorderLayout.NORTH);
            topFrame.setJMenuBar(menuBar);
        }
        UIRegistry.register(UIRegistry.MENUBAR, menuBar);


        JToolBar toolBar = createToolBar();
        if (toolBar != null)
        {
            top.add(toolBar, BorderLayout.CENTER);
        }
        UIRegistry.register(UIRegistry.TOOLBAR, toolBar);

        mainPanel = new MainPanel();

        int[] sections = {30, 50};
        statusField = new JStatusBar(sections);
        statusField.setErrorIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16));
        statusField.setWarningIcon(IconManager.getIcon("Warning", IconManager.IconSize.Std16));
        UIRegistry.setStatusBar(statusField);

        add(statusField, BorderLayout.SOUTH);

    }

    /**
     *
     * @return the toolbar for the app
     */
    public JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new ToolbarLayoutManager(2, 2));

        return toolBar;
    }

    /**
     * Create menus
     */
    public void preferences()
    {
        PreferencesDlg dlg = new PreferencesDlg(false);
        dlg.setVisible(true);
    }

    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;

        JMenu menu = UIHelper.createMenu(mb, "FileMenu", "FileMneu");
        mi = UIHelper.createMenuItem(menu, "Login", "L", "Database Login", true, null);
        mi.addActionListener(new ActionListener()
                {
                    @SuppressWarnings("synthetic-access")
                    public void actionPerformed(ActionEvent ae)
                    {
                        class DBListener implements DatabaseLoginListener
                        {
                            @SuppressWarnings("synthetic-access")
                            public void loggedIn(final String databaseNameArg, final String userNameArg)
                            {
                                specifyApp.loggedIn(databaseNameArg, userNameArg);
                            }

                            public void cancelled()
                            {
                                // Do not call this it will exit the application
                                //specifyApp.cancelled();
                            }
                        }

                        if (SubPaneMgr.getInstance().aboutToShutdown())
                        {
                            // Make sure the prefs are saved before logging out and loggin back in.
                            try
                            {
                                AppPreferences.getLocalPrefs().flush();
                                AppPreferences.getRemote().flush();
                                
                            } catch (BackingStoreException ex)
                            {
                                log.error(ex);
                            }
                            
                            AppPreferences.setConnectedToDB(false);
                            UIHelper.doLogin(false, true, true, new DBListener()); // true means do auto login if it can, second bool means use dialog instead of frame
                        }
                    }
                });
        mi.setEnabled(!isWorkbenchOnly);
        
        if (!isWorkbenchOnly)
        {
            // Add Menu for switching CatalogSeries
            changeCatSeriesBtn = UIHelper.createMenuItem(menu, "Change Catalog Series", "C", "Change Catalog Series", false, null);
            changeCatSeriesBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            if (SubPaneMgr.getInstance().aboutToShutdown())
                            {
                               
                                // Actually we really need to start over
                                // "true" means that it should NOT use any cached values it can find to automatically initialize itself
                                // instead it should ask the user any questions as if it were starting over
                                restartApp(databaseName, userName, true, false);
                            }
                        }
                    });
    
            changeCatSeriesBtn.setEnabled(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getNumOfCatalogSeriesForUser() > 1);
        }

        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            menu.addSeparator();
            mi = UIHelper.createMenuItem(menu, "Exit", "x", "Exit Appication", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            doExit();
                        }
                    });
        }
       
        menu = UIRegistry.getInstance().createEditMenu();
        mb.add(menu);
        //menu = UIHelper.createMenu(mb, "EditMenu", "EditMneu");
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            menu.addSeparator();
            mi = UIHelper.createMenuItem(menu, "Preferences", "P", "Preferences", false, null);
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            preferences();
                        }
                    });
            mi.setEnabled(true);
        }
                


        /*JMenuItem mi2;
        JMenu fileMenu2 = (JMenu) mb.add(new JMenu("Log off"));


        fileMenu2.setMnemonic('O');
        mi2 = UIHelper.createMenuItem(fileMenu2, "Log off", "O", "Log off database", false, null);
        mi2.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        if (hasChanged)
                        {

                        }
                        try {
                            if (mSessionFactory != null)
                            {
                                mSessionFactory.close();
                            }
                            if (mSession != null)
                            {
                                mSession.close();
                            }
                        } catch (Exception e)
                        {
                            log.error("UIHelper.createMenus - ", e);
                        }
                        //frame.dispose();
                        final Window parentWindow = SwingUtilities.getWindowAncestor(Specify.this);
                        parentWindow.dispose();
                        Specify ha = new Specify(grc);
                    }
                });
        */
        
        if (!isWorkbenchOnly)
        {
            menu = UIHelper.createMenu(mb, "AdvMenu", "AdvMneu");
            mi = UIHelper.createMenuItem(menu, getResourceString("ESConfig"), getResourceString("ESConfig_Mn"), getResourceString("ESConfig"), true, null);
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            ExpressSearchTask expressSearchTask = (ExpressSearchTask)ContextMgr.getTaskByName(ExpressSearchTask.EXPRESSSEARCH);
                            expressSearchTask.showIndexerPane();
                        }
                    });
            
            menu.add(UIHelper.createMenu(mb, "SystemMenu", "SystemMneu"));
        }


        menu = UIHelper.createMenu(mb, "TabsMenu", "TabsMneu");
        /*mi = UIHelper.createMenuItem(menu, "Close Current", "C", "Close C", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        SubPaneMgr.getInstance().closeCurrent();
                    }
                });*/

        Action closeAll = new AbstractAction() {
            public void actionPerformed(ActionEvent ae)
            {
                SubPaneMgr.getInstance().closeAll();
            }
        };
        mi = UIHelper.createMenuItemWithAction(menu, "Close All", "A", "Close All", false, closeAll);
        UIRegistry.registerAction("CloseAll", closeAll);

        if (!isRelease)
        {
            menu = UIHelper.createMenu(mb, "DebugMenu", "DebugMneu");
            mi = UIHelper.createMenuItem(menu, "Show Local Prefs", "L", "Show Local Prefs", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("synthetic-access")
                        public void actionPerformed(ActionEvent ae)
                        {
                            final JDialog dialog = new JDialog(topFrame, "Local Prefs", true);
                            dialog.setContentPane(new AppPrefsEditor(false));
                            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                            dialog.pack();
                            UIHelper.centerAndShow(dialog);
                        }
                    });
    
            mi = UIHelper.createMenuItem(menu, "Show Remote Prefs", "R", "Show Remote Prefs", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("synthetic-access")
                        public void actionPerformed(ActionEvent ae)
                        {
                            final JDialog dialog = new JDialog(topFrame, "Remote Prefs", true);
                            dialog.setContentPane(new AppPrefsEditor(true));
                            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                            dialog.pack();
                            UIHelper.centerAndShow(dialog);
                        }
                    });
    
            menu.addSeparator();
            JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Reload Views");
            menu.add(cbMenuItem);
            //mi = UIHelper.createMenuItem(menu, "Reload Views", "V", "Reload Views", false, null);
            cbMenuItem.setSelected(AppPreferences.getLocalPrefs().getBoolean("reload_views", false));
            cbMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae)
                        {
                            boolean isReload = !AppPreferences.getLocalPrefs().getBoolean("reload_views", false);                       
                            AppPreferences.getLocalPrefs().putBoolean("reload_views", isReload);
                            ((JMenuItem)ae.getSource()).setSelected(isReload);
                        }});
    
            mi = UIHelper.createMenuItem(menu, "Config Loggers", "C", "Config Logger", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("synthetic-access")
                        public void actionPerformed(ActionEvent ae)
                        {
                            final LoggerDialog dialog = new LoggerDialog(topFrame);
                            UIHelper.centerAndShow(dialog);
                        }
                    });
            
            JMenu prefsMenu = new JMenu("Prefs Import/Export");
            menu.add(prefsMenu);
            mi = UIHelper.createMenuItem(prefsMenu, "Import", "I", "Import Prefs", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("synthetic-access")
                        public void actionPerformed(ActionEvent ae)
                        {
                            importPrefs();
                        }
                    });
    
            mi = UIHelper.createMenuItem(prefsMenu, "Export", "E", "Export Prefs", true, null);
            mi.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("synthetic-access")
                        public void actionPerformed(ActionEvent ae)
                        {
                            exportPrefs();
                        }
                    });

        }
        
        JMenu helpMenu = UIHelper.createMenu(mb, "Help", "HelpMneu");
        HelpMgr.createHelpMenuItem(helpMenu, "Specify");
        helpMenu.addSeparator();
                
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
           mi = UIHelper.createMenuItem(helpMenu, getResourceString("About"), getResourceString("AboutMneu"), getResourceString("About"), true, null);
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
     * Checks to see if cache has changed before exiting.
     */
    protected void doAbout()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel iconLabel = new JLabel(IconManager.getIcon("SpecifyLargeIcon"));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 8));
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(new JLabel("<html>"+appName+" " + appVersion + 
                "<br><br>Biodiversity Research Center<br>University of Kansas<br>Lawrence, KS  USA 66045<br><br>" + 
                "www.specifysoftware.org<br>specify@ku.edu<br><br>" + 
                "<p>The Specify Software Project is<br>"+
                "funded by the Biological Databases<br>"+
                "and Informatics Program of the<br>"+
                "U.S. National Science Foundation <br>(Award DBI-0446544)</P><br>" +
                "Build: " + appBuildVersion + 
                "</html>"), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,0,6));
        CustomDialog aboutDlg = new CustomDialog(topFrame, getResourceString("About") + " " +appName, true, CustomDialog.OK_BTN, panel);
        aboutDlg.setOkLabel(getResourceString("Close"));
        UIHelper.centerAndShow(aboutDlg);
    }

    /**
     * Checks to see if cache has changed before exiting.
     *
     */
    protected void doExit()
    {
        if (AttachmentUtils.getAttachmentManager() != null)
        {
            AttachmentUtils.getAttachmentManager().cleanup();
        }
        
        AppPreferences.shutdownLocalPrefs();
        
        if (SubPaneMgr.getInstance().aboutToShutdown())
        {
    		log.info("Application shutdown");
    
    		// save the long term cache mapping info
    		try
    		{
    			UIRegistry.getLongTermFileCache().saveCacheMapping();
    			log.info("Successfully saved long term cache mapping");
    		}
    		catch( IOException e1 )
    		{
    			log.warn("Error while saving long term cache mapping.",e1);
    		}
    
            if (topFrame != null)
            {
                topFrame.setVisible(false);
            }
            System.exit(0);
        }
    }

    /**
     * Bring up the PPApp demo by showing the frame (only applicable if coming up
     * as an application, not an applet);
     */
    public void showApp()
    {
        // put PPApp in a frame and show it
        JFrame f = getFrame();
        f.setTitle("Specify 6.0");
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        f.addWindowListener(new WindowAdapter()
        		{
        			@Override
                    public void windowClosing(WindowEvent e)
        			{
        				doExit();
        			}
        		});
        UIHelper.centerAndShow(f);
    }
    
    /**
     * Returns the frame instance
     */
    public JFrame getFrame()
    {
      return topFrame;
    }

    /**
     * Returns the menubar
     */
    public JMenuBar getMenuBar()
    {
      return menuBar;
    }

    /**
     * Set the status
     */
    public void setStatus(final String s)
    {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SpecifyRunnable(this, s)
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void run()
            {
                mApp.statusField.setText((String) obj);
            }
        });
    }
    
    
    /**
     * Restarts the app with a new or old database and user name and creates the core app UI.
     * @param databaseNameArg the database name
     * @param userNameArg the user name
     * @param startOver tells the AppContext to start over
     * @param firstTime indicates this is the first time in the app and it should create all the UI for the core app
     */
    public void restartApp(final String databaseNameArg, final String userNameArg, final boolean startOver, final boolean firstTime)
    {
        if (dbLoginPanel != null)
        {
            dbLoginPanel.getStatusBar().setText(getResourceString("InitializingApp"));
        }
        
        AppPreferences.shutdownRemotePrefs();
        //moved here because context needs to be set before loading prefs, we need to know the SpecifyUser
        AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(databaseNameArg, userNameArg, startOver);
        SpecifyAppPrefs.initialPrefs();

        
        //CatalogSeries.setCurrentCatalogSeries(null);
        //CollectionObjDef.setCurrentCollectionObjDef(null);
        
        // "false" means that it should use any cached values it can find to automatically initialize itself
        //AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(databaseNameArg, userNameArg, startOver);
        if (status == AppContextMgr.CONTEXT_STATUS.OK)
        { 
            if (firstTime)
            {
                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                
                initialize(gc);
    
                topFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                UIRegistry.register(UIRegistry.FRAME, topFrame);
            }
            
            initStartUpPanels(databaseNameArg, userNameArg);
            
            if (changeCatSeriesBtn != null)
            {
                changeCatSeriesBtn.setEnabled(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getNumOfCatalogSeriesForUser() > 1);
            }
            
        } else if (status == AppContextMgr.CONTEXT_STATUS.Error)
        {

            if (dbLoginPanel != null)
            {
                dbLoginPanel.getWindow().setVisible(false);
            }
            
            if (CatalogSeries.getCurrentCatalogSeries().size() == 0)
            {
                
                // TODO This is really bad because there is a Database Login with no Specify login
                JOptionPane.showMessageDialog(null, 
                                              getResourceString("LoginUserMismatch"), 
                                              getResourceString("LoginUserMismatchTitle"), 
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        
        }
        
        CommandDispatcher.dispatch(new CommandAction("App", "Restart", null));
        
        if (dbLoginPanel != null)
        {
            dbLoginPanel.getWindow().setVisible(false);
            dbLoginPanel = null;
        }
    }
    
    /**
     * 
     */
    protected void importPrefs()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showDialog(null, "Select File or Directory") != JFileChooser.CANCEL_OPTION) // XXX I18N
        {
            File destFile = chooser.getSelectedFile();
            
            Properties properties = new Properties();
            try
            {
                properties.load(new FileInputStream(destFile));
                AppPreferences remotePrefs = AppPreferences.getRemote();
                
                for (Object key : properties.keySet())
                {
                    String keyStr = (String)key;
                    remotePrefs.getProperties().put(keyStr, properties.get(key)); 
                }
                
            } catch (Exception ex)
            {
                log.error(ex); // XXX Error Dialog
            }
            
        } else 
        {
            throw new NoSuchElementException("The External File Repository needs a valid directory.");// XXX LOCALIZE
        } 
    }

    /**
     * 
     */
    protected void exportPrefs()
    {
        AppPreferences remotePrefs = AppPreferences.getRemote();
        Properties     props       = remotePrefs.getProperties();
        try
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showDialog(null, "Select File or Directory") != JFileChooser.CANCEL_OPTION) // XXX LOCALIZE
            {
                File destFile = chooser.getSelectedFile();
                props.store(new FileOutputStream(destFile), "User Prefs");
            } else 
            {
                throw new NoSuchElementException("The External File Repository needs a valid directory.");// XXX LOCALIZE
            } 
            
        } catch (Exception ex)
        {
            log.error(ex); // XXX Error Dialog
        }
    }

    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.lang.String, java.lang.String)
     */
    public void loggedIn(final String databaseNameArg, final String userNameArg)
    {
        boolean firstTime = this.databaseName == null;
        
        this.databaseName = databaseNameArg;
        this.userName     = userNameArg;
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        if (localPrefs.get("startup.lastname", null) != null)
        {
            String userNameStr  = AppPreferences.getLocalPrefs().get("startup.username",  null);
            String passwordStr  = Encryption.decrypt(AppPreferences.getLocalPrefs().get("startup.password",  null));
            
            String firstNameStr = AppPreferences.getLocalPrefs().get("startup.firstname", null);
            String lastNameStr  = AppPreferences.getLocalPrefs().get("startup.lastname",  null);
            String emailStr     = AppPreferences.getLocalPrefs().get("startup.email",     null);
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            //for (SpecifyUser su : session.getDataList(SpecifyUser.class))
            //{
            //    System.out.println("["+su.getName()+"]");
            //}

            List list = session.getDataList(SpecifyUser.class, "name", "guest");
            if (list.size() == 1)
            {
                SpecifyUser user      = (SpecifyUser)list.get(0);
                Agent       userAgent = user.getAgent();
                if (StringUtils.isNotEmpty(userNameStr))
                {
                    user.setName(userNameStr);
                }
                userAgent.setFirstName(firstNameStr);
                userAgent.setLastName(lastNameStr);
                userAgent.setEmail(emailStr);
                
                try
                {
                    session.beginTransaction();
                    session.saveOrUpdate(userAgent);
                    session.saveOrUpdate(user);
                    session.commit();
                    session.flush();
                    
                    AppPreferences.getLocalPrefs().remove("startup.username");
                    AppPreferences.getLocalPrefs().remove("startup.password");
                    AppPreferences.getLocalPrefs().remove("startup.firstname");
                    AppPreferences.getLocalPrefs().remove("startup.lastname");
                    AppPreferences.getLocalPrefs().remove("startup.email");
                    
                    if (StringUtils.isNotEmpty(userNameStr))
                    {
                        this.userName = userNameStr;
                        AppPreferences.getLocalPrefs().put("login.username", userNameStr);
                        AppPreferences.getLocalPrefs().put("login.password", Encryption.encrypt(passwordStr));
                    }
                    
                    try
                    {
                        AppPreferences.getLocalPrefs().flush();
                        
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    
                } finally
                {
                    try
                    {
                        session.close();
                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                }
                
            } else
            {
                throw new RuntimeException("The user ["+userName+"] could  not be located as a Specify user.");
            }
        }
        
        restartApp(databaseName, userName, false, firstTime);
        
        statusField.setSectionText(0, userName);
        statusField.setSectionText(1, databaseName);
        
        AppPreferences.setConnectedToDB(true);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }


    // *******************************************************
    // *****************   Static Methods  *******************
    // *******************************************************

    /**
     *
     * @return the specify app object
     */
    public static Specify getSpecify()
    {
        return specifyApp;
    }
    
    /**
     * @return the cache manager
     */
    public static CacheManager getCacheManager()
    {
        return cacheManager;
    }

  // *******************************************************
  // ******************   Runnables  ***********************
  // *******************************************************

  /**
   * Generic Specify runnable. This is intended to run on the
   * AWT gui event thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a Specify and an Object
   * as arguments, which gives subtypes of this class the two
   * "must haves" needed in most runnables for this demo.
   */
  class SpecifyRunnable implements Runnable
  {

      protected Specify mApp;

      protected Object    obj;

      public SpecifyRunnable(Specify aApp, Object obj)
      {
        this.mApp = aApp;
        this.obj = obj;
      }

      public void run()
      {
          // do nothing
      }
  }

  //-----------------------------------------------------------------------------
  //-- Application MAIN
  //-----------------------------------------------------------------------------

  /**
   *
   */
  public static void main(String[] args)
  {
      log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]");
      // This is for Windows and Exe4J, turn the args into System Properties
	  for (String s : args)
	  {
		  String[] pairs = s.split("=");
		  if (pairs.length == 2)
		  {
			  log.debug("["+pairs[0]+"]["+pairs[1]+"]");
              if (pairs[0].startsWith("-D"))
              {
				  System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
			  } 
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
      
      String javadbdir = System.getProperty("javadbdir");
      if (StringUtils.isNotEmpty(javadbdir))
      {
          UIRegistry.setJavaDBDir(javadbdir);
      }
      
      SwingUtilities.invokeLater(new Runnable() {
          public void run()
          {
    	      // Set App Name, MUST be done very first thing!
              UIRegistry.setAppName("Specify");              
              // Then set this, which        
        	  IconManager.setApplicationClass(Specify.class);
              
              if (!isRelease)
              {
                  MemoryWarningSystem.setPercentageUsageThreshold(0.75);
    
                  MemoryWarningSystem mws = new MemoryWarningSystem();
                  mws.addListener(new MemoryWarningSystem.Listener()
                  {
                      protected void setMessage(final String msg, final boolean isError)
                      {
                          JStatusBar statusBar = UIRegistry.getStatusBar();
                          if (statusBar != null)
                          {
                              if (isError)
                              {
                                  statusBar.setErrorMessage(msg);
                              } else
                              {
                                  statusBar.setText(msg);
                              }
                          } else
                          {
                              System.err.println(msg);
                          }
                      }
                      
                      public void memoryUsage(long usedMemory, long maxMemory)
                      {
                          double percentageUsed = ((double) usedMemory) / maxMemory;
                          
                          String msg = String.format("Percent Memory Used %6.2f of Max %d", new Object[] {(percentageUsed * 100.0), maxMemory});
                          setMessage(msg, false);
    
                      }
    
                      public void memoryUsageLow(long usedMemory, long maxMemory)
                      {
                          double percentageUsed = ((double) usedMemory) / maxMemory;
                            
                          String msg = String.format("Memory is Low! Percentage Used = %6.2f of Max %d", new Object[] {(percentageUsed * 100.0), maxMemory});
                          setMessage(msg, true);
                            
                          if (MemoryWarningSystem.getThresholdPercentage() < 0.8)
                          {
                              MemoryWarningSystem.setPercentageUsageThreshold(0.8);
                          }
                        }
                    });
              }
              
              try
              {
                  UIHelper.OSTYPE osType = UIHelper.getOSType();
                  if (osType == UIHelper.OSTYPE.Windows )
                  {
                      //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                      UIManager.setLookAndFeel(new PlasticLookAndFeel());
                      PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                      
                  } else if (osType == UIHelper.OSTYPE.Linux )
                  {
                      //UIManager.setLookAndFeel(new GTKLookAndFeel());
                      UIManager.setLookAndFeel(new PlasticLookAndFeel());
                      //PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                      //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                      //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                      //PlasticLookAndFeel.setPlasticTheme(new DesertGreen());
                     
                  }
              }
              catch (Exception e)
              {
                  log.error("Can't change L&F: ", e);
              }
              
              
              HelpMgr.initializeHelp("SpecifyHelp");
              
              // Startup Specify
              Specify specify = new Specify();
              
              
              boolean startAsWorkBench = false; // XXX Workbench Testing (start up testing)
              if (startAsWorkBench)
              {
                  // For a WorkBench Only Release  
                  specify.startWithInitializer(true, true);  // true, true means doLoginOnly and assume Derby
                  
              } else
              {
                  // THis type of start up ALWAYS assumes the .Specify directory is in there "home" directory.
                  specify.preStartUp();
                  specify.startUp();    
              }
          }
      });

  }
}

