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
package edu.ku.brc.ui.db;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
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
import edu.ku.brc.specify.help.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;

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
public class DatabaseLoginPanel extends JPanel
{
    private static final Logger          log            = Logger.getLogger(DatabaseLoginPanel.class);

    // Form Stuff

    protected JTextField                 username;
    protected JPasswordField             password;

    protected JEditComboBox              databases;
    protected JEditComboBox              servers;

    protected JCheckBox                  rememberUsernameCBX;
    protected JCheckBox                  rememberPasswordCBX;
    protected JCheckBox                  autoLoginCBX;

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

    protected DatabaseLoginListener      dbListener;
    protected Window                     window;

    protected Vector<DatabaseDriverInfo> dbDrivers      = new Vector<DatabaseDriverInfo>();

    // User Feedback Data Members
    protected long                       elapsedTime    = -1;
    protected long                       loginCount     = 0;
    protected long                       loginAccumTime = 0;
    protected ProgressWorker             progressWorker = null;

    /**
     * Constructor that has the form created from the view system
     * 
     * @param dbListener
     *            listener to the panel (usually the frame or dialog)
     * @param isDlg
     *            whether the parent is a dialog (false mean JFrame)
     */
    public DatabaseLoginPanel(final DatabaseLoginListener dbListener, final boolean isDlg)
    {
        this.dbListener = dbListener;

        createUI(isDlg);

    }

    /**
     * Sets a window to be resized for extra options
     * 
     * @param window
     *            the window
     */
    public void setWindow(Window window)
    {
        this.window = window;
    }

    /**
     * Returns the owning window.
     * 
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
     * 
     * @return the statusbar.
     */
    public JStatusBar getStatusBar()
    {
        return statusBar;
    }

    /**
     * Creates a line in the form.
     * 
     * @param label
     *            JLabel text
     * @param comp
     *            the component to be added
     * @param pb
     *            the PanelBuilder to use
     * @param cc
     *            the CellConstratins to use
     * @param y
     *            the 'y' coordinate in the layout of the form
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
     * Creates the UI for the login and hooks up any listeners.
     * 
     * @param isDlg
     *            whether the parent is a dialog (false mean JFrame)
     */
    protected void createUI(final boolean isDlg)
    {

        // First create the controls and hook up listeners

        PropertiesPickListAdapter dbPickList = new PropertiesPickListAdapter("login.databases");
        PropertiesPickListAdapter svPickList = new PropertiesPickListAdapter("login.servers");

        username = new JTextField(20);
        password = new JPasswordField(20);

        databases = new JEditComboBox(dbPickList);
        servers = new JEditComboBox(svPickList);
        dbPickList.setComboBox(databases);
        svPickList.setComboBox(servers);

        autoLoginCBX = new JCheckBox(getResourceString("autologin"));
        rememberUsernameCBX = new JCheckBox(getResourceString("rememberuser"));
        rememberPasswordCBX = new JCheckBox(getResourceString("rememberpassword"));

        statusBar = new JStatusBar();
        statusBar.setErrorIcon(IconManager.getIcon("Error",IconManager.IconSize.Std16));

        cancelBtn = new JButton(getResourceString("Cancel"));
        loginBtn = new JButton(getResourceString("Login"));
        helpBtn = new JButton(getResourceString("Help"));

        forwardImgIcon = IconManager.getIcon("Forward");
        downImgIcon = IconManager.getIcon("Down");
        moreBtn = new JCheckBox("More", forwardImgIcon); // XXX I18N

        // Extra
        dbDrivers = DatabaseDriverInfo.getDriversList();
        dbDriverCBX = new JComboBox(dbDrivers);
        if (dbDrivers.size() > 0)
        {
            String selectedStr = AppPreferences.getLocalPrefs().get("login.dbdriver_selected", "MySQL");
            int inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(selectedStr, null, null));
            dbDriverCBX.setSelectedIndex(inx > -1 ? inx : -1);

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

        autoLoginCBX.setSelected(AppPreferences.getLocalPrefs()
                .getBoolean("login.autologin", false));
        rememberUsernameCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean(
                "login.rememberuser", false));
        rememberPasswordCBX.setSelected(AppPreferences.getLocalPrefs().getBoolean(
                "login.rememberpassword", false));

