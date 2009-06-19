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
package edu.ku.brc.specify;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.prefs.MySQLPrefs;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileCache;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 6, 2008
 *
 */
public class BackupAndRestoreApp extends JPanel implements DatabaseLoginListener, CommandListener, FrameworkAppIFace
{
    private static final Logger  log                = Logger.getLogger(BackupAndRestoreApp.class);
            
    // Status Bar
    private JStatusBar          statusField        = null;
    private JMenuBar            menuBar            = null;
    private JFrame              topFrame           = null;
    private MySQLPrefs          mainPanel          = null;
    private JLabel              appIcon            = null;

    protected boolean           hasChanged         = false;

    protected String             currentDatabaseName = null;
    protected DatabaseLoginPanel dbLoginPanel        = null;
    protected String             databaseName        = null;
    protected String             userName            = null;
    protected GhostGlassPane     glassPane;

    private boolean              isWorkbenchOnly     = false;
    
    private String               appName             = "Specify"; //$NON-NLS-1$
    private String               appVersion          = "6.0"; //$NON-NLS-1$

    private String               appBuildVersion     = "(Unknown)"; //$NON-NLS-1$

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
        Specify.setUpSystemProperties();
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doPreferences()
     */
    @Override
    public void doPreferences()
    {
        
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
        //setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setPreferredSize(new Dimension(1024, 768)); // For demo
        
        topFrame = new JFrame(gc);
        topFrame.setIconImage(IconManager.getImage("AppIcon").getImage()); //$NON-NLS-1$
        //topFrame.setAlwaysOnTop(true);
        
        topFrame.setGlassPane(glassPane = GhostGlassPane.getInstance());
        topFrame.setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UIRegistry.register(UIRegistry.GLASSPANE, glassPane);

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        UIRegistry.setTopWindow(topFrame);

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

        AppPrefsCache.setUseLocalOnly(true);
        SpecifyAppPrefs.setSkipRemotePrefs(true);
        AppContextMgr.getInstance().setHasContext(true);
        SpecifyAppPrefs.initialPrefs();
        
        mainPanel = new MySQLPrefs(true);
        add(mainPanel, BorderLayout.CENTER);
        
        int[] sections = {5};
        statusField = new JStatusBar(sections);
        statusField.setErrorIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16)); //$NON-NLS-1$
        statusField.setWarningIcon(IconManager.getIcon("Warning", IconManager.IconSize.Std16)); //$NON-NLS-1$
        UIRegistry.setStatusBar(statusField);

        add(statusField, BorderLayout.SOUTH);
        
        topFrame.setContentPane(this);
    }
    
    /**
     * @param imgEncoded uuencoded image string
     */
    protected void setAppIcon(final String imgEncoded)
    {
        ImageIcon appImgIcon = null;
        if (StringUtils.isNotEmpty(imgEncoded))
        {
            appImgIcon = GraphicsUtils.uudecodeImage("", imgEncoded); //$NON-NLS-1$
            if (appImgIcon != null && appImgIcon.getIconWidth() == 32 && appImgIcon.getIconHeight() == 32)
            {
                appIcon.setIcon(appImgIcon);
                return;
            }
        }
        appImgIcon = IconManager.getImage("AppIcon", IconManager.IconSize.Std32); //$NON-NLS-1$
        appIcon.setIcon(appImgIcon);
    }

    /**
     *
     * @return the toolbar for the app
     */
    public JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new ToolbarLayoutManager(2, 2));
        
        appIcon = new JLabel("  "); //$NON-NLS-1$
        
        return toolBar;
    }

    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;

        //--------------------------------------------------------------------
        //-- File Menu
        //--------------------------------------------------------------------
        JMenu menu = UIHelper.createLocalizedMenu(mb, "Specify.FILE_MENU", "Specify.FILE_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$

        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
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
     * Shows the About dialog.
     */
    public void doAbout()
    {
        CellConstraints cc     = new CellConstraints();
        PanelBuilder    infoPB = new PanelBuilder(new FormLayout("p,6px,f:min(400;p):g", "p:g"));
        
        JLabel       iconLabel = new JLabel(IconManager.getIcon("SpecifyLargeIcon"), SwingConstants.CENTER); //$NON-NLS-1$
        PanelBuilder iconPB    = new PanelBuilder(new FormLayout("p", "20px,t:p,f:p:g"));
        iconPB.add(iconLabel, cc.xy(1, 2));

        infoPB.setDefaultDialogBorder();
        
        infoPB.add(iconPB.getPanel(), cc.xy(1, 1));
        infoPB.add(createLabel(Specify.getAboutText(appName, appVersion)), cc.xy(3, 1));
        
        String title = getResourceString("Specify.ABOUT");//$NON-NLS-1$
        CustomDialog aboutDlg = new CustomDialog(topFrame,  title + " " +appName, true, CustomDialog.OK_BTN, infoPB.getPanel()); //$NON-NLS-1$ 
        String okLabel = getResourceString("Specify.CLOSE");//$NON-NLS-1$
        aboutDlg.setOkLabel(okLabel); 
        aboutDlg.createUI();
        aboutDlg.pack();
        // for some strange reason I can't get the dialog to size itself correctly
        Dimension size = aboutDlg.getSize();
        size.height += 80;
        aboutDlg.setSize(size);
        UIHelper.centerAndShow(aboutDlg);
    }

    /**
     * Checks to see if cache has changed before exiting.
     *
     */
    public boolean doExit(boolean doAppExit)
    {
        boolean okToShutdown = true;
        try
        {
            if (AttachmentUtils.getAttachmentManager() != null)
            {
                AttachmentUtils.getAttachmentManager().cleanup();
            }
            
            okToShutdown = SubPaneMgr.getInstance().aboutToShutdown();
            if (okToShutdown)
            {
                /*try
                {
                    DataProviderSessionIFace session     = null;
                    SpecifyUser              currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                    if (currentUser != null)
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        
                        SpecifyUser user = session.getData(SpecifyUser.class, "id", currentUser.getId(), DataProviderSessionIFace.CompareType.Equals);
                        user.setIsLoggedIn(false);
                        user.setLoginDisciplineName(null);
                        user.setLoginCollectionName(null);
                        user.setLoginOutTime(new Timestamp(System.currentTimeMillis()));
                        
                        try
                        {
                            session.beginTransaction();
                            session.saveOrUpdate(user);
                            session.commit();
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupAndRestoreApp.class, ex);
                            log.error(ex);
                            
                        } finally
                        {
                            if (session != null)
                            {
                                session.close();
                            }
                        }
                    }
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupAndRestoreApp.class, ex);
                    
                }*/
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupAndRestoreApp.class, ex);
            
        } finally
        {
            if (okToShutdown && doAppExit)
            {
                System.exit(0);
            }
        }
        return okToShutdown;
    }
    
    /**
     * If the version number is '(Unknown)' then it wasn't installed with install4j.
     * @return the title for Specify which may include the version number.
     */
    protected String getTitle()
    {
        String title        = "";
        String install4JStr = UIHelper.getInstall4JInstallString();
        if (StringUtils.isNotEmpty(install4JStr))
        {
            appVersion = install4JStr;
            title = appName + " Alpha " + appVersion; //$NON-NLS-1$
        } else
        {
            title = appName + " " + appVersion + "  - " + appBuildVersion; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return title;
    }

    /**
     * Bring up the PPApp demo by showing the frame (only applicable if coming up
     * as an application, not an applet);
     */
    public void showApp()
    {
        
        topFrame.pack();
        
        topFrame.setTitle(getTitle());

        topFrame.addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(WindowEvent e)
                    {
                        doExit(true);
                    }
                });
        
        UIHelper.centerWindow(topFrame);
        
        /*Rectangle r = f.getBounds();
        int x = AppPreferences.getLocalPrefs().getInt("APP.X", r.x);
        int y = AppPreferences.getLocalPrefs().getInt("APP.Y", r.y);
        int w = AppPreferences.getLocalPrefs().getInt("APP.W", r.width);
        int h = AppPreferences.getLocalPrefs().getInt("APP.H", r.height);
        UIHelper.positionAndFitToScreen(f, x, y, w, h);
        */
        
        Rectangle r = topFrame.getBounds();
        r.setBounds(1, 1, 600, 275);
        topFrame.setBounds(r);
        UIHelper.centerWindow(topFrame);
        topFrame.setVisible(true);
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
        SwingUtilities.invokeLater(new Runnable()
        {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void run()
            {
                BackupAndRestoreApp.this.statusField.setText(s);
            }
        });
    }
    
    /**
     * 
     */
    protected void setupDefaultFonts()
    {
        //if (UIHelper.isMacOS())
        {
            Font labelFont = (createLabel("")).getFont(); //$NON-NLS-1$
            log.debug("****** "+labelFont); //$NON-NLS-1$
            Font defaultFont;
            if (!UIHelper.isMacOS())
            {
                defaultFont = labelFont;
            } else
            {
                if (labelFont.getSize() == 13)
                {
                    defaultFont = labelFont.deriveFont((float)labelFont.getSize()-2);
                } else
                {
                    defaultFont = labelFont;
                }
            }
            BaseTask.setToolbarBtnFont(defaultFont); // For ToolbarButtons
            RolloverCommand.setDefaultFont(defaultFont);
        }
    }
    /**
     * Start up without the initializer, assumes there is at least one database to connect to.
     */
    public void startUp()
    {
        log.debug("StartUp..."); //$NON-NLS-1$
        
        // Adjust Default Swing UI Default Resources (Color, Fonts, etc) per Platform
        UIHelper.adjustUIDefaults();
        
        setupDefaultFonts();
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        UsageTracker.incrUsageCount("RunCount"); //$NON-NLS-1$
        
        //UIHelper.attachUnhandledException();

        FileCache.setDefaultPath(UIRegistry.getAppDataDir() + File.separator + "cache"); //$NON-NLS-1$

        UIRegistry.register(UIRegistry.MAINPANE, this); // important to be done immediately
 
        // Setup base font AFTER setting Look and Feel
        UIRegistry.setBaseFont((createLabel("")).getFont()); //$NON-NLS-1$

        log.info("Creating Database configuration "); //$NON-NLS-1$

        if (!isWorkbenchOnly)
        {
            HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
            HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
            // SInce Update get called when deleting an object there is no need to register this class.
            // The update deletes because first it removes the Lucene document and then goes to add it back in, but since the
            // the record is deleted it doesn't get added.
            HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
            //HibernateUtil.setListener("delete", new edu.ku.brc.specify.dbsupport.DeleteEventListener());
        }
        
        Specify.adjustLocaleFromPrefs();
        
        CommandDispatcher.register(BaseTask.APP_CMD_TYPE, this);
        
        
        dbLoginPanel = UIHelper.doLogin(null, false, false, this, "DatabaseIcon", getTitle(), null, "SpecifyWhite32"); // true means do auto login if it can, second bool means use dialog instead of frame
        localPrefs.load();
    }
    /**
     * Restarts the app with a new or old database and user name and creates the core app UI.
     * @param window the login window
     * @param databaseNameArg the database name
     * @param userNameArg the user name
     * @param startOver tells the AppContext to start over
     * @param firstTime indicates this is the first time in the application and it should create all the UI for the core app
     */
    public void restartApp(final Window  window, 
                           final String  databaseNameArg, 
                           final String  userNameArg, 
                           final boolean startOver, 
                           final boolean firstTime)
    {
        log.debug("restartApp"); //$NON-NLS-1$
        if (dbLoginPanel != null)
        {
            dbLoginPanel.getStatusBar().setText(getResourceString("Specify.INITIALIZING_APP")); //$NON-NLS-1$
        }
        
        if (firstTime)
        {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            
            initialize(gc);

            topFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            UIRegistry.register(UIRegistry.FRAME, topFrame);
            
        }    
        
        if (window != null)
        {
            window.setVisible(false);
        }
        
        showApp();
        
        if (dbLoginPanel != null)
        {
            dbLoginPanel.getWindow().setVisible(false);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(BaseTask.APP_CMD_TYPE) && cmdAction.isAction(BaseTask.APP_REQ_EXIT))
        {
            doExit(true);
        }
    }

    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.awt.Window, java.lang.String, java.lang.String)
     */
    public void loggedIn(final Window window, final String databaseNameArg, final String userNameArg)
    {
        log.debug("loggedIn - database["+databaseNameArg+"] username["+ userNameArg +"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        boolean firstTime = this.databaseName == null;
        
        this.databaseName = databaseNameArg;
        this.userName     = userNameArg;
        
        // This is used to fill who edited the object
        FormHelper.setCurrentUserEditStr(userNameArg);
        
        AppPreferences.setConnectedToDB(true);
        
        restartApp(window, databaseName, userName, false, firstTime);
        
        doLayout();

        JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
        if (toolBar != null && toolBar.getComponentCount() < 2)
        {
            toolBar.setVisible(false);
        }
        statusField.setSectionText(0, userName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.DatabaseLoginListener#cancelled()
     */
    @Override
    public void cancelled()
    {
        doExit(true);
    }
    
    //-----------------------------------------------------------------------------
    //-- Application MAIN
    //-----------------------------------------------------------------------------

    /**
     * 
     */
    public static void startApp()
    {
        // Then set this
        IconManager.setApplicationClass(BackupAndRestoreApp.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        try
        {
            UIHelper.OSTYPE osType = UIHelper.getOSType();
            if (osType == UIHelper.OSTYPE.Windows )
            {
                UIManager.setLookAndFeel(new PlasticLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                
            } else if (osType == UIHelper.OSTYPE.Linux )
            {
                UIManager.setLookAndFeel(new PlasticLookAndFeel());
            }
        }
        catch (Exception e)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BackupAndRestoreApp.class, e);
            log.error("Can't change L&F: ", e); //$NON-NLS-1$
        }
        
        ImageIcon helpIcon = IconManager.getIcon("AppIcon",IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$
        
        // Startup Specify
        BackupAndRestoreApp backupAndRestoreApp = new BackupAndRestoreApp();
        
        RolloverCommand.setHoverImg(IconManager.getIcon("DropIndicator")); //$NON-NLS-1$
        
        // THis type of start up ALWAYS assumes the .Specify directory is in there "home" directory.
        backupAndRestoreApp.preStartUp();
        backupAndRestoreApp.startUp();    
    }

    
    class BRSecurityMgr extends SecurityMgr
    {
        /* (non-Javadoc)
         * @see edu.ku.brc.af.auth.SecurityMgr#getPermission(java.lang.String)
         */
        @Override
        public PermissionSettings getPermission(String nameStr)
        {
            return new PermissionSettings(PermissionSettings.ALL_PERM);
        }
        
    }

   /**
    *
    */
   public static void main(String[] args)
   {
       log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       // This is for Windows and Exe4J, turn the args into System Properties
       
       UIRegistry.setEmbeddedDBDir(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
       
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
       
       String embeddeddbdir = System.getProperty("embeddeddbdir");
       if (StringUtils.isNotEmpty(embeddeddbdir))
       {
           UIRegistry.setEmbeddedDBDir(embeddeddbdir);
       }
       
       String mobile = System.getProperty("mobile");
       if (StringUtils.isNotEmpty(mobile))
       {
           UIRegistry.setEmbeddedDBDir(UIRegistry.getMobileEmbeddedDBPath());
       }
       
       SwingUtilities.invokeLater(new Runnable() {
           @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void run()
         {
              // Set App Name, MUST be done very first thing!
              UIRegistry.setAppName("Specify");  //$NON-NLS-1$
               
              // Load Local Prefs
              AppPreferences localPrefs = AppPreferences.getLocalPrefs();
              localPrefs.setDirPath(UIRegistry.getAppDataDir());
               
              startApp();
   
         }
       });

   }

}
