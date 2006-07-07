/* Filename:    $RCSfile: DatabaseLoginPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/07/06 00:00:00 $
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
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.extras.SwingWorker;
import edu.ku.brc.specify.helpers.Encryption;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.ImageDisplay;
import edu.ku.brc.specify.ui.JStatusBar;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * This panel enables the user to configure all the params necessary to log into a jDBC database.
 * The login is done asynchronously and the panel is notified if it was successful or not.
 * A DatabaseLoginListener can be registered to be notified of a successful login or when the cancel button is pressed.
 *
 *  NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by the user.
 *
 * @author rods
 *
 */
public class DatabaseLoginPanel extends JPanel
{
    private static final Logger log  = Logger.getLogger(DatabaseLoginPanel.class);

    // Form Stuff

    protected JTextField       username;
    protected JPasswordField   password;

    protected JEditComboBox    databases;
    protected JEditComboBox    servers;

    protected JCheckBox        rememberUsernameCBX;
    protected JCheckBox        autoLoginCBX;

    protected JButton          cancelBtn;
    protected JButton          loginBtn;

    protected JStatusBar       statusBar;

    protected JDialog          thisDlg;
    protected boolean          isCancelled = true;

    protected DatabaseLoginListener dbListener;


    //private String[]    serverNameStrs     = {
    //        "jdbc:mysql://localhost/",
    //        "jdbc:inetdae7://129.237.201.110/",
    //        "jdbc:mysql://129.237.201.110/",
    //        "jdbc:microsoft:sqlserver://129.237.201.110/" };                   // "jdbc:inetdae7://129.237.201.110/"};
//
    private String      serverName    = "";
    private String      databaseName  = "";
    private String      usernameStr   = "";
    private String      passwordStr   = "";
    private String      driverName    = "com.mysql.jdbc.Driver"; // XXX HARD CODED VALUE!

    private Preferences prefNode;

    /**
     * Constructor that has the form created from the view system
     */
    public DatabaseLoginPanel(final DatabaseLoginListener dbListener)
    {
        this.dbListener = dbListener;

        Preferences appsNode = UICacheManager.getAppPrefs();
        prefNode = appsNode.node("login");

        createUI();

    }

    /**
     * @return the login btn
     */
    public JButton getLoginBtn()
    {
        return loginBtn;
    }

    /**
     * @param label
     * @param comp
     * @param pb
     * @param cc
     * @param y
     * @return
     */
    protected int addLine(final String label, final JComponent comp, final PanelBuilder pb, final CellConstraints cc, int y)
    {
        pb.add(new JLabel(label != null ? getResourceString(label)+":" : " ", JLabel.RIGHT), cc.xy(1, y));
        pb.add(comp, cc.xy(3, y));
        y += 2;
        return y;
    }

