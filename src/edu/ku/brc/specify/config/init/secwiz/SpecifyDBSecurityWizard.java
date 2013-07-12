/* Copyright (C) 2013, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.config.init.BaseSetupPanel;
import edu.ku.brc.specify.config.init.UserInfoPanel;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 15, 2008
 *
 */
public class SpecifyDBSecurityWizard extends JPanel
{
    private static final Logger log = Logger.getLogger(SpecifyDBSecurityWizard.class);
    
    protected WizardListener         listener;
    
    protected final String           HOSTNAME    = "localhost";
    protected boolean                doLoginOnly = false;
    
    protected Properties             props       = new Properties();
    
    protected JButton                helpBtn;
    protected JButton                backBtn;
    protected JButton                nextBtn;
    protected JButton                cancelBtn;
    
    protected DatabasePanel          dbPanel;
    protected UserInfoPanel          userInfoPanel;
    
    protected int                    step     = 0;
    protected int                    lastStep = 0;
    
    protected boolean                isCancelled;
    protected JPanel                 cardPanel;
    protected CardLayout             cardLayout = new CardLayout();
    protected Vector<BaseSetupPanel> panels     = new Vector<BaseSetupPanel>();
    
    protected String                 setupXMLPath;
    protected JProgressBar           progressBar;
    protected ProgressFrame          progressFrame;
    
    
    /**
     * @param specify
     */
    public SpecifyDBSecurityWizard(final WizardListener listener)
    {
        super();
        
        this.listener   = listener;
        
        System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
        
        /*setupXMLPath = UIRegistry.getUserHomeAppDir() + File.separator + "setup_prefs.xml";
        try
        {
            props.loadFromXML(new FileInputStream(new File(setupXMLPath)));
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
        }*/
        
        HelpMgr.setLoadingPage("Load");
        
        cardPanel = new JPanel(cardLayout);
        
        cancelBtn  = createButton(UIRegistry.getResourceString("CANCEL"));
        helpBtn    = createButton(UIRegistry.getResourceString("HELP"));
        
        JPanel btnBar;
        backBtn    = createButton(UIRegistry.getResourceString("BACK"));
        nextBtn    = createButton(UIRegistry.getResourceString("NEXT"));
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder bbpb = new PanelBuilder(new FormLayout("f:p:g,p,4px,p,4px,p,4px,p,4px", "p"));
        
        bbpb.add(helpBtn,   cc.xy(2,1));
        bbpb.add(backBtn,   cc.xy(4,1));
        bbpb.add(nextBtn,   cc.xy(6,1));
        bbpb.add(cancelBtn, cc.xy(8,1));
        
        btnBar = bbpb.getPanel();

        boolean doTesting = AppPreferences.getLocalPrefs().getBoolean("wizard.defaults", false);
        if (doTesting)
        {
            props.put("hostName",   "localhost");
            props.put("dbName",     "testfish");
            props.put("dbUserName", "root");
            props.put("dbPassword", "root");
            
            props.put("saUserName", "Master");
            props.put("saPassword", "Master");
            
            props.put("firstName", "Test");
            props.put("lastName",  "User");
            props.put("middleInitial", "a");
            props.put("email", "tester@ku.edu");
            props.put("usrUsername", "testuser");
            props.put("usrPassword", "testuser");
    
            props.put("instName", "KU natural History Museum");
            props.put("instAbbrev", "KU-NHM");
    
            props.put("divName", "Fish");
            props.put("divAbbrev", "IT");
    
            props.put("collName", "Fish");
            props.put("collPrefix", "KUFSH");
            
            // Address
            props.put("addr1", "1345 Jayhawk Blvd");
            props.put("addr2", "606 Dyche Hall");
            props.put("city", "Lawrence");
            props.put("state", "KS");
            props.put("country", "USA");
            props.put("zip", "66044");
            props.put("phone", "785-864-5555");
            
            props.put("addtaxon",   true);
        } else
        {
            props.put("hostName",   "localhost");
            props.put("dbName",     "specify");
        }

        props.put("userType", SpecifyUserTypes.UserType.Manager.toString());
        
        UIFieldFormatterMgr.setDoingLocal(true);
        
        dbPanel = new DatabasePanel(nextBtn, backBtn, "security_wiz1", true);
        panels.add(dbPanel);
        HelpMgr.registerComponent(helpBtn, dbPanel.getHelpContext());
        
        MasterLoginPanel masterLoginPanel = new MasterLoginPanel("SA",
                "ENTER_SA_INFO", 
                "security_wiz2",
                new String[] { "SA_USERNAME", "SA_PASSWORD"}, 
                new String[] { "saUserName", "saPassword"}, 
                new Integer[] { 32, 32}, 
                nextBtn, backBtn, true);
        
        panels.add(masterLoginPanel);
        
        panels.add(new UserPanel("SECURITY", 
                "security_wiz3",
                nextBtn, 
                backBtn, 
                true, 
                masterLoginPanel));

        lastStep = panels.size();
        
        if (backBtn != null)
        {
            backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step > 0)
                    {
                        step--;
                        panels.get(step).doingPrev();
                        HelpMgr.registerComponent(helpBtn, panels.get(step).getHelpContext());
                        cardLayout.show(cardPanel, Integer.toString(step));
                    }
                    updateBtnBar();
                    if (listener != null)
                    {
                        listener.panelChanged(getResourceString(panels.get(step).getPanelName()+".TITLE"));
                    }
                }
            });
            
            backBtn.setEnabled(false);
        }
        
        nextBtn.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (step < lastStep-1)
                {
                    panels.get(step).getValues(props);
                    panels.get(step).aboutToLeave();
                    
                    advanceToNextPanel();
                    
                } else
                {
                    panels.get(step).aboutToLeave();
                    nextBtn.setEnabled(false);
                    SpecifyDBSecurityWizard.this.listener.finished();
                    
                }
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (SpecifyDBSecurityWizard.this.listener != null)
                {
                    if (step == lastStep)
                    {
                        SpecifyDBSecurityWizard.this.listener.finished();
                    } else
                    {
                        if (UIHelper.promptForAction("QUIT", "NO", "CANCEL", getResourceString("SURE_QUIT")))
                        {
                            SpecifyDBSecurityWizard.this.listener.cancelled();
                        }
                    }
                }
            }
         });

        for (int i=0;i<panels.size();i++)
        {   
            cardPanel.add(Integer.toString(i), panels.get(i));
            panels.get(i).setValues(props);
        }
        cardLayout.show(cardPanel, "0");
        
        if (dbPanel != null)
        {
            dbPanel.updateBtnUI();
        }

        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"));
        builder.add(cardPanel, cc.xy(1, 1));
        builder.add(btnBar, cc.xy(1, 3));
        
        builder.setDefaultDialogBorder();
        
        setLayout(new BorderLayout());
        PanelBuilder  iconBldr  = new PanelBuilder(new FormLayout("20px, f:p:g,p,f:p:g,8px", "20px,t:p,f:p:g, 8px"));
        JLabel        iconLbl   = new JLabel(IconManager.getIcon(getIconName()));
        iconLbl.setVerticalAlignment(SwingConstants.TOP);
        iconBldr.add(iconLbl, cc.xy(2, 3));
        add(iconBldr.getPanel(), BorderLayout.WEST);
        add(builder.getPanel(), BorderLayout.CENTER);
        
        progressBar = new JProgressBar(0, lastStep-1);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);
        
        panels.get(0).updateBtnUI();
        
    }
    
    /**
     * @return
     */
    public static String getIconName()
    {
        return IconManager.makeIconName("SpecifySecWiz");
    }
    
    /**
     * Advance Wizard to the next panel.
     */
    protected void advanceToNextPanel()
    {
        step++;
        HelpMgr.registerComponent(helpBtn, panels.get(step).getHelpContext());
        panels.get(step).doingNext();

        cardLayout.show(cardPanel, Integer.toString(step));

        updateBtnBar();
        if (listener != null)
        {
            listener.panelChanged(getResourceString(panels.get(step).getPanelName()+".TITLE"));
        }

    }
    
    /**
     * @param listener the listener to set
     */
    public void setListener(WizardListener listener)
    {
        this.listener = listener;
    }

    /**
     * 
     */
    protected void updateBtnBar()
    {
        progressBar.setValue(step);
        progressBar.setString(String.format("%d", (int)(((step) * 100.0) / (lastStep-1))) + 
                              UIRegistry.getResourceString("MSTR_PERCENT_COMPLETE"));

        if (step == lastStep-1)
        {
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText(getResourceString("DONE"));
            cancelBtn.setVisible(false);
            
        } else
        {
            cancelBtn.setVisible(true);
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText(getResourceString("NEXT"));
        }
        backBtn.setEnabled(step > 0 && panels.get(step).enablePreviousBtn()); 
    }

    /**
     * @param path
     * @return
     */
    protected String stripSpecifyDir(final String path)
    {
        String appPath = path;
        int endInx = appPath.indexOf("Specify.app");
        if (endInx > -1)
        {
            appPath = appPath.substring(0, endInx-1);
        }
        return appPath;
    }
    
    /**
     * Get the values form the panels.
     */
    protected void configSetup()
    {
        
        // Clear and Reset Everything!
        //AppPreferences.shutdownLocalPrefs();
        //UIRegistry.setDefaultWorkingPath(null);
        
        log.debug("********** WORK["+UIRegistry.getDefaultWorkingPath()+"]");
        log.debug("********** USER LOC["+stripSpecifyDir(UIRegistry.getAppDataDir())+"]");
        
        String baseAppDir;
        if (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX)
        {
            baseAppDir = stripSpecifyDir(UIRegistry.getAppDataDir());
            
        } else
        {
            baseAppDir = UIRegistry.getDefaultWorkingPath();
        }
        
        baseAppDir = UIHelper.stripSubDirs(baseAppDir, 1);
        UIRegistry.setDefaultWorkingPath(baseAppDir);
        
        log.debug("********** Working path for App ["+baseAppDir+"]");
    }

    /**
     * Sets up initial preference settings.
     */
    protected void setupLoginPrefs()
    {
        String userName   = props.getProperty("usrUsername");
        String password   = props.getProperty("usrPassword");
        String saUserName = props.getProperty("saUserName");
        String saPassword = props.getProperty("saPassword");
        
        String encryptedMasterUP = UserAndMasterPasswordMgr.encrypt(saUserName, saPassword, password);
        
        DatabaseDriverInfo driverInfo = dbPanel.getDriver();
        AppPreferences ap = AppPreferences.getLocalPrefs();
        
        String loginDBPrefName = "login.databases";
        String loginDBs        = ap.get(loginDBPrefName, null);
        if (StringUtils.isNotEmpty(loginDBs))
        {
            TreeSet<String> dbNames = new TreeSet<String>();
            for (String dbNm : StringUtils.splitPreserveAllTokens(loginDBs))
            {
                dbNames.add(dbNm);
            }
            StringBuilder sb = new StringBuilder();
            for (String dbNm : dbNames)
            {
                if (sb.length() > 0) sb.append(',');
                sb.append(dbNm);
            }
            if (sb.length() > 0) sb.append(',');
            sb.append(dbPanel.getDbName());
            loginDBs = sb.toString();
        } else
        {
            loginDBs = dbPanel.getDbName();
        }
        ap.put(userName+"_master.islocal",  "true");
        ap.put(userName+"_master.path",     encryptedMasterUP);
        ap.put("login.dbdriver_selected",  driverInfo.getName());
        ap.put("login.username",           props.getProperty("usrUsername"));
        ap.put("login.databases_selected", dbPanel.getDbName());
        ap.put(loginDBPrefName,            loginDBs);
        ap.put("login.servers",            props.getProperty("hostName"));
        ap.put("login.servers_selected",   props.getProperty("hostName"));
        ap.put("login.rememberuser",       "true");
        ap.put("extra.check",              "true");
        ap.put("version_check.auto",       "true");
        
        try
        {
            ap.flush();
            
        } catch (BackingStoreException ex) {}
    }

    /**
     * @return the props
     */
    public Properties getProps()
    {
        return props;
    }

    /**
     * 
     */
    public void configureDatabase()
    {
        setupLoginPrefs();
        
        if (SpecifyDBSecurityWizard.this.listener != null)
        {
            SpecifyDBSecurityWizard.this.listener.hide();
        }

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            protected boolean isOK = false;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    String             dbName     = props.getProperty("dbName");
                    String             hostName   = props.getProperty("hostName");
                    DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)props.get("driverObj");
                    
                    String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, hostName, dbName);
                    if (connStr == null)
                    {
                        connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, dbName);
                    }
                    
                    String saUserName = props.getProperty("saUserName"); // Master Username
                    String saPassword = props.getProperty("saPassword"); // Master Password

                    BuildSampleDatabase bsd = new BuildSampleDatabase();
                    
                    progressFrame = bsd.createProgressFrame(getResourceString("CREATE_DIV"));
                    progressFrame.adjustProgressFrame();
                    progressFrame.setProcessPercent(true);
                    progressFrame.setOverall(0, 12);
                    UIRegistry.pushWindow(progressFrame);
                    
                    UIHelper.centerAndShow(progressFrame);
                    
                    
                    if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                           driverInfo.getDialectClassName(), 
                                           dbName, 
                                           connStr, 
                                           saUserName, 
                                           saPassword))
                    {
                        isOK = false;
                        return null;
                    }   
                     
                } catch (Exception ex)
                {
                    //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
                    ex.printStackTrace();
                }
                return null;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                if (isOK)
                {
                    if (UIRegistry.isMobile())
                    {
                        DBConnection.setCopiedToMachineDisk(true);
                    }
                    HibernateUtil.shutdown();
                    DBConnection.shutdown();
                }
                if (listener != null)
                {
                    listener.hide();
                    listener.finished();
                }
            }
        };
        worker.execute();
    }
    
    //-------------------------------------------------
    //-- Wizard Listener
    //-------------------------------------------------
    public interface WizardListener
    {
        public abstract void panelChanged(String title);
        
        public abstract void cancelled();
        
        public abstract void hide();
        
        public abstract void finished();

    }
    
}
