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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

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
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
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
        
    protected static final String RESOURCE_NAME = "ipad_exporter";
    
    // Static Data Members
    private static final String ON_TASKBAR    = "iPadDBExporttask.OnTaskbar";
    private static final int maxRequiredRecs  = 0;
    
    public static final String DBEXPORTER     = "iPadDBExporter";
    public static final String EXPORT_DB      = "ExportRecordSet";
    public static final String ISOASSIGN_ICON = "iPadExportTask";

    // Data Members
    private String                  kErrorCreatingAcctMsg  = "";
    private iPadDBExporter          iPadDBExporterObj      = null;
    private JButton                 exportBtn;
    private JLabel                  panelTitle;
    
    private IPadCloudIFace          iPadCloud        = null;
    private NavBox                  actionNavBox     = null;
    private Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    private ToolBarDropDownBtn      toolBarBtn       = null;
    
    //private RolloverCommand         manageDataSetsBtn;
    private RolloverCommand         iPadInfoSetupBtn;
    private RolloverCommand         removeAccountBtn;
    private RolloverCommand         removeDatasetBtn;
    private RolloverCommand         loginBtn;
    private RolloverCommand         logoutBtn;
    private RolloverCommand         createAccountBtn;
    
    // Login Info
    private Integer                 cloudInstId = null;
    private boolean                 isLoggedIn  = false;

    /**
     * Constructor. 
     */
    public iPadDBExporterPlugin()
    {
        super(DBEXPORTER, getResourceString("iPadDBExporter"));
        this.iconName = ISOASSIGN_ICON;
        
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

            actionNavBox = new NavBox(getResourceString("Actions"));
            
            loadAndPushResourceBundle(RESOURCE_NAME);
            
            kErrorCreatingAcctMsg = getResourceString("ERROR_CREAT_ACCOUNT");
            
            createAccountBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("CREATE_ACCT"),     ISOASSIGN_ICON, null, null);
            loginBtn          = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("LOGIN"),     ISOASSIGN_ICON, null, null);
            iPadInfoSetupBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("IPAD_SETUP"), ISOASSIGN_ICON, null, null);
            removeAccountBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("DEL_ACCT"),  ISOASSIGN_ICON, null, null);
            removeDatasetBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("DEL_DATASET"),  ISOASSIGN_ICON, null, null);
            logoutBtn         = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("LOGOUT"),    ISOASSIGN_ICON, null, null);
            exportBtn         = createI18NButton("EXPORT_TO_IPAD");

            createAccountBtn.setEnabled(false);
            setUIEnabled(false);
            loginBtn.setEnabled(false);
            popResourceBundle();
            
            createAccountBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    createAccount();
                }
            });

            removeAccountBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    removeAccount();
                }
            });

            removeDatasetBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    removeDataset();
                }
            });

            loginBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    login(null);
                }
            });

            logoutBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    logout();
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
            exportBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    if (checkInstitutionInfo(false))
                    {
                        processDB();
                    }
                }
            });

        }
        
        //exportBtn.setEnabled(true); // YYY
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneShown(edu.ku.brc.af.core.SubPaneIFace)
     */
    public void subPaneShown(final SubPaneIFace subPane)
    {
        if (subPane == starterPane)
        {
            if (cloudInstId == null)
            {
                try
                {
                    Institution inst = iPadDBExporter.getCurrentInstitution();
                    cloudInstId = iPadCloud.getInstId(inst.getGuid());
                    if (cloudInstId == null)
                    {
                        loadAndPushResourceBundle(RESOURCE_NAME);
                        UIRegistry.showLocalizedMsg("NO_IPAD_INST_ACCOUNT");
                        popResourceBundle(); 
                    }
                    createAccountBtn.setEnabled(cloudInstId == null);
                    loginBtn.setEnabled(cloudInstId != null);
                    
                } catch (Exception ex)
                {
                    UIRegistry.showError("Error in iPad Exporter Plugin: "+ex.getMessage());
                }
            }
        }
    }
    
    /**
     * @param enabled
     */
    private void setUIEnabled(final boolean enabled)
    {
        //createAccountBtn.setEnabled(!enabled);
        removeAccountBtn.setEnabled(enabled);
        removeDatasetBtn.setEnabled(enabled);
        
        loginBtn.setEnabled(!enabled);
        logoutBtn.setEnabled(enabled);
        if (exportBtn != null) 
        {
            exportBtn.setEnabled(enabled);
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unused")
    private void manageDataSets()
    {
        ManageDataSetsDlg mdsDlg = new ManageDataSetsDlg(iPadCloud);
        centerAndShow(mdsDlg);
    }
    
    /**
     * 
     */
    private void removeAccount()
    {
        Institution inst = iPadDBExporter.getCurrentInstitution();
        if (iPadCloud.removeAccount(cloudInstId, inst.getGuid()))
        {
            createAccountBtn.setEnabled(true);
            removeAccountBtn.setEnabled(false);
            removeDatasetBtn.setEnabled(false);
            loginBtn.setEnabled(false);
            logoutBtn.setEnabled(false);
            
            UIRegistry.writeTimedSimpleGlassPaneMsg("The account was removed.");
        } else
        {
            UIRegistry.writeTimedSimpleGlassPaneMsg("There was a problem removing the account.");
        }
    }
    
    /**
     * 
     */
    private void removeDataset()
    {
        Institution                 inst = iPadDBExporter.getCurrentInstitution();
        List<Pair<String, String>> list = iPadCloud.getDatasetList(inst.getGuid());
        if (list != null && list.size() > 0)
        {
            CellConstraints cc = new CellConstraints();
            
            final JList<String> dsList = new JList<String>(new DSListModel(list));
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,f:p:g"));
            pb.add(UIHelper.createI18NLabel("Choose a Dataset to remove"), cc.xy(1, 1));
            pb.add(dsList, cc.xy(1, 3));
            pb.setDefaultDialogBorder();
            final CustomDialog dlg = new CustomDialog((Frame)getMostRecentWindow(), "Remove Collection", true, pb.getPanel());
            dsList.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        dlg.getOkBtn().setEnabled(!dsList.isSelectionEmpty());
                    }
                }
            });
            dlg.setPreferredSize(new Dimension(300, 400));
            dlg.createUI();
            dlg.getOkBtn().setEnabled(false);
            centerAndShow(dlg, 300, 400);
            if (!dlg.isCancelled())
            {
                String collectionGUID = list.get(dsList.getSelectedIndex()).first;
                if (StringUtils.isNotEmpty(collectionGUID))
                {
                    if (iPadCloud.removeDataSet(collectionGUID))
                    {
                        enableRemoveDatasetBtn();
                        UIRegistry.writeTimedSimpleGlassPaneMsg("The dataset was removed.");   
                    } else
                    {
                        UIRegistry.writeTimedSimpleGlassPaneMsg("The dataset was not removed.");
                    }
                }
            }
            return;
        }
        UIRegistry.showError("You do not have Datasets to remove."); // should not get here
    }
    
    /**
     * 
     */
    private void createAccount()
    {
        Institution inst = iPadDBExporter.getCurrentInstitution();
        if (StringUtils.isEmpty(inst.getGuid()))
        {
            UIRegistry.showError("Institution must have a GUID value.");
            return;
        }
        loadAndPushResourceBundle(RESOURCE_NAME);
        
        final JTextField     userNameTF = createTextField(15);
        final JPasswordField passwordTF = createPasswordField();
        final JLabel         statusLbl  = createLabel(" ");
        ImageIcon            imgIcon    = IconManager.getImage("SpecifySmalliPad128x128", IconManager.STD_ICON_SIZE.NonStd);
        JPanel               loginPanel = DatabaseLoginPanel.createLoginPanel("Username", userNameTF, "USRNM_EMAIL_HINT", "Password", passwordTF, statusLbl, imgIcon);

        final CustomDialog dlg = new CustomDialog((Frame)getMostRecentWindow(), 
                                         getResourceString("CREATE_INST_IN_CLOUD"), true, 
                                         CustomDialog.OKCANCEL, loginPanel)
        {
            @Override
            protected void okButtonPressed()
            {
                // NOTE: THis call should fail indicating is is not being used
                // so it if is OK then it is an error
                String uName = userNameTF.getText();                
                if (iPadCloud.isUserNameOK(uName))
                {
                    setErrorMsg(statusLbl, getFormattedResStr("USRNM_IS_TAKEN", uName));
                } else
                {
                    super.okButtonPressed();
                }
            }
        };
        userNameTF.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                boolean isOK = UIHelper.isValidEmailAddress(userNameTF.getText());
                dlg.getOkBtn().setEnabled(isOK);
            }
        });
        dlg.setOkLabel(getResourceString("NEW_ACCOUNT"));
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        centerAndShow(dlg);
        popResourceBundle();        
        
        if (!dlg.isCancelled())
        {
            cloudInstId = iPadCloud.createInstitution(inst.getName(), inst.getUri(), inst.getCode(), inst.getGuid());
            if (cloudInstId != null)
            {
                createAccountBtn.setEnabled(false);
                loginBtn.setEnabled(true);
                
                enableRemoveDatasetBtn();
                
                checkInstitutionInfo(false);
                
                String uName = userNameTF.getText();
                String pwd   = new String(passwordTF.getPassword());
                if (iPadCloud.addNewUser(uName, pwd, inst.getGuid()))
                {
                    if (iPadCloud.login(uName, pwd))
                    {
                        login(new Pair<String, String>(uName, pwd));
                    }
                } else
                {
                    setErrorMsg(statusLbl, kErrorCreatingAcctMsg);
                }

            } else
            {
                UIRegistry.showError(kErrorCreatingAcctMsg);
            }
        }
    }
    
    /**
     * @return
     */
    private boolean checkInstitutionInfo(final boolean forceDisplay)
    {
        if (!iPadDBExporter.IS_TESTING) // ZZZ       
        {
            InstitutionConfigDlg dlg = new InstitutionConfigDlg(iPadCloud, cloudInstId);
            if (forceDisplay || !dlg.isInstOK())
            {
                dlg.createUI();
                dlg.pack();
                centerAndShow(dlg, 800, null);
                return !dlg.isCancelled();
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
     * @return
     */
    private String getUserNamePrefName()
    {
        Division        div         = AppContextMgr.getInstance().getClassObject(Division.class);
        SpecifyUser     spUser      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        String          prefName    = String.format("IPAD_USERNAME_%d_%d", div.getId(), spUser.getId());
        return prefName;
    }
    
    /**
     * 
     */
    private void login(final Pair<String, String> loginUPInfo)
    {
        String          prefName    = getUserNamePrefName(); 
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        String          userName    = remotePrefs.get(prefName, "");
        boolean         wasInError  = false;
        while (true)
        {
            Pair<String, String> loginInfo = loginUPInfo != null ? loginUPInfo : getExportLoginCreds(userName, wasInError);
            if (loginInfo != null)
            {
                if (!iPadDBExporter.IS_TESTING) // ZZZ  
                {
                    writeSimpleGlassPaneMsg("Logging in...", 24);
                    userName = loginInfo.first;
                    if (iPadCloud.login(loginInfo.first, loginInfo.second))
                    {
                        remotePrefs.put(prefName, loginInfo.first);
                        loggedIn(loginInfo);
                        isLoggedIn = true;
                        clearSimpleGlassPaneMsg();
                        
                        enableRemoveDatasetBtn();
                        break;
                    } else
                    {
                        wasInError = true;
                        clearSimpleGlassPaneMsg();
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
    
    /**
     * 
     */
    private void logout()
    {
        if (isLoggedIn && iPadCloud.logout())
        {
            setUIEnabled(false);
            panelTitle.setText("");
            isLoggedIn = false;
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
        loginBtn.setEnabled(false);
        
        loadAndPushResourceBundle(RESOURCE_NAME);
        try
        {
            final JTextField     userNameTF = createTextField(15);
            final JPasswordField passwordTF = createPasswordField();
            final JLabel         statusLbl  = createLabel(" ");
            
            if (wasInError)
            {
                setErrorMsg(statusLbl, "Your username or password was not correct.");
            }
            
            ImageIcon imgIcon    = IconManager.getImage("SpecifySmalliPad128x128", IconManager.STD_ICON_SIZE.NonStd);
            JPanel    loginPanel = DatabaseLoginPanel.createLoginPanel("Username", userNameTF, "USRNM_EMAIL_HINT", "Password", passwordTF, statusLbl, imgIcon);
            if (!iPadDBExporter.IS_TESTING) // ZZZ
            {
                while (true)
                {
                    userNameTF.setText(userName);
                    final CustomDialog dlg = new CustomDialog((Frame)getMostRecentWindow(), 
                                                     getResourceString("iPad Cloud Login"), true, 
                                                     CustomDialog.OKCANCELAPPLY, loginPanel)
                    {
                        @Override
                        protected void applyButtonPressed()
                        {
                            String uName = userNameTF.getText();
                            if (iPadCloud.isUserNameOK(uName))
                            {
                                setErrorMsg(statusLbl, getFormattedResStr("USRNM_IS_TAKEN", uName));
                            } else
                            {
                                super.applyButtonPressed();
                            }
                        }
                    };
                    userNameTF.addKeyListener(new KeyAdapter()
                    {
                        @Override
                        public void keyTyped(KeyEvent e)
                        {
                            boolean isOK = UIHelper.isValidEmailAddress(userNameTF.getText());
                            dlg.getOkBtn().setEnabled(isOK);
                        }
                    });
                    
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
                            Institution inst = iPadDBExporter.getCurrentInstitution();
                            if (!iPadCloud.addNewUser(uName, pwd, inst.getGuid()))
                            {
                                setErrorMsg(statusLbl, kErrorCreatingAcctMsg);
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
            return null;//new Pair<String, String>("testuser@ku.edu", "testuser@ku.edu");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            popResourceBundle();
            loginBtn.setEnabled(true);
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
    public SubPaneIFace getExportPanel()
    {
        if (panelTitle == null || starterPane == null)
        {
            loadAndPushResourceBundle(RESOURCE_NAME);
            
            panelTitle     = createI18NLabel("NOT_LOGIN", SwingConstants.CENTER);
            JLabel desc    = createI18NLabel("EXPORTIN_TO_IPAD", SwingConstants.CENTER);
            JLabel iconLbl = createLabel("  ", IconManager.getImage("ExportSpMobile", IconManager.STD_ICON_SIZE.NonStd));
            
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
            
            popResourceBundle();
        }
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
            loadAndPushResourceBundle(RESOURCE_NAME);
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
        try
        {
            loadAndPushResourceBundle(RESOURCE_NAME);
            
            int totalColObjRecords = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionobject");
            if (totalColObjRecords > maxRequiredRecs-1)
            {
                double ratio = 510.0 / 720.0;
                int    width = 1024;
                int    height = (int)((width * ratio) + 0.5); 
                if (iPadDBExporterObj == null)
                {
                    iPadDBExporterObj = new iPadDBExporter(iPadCloud, "isite.db", width, height);
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
    
                    iPadDBExporterObj.initialize();
                    
                    iPadDBExporterObj.createMappings(coll.getId(), 
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
                            enableRemoveDatasetBtn();
                        }
                    };
                    
                    if (!iPadDBExporterObj.createSQLiteDatabase(null, cl))
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
    
    private void enableRemoveDatasetBtn()
    {
        Institution inst = iPadDBExporter.getCurrentInstitution();
        int dsCount = iPadCloud.getNumberOfDatasets(inst.getGuid());
        removeDatasetBtn.setEnabled(dsCount > 0);
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
            
        }  else if (cmdAction.isType("App") && cmdAction.isAction("AppRestart"))
        {
            if (isLoggedIn && iPadCloud != null)
            {
                logout();
            }
            if (iPadDBExporterObj != null)
            {
                iPadCloud.logout();
                iPadDBExporterObj.shutdown();
                iPadDBExporterObj = null;
            }
            //cloudInstId    = null;
        }
    }
    
    class DSListModel extends AbstractListModel<String> 
    {
        protected List<Pair<String, String>> list;
        
        /**
         * @param list
         */
        public DSListModel(List<Pair<String, String>> list)
        {
            super();
            this.list = list;
        }

        /* (non-Javadoc)
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize()
        {
            return list != null ? list.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public String getElementAt(int index)
        {
            return list != null ? list.get(index).second : "";
        }
        
    }
}