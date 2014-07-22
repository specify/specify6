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
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createLocalizedMenuItem;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.TOOLBAR;
import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.get;
import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.tasks.BaseTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 1, 2012
 *
 */
public class iPadDBExporterPlugin extends BaseTask
{
    //private static final Logger log = Logger.getLogger(iPadDBExporterPlugin.class);
        
    protected static final String RES_NAME       = "edu/ku/brc/specify/plugins/ipadexporter/res";
    protected static final String CHART_RES_NAME = "edu/ku/brc/specify/plugins/ipadexporter/chart";
    
    // Static Data Members
    private static final String ON_TASKBAR = "iPadDBExporttask.OnTaskbar";
    private static final int maxRequiredRecs = 0;
    
    public static final String DBEXPORTER = "iPadDBExporter";
    public static final String EXPORT_DB  = "ExportRecordSet";

    // Data Members
    private iPadDBExporter          iPadDBExporter   = null;
    private JButton                 exportBtn;
    private JLabel                  panelTitle;
    
    private IPadCloudIFace          iPadCloud        = null;
    private NavBox                  actionNavBox     = null;
    private Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    private ToolBarDropDownBtn      toolBarBtn       = null;
    
    //private RolloverCommand         manageDataSetsBtn;
    private RolloverCommand         iPadInfoSetupBtn;
    private RolloverCommand         removeAccountBtn;
    private RolloverCommand         loginBtn;
    private RolloverCommand         logoutBtn;

    /**
     * Constructor.
     */
    public iPadDBExporterPlugin()
    {
        super(DBEXPORTER, getResourceString("iPadDBExporter"));
        
        //iPadCloudDBHelper = new IPadCloudDBImpl();
        iPadCloud = new IPadCloudJSONImpl();
        
        CommandDispatcher.register(DBEXPORTER, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
        
        title = "iPad Exporter";
        
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isSingletonPane()
     */
    public boolean isSingletonPane()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        //CommandDispatcher.register(IMAGES, this);

        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("Actions"));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            extendedNavBoxes.addAll(navBoxes);

            loadAndPushResourceBundle(RES_NAME);
            
            loginBtn          = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("LOGIN"),     "image", null, null);
            //manageDataSetsBtn = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("MGR_DS"),    "image", null, null);
            iPadInfoSetupBtn     = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("IPAD_SETUP"), "image", null, null);
            removeAccountBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("DEL_ACCT"),  "image", null, null);
            logoutBtn         = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("LOGOUT"),    "image", null, null);
            exportBtn         = createI18NButton("EXPORT_TO_IPAD");

            setUIEnabled(false);
            popResourceBundle();

