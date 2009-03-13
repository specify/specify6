/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
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
public class SpecifyDBSetupWizard extends JPanel
{
    private static final Logger log = Logger.getLogger(SpecifyDBSetupWizard.class);
    
    public enum WizardType {Full, Brief}
    
    protected WizardType             wizardType  = WizardType.Full;
    protected WizardListener         listener;
    
    protected boolean                assumeDerby = false;
    protected final String           HOSTNAME    = "localhost";
    protected boolean                doLoginOnly = false;
    
    protected Properties             props       = new Properties();
    
    protected JButton                helpBtn;
    protected JButton                backBtn;
    protected JButton                nextBtn;
    protected JButton                cancelBtn;
    
    protected DisciplinePanel        disciplinePanel;
    protected DatabasePanel          dbPanel;
    protected TreeDefSetupPanel      storageTDPanel;
    protected TreeDefSetupPanel      taxonTDPanel;
    protected TreeDefSetupPanel      geoTDPanel;
    protected DBLocationPanel        locationPanel;
    protected UserInfoPanel          userInfoPanel;
    
    protected int                    step     = 0;
    protected int                    lastStep = 0;
    
    protected boolean                isCancelled;
    protected JPanel                 cardPanel;
    protected CardLayout             cardLayout = new CardLayout();
    protected Vector<BaseSetupPanel> panels     = new Vector<BaseSetupPanel>();
    
    protected String                 setupXMLPath;
    protected JProgressBar           progressBar;
    
