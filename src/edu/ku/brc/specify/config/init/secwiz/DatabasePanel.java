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

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getAppDataSubDir;
import static edu.ku.brc.ui.UIRegistry.getDefaultMobileEmbeddedDBPath;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.isMobile;
import static edu.ku.brc.ui.UIRegistry.setEmbeddedDBPath;
import static edu.ku.brc.ui.UIRegistry.setMobileEmbeddedDBPath;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DBMSUserMgr.DBSTATUS;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.PermissionInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.init.BaseSetupPanel;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * This is the configuration window for create a new user and new database.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public class DatabasePanel extends BaseSetupPanel
{
    private enum VerifyStatus {OK, CANCELLED, ERROR}
    
    protected final String            PROPNAME    = "PROPNAME";
    protected final String            DBNAME      = "dbName";
    protected final String            HOSTNAME    = "hostName";
    protected final String            DBPWD       = "dbPassword";
    protected final String            DBUSERNAME  = "dbUserName";

    protected JTextField              usernameTxt;
    protected JTextField              passwordTxt;
    protected JTextField              dbNameTxt;
    protected JTextField              hostNameTxt;
    protected JComboBox               drivers;
    
    protected Vector<DatabaseDriverInfo> driverList;
    protected boolean                    doSetDefaultValues;
    
    protected Boolean                 isOK = null;
    protected JLabel                  label;
    protected String                  errorKey = null;
    
    protected JButton                 skipStepBtn;
    protected JLabel                  advLabel;
    
    protected boolean                 checkForProcesses = true;

    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DatabasePanel(final JButton nextBtn, 
                         final JButton prevBtn, 
                         final String  helpContext,
                         final boolean doSetDefaultValues)
    {
        super("DATABASE", helpContext, nextBtn, prevBtn);
        
        this.doSetDefaultValues = doSetDefaultValues;
        
        String header = getResourceString("ENTER_DB_INFO") + ":";

        CellConstraints cc = new CellConstraints();
        
        String rowDef = "p,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", isMobile() ? 5 : 4) + ",10px,p,10px,p,4px,p,4px,p,10px,f:p:g";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", rowDef), this);
        int row = 1;
        
        builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1,row,3,1));row += 2;
        
        usernameTxt     = createField(builder, "IT_USERNAME",  true, row);      row += 2;
        passwordTxt     = createField(builder, "IT_PASSWORD",  true, row, true, null);row += 2;
        if (isMobile())
        {
            dbNameTxt   = createField(builder, "DB_NAME",   true, row);         row += 2;
        } else
        {
            dbNameTxt = createTextField();
        }
        hostNameTxt     = createField(builder, "HOST_NAME", true, row);         row += 2;

        driverList  = DatabaseDriverInfo.getDriversList();
        drivers     = createComboBox(driverList);
        
        // MySQL as the default
        drivers.setSelectedItem(DatabaseDriverInfo.getDriver("MySQL"));
        
        JLabel lbl = createI18NFormLabel("DRIVER", SwingConstants.RIGHT);
        lbl.setFont(bold);
        builder.add(lbl,     cc.xy(1, row));
        builder.add(drivers, cc.xy(3, row));
        row += 2;
        
        label       = UIHelper.createLabel("", SwingConstants.CENTER);
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "20px,p,8px,p"));
        panelPB.add(getProgressBar(), cc.xy(1, 2));
        panelPB.add(label,            cc.xy(1, 4));
        
        builder.add(panelPB.getPanel(), cc.xy(3, row));
        row += 2;
        
        //panelPB.getPanel().setBackground(Color.RED);
        //setBackground(Color.GREEN);
        
        // Advance part of pane
        advLabel    = UIHelper.createI18NLabel("SEC_ADV_MU_DESC", SwingConstants.CENTER);
        skipStepBtn = UIHelper.createI18NButton("ADV_DB_TEST");
        builder.add(advLabel, cc.xyw(3, row, 1)); row += 2;
        
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(skipStepBtn,          cc.xy(2, 1));
        builder.add(tstPB.getPanel(), cc.xyw(3, row, 1)); row += 2;
        
        JComponent helpComponent = createHelpPanel(getBackground(), "database");
        builder.add(helpComponent, cc.xyw(3, row, 1)); row += 2;
        
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
                
                isOK = ok;
                skipStepBtn.setEnabled(true);
                advLabel.setText(getResourceString(ok ? "ADV_DB_OK" : "ADV_DB_ERR"));
                advLabel.setForeground(ok ? Color.BLACK : Color.RED);
                nextBtn.setEnabled(isOK);
            }
        });
        
        if (isMobile())
        {
            skipStepBtn.setVisible(false);
            advLabel.setVisible(false);
        }
        
        progressBar.setVisible(false);

        updateBtnUI();
    }
    
    /**
     * @param bgColor
     * @param htmlFileName
     * @return
     */
    public static JComponent createHelpPanel(final Color bgColor, final String htmlFileName)
    {
        Locale currLocale = Locale.getDefault();
        
        String helpMasterPath = (new File(".")).getAbsolutePath() + File.separator + "../" + "help/securitywiz/" + htmlFileName;
        String fullHelpMasterPath = UIHelper.createLocaleName(currLocale, helpMasterPath, "html");
        
        JEditorPane htmlPane = null;
        try
        {
            File file = new File(fullHelpMasterPath);
            if (!file.exists()) // for testing
            {
                helpMasterPath = (new File(".")).getAbsolutePath() + File.separator + "help/securitywiz/" + htmlFileName;
                fullHelpMasterPath = UIHelper.createLocaleName(currLocale, helpMasterPath, "html");
                file = new File(fullHelpMasterPath);
                System.out.println(file.getCanonicalPath());
            }
            URI url = file.toURI();
            
            htmlPane = new JEditorPane(url.toURL()); //$NON-NLS-1$
            htmlPane.setEditable(false);
            htmlPane.setBackground(bgColor);

        } catch (IOException ex) 
        {
            File file = new File(fullHelpMasterPath);
            String htmlDesc = "";
            try
            {
                htmlDesc = "Error loading help: "+ file.getCanonicalPath();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            htmlPane   = new JEditorPane("text/plain", htmlDesc); //$NON-NLS-1$
        }
        
        JScrollPane scrollPane = UIHelper.createScrollPane(htmlPane, true);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setPreferredSize(new Dimension(400, 400));
        
        return scrollPane;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        props.put(DBUSERNAME,   usernameTxt.getText());
        props.put(DBPWD,        passwordTxt.getText());
        props.put(DBNAME,       dbNameTxt.getText());
        props.put(HOSTNAME,     hostNameTxt.getText());
        props.put("driver",     drivers.getSelectedItem().toString());
        props.put("driverObj",  drivers.getSelectedItem());
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
        }
    }

    /**
     * @param enable
     */
    protected void setUIEnabled(final boolean enable)
    {
        usernameTxt.setEnabled(enable);
        passwordTxt.setEnabled(enable);
        dbNameTxt.setEnabled(enable);
        hostNameTxt.setEnabled(enable);
        drivers.setEnabled(enable);
        skipStepBtn.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        super.setValues(values);
        
        usernameTxt.setText(values.getProperty(DBUSERNAME));
        passwordTxt.setText(values.getProperty(DBPWD));
        dbNameTxt.setText(values.getProperty(DBNAME));
        hostNameTxt.setText(values.getProperty(HOSTNAME));
        
        if (doSetDefaultValues)
        {
            drivers.setSelectedIndex(0);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        super.doingNext();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        //skipStepBtn.setEnabled(true);
        //skipStepBtn.setVisible(true);
    }

    /**
     * 
     */
    protected boolean skipDBCreate()
    {
        getValues(properties);
        
        DBMSUserMgr mgr     = DBMSUserMgr.getInstance();
        String dbUserName   = usernameTxt.getText();
        String dbPwd        = passwordTxt.getText();
        String hostName     = hostNameTxt.getText();
        String databaseName = dbNameTxt.getText();
        
        // Set up the DBConnection for later
        DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)drivers.getSelectedItem();
        String newConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, "", dbUserName, dbPwd, driverInfo.getName());
        DBConnection.checkForEmbeddedDir(newConnStr);
        
        try
        {
            if (mgr.connectToDBMS(dbUserName, dbPwd, hostName))
            {
                newConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, databaseName, dbUserName, dbPwd, driverInfo.getName());
                
                DBConnection dbc = DBConnection.getInstance();
                dbc.setConnectionStr(newConnStr);
                dbc.setDriver(driverInfo.getDriverClassName());
                dbc.setDialect(driverInfo.getDialectClassName());
                dbc.setDriverName(driverInfo.getName());
                dbc.setServerName(hostName);
                dbc.setUsernamePassword(dbUserName, dbPwd);
                dbc.setDatabaseName(databaseName);
                
                mgr.close();
                return true;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @return
     */
    private boolean checkForPermissions()
    {
        getValues(properties);
        DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
        
        String dbUserName   = usernameTxt.getText();
        String dbPwd        = passwordTxt.getText();
        String hostName     = hostNameTxt.getText();
        
        try
        {
            if (mgr.connectToDBMS(dbUserName, dbPwd, hostName))
            {
                List<PermissionInfo> permAll = new ArrayList<PermissionInfo>(1);
                permAll.add(new PermissionInfo("", "", DBMSUserMgr.PERM_ALL));
                permAll.add(new PermissionInfo("", "", DBMSUserMgr.PERM_GRANT));
				Pair<List<PermissionInfo>, List<PermissionInfo>> missingPerms = PermissionInfo.getMissingPerms( mgr.getPermissionsForCurrentUser(), permAll, "*");
				if (missingPerms.getFirst().size() > 0) {
						String missingPermStr = PermissionInfo.getMissingPermissionString(mgr, missingPerms.getFirst(), "*");
						UIRegistry.showLocalizedError("SEC_MISSING_PERMS", dbUserName, missingPermStr);	
						return false;
				}
                return true;
            }
            
            UIRegistry.showLocalizedError("SEC_UNEX_ERR_LGN");
            
        } finally
        {
            mgr.close();
        }
        return false;
    }

    /**
     * 
     */
    public void createDB()
    {
        getValues(properties);
        
        final String databaseName = dbNameTxt.getText();
        final String dbUserName   = usernameTxt.getText();
        final String dbPwd        = passwordTxt.getText();
        final String hostName     = hostNameTxt.getText();
        
        if (isMobile())
        {
            DBConnection.clearMobileMachineDir();
            File tmpDir = DBConnection.getMobileMachineDir(databaseName);
            setEmbeddedDBPath(tmpDir.getAbsolutePath());
        }

        final DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)drivers.getSelectedItem();
        String connStrInitial = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, databaseName, dbUserName, dbPwd, driverInfo.getName());
        //System.err.println(connStrInitial);
        
        DBConnection.checkForEmbeddedDir("createDB - "+connStrInitial);
        
        DBConnection.getInstance().setDriverName(driverInfo.getName());
        DBConnection.getInstance().setServerName(hostName);
        
        VerifyStatus status = verifyDatabase(properties);
        if ((isOK == null || !isOK) && status == VerifyStatus.OK)
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            label.setText(getResourceString("CONN_DB"));
            
            setUIEnabled(false);
            
            DatabasePanel.this.label.setForeground(Color.BLACK);
            
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
            {
                /* (non-Javadoc)
                 * @see javax.swing.SwingWorker#doInBackground()
                 */
                @Override
                protected Object doInBackground() throws Exception
                {
                    isOK = false;
                    
                    DBMSUserMgr mgr = DBMSUserMgr.getInstance();
                    
                    boolean dbmsOK = false;
                    if (driverInfo.isEmbedded())
                    {
                        if (checkForProcesses)
                        {
                            SpecifyDBSecurityWizardFrame.checkForMySQLProcesses();
                            checkForProcesses = false;
                        }
                        
                        if (isMobile())
                        {
                            File mobileTmpDir = DBConnection.getMobileMachineDir();
                            if (!mobileTmpDir.exists())
                            {
                                if (!mobileTmpDir.mkdirs())
                                {
                                    System.err.println("Dir["+mobileTmpDir.getAbsolutePath()+"] didn't get created!");
                                    // throw exception
                                }
                                DBConnection.setCopiedToMachineDisk(true);
                            } 
                        }
                        
                        String newConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, databaseName, dbUserName, dbPwd, driverInfo.getName());
                        if (driverInfo.isEmbedded())
                        {
                            //System.err.println(newConnStr);
                            try
                            {
                                Class.forName(driverInfo.getDriverClassName());
                                
                                // This call will create the database if it doesn't exist
                                DBConnection testDB = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), databaseName, newConnStr, dbUserName, dbPwd);
                                testDB.getConnection(); // Opens the connection
                                
                                if (testDB != null)
                                {
                                    testDB.close();
                                }
                                dbmsOK = true;
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                            DBConnection.getInstance().setDatabaseName(null);
                        }
                    } else if (mgr.connectToDBMS(dbUserName, dbPwd, hostName))
                    {
                        mgr.close();
                        dbmsOK = true;
                    }
                    
                    if (dbmsOK)
                    {
                        firePropertyChange(PROPNAME, 0, 1);
                        
                        try
                        {
                            SpecifySchemaGenerator.generateSchema(driverInfo, 
                                                                  hostName,
                                                                  databaseName,
                                                                  dbUserName, 
                                                                  dbPwd); // false means create new database, true means update
                            
                            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, databaseName);
                            if (connStr == null)
                            {
                                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName,  databaseName);
                            }
                            
                            firePropertyChange(PROPNAME, 0, 2);
                            
                            // tryLogin sets up DBConnection
                            if (UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                                    driverInfo.getDialectClassName(), 
                                                    dbNameTxt.getText(), 
                                                    connStr, 
                                                    dbUserName, 
                                                    dbPwd))
                            {
                                
                                if (!checkEngineCharSet(properties))
                                {
                                    return false;
                                }
                                isOK = true;
                                
                                firePropertyChange(PROPNAME, 0, 3);
                                
                                Thumbnailer thumb = Thumbnailer.getInstance();
                                File thumbFile = XMLHelper.getConfigDir("thumbnail_generators.xml");
                                thumb.registerThumbnailers(thumbFile);
                                thumb.setQuality(.5f);
                                thumb.setMaxSize(128, 128);

                                File attLoc = getAppDataSubDir("AttachmentStorage", true);
                                AttachmentManagerIface attachMgr = new FileStoreAttachmentManager(attLoc);
                                AttachmentUtils.setAttachmentManager(attachMgr);
                                AttachmentUtils.setThumbnailer(thumb);
                                
                            } else
                            {
                                errorKey = "NO_LOGIN_ROOT";
                            }
                        } catch (Exception ex)
                        {
                            errorKey = "DB_UNRECOVERABLE";
                        }
                    } else
                    {
                        errorKey = "NO_CONN_ROOT";
                        mgr.close();
                    }
                    return null;
                }
    
                /* (non-Javadoc)
                 * @see javax.swing.SwingWorker#done()
                 */
                @Override
                protected void done()
                {
                    super.done();
                    
                    setUIEnabled(true);
                    
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    
                    updateBtnUI();
                    
                    if (isOK)
                    {
                        label.setText(getResourceString("DB_CREATED"));
                        setUIEnabled(false);
                        
                    } else
                    {
                        label.setText(getResourceString(errorKey != null ? errorKey : "ERR_CRE_DB"));
                        showLocalizedError(errorKey != null ? errorKey : "ERR_CRE_DB");
                    }
                }
            };
            
            worker.addPropertyChangeListener(
                    new PropertyChangeListener() 
                    {
                        public  void propertyChange(final PropertyChangeEvent evt) 
                        {
                            if (PROPNAME.equals(evt.getPropertyName())) 
                            {
                                String key = null;
                                switch ((Integer)evt.getNewValue())
                                {
                                    case 1  : key = "BLD_SCHEMA";    break;
                                    case 2  : key = "DB_FRST_LOGIN"; break;
                                    case 3  : key = "BLD_CACHE";     break;
                                    default : break;
                                }
                                if (key != null)
                                {
                                    DatabasePanel.this.label.setText(getResourceString(key));
                                }
                            }
                        }
                    });
            worker.execute();
            
        } else if (status == VerifyStatus.ERROR)
        {
            errorKey = "NO_LOGIN_ROOT";
            DatabasePanel.this.label.setText(getResourceString(errorKey));
            DatabasePanel.this.label.setForeground(Color.RED);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    getTopWindow().pack();
                }
            });
        }
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    @Override
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid && (isOK == null || isOK));
        }
    }
    
    /**
     * @param label
     * @param text
     * @param badKeyArg
     * @param errKeyArg
     * @param isPwd
     * @return
     */
    public static  boolean checkForValidText(final JLabel label,
                                             final String text, 
                                             final String badKeyArg, 
                                             final String errKeyArg, 
                                             final boolean isPwd)
    {
        String errKey = null;
        if (!isPwd && !text.isEmpty() && !StringUtils.isAlpha(text.substring(0, 1)))
        {
            errKey = badKeyArg;
            
        } else if (StringUtils.contains(text, ' ') || StringUtils.contains(text, ','))
        {
            errKey = errKeyArg;
        }
        
        if (errKey != null)
        {
            label.setForeground(Color.RED);
            label.setText(getResourceString(errKeyArg));
            label.setVisible(true);
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    @Override
    public boolean isUIValid()
    {
        label.setText("");
        
        if ((!isMobile() || !checkForValidText(label, dbNameTxt.getText(),   "ERR_BAD_DBNAME",  "NO_SPC_DBNAME", false)) ||
            !checkForValidText(label, usernameTxt.getText(), "ERR_BAD_USRNAME", "NO_SPC_USRNAME", false) ||
            !checkForValidText(label, passwordTxt.getText(),  null,             "NO_SPC_PWDNAME", false))
        {
            isOK = false;
        }
        
        return isOK != null && isOK;
    }
    
    // Getters 
    
    public DatabaseDriverInfo getDriver()
    {
        return (DatabaseDriverInfo)drivers.getSelectedItem();
    }

    public String getDbName()
    {
        return dbNameTxt.getText();
    }

    public String getPassword()
    {
        return passwordTxt.getText();
    }

    public String getUsername()
    {
        return usernameTxt.getText();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        
        String pwd = passwordTxt.getText();
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<pwd.length();i++)
        {
            sb.append('*');
        }
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        list.add(new Pair<String, String>(getResourceString("IT_USERNAME"), usernameTxt.getText()));
        list.add(new Pair<String, String>(getResourceString("IT_PASSWORD"), sb.toString()));
        list.add(new Pair<String, String>(getResourceString("DB_NAME"),     dbNameTxt.getText()));
        list.add(new Pair<String, String>(getResourceString("HOST_NAME"),   hostNameTxt.getText()));
        return list;
    }
    
    /**
     * Checks and then asks user if they want to proceed, if DB exists.
     * @param props props
     * @return true if proceeding
     */
    protected VerifyStatus verifyDatabase(final Properties props)
    {
        boolean      isEmbedded   = DBConnection.getInstance().isEmbedded();
        String       databaseName = props.getProperty(DBNAME);
        if (isEmbedded)
        {
            File specifyDataDir = null;
            if (isMobile())
            {
                specifyDataDir = DBConnection.getMobileMachineDir();
                if (specifyDataDir == null)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, new RuntimeException("DBConnection.getMobileTempDir() return null"));
                }
                
                setMobileEmbeddedDBPath(getDefaultMobileEmbeddedDBPath(databaseName));
            } else
            {
                specifyDataDir = DBConnection.getEmbeddedDataDir(); 
            }
            
            if (specifyDataDir != null)
            {
                if (specifyDataDir.exists())
                {
                    boolean isOKay = UIHelper.promptForAction("PROCEED", "CANCEL", "DEL_CUR_DB_TITLE", getLocalizedMessage("DEL_CUR_DB", databaseName));
                    if (isOKay)
                    {
                        try
                        {
                            FileUtils.deleteDirectory(specifyDataDir);
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSecurityWizard.class, new Exception("The Embedded Data Dir has not been set"));
                            return VerifyStatus.ERROR;
                        }
                    } else
                    {
                       return VerifyStatus.CANCELLED; 
                    }
                }
            } else
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSecurityWizard.class, new Exception("The Embedded Data Dir has not been set"));
                return VerifyStatus.ERROR;
            }  
            return VerifyStatus.OK;
        }
        
        // Here is when we check when the database is NOT embedded
        
        switch (isOkToProceed(props))
        {
            case missingDB :
            case ok        : return VerifyStatus.OK;
            
            case error     : return VerifyStatus.ERROR;
            
            case cancelled : return VerifyStatus.CANCELLED;
            
            default        : return VerifyStatus.ERROR;
        }
    }
    
    /**
     * Checks to see if the database already exists and has tables.
     * @param props the props
     * @return true if it exists
     */
    private DBSTATUS isOkToProceed(final Properties props)
    {
        String databaseName = props.getProperty(DBNAME);
        String itUsername   = props.getProperty(DBUSERNAME);
        String itPassword   = props.getProperty(DBPWD);
        String hostName     = props.getProperty(HOSTNAME);
            
        // if db exists (whether it has tables or not) this next call will return ok or cancelled
        // or it will return missingDB or error
        return DBMSUserMgr.isOkToProceed(databaseName, hostName, itUsername, itPassword);
    }
    
    /**
     * Check the engine and charset.
     * @param props the props
     * @return true if it exists
     */
    protected boolean checkEngineCharSet(final Properties props)
    {
        final String databaseName = props.getProperty(DBNAME);
        
        DBMSUserMgr mgr = null;
        try
        {
            String itUsername = props.getProperty(DBUSERNAME);
            String itPassword = props.getProperty(DBPWD);
            String hostName   = props.getProperty(HOSTNAME);
            
            if (!DBConnection.getInstance().isEmbedded())
            {
                mgr = DBMSUserMgr.getInstance();
                
                if (mgr.connectToDBMS(itUsername, itPassword, hostName))
                {
                    if (!mgr.verifyEngineAndCharSet(databaseName))
                    {
                        String errMsg = mgr.getErrorMsg();
                        if (errMsg != null)
                        {
                            Object[] options = { 
                                    getResourceString("CLOSE")
                                  };
                            JOptionPane.showOptionDialog(getTopWindow(), 
                                                                         errMsg, 
                                                                         getResourceString("DEL_CUR_DB_TITLE"), 
                                                                         JOptionPane.OK_OPTION,
                                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        } 
                        return false;
                    }
                    return true;
                }
            } else
            {
                return true;
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSecurityWizard.class, ex);
            
        } finally
        {
            if (mgr != null)
            {
                mgr.close();
            }
        }
        return false;
    }

}
