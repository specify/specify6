/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.db.JEditComboBox;
import edu.ku.brc.ui.db.PropertiesPickListAdapter;

/**
 * This panel enables the user to configure all the params necessary to log into a JDBC database.<BR>
 * <BR>
 * The login is done asynchronously and the panel is notified if it was successful or not. A
 * CustomDBConverterListener can be registered to be notified of a successful login or when the cancel
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
public class CustomDBConverterPanel extends JPanel  implements CustomDBConverterListener
{
    private static final Logger          log            = Logger.getLogger(CustomDBConverterPanel.class);

    // Form Stuff

    protected JTextField                 usernameSource;
    protected JPasswordField             passwordSource;



    protected JCheckBox                  rememberUsernameSourceCBX;
    protected JCheckBox                  rememberPasswordSourceCBX;
    //protected JCheckBox                  autoLoginSourceCBX;

    protected JTextField                 usernameDest;
    protected JPasswordField             passwordDest;

    protected JEditComboBox              databasesDest;
    protected JEditComboBox              databasesSource;
    protected JEditComboBox              serversDest;    
    protected JEditComboBox              serversSource;

    protected JCheckBox                  rememberUsernameDestCBX;
    protected JCheckBox                  rememberPasswordDestCBX;
    protected JCheckBox                  autoLoginDestCBX;
    
    protected JButton                    cancelBtn;
    protected JButton                    loginBtn;
    protected JButton                    helpBtn;
    
    protected ImageIcon                  forwardImgIcon;
    protected ImageIcon                  downImgIcon;

    protected JStatusBar                 statusBar;

    // Extra UI
    protected JComboBox                  dbDriverCBX;
    protected JComboBox                  dbDriverCBX2;
    protected JPanel                     extraPanel;

    protected JDialog                    thisDlg;
    protected boolean                    isCancelled    = true;
    protected boolean                    isLoggingIn    = false;
    protected boolean                    isAutoClose    = false;

    protected CustomDBConverterListener  dbConverterListener;
    protected CustomDBConverterDlg       dbConverterDlg;
    protected Window                     window;

    protected Vector<DatabaseDriverInfo> dbDrivers      = new Vector<DatabaseDriverInfo>();

    // User Feedback Data Members
    protected long                       elapsedTime    = -1;
    protected long                       loginCount     = 0;
    protected long                       loginAccumTime = 0;
    protected ProgressWorker             progressWorker = null;

    protected  List<String>               dbNamesToConvert  = null;
    /**
     * Constructor that has the form created from the view system
     * @param dbConverterListener dbConverterListener to the panel (usually the frame or dialog)
     * @param isDlg whether the parent is a dialog (false mean JFrame)
     */
    public CustomDBConverterPanel(final CustomDBConverterListener dbConverterListener, final CustomDBConverterDlg dlg, final boolean isDlg)
    {
        this.dbConverterListener = dbConverterListener;
        this.dbConverterDlg = dlg;
        createUI(isDlg);
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
        pb.add(new JLabel(label != null ? getResourceString(label) + ":" : " ",
                SwingConstants.RIGHT), cc.xy(1, yy));
        pb.add(comp, cc.xy(3, yy));
        yy += 2;
        return yy;
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
                          final int x,
                          final int y)
    {
        int yy = y;
        pb.add(new JLabel(label != null ? getResourceString(label) + ":" : " ",
                SwingConstants.RIGHT), cc.xy(x, yy));
        pb.add(comp, cc.xy(x+2, yy));
        yy += 2;
        return yy;
    }
    /**
     * Creates the UI for the login and hooks up any listeners.
     * @param isDlg  whether the parent is a dialog (false mean JFrame)
     */
    protected void createUI(final boolean isDlg)
    {
        PropertiesPickListAdapter dbPickList = new PropertiesPickListAdapter("convert.databasesSource");
        PropertiesPickListAdapter svPickList = new PropertiesPickListAdapter("convert.serversSource");
        
        PropertiesPickListAdapter dbDestPickList = new PropertiesPickListAdapter("convert.databasesDest");
        PropertiesPickListAdapter svDestPickList = new PropertiesPickListAdapter("convert.serversDest");
        
        usernameSource = new JTextField(20);
        passwordSource = new JPasswordField(20);
        
        usernameDest = new JTextField(20);
        passwordDest = new JPasswordField(20);

        databasesSource = new JEditComboBox(dbPickList);
        serversSource = new JEditComboBox(svPickList);
        
        databasesDest = new JEditComboBox(dbDestPickList);
        serversDest = new JEditComboBox(svDestPickList);
        
        dbPickList.setComboBox(databasesSource);
        svPickList.setComboBox(serversSource);
        
        dbDestPickList.setComboBox(databasesDest);
        svDestPickList.setComboBox(serversDest);

       // autoLoginSourceCBX = new JCheckBox(getResourceString("autologin"));
        rememberUsernameSourceCBX = new JCheckBox(getResourceString("rememberuser"));
        rememberPasswordSourceCBX = new JCheckBox(getResourceString("rememberpassword"));
        
        autoLoginDestCBX = new JCheckBox(getResourceString("autologin"));
        rememberUsernameDestCBX = new JCheckBox(getResourceString("rememberuser"));
        rememberPasswordDestCBX = new JCheckBox(getResourceString("rememberpassword"));
        
        statusBar = new JStatusBar();
        statusBar.setErrorIcon(IconManager.getIcon("Error",IconManager.IconSize.Std16));

        cancelBtn = new JButton(getResourceString("Cancel"));
        loginBtn = new JButton(getResourceString("Login"));
        helpBtn = new JButton(getResourceString("Help"));

        forwardImgIcon = IconManager.getIcon("Forward");
        downImgIcon = IconManager.getIcon("Down");
        //moreBtn = new JCheckBox("More", forwardImgIcon); // XXX I18N

        // Extra
        dbDrivers = DatabaseDriverInfo.getDriversList();
        dbDriverCBX = new JComboBox(dbDrivers);
        
        dbDriverCBX2 = new JComboBox(dbDrivers);
        
        if (dbDrivers.size() > 0)
        {
            String selectedStr = AppPreferences.getLocalPrefs().get("convert.dbdriverSource_selected", "SQLServer");
            int inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(selectedStr, null, null));
            dbDriverCBX.setSelectedIndex(inx > -1 ? inx : -1);
             selectedStr = AppPreferences.getLocalPrefs().get("convert.dbdriverDest_selected", "SQLServer");
             inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(selectedStr, null, null));
            dbDriverCBX2.setSelectedIndex(inx > -1 ? inx : -1);

        } else
        {
            JOptionPane.showConfirmDialog(null, getResourceString("NO_DBDRIVERS"),
                    getResourceString("NO_DBDRIVERS_TITLE"), JOptionPane.CLOSED_OPTION);
            System.exit(1);
        }

        dbDriverCBX.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateUIControls();
            }
        });

        dbDriverCBX2.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateUIControls();
            }
        });
        addFocusListenerForTextComp(usernameSource);
        addFocusListenerForTextComp(passwordSource);

        addKeyListenerFor(usernameSource, !isDlg);
        addKeyListenerFor(passwordSource, !isDlg);

        addKeyListenerFor(databasesSource.getTextField(), !isDlg);
        addKeyListenerFor(serversSource.getTextField(), !isDlg);

        addFocusListenerForTextComp(usernameDest);
        addFocusListenerForTextComp(passwordDest);

        addKeyListenerFor(usernameDest, !isDlg);
        addKeyListenerFor(passwordDest, !isDlg);

        addKeyListenerFor(databasesDest.getTextField(), !isDlg);
        addKeyListenerFor(serversDest.getTextField(), !isDlg);
        
        if (!isDlg)
        {
            addKeyListenerFor(loginBtn, true);
        }

        //autoLoginSourceCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.autologin", false));
        rememberUsernameSourceCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.rememberuserSource", false));
        rememberPasswordSourceCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.rememberpasswordSource", false));

        autoLoginDestCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.autologin2", false));
        rememberUsernameDestCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.rememberuserDest", false));
        rememberPasswordDestCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean("convert.rememberpasswordDest", false));
        
