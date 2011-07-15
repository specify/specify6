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
package edu.ku.brc.af.auth;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValPasswordField;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class is used to get the Master Username and Password so the application can log in. It also
 * supports the UI and methods needed for changing the user's password and has methods that can be overridden
 * for when a user is created or removed.
 * 
 * This factory does not need ot be registered into the System properties, it will create itself as the default
 * factory/mgr.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jul 31, 2008
 *
 */
public class UserAndMasterPasswordMgr
{
    //private static final Logger log = Logger.getLogger(SecurityMgr.class);
    
    public static final String factoryName = "edu.ku.brc.af.auth.UserAndMasterPasswordMgr"; //$NON-NLS-1$
    
    public static final String MASTER_LOCAL = "master.islocal";
    public static final String MASTER_PATH  = "master.path";
    
    
    private static UserAndMasterPasswordMgr instance = null;
    
    private char                 currEcho;
    private String               showPwdLabel;
    private String               hidePwdLabel;
    private Pair<String, String> dbUsernameAndPassword = null;
    private String               usersUserName         = null;
    private String               usersPassword         = null;
    private String               databaseName          = null;
    
    
    /**
     * Protected Constructor
     */
    protected UserAndMasterPasswordMgr()
    {
        showPwdLabel = getResourceString("SHOW_PASSWORD");
        hidePwdLabel = getResourceString("HIDE_PASSWORD");
    }
    
    /**
     * @param includeDBName
     * @return
     */
    public String getIsLocalPrefPath(final boolean includeDBName)
    {
        return getIsLocalPrefPath(usersUserName, databaseName, includeDBName);
    }
    
    /**
     * @param usrName
     * @param dbName
     * @param includeDBName
     * @return
     */
    public static String getIsLocalPrefPath(final String usrName, final String dbName, final boolean includeDBName)
    {
        return (StringUtils.isNotEmpty(dbName) && includeDBName? (dbName + '_') : "") + usrName + "_" + MASTER_LOCAL;
    }
    
    /**
     * @param includeDBName
     * @return
     */
    public String getMasterPrefPath(final boolean includeDBName)
    {
        return getMasterPrefPath(usersUserName, databaseName, includeDBName);
    }
    
    /**
     * @param usrName
     * @param dbName
     * @param includeDBName
     * @return
     */
    public static String getMasterPrefPath(final String usrName, final String dbName, final boolean includeDBName)
    {
        return (StringUtils.isNotEmpty(dbName) && includeDBName? (dbName + '_') : "") + usrName + "_" + MASTER_PATH;
    }
    