        if (autoLoginCBX.isSelected())
        {
            username.setText(AppPreferences.getLocalPrefs().get("login.username", ""));
            password.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
                    "login.password", "")));
            username.requestFocus();

        } else
        {
            if (rememberUsernameCBX.isSelected())
            {
                username.setText(AppPreferences.getLocalPrefs().get("login.username", ""));
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        password.requestFocus();
                    }
                });

            }

            if (rememberPasswordCBX.isSelected())
            {
                password.setText(Encryption.decrypt(AppPreferences.getLocalPrefs().get(
                        "login.password", "")));
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

        //HelpManager.registerComponent(helpBtn, "login");
        HelpMgr.registerComponent(helpBtn, "login");

        autoLoginCBX.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if (autoLoginCBX.isSelected())
                {
                    rememberUsernameCBX.setSelected(true);
                    rememberPasswordCBX.setSelected(true);
                }
                updateUIControls();
            }

        });

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

        // Layout the form

        PanelBuilder formBuilder = new PanelBuilder(new FormLayout("p,3dlu,max(220px;p)", UIHelper
                .createDuplicateJGoodiesDef("p", "2dlu", 11)));
        CellConstraints cc = new CellConstraints();
        formBuilder.addSeparator(getResourceString("logintitle"), cc.xywh(1, 1, 3, 1));

        int y = 3;
        y = addLine("username", username, formBuilder, cc, y);
        y = addLine("password", password, formBuilder, cc, y);
        y = addLine("databases", databases, formBuilder, cc, y);
        y = addLine("servers", servers, formBuilder, cc, y);
        y = addLine(null, rememberUsernameCBX, formBuilder, cc, y);
        y = addLine(null, rememberPasswordCBX, formBuilder, cc, y);
        y = addLine(null, autoLoginCBX, formBuilder, cc, y);

        PanelBuilder extraPanelBlder = new PanelBuilder(new FormLayout("p,3dlu,max(220px;p)",
                "p,2dlu,p,2dlu,p"));
        extraPanel = extraPanelBlder.getPanel();
        extraPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 2));

        formBuilder.add(moreBtn, cc.xy(1, y));
        y += 2;

        extraPanelBlder.addSeparator(getResourceString("extratitle"), cc.xywh(1, 1, 3, 1));
        addLine("driver", dbDriverCBX, extraPanelBlder, cc, 3);
        extraPanel.setVisible(false);

        formBuilder.add(extraPanelBlder.getPanel(), cc.xywh(1, y, 3, 1));

        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p,3dlu,p", "p,2dlu,p,2dlu,p"),
                this);
        ImageDisplay icon = new ImageDisplay(IconManager.getIcon("SpecifyLargeIcon"), false, false);

        formBuilder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        outerPanel.add(icon, cc.xy(1, 1));
        outerPanel.add(formBuilder.getPanel(), cc.xy(3, 1));
        outerPanel.add(ButtonBarFactory.buildOKCancelHelpBar(loginBtn, cancelBtn, helpBtn), cc
                .xywh(1, 3, 3, 1));
        outerPanel.add(statusBar, cc.xywh(1, 5, 3, 1));

        updateUIControls();
    }

    /**
     * Creates a focus listener so the UI is updated when the focus leaves
     * 
     * @param textField
     *            the text field to be changed
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
     * 
     * @param textField
     *            the text field to be changed
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
     * Creates a Document listener so the UI is updated when the doc changes
     * 
     * @param textField
     *            the text field to be changed
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
        if (extraPanel == null || isLoggingIn)
            return; // if this is null then we should skip all the checks because nothing is created

        boolean shouldEnable = StringUtils.isNotEmpty(username.getText())
                && StringUtils.isNotEmpty(new String(password.getPassword()))
                && (servers.getSelectedIndex() != -1 || StringUtils.isNotEmpty(servers
                        .getTextField().getText())
                        && (databases.getSelectedIndex() != -1 || StringUtils.isNotEmpty(databases
                                .getTextField().getText())));

        if (dbDriverCBX.getSelectedIndex() == -1)
        {
            shouldEnable = false;
            setMessage(getResourceString("MISSING_DRIVER"), true);
            if (!extraPanel.isVisible())
            {
                moreBtn.doClick();
            }

        }

        loginBtn.setEnabled(shouldEnable);

        rememberUsernameCBX.setEnabled(!autoLoginCBX.isSelected());
        rememberPasswordCBX.setEnabled(!autoLoginCBX.isSelected());

        if (shouldEnable)
        {
            setMessage("", false);
        }
    }

    /**
     * Sets a string into the status bar
     * 
     * @param msg
     *            the msg for the status bar
     * @param isError
     *            whether the text should be shown in the error color
     */
    public void setMessage(final String msg, final boolean isError)
    {
        if (statusBar != null)
        {
            if (isError)
            {
                statusBar.setErrorMessage(msg,null);
            }
            else
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
        databases.getDBAdapter().save();
        servers.getDBAdapter().save();

        AppPreferences.getLocalPrefs().putBoolean("login.rememberuser",
                rememberUsernameCBX.isSelected());
        AppPreferences.getLocalPrefs().putBoolean("login.rememberpassword",
                rememberPasswordCBX.isSelected());
        AppPreferences.getLocalPrefs().putBoolean("login.autologin", autoLoginCBX.isSelected());

        if (autoLoginCBX.isSelected())
        {
            AppPreferences.getLocalPrefs().put("login.username", username.getText());
            AppPreferences.getLocalPrefs().put("login.password",
                    Encryption.encrypt(new String(password.getPassword())));

        } else
        {
            if (rememberUsernameCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("login.username", username.getText());

            } else if (AppPreferences.getLocalPrefs().exists("login.username"))
            {
                AppPreferences.getLocalPrefs().remove("login.username");
            }

            if (rememberPasswordCBX.isSelected())
            {
                AppPreferences.getLocalPrefs().put("login.password",
                        Encryption.encrypt(new String(password.getPassword())));

            } else if (AppPreferences.getLocalPrefs().exists("login.password"))
            {
                AppPreferences.getLocalPrefs().remove("login.password");
            }
        }
        AppPreferences.getLocalPrefs().put("login.dbdriver_selected",
                dbDrivers.get(dbDriverCBX.getSelectedIndex()).getName());

    }

    /**
     * Indicates the login is OK and closes the dialog for the user to conitinue on
     */
    protected void loginOK()
    {
        isCancelled = false;
        if (dbListener != null)
        {
            dbListener.loggedIn(getDatabaseName(), getUserName());
        }
    }

    /**
     * Tells it whether the parent (frame or dialog) should be auto closed when it is logged in
     * successfully.
     * 
     * @param isAutoClose
     *            true / false
     */
    public void setAutoClose(boolean isAutoClose)
    {
        this.isAutoClose = isAutoClose;
    }

    /**
     * Helper to enable all the UI components.
     * 
     * @param enable
     *            true or false
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
        rememberPasswordCBX.setEnabled(enable);
        autoLoginCBX.setEnabled(enable);
        moreBtn.setEnabled(enable);
    }

    /**
     * Performs a login on a separate thread and then notifies the dialog if it was successful.
     */
    public void doLogin()
    {
        isLoggingIn = true;

        save();

        statusBar.setIndeterminate(true);
        enableUI(false);

        setMessage(String
                .format(getResourceString("LoggingIn"), new Object[] { getDatabaseName() }), false);

        String basePrefName = getDatabaseName() + "." + getUserName() + ".";

        loginCount = AppPreferences.getLocalPrefs().getLong(basePrefName + "logincount", -1L);
        loginAccumTime = AppPreferences.getLocalPrefs().getLong(basePrefName + "loginaccumtime",
                -1L);

        if (loginCount != -1 && loginAccumTime != -1)
        {
            int timesPerSecond = 4;
            progressWorker = new ProgressWorker(statusBar.getProgressBar(), 0,
                    (int) (((double) loginAccumTime / (double) loginCount) + 0.5), timesPerSecond);
            new Timer(1000 / timesPerSecond, progressWorker).start();

        } else
        {
            loginCount = 0;
        }

        final SwingWorker worker = new SwingWorker()
        {
            boolean isLoggedIn = false;
            long    eTime;
            boolean timeOK     = false;

            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                eTime = System.currentTimeMillis();

                isLoggedIn = UIHelper.tryLogin(getDriverClassName(), 
                                               getDialectClassName(),
                                               getDatabaseName(), 
                                               getConnectionStr(), 
                                               getUserName(), 
                                               getPassword());

                if (isLoggedIn)
                {
                    DatabaseDriverInfo drvInfo = dbDrivers.get(dbDriverCBX.getSelectedIndex());
                    if (drvInfo != null)
                    {
                        DBConnection.getInstance().setDbCloseConnectionStr(drvInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Close, getServerName(), getDatabaseName()));
                    }
                    
                    // Note: this doesn't happen on the GUI thread
                    DataProviderFactory.getInstance().shutdown();

                    // This restarts the System
                    try
                    {
                        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                        session.close();

                    } catch (Exception ex)
                    {
                        log.warn(ex);
                        finished();
                    }
                }

                return null;
            }

            // Runs on the event-dispatching thread.
            @Override
            public void finished()
            {

                // I am not sure this is the rightplace for this
                // but this is where I am putting it for now
                if (isLoggedIn)
                {
                    setMessage(getResourceString("LoadingSchema"), false);
                    statusBar.repaint();

                    // Note: this doesn't happen on the GUI thread
                    DataProviderFactory.getInstance().shutdown();

                    // This restarts the System
                    DataProviderSessionIFace session = DataProviderFactory.getInstance()
                            .createSession();
                    session.close();
                }

                long endTime = System.currentTimeMillis();
                eTime = (endTime - eTime) / 1000;
                timeOK = true;

                if (progressWorker != null)
                {
                    progressWorker.stop();
                }

                isLoggingIn = false;
                statusBar.setIndeterminate(false);

                enableUI(true);

                if (isAutoClose)
                {
                    updateUIControls();
                }

                if (timeOK)
                {
                    elapsedTime = eTime;
                    loginAccumTime += elapsedTime;

                    if (loginCount < 1000)
                    {
                        String basePrefNameStr = getDatabaseName() + "." + getUserName() + ".";
                        AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "logincount",
                                ++loginCount);
                        AppPreferences.getLocalPrefs().putLong(basePrefNameStr + "loginaccumtime",
                                loginAccumTime);
                    }
                }

                if (!isLoggedIn)
                {
                    setMessage(DBConnection.getInstance().getErrorMsg(), true);

                } else
                {
                    loginOK();
                }
            }
        };
        worker.start();
    }

    /**
     * @return the server name
     */
    public String getServerName()
    {
        return servers.getTextField().getText();
    }

    /**
     * @return the database name
     */
    public String getDatabaseName()
    {
        return databases.getTextField().getText();
    }

    /**
     * 
     * @return the username
     */
    public String getUserName()
    {
        return username.getText();
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
     * 
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return AppPreferences.getLocalPrefs().getBoolean("autologin", false);
    }

    /**
     * Return whether dialog was cancelled
     * 
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
}