    /**
     * Creates the proper form from the view system
     * @param viewSetName the viewset name
     * @param viewName the view's name
     * @param title the title of the dialog
     */
    protected void createUI()
    {

        PropertiesPickListAdapter dbPickList = new PropertiesPickListAdapter(prefNode, "databases");
        PropertiesPickListAdapter svPickList = new PropertiesPickListAdapter(prefNode, "servers");

        class MyFocusAdapter extends FocusAdapter
        {
            public void focusLost(FocusEvent e)
            {
                updateUIControls();
            }
        }
        username  = new JTextField(20);
        password  = new JPasswordField(20);

        username.addFocusListener(new MyFocusAdapter());
        password.addFocusListener(new MyFocusAdapter());

        databases = new JEditComboBox(dbPickList);
        servers   = new JEditComboBox(svPickList);
        dbPickList.setComboBox(databases);
        svPickList.setComboBox(servers);

        autoLoginCBX        = new JCheckBox(getResourceString("autologin"));
        rememberUsernameCBX = new JCheckBox(getResourceString("rememberuser"));

        statusBar = new JStatusBar();

        cancelBtn = new JButton(getResourceString("cancel"));
        loginBtn  = new JButton(getResourceString("login"));

        autoLoginCBX.setSelected(prefNode.getBoolean("autologin", false));
        rememberUsernameCBX.setSelected(prefNode.getBoolean("rememberuser", false));

        if (autoLoginCBX.isSelected())
        {
            username.setText(prefNode.get("username", ""));
            password.setText(Encryption.decrypt(prefNode.get("password", "")));
            username.requestFocus();

        } else if (rememberUsernameCBX.isSelected())
        {
            username.setText(prefNode.get("username", ""));
            password.requestFocus();

            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    password.requestFocus();
                }
          });

        }

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (dbListener != null)
                {
                    dbListener.cancelled();
                }
             }
         });

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doLogin();
            }
         });


        // Ask the PropertiesPickListAdapter to set the index from the prefs
        dbPickList.setSelectedIndex();
        svPickList.setSelectedIndex();

        servers.getTextField().addKeyListener(new KeyAdapter()
                {
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });

        databases.getTextField().addKeyListener(new KeyAdapter()
                {
            public void keyReleased(KeyEvent e)
            {
                updateUIControls();
            }
        });



        PanelBuilder formBuilder = new PanelBuilder(new FormLayout("p,3dlu,max(220px;p)", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p"));
        CellConstraints cc = new CellConstraints();
        formBuilder.addSeparator(getResourceString("logintitle"), cc.xywh(1,1,3,1));

        int y = 3;
        y = addLine("username",  username, formBuilder, cc, y);
        y = addLine("password",  password, formBuilder, cc, y);
        y = addLine("databases", databases, formBuilder, cc, y);
        y = addLine("servers",   servers, formBuilder, cc, y);
        y = addLine(null,        rememberUsernameCBX, formBuilder, cc, y);
        y = addLine(null,        autoLoginCBX, formBuilder, cc, y);

        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p,3dlu,p", "p,2dlu,p,2dlu,p"), this);
        ImageDisplay icon       = new ImageDisplay(IconManager.getImage("SpecifyLargeIcon"), false, false);

        formBuilder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        outerPanel.add(icon, cc.xy(1, 1));
        outerPanel.add(formBuilder.getPanel(), cc.xy(3, 1));
        outerPanel.add(ButtonBarFactory.buildOKCancelBar(loginBtn, cancelBtn), cc.xywh(1,3,3,1));
        outerPanel.add(statusBar, cc.xywh(1,5,3,1));



        updateUIControls();
    }

    /**
     * Enables or disables the UI based of the values of the controls
     */
    protected void updateUIControls()
    {
        loginBtn.setEnabled(StringUtils.isNotEmpty(username.getText()) &&
                            StringUtils.isNotEmpty(new String(password.getPassword())) &&
                            StringUtils.isNotEmpty(servers.getTextField().getText()) &&
                            StringUtils.isNotEmpty(databases.getTextField().getText()));

        rememberUsernameCBX.setEnabled(!autoLoginCBX.isSelected());
    }

    /**
     * Sets a string into the status bar
     * @param msg the msg for the status bar
     */
    public void setMessage(final String msg, final boolean isError)
    {
        if (statusBar != null)
        {
            statusBar.setText(msg);
            if (isError)
            {
                statusBar.setAsError();
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

        prefNode.putBoolean("rememberuser", rememberUsernameCBX.isSelected());
        prefNode.putBoolean("autologin", autoLoginCBX.isSelected());

        if (autoLoginCBX.isSelected())
        {
            prefNode.put("username", username.getText());
            prefNode.put("password", Encryption.encrypt(new String(password.getPassword())));

        } else if (rememberUsernameCBX.isSelected())
        {
            prefNode.put("username", username.getText());
        }
    }

    /**
     * Grabs all the values from the UI
     */
    protected void getValuesFromUI()
    {
        databaseName = databases.getTextField().getText();
        serverName   = servers.getTextField().getText();
        usernameStr  = username.getText();
        passwordStr  = new String(password.getPassword());

    }

    /**
     * Indicates the login is OK and closes the dialog for the user to conitinue on
     */
    protected void loginOK()
    {
        isCancelled = false;
        if (dbListener != null)
        {
            dbListener.loggedIn();
        }
    }


    /**
     * Performs a login on a separate thread and then notifies the dialog if it was successful.
     */
    protected void doLogin()
    {
        getValuesFromUI();

        save();

        statusBar.setIndeterminate(true);
        cancelBtn.setEnabled(false);
        loginBtn.setEnabled(false);

        setMessage(String.format(getResourceString("LoggingIn"), new Object[] {databaseName}), false);

        final SwingWorker worker = new SwingWorker()
        {
            boolean isLoggedIn = false;

            public Object construct()
            {
                isLoggedIn = UIHelper.tryLogin(driverName, serverName, databaseName, usernameStr, passwordStr);
                 return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                statusBar.setIndeterminate(false);
                cancelBtn.setEnabled(true);
                loginBtn.setEnabled(true);

                if (!isLoggedIn)
                {
                    DBConnection dbConn = DBConnection.getInstance();
                    setMessage(dbConn.getErrorMsg(), true);

                } else
                {
                    loginOK();
                }
            }
        };
        worker.start();
    }


    public String getServerName()
    {
        return serverName;
    }

    public String getDatabaseName()
    {
        return databaseName;
    }

    public String getUserName()
    {
        return usernameStr;
    }

    public String getPassword()
    {
        return passwordStr;
    }

    public String getDriverName()
    {
        return driverName;
    }

    /**
     * Returns true if doing auto login
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return prefNode.getBoolean("autologin", false);
    }

    /**
     * Return whether dialog was cancelled
     * @return whether dialog was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    //-------------------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------------------

    /*
     * This derived class of the PickListDBAdapter enables a PickList to have it's contents
     * come from a Pref with a scomma separated list of values, instead of from the database.
     * This is certainly a candidate for be pulled out and made a "full class"
     */
    class PropertiesPickListAdapter extends PickListDBAdapter
    {
        protected Preferences       prefNode;
        protected String            prefName;
        protected String            prefSelectedName;
        protected JEditComboBox     comboBox;
        protected boolean           savePickList = true;

        public PropertiesPickListAdapter(final Preferences prefNode,
                                         final String      prefName)
        {
            super();

            this.prefNode = prefNode;
            this.prefName = prefName;

            this.prefSelectedName = prefName + "_selected";

            if (savePickList)
            {
                readData();
            }
        }

        public void setComboBox(JEditComboBox comboBox)
        {
            this.comboBox = comboBox;
        }



        public void setSavePickList(boolean savePickList)
        {
            this.savePickList = savePickList;
        }

        /**
         * Builds the PickList from a prefs string
         */
        protected void readData()
        {
            String valuesStr = prefNode.get(prefName, "");
            //log.debug("["+prefName+"]["+valuesStr+"]");

            if (StringUtils.isNotEmpty(valuesStr))
            {
                String[] strs = StringUtils.split(valuesStr, ",");
                if (strs.length > 0)
                {
                    for (int i=0;i<strs.length;i++)
                    {
                        PickListItem pli = pickList.addPickListItem(new PickListItem(strs[i], strs[i], null));
                        items.add(pli);
                    }
                }
                // Always keep the list sorted
                Collections.sort(items);
            }

        }

        /**
         * Sets the proper index from the pref
         */
        public void setSelectedIndex()
        {
            String selectStr = prefNode.get(prefSelectedName, "");
            //log.debug("["+prefSelectedName+"]["+selectStr+"]");

            int selectedIndex = -1;
            int i = 0;
            for (PickListItem item : items)
            {
                if (StringUtils.isNotEmpty(selectStr) && item.getValue().equals(selectStr))
                {
                    selectedIndex = i;
                }
                i++;
            }
            comboBox.setSelectedIndex(selectedIndex);
        }

        /**
         * @param model
         * @return
         */
        protected String convertModelToStr(final PickList pickList)
        {
            StringBuilder strBuf = new StringBuilder();
            for (PickListItem item : pickList.getItems())
            {
                if (strBuf.length() > 0) strBuf.append(",");
                strBuf.append(item.getValue());
            }
            return strBuf.toString();
        }


        /* (non-Javadoc)
         * @see edu.ku.brc.specify.ui.db.PickListDBAdapter#save()
         */
        public void save()
        {
            log.debug("Saving PickList");
            if (savePickList)
            {
                prefNode.put(prefName, convertModelToStr(pickList));
                log.debug("["+prefName+"]["+convertModelToStr(pickList)+"]");
            }

            Object selectedItem = comboBox.getModel().getSelectedItem();
            if (selectedItem == null && comboBox.getTextField() != null)
            {
                selectedItem = comboBox.getTextField().getText();
            }

            if (selectedItem != null)
            {
                prefNode.put(prefSelectedName, selectedItem.toString());
                log.debug("["+prefSelectedName+"]["+selectedItem.toString()+"]");
            }
        }
    }

}
