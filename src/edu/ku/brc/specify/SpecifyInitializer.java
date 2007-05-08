/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.specify.tests.BuildSampleDatabase;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.BrowseBtnPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;

/**
 * This class checks the local disk and the user's home directory to see if Specify has been used. 
 * If it could find the "Specify" or ".Specify" directory then is asks the user if they want to 
 * create a new empty database.
 * 
 * XXX I18N - This entire class needs to be Localized!
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 1, 2007
 *
 */
public class SpecifyInitializer
{
    private static final Logger log                = Logger.getLogger(SpecifyInitializer.class);
    
    private static final boolean DO_DEBUG           = false;
    private static final boolean DO_CHANGE_USERNAME = false;
    
    protected final String HOSTNAME = "localhost";
    protected CellConstraints    cc = new CellConstraints();
    
    protected boolean           doLoginOnly        = false;
    protected boolean           assumeDerby        = false;

    /**
     * Constructor.
     */
    public SpecifyInitializer(final boolean doLoginOnly, final boolean assumeDerby)
    {
        this.doLoginOnly = doLoginOnly;
        this.assumeDerby = assumeDerby;

    }

    /**
     * Looks for a Specify directory and if not, then gives the user an opportuntity
     * to create a new database. If it finds a directory then it display the Specify login window.
     * 
     * @param specify a Specify application object
     * @return
     */
    public boolean setup(final Specify specify)
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        if (StringUtils.isEmpty(localPrefs.get("login.dbdriver_selected", null)))
        {
            final SetupDialog specifyInitFrame = new SetupDialog(specify);
            // I can't believe I have to do the following....
            UIHelper.centerWindow(specifyInitFrame);
            specifyInitFrame.pack();
            Dimension size = specifyInitFrame.getSize();
            size.height += 10;
            specifyInitFrame.setSize(size);
            UIHelper.centerAndShow(specifyInitFrame);
            
        } else
        {
            HibernateUtil.shutdown();
            specify.startUp();
        }

