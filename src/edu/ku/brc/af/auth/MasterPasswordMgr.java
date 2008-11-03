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
    
    //private static final Logger log = Logger.getLogger(SecurityMgr.class);
    
    protected static MasterPasswordMgr instance = null;
    
    protected String  extraEncryptionKey = "Specify";
    protected boolean errCreatingFile    = false;
    
    /**
     * Protected Constructor
     */
    protected MasterPasswordMgr()
    {
        
    }
    
    /**
     * @param str
     * @return
     */
    protected String decrypt(final String str)
    {
        return Encryption.decrypt(str);
    }

    /**
     * @param str
     * @return
     */
    protected String encrypt(final String str)
    {
        return Encryption.encrypt(str);
    }

    /**
     * @return Username and Password as a pair
     */
    public Pair<String, String> getUserNamePassword()
    {
        Boolean isLocal   = AppPreferences.getLocalPrefs().getBoolean("master.islocal", null);
        String  masterKey = AppPreferences.getLocalPrefs().get("master.path", null);
        
        if (isLocal == null ||
            StringUtils.isEmpty(masterKey))
        {
            if (!askForInfo())
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
                keyStr = decrypt(masterKey);
                
            } else
            {
                keyStr = getKeyFromURL(masterKey);
                if (StringUtils.isNotEmpty(keyStr))
                {
                    keyStr = decrypt(keyStr);
                    
                } else
                {
                    return noUP;
                }
            }
            
            String[] tokens = StringUtils.split(keyStr, ",");
            if (tokens.length == 3)
            {
                extraEncryptionKey = tokens[2];
                return new Pair<String, String>(tokens[0], tokens[1]);
            }
            return noUP;

        }
        return noUP;

    }
    
    /**
     * @return
     */
    protected boolean askForInfo()
    {
        loadAndPushResourceBundle("masterusrpwd");
        
        FormLayout layout = new FormLayout("p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p, 4dlu, p", "pref, 2dlu, pref, 2dlu, pref");
                 
        layout.setRowGroups(new int[][] { { 1, 3, 5 } });

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        
        ButtonGroup group = new ButtonGroup();
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
        
        panel.add(keyLbl,       cc.xy (1, 3)); 
        panel.add(keyTxt,       cc.xyw (3, 3, 4)); 
        panel.add(createBtn,    cc.xy (7, 3)); 
        panel.add(urlLbl,       cc.xy (1, 5)); 
        panel.add(urlTxt,       cc.xyw (3, 5, 4)); 
        panel.add(fileBtn,      cc.xy (7, 5)); 
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_TITLE"), true, CustomDialog.OKCANCELHELP, panel);
        dlg.setOkLabel(getResourceString("CONT"));
        dlg.setCancelLabel(getResourceString("EXIT"));
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
                String encryptedStr = encrypt(keys[0], keys[1], keys[2]);
                if (encryptedStr != null)
                {
                    keyTxt.setText(encryptedStr);
                    dlg.getOkBtn().setEnabled(true);
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
            AppPreferences.getLocalPrefs().putBoolean("master.islocal", !isNetworkRB.isSelected());
            AppPreferences.getLocalPrefs().put("master.path", value);
            
        } else
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
                                           "p, 2dlu, p, 2dlu, p");
                 
        layout.setRowGroups(new int[][] { { 1, 3, 5 } });

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        
        final JTextField usrTxt      = new JTextField(30);
        final JTextField pwdTxt      = new JTextField(30);
        final JTextField keyTxt      = new JTextField(30);
        final JLabel     usrLbl      = createI18NFormLabel("USERNAME", SwingConstants.RIGHT);
        final JLabel     pwdLbl      = createI18NFormLabel("PASSWORD", SwingConstants.RIGHT);
        final JLabel     keyLbl      = createI18NFormLabel("ENCRYPTION_KEY", SwingConstants.RIGHT);
        
        CellConstraints cc = new CellConstraints(); 
        
        panel.add(usrLbl,       cc.xy (1, 1)); 
        panel.add(usrTxt,       cc.xy (3, 1)); 
        panel.add(pwdLbl,       cc.xy (1, 3)); 
        panel.add(pwdTxt,       cc.xy (3, 3)); 
        panel.add(keyLbl,       cc.xy (1, 5)); 
        panel.add(keyTxt,       cc.xy (3, 5)); 
        
        final CustomDialog dlg = new CustomDialog((Frame)null, getResourceString("MASTER_INFO_TITLE"), true, CustomDialog.OKCANCELHELP, panel);
        dlg.setOkLabel(getResourceString("DONE"));
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        popResourceBundle();
        
        DocumentListener docListener = new DocumentListener() {
            
            public void check()
            {
                boolean enable = !usrTxt.getText().isEmpty() &&
                                 !pwdTxt.getText().isEmpty() &&
                                 !keyTxt.getText().isEmpty();
                dlg.getOkBtn().setEnabled(enable);
            }
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                check();
            }
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                check();
            }
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                check();
            }
        };
        
        usrTxt.getDocument().addDocumentListener(docListener);
        pwdTxt.getDocument().addDocumentListener(docListener);
        keyTxt.getDocument().addDocumentListener(docListener);
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return new String[] { usrTxt.getText(), pwdTxt.getText(), keyTxt.getText()};
        }
        return null;      
    }
    
    /**
     * @param urlLoc
     * @return
     */
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
            while ((s = dis.readUTF()) != null) 
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
     * @return the encryption key used for other passwords
     */
    public String getEncryptionKey()
    {
        return extraEncryptionKey;
    }
    
    /**
     * @param username
     * @param password
     * @param extraEncyptionKey
     * @return
     */
    public String encrypt(final String username, 
                          final String password,
                          final String extraEncyptionKey)
    {
        return encrypt(username+","+password+","+extraEncyptionKey);
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


