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
package edu.ku.brc.af.ui.db;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.dbsupport.SchemaUpdateService.SchemaUpdateType;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.specify.config.init.SpecifyDBSetupWizardFrame;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.JTiledPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;
import edu.ku.brc.util.Pair;

/**
 * This panel enables the user to configure all the params necessary to log into a JDBC database.<BR>
 * <BR>
 * The login is done asynchronously and the panel is notified if it was successful or not. A
 * DatabaseLoginListener can be registered to be notified of a successful login or when the cancel
 * button is pressed. <BR>
 * <BR>
 * NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by
 * the user. <BR>
 * <BR>
 * The "extra" portion of the dialog that is initially hidden is for configuring the driver (the
 * fully specified class name of the driver) and the protocol for the JDBC connection string.
 * 
 * @code_status Complete
 * 
 * @author rods
 * 
 */
public class DatabaseLoginPanel extends JTiledPanel
{
    private static final Logger          log            = Logger.getLogger(DatabaseLoginPanel.class);

    // Form Stuff

    protected JTextField                 username;
    protected JPasswordField             password;

    protected JEditComboBox              databases;
    protected JEditComboBox              servers;

    protected JCheckBox                  rememberUsernameCBX;
    protected JButton                    editKeyInfoBtn;

    protected JButton                    cancelBtn;
    protected JButton                    loginBtn;
    protected JButton                    helpBtn;
    protected JCheckBox                  moreBtn;
    protected ImageIcon                  forwardImgIcon;
    protected ImageIcon                  downImgIcon;

    protected JStatusBar                 statusBar;

    // Extra UI
    protected JComboBox                  dbDriverCBX;
    protected JPanel                     extraPanel;

    protected JDialog                    thisDlg;
    protected boolean                    isCancelled    = true;
    protected boolean                    isLoggingIn    = false;
    protected boolean                    isAutoClose    = false;
    protected boolean                    doLoginWithDB  = true;
    protected boolean                    engageUPPrefs  = false;

    protected DatabaseLoginListener      dbListener;
    protected JaasContext                jaasContext;
    protected Window                     window;

    protected Vector<DatabaseDriverInfo> dbDrivers      = new Vector<DatabaseDriverInfo>();

    // User Feedback Data Members
    protected long                       elapsedTime    = -1;
    protected long                       loginCount     = 0;
    protected long                       loginAccumTime = 0;
    protected ProgressWorker             progressWorker = null;

    protected String                     title;
    protected String                     appName;
    
    protected String                      ssUserName     = null;
    protected String                      ssPassword     = null;
    protected MasterPasswordProviderIFace masterUsrPwdProvider = null;
    
    protected boolean                     doSaveUPPrefs     = true;
    protected boolean                     checkForProcesses = true;
    
    //--------------------------------------------------------------------
    public interface MasterPasswordProviderIFace
    {
        /**
         * @return true if the Master Username and Password has been setup.
         */
        public abstract boolean hasMasterUserAndPwdInfo(final String username, final String password, final String databaseName);
        
        /**
         * @param username the user's username
         * @param password the user's password
         * @return the Master UserName and Password
         */
        public abstract Pair<String, String> getUserNamePassword(String username, String password, final String databaseName);
        
        /**
         * Shows UI for the user to set up the Master Username and Password
         * @param username the user's username
         * @param askForCredentials it should or should not ask for the credentials when they are missing
         * @return true if it was setup correctly
         */
        public abstract boolean editMasterInfo(String username, final String databaseName, boolean askForCredentials);
    }
    
    /**
     * Constructor that has the form created from the view system
     * @param userName single signon username (for application)
     * @param password single signon password (for application)
     * @param engageUPPrefs indicates whether the username and password should be loaded and remembered by local prefs
     * @param dbListener listener to the panel (usually the frame or dialog)
     * @param isDlg whether the parent is a dialog (false mean JFrame)
     * @param iconName name of icon to use
     * @param helpContext context for help btn on dialog
     */
    public DatabaseLoginPanel(final String                userName,
                              final String                password,
                              final boolean               engageUPPrefs,
                              final DatabaseLoginListener dbListener,  
                              final boolean               isDlg,
                              final String                iconName,
                              final String                helpContext)
    {
        this(userName, password, engageUPPrefs, dbListener, isDlg, null, null, iconName, helpContext);
    }
    
    /**
     * Constructor that has the form created from the view system
     * @param userName single signon username (for application)
     * @param password single signon password (for application)
     * @param dbListener listener to the panel (usually the frame or dialog)
     * @param isDlg whether the parent is a dialog (false mean JFrame)
     * @param title the title for the title bar
     * @param appName the name of the app
     * @param iconName name of icon to use
     * @param helpContext context for help btn on dialog
     */
    public DatabaseLoginPanel(final String userName,
                              final String password,
                              final boolean               engageUPPrefs,
                              final DatabaseLoginListener dbListener,  
                              final boolean isDlg, 
                              final String title,
                              final String appName,
                              final String iconName,
                              final String helpContext)
    {
        this(userName, password, engageUPPrefs, null, dbListener, isDlg, true, title, appName, iconName, helpContext);
    }

