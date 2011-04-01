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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
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
public class InstSetupPanel extends GenericFormPanel
{
    protected String                  propName = "next";
    protected Boolean                 isOK     = null;
    protected JButton                 testBtn;
    protected JLabel                  label;
    protected DBConnection            conn     = null;
    protected String                  errorKey = null;
    
    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     * @param makeStretchy
     */
    public InstSetupPanel(final String name, 
                          final String title, 
                          final String helpContext, 
                          final String[] labels,
                          final String[] fields, 
                          final Integer[] numCols, 
                          final JButton nextBtn, 
                          final JButton prevBtn,
                          final boolean makeStretchy)
    {
        super(name, title, helpContext, labels, fields, numCols, nextBtn, prevBtn, makeStretchy);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#init(java.lang.String, java.lang.String[], boolean[], java.lang.String[], java.lang.Integer[])
     */
    @Override
    protected void init(final String    title, 
                        final String[]  fields, 
                        final boolean[] required,
                        final String[] types,
                        final Integer[] numColumns)
    {
        super.init(title, fields, required, types, numColumns);

        label   = UIHelper.createLabel("", SwingConstants.CENTER);
        testBtn = UIHelper.createI18NButton("CREATEINST_BTN");
        
        PanelBuilder tstPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tstPB.add(testBtn,            cc.xy(2, 1));
        
        PanelBuilder panelPB = new PanelBuilder(new FormLayout("f:p:g", "20px,p,2px,p,2px,p:g,f:p:g"));
        panelPB.add(tstPB.getPanel(), cc.xy(1, 2));
        panelPB.add(getProgressBar(), cc.xy(1, 4));
        panelPB.add(label,            cc.xy(1, 6));
        
        builder.add(panelPB.getPanel(), cc.xyw(3, row, 2));
        row += 2;
        
        testBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doCreate();
            }
        });
        
        progressBar.setVisible(false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#enablePreviousBtn()
     */
    @Override
    public boolean enablePreviousBtn()
    {
        return isOK == null || !isOK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#getAdditionalRowDefs()
     */
    @Override
    protected String getAdditionalRowDefs()
    {
        return ",p";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        boolean isValid = super.isUIValid();
        
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
        testBtn.setEnabled(enable);
    }
    
    /**
     * 
     */
    protected void doCreate()
    {
        if (isOK == null || !isOK)
        {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            
            setUIEnabled(false);
            
            label.setText(UIRegistry.getResourceString("CONN_DB"));
            
            testBtn.setVisible(false);
            
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
            {
                @Override
                protected Object doInBackground() throws Exception
                {
                    isOK = false;
                    try
                    {
                        getValues(properties);
                        
                        firePropertyChange(propName, 0, 1);
                        
                        conn = DBConnection.getInstance();
                        
                        AppContextMgr.getInstance().setHasContext(true); // override

                        BuildSampleDatabase bsd = new BuildSampleDatabase();
                        
                        bsd.setSession(HibernateUtil.getCurrentSession());
                        
                        isOK = bsd.createEmptyInstitution(properties, false, false, true);
                        
                        AppContextMgr.getInstance().setClassObject(DataType.class, bsd.getDataType());

                        HibernateUtil.closeSession();
                        
                        if (!isOK)
                        {
                            errorKey = "BAD_INST";
                            return null;
                        }                         
                        
                        String userName = properties.getProperty("usrUsername");
                        String password = properties.getProperty("usrPassword");
                        String dbName   = properties.getProperty("dbName");
                        
                        firePropertyChange(propName, 0, 2);
                        
                        isOK = tryLogginIn(userName, password, dbName);
                        if (!isOK)
                        {
                            errorKey = "BAD_LOGIN";
                            return null;
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        
                        errorKey = "INST_UNRECOVERABLE";
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
                    
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    
                    setUIEnabled(true);
                    
                    updateBtnUI();
                    
                    label.setText(UIRegistry.getResourceString(isOK ? "INST_CREATED" : (errorKey != null ? errorKey : "ERR_CRE_INST")));
                    
                    if (isOK)
                    {
                        setUIEnabled(false);
                        prevBtn.setEnabled(false);
                    }
                }
            };
            
            worker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public  void propertyChange(final PropertyChangeEvent evt) {
                            if (propName.equals(evt.getPropertyName())) 
                            {
                                String key = null;
                                switch ((Integer)evt.getNewValue())
                                {
                                    case 1  : key = "CREATING_INST"; break;
                                    case 2  : key = "LOGIN_USER"; break;
                                    default : break;
                                }
                                if (key != null)
                                {
                                    InstSetupPanel.this.label.setText(UIRegistry.getResourceString(key));
                                }
                            }
                        }
                    });
            worker.execute();
        }
    }
    
    /**
     * @param usrName
     * @param pwd
     * @param databaseName is optional (can be null) 
     * @return
     */
    protected boolean tryLogginIn(final String usrName, final String userPwd, final String databaseName)
    {
        String encryptedMasterUP = UserAndMasterPasswordMgr.getInstance().encrypt(
                                        properties.getProperty("saUserName"), 
                                        properties.getProperty("saPassword"), 
                                        userPwd);
        
        AppPreferences ap = AppPreferences.getLocalPrefs();
        ap.put(UserAndMasterPasswordMgr.getIsLocalPrefPath(usrName, databaseName, true),  "true");
        ap.put(UserAndMasterPasswordMgr.getMasterPrefPath(usrName, databaseName, true),     encryptedMasterUP);
        
        DatabaseLoginPanel.MasterPasswordProviderIFace usrPwdProvider = new DatabaseLoginPanel.MasterPasswordProviderIFace()
        {
            @Override
            public boolean hasMasterUserAndPwdInfo(final String username, final String password, final String dbName)
            {
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                {
                    UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
                    
                    return UserAndMasterPasswordMgr.getInstance().hasMasterUsernameAndPassword();
                }
                return false;
            }

            @Override
            public Pair<String, String> getUserNamePassword(final String username, final String password, final String dbName)
            {
                UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
                
                Pair<String, String> usrPwd = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                
                return usrPwd;
            }

            @Override
            public boolean editMasterInfo(final String username, final String dbName, final boolean askForCredentials)
            {
                return UserAndMasterPasswordMgr.getInstance().editMasterInfo(username, dbName, askForCredentials);
            }
        };
        
        Pair<String, String> masterUsrPwd = usrPwdProvider.getUserNamePassword(usrName, userPwd, databaseName);
        
        if (masterUsrPwd != null)
        {
            String hostName = properties.getProperty("hostName");
            
            DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)properties.get("driverObj");
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, databaseName);
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName,  databaseName);
            }
            
            firePropertyChange(propName, 0, 2);
            
            // tryLogin sets up DBConnection
            boolean isLoggedIn = UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                                    driverInfo.getDialectClassName(), 
                                                    databaseName, 
                                                    connStr, 
                                                    masterUsrPwd.first, 
                                                    masterUsrPwd.second);
            return isLoggedIn;
        }
        return false;
    }
    
}
