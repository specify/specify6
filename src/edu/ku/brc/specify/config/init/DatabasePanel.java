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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.XMLHelper;
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
    protected final String            PROPNAME    = "PROPNAME";
    protected final String            DBNAME      = "dbName";
    protected final String            HOSTNAME    = "hostName";
    protected final String            DBPWD       = "dbPassword";
    protected final String            DBUSERNAME  = "dbUserName";

    protected boolean                 assumeDerby = false;
    
    protected JTextField              usernameTxt;
    protected JTextField              passwordTxt;
    protected JTextField              dbNameTxt;
    protected JTextField              hostNameTxt;
    protected JComboBox               drivers;
    
    protected Vector<DatabaseDriverInfo> driverList;
    protected boolean                    doSetDefaultValues;
    
    protected Boolean                 isOK = null;
    protected JButton                 testBtn;
    protected JLabel                  label;
    protected String                  errorKey = null;

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
        
        String rowDef = "p,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 5) + ",10px,p,p:g";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", rowDef), this);
        int row = 1;
        
        builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1,row,3,1));row += 2;
        
        usernameTxt     = createField(builder, "IT_USERNAME",  true, row);      row += 2;
        passwordTxt     = createField(builder, "IT_PASSWORD",  true, row, true);row += 2;
        dbNameTxt       = createField(builder, "DB_NAME",   true, row);         row += 2;
        hostNameTxt     = createField(builder, "HOST_NAME", true, row);         row += 2;

        driverList  = DatabaseDriverInfo.getDriversList();
        drivers     = createComboBox(driverList);
        
        // Select Derby or MySQL as the default
        drivers.setSelectedItem(DatabaseDriverInfo.getDriver(assumeDerby ? "Derby" : "MySQL"));
        
        JLabel lbl = createI18NFormLabel("DRIVER", SwingConstants.RIGHT);
        lbl.setFont(bold);
        builder.add(lbl,     cc.xy(1, row));
        builder.add(drivers, cc.xy(3, row));
        row += 2;
        
        label   = UIHelper.createLabel("", SwingConstants.CENTER);
        testBtn = UIHelper.createI18NButton("CREATE_DB");
        
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(testBtn,            cc.xy(2, 1));
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,2px,p:g,f:p:g"));
        panelPB.add(tstPB.getPanel(), cc.xy(1, 1));
        panelPB.add(getProgressBar(), cc.xy(1, 3));
        panelPB.add(label,            cc.xy(1, 5));
        
        builder.add(panelPB.getPanel(), cc.xy(3, row));
        row += 2;
        
        testBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                testConnection();
            }
        });
        
        progressBar.setVisible(false);

        updateBtnUI();
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
            testBtn.setVisible(true);
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
        testBtn.setEnabled(enable);
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
    
    /**
     * 
     */
    public void testConnection()
    {
        if ((isOK == null || !isOK) && verifyDatabase(properties))
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            label.setText(getResourceString("CONN_DB"));
            testBtn.setVisible(false);
            
            setUIEnabled(false);
            
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
            {
                /* (non-Javadoc)
                 * @see javax.swing.SwingWorker#doInBackground()
                 */
                @Override
                protected Object doInBackground() throws Exception
                {
                    isOK = false;
                    
                    DBMSUserMgr mgr   = DBMSUserMgr.getInstance();
                    String dbName     = dbNameTxt.getText();
                    String dbUserName = usernameTxt.getText();
                    String dbPwd      = passwordTxt.getText();
                    String hostName   = hostNameTxt.getText();
                    DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)drivers.getSelectedItem();
                    
                    if (mgr.connectToDBMS(dbUserName, dbPwd, hostName))
                    {
                        mgr.close();
                        
                        firePropertyChange(PROPNAME, 0, 1);
                        
                        try
                        {
                            SpecifySchemaGenerator.generateSchema(driverInfo, 
                                    hostName,
                                    dbName,
                                    dbUserName, 
                                    dbPwd);
                            
                            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
                            if (connStr == null)
                            {
                                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName,  dbName);
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
                                isOK = true;
                                
                                firePropertyChange(PROPNAME, 0, 3);
                                
                                Thumbnailer thumb = new Thumbnailer();
                                File thumbFile = XMLHelper.getConfigDir("thumbnail_generators.xml");
                                thumb.registerThumbnailers(thumbFile);
                                thumb.setQuality(.5f);
                                thumb.setMaxHeight(128);
                                thumb.setMaxWidth(128);

                                File attLoc = UIRegistry.getAppDataSubDir("AttachmentStorage", true);
                                FileUtils.cleanDirectory(attLoc);
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
                    
                    testBtn.setVisible(false);
                    
                    updateBtnUI();
                    
                    if (isOK)
                    {
                        label.setText(UIRegistry.getResourceString("DB_CREATED"));
                        setUIEnabled(false);
                        
                    } else
                    {
                        label.setText(UIRegistry.getResourceString(errorKey != null ? errorKey : "ERR_CRE_DB"));
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
                                    DatabasePanel.this.label.setText(UIRegistry.getResourceString(key));
                                }
                            }
                        }
                    });
            worker.execute();
        }
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
    }
    
    /**
     * @return
     */
    public boolean isUsingDerby()
    {
        DatabaseDriverInfo database = (DatabaseDriverInfo)drivers.getSelectedItem();
        return database.getDialectClassName().equals("org.hibernate.dialect.DerbyDialect");
    }
    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        JTextField[] txtFields = {usernameTxt, passwordTxt, dbNameTxt};
        for (JTextField tf : txtFields)
        {
            if (StringUtils.isEmpty(tf.getText()))
            {
                return false;
            }
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
    protected boolean verifyDatabase(final Properties props)
    {
        boolean proceed = true;
        if (checkForDatabase(props))
        {
            Object[] options = { 
                    getResourceString("PROCEED"), 
                    getResourceString("CANCEL")
                  };
            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                         UIRegistry.getLocalizedMessage("DEL_CUR_DB", props.getProperty(DBNAME)), 
                                                         getResourceString("DEL_CUR_DB_TITLE"), 
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            proceed = userChoice == JOptionPane.YES_OPTION;
            
        } 
        return proceed;
    }
    
    /**
     * Checks to see if the database already exists.
     * @param props the props
     * @return true if it exists
     */
    private boolean checkForDatabase(final Properties props)
    {
        final String dbName = props.getProperty(DBNAME);
        
        DBMSUserMgr mgr = null;
        try
        {
            
            String itUsername = props.getProperty(DBUSERNAME);
            String itPassword = props.getProperty(DBPWD);
            String hostName   = props.getProperty(HOSTNAME);
            
            mgr = DBMSUserMgr.getInstance();
            
            if (mgr.connectToDBMS(itUsername, itPassword, hostName))
            {
                if (mgr.doesDBExists(dbName))
                {
                    mgr.close();
                    
                    if (mgr.connect(itUsername, itPassword, hostName, dbName))
                    {
                        return mgr.doesDBHaveTables();
                    }
                    
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
            
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
