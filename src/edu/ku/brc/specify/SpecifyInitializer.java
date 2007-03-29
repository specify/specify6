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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

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
import edu.ku.brc.ui.UICacheManager;
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
    private static final boolean DO_DEBUG = false;
    
    protected final String HOSTNAME = "localhost";
    protected CellConstraints    cc = new CellConstraints();

    /**
     * Constructor.
     */
    public SpecifyInitializer()
    {

    }
    
    /**
     * Sets whether it is using the the current disk or whether to use the user's home directory.
     * @return true if a Specify Directory was found, false if a Specify Data directory couldn't be found
     * and one should be created.
     */
    public static boolean setUseCurrentLocation()
    {
        File file = new File(UICacheManager.getUserDataDir(true)); // Check current ocation first
        //System.err.println(file.getAbsolutePath());
        if (file.exists())
        {
            UICacheManager.setUseCurrentLocation(true);
            
        } else
        {
            file = new File(UICacheManager.getUserDataDir(false)); // Check user data location
            if (file.exists())
            {
                UICacheManager.setUseCurrentLocation(false);
                
            } else
            {
                file = null;
            }
        }
        return file != null;
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
        localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
        
        if (!setUseCurrentLocation() || StringUtils.isEmpty(localPrefs.get("login.dbdriver_selected", null)))
        {
            final SetupDialog specifyInitFrame = new SetupDialog(specify);
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
            builder.add(new JLabel(label+":", JLabel.RIGHT), cc.xy(1, row));
            builder.add(txt,                                 cc.xy(3, row));
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
            nextBtn.setEnabled(isUIValid());
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
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public DatabasePanel(final JButton nextBtn)
        {
            super(nextBtn);
            
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
            
            PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 5)+",p:g"), this);
            int row = 1;
            
            builder.add(new JLabel(header), cc.xywh(1,row,3,1));row += 2;
            
            usernameTxt     = createField(builder, "Username",      row);row += 2;
            passwordTxt     = createField(builder, "Password",      row, true);row += 2;
            dbNameTxt       = createField(builder, "Database Name", row);row += 2;
            
            builder.add(new JLabel("Discipline:", JLabel.RIGHT), cc.xy(1, row));
            builder.add(disciplines,                             cc.xy(3, row));
            row += 2;
            
            builder.add(new JLabel("Driver:", JLabel.RIGHT), cc.xy(1, row));
            builder.add(drivers,                             cc.xy(3, row));
            row += 2;
            
            // Select Fish as the default
            for (Discipline discipline : dispList)
            {
                if (discipline.getName().equals("fish"))
                {
                    disciplines.setSelectedItem(discipline);
                }
            }
            
            if (DO_DEBUG) // XXX Debug
            {
                usernameTxt.setText("rods");
                passwordTxt.setText("rods");
                dbNameTxt.setText("mydata");
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
            nextBtn.setEnabled(isUIValid());
        }
        
        public void adjustLabel()
        {
            nextBtn.setText(isUsingDerby() ? "Next" : "Finished");
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
        protected JCheckBox                  useCurrentDrive;
        protected BrowseBtnPanel             browse;
        
        protected JRadioButton               useCurrentRB;
        protected JRadioButton               useHomeRB;
        protected JRadioButton               useUserDefinedRB;
        protected JTextField                 filePath          = null;
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public DBLocationPanel(final JButton nextBtn)
        {
            super(nextBtn);

            
            boolean localDirOK = true;
            File currentPath = new File(UICacheManager.getUserDataDir(true) + File.separator + "specify_tmp.tmp");
            try
            {
                FileUtils.touch(currentPath);
                currentPath.delete();
                
            } catch (IOException ex)
            {
                localDirOK = false;
            }
            
            ButtonGroup  grp       = new ButtonGroup();
            useHomeRB = new JRadioButton("<html>Use your home directory: <b>"+UICacheManager.getUserDataDir(false)+"</b></html>");
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

                useCurrentRB = new JRadioButton("<html>Use your current directory: <b>"+UICacheManager.getUserDataDir(true)+"</b></html>");
                grp.add(useCurrentRB);
                useCurrentRB.setSelected(true);
                numRows++;
                
            } else
            {
                header.append("<br>The database cannot be stored on the media you are currently running OnRamp from, ");
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
        protected DatabasePanel           userPanel;
        protected DBLocationPanel        locationPanel;
        
        protected int                    step = 0;
        protected int                    LAST_STEP = 3;
        
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
            
            helpBtn    = new JButton("Help");    // XXX I18N
            backBtn    = new JButton("Back");
            nextBtn    = new JButton("Next");
            cancelBtn  = new JButton("Cancel");
            
            HelpMgr.registerComponent(helpBtn, "ConfiguringDatabase");
            
            JPanel btnBar = ButtonBarFactory.buildWizardBar(helpBtn, backBtn, nextBtn, cancelBtn);
             
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
            
            nextBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step == 1 && !userPanel.isUsingDerby())
                    {
                        setVisible(false);
                        configureDatabase();
                        dispose();
                        
                    } else if (step < LAST_STEP-1)
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
            
            backBtn.setEnabled(false);
            
            panels.add(agentPanel    = new NewAgentPanel(nextBtn));
            panels.add(userPanel     = new DatabasePanel(nextBtn));
            panels.add(locationPanel = new DBLocationPanel(nextBtn));
            
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
            if (step == LAST_STEP-1)
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

        public void configureDatabase()
        {
            // CLear and Reset Everything!
            AppPreferences.shutdownLocalPrefs();
            UICacheManager.setDefaultWorkingPath(null);
            
            //UICacheManager.setUseCurrentLocation(dlg.getUseCurrentDrive());
            
            // Now initialize
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
            
            String databasePath = UICacheManager.getDefaultWorkingPath();
            if (locationPanel.isUsingUserDefinedDirectory())
            {
                databasePath = locationPanel.getUserDefinedPath();
            }
            
            System.setProperty("derby.system.home", databasePath + File.separator + "DerbyDatabases");
            //System.err.println(UICacheManager.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
   
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
                        if (isOK)
                        {
                            AppPreferences.getLocalPrefs().putBoolean("login.rememberuser", true);
                            AppPreferences.getLocalPrefs().putBoolean("login.rememberpassword", true);
                            AppPreferences.getLocalPrefs().putBoolean("login.autologin", false);
                            
                            AppPreferences.getLocalPrefs().put("login.username", userPanel.getUsername());
                            AppPreferences.getLocalPrefs().put("login.password", Encryption.encrypt(new String(userPanel.getPassword())));
                            
                            AppPreferences.getLocalPrefs().put("login.databases", userPanel.getDbName());
                            AppPreferences.getLocalPrefs().put("login.servers",   HOSTNAME);
                            
                            AppPreferences.getLocalPrefs().put("login.servers_selected", HOSTNAME);
                            AppPreferences.getLocalPrefs().put("login.dbdriver_selected", userPanel.getDriver().getName());
                            AppPreferences.getLocalPrefs().put("login.databases_selected", userPanel.getDbName());

                            try{
                                AppPreferences.getLocalPrefs().flush();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            
                            HibernateUtil.shutdown();
                            specify.startUp();
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
