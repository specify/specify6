/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.auth;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIRegistry.displayErrorDlgLocalized;
import static edu.ku.brc.ui.UIRegistry.displayInfoMsgDlgLocalized;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getUserHomeDir;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 31, 2008
 *
 */
public class MasterPasswordMgr
{
    public static final String factoryName = "edu.ku.brc.af.auth.MasterPasswordMgr"; //$NON-NLS-1$
    
    private static final String MASTER_LOCAL = "master.islocal";
    private static final String MASTER_PATH  = "master.path";
    
    //private static final Logger log = Logger.getLogger(SecurityMgr.class);
    
    protected static MasterPasswordMgr instance = null;
    
    protected boolean errCreatingFile    = false;
    
    private Pair<String, String> dbUsernameAndPassword = null;
    
    private String               usersUserName       = null;
    private String               usersPassword       = null;

    
    /**
     * Protected Constructor
     */
    protected MasterPasswordMgr()
    {
        
    }
    
    /**
     * 
     */
    public boolean editMasterInfo(final String username)
    {
        String uNameCached = username != null ? username: usersUserName;
        usersUserName = username;
        
        Boolean isLocal   = AppPreferences.getLocalPrefs().getBoolean(usersUserName+"_"+MASTER_LOCAL, null);
        String  masterKey = AppPreferences.getLocalPrefs().get(usersUserName+"_"+MASTER_PATH, null); 
        
        boolean isOK = askForInfo(isLocal, masterKey);
        
        usersUserName = uNameCached;
        
        return isOK;
    }

    /**
     * @param usersPassword
     */
    public void setUsersPassword(String usersPassword)
    {
        this.usersPassword = usersPassword;
        clear();
    }

    /**
     * @param usersUserName
     */
    public void setUsersUserName(String usersUserName)
    {
        this.usersUserName = usersUserName;
        clear();
    }

    /**
     * Clears db Username and Password
     */
    public void clear()
    {
        dbUsernameAndPassword = null;
    }
    
    /**
     * @return
     */
    public Pair<String, String> getUserNamePassword()
    {
        if (dbUsernameAndPassword == null && usersPassword != null)
        {
            dbUsernameAndPassword = getUserNamePasswordInternal();
        }
        return dbUsernameAndPassword;
    }
    