//        if (autoLoginSourceCBX.isSelected())
//        {
//            usernameSource.setText(AppPreferences.getLocalPrefs().get("convert.usernameSource", ""));
//            passwordSource.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
//                    "convert.passwordSource", "")));
//            usernameSource.requestFocus();
//
//        } else
//        {
            if (rememberUsernameSourceCBX.isSelected())
            {
                usernameSource.setText(AppPreferences.getLocalPrefs().get("convert.usernameSource", ""));
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        passwordSource.requestFocus();
                    }
                });

            }

            if (rememberPasswordSourceCBX.isSelected())
            {
                passwordSource.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
                        "convert.passwordSource", "")));
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        loginBtn.requestFocus();
                    }
                });

            }
        //}

        if (autoLoginDestCBX.isSelected())
        {
            usernameDest.setText(AppPreferences.getLocalPrefs().get("convert.usernameDest", ""));
            passwordDest.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
                    "convert.passwordDest", "")));
            usernameDest.requestFocus();

        } else
        {
            if (rememberUsernameDestCBX.isSelected())
            {
                usernameDest.setText(AppPreferences.getLocalPrefs().get("convert.usernameDest", ""));
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        passwordDest.requestFocus();
                    }
                });

            }

            if (rememberPasswordDestCBX.isSelected())
            {
                passwordDest.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
                        "convert.passwordDest", "")));
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        loginBtn.requestFocus();
                    }
                });

            }
        }
        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (dbConverterListener != null)
                {
                    dbConverterListener.cancelled();
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

        //HelpManager.registerComponent(helpBtn, "login");
        HelpMgr.registerComponent(helpBtn, "convert");

//        autoLoginSourceCBX.addChangeListener(new ChangeListener()
//        {
//            public void stateChanged(ChangeEvent e)
//            {
//                if (autoLoginSourceCBX.isSelected())
//                {
//                    rememberUsernameSourceCBX.setSelected(true);
//                    rememberPasswordSourceCBX.setSelected(true);
//                }
//                updateUIControls();
//            }
//
//        });

//        moreBtn.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                if (extraPanel.isVisible())
//                {
//                    if (dbDriverCBX.getSelectedIndex() != -1)
//                    {
//                        extraPanel.setVisible(false);
//                        moreBtn.setIcon(forwardImgIcon);
//                    }
//
//                } else
//                {
//                    extraPanel.setVisible(true);
//                    moreBtn.setIcon(downImgIcon);
//                }
//                if (window != null)
//                {
//                    window.pack();
//                }
//            }
//        });

        // Ask the PropertiesPickListAdapter to set the index from the prefs
        dbPickList.setSelectedIndex();
        svPickList.setSelectedIndex();
        
        dbDestPickList.setSelectedIndex();
        svDestPickList.setSelectedIndex();

        serversSource.getTextField().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });

        databasesSource.getTextField().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });

        // Layout the form
        JPanel p = new JPanel();//FormDebugPanel();
        PanelBuilder formBuilder = new PanelBuilder(new FormLayout("p,3dlu,max(220px;p):g,3dlu,p,3dlu,max(220px;p):g", UIHelper.createDuplicateJGoodiesDef("p", "2dlu", 13)),p);
        CellConstraints cc = new CellConstraints();
        formBuilder.addSeparator(getResourceString("SOURCE_DB"), cc.xywh(1, 1, 3, 1));

        int y = 3;
        y = addLine("username", usernameSource, formBuilder, cc, y);
        y = addLine("password", passwordSource, formBuilder, cc, y);
        y = addLine("databases", databasesSource, formBuilder, cc, y);
        y = addLine("servers", serversSource, formBuilder, cc, y);