    /**
     * Constructor that has the form created from the view system
     * @param userName single signon username (for application)
     * @param password single signon password (for application)
     * @param usrPwdProvider sprivdes app username and password
     * @param dbListener listener to the panel (usually the frame or dialog)
     * @param isDlg whether the parent is a dialog (false mean JFrame)
     * @param title the title for the title bar
     * @param appName the name of the app
     * @param iconName name of icon to use
     * @param helpContext context for help btn on dialog
     */
    public DatabaseLoginPanel(final MasterPasswordProviderIFace usrPwdProvider,
                              final boolean               engageUPPrefs,
                              final DatabaseLoginListener dbListener,  
                              final boolean isDlg, 
                              final String title,
                              final String appName,
                              final String iconName,
                              final String helpContext)
    {
        this(null, null, engageUPPrefs, usrPwdProvider, dbListener, isDlg, true, title, appName, iconName, helpContext);
    }

    /**
     * Constructor that has the form created from the view system
     * @param userName single signon username (for application)
     * @param password single signon password (for application)
     * @param masterUsrPwdProvider provides a hook to get the Master UserName and Password
     * @param dbListener listener to the panel (usually the frame or dialog)
     * @param isDlg whether the parent is a dialog (false mean JFrame)
     * @param doLoginWithDB whether it should login using the database name
     * @param title the title for the title bar
     * @param appName the name of the app
     * @param iconName name of icon to use
     * @param helpContext context for help btn on dialog
     */
    public DatabaseLoginPanel(final String userName,
                              final String password,
                              final boolean               engageUPPrefs,
                              final MasterPasswordProviderIFace masterUsrPwdProvider,
                              final DatabaseLoginListener dbListener,  
                              final boolean isDlg,
                              final boolean doLoginWithDB,
                              final String title,
                              final String appName,
                              final String iconName,
                              final String helpContext)
    {
        this.ssUserName    = userName;
        this.ssPassword    = password;
        this.masterUsrPwdProvider = masterUsrPwdProvider;
        this.dbListener    = dbListener;
        this.jaasContext   = new JaasContext(); 
        this.title         = title;
        this.appName       = appName;
        this.doSaveUPPrefs = engageUPPrefs;
        this.doLoginWithDB = doLoginWithDB;
        this.engageUPPrefs = engageUPPrefs;
        
        createUI(isDlg, iconName, helpContext);
        
        SkinItem skinItem = SkinsMgr.getSkinItem("LoginPanel");
        if (skinItem != null)
        {
            skinItem.setupPanel(this);
        }
    }

    /**
     * Sets a window to be resized for extra options
     * @param window the window
     */
    public void setWindow(Window window)
    {
        this.window = window;
    }

    /**
     * Returns the owning window.
     * @return the owning window.
     */
    public Window getWindow()
    {
        return window;
    }

    /**
     * @return the login btn
     */
    public JButton getLoginBtn()
    {
        return loginBtn;
    }

    /**
     * Returns the statusbar.
     * @return the statusbar.
     */
    public JStatusBar getStatusBar()
    {
        return statusBar;
    }

    /**
     * Creates a line in the form.
     * 
     * @param label JLabel text
     * @param comp the component to be added
     * @param pb the PanelBuilder to use
     * @param cc the CellConstratins to use
     * @param y the 'y' coordinate in the layout of the form
     * @return return an incremented by 2 'y' position
     */
    protected int addLine(final String label,
                          final JComponent comp,
                          final PanelBuilder pb,
                          final CellConstraints cc,
                          final int y)
    {
        int yy = y;
        pb.add(createLabel(label != null ? getResourceString(label) + ":" : " ", //$NON-NLS-1$ //$NON-NLS-2$
                SwingConstants.RIGHT), cc.xy(1, yy));
        pb.add(comp, cc.xy(3, yy));
        yy += 2;
        return yy;
    }
    
    /**
     * @param usrName
     * @param dbName
     * @param includeDBName
     * @return
     */
    private String getPasswordPrefPath(final String usrName, final String dbName, final boolean includeDBName)
    {
        return (StringUtils.isNotEmpty(dbName) && includeDBName? (dbName + '_' + usrName + "_") : "")  + "login.password";
    }

    /**
     * @param usrName
     * @param dbName
     * @param includeDBName
     * @return
     */
    private String getUserPrefPath(final String dbName, final boolean includeDBName)
    {
        String key = (StringUtils.isNotEmpty(dbName) && includeDBName? (dbName + '_') : "")  + "login.username";
        return key;
    }

    /**
     * Creates the UI for the login and hooks up any listeners.
     * @param isDlg  whether the parent is a dialog (false mean JFrame)
     * @param iconName the icon that will be shown in the panel
     * @param engageUPPrefs whether it should load and save the username password into the prefs
     * @param helpContext the help context to use.
     */
    protected void createUI(final boolean isDlg,
                            final String  iconName,
                            final String  helpContext)
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        //Font cachedFont = UIManager.getFont("JLabel.font");
        SkinItem skinItem = SkinsMgr.getSkinItem("LoginPanel");
        if (skinItem != null)
        {
            skinItem.pushFG("Label.foreground");
        }
        
        // First create the controls and hook up listeners

        PropertiesPickListAdapter dbPickList = new PropertiesPickListAdapter("login.databases"); //$NON-NLS-1$
        PropertiesPickListAdapter svPickList = new PropertiesPickListAdapter("login.servers"); //$NON-NLS-1$

        username = createTextField(15);
        password = createPasswordField(15);
        
