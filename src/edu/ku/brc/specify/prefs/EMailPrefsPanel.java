/* Filename:    $RCSfile: EMailPrefsPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.specify.helpers.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.helpers.EMailHelper;
import edu.ku.brc.specify.helpers.Encryption;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.validation.*;

/**
 * Preference Panel for setting EMail Preferences.
 *  
 * This also includes a method that kicks off a dialog on a thread to check to make sure all the email settings are correct.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class EMailPrefsPanel extends JPanel implements PrefsSavable
{
    private static Log log  = LogFactory.getLog(EMailPrefsPanel.class);
    
    protected FormValidator formValidator = new FormValidator();

    Preferences prefNode = null;
    
    protected JComboBox   acctTypeSelect;
    
    protected JTextField acctName;
    protected JTextField serverName;
    protected JTextField username;
    protected JTextField password;
    protected JTextField email;
    
    protected JTextField localMailBox;
    protected JButton    browseBtn;
    protected JButton    testSettingsBtn;
    
    protected JTextField smtp;             // needed for outgoing
    protected int        timeToCheck = 10; // minutes
    
    protected Hashtable<String, JLabel> labelHash = new Hashtable<String, JLabel>();
    
    // Checker
    protected ImageIcon  checkIcon     = new ImageIcon(IconManager.getImagePath("check.gif"));  // Move to icons.xml
    protected ImageIcon  exclaimIcon   = new ImageIcon(IconManager.getImagePath("exclaim.gif"));
    protected ImageIcon  exclaimYWIcon = new ImageIcon(IconManager.getImagePath("exclaim_yellow.gif"));
    
    protected JDialog      checkerDialog = null;
    protected JLabel[]     checkerLabels;
    protected JLabel[]     checkerIcons;
    protected JButton      closeCheckerBtn;
    protected JProgressBar progressBar;
    protected JPanel       checkPanel;
    
    protected String testMessage = "Specify Test Message";
    
    
    protected EMailCheckerRunnable emailCheckerRunnable;

    
    /**
     * Constructor of the EMail setting panel
     */
    public EMailPrefsPanel()
    {
        super(new BorderLayout());
        
        Preferences userPrefNode = Preferences.userRoot();
        prefNode = userPrefNode.node("settings/email");
        if (prefNode == null)
        {
            throw new RuntimeException("Could find pref for email!");
        }
        
        createUI();

    }
    
    /**
     * Helper method for creating a field and label 
     * @param builder the jgoodies builder
     * @param labelKey the label key that uses to look up the actual string in a resource bundle
     * @param comp the component to be added
     * @param cc the CellConstraints
     * @param col the column
     * @param row the row
     * @return the updated column value (incremented by 4)
     */
    protected int addField(final PanelBuilder     builder, 
                           final String           labelKey, 
                           final JComponent       comp, 
                           final CellConstraints  cc,
                           int                    col,
                           int                    row)
    {
        JLabel label = new JLabel(getResourceString(labelKey)+":");
        labelHash.put(labelKey, label);
        builder.add(label , cc.xy(col,row));
        col += 2;
        builder.add( comp, cc.xy(col,row));
        col += 2;
        
        formValidator.addUIComp(labelKey, comp);
        formValidator.addUILabel(labelKey, label);
        
        return col;        
    }
    
    /**
     * CRate the UI for the panel
     */
    protected void createUI()
    {
        
        String rowDef = createDuplicateJGoodiesDef("p","4dlu", 14);
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("r:p, 2dlu, l:p, 5dlu, r:p, 2dlu, l:p", rowDef));
        CellConstraints cc         = new CellConstraints();
        
        int row = 1;
        int col = 1;
        
        builder.addSeparator(getResourceString("emailacctsettings"), cc.xy(col,row));
        row += 2;
        
        String notEmptyRule = "obj.isNotEmpty()";
        
        col = 1;
        acctTypeSelect = formValidator.createComboBox("accounttype", new String[] {getResourceString("pop3"), getResourceString("imap")});
        addField(builder, "accounttype", acctTypeSelect, cc, col,row);
        row += 2;
        
        addField(builder, "accountname", acctName = formValidator.createTextField("accountname", 20, true, UIValidator.ValidationType.OK, notEmptyRule), cc, col,row);
        row += 2;
        
        col = 1;
        col = addField(builder, "servername", serverName = formValidator.createTextField("servername", 20, true, UIValidator.ValidationType.OK, notEmptyRule), cc, col,row);
        col = addField(builder, "smtp", smtp = new JTextField(20), cc, col,row);
        row += 2;
        
        col = 1;
        col = addField(builder, "localmailbox", localMailBox = new JTextField(20), cc, col,row);
        builder.add(browseBtn = new JButton(getResourceString("browsedisk")), cc.xy(col,row));
        row += 2;
        
        col = 1;
        builder.addSeparator(getResourceString("userinfo"), cc.xy(col,row));
        row += 2;
        
        col = 1;
        col = addField(builder, "username",   username   = new JTextField(20), cc, col,row);
        col = addField(builder, "password",   password   = new JPasswordField(20), cc, col,row);
        //col = addField(builder, "password",   password   = new JTextField(20), cc, col,row);
        row += 2;
        
        col = 1;
        addField(builder, "email",      email  = new JTextField(20), cc, col,row);
        row += 2;
        
        col = 3;
        builder.add(testSettingsBtn = new JButton(getResourceString("testconnection")), cc.xy(col,row));
        row += 2;
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(builder.getPanel(), BorderLayout.CENTER);
        
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                browseForMailBox();
            }
        });    

        testSettingsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                startEMailSettingsTest();
            }
        });    
        
        /*acctTypeSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                updateUIState();
            }
        });*/

        formValidator.addEnableRule("localmailbox", "accounttype.getSelectedIndex() == 0");

        readDataFromPrefs();
        updateUIState();
        
        formValidator.processEnableRules();
        
        formValidator.resetFields();

    }
    
    /**
     * Set the data from the pref into the UI components
     * @param pref the pref
     * @param tf the text field
     * @param prefName the name of the pref item
     * @param defValue a default value
     * @param focus adds a focus listener for updating UI
     */
    protected void setData(final Preferences pref, 
                           final JTextField tf, 
                           final String prefName, 
                           final String defValue,
                           final FocusAdapter focus)
    {
        tf.setText(pref.get(prefName, defValue));
        if (focus != null)
        {
            tf.addFocusListener(focus);
        }
    }
    
    /**
     * Checks the that state of various UI components and enables/disables items appropriately
     */
    protected void updateUIState()
    {
        boolean localEnabled = acctTypeSelect.getSelectedItem().toString().equals(getResourceString("pop3"));
        localMailBox.setEnabled(localEnabled);
        labelHash.get("localmailbox").setEnabled(localEnabled);
        browseBtn.setEnabled(localEnabled);
        
        String usernameStr   = username.getText();
        String passwordStr   = password.getText();
        String smtpStr       = smtp.getText();
        String emailStr      = email.getText();
        String serverNameStr = serverName.getText();
        String acctTypeStr   = acctTypeSelect.getSelectedItem().toString();
        String localMailBoxStr = localMailBox.getText();
        
        testSettingsBtn.setEnabled(EMailHelper.hasEMailSettings(usernameStr, passwordStr, emailStr, smtpStr, serverNameStr, acctTypeStr, localMailBoxStr));

    }
    
    /**
     * Read the data from the pref into all the UI components 
     */
    protected void readDataFromPrefs()
    {
        FocusAdapter focus = new FocusAdapter() {
           public void  focusGained(FocusEvent e){}
           public void focusLost(FocusEvent e) { updateUIState();}
        };
        
        setData(prefNode, username,   "username", "", focus);
        setData(prefNode, email,      "email", "", focus);
        setData(prefNode, acctName,   "acctname", "", focus);
        setData(prefNode, serverName, "servername", "", focus);
        setData(prefNode, localMailBox, "localmailbox", "", focus);
        setData(prefNode, smtp,         "smtp", "", focus);
        
        acctTypeSelect.setSelectedItem(prefNode.get("accountype", EMailHelper.POP3));
        
        password.setText(Encryption.decrypt(prefNode.get("password", "")));
        password.addFocusListener(focus);
     }

    
    /**
     * Displays file dialog for user to locate their mailbox
     */
    protected void browseForMailBox()
    {
        JFileChooser chooser = new JFileChooser();
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the JDK.
        /*ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("jpg");
        filter.addExtension("gif");
        filter.setDescription("JPG & GIF Images");
        chooser.setFileFilter(filter);
        */
        int returnVal = chooser.showOpenDialog(UICacheManager.get(UICacheManager.TOPFRAME));
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            localMailBox.setText(chooser.getSelectedFile().getName());
        }
    }
    
    /**
     * Test to make the mailbox is present and we can read it and 
     * then check to see if we can download messages
     */
    protected void testSettings()
    {
        String usernameStr   = username.getText();
        String passwordStr   = password.getText();
        String smtpStr       = smtp.getText();
        String emailStr      = email.getText();
        String serverNameStr = serverName.getText();
        String acctTypeStr   = acctTypeSelect.getSelectedItem().toString();
        String localMailBoxStr = localMailBox.getText();
        EMailHelper.AccountType acctType = EMailHelper.getAccountType(acctTypeStr);

        
        boolean checkSendMail = true;
        if (checkSendMail)
        {
            String htmlMsg = "<html><body>" + testMessage + "</body></html>";
            if (!EMailHelper.sendMsg(smtpStr, usernameStr, passwordStr, emailStr, emailStr, testMessage, htmlMsg, EMailHelper.HTML_TEXT, null))
            {
                // XXX Get response error message from Helper and display it.
                //JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.TOPFRAME), "Error Sending EMail");
                checkerIcons[0].setIcon(exclaimIcon);
                checkerLabels[0].setText(EMailHelper.getLastErrorMsg());
            } else
            {
                checkerIcons[0].setIcon(checkIcon);
            }
        } else
        {
            checkerIcons[0].setIcon(checkIcon); 
        }
        
        // Open Local Box if POP  
        if (acctType == EMailHelper.AccountType.POP3)
        {
            try
            {
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props);
                
                Store store = session.getStore(new URLName("mstor:"+localMailBoxStr));
                store.connect();
        
                java.util.List<javax.mail.Message> msgList = new java.util.ArrayList<javax.mail.Message>();
                if (EMailHelper.getMessagesFromInbox(store, msgList))
                {
                    String msgStr = String.format(getResourceString("messagewerefound"), new Object[] {(msgList.size())});
                    checkerIcons[1].setIcon(checkIcon);
                    checkerLabels[1].setText(msgStr);
                    msgList.clear();
                } else
                {
                    checkerIcons[1].setIcon(exclaimIcon);
                    checkerLabels[1].setText(EMailHelper.getLastErrorMsg());
                }
                
            } catch (Exception ex)
            {
                checkerIcons[1].setIcon(exclaimIcon);
                checkerLabels[1].setText(ex.toString());
                
                JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.TOPFRAME), ex.toString());
                ex.printStackTrace();
            }
            
            
        } else if (acctType == EMailHelper.AccountType.IMAP)
        {
            checkerIcons[1].setIcon(checkIcon); 
            
        } else
        {
            throw new RuntimeException("Unknown Account Type ["+acctTypeStr+"] must be POP3 or IMAP"); // XXX FIXME
        }
        
        // Try to download message from pop account
        try
        {
            Properties props = System.getProperties();
            //props.put("mail.smtp.host", serverName.getText());
            //props.put( "mail.smtp.auth", "true");
            
            boolean foundMsg = false;
           
            if (acctType == EMailHelper.AccountType.POP3)
            {
                //props.put("mail.pop3.host", serverName);
                //props.put("mail.pop3.user", username);

                Session session = Session.getInstance(props, null);   
                Store store = session.getStore("pop3");
                store.connect(serverNameStr, usernameStr, passwordStr);
                
                java.util.List<javax.mail.Message> msgList = new java.util.ArrayList<javax.mail.Message>();
                if (EMailHelper.getMessagesFromInbox(store, msgList))
                {
                    String msgStr = String.format(getResourceString("messagewerefound"), new Object[] {(msgList.size())});
                    for (javax.mail.Message msg : msgList)
                    {
                        String subject = msg.getSubject();
                        if (subject != null && subject.indexOf(testMessage) != -1)
                        {
                            foundMsg = true;
                        }
                    }
                    checkerIcons[2].setIcon(checkIcon);
                    checkerLabels[2].setText(msgStr);
                    msgList.clear();
                    EMailHelper.closeAllMailBoxes();
                    
                } else
                {
                    checkerIcons[2].setIcon(exclaimIcon);
                    checkerLabels[2].setText(EMailHelper.getLastErrorMsg());
                }
                checkerLabels[2].invalidate();
                
            } else if (acctType == EMailHelper.AccountType.IMAP)
            {
                Session session = Session.getInstance(props, null);   
                Store store = session.getStore("imap");
                store.connect(serverNameStr, usernameStr, passwordStr);
                
                java.util.List<javax.mail.Message> msgList = new java.util.ArrayList<javax.mail.Message>();
                if (EMailHelper.getMessagesFromInbox(store, msgList))
                {
                    String msgStr = String.format(getResourceString("messagewerefound"), new Object[] {(msgList.size())});
                    for (javax.mail.Message msg : msgList)
                    {
                        String subject = msg.getSubject();
                        if (subject != null && subject.indexOf(testMessage) != -1)
                        {
                            foundMsg = true;
                        }
                    }
                    checkerIcons[2].setIcon(checkIcon);
                    checkerLabels[2].setText(msgStr);
                    msgList.clear();
                    EMailHelper.closeAllMailBoxes();

                } else
                {
                    checkerIcons[2].setIcon(exclaimIcon);
                    checkerLabels[2].setText(EMailHelper.getLastErrorMsg());
                }
                checkerLabels[2].invalidate();
                
            } else
            {
                String msgStr = "Unknown Account Type ["+acctTypeStr+"] must be POP3 or IMAP"; // XXX I18N
                checkerIcons[2].setIcon(exclaimIcon);
                checkerLabels[2].setText(msgStr);
                throw new RuntimeException(msgStr); // XXX FIXME
            }
            
            if (foundMsg)
            {
                checkerIcons[3].setIcon(checkIcon);
                checkerLabels[3].setText(getResourceString("fndtestmsg"));                    
            } else
            {
                checkerIcons[3].setIcon(exclaimIcon);
                checkerLabels[3].setText(getResourceString("nofndtestmsg"));                    
            }

        } catch (Exception ex)
        {
            checkerIcons[2].setIcon(exclaimIcon);
            checkerLabels[2].setText(ex.toString());
            
            ex.printStackTrace();
        }

        checkPanel.doLayout();
        progressBar.setVisible(false);

        checkerDialog.getContentPane().doLayout();
        checkerDialog.getContentPane().repaint();
        checkerDialog.setSize(checkerDialog.getPreferredSize());

    }
    
    //--------------------------------------------------------------------
    // PrefsSavable Interface
    //--------------------------------------------------------------------
    
    /**
     * Helper method (not part of interface) to set the values back into the pref
     * @param pref the pref object
     * @param tf the text field (source)
     * @param prefName the name of the pref field to be set
     */
    protected void putData(final Preferences pref, final JTextField tf, final String prefName)
    {
        String value = tf.getText();
        pref.put(prefName, value == null ? "" : value);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.prefs.PrefsSavable#savePrefs()
     */
    public void savePrefs()
    {
        putData(prefNode, username,   "username");
        putData(prefNode, email,      "email");
        putData(prefNode, acctName,   "acctname");
        putData(prefNode, serverName, "servername");
        putData(prefNode, localMailBox, "localmailbox");
        putData(prefNode, smtp,         "smtp");
        
        prefNode.put("accountype", acctTypeSelect.getSelectedItem().toString());
        
        prefNode.put("password", Encryption.encrypt(password.getText()));
        
    }
    
    /**
     * This creates a dialog and start a thread to check to make sure all the email settings work.
     */
    protected void startEMailSettingsTest()
    {
        
        String rowDef = createDuplicateJGoodiesDef("p","4dlu", 6);
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p, 2dlu, p", rowDef));
        CellConstraints cc         = new CellConstraints();
        
        int row = 1;
        int col = 1;
        
        builder.addSeparator(getResourceString("checkingemailsettings"), cc.xyw(col,row,3));
        row += 2;
        
        String[] labels = {getResourceString("chksendingmail"), getResourceString("chkmailbox"), 
                           getResourceString("chkgetmail"), getResourceString("chkforsentmsg")};
        checkerIcons  = new JLabel[labels.length];
        checkerLabels = new JLabel[labels.length];
        for (int i=0;i<labels.length;i++)
        {
            builder.add(checkerIcons[i] = new JLabel(exclaimYWIcon), cc.xy(col,row));
            builder.add(checkerLabels[i] = new JLabel(labels[i]), cc.xy(col+2,row));
            row += 2;
        }
        
        col = 1;
        builder.add(progressBar = new JProgressBar(0,100), cc.xyw(col,row,3));
        progressBar.setIndeterminate(true);
        row += 2;
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        JPanel panel = new JPanel(new BorderLayout());
        checkPanel = builder.getPanel();
        panel.add(builder.getPanel(), BorderLayout.CENTER);
                
        builder = new PanelBuilder(new FormLayout("c:p:g", "c:p:g"));
        closeCheckerBtn = new JButton(getResourceString("close"));
        builder.add(closeCheckerBtn, cc.xy(1,1));
        panel.add(builder.getPanel(), BorderLayout.SOUTH);
        
        closeCheckerBtn.addActionListener(new ActionListener() 
                {
            public void actionPerformed(ActionEvent e) 
            {
                checkerDialog.setVisible(false);
                emailCheckerRunnable = null;
            }
        });
        
        panel.doLayout();
        checkPanel.doLayout();
        builder.getPanel().doLayout();
        
        checkerDialog = new JDialog();
        checkerDialog.setModal(true);
        
        checkerDialog.setContentPane(panel);
        checkerDialog.pack();
        checkerDialog.doLayout();
        //checkerDialog.setPreferredSize(checkerDialog.getPreferredSize());
        checkerDialog.setSize(checkerDialog.getPreferredSize());
        
        emailCheckerRunnable = new EMailCheckerRunnable();
        emailCheckerRunnable.start();
        
        UIHelper.centerAndShow(checkerDialog);          
    }
    
    
    //----------------------------------------------------------------------------
    // Runnable to check for the email settings
    //----------------------------------------------------------------------------
    
    public class EMailCheckerRunnable implements Runnable
    {
        protected Thread               thread;

        /**
         * Constructs a an object to execute an SQL staement and then notify the listener
         * @param listener the listener
         * @param sqlStr the SQL statement to be executed.
         */
        public EMailCheckerRunnable()
        {
        }
        
        /**
         * Starts the thread to make the SQL call
         *
         */
        public void start()
        {
            thread = new Thread(this);
            thread.start();
        }
        
        /**
         * Stops the thread making the call
         *
         */
        public synchronized void stop()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
            thread = null;
            notifyAll();
        }
        
        /**
         * Test the various settings 
         */
        public void run()
        {
            try
            {
                testSettings();
                
            } catch (Exception ex)
            {
                //ex.printStackTrace();
                log.error(ex);
            }
        }
    }

    //---------------------------------------------------
    // PrefPanelIFace
    //---------------------------------------------------
    public void setOKButton(JButton okBtn)
    {
        formValidator.registerOKButton(okBtn);
    }

}
