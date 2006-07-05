/* Filename:    $RCSfile: DatabaseLogin.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.extras.SwingWorker;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.ui.JStatusBar;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.forms.persist.ViewSet;
import edu.ku.brc.specify.ui.validation.ValComboBox;
import edu.ku.brc.specify.ui.validation.ValPasswordField;
import edu.ku.brc.specify.ui.validation.ValTextField;

/**
 * This is a rather complicated dialog that is created from the View System. Which means although this may be
 * the first thing displayed it still uses a lot of the UI engine to get it displayed. The up side is the user
 * is presented with a dialog that looks and behaves just like the rest of the app. The down side it requires 
 * a lot of stuff to happen before it can be displayed.
 * 
 *  NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by the user.
 *  
 *  This dialog is overly complicated because it interacts with Specify's platform to make sure there is a set
 *  of "forms" or views that the user can use to actually login to Specify.
 *  
 * @author rods
 *
 */
public class DatabaseLogin extends JDialog
{
    private static final Logger log  = Logger.getLogger(DatabaseLogin.class);
    
    // Form Stuff
    protected MultiView        multiView;
    protected View             formView;
    protected Viewable         form;
    protected List<String>     fieldNames;

    protected ValTextField     username;
    protected ValPasswordField password;
    
    protected ValComboBox      databases;
    protected ValComboBox      servers;
    protected ValComboBox      viewSets;
    
    protected JCheckBox        rememberUsernameCBX;
    protected JCheckBox        autoLoginCBX;
    
    protected JButton          cancelBtn;
    protected JButton          loginBtn;
    
    protected JStatusBar       statusBar;

    protected JDialog          thisDlg;
    protected boolean          isCancelled = true;
    

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
    
    private String      usernameCache   = "";
    private String      databaseCache   = "";
    
    private PropertiesPickListAdapter viewSetAdapter;
    
    private Preferences prefNode;