    /**
     * @return Username and Password as a pair
     */
    protected Pair<String, String> getUserNamePasswordInternal()
    {
        Boolean isLocal   = AppPreferences.getLocalPrefs().getBoolean(usersUserName+"_"+MASTER_LOCAL, null);
        String  masterKey = AppPreferences.getLocalPrefs().get(usersUserName+"_"+MASTER_PATH, null);
        
        if (isLocal == null ||
            StringUtils.isEmpty(masterKey))
        {
            if (!askForInfo(null, null))
            {
                return getUserNamePassword();
            }
        }
        
        Pair<String, String> noUP = new Pair<String, String>("", "");
        
        if (StringUtils.isNotEmpty(masterKey))
        {
            String keyStr = null;
            if (isLocal)
            {
                keyStr = Encryption.decrypt(masterKey, usersPassword);
                
            } else
            {
                keyStr = getKeyFromURL(masterKey);
                if (StringUtils.isNotEmpty(keyStr))
                {
                    keyStr = Encryption.decrypt(keyStr, usersPassword);
                    
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
     * @param isNetworkBased
     * @param masterPath
     * @return
     */
    protected boolean askForInfo(final Boolean isLocal, 
                                 final String masterPath)
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        FormLayout layout = new FormLayout("p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p", "pref, 2dlu, pref, 2dlu, pref");
                 
        layout.setRowGroups(new int[][] { { 1, 3, 5 } });

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        
        ButtonGroup        group         = new ButtonGroup();
        final JRadioButton isNetworkRB   = new JRadioButton(getResourceString("IS_NET_BASED"));
        final JRadioButton isPrefBasedRB = new JRadioButton(getResourceString("IS_ENCRYPTED_KEY"));
        isPrefBasedRB.setSelected(true);
        group.add(isNetworkRB);
        group.add(isPrefBasedRB);
        
        final JTextField keyTxt      = new JTextField(30);
        final JTextField urlTxt      = new JTextField(30);
        final JLabel     keyLbl      = createI18NFormLabel("ENCRYPTED_USRPWD");
        final JLabel     urlLbl      = createI18NFormLabel("URL");
        final JButton    createBtn   = createI18NButton("CREATE_KEY");
        final JButton    fileBtn     = createI18NButton("CREATE_FILE");
        
        CellConstraints cc = new CellConstraints(); 
        panel.add(isPrefBasedRB, cc.xy(3, 1)); 
        panel.add(isNetworkRB,   cc.xy(5, 1)); 
        
        panel.add(keyLbl,    cc.xy (1, 3)); 
        panel.add(keyTxt,    cc.xyw (3, 3, 4)); 
        panel.add(createBtn, cc.xy (7, 3)); 
        panel.add(urlLbl,    cc.xy (1, 5)); 
        panel.add(urlTxt,    cc.xyw (3, 5, 4)); 
        panel.add(fileBtn,   cc.xy (7, 5)); 
        
        boolean isEditMode = isLocal != null && StringUtils.isNotEmpty(masterPath);
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
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_TITLE"), true, CustomDialog.OKCANCELHELP, panel);
        if (!isEditMode)
        {
            dlg.setOkLabel(getResourceString("CONT"));
            dlg.setCancelLabel(getResourceString("EXIT"));
        }
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        urlLbl.setEnabled(false);  
        urlTxt.setEnabled(false);
        fileBtn.setEnabled(false);
        
        ChangeListener chgListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                boolean isNet = isNetworkRB.isSelected();
                keyLbl.setEnabled(!isNet);  
                keyTxt.setEnabled(!isNet);  
                createBtn.setEnabled(!isNet);  
                urlLbl.setEnabled(isNet);  
                urlTxt.setEnabled(isNet);
                fileBtn.setEnabled(isNet);  
                dlg.getOkBtn().setEnabled(true);
            }
        };
        isNetworkRB.addChangeListener(chgListener);
        isPrefBasedRB.addChangeListener(chgListener);
        
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
                    }
                }
            }
        });
        
        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String[] keys = getUserNamePasswordKey();
                String encryptedStr = encrypt(keys[0], keys[1], keys[2]);
                if (encryptedStr != null)
                {
                    loadAndPushResourceBundle("masterusrpwd");
                    String keyFilePath = getUserHomeDir() + File.separator +"specify.key";
                    try
                    {
                        FileUtils.writeStringToFile(new File(keyFilePath), encryptedStr);
                        dlg.getOkBtn().setEnabled(true);
                        urlTxt.setText("");
                        displayInfoMsgDlgLocalized("FILE_CREATED_AT", keyFilePath);
                        
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                        displayErrorDlgLocalized("FILE_NOT_CREATED", keyFilePath);
                        
                    }
                    popResourceBundle();
                    
                    dlg.getOkBtn().setEnabled(true);
                }
            }
        });

        popResourceBundle();
        
        errCreatingFile = false;
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
            AppPreferences.getLocalPrefs().putBoolean(usersUserName+"_"+MASTER_LOCAL, !isNetworkRB.isSelected());
            AppPreferences.getLocalPrefs().put(usersUserName+"_"+MASTER_PATH, value);
            return true;
            
        } else if (!isEditMode)
        {
            System.exit(0);
        }
        
        return false;
    }
    
    /**
     * @return
     */
    protected String[] getUserNamePasswordKey()
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        FormLayout layout = new FormLayout("p, 4dlu, p", 
                                           "p, 2dlu, p, 2dlu, p, 2dlu, p");
                 
        layout.setRowGroups(new int[][] { { 1, 3, 5 } });

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        
        final JTextField dbUsrTxt    = new JTextField(30);
        final JTextField dbPwdTxt    = new JTextField(30);
        final JTextField usrText     = new JTextField(30);
        final JTextField pwdText     = new JTextField(30);
        
        final JLabel     dbUsrLbl    = createI18NFormLabel("DBUSERNAME", SwingConstants.RIGHT);
        final JLabel     dbPwdLbl    = createI18NFormLabel("DBPASSWORD", SwingConstants.RIGHT);
        final JLabel     usrLbl      = createI18NFormLabel("USERNAME", SwingConstants.RIGHT);
        final JLabel     pwdLbl      = createI18NFormLabel("PASSWORD", SwingConstants.RIGHT);
        
        CellConstraints cc = new CellConstraints(); 
        
        panel.add(dbUsrLbl, cc.xy(1, 1)); 
        panel.add(dbUsrTxt, cc.xy(3, 1)); 
        panel.add(dbPwdLbl, cc.xy(1, 3)); 
        panel.add(dbPwdTxt, cc.xy(3, 3)); 
        panel.add(usrLbl,   cc.xy(1, 5)); 
        panel.add(usrText,  cc.xy(3, 5)); 
        panel.add(pwdLbl,   cc.xy(1, 7)); 
        panel.add(pwdText,  cc.xy(3, 7)); 
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_INFO_TITLE"), true, CustomDialog.OKCANCELHELP, panel);
        dlg.setOkLabel(getResourceString("DONE"));
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        popResourceBundle();
        
        DocumentListener docListener = new DocumentListener() {
            
            public void check()
            {
                boolean enable = !dbUsrTxt.getText().isEmpty() &&
                                 !dbPwdTxt.getText().isEmpty() &&
                                 !usrText.getText().isEmpty() &&
                                 !pwdText.getText().isEmpty();
                dlg.getOkBtn().setEnabled(enable);
            }
            @Override
            public void changedUpdate(DocumentEvent e) { check(); }
            @Override
            public void insertUpdate(DocumentEvent e) { check(); }
            @Override
            public void removeUpdate(DocumentEvent e) { check(); }
        };
        
        dbUsrTxt.getDocument().addDocumentListener(docListener);
        dbPwdTxt.getDocument().addDocumentListener(docListener);
        usrText.getDocument().addDocumentListener(docListener);
        pwdText.getDocument().addDocumentListener(docListener);
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return new String[] { dbUsrTxt.getText(), dbPwdTxt.getText(), usrText.getText(), pwdText.getText()};
        }
        return null;      
    }
    
    /**
     * @param urlLoc
     * @return
     */
    @SuppressWarnings("deprecation")
    protected String getKeyFromURL(final String urlLoc)
    {
        DataInputStream dis = null;
        
        try 
        {
            URL url = new URL(urlLoc);

            URLConnection urlConn = url.openConnection(); 
            urlConn.setDoInput(true); 
            urlConn.setUseCaches(false);

            dis = new DataInputStream(urlConn.getInputStream()); 
          
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = dis.readLine()) != null) 
            { 
                sb.append(s); 
            } 
            return sb.toString();
            
        } 
        catch (MalformedURLException mue) 
        {
            mue.printStackTrace();
        } 
        catch (IOException ioe) 
        {
            ioe.printStackTrace();
            
        } catch (Exception ex) 
        {
            ex.printStackTrace(); 
            
        } finally
        {
            try
            {
                if (dis != null) dis.close();
                
            } catch (IOException ioe) 
            {
                ioe.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param username
     * @param password
     * @param encyptionKey
     * @return
     */
    public String encrypt(final String username, 
                          final String password,
                          final String encyptionKey)
    {
        return Encryption.encrypt(username+","+password, encyptionKey);
    }
    
    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static MasterPasswordMgr getInstance()
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
                return instance = (MasterPasswordMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate RecordSet factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new MasterPasswordMgr();
    }
}