        return true;
    }
    
    /**
     * This is the configuration window for create a new user and new database.
     */
    abstract class BaseSetupPanel extends JPanel
    {
        protected KeyAdapter         keyAdapter;
        protected JButton            nextBtn;
        
        public BaseSetupPanel(final JButton nextBtn)
        {
            this.nextBtn = nextBtn;
            
            keyAdapter = new KeyAdapter() {
              public void keyPressed(KeyEvent e)
              {
                  nextBtn.setEnabled(isUIValid());
              }
            };
        }
        
        protected abstract boolean isUIValid();
        
        protected abstract void updateBtnUI();
        
        /**
         * Helper function for creating the UI.
         * @param builder builder
         * @param label the string label
         * @param row the row to place it on
         * @return the create JTextField (or JPasswordField)
         */
        protected JTextField createField(final PanelBuilder builder, final String label, final int row)
        {
            return createField(builder, label, row, false);
        }
        
        /**
         * Helper function for creating the UI.
         * @param builder builder
         * @param label the string label
         * @param row the row to place it on
         * @param isPassword whether to create a password or text field
         * @return the create JTextField (or JPasswordField)
         */
        protected JTextField createField(final PanelBuilder builder, final String label, final int row, final boolean isPassword)
        {
            JTextField txt = isPassword ? new JPasswordField(15) : new JTextField(15);
            builder.add(new JLabel(label+":", SwingConstants.RIGHT), cc.xy(1, row));
            builder.add(txt,                                         cc.xy(3, row));
            //txt.addFocusListener(this);
            //txt.addKeyListener(keyAdapter);
            
            txt.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {updateBtnUI();}
                public void removeUpdate(DocumentEvent e) {updateBtnUI();}
                public void changedUpdate(DocumentEvent e) {updateBtnUI();}
            });
            return txt;
        }
    }
    
    
    /**
     * This is the configuration window for create a new user and new database.
     */
    class NewAgentPanel extends BaseSetupPanel
    {
        protected JTextField firstNameTxt;
        protected JTextField lastNameTxt;
        protected JTextField emailTxt;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public NewAgentPanel(final JButton nextBtn)
        {
            super(nextBtn);

            String header = "Fill in your information:";

            PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,5px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 3)+",p:g"), this);
            int row = 1;
            
            builder.add(new JLabel(header), cc.xywh(1,row,3,1));row += 2;
            
            firstNameTxt    = createField(builder, "First Name", row);row += 2;
            lastNameTxt     = createField(builder, "Last Name",  row);row += 2;
            emailTxt        = createField(builder, "EMail",      row);row += 2;
            
            if (DO_DEBUG) // XXX Debug
            {
                firstNameTxt.setText("Rod");
                lastNameTxt.setText("Spears");
                emailTxt.setText("rods@ku.edu");
            }
            updateBtnUI();
        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected void updateBtnUI()
        {
            if (nextBtn != null)
            {
                nextBtn.setEnabled(isUIValid());
            }
        }

        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected boolean isUIValid()
        {
            JTextField[] txtFields = {firstNameTxt, lastNameTxt, emailTxt};
            for (JTextField tf : txtFields)
            {
                if (StringUtils.isEmpty(tf.getText()))
                {
                    return false;
                }
            }
            return true;
        }

        // Getters 
        public String getEmail()
        {
            return emailTxt.getText();
        }

        public String getFirstName()
        {
            return firstNameTxt.getText();
        }

        public String getLastName()
        {
            return lastNameTxt.getText();
        }
    }

    
    /**
     * This is the configuration window for create a new user and new database.
     */
    class DatabasePanel extends BaseSetupPanel
    {
        protected JTextField         usernameTxt;
        protected JTextField         passwordTxt;
        protected JTextField         dbNameTxt;
        protected JComboBox          drivers;
        protected JComboBox          disciplines;
        
        protected Vector<DatabaseDriverInfo> driverList;
        protected boolean                    doSetDefaultValues;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public DatabasePanel(final JButton nextBtn, final boolean doSetDefaultValues)
        {
            super(nextBtn);
            
            this.doSetDefaultValues = doSetDefaultValues;
            
            String header = "Fill in following information for the database:";

            Vector<Discipline> dispList = new Vector<Discipline>();
            for (Discipline discipline : Discipline.getDisciplineList())
            {
                if (discipline.getType() == 0)
                {
                    dispList.add(discipline);
                }
            }
            
            driverList  = DatabaseDriverInfo.getDriversList();
            drivers     = new JComboBox(driverList);
            disciplines = new JComboBox(dispList);
            
            drivers.getModel().addListDataListener(new ListDataListener() {
                public void intervalAdded(ListDataEvent e)   { adjustLabel(); }
                public void intervalRemoved(ListDataEvent e) { adjustLabel(); }
                public void contentsChanged(ListDataEvent e) { adjustLabel(); }
            });
            
            int numRows = 3 + (doLoginOnly ? 0 : 2);
            PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", numRows)+",p:g"), this);
            int row = 1;
            
            builder.add(new JLabel(header), cc.xywh(1,row,3,1));row += 2;
            
            usernameTxt     = createField(builder, "Username",      row);row += 2;
            passwordTxt     = createField(builder, "Password",      row, true);row += 2;
            dbNameTxt       = createField(builder, "Database Name", row);row += 2;
            
            if (!doLoginOnly)
            {
                builder.add(new JLabel("Discipline:", SwingConstants.RIGHT), cc.xy(1, row));
                builder.add(disciplines, cc.xy(3, row));
                row += 2;
                
                builder.add(new JLabel("Driver:", SwingConstants.RIGHT), cc.xy(1, row));
                builder.add(drivers, cc.xy(3, row));
                row += 2;
            }
            
            // Select Derby or MySQL as the default
            drivers.setSelectedItem(DatabaseDriverInfo.getDriver(assumeDerby ? "Derby" : "MySQL"));

            
            // Select Fish as the default
            for (Discipline discipline : dispList)
            {
                if (discipline.getName().equals("fish"))
                {
                    disciplines.setSelectedItem(discipline);
                }
            }
            
            if (doSetDefaultValues)
            {
                usernameTxt.setText("guest");
                passwordTxt.setText("guest");
                dbNameTxt.setText("WorkBench");  
            }
            
            if (DO_DEBUG) // XXX Debug
            {
                usernameTxt.setText("rods");
                passwordTxt.setText("rods");
                dbNameTxt.setText("WorkBench");
                drivers.setSelectedIndex(0);
            }
            updateBtnUI();
        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected void updateBtnUI()
        {
            if (nextBtn != null)
            {
                nextBtn.setEnabled(isUIValid());
            }
        }
        
        public void adjustLabel()
        {
            if (nextBtn != null)
            {
                nextBtn.setText(isUsingDerby() ? "Next" : "Finished");
            }
        }
        
        public boolean isUsingDerby()
        {
            DatabaseDriverInfo database = (DatabaseDriverInfo)drivers.getSelectedItem();
            return database.getDialectClassName().equals("org.hibernate.dialect.DerbyDialect");
        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected boolean isUIValid()
        {
            JTextField[] txtFields = {usernameTxt, passwordTxt, dbNameTxt};
            for (JTextField tf : txtFields)
            {
                if (StringUtils.isEmpty(tf.getText()))
                {
                    return false;
                }
            }
            return true;
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

        public Discipline getDiscipline()
        {
            return (Discipline)disciplines.getSelectedItem();
        }
    }

    
    /**
     * This is the configuration window for create a new user and new database.
     */
    class DBLocationPanel extends BaseSetupPanel
    {
        protected BrowseBtnPanel             browse;
        
        protected JRadioButton               useCurrentRB;
        protected JRadioButton               useHomeRB;
        protected JRadioButton               useUserDefinedRB;
        protected JTextField                 filePath          = null;
        protected boolean                    localDirOK        = true;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public DBLocationPanel(final JButton nextBtn)
        {
            super(nextBtn);

            
            localDirOK = true;
            File currentPath = new File(UIRegistry.getAppDataDir() + File.separator + "specify_tmp.tmp");
            try
            {
                FileUtils.touch(currentPath);
                currentPath.delete();
                
            } catch (IOException ex)
            {
                localDirOK = false;
            }
            
            //localDirOK = false ; // XXX TESTING
            
            ButtonGroup  grp       = new ButtonGroup();
            useHomeRB = new JRadioButton("<html>Use your home directory: <b>"+UIRegistry.getUserHomeAppDir()+"</b></html>");
            grp.add(useHomeRB);
            useHomeRB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (browse != null)
                    {
                        browse.setEnabled(false);
                    }
                    updateBtnUI();
                }
            });

            int numRows = 3;
            StringBuilder header = new StringBuilder("<html>This step requires you to select a location for the database.");
            //localDirOK = false; // DEBUG
            if (localDirOK)
            {
                header.append("There are three options:</html>");

                useCurrentRB = new JRadioButton("<html>Use your current directory: <b>"+UIRegistry.getDefaultWorkingPath()+"</b></html>");
                grp.add(useCurrentRB);
                useCurrentRB.setSelected(true);
                numRows++;
                
            } else
            {
                header.append("<br>The database cannot be stored on the media you are currently running Workbench from, ");
                header.append("so you can allow it to default to your '<i>home</i>' directory. Or choose a different location.</html>");
                useHomeRB.setSelected(true);
            }
            
            useUserDefinedRB  = new JRadioButton("Use other location:");
            grp.add(useUserDefinedRB);
            useUserDefinedRB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    browse.setEnabled(true);
                    updateBtnUI();
                }
            });
            
            filePath = new JTextField(30);
            filePath.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {updateBtnUI();}
                public void removeUpdate(DocumentEvent e) {updateBtnUI();}
                public void changedUpdate(DocumentEvent e) {updateBtnUI();}
            });
            browse = new BrowseBtnPanel(filePath, true);
            browse.setEnabled(false);

            JLabel       lbl     = new JLabel(header.toString());
            PanelBuilder cmtBldr = new PanelBuilder(new FormLayout("f:min(300px;p):g", "f:p:g"));
            cmtBldr.add(lbl, cc.xy(1,1));

            PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", numRows)+",f:p:g"), this);
            int row = 1;

            builder.add(cmtBldr.getPanel(), cc.xywh(1,row,3,1));row += 2;
            builder.add(useHomeRB, cc.xy(1,row));row += 2;
            if (useCurrentRB != null)
            {
                builder.add(useCurrentRB, cc.xy(1,row));row += 2;
            }
            builder.add(useUserDefinedRB, cc.xy(1,row));row += 2;
            builder.add(browse, cc.xy(1,row));row += 2;

        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected void updateBtnUI()
        {
            nextBtn.setEnabled(isUIValid());
        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected boolean isUIValid()
        {
            if (useUserDefinedRB.isSelected())
            {
                String path = filePath.getText();
                if (StringUtils.isNotEmpty(path))
                {
                    return new File(path).exists();
                }
                return false;
            }
            return true;
        }
        
        public boolean isUsingUserDefinedDirectory()
        {
            return useUserDefinedRB.isSelected();
        }
        
        public boolean isUseHomeDirectory()
        {
            return useHomeRB.isSelected();
        }
        
        public boolean isLocalOKForWriting()
        {
            return localDirOK;
        }
        
        public String getUserDefinedPath()
        {
            return filePath.getText();
        }
    }

    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public class SetupDialog extends JFrame
    {
        protected JButton                helpBtn;
        protected JButton                backBtn;
        protected JButton                nextBtn;
        protected JButton                cancelBtn;
        
        protected NewAgentPanel          agentPanel;
        protected DatabasePanel          userPanel;
        protected DBLocationPanel        locationPanel;
        
        protected int                    step     = 0;
        protected int                    lastStep = 3;
        
        protected boolean                isCancelled;
        protected JPanel                 cardPanel;
        protected CardLayout             cardLayout = new CardLayout();
        protected Vector<BaseSetupPanel> panels     = new Vector<BaseSetupPanel>();
        
        protected Specify                specify;
        
        public SetupDialog(final Specify specify)
        {
            super();
            
            this.specify = specify;
            
            setTitle("Configuring a Database");
            cardPanel = new JPanel(cardLayout);
            
            agentPanel    = new NewAgentPanel(nextBtn);
            userPanel     = new DatabasePanel(nextBtn, true);
            locationPanel = new DBLocationPanel(nextBtn);
            
            // Even though we don't show the extra panels they still have important default values
            panels.add(agentPanel);
            if (DO_CHANGE_USERNAME)
            {
                panels.add(userPanel);
            }
            //panels.add(locationPanel);
            
            lastStep = panels.size();
            
            cancelBtn  = new JButton(UIRegistry.getResourceString("Cancel"));
            helpBtn    = new JButton(UIRegistry.getResourceString("Help"));
            
            JPanel btnBar;
            if (lastStep > 1)
            {
                backBtn    = new JButton("Back");    // XXX I18N
                nextBtn    = new JButton("Next");    // XXX I18N
                
                HelpMgr.registerComponent(helpBtn, "ConfiguringDatabase");
                btnBar = ButtonBarFactory.buildWizardBar(helpBtn, backBtn, nextBtn, cancelBtn);
                
            } else
            {
                nextBtn = new JButton(UIRegistry.getResourceString("OK"));
                btnBar  = ButtonBarFactory.buildOKCancelHelpBar(nextBtn, cancelBtn, helpBtn);
            }
            
            
             
            if (backBtn != null)
            {
                backBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        if (step > 0)
                        {
                            step--;
                            cardLayout.show(cardPanel, Integer.toString(step));
                        }
                        updateBtnBar();
                    }
                });
                
                backBtn.setEnabled(false);
            }
            
            nextBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step == 1 && !userPanel.isUsingDerby())
                    {
                        setVisible(false);
                        configureDatabase();
                        dispose();
                        
                    } else if (step < lastStep-1)
                    {
                        step++;
                        cardLayout.show(cardPanel, Integer.toString(step));
                        updateBtnBar();
                          
                    } else
                    {
                        setVisible(false);
                        configureDatabase();
                        dispose();
                    }
                }
            });
            
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    isCancelled = true;
                    setVisible(false);
                    dispose();
                }
             });

            
            for (int i=0;i<panels.size();i++)
            {
                cardPanel.add(Integer.toString(i), panels.get(i));
            }
            agentPanel.setBorder(BorderFactory.createEtchedBorder());
            cardLayout.show(cardPanel, "0");

            PanelBuilder    builder    = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"));
            builder.add(cardPanel, cc.xy(1, 1));
            builder.add(btnBar, cc.xy(1, 3));
            
            builder.setDefaultDialogBorder();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setContentPane(builder.getPanel());
            
            pack();

        }
        
        protected void updateBtnBar()
        {
            if (step == lastStep-1)
            {
                nextBtn.setEnabled(panels.get(step).isUIValid());
                nextBtn.setText("Finished");
                
            } else
            {
                nextBtn.setEnabled(panels.get(step).isUIValid());
                nextBtn.setText("Next");
            }
            
            backBtn.setEnabled(step > 0); 
        }
        
        protected boolean fillPrefs(final boolean doAutoLogin)
        {
            AppPreferences.getLocalPrefs().putBoolean("login.rememberuser", true);
            AppPreferences.getLocalPrefs().putBoolean("login.rememberpassword", true);
            AppPreferences.getLocalPrefs().putBoolean("login.autologin", doAutoLogin);
            
            AppPreferences.getLocalPrefs().put("login.username", "guest");
            AppPreferences.getLocalPrefs().put("login.password", Encryption.encrypt("guest"));
            
            AppPreferences.getLocalPrefs().put("login.databases", userPanel.getDbName());
            AppPreferences.getLocalPrefs().put("login.servers",   HOSTNAME);
            
            AppPreferences.getLocalPrefs().put("login.servers_selected", HOSTNAME);
            AppPreferences.getLocalPrefs().put("login.dbdriver_selected", userPanel.getDriver().getName());
            AppPreferences.getLocalPrefs().put("login.databases_selected", userPanel.getDbName());
            
            AppPreferences.getLocalPrefs().put("javadb.location", UIRegistry.getJavaDBPath());
            log.debug(UIRegistry.getJavaDBPath());

            if (DO_CHANGE_USERNAME)
            {
                AppPreferences.getLocalPrefs().put("startup.username",  userPanel.getUsername());
                AppPreferences.getLocalPrefs().put("startup.password",  Encryption.encrypt(new String(userPanel.getPassword())));
            }
            
            AppPreferences.getLocalPrefs().put("startup.firstname", agentPanel.getFirstName());
            AppPreferences.getLocalPrefs().put("startup.lastname",  agentPanel.getLastName());
            AppPreferences.getLocalPrefs().put("startup.email",     agentPanel.getEmail());
            
            try
            {
                AppPreferences.getLocalPrefs().flush();
                return true;
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        }
        
        /*
        protected boolean loginAndFixUserName()
        {
            HibernateUtil.shutdown();
            
            DatabaseDriverInfo driverInfo = userPanel.getDriver();
            
            String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Create, HOSTNAME, userPanel.getDbName());
            if (connStr == null)
            {
                connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, HOSTNAME, userPanel.getDbName());
            }
            
            if (UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    userPanel.getDbName(), 
                    connStr, 
                    userPanel.getUsername(), 
                    userPanel.getPassword()))
            {
                
                
                specify.startUp();
                return true;
            }
            
            return false;
        }*/

        
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
         * 
         */
        public void configureDatabase()
        {
            // Clear and Reset Everything!
            AppPreferences.shutdownLocalPrefs();
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
            
            String derbyPath;
            if (locationPanel.isUsingUserDefinedDirectory())
            {
                derbyPath = locationPanel.getUserDefinedPath() + File.separator + "DerbyDatabases";
                
            } else
            {
                if (locationPanel.isUseHomeDirectory())
                {
                    // Copy over the database if we can't use this directory for writing.
                    // if it is being run off of a CD
                    if (!locationPanel.isLocalOKForWriting())
                    {
                        log.debug("WORKING PATH["+UIRegistry.getDefaultWorkingPath()+"]");
                        
                        File derbyDestDir = new File(UIRegistry.getUserHomeDir() + File.separator + "DerbyDatabases");
                        derbyPath = derbyDestDir.getAbsolutePath();
                        
                        String srcDerbyPath = UIRegistry.getJavaDBPath();
                        
                        log.debug("Derby Source Path["+srcDerbyPath+"] "+StringUtils.isNotEmpty(srcDerbyPath));
                        if (StringUtils.isNotEmpty(srcDerbyPath))
                        {
                            File srcDir = new File(srcDerbyPath);
                            log.debug("Derby Source Path ["+srcDir.getAbsoluteFile()+"] "+srcDir.exists());
                            if (srcDir.exists())
                            {
                                File destDir = new File("");//destParentDirStr);
                                log.debug("Derby Destination Path ["+destDir.getAbsoluteFile()+"] exists! "+destDir.exists());
                                if (destDir.exists())
                                {
                                    if (!derbyDestDir.exists())
                                    {
                                    	derbyDestDir.mkdir();
                                    }
                                    
                                    try
                                    {
                                        log.debug("Copy Derby Databases from["+srcDir.getAbsoluteFile()+"] to["+derbyDestDir.getAbsoluteFile()+"]");
                                        FileUtils.copyDirectory(srcDir, derbyDestDir);
                                        UIRegistry.setJavaDBDir(derbyDestDir.getAbsolutePath());
                                        
                                    } catch (Exception ex)
                                    {
                                        log.error(ex);
                                    }
                                } else
                                {
                                    log.error("Dir ["+destDir.getAbsoluteFile()+"] DOESN'T exists!");
                                }
                                
                            } else
                            {
                                log.error("Path doesn't exist ["+srcDir.getAbsolutePath()+"]");
                            }
                        } else
                        {
                            log.error("Empty sibling path for ["+srcDerbyPath+"]");
                        }
                    } else
                    {
                        derbyPath = UIRegistry.getJavaDBPath();
                    }
                    
                } else
                {
                    //log.debug((new File(baseAppDir).exists())+" baseAppDir["+baseAppDir+"]");
                    //
                    //derbyPath = baseAppDir + File.separator + "DerbyDatabases";
                	derbyPath = UIRegistry.getJavaDBPath();
                    if (StringUtils.isEmpty(derbyPath))
                    {
                        derbyPath = (new File(UIRegistry.getUserHomeDir() + File.separator + "DerbyDatabases")).getAbsolutePath();
                    }
                	log.debug(" Derby Database Path ["+derbyPath+"]");
                }
            }
            
            UIRegistry.setJavaDBDir(derbyPath);
            log.debug("**** JavaDB Path="+UIRegistry.getJavaDBPath());
            
            // Now initialize
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UIRegistry.getDefaultWorkingPath());
            System.out.println(UIRegistry.getDefaultWorkingPath()+" "+doLoginOnly+" "+assumeDerby);

            if (doLoginOnly && assumeDerby)
            {
                if (fillPrefs(true))
                {
                    specify.startUp();
                }
                
            } else
            {
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
                                DatabaseDriverInfo driverInfo = userPanel.getDriver();
                                if (driverInfo == null)
                                {
                                    throw new RuntimeException("Couldn't find driver by name ["+driverInfo+"] in driver list.");
                                }
    
                                BuildSampleDatabase builder = new BuildSampleDatabase();
                                builder.getFrame().setIconImage(IconManager.getImage("Specify16", IconManager.IconSize.Std16).getImage());
                                isOK = builder.buildEmptyDatabase(driverInfo,
                                                                  HOSTNAME,
                                                                  userPanel.getDbName(),
                                                                  userPanel.getUsername(), 
                                                                  userPanel.getPassword(), 
                                                                  agentPanel.getFirstName(), 
                                                                  agentPanel.getLastName(), 
                                                                  agentPanel.getEmail(),
                                                                  userPanel.getDiscipline());
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                            return null;
                        }
    
                        //Runs on the event-dispatching thread.
                        public void finished()
                        {
                        	log.debug("isOK "+isOK);
                            if (isOK)
                            {
                                if (fillPrefs(false))
                                {
                                    HibernateUtil.shutdown();
                                    specify.startUp();
                                }
                            }
                        }
                    };
                    worker.start();
                
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
}