//        formBuilder.addSeparator(getResourceString("extratitle"), cc.xywh(1, y, 3, 1));
//        y +=2;
        y = addLine("driver", dbDriverCBX, formBuilder, cc, y);
        y = addLine(null, rememberUsernameSourceCBX, formBuilder, cc, y);
        y = addLine(null, rememberPasswordSourceCBX, formBuilder, cc, y);
        //y = addLine(null, autoLoginSourceCBX, formBuilder, cc, y);

        int x = 5;
        formBuilder.addSeparator(getResourceString("DEST_DB"), cc.xywh(x, 1, 3, 1));
         y = 3;
        
        y = addLine("username", usernameDest, formBuilder, cc, x, y);
        y = addLine("password", passwordDest, formBuilder, cc, x, y);
        y = addLine("databases", databasesDest, formBuilder, cc, x, y);
        y = addLine("servers", serversDest, formBuilder, cc, x, y);
//        formBuilder.addSeparator(getResourceString("extratitle"), cc.xywh(1, y, 3, 1));
//        y +=2;
        y = addLine("driver", dbDriverCBX2, formBuilder, cc, x, y);
        y = addLine(null, rememberUsernameDestCBX, formBuilder, cc, x, y);
        y = addLine(null, rememberPasswordDestCBX, formBuilder, cc, x, y);
        y = addLine(null, autoLoginDestCBX, formBuilder, cc, x, y);
        
        PanelBuilder extraPanelBlder = new PanelBuilder(new FormLayout("p,3dlu,max(220px;p):g", "p,2dlu,p,2dlu,p"));
        extraPanel = extraPanelBlder.getPanel();
        extraPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

        //formBuilder.add(moreBtn, cc.xy(1, y));
        //y += 2;

        //extraPanelBlder.addSeparator(getResourceString("extratitle"), cc.xywh(1, 1, 3, 1));
        //addLine("driver", dbDriverCBX, extraPanelBlder, cc, 3);
        //extraPanel.setVisible(false);

        //formBuilder.add(extraPanelBlder.getPanel(), cc.xywh(1, y, 3, 1));

        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p,3dlu,p:g", "p,2dlu,p,2dlu,p"), this);
        //JLabel icon = new JLabel(IconManager.getIcon("SpecifyLargeIcon"));
        //icon.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));

        formBuilder.getPanel().setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));

        //outerPanel.add(icon, cc.xy(1, 1));
        outerPanel.add(formBuilder.getPanel(), cc.xy(3, 1));
        outerPanel.add(ButtonBarFactory.buildOKCancelHelpBar(loginBtn, cancelBtn, helpBtn), cc.xywh(1, 3, 3, 1));
        outerPanel.add(statusBar, cc.xywh(1, 5, 3, 1));
        
        updateUIControls();
    }

    /**
     * Creates a focus dbConverterListener so the UI is updated when the focus leaves
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
     * Creates a Document dbConverterListener so the UI is updated when the doc changes
     * @param textField the text field to be changed
     */
    protected void addDocListenerForTextComp(final JTextComponent textField)
    {
        textField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                updateUIControls();
            }

            public void insertUpdate(DocumentEvent e)
            {
                updateUIControls();
            }

            public void removeUpdate(DocumentEvent e)
            {
                updateUIControls();
            }
        });
    }

    /**
     * Creates a Document dbConverterListener so the UI is updated when the doc changes
     * @param textField the text field to be changed
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
            public void keyPressed(KeyEvent e)
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
        //log.error("need to update this with new fields");//XXX TODO
        if (extraPanel == null || isLoggingIn)
            return; // if this is null then we should skip all the checks because nothing is created

        boolean shouldEnable = StringUtils.isNotEmpty(usernameSource.getText())
                && StringUtils.isNotEmpty(new String(passwordSource.getPassword()))
                && (serversSource.getSelectedIndex() != -1 || StringUtils.isNotEmpty(serversSource
                        .getTextField().getText())
                        && (databasesSource.getSelectedIndex() != -1 || StringUtils.isNotEmpty(databasesSource
                                .getTextField().getText())));

        if (dbDriverCBX.getSelectedIndex() == -1)
        {
            shouldEnable = false;
            setMessage(getResourceString("MISSING_DRIVER"), true);
//            if (!extraPanel.isVisible())
//            {
//                moreBtn.doClick();
//            }

        }
        if (dbDriverCBX2.getSelectedIndex() == -1)
        {
            shouldEnable = false;
            setMessage(getResourceString("MISSING_DRIVER"), true);
//            if (!extraPanel.isVisible())
//            {
//                moreBtn.doClick();
//            }

        }
        loginBtn.setEnabled(shouldEnable);

        //rememberUsernameSourceCBX.setEnabled(!autoLoginSourceCBX.isSelected());
        //rememberPasswordSourceCBX.setEnabled(!autoLoginSourceCBX.isSelected());

        if (shouldEnable)
        {
            setMessage("", false);
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
        databasesSource.getDBAdapter().save();
        serversSource.getDBAdapter().save();

        AppPreferences.getLocalPrefs().putBoolean("convert.rememberuserSource", rememberUsernameSourceCBX.isSelected());
        AppPreferences.getLocalPrefs().putBoolean("convert.rememberpasswordSource", rememberPasswordSourceCBX.isSelected());
        
        AppPreferences.getLocalPrefs().putBoolean("convert.rememberuserDest", rememberUsernameDestCBX.isSelected());
        AppPreferences.getLocalPrefs().putBoolean("convert.rememberpasswordDest", rememberPasswordDestCBX.isSelected());
        
        //AppPreferences.getLocalPrefs().putBoolean("convert.autologin", autoLoginSourceCBX.isSelected());


        
        //if (autoLoginSourceCBX.isSelected())
        //{
        //    AppPreferences.getLocalPrefs().put("convert.usernameSource", usernameSource.getText());
        //    AppPreferences.getLocalPrefs().put("convert.passwordSource", Encryption.encrypt(new String(passwordSource.getPassword())));

        //} else
        //{
            if (rememberUsernameSourceCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("convert.usernameSource", usernameSource.getText());

            } else if (AppPreferences.getLocalPrefs().exists("convert.usernameSource"))
            {
                AppPreferences.getLocalPrefs().remove("convert.usernameSource");
            }

            if (rememberPasswordSourceCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("convert.passwordSource",  Encryption.encrypt(new String(passwordSource.getPassword())));

            } else if (AppPreferences.getLocalPrefs().exists("convert.passwordSource"))
            {
                AppPreferences.getLocalPrefs().remove("convert.passwordSource");
            }
        //}
        
//        if (autoLoginDestCBX.isSelected())
//        {
//            AppPreferences.getLocalPrefs().put("convert.usernameDest", usernameDest.getText());
//            AppPreferences.getLocalPrefs().put("convert.passwordDest", Encryption.encrypt(new String(passwordDest.getPassword())));
//
//        } else
//        {
            if (rememberUsernameDestCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("convert.usernameDest", usernameDest.getText());

            } else if (AppPreferences.getLocalPrefs().exists("convert.usernameDest"))
            {
                AppPreferences.getLocalPrefs().remove("convert.usernameDest");
            }

            if (rememberPasswordSourceCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("convert.passwordDest",  Encryption.encrypt(new String(passwordDest.getPassword())));

            } else if (AppPreferences.getLocalPrefs().exists("convert.passwordDest"))
            {
                AppPreferences.getLocalPrefs().remove("convert.passwordDest");
            }
        //}
        
//        protected JEditComboBox              databasesDest;
//        protected JEditComboBox              databasesSource;
//        protected JEditComboBox              serversDest;    
//        protected JEditComboBox              serversSource;

       // AppPreferences.getLocalPrefs().put("convert.dbdriverSource_selected", dbDrivers.get(dbDriverCBX.getSelectedIndex()).getName());
       // AppPreferences.getLocalPrefs().put("convert.dbdriverDest_selected", dbDrivers.get(dbDriverCBX2.getSelectedIndex()).getName());
        
        //databasesSource.getSelectedIndex()
        //AppPreferences.getLocalPrefs().put("convert.databasesSource_selected", databasesDest.getTextField().getText());
        //AppPreferences.getLocalPrefs().put("convert.databasesDest_selected", databasesSource.getTextField().getText());
    }

    /**
     * Indicates the login is OK and closes the dialog for the user to conitinue on
     */
    protected void loginOK()
    {
        isCancelled = false;
        if (dbConverterListener != null)
        {
            dbConverterListener.loggedIn(getSourceDatabaseName(), getSourceUserName());
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

        usernameSource.setEnabled(enable);
        passwordSource.setEnabled(enable);
        databasesSource.setEnabled(enable);
        serversSource.setEnabled(enable);
        rememberUsernameSourceCBX.setEnabled(enable);
        rememberPasswordSourceCBX.setEnabled(enable);
        
        usernameDest.setEnabled(enable);
        passwordDest.setEnabled(enable);
        databasesDest.setEnabled(enable);
        serversDest.setEnabled(enable);
        rememberUsernameDestCBX.setEnabled(enable);
        rememberPasswordDestCBX.setEnabled(enable);
        
        //autoLoginSourceCBX.setEnabled(enable);
        //moreBtn.setEnabled(enable);
    }

    /**
     * Performs a login on a separate thread and then notifies the dialog if it was successful.
     */
    public void doLogin()
    {
        log.debug("do nothing for now");
        save();
        isLoggingIn = true;

        save();

        //statusBar.setIndeterminate(true);
        enableUI(false);
        //this.setVisible(false);
        this.getDbConverterDlg().setVisible(false);
        //this.getRootPane().disable();
        //this.di
        //this.do

       // setMessage(String
        //        .format(getResourceString("LoggingIn"), new Object[] { getDatabaseName() }), false);

        //String basePrefName = getDatabaseName() + "." + getUserName() + ".";

//        loginCount = AppPreferences.getLocalPrefs().getLong(basePrefName + "logincount", -1L);
//        loginAccumTime = AppPreferences.getLocalPrefs().getLong(basePrefName + "loginaccumtime",
//                -1L);
//
//        if (loginCount != -1 && loginAccumTime != -1)
//        {
//            int timesPerSecond = 4;
//            progressWorker = new ProgressWorker(statusBar.getProgressBar(), 0,
//                    (int) (((double) loginAccumTime / (double) loginCount) + 0.5), timesPerSecond);
//            new Timer(1000 / timesPerSecond, progressWorker).start();
//
//        } else
//        {
//            loginCount = 0;
//        }
//
//        final SwingWorker worker = new SwingWorker()
//        {
//            boolean isLoggedIn = false;
//            long    eTime;
//            boolean timeOK     = false;
//
//            @SuppressWarnings("synthetic-access")
//            @Override
//            public Object construct()
//            {
//                eTime = System.currentTimeMillis();
//
//                isLoggedIn = UIHelper.tryLogin(getDriverClassName(), 
//                                               getDialectClassName(),
//                                               getDatabaseName(), 
//                                               getConnectionStr(), 
//                                               getUserName(), 
//                                               getPassword());
//
//                if (isLoggedIn)
//                {
//                    DatabaseDriverInfo drvInfo = dbDrivers.get(dbDriverCBX.getSelectedIndex());
//                    if (drvInfo != null)
//                    {
//                        DBConnection.getInstance().setDbCloseConnectionStr(drvInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Close, getServerName(), getDatabaseName()));
//                    }
//                    
//                    // Note: this doesn't happen on the GUI thread
//                    DataProviderFactory.getInstance().shutdown();
//
//                    // This restarts the System
//                    try
//                    {
//                        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//                        session.close();
//
//                    } catch (Exception ex)
//                    {
//                        log.warn(ex);
//                        finished();
//                    }
//                }
//
//                return null;
//            }
//
//            // Runs on the event-dispatching thread.
//            @Override
//            public void finished()
//            {
//
//                // I am not sure this is the rightplace for this
//                // but this is where I am putting it for now
//                if (isLoggedIn)
//                {
//                    setMessage(getResourceString("LoadingSchema"), false);
//                    statusBar.repaint();
//
//                    // Note: this doesn't happen on the GUI thread
//                    DataProviderFactory.getInstance().shutdown();
//
//                    // This restarts the System
//                    DataProviderSessionIFace session = DataProviderFactory.getInstance()
//                            .createSession();
//                    session.close();
//                }
//
//                long endTime = System.currentTimeMillis();
//                eTime = (endTime - eTime) / 1000;
//                timeOK = true;
//
//                if (progressWorker != null)
//                {
//                    progressWorker.stop();
//                }
//
//                isLoggingIn = false;
//                statusBar.setIndeterminate(false);
//
//                enableUI(true);
//
//                if (isAutoClose)
//                {
//                    updateUIControls();
//                }
//
//                if (timeOK)
//                {
//                    elapsedTime = eTime;
//                    loginAccumTime += elapsedTime;
//
//                    if (loginCount < 1000)
//                    {
//                        String basePrefNameStr = getDatabaseName() + "." + getUserName() + ".";
//                        AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "logincount",
//                                ++loginCount);
//                        AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "loginaccumtime",
//                                loginAccumTime);
//                    }
//                }
//
//                if (!isLoggedIn)
//                {
//                    setMessage(DBConnection.getInstance().getErrorMsg(), true);
//
//                } else
//                {
//                    loginOK();
//                }
//            }
//        };
//        worker.start();
    }

    /**
     * @return the server name
     */
    public String getSourceServerName()
    {
        return serversSource.getTextField().getText();
    }
    /**
     * @return the server name
     */
    public String getDestServerName()
    {
        return serversDest.getTextField().getText();
    }
    /**
     * @return the database name
     */
    public String getSourceDatabaseName()
    {
        return databasesSource.getTextField().getText();
    }

    /**
     * @return the database name
     */
    public String getDestDatabaseName()
    {
        return databasesDest.getTextField().getText();
    }
    /**
     * 
     * @return the usernameSource
     */
    public String getSourceUserName()
    {
        return usernameSource.getText();
    }
    /**
     * 
     * @return the usernameDest
     */
    public String getDestUserName()
    {
        return usernameDest.getText();
    }
    /**
     * @return the passwordSource string
     */
    public String getSourcePassword()
    {
        return new String(passwordSource.getPassword());
    }
    /**
     * @return the passwordSource string
     */
    public String getDestPassword()
    {
        return new String(passwordDest.getPassword());
    }
    /**
     * @return the formatted connection string
     */
    public String getSourceConnectionStr()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get(dbDriverCBX.getSelectedIndex()).getConnectionStr(
                                    DatabaseDriverInfo.ConnectionType.Open, 
                                    getSourceServerName(), 
                                    getSourceDatabaseName(), 
                                    this.getSourceUserName(),
                                    this.getSourcePassword(),
                                    this.getSourceDriverType());
        }
        return null; // we should never get here
    }

    /**
     * @return the formatted connection string
     */
    public String getDestConnectionStr()
    {
        if (dbDriverCBX2.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get(dbDriverCBX2.getSelectedIndex()).getConnectionStr(
                                    DatabaseDriverInfo.ConnectionType.Open, 
                                    getDestServerName(), 
                                    getDestDatabaseName(), 
                                    this.getDestUserName(),
                                    this.getDestPassword(),
                                    this.getDestDriverType());
        }
        // else
        return null; // we should never get here
    }
    /**
     * @return dialect clas name
     */
    public String getSourceDialectClassName()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX.getSelectedIndex()).getDialectClassName();
        }
        // else
        return null; // we should never get here
    }

    /**
     * @return dialect clas name
     */
    public String getDestDialectClassName()
    {
        if (dbDriverCBX2.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX2.getSelectedIndex()).getDialectClassName();
        }
        // else
        return null; // we should never get here
    }    
    /**
     * @return the driver class name
     */
    public String getSourceDriverClassName()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX.getSelectedIndex()).getDriverClassName();
        }
        // else
        return null; // we should never get here
    }
    /**
     * @return the driver class name
     */
    public String getDestDriverClassName()
    {
        if (dbDriverCBX2.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX2.getSelectedIndex()).getDriverClassName();
        }
        // else
        return null; // we should never get here
    }  
    
    public String getSourceDriverType()
    {
        if (dbDriverCBX.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX.getSelectedIndex()).getName();
        }       
        return null; // we should never get here      
    }
    
    public String getDestDriverType()
    {
        if (dbDriverCBX2.getSelectedIndex() > -1) 
        { 
            return dbDrivers.get( dbDriverCBX2.getSelectedIndex()).getName();
        }
        return null; // we should never get here      
    }
    
    /**
     * Returns true if doing auto login
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return AppPreferences.getLocalPrefs().getBoolean("autologin", false);
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
    
    public void loggedIn(final String databaseNameArg, final String userNameArg)
    {
    
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        
    }
    
    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    @SuppressWarnings("unchecked")
    public List<String> getSelectedObjects()
    {
        List<String> selectedItems = new ArrayList<String>(5);
        //for (Object obj : list.getSelectedValues())
        //{
            selectedItems.add(getSourceDatabaseName().toLowerCase());
            selectedItems.add(getDestDatabaseName().toLowerCase());
       // }
        return selectedItems;
    }

    /**
     * @return the dbConverterDlg
     */
    public CustomDBConverterDlg getDbConverterDlg()
    {
        return dbConverterDlg;
    }

    /**
     * @param dbConverterDlg the dbConverterDlg to set
     */
    public void setDbConverterDlg(CustomDBConverterDlg dbConverterDlg)
    {
        this.dbConverterDlg = dbConverterDlg;
    }
}