    /**
     * Displays a dialog that is used for changing the Mast Username and Password.
     * @param username the current user's username
     * @return true if changed successfully
     */
    public boolean editMasterInfo(final String username, final String dbName, final boolean askForCredentials)
    {
        String uNameCached = username != null ? username: usersUserName;
        usersUserName = username;
        
        Boolean isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(true), null);
        if (isLocal == null)
        {
            isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(false), null); 
        } else
        {
            AppPreferences.getLocalPrefs().remove(getIsLocalPrefPath(false));
        }
        
        String  masterKey = AppPreferences.getLocalPrefs().get(getMasterPrefPath(true), null); 
        if (StringUtils.isEmpty(masterKey))
        {
            masterKey = AppPreferences.getLocalPrefs().get(getMasterPrefPath(false), null); 
        } else
        {
            AppPreferences.getLocalPrefs().remove(getMasterPrefPath(false));
        }
        
        if (masterKey == null)
        {
            if (askForCredentials && askToContForCredentials() == JOptionPane.NO_OPTION)
            {
                return false;
            }
        }
        boolean isOK = askForInfo(isLocal, usersUserName, dbName, masterKey);
        
        if (StringUtils.isEmpty(usersUserName) && StringUtils.isNotEmpty(uNameCached))
        {
            usersUserName = uNameCached;
        }
        
        return isOK;
    }
    
    public void set(final String usersUserName, final String usersPassword, final String databaseName)
    {
        this.usersUserName = usersUserName;
        this.usersPassword = usersPassword;
        this.databaseName  = databaseName;
        clear();
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName()
    {
        return databaseName;
    }

    /**
     * Clears db Username and Password.
     */
    public void clear()
    {
        dbUsernameAndPassword = null;
    }
    
    /**
     * @return true if the Master has been setup, false if not.
     */
    public boolean hasMasterUsernameAndPassword()
    {
        Boolean isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(true), null);
        if (isLocal == null)
        {
            isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(false), true); 
        }
        
        if (isLocal)
        {
            String masterKey = AppPreferences.getLocalPrefs().get(getMasterPrefPath(true), null); 
            if (StringUtils.isEmpty(masterKey))
            {
                return AppPreferences.getLocalPrefs().get(getMasterPrefPath(false), null) != null;
            }
        }
        return true; // using network approach
    }
    
    /**
     * @return the Master Username and Password
     */
    public Pair<String, String> getUserNamePasswordForDB()
    {
        if (dbUsernameAndPassword == null && usersPassword != null)
        {
            dbUsernameAndPassword = getUserNamePasswordInternal();
        }
        return dbUsernameAndPassword;
    }
    
    /**
     * @return
     */
    protected int askToContForCredentials()
    {
        int userChoice = JOptionPane.NO_OPTION;
        Object[] options = { getResourceString("Continue"),  //$NON-NLS-1$
                             getResourceString("CANCEL")  //$NON-NLS-1$
              };
        loadAndPushResourceBundle("masterusrpwd");

        userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getLocalizedMessage("MISSING_CREDS", usersUserName),  //$NON-NLS-1$
                                                     getResourceString("MISSING_CREDS_TITLE"),  //$NON-NLS-1$
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        popResourceBundle();
        
        return userChoice;
    }
    
    /**
     * @return Username and Password as a pair
     */
    protected Pair<String, String> getUserNamePasswordInternal()
    {
        Pair<String, String> noUP = new Pair<String, String>("", "");
        
        Boolean isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(true), null);
        if (isLocal == null)
        {
            isLocal = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(false), null); 
        }
        
        String masterKey = AppPreferences.getLocalPrefs().get(getMasterPrefPath(true), null); 
        if (StringUtils.isEmpty(masterKey))
        {
            masterKey = AppPreferences.getLocalPrefs().get(getMasterPrefPath(false), null); 
        }
        
        if (isLocal == null || StringUtils.isEmpty(masterKey))
        {
            if (askToContForCredentials() == JOptionPane.NO_OPTION)
            {
                return null;
            }
            
            if (!askForInfo(null, null, null, null))
            {
                return noUP;//getUserNamePassword();
            }
        }
        
        if (StringUtils.isNotEmpty(masterKey))
        {
            String keyStr = null;
            if (isLocal)
            {
                try
                {
                    keyStr = Encryption.decrypt(masterKey, usersPassword);
                    if (keyStr == null)
                    {
                        return noUP;
                    }
                    
                } catch (Exception ex) // catch any exception
                {
                    return noUP;
                }
                
            } else
            {
                keyStr = getResourceStringFromURL(masterKey, usersUserName, usersPassword);
                if (StringUtils.isNotEmpty(keyStr))
                {
                    try
                    {
                        keyStr = Encryption.decrypt(keyStr, usersPassword);
                        
                    } catch (Exception ex) // catch any exception
                    {
                        return noUP;
                    }
                } else
                {
                    return noUP;
                }
            }
            
            String[] tokens = StringUtils.split(keyStr, ",");
            if (tokens.length == 2)
            {
                return new Pair<String, String>(tokens[0], tokens[1]);
            }
            return noUP;
        }
        return noUP;

    }
    
    /**
     * Displays a dialog used for editing the Master Username and Password.
     * @param isLocal whether u/p is stored locally or not
     * @param usrName
     * @param dbName
     * @param masterPath the path to the password
     * @return whether to ask for the information because it wasn't found
     */
    protected boolean askForInfo(final Boolean isLocal, 
                                 final String  usrName, 
                                 final String  dbName,
                                 final String  masterPath)
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        FormLayout layout = new FormLayout("p, 2px, f:p:g, 4px, p, 4px, p, 4px, p", "p,2px,p,2px,p,2dlu,p,2dlu,p");
                 
        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();
        
        ButtonGroup        group         = new ButtonGroup();
        final JRadioButton isNetworkRB   = new JRadioButton(getResourceString("IS_NET_BASED"));
        final JRadioButton isPrefBasedRB = new JRadioButton(getResourceString("IS_ENCRYPTED_KEY"));
        isPrefBasedRB.setSelected(true);
        group.add(isNetworkRB);
        group.add(isPrefBasedRB);
        
        final JTextField keyTxt      = createTextField(35);
        final JTextField urlTxt      = createTextField(35);
        final JLabel     keyLbl      = createI18NFormLabel("ENCRYPTED_USRPWD");
        final JLabel     urlLbl      = createI18NFormLabel("URL");
        final JButton    createBtn   = createI18NButton("CREATE_KEY");
        
        final JButton    copyCBBtn   = createIconBtn("ClipboardCopy", IconManager.IconSize.Std24, "CPY_TO_CB_TT", null);
        final JButton    pasteCBBtn  = createIconBtn("ClipboardPaste", IconManager.IconSize.Std24, "CPY_FROM_CB_TT", null);
        
        // retrieves the encrypted key for the current settings in the dialog
        String dbNameFromForm = AppPreferences.getLocalPrefs().get("login.databases_selected", null);
        if (isNotEmpty(dbNameFromForm) && isNotEmpty(usersUserName))
        {
            String masterKey = getMasterPrefPath(usersUserName, dbNameFromForm, true);
            if (isNotEmpty(masterKey))
            {
                String encryptedKey = AppPreferences.getLocalPrefs().get(masterKey, null);
                if (isNotEmpty(encryptedKey))
                {
                    keyTxt.setText(encryptedKey);
                }
            }
        }
        
        CellConstraints cc = new CellConstraints(); 
        
        int y = 1;
        pb.add(createI18NFormLabel("MASTER_LOC"), cc.xywh(1, y, 1, 3));
        pb.add(isPrefBasedRB, cc.xy(3, y)); y += 2;
        pb.add(isNetworkRB,   cc.xy(3, y)); y += 2;
        
        pb.addSeparator("", cc.xyw(1, y, 9));  y += 2;
        pb.add(keyLbl,    cc.xy(1, y)); 
        pb.add(keyTxt,    cc.xy(3, y)); 
        pb.add(createBtn, cc.xy(5, y));  
        pb.add(copyCBBtn, cc.xy(7, y));  
        pb.add(pasteCBBtn, cc.xy(9, y));  y += 2;
        
        pb.add(urlLbl,    cc.xy(1, y)); 
        pb.add(urlTxt,    cc.xy(3, y));  y += 2;
        
        boolean isEditMode = isLocal != null && isNotEmpty(masterPath);
        if (isEditMode)
        {
            isPrefBasedRB.setSelected(isLocal);
            if (isLocal)
            {
                keyTxt.setText(masterPath);
            } else
            {
                urlTxt.setText(masterPath);
            }
        }
        
        copyCBBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Copy to Clipboard
                UIHelper.setTextToClipboard(keyTxt.getText());
            }
        });
        
        pasteCBBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keyTxt.setText(UIHelper.getTextFromClipboard());
            }
        });
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_TITLE"), true, CustomDialog.OKCANCELHELP, pb.getPanel());
        if (!isEditMode)
        {
            dlg.setOkLabel(getResourceString("CONT"));
            dlg.setCancelLabel(getResourceString("BACK"));
        }
        dlg.setHelpContext("MASTERPWD_MAIN");
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        urlLbl.setEnabled(false);  
        urlTxt.setEnabled(false);
        copyCBBtn.setEnabled(true);  
        pasteCBBtn.setEnabled(true);  
        
        DocumentListener dl = new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent e)
            {
                dlg.getOkBtn().setEnabled((isPrefBasedRB.isSelected() && !keyTxt.getText().isEmpty()) || 
                                          (isNetworkRB.isSelected() && !urlTxt.getText().isEmpty()));
            }
        };
        keyTxt.getDocument().addDocumentListener(dl);
        urlTxt.getDocument().addDocumentListener(dl);
        
        ChangeListener chgListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                boolean isNet = isNetworkRB.isSelected();
                keyLbl.setEnabled(!isNet);  
                keyTxt.setEnabled(!isNet);  
                createBtn.setEnabled(!isNet);  
                copyCBBtn.setEnabled(!isNet);  
                pasteCBBtn.setEnabled(!isNet);  
                urlLbl.setEnabled(isNet);  
                urlTxt.setEnabled(isNet);
                dlg.getOkBtn().setEnabled((isPrefBasedRB.isSelected() && !keyTxt.getText().isEmpty()) || 
                                          (isNetworkRB.isSelected() && !urlTxt.getText().isEmpty()));
            }
        };
        
        isNetworkRB.addChangeListener(chgListener);
        isPrefBasedRB.addChangeListener(chgListener);
        
        boolean isPref = AppPreferences.getLocalPrefs().getBoolean(getIsLocalPrefPath(usrName, dbName, true), true);
        isNetworkRB.setSelected(!isPref);
        isPrefBasedRB.setSelected(isPref);
        
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String[] keys = getUserNamePasswordKey();
                if (keys != null && keys.length == 4)
                {
                    String encryptedStr = encrypt(keys[0], keys[1], keys[3]);
                    if (encryptedStr != null)
                    {
                        keyTxt.setText(encryptedStr);
                        dlg.getOkBtn().setEnabled(true);
                        
                        usersUserName = keys[2];
                    }
                }
            }
        });

        popResourceBundle();
        
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            String value;
            if (isNetworkRB.isSelected())
            {
                value = StringEscapeUtils.escapeHtml(urlTxt.getText());
            } else
            {
                value = keyTxt.getText();
            }
            
            AppPreferences.getLocalPrefs().putBoolean(getIsLocalPrefPath(usrName, dbName, true), !isNetworkRB.isSelected());
            AppPreferences.getLocalPrefs().put(getMasterPrefPath(usrName, dbName, true), value);
            return true;
        }
        return false;
    }
    
    /**
     * @return
     */
    protected String[] getUserNamePasswordKey()
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        FormLayout layout = new FormLayout("p, 4dlu, p, 8px, p", 
                                           "p, 2dlu, p, 2dlu, p, 16px, p, 2dlu, p, 2dlu, p");
        layout.setRowGroups(new int[][] { { 1, 3, 5 } });
        
        PanelBuilder pb = new PanelBuilder(layout);

        final JTextField     dbUsrTxt    = createTextField(30);
        final JPasswordField dbPwdTxt    = createPasswordField(30);
        final JTextField     usrText     = createTextField(30);
        final JPasswordField pwdText     = createPasswordField(30);
        final char           echoChar    = pwdText.getEchoChar();
        
        final JLabel     dbUsrLbl    = createI18NFormLabel("USERNAME", SwingConstants.RIGHT);
        final JLabel     dbPwdLbl    = createI18NFormLabel("PASSWORD", SwingConstants.RIGHT);
        final JLabel     usrLbl      = createI18NFormLabel("USERNAME", SwingConstants.RIGHT);
        final JLabel     pwdLbl      = createI18NFormLabel("PASSWORD", SwingConstants.RIGHT);
        
        usrText.setText(usersUserName);
        
        CellConstraints cc = new CellConstraints(); 
        
        int y = 1;
        pb.addSeparator(UIRegistry.getResourceString("MASTER_SEP"), cc.xyw(1, y, 5)); y += 2;
        
        pb.add(dbUsrLbl, cc.xy(1, y)); 
        pb.add(dbUsrTxt, cc.xy(3, y)); y += 2;
        
        pb.add(dbPwdLbl, cc.xy(1, y)); 
        pb.add(dbPwdTxt, cc.xy(3, y)); y += 2;
        
        pb.addSeparator(UIRegistry.getResourceString("USER_SEP"), cc.xyw(1, y, 5)); y += 2;
        
        pb.add(usrLbl,   cc.xy(1, y)); 
        pb.add(usrText,  cc.xy(3, y)); y += 2;
        
        pb.add(pwdLbl,   cc.xy(1, y)); 
        pb.add(pwdText,  cc.xy(3, y)); 
        
        pb.setDefaultDialogBorder();
               
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_INFO_TITLE"), 
                                                  true, CustomDialog.OKCANCELAPPLYHELP, pb.getPanel());
        dlg.setOkLabel(getResourceString("GENERATE_KEY"));
        dlg.setHelpContext("MASTERPWD_GEN");
        dlg.setApplyLabel(showPwdLabel);

        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        popResourceBundle();
        
        DocumentListener docListener = new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                String dbUserStr = dbUsrTxt.getText();
                
                boolean enable = !dbUserStr.isEmpty() &&
                                 !((JTextField)dbPwdTxt).getText().isEmpty() &&
                                 !usrText.getText().isEmpty() &&
                                 !((JTextField)pwdText).getText().isEmpty();
                if (enable && isNotEmpty(dbUserStr) && dbUserStr.equalsIgnoreCase("root"))
                {
                    loadAndPushResourceBundle("masterusrpwd");
                    UIRegistry.showLocalizedError("MASTER_NO_ROOT");
                    popResourceBundle();
                    enable = false;
                }
                dlg.getOkBtn().setEnabled(enable);
            }
        };
        
        dbUsrTxt.getDocument().addDocumentListener(docListener);
        dbPwdTxt.getDocument().addDocumentListener(docListener);
        usrText.getDocument().addDocumentListener(docListener);
        pwdText.getDocument().addDocumentListener(docListener);

        currEcho = echoChar;
        
        dlg.getApplyBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dlg.getApplyBtn().setText(currEcho == echoChar ? hidePwdLabel : showPwdLabel);
                currEcho = currEcho == echoChar ? 0 : echoChar;
                pwdText.setEchoChar(currEcho);
                dbPwdTxt.setEchoChar(currEcho);
            }
        });
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return new String[] { dbUsrTxt.getText(), ((JTextField)dbPwdTxt).getText(), usrText.getText(), ((JTextField)pwdText).getText()};
        }
        
        return null;      
    }
    
    /**
     * Calls the URL by adding the username and password to the URL in the form of
     * "u=<username>&p=<password" both are encrypted so they are not password as clear text.
     * @param urlLoc the URL called to retrive the Master's username,password
     * @param username the encrypted user's username
     * @param password the encrypted user's password
     * @return a single string containing the Master username and password that can be decypted with
     * the user's password.
     */
    protected String getResourceStringFromURL(final String urlLoc,
                                              final String username,
                                              final String password)
    {
        String encrytpedStr = Encryption.encrypt(username+","+password, username);
        String fullURL      = urlLoc + "?data=" + encrytpedStr + ";db=" + DBConnection.getInstance().getDatabaseName();
        
        Exception exception = null;
        BufferedReader bufRdr = null;
        try 
        {
            URL url = new URL(fullURL); 

            URLConnection urlConn = url.openConnection(); 
            urlConn.setDoInput(true); 
            urlConn.setUseCaches(false);

            StringBuilder sb = new StringBuilder();
            String s;
            bufRdr = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((s = bufRdr.readLine()) != null) 
            { 
                sb.append(s); 
            }
            bufRdr.close();
            
            return sb.toString();
            
        } 
        catch (MalformedURLException mue) 
        {
            mue.printStackTrace();
            exception = mue;
        } 
        catch (IOException ioe) 
        {
            ioe.printStackTrace();
            exception = ioe;
            
        } catch (Exception ex) 
        {
            ex.printStackTrace(); 
            exception = ex;
            
        } finally
        {
            try
            {
                if (bufRdr != null) bufRdr.close();
                
            } catch (IOException ioe) 
            {
                ioe.printStackTrace();
                exception = ioe;
            }
        }
        
        if (exception != null)
        {
            UIRegistry.showLocalizedError("WEBSRV_ERROR", exception.toString());
        }
        return null;
    }

    /**
     * @param username
     * @param password
     * @param encyptionKey
     * @return
     */
    public static String encrypt(final String username, 
                                 final String password,
                                 final String encyptionKey)
    {
        return Encryption.encrypt(username+","+password, encyptionKey);
    }
    
    /**
     * Display a dialog for changing the password.
     * @return true if successful, false if in error or the user clicked on Cancel
     */
    public boolean changePassword()
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                "SystemSetup",
                "ChangePassword",
                null,
                getResourceString(getResourceString("CHG_PWD_TITLE")),
                "OK",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        dlg.setHelpContext("CHANGE_PWD");
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        
        dlg.setFormAdjuster(new FormPane.FormPaneAdjusterIFace() {
            @Override
            public void adjustForm(final FormViewObj fvo)
            {
                final ValPasswordField   oldPwdVTF    = fvo.getCompById("1");
                final ValPasswordField   newPwdVTF    = fvo.getCompById("2");
                final ValPasswordField   verPwdVTF    = fvo.getCompById("3");
                final PasswordStrengthUI pwdStrenthUI = fvo.getCompById("4");
                
                DocumentAdaptor da = new DocumentAdaptor() {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        super.changed(e);
                        
                        // Need to invoke later so the da gets to set the enabled state last.
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run()
                            {
                                boolean enabled = dlg.getOkBtn().isEnabled();
                                String  pwdStr  = new String(newPwdVTF.getPassword());
                                boolean pwdOK   = pwdStrenthUI.checkStrength(pwdStr);
                                
                                dlg.getOkBtn().setEnabled(enabled && pwdOK);
                                pwdStrenthUI.repaint();
                            }
                        });
                    }
                };
                
                oldPwdVTF.getDocument().addDocumentListener(da);
                verPwdVTF.getDocument().addDocumentListener(da);
                newPwdVTF.getDocument().addDocumentListener(da);
            }
        
        });
        Hashtable<String, String> valuesHash = new Hashtable<String, String>();
        dlg.setData(valuesHash);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            int pwdLen = 6;
            
            String oldPwd  = valuesHash.get("OldPwd");
            String newPwd1 = valuesHash.get("NewPwd1");
            String newPwd2 = valuesHash.get("NewPwd2");
            
            if (newPwd1.equals(newPwd2))
            {
                if (newPwd1.length() < pwdLen)
                {
                    SpecifyUser spUser    = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                    String      spuOldPwd = spUser.getPassword();
                    if (oldPwd.equals(spuOldPwd))
                    {
                        popResourceBundle();
                        return changePassword(oldPwd, newPwd2);
                    }
                    UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getResourceString("PWD_ERR_BAD")), Color.RED);
                } else
                {
                    UIRegistry.writeTimedSimpleGlassPaneMsg(getFormattedResStr(getResourceString("PWD_ERR_LEN"), pwdLen), Color.RED);
                }
            } else
            {
                UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getResourceString("PWD_ERR_NOTSAME")), Color.RED);
            }
        }
        popResourceBundle();
        return false;
    }
    
    /**
     * This method changes the password and will display error dialogs when appropriate.
     * @param oldPwd the old password
     * @param newPwd the new password
     * @return true if successful
     */
    protected boolean changePassword(final String oldPwd, 
                                     final String newPwd)
    {
        SpecifyUser spUser    = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        Pair<String, String> masterPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
        
        String encryptedMasterUP = UserAndMasterPasswordMgr.encrypt(masterPwd.first, masterPwd.second, newPwd);
        if (isNotEmpty(encryptedMasterUP))
        {
            AppPreferences.getLocalPrefs().put(getMasterPrefPath(true), encryptedMasterUP);
            spUser.setPassword(newPwd);
            if (DataModelObjBase.save(spUser))
            {
                return true;
            }
            
            UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getResourceString("PWD_ERR_SAVE")), Color.RED);
        } else
        {
            UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(getResourceString("PWD_ERR_RTRV")), Color.RED);
        }
        return false;
    }
    
    /**
     * @param username
     * @param password
     * @return
     */
    public boolean createUser(final String username, final String password)
    {
        return true;
    }

    /**
     * @param username
     * @param password
     * @return
     */
    public boolean deleteUser(final String username, final String password)
    {
        return true;
    }
    
    
    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static UserAndMasterPasswordMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (UserAndMasterPasswordMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserAndMasterPasswordMgr.class, e);
                InternalError error = new InternalError("Can't instantiate UserAndMasterPasswordMgr factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new UserAndMasterPasswordMgr();
    }
}