//            manageDataSetsBtn.addActionListener(new ActionListener()
//            {
//                @Override
//                public void actionPerformed(ActionEvent e)
//                {
//                    manageDataSets();
//                }
//            });
            
            removeAccountBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    removeAccount();
                }
            });

            loginBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    login();
                }
            });

            logoutBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (iPadCloud.logout())
                    {
                        setUIEnabled(false);
                        panelTitle.setText("");
                    }
                }
            });
            iPadInfoSetupBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    checkInstitutionInfo(true);
                }
            });
            exportBtn.setEnabled(false);
        }
    }
    
    /**
     * @param enabled
     */
    private void setUIEnabled(final boolean enabled)
    {
        //manageDataSetsBtn.setEnabled(enabled);
        removeAccountBtn.setEnabled(enabled);
        iPadInfoSetupBtn.setEnabled(enabled);
        loginBtn.setEnabled(!enabled);
        logoutBtn.setEnabled(enabled);
        if (exportBtn != null) exportBtn.setEnabled(enabled);
    }
    
    /**
     * 
     */
    private void manageDataSets()
    {
        //((IPadCloudDBHelper)iPadCloudDBHelper).createTestData();
        
        ManageDataSetsDlg mdsDlg = new ManageDataSetsDlg(iPadCloud);
        centerAndShow(mdsDlg);
    }
    
    /**
     * 
     */
    private void removeAccount()
    {
        
    }
    
    /**
     * @return
     */
    private boolean checkInstitutionInfo(final boolean forceShow)
    {
        if (!iPadDBExporter.IS_TESTING) // ZZZ       
        {
            ImageSetupDlg dlg = new ImageSetupDlg(iPadCloud);
            if (dlg.initializeInstitutionData())
            {
                if (forceShow || !dlg.isInstOK())
                {
                    dlg.createUI();
                    dlg.pack();
                    centerAndShow(dlg, 800, null);
                    return !dlg.isCancelled();
                }
            } else
            {
                //loadAndPushResourceBundle(RES_NAME);
                UIRegistry.showLocalizedError("POSSIBLE_NETWORK_ERROR");
                //popResourceBundle();
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param loginInfo
     */
    private void loggedIn(Pair<String, String> loginInfo)
    {
        setUIEnabled(true);
        panelTitle.setText("Logged In: "+loginInfo.first);
    }
    
    /**
     * 
     */
    private void login()
    {
        Division        div         = AppContextMgr.getInstance().getClassObject(Division.class);
        SpecifyUser     spUser      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        String          prefName    = String.format("IPAD_USERNAME_%d_%d", div.getId(), spUser.getId());
        String          userName    = remotePrefs.get(prefName, "");
        boolean         wasInError  = false;
        while (true)
        {
            Pair<String, String> loginInfo = getExportLoginCreds(userName, wasInError);
            if (loginInfo != null)
            {
                if (!iPadCloud.isNetworkError())
                {
                    loggedIn(loginInfo);
                }
                if (!iPadDBExporter.IS_TESTING) // ZZZ  
                {
                    userName = loginInfo.first;
                    if (iPadCloud.login(loginInfo.first, loginInfo.second))
                    {
                        remotePrefs.put(prefName, loginInfo.first);
                        loggedIn(loginInfo);
                        break;
                    } else
                    {
                        wasInError = true;
                    }
                } else
                {
                    break; // testing
                }
            } else
            {
                break;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        if (starterPane == null)
        {
            super.requestContext();
            
        } else
        {
            SubPaneMgr.getInstance().showPane(starterPane);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = null;
        }
    }
    
    /**
     * @param lbl
     * @param text
     */
    public static void setErrorMsg(final JLabel lbl, final String text)
    {
        lbl.setText(String.format("<html><font color='red'>%s</font>", text));
    }
 
    /**
     * @return
     */
    private Pair<String, String> getExportLoginCreds(final String userName, final boolean wasInError)
    {
        loadAndPushResourceBundle(RES_NAME);
        try
        {
            final JTextField     userNameTF = createTextField(15);
            final JPasswordField passwordTF = createPasswordField();
            final JLabel         statusLbl  = createLabel(" ");
            
            if (wasInError)
            {
                setErrorMsg(statusLbl, "Your username or password was not correct.");
            }
            
            ImageIcon imgIcon    = new ImageIcon(this.getClass().getResource("SpecifySmalliPad128x128.png"));
            JPanel    loginPanel = DatabaseLoginPanel.createLoginPanel("Username", userNameTF, "Password", passwordTF, statusLbl, imgIcon);
            if (!iPadDBExporter.IS_TESTING) // ZZZ
            {
                if (checkInstitutionInfo(false))
                {
                    while (true)
                    {
                        userNameTF.setText(userName);
                        CustomDialog dlg = new CustomDialog((Frame)getMostRecentWindow(), 
                                getResourceString("iPad Cloud Login"), true, CustomDialog.OKCANCELAPPLY, loginPanel)
                        {
                            @Override
                            protected void applyButtonPressed()
                            {
                                String uName = userNameTF.getText();
                                
                                if (!iPadCloud.isUserNameOK(uName))
                                {
                                    super.applyButtonPressed();
                                } else
                                {
                                    setErrorMsg(statusLbl, getFormattedResStr("USRNM_IS_TAKEN", uName));
                                }
                            }
                        };
                        dlg.setCloseOnApplyClk(true);
                        dlg.setApplyLabel(getResourceString("NEW_USER"));
                        dlg.setOkLabel(getResourceString("LOGIN"));
                        
                        centerAndShow(dlg);
                        
                        if (!dlg.isCancelled())
                        {
                            boolean isOK = true;
                            String uName = userNameTF.getText();
                            String pwd   = new String(passwordTF.getPassword());
                            if (dlg.getBtnPressed() == CustomDialog.APPLY_BTN)
                            {
                                Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                                if (!iPadCloud.addNewUser(uName, pwd, inst.getUri()))
                                {
                                    setErrorMsg(statusLbl, getResourceString("ERROR_CREAT_ACCOUNT"));
                                    isOK = false;
                                }
                            }
                            if (isOK)
                            {
                                return new Pair<String, String>(uName, pwd);
                            }
                        } else
                        {
                            return null;
                        }
                    }
                }
            }
            return null;//new Pair<String, String>("testuser@ku.edu", "testuser@ku.edu");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            popResourceBundle();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        /*if (iPadCloudDBHelper.isLoggedIn())
        {
            return starterPane = getLoginPanel();
        }*/
        return starterPane = getExportPanel();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    /*public SubPaneIFace getLoginPanel()
    {
        SimpleDescPane pane = null;
        exportBtn = null;
        if (exportBtn == null)
        {
            JLabel desc    = createLabel("Login Into the iPad Cloud", SwingConstants.CENTER);
            JLabel iconLbl = createLabel("", new ImageIcon(this.getClass().getResource("export_spmobile.png")));
            loginBtn       = createButton("Login");
            signInBtn      = createButton("Create Account");
            
            desc.setFont(desc.getFont().deriveFont(24.0f));
            
            CellConstraints cc = new CellConstraints();
            
            PanelBuilder pb0 = new PanelBuilder(new FormLayout("f:p:g,p,12px,p,f:p:g", "p"));
            pb0.add(exportBtn,      cc.xy(2,1));
            pb0.add(signInBtn,      cc.xy(4,1));
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,20px,p,20px,p,f:p:g"));
            pb.add(desc,           cc.xy(2,2));
            pb.add(iconLbl,        cc.xy(2,4));
            pb.add(pb0.getPanel(), cc.xy(2,6));
            
            pane = new SimpleDescPane(title, this, pb.getPanel());
            
            loginBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    loginToCloud();
                }
            });
            
            signInBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    createAcount();
                }
            });
        } else
        {
            exportBtn.setEnabled(true);
        }
        return pane;

    }*/
    
//    private void loginToCloud()
//    {
//        
//    }
//
//    private void createAcount()
//    {
//    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getExportPanel()
    {
        loadAndPushResourceBundle(RES_NAME);
        
        panelTitle = null;
        if (panelTitle == null)
        {
            panelTitle     = createI18NLabel("NOT_LOGIN", SwingConstants.CENTER);
            JLabel desc    = createI18NLabel("EXPORTIN_TO_IPAD", SwingConstants.CENTER);
            JLabel iconLbl = createLabel("  ", new ImageIcon(this.getClass().getResource("export_spmobile.png")));
            
            desc.setFont(desc.getFont().deriveFont(24.0f));
            
            CellConstraints cc = new CellConstraints();
            
            PanelBuilder pb0 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
            pb0.add(exportBtn,      cc.xy(2,1));
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "20px,p,f:p:g,p,20px,p,20px,p,f:p:g"));
            pb.add(panelTitle,     cc.xy(2,2));
            pb.add(desc,           cc.xy(2,4));
            pb.add(iconLbl,        cc.xy(2,6));
            pb.add(pb0.getPanel(), cc.xy(2,8));
            
            starterPane = new SimpleDescPane(title, this, pb.getPanel());
            
            exportBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    processDB();
                }
            });
        }
        popResourceBundle();
        return starterPane;
    }
    
    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.add(actionNavBox);
        
        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString("iPad Exporter");
        String hint     = getResourceString("ipadexport_hint");
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        if (AppPreferences.getRemote().getBoolean(ON_TASKBAR, false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        String menuDesc = "Specify.SYSTEM_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canView())
        {
            loadAndPushResourceBundle(RES_NAME);
            String    menuTitle = "iPadDBExporterPlugin.PLUGIN_MENU"; //$NON-NLS-1$
            String    mneu      = "t";//"iPadDBExporterPlugin.PLUGIN_MNEU"; //$NON-NLS-1$
            String    desc      = menuTitle;//"iPadDBExporterPlugin.PLUGIN_DESC"; //$NON-NLS-1$
            JMenuItem mi        = createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    iPadDBExporterPlugin.this.requestContext();
                }
            });
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            rsMI.setPosition(MenuItemDesc.Position.After);
            rsMI.setSepPosition(MenuItemDesc.Position.Before);
            menuItems.add(rsMI);
            popResourceBundle(); 
        }
        
        return menuItems;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * 
     */
    private void processDB()
    {
//        if (!remoteImageSetup(true)) // always show
//        {
//            return;
//        }
        if (!checkInstitutionInfo(false))
        {
            return;
        }
        
        try
        {
            loadAndPushResourceBundle(RES_NAME);
            
            int totalColObjRecords = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionobject");
            if (totalColObjRecords > maxRequiredRecs-1)
            {
                double ratio = 510.0 / 720.0;
                int    width = 1024;
                int    height = (int)((width * ratio) + 0.5); 
                if (iPadDBExporter == null)
                {
                    iPadDBExporter = new iPadDBExporter(iPadCloud, "isite.db", width, height);
                }
                
                writeSimpleGlassPaneMsg(getResourceString("EXPORTING"), 24);
                
                AppContextMgr ac = AppContextMgr.getInstance();
                if (ac != null)
                {
                    Collection        coll     = AppContextMgr.getInstance().getClassObject(Collection.class);
                    Discipline        disp     = AppContextMgr.getInstance().getClassObject(Discipline.class);
                    Division          div      = AppContextMgr.getInstance().getClassObject(Division.class);
                    TaxonTreeDef      taxDef   = disp.getTaxonTreeDef();
                    GeographyTreeDef  geoDef   = disp.getGeographyTreeDef();
                    LithoStratTreeDef lithoDef = disp.getLithoStratTreeDef();
                    GeologicTimePeriodTreeDef gtpDef = disp.getGeologicTimePeriodTreeDef();
    
                    iPadDBExporter.initialize();
                    
                    iPadDBExporter.createMappings(coll.getId(), 
                                                  disp.getId(), 
                                                  div.getId(), 
                                                  taxDef.getId(), 
                                                  geoDef.getId(), 
                                                  lithoDef.getId(), 
                                                  gtpDef.getId());
                    
                    exportBtn.setEnabled(false);
                    
                    ChangeListener cl = new ChangeListener()
                    {
                        @Override
                        public void stateChanged(ChangeEvent e)
                        {
                            popResourceBundle();

                            clearSimpleGlassPaneMsg();
                            exportBtn.setEnabled(true);
                        }
                    };
                    
                    if (!iPadDBExporter.createSQLiteDatabase(null, cl))
                    {
                        cl.stateChanged(new ChangeEvent(this));
                    }
                }
            } else
            {
                showLocalizedError("ERR_TOO_FEW", maxRequiredRecs);
                popResourceBundle();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * 
     */
    private void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
        
        if (appPrefs == AppPreferences.getRemote())
        {
            // Note: The event send with the name of pref from the form
            // not the name that was saved. So we don't need to append the discipline name on the end
            Object value = cmdAction.getProperties().get(ON_TASKBAR);
            if (value != null && value instanceof Boolean)
            {
                /*
                 * This doesn't work because it isn't added to the Toolbar correctly
                 * */
                JToolBar toolBar = (JToolBar)get(TOOLBAR);
                
                Boolean isChecked = (Boolean)value;
                if (isChecked)
                {
                    TaskMgr.addToolbarBtn(toolBarBtn, toolBar.getComponentCount()-1);
                } else
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                }
                toolBar.validate();
                toolBar.repaint();
                 
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(DBEXPORTER, "ENABLE", null, null, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(DBEXPORTER))
        {
            if (cmdAction.isAction(EXPORT_DB))
            {
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }
}