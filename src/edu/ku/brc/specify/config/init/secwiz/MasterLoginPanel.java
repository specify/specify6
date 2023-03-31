/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.specify.config.init.GenericFormPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 01, 2009
 *
 */
public class MasterLoginPanel extends GenericFormPanel
{
    private static final String HELP_CONTEXT = "security_wiz1";
    
    protected String                  propName = "next";
    protected Boolean                 isOK     = null;
    protected JLabel                  label;
    protected String                  errorKey = null;
    
    // Advanced Part
    protected JButton                 skipStepBtn;
    protected boolean                 manualLoginOK = false;
    protected JLabel                  advLabel;
    protected JButton                 resetMasterBtn;
    protected JButton                 resetMasterPermsBtn;
    
    protected boolean                 isEmbedded = false;

    protected List<String>            dbNamesForMaster = null;
    protected List<String>            dbNameList       = null;


    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param nextBtn
     * @param makeStretchy
     */
    public MasterLoginPanel(String name, 
                           String title, 
                           String helpContext, 
                           String[] labels,
                           String[] fields, 
                           Integer[] numColumns, 
                           JButton nextBtn, 
                           JButton prevBtn, 
                           boolean makeStretchy)
    {
        super(name, title, helpContext, labels, fields, numColumns, nextBtn, prevBtn, makeStretchy);
        this.helpContext = HELP_CONTEXT;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#init(java.lang.String, java.lang.String[], boolean[], java.lang.String[], java.lang.Integer[])
     */
    @Override
    protected void init(final String    title, 
                        final String[]  fields,
                        final boolean[] required, 
                        final String[]  types, 
                        final Integer[] numColumns)
    {
        super.init(title, fields, required, types, numColumns);
        this.helpContext = HELP_CONTEXT;
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "20px,p,2px,p"));
        panelPB.add(getProgressBar(), cc.xy(1, 2));
        panelPB.add(label = createLabel("  "),  cc.xy(1, 4));
        
        builder.add(panelPB.getPanel(), cc.xyw(3, row, 2));
        row += 2;
        
        // Advance part of pane
        advLabel    = UIHelper.createI18NLabel("SEC_ADV_MSTR_DESC", SwingConstants.CENTER);
        skipStepBtn = UIHelper.createI18NButton("ADV_MU_TEST");
        builder.add(advLabel, cc.xyw(3, row, 2)); row += 2;
        builder.add(advLabel, cc.xyw(3, row, 2)); row += 2;
        
        JComponent helpComponent = DatabasePanel.createHelpPanel(getBackground(), "master");
        resetMasterBtn      = UIHelper.createI18NButton("SEC_RESET_BTN");
        resetMasterPermsBtn = UIHelper.createI18NButton("SEC_RESET_PERMS_BTN");
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p,4px,p,0px,p,10px,f:p:g"));
        tstPB.add(skipStepBtn,          cc.xy(2, 1));
        tstPB.add(resetMasterBtn,       cc.xy(2, 3));
        tstPB.add(resetMasterPermsBtn,  cc.xy(2, 5));
        tstPB.add(helpComponent,        cc.xyw(1, 7, 3));
        
        resetMasterBtn.setVisible(false);
        resetMasterPermsBtn.setVisible(false);
        