    /**
     * Constructor that has the form created from the view system
     */
    public DatabaseLogin()
    {
        thisDlg = this;
        
        Preferences appsNode = UICacheManager.getAppPrefs();
        prefNode = appsNode.node("login");
        
        createUI("SystemSetup", "DatabaseLogin", "Specify Login");

        cancelBtn = (JButton)form.getCompById("cancel");
        loginBtn = (JButton)form.getCompById("login");
        
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                thisDlg.dispose();
             }
         });
        
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doLogin();
            }
         });
        
        getRootPane().setDefaultButton(loginBtn);
        
        pack();
    }
    
    /**
     * Creates the proper form from the view system
     * @param viewSetName the viewset name
     * @param viewName the view's name
     * @param title the title of the dialog
     */
    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
        formView = ViewMgr.getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView   = new MultiView(null, formView, AltView.CreationMode.Edit, false, true);
            form = multiView.getCurrentView();//ViewFactory.createFormView(null, formView, null, null);
            
            /*ImageDisplay imgDisplay = (ImageDisplay)form.getCompById("image");
            if (imgDisplay != null)
            {
                imgDisplay.setImage(IconManager.getImage("SpecifyLargeIcon"));
            }*/
            
            username  = (ValTextField)form.getCompById("username");
            password  = (ValPasswordField)form.getCompById("password");
            
            databases = (ValComboBox)form.getCompById("databases");
            servers   = (ValComboBox)form.getCompById("servers");
            viewSets  = (ValComboBox)form.getCompById("views");
            
            autoLoginCBX = (JCheckBox)form.getCompById("autologin");
            rememberUsernameCBX = (JCheckBox)form.getCompById("rememberuser");
            
            autoLoginCBX.setSelected(prefNode.getBoolean("autologin", false));
            rememberUsernameCBX.setSelected(prefNode.getBoolean("rememberuser", false));
            
            if (autoLoginCBX.isSelected())
            {
                username.setText(prefNode.get("username", ""));
                password.setValue(prefNode.get("password", ""), "");
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

            statusBar = (JStatusBar)form.getCompById("statusbar");
            
            // Ask the ValComboBox to re-create the combobox with a new and different DBAdapter
            databases.getComboBox().init(new PropertiesPickListAdapter(databases.getComboBox(), prefNode, "databases"), true);
            servers.getComboBox().init(new PropertiesPickListAdapter(servers.getComboBox(), prefNode, "servers"), true);
            
            // Ask the PropertiesPickListAdapter to set the index from the prefs
            ((PropertiesPickListAdapter)databases.getComboBox().getDBAdapter()).setSelectedIndex();
            ((PropertiesPickListAdapter)servers.getComboBox().getDBAdapter()).setSelectedIndex();
            
            servers.getComboBox().getTextField().addKeyListener(new KeyAdapter()
                    {
                public void keyReleased(KeyEvent e)
                {
                    form.getValidator().validateForm();
                }
            });
            
            databases.getComboBox().getTextField().addKeyListener(new KeyAdapter()
                    {
                public void keyReleased(KeyEvent e)
                {
                    form.getValidator().validateForm();
                }
            });
            
            // Set up the List for ViewSets (Forms) for ther to select from
            viewSetAdapter = new PropertiesPickListAdapter(viewSets.getComboBox(), prefNode, "viewset");
            viewSetAdapter.setSavePickList(false);
            viewSets.getComboBox().init(viewSetAdapter, false);
            username.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e)
                        {
                            configureViewSets();
                        }
                    });
            databases.getComboBox().getTextField().addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e)
                        {
                            configureViewSets();
                        }
                    });
            /*databases.getComboBox().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //configureViewSets();   
                    System.out.println(e);
                }
            });*/
            databases.getComboBox().addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        configureViewSets();
                    }
                }
            });

            configureViewSets();
            
            form.getValidator().validateForm();
            
            getValuesFromUI();

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }

        setContentPane(multiView);
        pack();
    }
    
    /**
     * Return whether dialog was cancelled
     * @return whether dialog was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }
    
    /**
     * Sets a string into the status bar
     * @param msg the msg for the status bar
     */
    public void setMessage(final String msg, final boolean isError)
    {
        JStatusBar statusBar = (JStatusBar)form.getCompById("statusbar");
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
        databases.getComboBox().getDBAdapter().save();
        servers.getComboBox().getDBAdapter().save();
        
        prefNode.putBoolean("rememberuser", rememberUsernameCBX.isSelected());
        prefNode.putBoolean("autologin", autoLoginCBX.isSelected());
        
        if (autoLoginCBX.isSelected())
        {
            prefNode.put("username", username.getText());
            prefNode.put("password", (String)password.getValue());
            
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
        databaseName = (String)databases.getValue();
        serverName   = (String)servers.getValue();
        usernameStr  = (String)username.getText();
        passwordStr  = (String)password.getPasswordText();

    }
    
    /**
     * Indicates the login is OK and closes the dialog for the user to conitinue on
     */
    protected void loginOK()
    {
        isCancelled = false;
        setVisible(false);
        dispose();
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
     * Given a database and a username it figures out what viewsets are available for the current user
     * and populates the dropdown list with these options
     */
    protected void configureViewSets()
    {
        usernameStr  = (String)username.getText();
        databaseName = (String)databases.getValue();
        
        if (StringUtils.isNotEmpty(usernameStr) &&  !usernameStr.equals(usernameCache) ||
            StringUtils.isNotEmpty(databaseName) &&  !databaseName.equals(databaseCache))
        {
            boolean enabled;
            List<ViewSet> list = ViewMgr.getViewSetsForUserAndDatabase(usernameStr, databaseName);
            if (list.size() > 0)
            {
                Vector<PickListItem> pickListItems = viewSetAdapter.getList();
                pickListItems.clear();
                for (ViewSet vs : list)
                {
                    viewSetAdapter.addItem(vs.getTitle(), vs.getTitle());
                }
                
                usernameCache = usernameStr;
                databaseCache = databaseName;
                
                if (list.size() == 1)
                {
                    viewSets.getComboBox().setSelectedIndex(0);
                    
                } else
                {
                    viewSetAdapter.setSelectedIndex();
                }
                setMessage("", false);
                enabled = true;
                
            } else
            {
                setMessage(String.format(getResourceString("NoViewsForUserDatabase"), new Object[] {databaseName}), true);
                enabled = true;
                databaseCache = "";
                viewSetAdapter.getList().clear();
                viewSets.setValue(null, null);
            }
            
            viewSets.setEnabled(enabled);
            form.getLabelFor("views").setEnabled(enabled);
            
        }
        //form.getValidator().validateForm();
    }
    
    //-------------------------------------------------------------------------
    //--Inner Classes
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
        protected JAutoCompComboBox comboBox;
        protected boolean           savePickList = true;

        public PropertiesPickListAdapter(final JAutoCompComboBox comboBox,
                                         final Preferences       prefNode, 
                                         final String            prefName)
        {
            super();
            
            this.comboBox = comboBox;
            this.prefNode = prefNode;
            this.prefName = prefName;
            
            this.prefSelectedName = prefName + "_selected";
            
            if (savePickList)
            {
                readData();
            }
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
            if (savePickList)
            {
                prefNode.put(prefName, convertModelToStr(pickList));
                //log.debug("["+prefName+"]["+convertModelToStr(pickList)+"]");
            }
            
            Object selectedItem = comboBox.getModel().getSelectedItem();
            if (selectedItem == null && comboBox.getTextField() != null)
            {
                selectedItem = comboBox.getTextField().getText();
            }
            
            if (selectedItem != null)
            {
                prefNode.put(prefSelectedName, selectedItem.toString());
                //log.debug("["+prefSelectedName+"]["+selectedItem.toString()+"]");
            }
        }
    }

}