    /**
     * @param specify
     */
    public SpecifyDBSetupWizard(final WizardType wizardType,
                                final WizardListener listener)
    {
        super();
        
        this.wizardType = wizardType;
        this.listener   = listener;
        
        UIRegistry.loadAndPushResourceBundle("specifydbsetupwiz");
        
        /*setupXMLPath = UIRegistry.getUserHomeAppDir() + File.separator + "setup_prefs.xml";
        try
        {
            props.loadFromXML(new FileInputStream(new File(setupXMLPath)));
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
        }*/
        
        cardPanel = new JPanel(cardLayout);
        
        cancelBtn  = createButton(UIRegistry.getResourceString("CANCEL"));
        helpBtn    = createButton(UIRegistry.getResourceString("HELP"));
        
        JPanel btnBar;
        backBtn    = createButton(UIRegistry.getResourceString("BACK"));
        nextBtn    = createButton(UIRegistry.getResourceString("NEXT"));
        
        HelpMgr.registerComponent(helpBtn, "SetupSpecifyDB");
        CellConstraints cc = new CellConstraints();
        PanelBuilder bbpb = new PanelBuilder(new FormLayout("f:p:g,p,4px,p,4px,p,4px,p,4px", "p"));
        
        bbpb.add(helpBtn,   cc.xy(2,1));
        bbpb.add(backBtn,   cc.xy(4,1));
        bbpb.add(nextBtn,   cc.xy(6,1));
        
        if (wizardType == WizardType.Full)
        {
            bbpb.add(cancelBtn, cc.xy(8,1));
        }
        btnBar = bbpb.getPanel();

        boolean doTesting = true;
        if (doTesting)
        {
            props.put("hostName",   "localhost");
            props.put("dbName",     "testfish");
            props.put("dbUserName", "Specify");
            props.put("dbPassword", "Specify");
            
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
        }

        props.put("userType", SpecifyUserTypes.UserType.Manager.toString());
        
        UIFieldFormatterMgr.setDoingLocal(true);
        
        if (wizardType == WizardType.Full)
        {
            dbPanel = new DatabasePanel(nextBtn, true);
            panels.add(dbPanel);
        }
        
        if (wizardType == WizardType.Full)
        {
            panels.add(new GenericFormPanel("SA", 
                    "ENTER_SA_INFO", 
                    new String[] { "SA_USERNAME", "SA_PASSWORD"}, 
                    new String[] { "saUserName", "saPassword"}, 
                    nextBtn, true));
    
            userInfoPanel = new UserInfoPanel("AGENT", 
                    "ENTER_COLMGR_INFO", 
                    new String[] { "FIRSTNAME", "LASTNAME", "MIDNAME",       "EMAIL", "USERLOGININFO", "USERNAME", "PASSWORD"}, 
                    new String[] { "firstName", "lastName", "middleInitial", "email", "-", "usrUsername", "usrPassword"}, 
                    nextBtn);
            panels.add(userInfoPanel);
            
            panels.add(new GenericFormPanel("INST", 
                    "ENTER_INST_INFO",
                    new String[] { "NAME",     "ABBREV"}, 
                    new String[] { "instName", "instAbbrev"}, 
                    nextBtn, true));
            
            panels.add(new GenericFormPanel("ADDR", 
                    "ENTER_ADDR_INFO",
                    new String[] { "ADDR1", "ADDR2", "CITY",  "STATE", "COUNTRY", "ZIP", "PHONE"}, 
                    new String[] { "addr1", "addr2", "city", "state", "country", "zip", "phone"}, 
                    new boolean[] {true, false, true, true, true, true, true},
                    nextBtn, true));
            
            storageTDPanel = new TreeDefSetupPanel(StorageTreeDef.class, 
                    getResourceString("Storage"), 
                    "Storage", 
                    "CONFIG_TREEDEF", 
                    nextBtn, 
                    null);
            panels.add(storageTDPanel);
            
            panels.add(new GenericFormPanel("DIV", 
                    "ENTER_DIV_INFO",
                    new String[] { "NAME",    "ABBREV"}, 
                    new String[] { "divName", "divAbbrev"}, 
                    nextBtn, true));
        }
        
        disciplinePanel = new DisciplinePanel(nextBtn);
        panels.add(disciplinePanel);
        
        taxonTDPanel = new TreeDefSetupPanel(TaxonTreeDef.class, 
                                             getResourceString("Taxon"), 
                                             "Taxon", 
                                             "CONFIG_TREEDEF", 
                                             nextBtn, 
                                             disciplinePanel.getDisciplineType().getDisciplineType());
        panels.add(taxonTDPanel);
         
        geoTDPanel = new TreeDefSetupPanel(GeographyTreeDef.class, 
                                           getResourceString("Geography"), 
                                           "Geography", 
                                           "CONFIG_TREEDEF", 
                                           nextBtn, 
                                           disciplinePanel.getDisciplineType().getDisciplineType());
        panels.add(geoTDPanel);

        if (wizardType == WizardType.Full)
        {
            panels.add(new GenericFormPanel("COLLECTION", 
                    "ENTER_COL_INFO",
                    new String[] { "NAME",     "PREFIX", }, 
                    new String[] { "collName", "collPrefix", }, 
                    nextBtn, true));
        }  
        panels.add(new FormatterPickerPanel("CATNOFMT", nextBtn, true));
        panels.add(new FormatterPickerPanel("ACCNOFMT", nextBtn, false));
        
        panels.add(new SummaryPanel("SUMMARY", nextBtn, panels));
         
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
        
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (step < lastStep-1)
                {
                    step++;
                    panels.get(step).doingNext();
                    cardLayout.show(cardPanel, Integer.toString(step));

                    updateBtnBar();
                    if (listener != null)
                    {
                        listener.panelChanged(getResourceString(panels.get(step).getPanelName()+".TITLE"));
                    }

                } else
                {
                    configSetup();
                    if (wizardType == WizardType.Full)
                    {
                        createDBAndMaster();
                        
                    } else if (SpecifyDBSetupWizard.this.listener != null)
                    {
                        SpecifyDBSetupWizard.this.listener.hide();
                        SpecifyDBSetupWizard.this.listener.finished();
                    }
                }
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (SpecifyDBSetupWizard.this.listener != null)
                {
                    SpecifyDBSetupWizard.this.listener.finished();
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

        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"));
        builder.add(cardPanel, cc.xy(1, 1));
        builder.add(btnBar, cc.xy(1, 3));
        
        builder.setDefaultDialogBorder();
        
        setLayout(new BorderLayout());
        PanelBuilder  iconBldr  = new PanelBuilder(new FormLayout("20px, f:p:g,p,f:p:g,8px", "20px,t:p,f:p:g, 8px"));
        JLabel        iconLbl   = new JLabel(IconManager.getIcon("WizardIcon"));
        iconLbl.setVerticalAlignment(SwingConstants.TOP);
        iconBldr.add(iconLbl, cc.xy(2, 3));
        add(iconBldr.getPanel(), BorderLayout.WEST);
        add(builder.getPanel(), BorderLayout.CENTER);
        
        progressBar = new JProgressBar(0, lastStep-1);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);
    }
    