        final JTextField pwdTF = (JTextField)comps.get("saPassword");
        pwdTF.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                skipStepBtn.setEnabled(pwdTF.getText().length() > 0);
            }
        });

        builder.add(tstPB.getPanel(),   cc.xyw(3, row, 2)); row += 2;
        
        skipStepBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                skipStepBtn.setEnabled(false);
                boolean ok = skipDBCreate();
                
                if (ok)
                {
                    ok = checkForPermissions();
                }
                
                //skipStepBtn.setEnabled(!ok);
                nextBtn.setEnabled(ok);
                
                advLabel.setText(UIRegistry.getResourceString(ok ? "ADV_DB_OK" : "ADV_DB_ERR"));
                advLabel.setForeground(ok ? Color.BLACK : Color.RED);
            }
        });
        
        resetMasterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                skipStepBtn.setEnabled(false);
                boolean ok = doResetMaster(null);
                skipStepBtn.setEnabled(!ok);
                nextBtn.setEnabled(ok);
                
                advLabel.setText(UIRegistry.getResourceString(ok ? "SEC_RESET_DB_OK" : "SEC_RESET_DB_ERR"));
                advLabel.setForeground(ok ? Color.BLACK : Color.RED);
            }
        });

        resetMasterPermsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                skipStepBtn.setEnabled(false);
                boolean ok = doResetMasterPerms();
                nextBtn.setEnabled(ok);
                skipStepBtn.setEnabled(!ok);
                advLabel.setText(UIRegistry.getResourceString(ok ? "SEC_RESET_PERMS_OK" : "SEC_RESET_PERMS_ERR"));
                advLabel.setForeground(ok ? Color.BLACK : Color.RED);
            }
        });
        
        progressBar.setVisible(false);
        
        if (UIRegistry.isMobile())
        {
            skipStepBtn.setVisible(false);
            advLabel.setVisible(false);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        advLabel.setText(getResourceString("SEC_ADV_MSTR_DESC"));
        advLabel.setForeground(Color.BLACK);
        
        skipStepBtn.setEnabled(true);
        skipStepBtn.setVisible(true);
        resetMasterBtn.setVisible(false);
        resetMasterPermsBtn.setVisible(false);
        
        isEmbedded = DBConnection.getInstance().isEmbedded();
        if (isEmbedded)
        {
            ((JTextField)comps.get("saUserName")).setText(properties.getProperty("dbUserName"));
            ((JTextField)comps.get("saPassword")).setText(properties.getProperty("dbPassword"));
            ((JTextField)comps.get("saUserName")).setEnabled(false);
            ((JTextField)comps.get("saPassword")).setEnabled(false);
        }
    }

    /**
     * @return the Row and Column JGoodies definitions
     */
    protected Pair<String, String> getRowColDefs()
    {
        String rowDef = "p,5px" + (fieldsNames.length > 0 ? ","+createDuplicateJGoodiesDef("p", "2px", fieldsNames.length) : "") + getAdditionalRowDefs() + ",2px,p,2px,p,8px,p";
        return new Pair<String, String>("p,2px,p,f:p:g", rowDef);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        advLabel.setText("");
        skipStepBtn.setEnabled(true);
        skipStepBtn.setVisible(true);
        resetMasterBtn.setVisible(false);
        resetMasterPermsBtn.setVisible(false);
    }
    
    /**
     * 
     */
    protected boolean skipDBCreate()
    {
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String hostName   = properties.getProperty("hostName");
        String saUserName = ((JTextField)comps.get("saUserName")).getText();
        String saPassword = ((JTextField)comps.get("saPassword")).getText();

        if (!isEmbedded)
        {
            if (mgr.connectToDBMS(saUserName, saPassword, hostName))
            {
                nextBtn.setEnabled(true);
                mgr.close();
                return true;
            }
        } else
        {
            nextBtn.setEnabled(true);
            return true;
        }
        
        resetMasterBtn.setVisible(true);
        return false;
    }
    
    /**
     * @param dbNames
     * @return
     */
    private List<String> getSpecifyDatabases(final DBMSUserMgr mgr, final List<String> dbNames)
    {
        if (dbNames != null && dbNames.size() > 0)
        {
            ArrayList<String> specifyDBs = new ArrayList<String>(dbNames.size());
            for (String dbName : dbNames)
            {
                if (mgr.doesDBHaveTable(dbName, "specifyuser"))
                {
                    specifyDBs.add(dbName);
                }
            }
            return specifyDBs;
        }
        return new ArrayList<String>();
    }
    
    /**
     * @return
     */
    public List<String> getDatabaseList(final DBMSUserMgr mgr)
    {
        List<String> dbList = getSpecifyDatabases(mgr, mgr.getDatabaseList());
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(), "SEC_SELECT_DBS",
                                                                                "SEC_SELECT_DBS_DESC", dbList, 
                                                                                CustomDialog.OKCANCELHELP, ToggleButtonChooserPanel.Type.Checkbox);
        dlg.setHelpContext("security_wizreset");
        dlg.setAddSelectAll(true);
        dlg.setUseScrollPane(true);
        dlg.createUI();
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            return dlg.getSelectedObjects();
        }
        return null;
    }
    
    
    /**
     * @return
     */
    private boolean checkForPermissions()
    {
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String dbUserName = properties.getProperty("dbUserName");
        String dbPassword = properties.getProperty("dbPassword");
        String hostName   = properties.getProperty("hostName");
        
        if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
        {
            Vector<String> badDBs = new Vector<String>();
            
            String saUserName = properties.getProperty("saUserName");
            
            boolean doFix = false;
            dbNameList                  = new ArrayList<String>();
            List<String> mastersDBNames = mgr.getDatabaseListForUser(saUserName);
            List<String> allSp6DBs      = getSpecifyDatabases(mgr, mgr.getDatabaseList()); // returns all database that are Specify 6
            
            HashSet<String> sp6DBNameHash  = new HashSet<String>(allSp6DBs);
            HashSet<String> mstrDBNameHash = new HashSet<String>(mastersDBNames);
            
            if (dbNamesForMaster == null)
            {
                dbNamesForMaster = new ArrayList<String>();
            } else
            {
                dbNamesForMaster.clear();
            }
            
            // Loop through all the Master User's databases and 
            // see if they are a Specify 6 database
            for (String dbName : mastersDBNames)
            {
                if (sp6DBNameHash.contains(dbName) && mgr.doesDBExists(dbName))
                {
                    dbNamesForMaster.add(dbName);
                }
            }
            
            // Loop through all the Specify 6's databases and 
            // see if they are they are in the Master's list
            for (String dbName : allSp6DBs)
            {
                if (!mstrDBNameHash.contains(dbName) && mgr.doesDBExists(dbName))
                {
                    dbNameList.add(dbName);
                }
            }
            
            if (dbNamesForMaster.size() == 0)
            {
                int rv = UIRegistry.askYesNoLocalized("SEC_FIX_PERMS", "CANCEL", getResourceString("SEC_NO_DBS_PERMS"), "WARNING");
                doFix = rv == JOptionPane.OK_OPTION;
                
            } else
            {
                // Now check all the databases that the Master has access to to make sure 
                // they have the correct permissions.
                for (String dbn : dbNamesForMaster)
                {
                    int perms = mgr.getPermissionsForAccess(saUserName, dbn);
                    //System.out.println(dbn+" => "+perms+"  "+DBMSUserMgr.PERM_ALL_BASIC+" ("+(perms & DBMSUserMgr.PERM_ALL_BASIC)+") => "+((perms & DBMSUserMgr.PERM_ALL_BASIC) != DBMSUserMgr.PERM_ALL_BASIC));
                    if ((perms & DBMSUserMgr.PERM_ALL_BASIC) != DBMSUserMgr.PERM_ALL_BASIC)
                    {
                        badDBs.add(dbn);
                    }
                }
                
                if (badDBs.size() > 0)
                {
                    PanelBuilder    pb   = new PanelBuilder(new FormLayout("f:MAX(300px; p):g", "p,8px,f:MAX(300px; p):g"));
                    JList           list = new JList(badDBs);
                    pb.add(UIHelper.createI18NLabel("SEC_BAD_DBS_PERMS"), cc.xy(1, 1));
                    pb.add(UIHelper.createScrollPane(list), cc.xy(1, 3));
                    
                    pb.setDefaultDialogBorder();
                    CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), getResourceString("WARNING"), true, pb.getPanel());
                    dlg.setOkLabel(getResourceString("SEC_FIX_PERMS"));
                    UIHelper.centerAndShow(dlg);
                    doFix = !dlg.isCancelled();
                    
                } else
                {
                    mgr.close();
                    return true;
                }
            }
            
            if (doFix)
            {
                boolean isResetOK = doResetMaster(badDBs);
                if (isResetOK)
                {
                    dbNamesForMaster = mgr.getDatabaseListForUser(saUserName); // Get them again after everything is fixed.
                    
                    advLabel.setText(getResourceString("SEC_RESET_PERMS_OK"));
                    mgr.close();
                    return true;
                }
            }
            mgr.close();
        } else
        {
            UIRegistry.showLocalizedError("SEC_UNEX_ERR_LGN");
        }
        return false;
    }
    
    /**
     * 
     */
    protected boolean doResetMaster(final Vector<String> databaseNames)
    {
        // Clear the database list associated with the Master user
        if (dbNameList != null)
        {
            dbNameList.clear();
        }
        
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String dbUserName = properties.getProperty("dbUserName");
        String dbPassword = properties.getProperty("dbPassword");
        String hostName   = properties.getProperty("hostName");
        
        String saUserName = ((JTextField)comps.get("saUserName")).getText();
        String saPassword = ((JTextField)comps.get("saPassword")).getText();
        
        if (!isEmbedded)
        {
            if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
            {
                List<String> dbNames = databaseNames;
                if (dbNames == null || dbNames.size() == 0)
                {
                    dbNames = getDatabaseList(mgr);
                    if (dbNames == null || dbNames.size() == 0)
                    {
                        return false;
                    }
                }
                
                boolean dropUserOK = true;
                if (mgr.doesUserExists(saUserName))
                {
                    dropUserOK = mgr.dropUser(saUserName);
                }
                
                if (dropUserOK)
                {
                    int okCnt = 0;
                    for (String dbnm : dbNames)
                    {
                        if (!mgr.createUser(saUserName, saPassword, dbnm, DBMSUserMgr.PERM_ALL_BASIC))
                        {
                            UIRegistry.showLocalizedError("MSTR_ERR_SETDB", dbnm);
                        } else
                        {
                            okCnt++;
                        }
                    }
                    
                    String key = okCnt > 0 ? "MSTR_PERMS_SET_OK" : "MSTR_PERMS_SET_ERR";
                    advLabel.setText(getResourceString(key));
                    /*boolean rv = false;
                    if (mgr.createUser(saUserName, saPassword, dbNames.get(0), DBMSUserMgr.PERM_ALL_BASIC))
                    {
                        rv = true;
                        for (int i=1;i<dbNames.size();i++)
                        {
                            rv = mgr.setPermissions(saUserName, dbNames.get(i), DBMSUserMgr.PERM_ALL_BASIC);
                            if (!rv)
                            {
                                advLabel.setText("Master User's username and password have been reset."); // I18N
                                break; 
                            }
                        }
                    }*/
                    
                    if (okCnt > 0)
                    {
                        nextBtn.setEnabled(true);
                        skipStepBtn.setEnabled(true);
                        resetMasterBtn.setVisible(false);
                        mgr.close();
                        return true;
                    }
                    advLabel.setText("There was an error setting the Master User's username and password."); // I18N
                } else
                {
                    UIRegistry.showLocalizedError("SEC_ERR_DROP_USER");
                }
                mgr.close();
                
            } else
            {
                UIRegistry.showLocalizedError("SEC_UNEX_ERR_LGN");
            }
        } else
        {
            nextBtn.setEnabled(true);
            return true;
        }
        
        resetMasterBtn.setVisible(true);
        return false;
    }
    
    /**
     * @return
     */
    private boolean doResetMasterPerms()
    {
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String dbUserName = properties.getProperty("dbUserName");
        String dbPassword = properties.getProperty("dbPassword");
        String hostName   = properties.getProperty("hostName");
        
        String saUserName = ((JTextField)comps.get("saUserName")).getText();
        String saPassword = ((JTextField)comps.get("saPassword")).getText();
        
        if (!isEmbedded)
        {
            if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
            {
                if (mgr.dropUser(saUserName))
                {
                    List<String> dbNames= getDatabaseList(mgr);
                    if (dbNames != null)
                    {
                        boolean rv = false;
                        if (mgr.createUser(saUserName, saPassword, dbNames.get(0), DBMSUserMgr.PERM_ALL_BASIC))
                        {
                            for (int i=1;i<dbNames.size();i++)
                            {
                                rv = mgr.setPermissions(saUserName, dbNames.get(i), DBMSUserMgr.PERM_ALL_BASIC);
                                if (!rv)
                                {
                                    advLabel.setText("Master User's username and password have been reset.");
                                    break; 
                                }
                            }
                        }
                        
                        if (rv)
                        {
                            nextBtn.setEnabled(true);
                            mgr.close();
                            return true;
                        }
                        advLabel.setText("There was an error setting the Master User's username and password.");
                    }
                } else
                {
                    UIRegistry.showLocalizedError("SEC_ERR_DROP_USER");
                }
                mgr.close();
            } else
            {
                UIRegistry.showLocalizedError("SEC_UNEX_ERR_LGN");
            }
        } else
        {
            nextBtn.setEnabled(true);
            return true;
        }
        
        resetMasterBtn.setVisible(true);
        return false;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
        super.getValues(props);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getAdditionalRowDefs()
     */
    @Override
    protected String getAdditionalRowDefs()
    {
        return ",2px,p";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        boolean isValid = super.isUIValid();
        
        if (properties != null)
        {
            String dbUsername = properties.getProperty("dbUserName");
            String saUserName = ((JTextField)comps.get("saUserName")).getText();
            String saPassword = ((JTextField)comps.get("saPassword")).getText();
            
            if (!DatabasePanel.checkForValidText(label, saUserName, "ERR_BAD_USRNAME", "NO_SPC_USRNAME", false) ||
                !DatabasePanel.checkForValidText(label, saPassword,  null,             "NO_SPC_PWDNAME", false))
            {
                isOK = false;
                return false;
            }
            
            if (dbUsername.equals(saUserName) && !isEmbedded)
            {
                label.setForeground(Color.RED);
                label.setText(UIRegistry.getResourceString("DB_SA_USRNAME_MATCH"));
                return false;
            }
            label.setText("");
        }
        
        return isValid && isOK != null && isOK;
    }
    
    /**
     * @param enable
     */
    protected void setUIEnabled(final boolean enable)
    {
        for (JComponent c : compList)
        {
            c.setEnabled(enable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#textChanged(javax.swing.JTextField)
     */
    @Override
    protected void textChanged(final JTextField txt)
    {
        super.textChanged(txt);
        
        if (isOK != null && !isOK)
        {
            isOK = null;
            label.setText(" ");
            properties.put("masterChanged", true);
        }
    }

    /**
     * @return the dbNamesForMaster
     */
    public List<String> getDbNamesForMaster()
    {
        if (dbNamesForMaster == null || dbNamesForMaster.size() == 0)
        {
            DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
            
            String dbUserName = properties.getProperty("dbUserName");
            String dbPassword = properties.getProperty("dbPassword");
            String hostName   = properties.getProperty("hostName");
            
            if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
            {
                
                String saUserName = properties.getProperty("saUserName");
                if (saUserName != null)
                {
                    dbNamesForMaster = mgr.getDatabaseListForUser(saUserName);
                }
                mgr.close();
            }
        }
        return dbNamesForMaster;
    }

    /**
     * @return the dbNameList
     */
    public List<String> getDbNameList()
    {
        if (dbNameList == null || dbNameList.size() == 0)
        {
            DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
            
            String dbUserName = properties.getProperty("dbUserName");
            String dbPassword = properties.getProperty("dbPassword");
            String hostName   = properties.getProperty("hostName");
            
            if (mgr.connectToDBMS(dbUserName, dbPassword, hostName))
            {
                dbNameList = getSpecifyDatabases(mgr, mgr.getDatabaseList());
                mgr.close();
            }
        }
        return dbNameList;
    }
    
}
