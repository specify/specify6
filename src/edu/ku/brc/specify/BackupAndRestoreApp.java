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
import edu.ku.brc.af.ui.ProcessListUtil;
import edu.ku.brc.af.ui.ProcessListUtil.PROC_STATUS;
import edu.ku.brc.af.ui.ProcessListUtil.ProcessListener;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel.MasterPasswordProviderIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.prefs.MySQLPrefs;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    
    private String               appName             = "Backup and Restore"; //$NON-NLS-1$
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
        topFrame.setIconImage(IconManager.getImage("Backup", IconManager.IconSize.Std32).getImage()); //$NON-NLS-1$
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
    
    public static String getIconName()
    {
        String postFix = "";
        if (UIRegistry.isEmbedded())
        {
            postFix = "E";
        } else if (UIRegistry.isMobile())
        {
            postFix = "M";
        }
        return "DatabaseIcon" + postFix;
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
        appImgIcon = IconManager.getImage(getIconName(), IconManager.IconSize.Std32); //$NON-NLS-1$
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

        HelpMgr.setAppDefHelpId("Backup_Restore");
        
        JMenu helpMenu = UIHelper.createLocalizedMenu(mb, "Specify.HELP_MENU", "Specify.HELP_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
        HelpMgr.createHelpMenuItem(helpMenu, "Backup and Restore"); //$NON-NLS-1$
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
        size.height += 180;
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
                if (mainPanel != null)
                {
                    if (mainPanel.isOKToSave())
                    {
                        mainPanel.savePrefs();
                        AppPreferences.getLocalPrefs().flush();
                        AppPreferences.shutdownLocalPrefs();
                    }
                }                

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
        String resAppVersion = UIRegistry.getAppVersion();
        
        String postFix = "";
        if (UIRegistry.isEmbedded())
        {
            postFix = " (EZDB)";
            
        } else if (UIRegistry.isMobile())
        {
            postFix = " (Mobile)";
        }
        
        if (StringUtils.isNotEmpty(resAppVersion))
        {
            appVersion = resAppVersion;
            title = appName + postFix + " " + appVersion; //$NON-NLS-1$
        } else
        {
            title = appName + postFix + " " + appVersion + "  - " + appBuildVersion; //$NON-NLS-1$ //$NON-NLS-2$
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
        
        //HelpMgr.setHelpID(topFrame, mainPanel.getHelpContext());
        
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
                        startupContinuing(); // On UI Thread
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
    private void startupContinuing() // needs to be called on the UI Thread
    {
        
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
        
        UIRegistry.loadAndPushResourceBundle("backuprestore");
        dbLoginPanel = UIHelper.doLogin(null, false, false, false, this, getIconName(), getTitle(), null, Specify.getOpaqueIconName(), "Backup_Restore"); // true means do auto login if it can, second bool means use dialog instead of frame
        dbLoginPanel.setShouldCheckForSchemaUpdate(false);
        dbLoginPanel.doAutoExpand();
        UIRegistry.popResourceBundle();
        
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
        
        statusField.setText(DBConnection.getInstance().getDatabaseName());
        
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
    
    /**
     * Tries to do the login, if doAutoLogin is set to true it will try without displaying a dialog
     * and if the login fails then it will display the dialog
     * @param userName single signon username (for application)
     * @param password single signon password (for application)
     * @param usrPwdProvider the provider
     * @param engageUPPrefs indicates whether the username and password should be loaded and remembered by local prefs
     * @param doAutoLogin whether to try to automatically log the user in
     * @param doAutoClose whether it should automatically close the window when it is logged in successfully
     * @param useDialog use a Dialog or a Frame
     * @param listener a listener for when it is logged in or fails
     * @param iconName name of icon to use
     * @param title name
     * @param appName name
     * @param appIconName application icon name
     * @param helpContext help context for Help button on dialog
     */
    public static DatabaseLoginPanel doLogin(final boolean engageUPPrefs,
                                             final MasterPasswordProviderIFace usrPwdProvider,
                                             final boolean doAutoClose,
                                             final DatabaseLoginListener listener,
                                             final String  iconName,
                                             final String  title,
                                             final String  appName,
                                             final String  appIconName,
                                             final String  helpContext) //frame's icon name
    {  
        
        ImageIcon icon = IconManager.getIcon("AppIcon", IconManager.IconSize.Std32);
        if (StringUtils.isNotEmpty(appIconName))
        {
            ImageIcon imgIcon = IconManager.getIcon(appIconName);
            if (imgIcon != null)
            {
                icon = imgIcon;
            }
        }

        // else
        class DBListener implements DatabaseLoginListener
        {
            protected JFrame                frame;
            protected DatabaseLoginListener frameDBListener;
            protected boolean               doAutoCloseOfListener;

            public DBListener(JFrame frame, DatabaseLoginListener frameDBListener, boolean doAutoCloseOfListener)
            {
                this.frame                 = frame;
                this.frameDBListener       = frameDBListener;
                this.doAutoCloseOfListener = doAutoCloseOfListener;
            }
            
            public void loggedIn(final Window window, final String databaseName, final String userNameArg)
            {
                log.debug("UIHelper.doLogin[DBListener]");
                if (doAutoCloseOfListener)
                {
                    frame.setVisible(false);
                }
                frameDBListener.loggedIn(window, databaseName, userNameArg);
            }

            public void cancelled()
            {
                frame.setVisible(false);
                frameDBListener.cancelled();
            }
        }
        JFrame.setDefaultLookAndFeelDecorated(false);

        JFrame frame = new JFrame(title);
        DatabaseLoginPanel panel = new DatabaseLoginPanel(null, null, false, usrPwdProvider, new DBListener(frame, listener, doAutoClose), 
                                                          false, false, title, appName, iconName, helpContext);
        
        panel.setAutoClose(doAutoClose);
        panel.setWindow(frame);
        frame.setContentPane(panel);
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.pack();

        UIHelper.centerAndShow(frame);

        return panel;
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
       AppBase.processArgs(args);
       AppBase.setupTeeForStdErrStdOut(true, false);
       
       SwingUtilities.invokeLater(new Runnable() {
           @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void run()
         {
              // Set App Name, MUST be done very first thing!
               // This must be 'Specify'
              UIRegistry.setAppName("Specify");  //$NON-NLS-1$
              
              // Load Local Prefs
              AppPreferences.getLocalPrefs().setDirPath(UIRegistry.getAppDataDir());
               
              startApp();
   
         }
       });

   }

}