    /**
     * @return
     */
    public DisciplineType getDisciplineType()
    {
        return disciplinePanel.getDisciplineType();
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
        progressBar.setString(String.format("%d", (int)(((step) * 100.0) / (lastStep-1)))+"% Complete"); // I18N

        if (step == lastStep-1)
        {
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText(getResourceString("FINISHED"));
            
        } else
        {
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText(getResourceString("NEXT"));
        }
        backBtn.setEnabled(step > 0); 
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
     * @param fmt
     * @param fileName
     */
    protected boolean saveFormatters(final UIFieldFormatterIFace fmt, final String fileName)
    {
        if (fmt != null)
        {
            StringBuilder sb = new StringBuilder();
            fmt.toXML(sb);
            
            String path = UIRegistry.getAppDataDir() + File.separator + fileName;
            try
            {
                FileUtils.writeStringToFile(new File(path), sb.toString());
                return true;
                
            } catch (IOException ex)
            {
                
            }
        } else
        {
            return true; // null fmtr doesn't mean an error
        }
        return false;
    }
    
    /**
     * Get the values form the panels.
     */
    protected void configSetup()
    {
        try
        {
            for (SetupPanelIFace panel : panels)
            {
                panel.getValues(props);
            }
            //props.storeToXML(new FileOutputStream(new File(setupXMLPath)), "SetUp Props");
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
            
        }
        
        if (wizardType == WizardType.Full)
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
    }

    /**
     * 
     */
    public void createDBAndMaster()
    {
        final ProgressFrame frame = new ProgressFrame("Creating master user ...", "SpecifyLargeIcon");
        frame.getCloseBtn().setVisible(false);
        frame.pack();
        Dimension size = frame.getSize();
        size.width = Math.max(size.width, 500);
        frame.setSize(size);
        UIHelper.centerAndShow(frame);
        
        final SwingWorker worker = new SwingWorker()
        {
            protected boolean isOK = false;
            
            public Object construct()
            {
                System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
                DBMSUserMgr mgr = DBMSUserMgr.getInstance();
                
                DatabaseDriverInfo driverInfo = dbPanel.getDriver();
                String             dbName     = props.getProperty("dbName");
                String             hostName   = props.getProperty("hostName");
                
                String connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Opensys, hostName, dbName);
        
                String saUsername = props.getProperty("saUserName");
                String saPassword = props.getProperty("saPassword");
                
                String itUsername = props.getProperty("dbUserName");
                String itPassword = props.getProperty("dbPassword");
                
                DBConnection dbConn = DBConnection.getInstance();
        
                dbConn.setDriver(driverInfo.getDriverClassName());
                dbConn.setDialect(driverInfo.getDialectClassName());
                dbConn.setConnectionStr(connStr);
                dbConn.setUsernamePassword(itUsername, itPassword);
                dbConn.setSkipDBNameCheck(true);
                
                mgr.setHostName(hostName);
                
                if (mgr.connect(itUsername, itPassword))
                {
                    if (!mgr.doesUserExists(saUsername))
                    {
                        isOK = mgr.createUser(saUsername, saPassword, dbName, DBMSUserMgr.PERM_ALL);
                    } else
                    {
                        isOK = true;
                    }
                } else
                {
                    // No Connect Error
                    isOK = false;
                }
                mgr.close();
                dbConn.setSkipDBNameCheck(false);
                dbConn.close();
                
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                if (isOK)
                {
                    configureDatabase();
                    
                    frame.setVisible(false); // Progress Dialog
                    frame.dispose();
                    
                    if (SpecifyDBSetupWizard.this.listener != null)
                    {
                        SpecifyDBSetupWizard.this.listener.hide();
                    } 
                } else
                {
                    frame.setVisible(false); // Progress Dialog
                    frame.dispose();
                    
                    if (SpecifyDBSetupWizard.this.listener != null)
                    {
                        SpecifyDBSetupWizard.this.listener.hide();
                        SpecifyDBSetupWizard.this.listener.cancelled();
                    } 
                }
            }
        };
        SwingUtilities.invokeLater(new Runnable() {

            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run()
            {
                worker.start();
            }
        });
    }
    
