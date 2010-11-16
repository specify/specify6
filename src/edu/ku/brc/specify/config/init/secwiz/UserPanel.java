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
package edu.ku.brc.specify.config.init.secwiz;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.config.init.BaseSetupPanel;
import edu.ku.brc.specify.config.init.PrintTableHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 21, 2010
 *
 */
public class UserPanel extends BaseSetupPanel
{
    protected JList             dbList = null;
    protected JScrollPane       dbScrollPane;
    protected JLabel            label;
    
    protected JTable            userTable = null;
    protected UserTableModel    userModel;
    protected JScrollPane       userScrollPane;

    protected JButton           saveBtn    = null;
    protected JButton           mkKeysBtn    = null;
    protected JButton           sendKeysBtn  = null;
    protected JButton           printKeysBtn = null;
    protected JButton[]         btns         = null;
    
    protected MasterLoginPanel  masterPanel;
    protected String            databaseName = null;
    
    /**
     * @param panelName
     * @param helpContext
     * @param nextBtn
     * @param prevBtn
     * @param makeStretchy
     */
    public UserPanel(String panelName, 
                     String helpContext, 
                     JButton nextBtn, 
                     JButton prevBtn,
                     boolean makeStretchy,
                     MasterLoginPanel masterPanel)
    {
        super(panelName, helpContext, nextBtn, prevBtn, makeStretchy);
        this.masterPanel = masterPanel;
        createUI();
    }

