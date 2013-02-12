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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;
import com.install4j.api.update.UpdateDescriptorEntry;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.GenericGUIDGeneratorFactory;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsEditor;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.ui.ProcessListUtil;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.exceptions.ExceptionTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.init.RegisterSpecify;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.JTiledToolbar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.TeeOutputStream;
/**
 * A base class for Specify derived Applications
 *
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 2, 2009
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class AppBase extends JPanel implements DatabaseLoginListener, CommandListener, FrameworkAppIFace
{
    private static final Logger  log                = Logger.getLogger(AppBase.class);
    
    private static AppBase       appInstance         = null; // needed for ActionListeners etc.
    
    public static final String ERROR_LOG    = "error.log";
    public static final String SPECIFY_LOG  = "specify.log";
    public static final String OUTPUT_LOG   = "output.log";
    public static final String ERRORSYS_LOG = "error_sys.log";

    // Status Bar
    protected JStatusBar         statusField        = null;
    protected JMenuBar           menuBar            = null;
    protected JFrame             topFrame           = null;
    protected JLabel             appIcon            = null;

    protected DatabaseLoginPanel dbLoginPanel        = null;
    protected String             databaseName        = null;
    protected String             userName            = null;
    
    protected GhostGlassPane     glassPane;

    protected String             appName             = "Specify"; //$NON-NLS-1$
    protected String             appVersion          = "6.0"; //$NON-NLS-1$

    protected String             appBuildVersion     = "(Unknown)"; //$NON-NLS-1$
    
    /**
     * Constructor.
     */
    public AppBase()
    {
        
    }
    
    /**
     * The very very first step in initializing Specify. 
     */
    protected void preStartUp()
    {
        //UIHelper.attachUnhandledException();
        
        // we simply need to create this class, not use it
        new MacOSAppHandler(this);

        // Name factories
        setUpSystemProperties();
    }
    
    /**
     * Check for and kills and existing embedded MySQl processes.
     */
    public static void checkForSpecifyAppsRunning()
    {
        List<Integer> ids = ProcessListUtil.getProcessIdWithText("exe4j.moduleName", 
                                                                 "specify", 
                                                                 "SpiReport");
        if (ids.size() > 1)
        {
            UIRegistry.showLocalizedMsg("WARNING", "Specify.TOO_MANY_SP");
            System.exit(0);
        }
    }
    
    /**
     * Setup all the System properties. This names all the needed factories. 
     */
    public static void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(ViewFactory.factoryName,                     "edu.ku.brc.specify.config.SpecifyViewFactory");        // Needed by ViewFactory //$NON-NLS-1$
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
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(RecordSetFactory.factoryName,                "edu.ku.brc.specify.config.SpecifyRecordSetFactory");          // Needed for Searching //$NON-NLS-1$
        System.setProperty(DBTableIdMgr.factoryName,                    "edu.ku.brc.specify.config.SpecifyDBTableIdMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        //System.setProperty(UserAndMasterPasswordMgr.factoryName,               "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(BackupServiceFactory.factoryName,            "edu.ku.brc.af.core.db.MySQLBackupService");                   // Needed for Backup and Restore //$NON-NLS-1$
        System.setProperty(ExceptionTracker.factoryName,                "edu.ku.brc.specify.config.SpecifyExceptionTracker");                   // Needed for Backup and Restore //$NON-NLS-1$
        
        System.setProperty(DBMSUserMgr.factoryName,                     "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
        System.setProperty(SchemaUpdateService.factoryName,             "edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService");   // needed for updating the schema
        System.setProperty(GenericGUIDGeneratorFactory.factoryName,     "edu.ku.brc.specify.config.SpecifyGUIDGeneratorFactory");
    }
    
    /**
     * @param imgEncoded uuencoded image string
     */
    protected void setAppIcon(final String imgEncoded)
    {
        String appIconName      = "AppIcon";
        String innerAppIconName = "InnerAppIcon";
        
        ImageIcon appImgIcon = null;
        if (StringUtils.isNotEmpty(imgEncoded))
        {
            appImgIcon = GraphicsUtils.uudecodeImage("", imgEncoded); //$NON-NLS-1$
            if (appImgIcon != null && appImgIcon.getIconWidth() == 32 && appImgIcon.getIconHeight() == 32)
            {
                appIcon.setIcon(appImgIcon);
                CustomDialog.setAppIcon(appImgIcon);
                CustomFrame.setAppIcon(appImgIcon);
                IconManager.register(innerAppIconName, appImgIcon, null, IconManager.IconSize.Std32);
                return;
            }
        }
        appImgIcon = IconManager.getImage(appIconName, IconManager.IconSize.Std32); //$NON-NLS-1$
        appIcon.setIcon(appImgIcon);
        if (!UIHelper.isMacOS())
        {
        	appImgIcon = IconManager.getImage("SpecifyWhite32", IconManager.IconSize.Std32); //$NON-NLS-1$
        }
        CustomDialog.setAppIcon(appImgIcon);
        CustomFrame.setAppIcon(appImgIcon);
        IconManager.register(innerAppIconName, appImgIcon, null, IconManager.IconSize.Std32);
        
        if (this.topFrame != null)
        {
            this.topFrame.setIconImage(appImgIcon.getImage());
        }
    }

    /**
     *
     * @return the toolbar for the app
     */
    protected JToolBar createToolBar()
    {
        
        JToolBar toolBar;
        SkinItem skinItem = SkinsMgr.getSkinItem("ToolBar");
        if (SkinsMgr.hasSkins() && skinItem != null && (skinItem.getBGImage() != null || skinItem.getBgColor() != null))
        {
            JTiledToolbar ttb = new JTiledToolbar(skinItem.getBGImage());
            if (skinItem.getBgColor() != null)
            {
                ttb.setBackground(skinItem.getBgColor());
                ttb.setOpaque(true);
            } else
            {
                ttb.setOpaque(false);  
            }
            toolBar = ttb;
        } else
        {
            toolBar = new JToolBar();
        }
        toolBar.setLayout(new ToolbarLayoutManager(2, 2));
        
        appIcon = new JLabel("  "); //$NON-NLS-1$
        
        setAppIcon(AppPreferences.getRemote().get("ui.formatting.user_icon_image", null)); //$NON-NLS-1$
        
        CommandDispatcher.register("Preferences", new CommandListener() { //$NON-NLS-1$
            public void doCommand(CommandAction cmdAction)
            {
                if (cmdAction.isAction("Updated")) //$NON-NLS-1$
                {
                    setAppIcon(AppPreferences.getRemote().get("ui.formatting.user_icon_image", null)); //$NON-NLS-1$
                }
            }
        });

        return toolBar;
    }

    /**
     * Show preferences.
     */
    public void doPreferences()
    {
        //AppContextMgr acm        = AppContextMgr.getInstance();
        if (AppContextMgr.getInstance().hasContext())
        {
            PreferencesDlg dlg = new PreferencesDlg(false);
            dlg.setVisible(true);
        } else
        {
            UIRegistry.showLocalizedMsg("Specify.NOTAVAIL");
        }
    }
    
    /**
     * 
     */
    protected void checkForUpdates()
    {
        try
        {
            UpdateDescriptor updateDesc = UpdateChecker.getUpdateDescriptor(UIRegistry.getResourceString("UPDATE_PATH"),
                                                                           ApplicationDisplayMode.UNATTENDED);

            UpdateDescriptorEntry entry = updateDesc.getPossibleUpdateEntry();

            if (entry != null)
            {
                Object[] options = { getResourceString("Specify.INSTALLUPDATE"),  //$NON-NLS-1$
                        getResourceString("Specify.SKIP")  //$NON-NLS-1$
                      };
                int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                             getLocalizedMessage("Specify.UPDATE_AVAIL", entry.getNewVersion()),  //$NON-NLS-1$
                                                             getResourceString("Specify.UPDATE_AVAIL_TITLE"),  //$NON-NLS-1$
                                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (userChoice == JOptionPane.YES_OPTION)
                {
                    if (!doExit(false))
                    {
                        return;
                    }
                    
                } else
                {
                    return;
                }
            } else
            {
                UIRegistry.showLocalizedError("Specify.NO_UPDATE_AVAIL");
                return ;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            UIRegistry.showLocalizedError("Specify.UPDATE_CHK_ERROR");
            return;
        }
        
        try
        {
            ApplicationLauncher.Callback callback = new ApplicationLauncher.Callback()
           {
               public void exited(int exitValue)
               {
                   System.err.println("exitValue: "+exitValue);
                   //startApp(doConfig);
               }
               public void prepareShutdown()
               {
                   System.err.println("prepareShutdown");
                   
               }
            };
            ApplicationLauncher.launchApplication("100", null, true, callback);
            
        } catch (Exception ex)
        {
            System.err.println("EXPCEPTION");
        }
    }
    
    
    /**
     * @param path
     * @param fileName
     * @param doCreate
     * @return
     */
    private static File getFile(final String path, final String fileName, final boolean doCreate)
    {
        String fullPath = path + File.separator + fileName;
        File   file     = new File(fullPath);
        if (file.exists())
        {
            return file;
        }
        
        if (doCreate)
        {
            File dir = new File(path);
            if (dir.exists() || dir.mkdirs())
            {
                return file;
            }
        }
        return null;
    }
    
    /**
     * @param fileName
     * @return
     */
    private static File getFullLogFilePath(final String fileName)
    {
        String homePath = System.getProperty("user.home");
        String userHome = homePath + File.separator + "Specify";
        
        File   logFile  = getFile(userHome, fileName, true);
        if (logFile != null) return logFile;
        
        return null;
    }
    
    /**
     * Creates a ScrollPane with the text from the log file.
     * @param logFile the file
     * @param doError indicates it should display the error log
     * @return the ScrollPane.
     */
    protected static JScrollPane getLogFilePanel(final File logFile, 
                                                 final boolean doError)
    {
        JTextArea textArea = new JTextArea();
        if (logFile.exists())
        {
            try
            {
                textArea.setText(FileUtils.readFileToString(logFile));
                
            } catch (Exception ex) {} // no catch on purpose
            
        } else
        {
            textArea.setText(doError ? getResourceString("Specify.LOG_NO_ERRORS") : getResourceString("Specify.LOG_EMPTY")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        textArea.setEditable(false);
            
        return new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    /**
     * Creates a modal dialog displaying the the error and specify log files. 
     */
    public static void displaySpecifyLogFiles()
    {
        File spLogFile  = getFullLogFilePath(AppBase.SPECIFY_LOG); //$NON-NLS-1$
        File errLogFile = getFullLogFilePath(AppBase.ERRORSYS_LOG); //$NON-NLS-1$
        
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add(getResourceString("Specify.ERROR"), getLogFilePanel(errLogFile, true)); //$NON-NLS-1$
        tabPane.add("Specify",                          getLogFilePanel(spLogFile, true)); //$NON-NLS-1$
        
        String title = getResourceString("Specify.LOG_FILES_TITLE");//$NON-NLS-1$
        CustomDialog dialog = new CustomDialog((JFrame)UIRegistry.getTopWindow(), title, true, CustomDialog.OK_BTN, tabPane); 
        String okLabel = getResourceString("Specify.CLOSE");//$NON-NLS-1$
        dialog.setOkLabel(okLabel); 
        dialog.createUI();
        dialog.setSize(800, 600);
        UIHelper.centerWindow(dialog);
        dialog.setVisible(true);
    }

    /**
     * Sets up StdErr and StdOut to be 'tee'd' to files. 
     */
    public static void setupTeeForStdErrStdOut(final boolean doStdErr, 
                                               final boolean doStdOut)
    {
        try 
        {
            if (doStdOut)
            {
                // Tee standard output
                File file = getFullLogFilePath(OUTPUT_LOG);
                if (file != null)
                {
                    PrintStream out = new PrintStream(new FileOutputStream(file));
                    PrintStream tee = new TeeOutputStream(System.out, out);
                    System.setOut(tee);
                }
            }
            
            if (doStdErr)
            {
                // Tee standard error
                File file = getFullLogFilePath(ERRORSYS_LOG);
                if (file != null)
                {
                    PrintStream err = new PrintStream(new FileOutputStream(file));
                    PrintStream tee = new TeeOutputStream(System.err, err);
                    System.setErr(tee);
                }
            }
            
        } catch (FileNotFoundException e) {}
    }
    
    
    /**
     * 
     */
    private void openPrefsEditor(final AppPreferences prefs, final String titleKey)
    {
        String             titleStr = UIRegistry.getResourceString("Specify."+titleKey); //$NON-NLS-1$
        final CustomDialog dialog   = new CustomDialog(topFrame, titleStr, true, CustomDialog.OK_BTN, new AppPrefsEditor(prefs));
        String             okLabel  = UIRegistry.getResourceString("Specify.CLOSE"); //$NON-NLS-1$
        dialog.setOkLabel(okLabel);
        dialog.pack();
        UIHelper.centerAndShow(dialog);
        if (!dialog.isCancelled())
        {
            try
            {
                prefs.flush();
            } catch (BackingStoreException ex) { }
            
            CommandDispatcher.dispatch(new CommandAction("Preferences", "Changed", prefs));
        }
    }

    /**
     * 
     */
    private void openLocalPrefs()
    {
        openPrefsEditor(AppPreferences.getLocalPrefs(), "LOCAL_PREFS");
    }

    /**
     * 
     */
    private void openRemotePrefs()
    {
        openPrefsEditor(AppPreferences.getRemote(), "REMOTE_PREFS");
    }

    /**
     * 
     */
    private void openGlobalPrefs()
    {
        openPrefsEditor(AppPreferences.getGlobalPrefs(), "GLOBAL_PREFS");
    }
    
    /**
     * Shows the About dialog.
     */
    public void doAbout()
    {
        AppContextMgr acm        = AppContextMgr.getInstance();
        boolean       hasContext = acm.hasContext();
        
        int baseNumRows = 9;
        String serverName = AppPreferences.getLocalPrefs().get("login.servers_selected", null);
        if (serverName != null)
        {
            baseNumRows++;
        }
        
        CellConstraints cc     = new CellConstraints();
        PanelBuilder    infoPB = new PanelBuilder(new FormLayout("p,6px,f:p:g", "p,4px,p,4px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", baseNumRows)));
        
        JLabel       iconLabel = new JLabel(IconManager.getIcon("SpecifyLargeIcon"), SwingConstants.CENTER); //$NON-NLS-1$
        PanelBuilder iconPB    = new PanelBuilder(new FormLayout("p", "20px,t:p,f:p:g"));
        iconPB.add(iconLabel, cc.xy(1, 2));
        
        if (hasContext)
        {
            DBTableIdMgr tableMgr = DBTableIdMgr.getInstance();
            boolean      hasReged = !RegisterSpecify.isAnonymous() && RegisterSpecify.hasInstitutionRegistered();
            
            int y = 1;
            infoPB.addSeparator(getResourceString("Specify.SYS_INFO"), cc.xyw(1, y, 3)); y += 2;
            
            JLabel lbl = UIHelper.createLabel(databaseName);
            infoPB.add(UIHelper.createI18NFormLabel("Specify.DB"), cc.xy(1, y));
            infoPB.add(lbl,   cc.xy(3, y)); y += 2;
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        openLocalPrefs();
                    }
                }
            });
            
            infoPB.add(UIHelper.createFormLabel(tableMgr.getTitleForId(Institution.getClassTableId())),  cc.xy(1, y));
            infoPB.add(lbl = UIHelper.createLabel(acm.getClassObject(Institution.class).getName()), cc.xy(3, y)); y += 2;
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        openRemotePrefs();
                    }
                }
            });
            infoPB.add(UIHelper.createFormLabel(tableMgr.getTitleForId(Division.getClassTableId())), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(acm.getClassObject(Division.class).getName()),      cc.xy(3, y)); y += 2;
            
            infoPB.add(UIHelper.createFormLabel(tableMgr.getTitleForId(Discipline.getClassTableId())), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(acm.getClassObject(Discipline.class).getName()),      cc.xy(3, y)); y += 2;
            
            infoPB.add(UIHelper.createFormLabel(tableMgr.getTitleForId(Collection.getClassTableId())), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(acm.getClassObject(Collection.class).getCollectionName()),cc.xy(3, y)); y += 2;
            
            infoPB.add(UIHelper.createI18NFormLabel("Specify.BLD"), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(appBuildVersion),cc.xy(3, y)); y += 2;
            
            infoPB.add(UIHelper.createI18NFormLabel("Specify.REG"), cc.xy(1, y));
            infoPB.add(UIHelper.createI18NLabel(hasReged ? "Specify.HASREG" : "Specify.NOTREG"),cc.xy(3, y)); y += 2;
            
            String isaNumber = RegisterSpecify.getISANumber();
            infoPB.add(UIHelper.createI18NFormLabel("Specify.ISANUM"), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(StringUtils.isNotEmpty(isaNumber) ? isaNumber : ""),cc.xy(3, y)); y += 2;
            
            if (serverName != null)
            {
                infoPB.add(UIHelper.createI18NFormLabel("Specify.SERVER"), cc.xy(1, y));
                infoPB.add(UIHelper.createLabel(StringUtils.isNotEmpty(serverName) ? serverName : ""),cc.xy(3, y)); y += 2;
            }
            
            if (StringUtils.contains(DBConnection.getInstance().getConnectionStr(), "mysql"))
            {
                Vector<Object[]> list = BasicSQLUtils.query("select version() as ve");
                if (list != null && list.size() > 0)
                {
                    infoPB.add(UIHelper.createFormLabel("MySQL Version"), cc.xy(1, y));
                    infoPB.add(UIHelper.createLabel(list.get(0)[0].toString()),cc.xy(3, y)); y += 2;
                }
            }
    
            
            infoPB.add(UIHelper.createFormLabel("Java Version"), cc.xy(1, y));
            infoPB.add(UIHelper.createLabel(System.getProperty("java.version")),cc.xy(3, y)); y += 2;
        }
        
        String txt = getAboutText(appName, appVersion);
        JLabel txtLbl = createLabel(txt);
        txtLbl.setFont(UIRegistry.getDefaultFont());
        
        final JEditorPane txtPane = new JEditorPane("text/html", txt);
        txtPane.setEditable(false);
        txtPane.setBackground(new JPanel().getBackground());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,20px,f:min(400px;p):g,10px,8px,10px,p:g", "f:p:g"));

        pb.add(iconPB.getPanel(), cc.xy(1, 1));
        pb.add(txtPane,           cc.xy(3, 1));
        Color bg = getBackground();
        
        if (hasContext)
        {
            pb.add(new VerticalSeparator(bg.darker(), bg.brighter()), cc.xy(5, 1));
            pb.add(infoPB.getPanel(), cc.xy(7, 1));
        }
        
        pb.setDefaultDialogBorder();
        
        String       title    = getResourceString("Specify.ABOUT");//$NON-NLS-1$
        CustomDialog aboutDlg = new CustomDialog(topFrame,  title + " " +appName, true, CustomDialog.OK_BTN, pb.getPanel()); //$NON-NLS-1$ 
        String       okLabel  = getResourceString("Specify.CLOSE");//$NON-NLS-1$
        aboutDlg.setOkLabel(okLabel); 
        
        aboutDlg.createUI();
        aboutDlg.pack();
        
        // for some strange reason I can't get the dialog to size itself correctly
        Dimension size = aboutDlg.getSize();
        size.height += 120;
        aboutDlg.setSize(size);
        
        txtPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event)
            {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    try
                    {
                        AttachmentUtils.openURI(event.getURL().toURI());
                        
                    }
                    catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    }
                }
            }
        });
        
        UIHelper.centerAndShow(aboutDlg);
    }
    
    /**
     * Returns a standard String for the about box
     * @param appNameArg the application name  
     * @param appVersionArg the application version
     * @return the about string
     */
    public static String getAboutText(final String appNameArg, final String appVersionArg)
    {
        return "<html><font face=\"sans-serif\" size=\"11pt\">"+appNameArg+" " + appVersionArg +  //$NON-NLS-1$ //$NON-NLS-2$
        "<br><br>Specify Software Project<br>" +//$NON-NLS-1$
        "Biodiversity Institute<br>University of Kansas<br>1345 Jayhawk Blvd.<br>Lawrence, KS  USA 66045<br><br>" +  //$NON-NLS-1$
        "<a href=\"http://www.specifysoftware .org\">www.specifysoftware.org</a>"+ //$NON-NLS-1$
        "<br><a href=\"mailto:specify@ku.edu\">specify@ku.edu</a><br>" +  //$NON-NLS-1$
        "<p>The Specify Software Project is "+ //$NON-NLS-1$
        "funded by the Advances in Biological Informatics Program, " + //$NON-NLS-1$
        "U.S. National Science Foundation  (Award DBI-0446544 and earlier awards).<br><br>" + //$NON-NLS-1$
        "Specify 6.0 Copyright \u00A9 2009 University of Kansas Center for Research. " + 
        "Specify comes with ABSOLUTELY NO WARRANTY.<br><br>" + //$NON-NLS-1$
        "This is free software licensed under GNU General Public License 2 (GPL2).</P></font></html>"; //$NON-NLS-1$

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    @Override
    public boolean doExit(boolean doAppExit)
    {
        return true;
    }

    /**
     * @return the statusField
     */
    public JStatusBar getStatusField()
    {
        return statusField;
    }
    
    protected String getTitle()
    {
        String resAppVersion = UIRegistry.getAppVersion();;
        if (StringUtils.isNotEmpty(resAppVersion))
        {
            appVersion = resAppVersion;
        }
        
        return getTitle(appVersion, appBuildVersion, appName);
    }

    /**
     * If the version number is '(Unknown)' then it wasn't installed with install4j.
     * @return the title for Specify which may include the version number.
     */
    public static String getTitle(final String appVersionStr, 
                                  final String appBuildVersionStr, 
                                  final String appNameStr)
    {
        String title         = "";
        String resAppVersion = UIRegistry.getAppVersion();
        if (StringUtils.isNotEmpty(resAppVersion))
        {
            title = appNameStr + " " + appVersionStr; //$NON-NLS-1$
        } else
        {
            title = appNameStr + " " + appVersionStr + "  - " + appBuildVersionStr; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (UIRegistry.isEmbedded())
        {
            title += " (EZDB)";
            
        } else if (UIRegistry.isMobile())
        {
            title += " (Mobile)";
        }
        return title;
    }

    /**
     * Bring up the PPApp demo by showing the frame (only applicable if coming up
     * as an application, not an applet);
     */
    public void showApp()
    {
        JFrame f = getFrame();
        f.setTitle(getTitle());
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        f.addWindowListener(new WindowAdapter()
        		{
        			@Override
                    public void windowClosing(WindowEvent e)
        			{
        				doExit(true);
        			}
        		});
        
        UIHelper.centerWindow(f);
        Rectangle r = f.getBounds();
        int x = AppPreferences.getLocalPrefs().getInt("APP.X", r.x);
        int y = AppPreferences.getLocalPrefs().getInt("APP.Y", r.y);
        int w = AppPreferences.getLocalPrefs().getInt("APP.W", r.width);
        int h = AppPreferences.getLocalPrefs().getInt("APP.H", r.height);
        UIHelper.positionAndFitToScreen(f, x, y, w, h);
        
        f.setVisible(true);
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
    public void setStatus(final String str)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                getAppInstance().getStatusField().setText(str);
            }
        });
    }
    
    /**
     * 
     */
    protected static void setupDefaultFonts()
    {
        Font labelFont = (createLabel("")).getFont(); //$NON-NLS-1$
        Font defaultFont;
        if (!UIHelper.isMacOS())
        {
            defaultFont = labelFont;
        } else
        {
            //if (labelFont.getSize() == 13)
            //{
            //    defaultFont = labelFont.deriveFont((float)labelFont.getSize()-2);
            //} else
            {
                defaultFont = labelFont;
            }
        }
        BaseTask.setToolbarBtnFont(defaultFont); // For ToolbarButtons
        RolloverCommand.setDefaultFont(defaultFont);
    }
    
    /**
     * Restarts the app with a new or old database and user name and creates the core app UI.
     * @param window the login window
     * @param databaseNameArg the database name
     * @param userNameArg the user name
     * @param startOver tells the AppContext to start over
     * @param firstTime indicates this is the first time in the app and it should create all the UI for the core app
     */
    public void restartApp(final Window  window, 
                           final String  databaseNameArg, 
                           final String  userNameArg, 
                           final boolean startOver, 
                           final boolean firstTime)
    {
        
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
        
        // This is used to fill who editted the object
        FormHelper.setCurrentUserEditStr(userNameArg);
        
        AppPreferences.setConnectedToDB(true);
        
        restartApp(window, databaseName, userName, false, firstTime);
        
        statusField.setSectionText(2, userName);
        statusField.setSectionToolTipText(2, DBTableIdMgr.getInstance().getTitleForId(SpecifyUser.getClassTableId()));
        
    }
    
    /**
     * Sets the Database Name and the Collection Name into the Status Bar. 
     */
    protected void setDatabaseNameAndCollection()
    {
        AppContextMgr mgr = AppContextMgr.getInstance();
        String disciplineName = mgr.getClassObject(Discipline.class).getName();
        String collectionName = mgr.getClassObject(Collection.class) != null ? mgr.getClassObject(Collection.class).getCollectionName() : ""; //$NON-NLS-1$ //$NON-NLS-2$
        statusField.setSectionText(0, disciplineName);
        statusField.setSectionText(1, collectionName);
        
        statusField.setSectionToolTipText(0, DBTableIdMgr.getInstance().getTitleForId(Discipline.getClassTableId()));
        statusField.setSectionToolTipText(1, DBTableIdMgr.getInstance().getTitleForId(Collection.getClassTableId()));
        
        AppPreferences.getLocalPrefs().put("CURRENT_DB", databaseName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }
    
    /**
     * Reads Local Preferences for the Locale setting.
     */
    public static void adjustLocaleFromPrefs()
    {
        String language = AppPreferences.getLocalPrefs().get("locale.lang", null); //$NON-NLS-1$
        if (language != null)
        {
            String country  = AppPreferences.getLocalPrefs().get("locale.country", null); //$NON-NLS-1$
            String variant  = AppPreferences.getLocalPrefs().get("locale.var",     null); //$NON-NLS-1$
            
            Locale prefLocale = new Locale(language, country, variant);
            
            Locale.setDefault(prefLocale);
            UIRegistry.setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
    }

    /**
     * @return the appVersion
     */
    public String getAppVersion()
    {
        return appVersion;
    }

    /**
     * @return the appBuildVersion
     */
    public String getAppBuildVersion()
    {
        return appBuildVersion;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(BaseTask.APP_CMD_TYPE))
        {
            if (cmdAction.isAction(BaseTask.APP_REQ_RESTART))
            {
                UIRegistry.writeGlassPaneMsg(getResourceString("Specify.RESET_ENV"), 24);
                
                SwingWorker workerThread = new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        restartApp(null, databaseName, userName, true, false);
                        return null;
                    }
                    
                    @Override
                    public void finished()
                    {
                        UIRegistry.clearGlassPaneMsg();
                    }
                };
                
                // start the background task
                workerThread.start();
                
            } else if (cmdAction.isAction(BaseTask.APP_REQ_EXIT))
            {
                doExit(true);
                
            } else if (cmdAction.isAction("CheckForUpdates"))
            {
                checkForUpdates();
            }
        }
    }
    
    /**
     * @param args
     */
    public static void processArgs(final String[] args)
    {
        //log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                //System.err.println("["+symbol+"]");
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
            
        String mobile = System.getProperty("mobile");
        if (StringUtils.isNotEmpty(mobile))
        {
            UIRegistry.setMobile(true);
            DBConnection.setIsEmbeddedDB(true);
        }
        
        String embeddedStr = System.getProperty("embedded");
        if (StringUtils.isNotEmpty(embeddedStr))
        {
            UIRegistry.setEmbedded(true);
            DBConnection.setIsEmbeddedDB(true);
        }
        
        String embeddeddbdir = System.getProperty("embeddeddbdir");
        if (StringUtils.isNotEmpty(embeddeddbdir))
        {
            UIRegistry.setEmbeddedDBPath(embeddeddbdir);
        } else
        {
            UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        }
    }


    // *******************************************************
    // *****************   Static Methods  *******************
    // *******************************************************

    /**
     *
     * @return the specify app object
     */
    public static AppBase getAppInstance()
    {
        return appInstance;
    }
}