    /**
     * Sets up initial preference settings.
     */
    protected void setupLoginPrefs()
    {
        String encryptedMasterUP = UserAndMasterPasswordMgr.getInstance().encrypt(props.getProperty("saUserName"), 
                props.getProperty("saPassword"), props.getProperty("usrPassword"));

        DatabaseDriverInfo driverInfo = dbPanel.getDriver();
        AppPreferences ap = AppPreferences.getLocalPrefs();
        ap.put("testuser_master.islocal",  "true");
        ap.put("testuser_master.path",     encryptedMasterUP);
        ap.put("login.dbdriver_selected",  driverInfo.getName());
        ap.put("login.username",           props.getProperty("usrUsername"));
        ap.put("login.databases_selected", dbPanel.getDbName());
        ap.put("login.databases",          dbPanel.getDbName());
        ap.put("login.servers",            props.getProperty("hostName"));
        ap.put("login.servers_selected",   props.getProperty("hostName"));
        ap.put("login.rememberuser",       "true");
        
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

       //System.err.println(UIRegistry.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
        try
        {
            final SwingWorker worker = new SwingWorker()
            {
                protected boolean isOK = false;
                
                public Object construct()
                {
                    try
                    {
                        DatabaseDriverInfo driverInfo = dbPanel.getDriver();
                        props.put("driver", driverInfo);
                        
                        if (driverInfo == null)
                        {
                            throw new RuntimeException("Couldn't find driver by name ["+driverInfo+"] in driver list.");
                        }

                        BuildSampleDatabase builder = new BuildSampleDatabase();
                        
                        //builder.getFrame().setIconImage(IconManager.getImage("Specify16", IconManager.IconSize.Std16).getImage());
                        
                        props.put("disciplineType", disciplinePanel.getDisciplineType());
                        
                        boolean proceed = true;
                        if (checkForDatabase(props))
                        {
                            Object[] options = { 
                                    getResourceString("PROCEED"), 
                                    getResourceString("CANCEL")
                                  };
                            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                         UIRegistry.getLocalizedMessage("DEL_CUR_DB", dbPanel.getDbName()), 
                                                                         getResourceString("DEL_CUR_DB_TITLE"), 
                                                                         JOptionPane.YES_NO_OPTION,
                                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            proceed = userChoice == JOptionPane.YES_OPTION;
                            
                        } 

                        if (proceed)
                        {
                            isOK = builder.buildEmptyDatabase(props);
                            
                            if (isOK)
                            {
                                Object catNumFmtObj = props.get("catnumfmt");
                                Object accNumFmtObj = props.get("accnumfmt");
                                
                                UIFieldFormatterIFace catNumFmt = catNumFmtObj instanceof UIFieldFormatterIFace ? (UIFieldFormatterIFace)catNumFmtObj : null;
                                UIFieldFormatterIFace accNumFmt = accNumFmtObj instanceof UIFieldFormatterIFace ? (UIFieldFormatterIFace)accNumFmtObj : null;
                                
                                if (catNumFmt != null)
                                {
                                    saveFormatters(catNumFmt, "catnumfmt.xml");
                                }
                                if (accNumFmt != null)
                                {
                                    saveFormatters(accNumFmt, "accnumfmt.xml");
                                }
                            }

                            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                    getLocalizedMessage("BLD_DONE", getResourceString(isOK ? "BLD_OK" :"BLD_NOTOK")),
                                    getResourceString("COMPLETE"), JOptionPane.INFORMATION_MESSAGE);                                
                        }
                        
                        UIRegistry.popResourceBundle();
                            
                        } catch (Exception ex)
                        {
                            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
                            ex.printStackTrace();
                        }
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    public void finished()
                    {
                        if (isOK)
                        {
                            HibernateUtil.shutdown();
                        }
                        if (listener != null)
                        {
                            listener.hide();
                            listener.finished();
                        }
                    }
                };
                worker.start();
            
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
            }
    }
    
    /**
     * @param properties
     * @return
     */
    private boolean checkForDatabase(final Properties properties)
    {
        final String dbName = properties.getProperty("dbName");
        
        DatabaseDriverInfo driverInfo = (DatabaseDriverInfo)properties.get("driver");
        
        try
        {
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, 
                                                         properties.getProperty("hostName"), 
                                                         dbName);
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, properties.getProperty("hostName"),  dbName);
            }
            String userName = properties.getProperty("dbUserName");
            String password = properties.getProperty("dbPassword");
            
            if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    dbName, 
                    connStr, 
                    userName, 
                    password))
            {
                return false;
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
            return false;
        }
        return true;
    }
    
    //-------------------------------------------------
    public interface WizardListener
    {
        public abstract void panelChanged(String title);
        
        public abstract void cancelled();
        
        public abstract void hide();
        
        public abstract void finished();
    }
    
}