    /**
     * 
     */
    protected void createUI()
    {
        dbList = new JList(new DefaultListModel());
        
        userModel = new UserTableModel(null);
        userTable = new JTable(userModel);
        
        CellConstraints cc = new CellConstraints();
        
        saveBtn    = UIHelper.createButton("Save");
        mkKeysBtn    = UIHelper.createButton("Make Keys");
        sendKeysBtn  = UIHelper.createButton("Send Keys");
        printKeysBtn = UIHelper.createButton("Print");
        btns         = new JButton[] {saveBtn, sendKeysBtn, mkKeysBtn, printKeysBtn};
        
        String       colDef = UIHelper.createDuplicateJGoodiesDef("p", "8px", btns.length);
        PanelBuilder btnPB  = new PanelBuilder(new FormLayout("f:p:g,"+colDef, "p"));
        int          x      = 2;
        for (JButton b : btns)
        {
            btnPB.add(b, cc.xy(x, 1));
            x += 2;
        }
        saveBtn.setEnabled(false);
        
        label = UIHelper.createLabel("", SwingConstants.CENTER);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p", "f:p:g,8px,p,8px,p,4px,f:p:g,4px,p,4px,p"), this);
        
        sendKeysBtn.setVisible(false);
        
        int y = 1;
        pb.add(dbScrollPane = UIHelper.createScrollPane(dbList), cc.xyw(1, y, 2)); y += 2;
        pb.addSeparator("",        cc.xyw(1, y, 2)); y += 2;
        pb.add(label,              cc.xyw(1, y, 2)); y += 2;
        pb.add(userScrollPane = UIHelper.createScrollPane(userTable), cc.xyw(1, y, 2)); y += 2;
        pb.add(btnPB.getPanel(),   cc.xy(2, y));  y += 2;
        pb.addSeparator("",        cc.xyw(1, y, 2));  y += 2;
        
        dbList.setVisibleRowCount(8);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                Dimension size = userTable.getPreferredScrollableViewportSize();
                size.height = 10 * userTable.getRowHeight();
                userTable.setPreferredScrollableViewportSize(size);
            }
        });
        
        dbScrollPane.setVisible(false);
        updateBtnUI(false);
        
        dbList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    loadData(false);
                }
            }
        });
        
        userTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    doUserSelected();
                }
            }
        });
        
        saveBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveUserData();
            }
        });
        
        sendKeysBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendKeys();
            }
        });
        
        mkKeysBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                makeKeys();
            }
        });
        
        printKeysBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                printUserData();
            }
        });
    }
    
    /**
     * @param enabled
     */
    private void updateBtnUI(final boolean enabled)
    {
        saveBtn.setEnabled(enabled && userModel.isChanged());
    }
    
    /**
     * 
     */
    private void saveUserData()
    {
        boolean     hasErrors = false;
        DBMSUserMgr mgr       = DBMSUserMgr.getInstance();
        
        PreparedStatement pStmtSp = null;
        PreparedStatement pStmtAg = null;
        try
        {
            String dbUserName = properties.getProperty("dbUserName");
            String dbPassword = properties.getProperty("dbPassword");
            String hostName   = properties.getProperty("hostName");
           
            if (mgr.connect(dbUserName, dbPassword, hostName, databaseName))
            {
                pStmtSp = mgr.getConnection().prepareStatement("UPDATE specifyuser SET EMail=?, Password=? WHERE SpecifyUserID = ?");
                pStmtAg = mgr.getConnection().prepareStatement("UPDATE agent SET Email=? WHERE AgentID = ?");
                
                for (UserData ud : userModel.getUserData())
                {
                    if (ud.isChanged())
                    {
                        if (StringUtils.isNotEmpty(ud.getEmail()))
                        {
                            pStmtSp.setString(1, ud.getEmail());
                        } else
                        {
                            pStmtSp.setObject(1, null);
                        }
                        pStmtSp.setString(2, ud.getPassword());
                        pStmtSp.setInt(3,    ud.getId());
                        int rv = pStmtSp.executeUpdate();
                        if (rv == 1)
                        {
                            ud.setChanged(false);
                        } else
                        {
                            System.err.println("Error "+pStmtSp.getWarnings());
                        }
                        
                        if (StringUtils.isNotEmpty(ud.getEmail()))
                        {
                            String sql = String.format("SELECT AgentID WHERE SpecifyUserID = %d AND (Email IS NULL OR Email <> '%s')", ud.getId(), ud.getEmail());
                            Vector<Integer> agentIds = BasicSQLUtils.queryForInts(mgr.getConnection(), sql);
                            for (Integer agentId : agentIds)
                            {
                                pStmtAg.setString(1, ud.getEmail());
                                pStmtAg.setInt(2, agentId);
                            }
                            
                            if (pStmtSp.executeUpdate() != 1)
                            {
                                // error
                                hasErrors = true;
                            }
                        }
                    }
                }

            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (pStmtSp != null)pStmtSp.close();
                if (pStmtAg != null)pStmtSp.close();
                mgr.close();
            } catch (SQLException e){}
        }
        if (!hasErrors)
        {
            userModel.setChanged(false);
            saveBtn.setEnabled(false);
            mkKeysBtn.setEnabled(false);
        }
    }
    
    /**
     * @return
     */
    protected int[] getSelectedIds()
    {
        int[] selectedIds = userTable.getSelectedRows();
        if (userTable.getSelectedRowCount() > 0)
        {
            selectedIds = userTable.getSelectedRows();
        } else
        {
            selectedIds = new int[userTable.getRowCount()];
            for (int i=0;i<userTable.getRowCount();i++)
            {
                selectedIds[i] = i;
            }
        }
        return selectedIds;
    }

    /**
     * 
     */
    private void makeKeys()
    {
        //String saUserName = properties.getProperty("saUserName");
        String saPassword = properties.getProperty("saPassword");
        
        int[] selectedIds = getSelectedIds();
        
        Vector<UserData> items = userModel.getUserData();
        for (int inx : selectedIds)
        {
            UserData ud  = items.get(inx);
            String   pwd = ud.getPassword();
            if (StringUtils.isNotEmpty(pwd) && 
                    (!StringUtils.isAlphanumeric(pwd) ||
                     !UIHelper.isAllCaps(pwd) ||
                     pwd.length() < 25))
            {
                String encryptedPwd = Encryption.encrypt(saPassword, pwd);
                if (StringUtils.isNotEmpty(encryptedPwd))
                {
                    ud.setPassword(encryptedPwd);
                    ud.setChanged(true);
                    userModel.setChanged(true);
                    ud.setClearTextPassword(pwd);
                }
            }
        }
        saveBtn.setEnabled(userModel.isChanged());
    }

    /**
     * 
     */
    private void sendKeys()
    {
        final Hashtable<String, String> emailPrefs = new Hashtable<String, String>();
        if (!EMailHelper.isEMailPrefsOK(emailPrefs))
        {
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                    getResourceString("NO_EMAIL_PREF_INFO"), 
                    getResourceString("NO_EMAIL_PREF_INFO_TITLE"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int[] selectedIds = getSelectedIds();
        
        Vector<UserData> items = userModel.getUserData();
        for (int inx : selectedIds)
        {
            UserData ud  = items.get(inx);
            /*emailPrefs.put("to", toAgent.getEmail() != null ? toAgent.getEmail() : "");
            emailPrefs.put("from", emailPrefs.get("email"));
            emailPrefs.put("subject", String.format(getResourceString("SEC_WIZ"), new Object[] {infoRequest.getIdentityTitle()}));
            emailPrefs.put("bodytext", "");
            */
            StringBuilder sb = new StringBuilder();
    
            // EMailHelper.setDebugging(true);
            String text = emailPrefs.get("bodytext").replace("\n", "<br>") + "<BR><BR>" + sb.toString();
            UIRegistry.displayLocalizedStatusBarText("SENDING_EMAIL");
    
            String password = Encryption.decrypt(emailPrefs.get("password"));
            if (StringUtils.isEmpty(password))
            {
                password = EMailHelper.askForPassword((Frame)UIRegistry.getTopWindow());
            }
    
            if (StringUtils.isNotEmpty(password))
            {
                final EMailHelper.ErrorType status = EMailHelper.sendMsg(emailPrefs.get("smtp"),
                                                                         emailPrefs.get("username"), 
                                                                         password, emailPrefs.get("email"), 
                                                                         emailPrefs.get("to"), 
                                                                         emailPrefs.get("subject"), 
                                                                         text, 
                                                                         EMailHelper.HTML_TEXT,
                                                                         emailPrefs.get("port"), 
                                                                         emailPrefs.get("security"), 
                                                                         null);
                if (status != EMailHelper.ErrorType.Cancel)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            UIRegistry.displayLocalizedStatusBarText(status == EMailHelper.ErrorType.Error ? "EMAIL_SENT_ERROR" : "EMAIL_SENT_OK");
                        }
                    });
                }
            }
        }
    }

    /**
     * 
     */
    private void printUserData()
    {
        JTable printTable = new JTable();
        
        
        int i = 0;
        Object[][] pValueObjs = new Object[userModel.getUserData().size()][6];
        for (UserData ud : userModel.getUserData())
        {
            pValueObjs[i++] = ud.getData();
        }

        String[] headers = {"Username", "Passsword", "Last Name", "First Name", "EMail", "New Password"};
        DefaultTableModel model = new DefaultTableModel(pValueObjs, headers)
        {
            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                return String.class;
            }
        };
        printTable.setModel(model);
        
        PrintTableHelper pth = new PrintTableHelper(printTable);
        pth.printGrid(UIRegistry.getResourceString("SUMMARY"));
    }

    /**
     * 
     */
    private void doUserSelected()
    {
        int index = userTable.getSelectedRow();
        updateBtnUI(index > -1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        super.doingNext();
        
        loadData(true);
    }
    
    /**
     * @param isInitial
     */
    private void loadData(final boolean isInitial)
    {
        label.setText("");
        
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String hostName   = properties.getProperty("hostName");
        
        String dbUserName = properties.getProperty("dbUserName");
        String dbPassword = properties.getProperty("dbPassword");
        
        //String saUserName = properties.getProperty("saUserName");
        //String saPassword = properties.getProperty("saPassword");
        
        int index = 0;
        List<String>   dbNamesList = masterPanel.getDbNamesForMaster();
        Vector<String> items       = new Vector<String>(dbNamesList);
        Collections.sort(items);
        if (!isInitial)
        {
            index = dbList.getSelectedIndex();
            if (index == -1)
            {
                return;
            }
        }
        
        databaseName = items.get(index);
        
        if (mgr.connect(dbUserName, dbPassword, hostName, databaseName))
        {
            dbScrollPane.setVisible(dbNamesList.size() > 1);
            DefaultListModel model = new DefaultListModel();
            for (String nm : items)
            {
                model.addElement(nm);
            }
            dbList.setModel(model);
            
            label.setText(databaseName);
            
            Vector<UserData> userDataList = new Vector<UserData>();
            String           sql          = "SELECT SpecifyUserId, Name, Password, EMail FROM specifyuser";
            Vector<Object[]> data         = BasicSQLUtils.query(mgr.getConnection(), sql);
            
            for (Object[] c : data)
            {
                UserData ud = new UserData((Integer)c[0], (String)c[1], (String)c[2], (String)c[3]);
                userDataList.add(ud);
                
                sql = String.format("SELECT LastName, FirstName, EMail FROM agent WHERE SpecifyUserID = %d ORDER BY TimestampModified, TimestampCreated LIMIT 0,1", ud.getId());
                Vector<Object[]> uData = BasicSQLUtils.query(mgr.getConnection(), sql);
                if (uData.size() > 0)
                {
                    Object[] d = uData.get(0);
                    ud.setLastName((String)d[0]);
                    ud.setFirstName((String)d[1]);
                    
                    String email = (String)d[2];
                    if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(ud.getEmail()))
                    {
                        ud.setEmail(email);
                    }
                } else
                {
                    // error
                }
            }
            mgr.close();
            userModel.setUserData(userDataList);
            UIHelper.calcColumnWidths(userTable);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    Window    window       = getTopWindow();
                    Insets    screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
                    Rectangle screenRect   = window.getGraphicsConfiguration().getBounds();
                    
                    screenRect.height -= screenInsets.top + screenInsets.bottom;
                    screenRect.width  -= screenInsets.left + screenInsets.right;
                    
                    Rectangle rect     = window.getBounds();
                    Dimension size     = window.getPreferredSize();
                    
                    // Make sure the window isn't larger than the screen
                    size.width  = Math.min(size.width, screenRect.width);
                    size.height = Math.min(size.height, screenRect.height);
                    
                    if (size.height > rect.height || size.width > rect.width)
                    {
                        window.setBounds(rect.x, rect.y, size.width, size.height);
                        UIHelper.centerWindow(getTopWindow());
                        
                    }
                }
            });
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        super.doingPrev();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getPanelName()
     */
    @Override
    public String getPanelName()
    {
        return super.getPanelName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Properties)
     */
    @Override
    public void setValues(Properties values)
    {
        super.setValues(values);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        return null;
    }

    
    //-----------------------------------------------------------------------
    //--
    //-----------------------------------------------------------------------
    class UserData
    {
        protected boolean isChanged;
        protected int     id;
        protected String  userName;
        protected String  password;
        protected String  lastName;
        protected String  firstName;
        protected String  email;
        
        protected String  clearTextPassword;
        
        // Cached Value
        protected String cachedEmail;
        
        /**
         * @param id
         * @param userName
         * @param password
         * @param email
         */
        public UserData(int id, String userName, String password, String email)
        {
            super();
            this.isChanged = false;
            this.id        = id;
            this.userName  = userName;
            this.password  = password;
            this.lastName  = "";
            this.firstName = "";
            this.email     = email;
            
            this.clearTextPassword = null;
            this.cachedEmail = email;
        }
        
        /**
         * @return
         */
        public Object[] getData()
        {
            int i = 0;
            Object[] data = new Object[6];
            data[i++] = userName;
            data[i++] = password;
            data[i++] = lastName;
            data[i++] = firstName;
            data[i++] = email;
            data[i++] = clearTextPassword;
            return data;
        }
        
        /**
         * @return the id
         */
        public int getId()
        {
            return id;
        }
        /**
         * @param id the id to set
         */
        public void setId(int id)
        {
            this.id = id;
        }
        /**
         * @return the userName
         */
        public String getUserName()
        {
            return userName;
        }
        /**
         * @param userName the userName to set
         */
        public void setUserName(String userName)
        {
            this.userName = userName;
        }
        /**
         * @return the password
         */
        public String getPassword()
        {
            return password;
        }
        /**
         * @param password the password to set
         */
        public void setPassword(String password)
        {
            this.password = password;
        }
        /**
         * @return the lastName
         */
        public String getLastName()
        {
            return lastName;
        }
        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }
        /**
         * @return the firstName
         */
        public String getFirstName()
        {
            return firstName;
        }
        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }
        /**
         * @return the email
         */
        public String getEmail()
        {
            return email;
        }
        /**
         * @param email the email to set
         */
        public void setEmail(String email)
        {
            this.email = email;
        }
        /**
         * @return the isChanged
         */
        public boolean isChanged()
        {
            return isChanged;
        }
        /**
         * @param isChanged the isChanged to set
         */
        public void setChanged(boolean isChanged)
        {
            this.isChanged = isChanged;
        }
        /**
         * @return the cachedEmail
         */
        public String getCachedEmail()
        {
            return cachedEmail;
        }
        /**
         * @return the clearTextPassword
         */
        public String getClearTextPassword()
        {
            return clearTextPassword;
        }
        /**
         * @param clearTextPassword the clearTextPassword to set
         */
        public void setClearTextPassword(String clearTextPassword)
        {
            this.clearTextPassword = clearTextPassword;
        }
        
    }
    
    //-----------------------------------------------------------------------
    //--
    //-----------------------------------------------------------------------
    class UserTableModel extends DefaultTableModel
    {
        protected String[] headers = {"Changed", "Username", "Passsword", "Last Name", "First Name", "EMail"};
        
        protected Vector<UserData> items     = new Vector<UserData>();
        protected boolean          isChanged = false;
        
        /**
         * @param items
         */
        public UserTableModel(final List<UserData> items)
        {
            super();
            if (items != null)
            {   
                this.items.addAll(items);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return headers != null ? headers.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers != null ? headers[column] : "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return items != null ? items.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            UserData ud = items.get(row);
            if (ud != null)
            {
                switch (column)
                {
                    case 0: return ud.isChanged();
                    case 1: return ud.getUserName();
                    case 2: return ud.getPassword();
                    case 3: return ud.getLastName();
                    case 4: return ud.getFirstName();
                    case 5: return ud.getEmail();
                }
            }
            return "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getDataVector()
         */
        @Override
        public Vector<?> getDataVector()
        {
            return items;
        }
        
        /**
         * @return
         */
        public Vector<UserData> getUserData()
        {
            return items;
        }

        /**
         * @param isChanged the isChanged to set
         */
        public void setChanged(boolean isChanged)
        {
            this.isChanged = isChanged;
            fireTableDataChanged();
        }

        /**
         * @param list
         */
        public void setUserData(final List<UserData> list)
        {
            if (items != null)
            {   
                this.items.clear();
                this.items.addAll(list);
                fireTableDataChanged();
                isChanged = false;
            }
        }
        
        /**
         * @return the isChanged
         */
        public boolean isChanged()
        {
            return isChanged;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            UserData ud = items.get(row);
            switch (column)
            {
                case 0: return ud.isChanged();
                case 1: return false;
                case 2: return true;
                case 3: return false;
                case 4: return false;
                case 5: return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            UserData ud = items.get(row);
            if (ud != null)
            {
                switch (column)
                {
                    case 0:
                        ud.setChanged((Boolean)aValue);
                        ud.setEmail(ud.getCachedEmail());
                        fireTableDataChanged();
                        break;
                        
                    case 2: 
                        ud.setPassword((String)aValue);
                        ud.setChanged(true);
                        break;
                        
                    case 5: 
                        isChanged = true;
                        ud.setEmail((String)aValue);
                        ud.setChanged(true);
                        break;
                }
            }
        }
    }
}