        FocusAdapter focusAdp = new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                super.focusGained(e);
                
                JTextField tf = (JTextField)e.getSource();
                tf.selectAll();
            }
        };
        username.addFocusListener(focusAdp);
        password.addFocusListener(focusAdp);

        databases = new JEditComboBox(dbPickList);
        servers   = new JEditComboBox(svPickList);
        dbPickList.setComboBox(databases);
        svPickList.setComboBox(servers);
        
        setControlSize(password);
        setControlSize(databases);
        setControlSize(servers);
        
        if (masterUsrPwdProvider != null)
        {
            /*editKeyInfoBtn = UIHelper.createIconBtn("Key", IconManager.IconSize.Std16, null, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (masterUsrPwdProvider != null)
                    {
                        masterUsrPwdProvider.editMasterInfo(username.getText());
                    }
                }
            });
            editKeyInfoBtn.setText("Configure Master Key...");
            editKeyInfoBtn.setEnabled(true);
            */
            editKeyInfoBtn = UIHelper.createI18NButton("CONFIG_MSTR_KEY");
            editKeyInfoBtn.setIcon(IconManager.getIcon("Key", IconManager.IconSize.Std20));
            editKeyInfoBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (masterUsrPwdProvider != null)
                    {
                        final PickListItemIFace pli = (PickListItemIFace)databases.getSelectedItem();
                        masterUsrPwdProvider.editMasterInfo(username.getText(), pli.getValue(), false);
                    }
                }
            });
        }

        rememberUsernameCBX = createCheckBox(getResourceString("rememberuser")); //$NON-NLS-1$
        rememberUsernameCBX.setEnabled(engageUPPrefs);
        
        statusBar = new JStatusBar();
        statusBar.setErrorIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16)); //$NON-NLS-1$

        cancelBtn = createButton(getResourceString("CANCEL")); //$NON-NLS-1$
        loginBtn  = createButton(getResourceString("Login")); //$NON-NLS-1$
        helpBtn   = createButton(getResourceString("HELP")); //$NON-NLS-1$
        
        forwardImgIcon = IconManager.getIcon("Forward"); //$NON-NLS-1$
        downImgIcon    = IconManager.getIcon("Down"); //$NON-NLS-1$
        moreBtn        = new JCheckBox(getResourceString("LOGIN_DLG_MORE"), forwardImgIcon); // XXX I18N //$NON-NLS-1$
        setControlSize(moreBtn);

        // Extra
        dbDrivers = DatabaseDriverInfo.getDriversList();
        dbDriverCBX = createComboBox(dbDrivers);
        if (dbDrivers.size() > 0)
        {
            if (dbDrivers.size() == 1)
            {
                dbDriverCBX.setSelectedIndex(0);
                dbDriverCBX.setEnabled(false);
                
            } else
            {
                String selectedStr = localPrefs.get("login.dbdriver_selected", "MySQL"); //$NON-NLS-1$ //$NON-NLS-2$
                int inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(selectedStr, null, null, false, null));
                dbDriverCBX.setSelectedIndex(inx > -1 ? inx : -1);
            }

        } else
        {
            JOptionPane.showConfirmDialog(null, getResourceString("NO_DBDRIVERS"), //$NON-NLS-1$
                    getResourceString("NO_DBDRIVERS_TITLE"), JOptionPane.CLOSED_OPTION); //$NON-NLS-1$
            System.exit(1);
        }

        dbDriverCBX.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateUIControls();
            }
        });

        addFocusListenerForTextComp(username);
        addFocusListenerForTextComp(password);

        addKeyListenerFor(username, !isDlg);
        addKeyListenerFor(password, !isDlg);

        addKeyListenerFor(databases.getTextField(), !isDlg);
        addKeyListenerFor(servers.getTextField(), !isDlg);

        if (!isDlg)
        {
            addKeyListenerFor(loginBtn, true);
        }

        rememberUsernameCBX.setSelected(engageUPPrefs ? localPrefs.getBoolean("login.rememberuser", false) : false); //$NON-NLS-1$
        
        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (dbListener != null)
                {
                    dbListener.cancelled();
                }
            }
        });

        loginBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doLogin();
            }
        });

        HelpMgr.registerComponent(helpBtn, helpContext); //$NON-NLS-1$

        moreBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (extraPanel.isVisible())
                {
                    if (dbDriverCBX.getSelectedIndex() != -1)
                    {
                        extraPanel.setVisible(false);
                        moreBtn.setIcon(forwardImgIcon);
                    }

                } else
                {
                    extraPanel.setVisible(true);
                    moreBtn.setIcon(downImgIcon);
                }
                if (window != null)
                {
                    window.pack();
                }
            }
        });

        // Ask the PropertiesPickListAdapter to set the index from the prefs
        dbPickList.setSelectedIndex();
        svPickList.setSelectedIndex();

        servers.getTextField().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });

        databases.getTextField().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });
        
        databases.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println(e.getActionCommand() + " "+e.getSource());
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setUsrPwdControlsFromPrefs(); 
                    }
                });
            }
        });
        
        setUsrPwdControlsFromPrefs();

        // Layout the form

        PanelBuilder    formBuilder = new PanelBuilder(new FormLayout("p,3dlu,p:g", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc          = new CellConstraints();
        formBuilder.addSeparator(getResourceString("LOGINLABEL"), cc.xywh(1, 1, 3, 1)); //$NON-NLS-1$

        addLine("username", username, formBuilder, cc, 3); //$NON-NLS-1$
        addLine("password", password, formBuilder, cc, 5); //$NON-NLS-1$
        formBuilder.add(moreBtn,                   cc.xy(3, 7));
        
        PanelBuilder extraPanelBlder = new PanelBuilder(new FormLayout("p,3dlu,p:g",//$NON-NLS-1$ 
                UIHelper.createDuplicateJGoodiesDef("p", "2dlu", 9))); //$NON-NLS-1$ //$NON-NLS-2$
        
        extraPanel = extraPanelBlder.getPanel();
        extraPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

        //extraPanelBlder.addSeparator("", cc.xywh(1, 1, 3, 1)); //$NON-NLS-1$
        int y = 1;
        y = addLine(null,        rememberUsernameCBX, extraPanelBlder, cc, y);
        y = addLine("databases", databases,           extraPanelBlder, cc, y); //$NON-NLS-1$
        y = addLine("servers",   servers,             extraPanelBlder, cc, y); //$NON-NLS-1$
        
        y = addLine("driver", dbDriverCBX, extraPanelBlder, cc, y); //$NON-NLS-1$
        if (editKeyInfoBtn != null)
        {
            PanelBuilder pb = new PanelBuilder(new FormLayout("p,f:p:g", "p"));
            pb.add(editKeyInfoBtn, cc.xy(1, 1));
            y = addLine(null, pb.getPanel(), extraPanelBlder, cc, y);
            pb.getPanel().setOpaque(false);
        }
        extraPanel.setVisible(false);

        formBuilder.add(extraPanelBlder.getPanel(), cc.xywh(3, 9, 1, 1));

        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p,3dlu,p:g", "t:p,2dlu,p,2dlu,p"), this); //$NON-NLS-1$ //$NON-NLS-2$
        JLabel icon = StringUtils.isNotEmpty(iconName) ? new JLabel(IconManager.getIcon(iconName)) : null;
        
        if (icon != null)
        {
            icon.setBorder(BorderFactory.createEmptyBorder(10, 10, 2, 2));
        }

        formBuilder.getPanel().setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));

        if (icon != null)
        {
            outerPanel.add(icon, cc.xy(1, 1));
        }
        JPanel btnPanel = ButtonBarFactory.buildOKCancelHelpBar(loginBtn, cancelBtn, helpBtn);
        outerPanel.add(formBuilder.getPanel(), cc.xy(3, 1));
        outerPanel.add(btnPanel,               cc.xywh(1, 3, 3, 1));
        outerPanel.add(statusBar,              cc.xywh(1, 5, 3, 1));
        
        formBuilder.getPanel().setOpaque(false);
        outerPanel.getPanel().setOpaque(false);
        btnPanel.setOpaque(false);
        
        updateUIControls();
        
        if (skinItem != null)
        {
            skinItem.popFG("Label.foreground");
        }
    }
    
    /**
     * @return the String of the database name from the edit combobox
     */
    private String getSelectedDatabase()
    {
        Object obj = databases.getSelectedItem();
        if (obj instanceof PickListItemIFace)
        {
            return ((PickListItemIFace)obj).getValue();
        }
        return obj != null ? obj.toString() : null;
    }
    
    /**
     * 
     */
    private void setUsrPwdControlsFromPrefs()
    {
        String dbName = getSelectedDatabase();
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        String userNameStr = "";
        String pwdStr      = "";
        
        if (StringUtils.isNotEmpty(dbName) && engageUPPrefs)
        {
            userNameStr = localPrefs.get(getUserPrefPath(dbName, true), null);
            if (userNameStr == null)
            {
                userNameStr = localPrefs.get(getUserPrefPath(dbName, false), null);
            }
            
            if (StringUtils.isNotEmpty(userNameStr))
            {
                pwdStr = localPrefs.get(getPasswordPrefPath(userNameStr, dbName, true), null);
                if (pwdStr == null)
                {
                    pwdStr = localPrefs.get(getPasswordPrefPath(userNameStr, dbName, false), null);
                }
                if (pwdStr != null)
                {
                    pwdStr = Encryption.decrypt(pwdStr);
                }
            }
        }

        if (StringUtils.isNotEmpty(userNameStr) && rememberUsernameCBX.isSelected())
        {
            username.setText(userNameStr); //$NON-NLS-1$ //$NON-NLS-2$
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    password.requestFocus();
                }
            });
        }
        
        if (StringUtils.isNotEmpty(pwdStr) && !UIRegistry.isRelease() || localPrefs.getBoolean("pwd.save", false))
        {
            password.setText(pwdStr); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Creates a focus listener so the UI is updated when the focus leaves
     * @param textField  the text field to be changed
     */
    protected void addFocusListenerForTextComp(final JTextComponent textField)
    {
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                updateUIControls();
            }
        });
    }

    /**
     * Creates a Document listener so the UI is updated when the doc changes
     * @param textField the text field to be changed
     */
    protected void addDocListenerForTextComp(final JTextComponent textField)
    {
        textField.getDocument().addDocumentListener(new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent e)
            {
                updateUIControls();
            }
        });
    }

    /**
     * Creates a Document listener so the UI is updated when the doc changes
     * @param uiComponent the text field to be changed
     */
    protected void addKeyListenerFor(final JComponent comp, final boolean checkForRet)
    {
        class KeyAdp extends KeyAdapter
        {
            private boolean checkForRetLocal = false;

            public KeyAdp(final boolean checkForRetArg)
            {
                this.checkForRetLocal = checkForRetArg;
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
                if (checkForRetLocal && e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    doLogin();
                }
            }
        }

        comp.addKeyListener(new KeyAdp(checkForRet));
    }

    /**
     * Enables or disables the UI based of the values of the controls. The Login button doesn't
     * become enabled unless everything is filled in. It also expands the "Extra" options if any of
     * them are missing a value
     */
    protected void updateUIControls()
    {
        if (extraPanel == null || isLoggingIn)
        {
            return;
        }

        String dbName = StringUtils.deleteWhitespace(databases.getTextField().getText());
        String uName  = username.getText();
        String pwd    = new String(password.getPassword());
        
        boolean shouldEnable = StringUtils.isNotEmpty(uName) &&
                               StringUtils.isNotEmpty(pwd) &&
                               (servers.getSelectedIndex() != -1 || StringUtils.isNotEmpty(servers.getTextField().getText())) && 
                               StringUtils.isNotEmpty(dbName);
        
        if (shouldEnable && (StringUtils.contains(uName, ' ') || StringUtils.contains(uName, ',')))
        {
            shouldEnable = false;
            setMessage(getResourceString("NO_SPC_USRNAME"), true); //$NON-NLS-1$
            
        } else if (shouldEnable && (StringUtils.contains(pwd, ' ') || StringUtils.contains(pwd, ',')))
        {
            shouldEnable = false;
            setMessage(getResourceString("NO_SPC_PWDNAME"), true); //$NON-NLS-1$
            
        } else if (shouldEnable && (StringUtils.contains(dbName, ' ') || StringUtils.contains(dbName, ',')))
        {
            shouldEnable = false;
            setMessage(getResourceString("NO_SPC_DBNAME"), true); //$NON-NLS-1$
            
        } else if (dbDriverCBX.getSelectedIndex() == -1)
        {
            shouldEnable = false;
            setMessage(getResourceString("MISSING_DRIVER"), true); //$NON-NLS-1$
            if (!extraPanel.isVisible())
            {
                moreBtn.doClick();
            }
        }

        loginBtn.setEnabled(shouldEnable);

        if (shouldEnable)
        {
            setMessage("", false); //$NON-NLS-1$
        }
    }

    /**
     * Sets a string into the status bar
     * @param msg the msg for the status bar
     * @param isError whether the text should be shown in the error color
     */
    public void setMessage(final String msg, final boolean isError)
    {
        if (statusBar != null)
        {
            if (isError)
            {
                statusBar.setErrorMessage(msg, null);
            } else
            {
                statusBar.setText(msg);
            }
        }
    }

    /**
     * Saves the values out to prefs
     */
    protected void save()
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();

        databases.getDBAdapter().save();
        servers.getDBAdapter().save();

        if (doSaveUPPrefs)
        {
            String dbName = null;
            if (databases.getSelectedItem() instanceof PickListItemIFace)
            {
                PickListItemIFace pli = (PickListItemIFace)databases.getSelectedItem();
                if (pli != null)
                {
                    dbName = pli.getValue();
                }
            } else
            {
                dbName = (String)databases.getSelectedItem();
            }
            
            localPrefs.putBoolean("login.rememberuser", rememberUsernameCBX.isSelected()); //$NON-NLS-1$

            if (rememberUsernameCBX.isSelected())
            {
                localPrefs.put(getUserPrefPath(dbName, true), username.getText()); //$NON-NLS-1$
            }
    
            if (!UIRegistry.isRelease() || localPrefs.getBoolean("pwd.save", false))
            {
                localPrefs.put(getPasswordPrefPath(username.getText(), dbName, true), Encryption.encrypt(new String(password.getPassword()))); //$NON-NLS-1$
            }
        }
        
        localPrefs.put("login.dbdriver_selected", dbDrivers.get(dbDriverCBX.getSelectedIndex()).getName()); //$NON-NLS-1$


    }

    /**
     * Indicates the login is OK and closes the dialog for the user to continue on
     */
    protected void loginOK()
    {
        Component parent = getParent();
        while (!(parent instanceof Window))
        {
            parent = parent.getParent();
        }
        
        isCancelled = false;
        if (dbListener != null)
        {
            dbListener.loggedIn((Window)parent, getDatabaseName(), getUserName());
        }
        else
        {
            log.debug("listener is NULL"); //$NON-NLS-1$
        }
    }

    /**
     * Tells it whether the parent (frame or dialog) should be auto closed when it is logged in
     * successfully.
     * @param isAutoClose true / false
     */
    public void setAutoClose(final boolean isAutoClose)
    {
        this.isAutoClose = isAutoClose;
    }

    /**
     * Helper to enable all the UI components.
     * @param enable true or false
     */
    protected void enableUI(final boolean enable)
    {
        cancelBtn.setEnabled(enable);
        loginBtn.setEnabled(enable);
        helpBtn.setEnabled(enable);

        username.setEnabled(enable);
        password.setEnabled(enable);
        databases.setEnabled(enable);
        servers.setEnabled(enable);
        rememberUsernameCBX.setEnabled(enable);
        dbDriverCBX.setEnabled(enable);
        moreBtn.setEnabled(enable);
        
        if (editKeyInfoBtn != null)
        {
            editKeyInfoBtn.setEnabled(enable);
        }
    }
    
    /**
     * @return
     */
    private Pair<String, String> getMasterUsrPwd()
    {
        String usr = null;
        String pwd = null;
        
        if (masterUsrPwdProvider != null)
        {
            Pair<String, String> masterUsrPwd = masterUsrPwdProvider.getUserNamePassword(getUserName(), getPassword(), getDatabaseName());
            if (masterUsrPwd == null)
            {
                return null;
            }
            if (StringUtils.isEmpty(masterUsrPwd.first) || StringUtils.isEmpty(masterUsrPwd.second))
            {
                setMessage(getResourceString("BAD_USRPWD"), true);
                return null;
            }
            usr = masterUsrPwd.first;
            pwd = masterUsrPwd.second;
            
        } else
        {
            usr = StringUtils.isNotEmpty(ssUserName) ? ssUserName : getUserName();
            pwd = StringUtils.isNotEmpty(ssPassword) ? ssPassword : getPassword();
        }
        return new Pair<String, String>(usr, pwd); 
    }

    /**
     * Performs a login on a separate thread and then notifies the dialog if it was successful.
     */
    public void doLogin()
    {
        isLoggingIn = true;
        save();
        
        if (masterUsrPwdProvider != null && !masterUsrPwdProvider.hasMasterUserAndPwdInfo(getUserName(), getPassword(), getDatabaseName()))
        {
            if (!masterUsrPwdProvider.editMasterInfo(getUserName(), getDatabaseName(), true))
            {
                isLoggingIn = false;
                return;
            }
        }

        final String name = getClass().getName();
        statusBar.setIndeterminate(name, true);
        enableUI(false);
        
        setMessage(String.format(getResourceString("LoggingIn"), new Object[] { getDatabaseName() }), false); //$NON-NLS-1$

        String basePrefName = getDatabaseName() + "." + getUserName() + "."; //$NON-NLS-1$ //$NON-NLS-2$

        loginCount     = AppPreferences.getLocalPrefs().getLong(basePrefName + "logincount", -1L); //$NON-NLS-1$
        loginAccumTime = AppPreferences.getLocalPrefs().getLong(basePrefName + "loginaccumtime", -1L);//$NON-NLS-1$
                
        if (loginCount != -1 && loginAccumTime != -1)
        {
            int timesPerSecond = 4;
            progressWorker = new ProgressWorker(statusBar.getProgressBar(), 0,(int) (((double) loginAccumTime / (double) loginCount) + 0.5), timesPerSecond);
            new Timer(1000 / timesPerSecond, progressWorker).start();

        } else
        {
            loginCount = 0;
        }

        final SwingWorker worker = new SwingWorker()
        {
            long    eTime;
            boolean isLoggedIn       = false;
            boolean timeOK           = false;
            boolean isLoginCancelled = false;
            
            Pair<String, String> usrPwd = null;

            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public Object construct()
            {
                if (checkForProcesses && (DBConnection.getInstance().isEmbedded() || UIRegistry.isMobile())) // isEmbdded may not be setup yet
                {
                    SpecifyDBSetupWizardFrame.checkForMySQLProcesses();
                    checkForProcesses = false;
                }
                
                if (UIRegistry.isMobile())
                {
                    File mobileTmpDir = DBConnection.getMobileMachineDir(getDatabaseName());
                    if (mobileTmpDir == null)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, new RuntimeException("Couldn't get MobileTempDir"));
                    }
                    
                    UIRegistry.setEmbeddedDBPath(mobileTmpDir.getAbsolutePath());
                    //log.debug(UIRegistry.getEmbeddedDBPath());
                    
                    if (UIRegistry.getMobileEmbeddedDBPath() == null)
                    {
                        UIRegistry.setMobileEmbeddedDBPath(UIRegistry.getDefaultMobileEmbeddedDBPath(getDatabaseName()));
                        //log.debug(UIRegistry.getMobileEmbeddedDBPath());
                    }
                }
                
                String connStr = getConnectionStr();
                
                DBConnection.checkForEmbeddedDir(connStr);
                
                UIRegistry.dumpPaths();
                
                eTime = System.currentTimeMillis();
                
                usrPwd = getMasterUsrPwd();
                if (usrPwd != null)
                {
                    isLoggedIn = UIHelper.tryLogin(getDriverClassName(), 
                                                   getDialectClassName(),
                                                   getDatabaseName(), 
                                                   getConnectionStr(), 
                                                   usrPwd.first, 
                                                   usrPwd.second);
                    if (isLoggedIn && masterUsrPwdProvider != null)
                    {
                        isLoggedIn &= jaasLogin();
                    }
                } else if (usrPwd == null)
                {
                    isLoginCancelled = true;
                    return null;
                }

                if (isLoggedIn)
                {
                    if (StringUtils.isNotEmpty(appName))
                    {
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run()
                            {
                                //Not exactly true yet, but make sure users know that this is NOT Specify starting up. 
                                setMessage(String.format(getResourceString("Starting"), appName), false); //$NON-NLS-1$
                            }
                        });
                    }
                    
                    DatabaseDriverInfo drvInfo = dbDrivers.get(dbDriverCBX.getSelectedIndex());
                    if (drvInfo != null)
                    {
                        DBConnection.getInstance().setDbCloseConnectionStr(drvInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Close, getServerName(), getDatabaseName()));
                        DBConnection.getInstance().setServerName(getServerName());
                        DBConnection.getInstance().setDriverName(((DatabaseDriverInfo)dbDriverCBX.getSelectedItem()).getName());
                        DBConnection.getInstance().setConnectionStr(drvInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, getServerName(), getDatabaseName()));
                        
                        // This needs to be done before Hibernate starts up
                        SchemaUpdateType status = SchemaUpdateService.getInstance().updateSchema(UIRegistry.getAppVersion());
                        if (status == SchemaUpdateType.Error)
                        {
                            StringBuilder sb = new StringBuilder();
                            for (String s : SchemaUpdateService.getInstance().getErrMsgList())
                            {
                                sb.append(s);
                                sb.append("\n");
                            }
                            sb.append(getResourceString("APP_EXIT")); // 18N
                            UIRegistry.showError(sb.toString());
                            
                            CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit", null));
                            
                        } else if (status == SchemaUpdateType.Success)
                        {
                            UIRegistry.showLocalizedMsg(JOptionPane.QUESTION_MESSAGE, "INFORMATION", "SCHEMA_UP_OK");
                        }
                    }
                }
                return null;
            }

            // Runs on the event-dispatching thread.
            @Override
            public void finished()
            {
                statusBar.setIndeterminate(name, false);
                
                // I am not sure this is the rightplace for this
                // but this is where I am putting it for now
                if (isLoggedIn)
                {
                    setMessage(getResourceString("LoadingSchema"), false); //$NON-NLS-1$
                    statusBar.repaint();

                    // Note: this doesn't happen on the GUI thread
                    DataProviderFactory.getInstance().shutdown();

                    // This restarts the System
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    session.close();
                    
                    long endTime = System.currentTimeMillis();
                    eTime = (endTime - eTime) / 1000;
                    timeOK = true;

                    if (progressWorker != null)
                    {
                        progressWorker.stop();
                    }

                    statusBar.setProgressDone(getClass().getName());

                    if (timeOK)
                    {
                        elapsedTime = eTime;
                        loginAccumTime += elapsedTime;

                        if (loginCount < 1000)
                        {
                            String basePrefNameStr = getDatabaseName() + "." + getUserName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
                            AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "logincount", ++loginCount);//$NON-NLS-1$
                            AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "loginaccumtime", loginAccumTime);//$NON-NLS-1$
                                    
                        }
                    }
                    
                    loginOK();
                    
                    isLoggingIn = false;
                    
                } else 
                {
                    if (!isLoginCancelled)
                    {
                        String msg = DBConnection.getInstance().getErrorMsg();
                        setMessage(StringUtils.isEmpty(msg) ? getResourceString("INVALID_LOGIN") : msg, true);
                        
                        if (DBConnection.getInstance().isEmbedded() || UIRegistry.isMobile())
                        {
                            DataProviderFactory.getInstance().shutdown();
                            DBConnection.shutdown();
                            DBConnection.shutdownFinalConnection(false, true);
                            DBConnection.startOver();
                            
                        } else if (usrPwd != null)
                        {
                            doCheckPermissions(usrPwd);
                        }
                    }
                    
                    enableUI(true);
                    isLoggingIn = false;
                }

                if (isAutoClose)
                {
                    updateUIControls();
                }

            }
        };
        worker.start();
    }
    
    /**
     * @return a username/password pair if valid or null if canceled
     * @throws SQLException
     */
    public static Pair<String, String> getITUsernamePwd()
    {
        JTextField     userNameTF = UIHelper.createTextField(15);
        JPasswordField passwordTF = UIHelper.createPasswordField();
        JLabel         statusLbl  = UIHelper.createLabel("");
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,10px,p"));
        
        pb.add(UIHelper.createI18NFormLabel("IT_Username"), cc.xy(1, 1));
        pb.add(userNameTF, cc.xy(3, 1));
        
        pb.add(UIHelper.createI18NFormLabel("IT_Password"), cc.xy(1, 3));
        pb.add(passwordTF, cc.xy(3, 3));
        
        pb.add(statusLbl, cc.xyw(1, 5, 3));
        
        pb.setDefaultDialogBorder();
        
        while (true)
        {
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), UIRegistry.getResourceString("IT_LOGIN"), true, pb.getPanel());
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                String uName = userNameTF.getText();
                String pwd   = new String(passwordTF.getPassword());
    
                DBConnection dbc    = DBConnection.getInstance();
                DBConnection dbConn = DBConnection.createInstance(dbc.getDriver(), 
                                                                  dbc.getDialect(), 
                                                                  dbc.getDatabaseName(), 
                                                                  dbc.getConnectionStr(), 
                                                                  uName, 
                                                                  pwd);
                if (dbConn != null)
                {
                    DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                    dbMgr.close();
                    
                    if (dbMgr.connect(uName, pwd, dbc.getServerName(), dbc.getDatabaseName()))
                    {
                        dbMgr.close();
                        return new Pair<String, String>(uName, pwd);
                    }
                    dbMgr.close();
                    statusLbl.setText("<HTML><font color=\"red\">"+UIRegistry.getResourceString("IT_LOGIN_ERROR")+"</font></HTML>");
                }
            } else
            {
                return null;
            }
        }
    }
    
    /**
     * @param usrPwd
     */
    private void doCheckPermissions(final Pair<String, String> usrPwd)
    {
        if (System.getProperty("user.name").equals("rods"))
        {
            String       dbDriver   = DBConnection.getInstance().getDriver();
            String       serverName = getServerName();
            String       dbName     = getDatabaseName();
            SQLException loginEx    = DBConnection.getLoginException();
            
            if (StringUtils.isNotEmpty(dbDriver) && 
                    dbDriver.equals("com.mysql.jdbc.Driver") &&
                    loginEx != null &&
                    StringUtils.isNotEmpty(loginEx.getSQLState()) && 
                    loginEx.getSQLState().equals("08001"))
            {
                boolean doFixIt = UIRegistry.displayConfirmLocalized("MISSING_PRIV_TITLE", "MISSING_PRIV", "MISSING_PRIV_FIX", "Cancel", JOptionPane.WARNING_MESSAGE);
                if (doFixIt)
                {
                    DBConnection.getInstance().setServerName(serverName); // Needed for SchemaUpdateService
                    
                    Pair<String, String> itUP = getITUsernamePwd();
                    DBMSUserMgr mgr = DBMSUserMgr.getInstance();
                    try
                    {
                        if (mgr != null && mgr.connectToDBMS(itUP.first, itUP.second, serverName))
                        {
                            boolean isOK = mgr.setPermissions(usrPwd.first, dbName, DBMSUserMgr.PERM_ALL_BASIC);
                            UIRegistry.showLocalizedMsg(isOK ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE, getResourceString("MISSING_PRIV_TITLE"), (isOK ? "MISSING_PRIV_OK" : "MISSING_PRIV_ERR"));
                            mgr.close();
                        } else
                        {
                            UIRegistry.showError("MISSING_PRIV_NO_LOGIN");
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @return
     */
    public boolean jaasLogin()
    {
        if (jaasContext != null)
        {
            Pair<String, String> usrPwd = getMasterUsrPwd();
            
            return jaasContext.jaasLogin(getUserName(),
            							 getPassword(),
            							 getConnectionStr(), 
            							 getDriverClassName(),
            							 usrPwd.first,
            							 usrPwd.second);
        }

        return false;
    }
    
    /**
     * @return the server name
     */
    public String getServerName()
    {
        return servers.getTextField().getText().trim();
    }

    /**
     * @return the database name
     */
    public String getDatabaseName()
    {
        return doLoginWithDB ? databases.getTextField().getText().trim() : null;
    }

    /**
     * 
     * @return the username
     */
    public String getUserName()
    {
        return username.getText().trim();
    }

    /**
     * @return the password string
     */
    public String getPassword()
    {
        return new String(password.getPassword());
    }

    /**
     * @return the formatted connection string
     */
    public String getConnectionStr()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get(dbDriverCBX.getSelectedIndex()).getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, getServerName(), getDatabaseName());
        }
        // else
        return null; // we should never get here
    }

    /**
     * @return dialect clas name
     */
    public String getDialectClassName()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX.getSelectedIndex()).getDialectClassName();
        }
        // else
        return null; // we should never get here
    }

    /**
     * @return the driver class name
     */
    public String getDriverClassName()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX.getSelectedIndex()).getDriverClassName();
        }
        // else
        return null; // we should never get here
    }

    /**
     * Returns true if doing auto login
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return AppPreferences.getLocalPrefs().getBoolean("autologin", false); //$NON-NLS-1$
    }

    /**
     * Return whether dialog was cancelled
     * @return whether dialog was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    // -------------------------------------------------------------------------
    // -- Inner Classes
    // -------------------------------------------------------------------------

    class ProgressWorker implements ActionListener
    {
        protected int          timesASecond;
        protected JProgressBar progressBar;
        protected int          count;
        protected int          totalCount;
        protected boolean      stop = false;

        public ProgressWorker(final JProgressBar progressBar, final int count,
                final int totalCount, final int timesPerSecond)
        {
            this.timesASecond = timesPerSecond;
            this.progressBar = progressBar;
            this.count = count;
            this.totalCount = totalCount * timesASecond;

            this.progressBar.setIndeterminate(false);
            this.progressBar.setMinimum(0);
            this.progressBar.setMaximum(this.totalCount);
            // log.info("Creating PW: "+count+" "+this.totalCount);
        }

        public void actionPerformed(ActionEvent e)
        {
            count++;
            progressBar.setValue(count);

            if (!stop)
            {
                if (count < totalCount && progressBar.getValue() < totalCount)
                {
                    // nothing
                }
                else
                {
                    progressBar.setIndeterminate(true);
                }

            }
            else
            {
                ((Timer) e.getSource()).stop();
            }

        }

        public synchronized void stop()
        {
            this.stop = true;
        }
    }

    /**
     * @return the isLoggingIn
     */
    public boolean isLoggingIn()
    {
        return isLoggingIn;
    }

    /**
     * @param isLoggingIn the isLoggingIn to set
     */
    public void setLoggingIn(boolean isLoggingIn)
    {
        this.isLoggingIn = isLoggingIn;
    }
}
