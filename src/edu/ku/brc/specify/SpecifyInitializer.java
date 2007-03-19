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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.Discipline;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

/**
 * This class checks the local disk and the user's home directory to see if Specify has been used. 
 * If it could find the "Specify" or ".Specify" directory then is asks the user if they want to 
 * create a new empty database.
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
    private static final boolean DO_DEBUG = true;
    
    protected final String HOSTNAME = "localhost";
    protected CellConstraints    cc         = new CellConstraints();

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
            final SetupDialog dlg = new SetupDialog();
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
            dlg.dispose();
            
            if (!dlg.isCancelled())
            {
                /*
                // CLear and Reset Everything!
                AppPreferences.shutdownLocalPrefs();
                UICacheManager.setDefaultWorkingPath(null);
                
                //UICacheManager.setUseCurrentLocation(dlg.getUseCurrentDrive());
                
                // Now initialize
                localPrefs = AppPreferences.getLocalPrefs();
                localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
                
                System.setProperty("derby.system.home", UICacheManager.getDefaultWorkingPath() + File.separator + "DerbyDatabases");
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
                                DatabaseDriverInfo driverInfo = dlg.getDriver();
                                if (driverInfo == null)
                                {
                                    throw new RuntimeException("Couldn't find driver by name ["+driverInfo+"] in driver list.");
                                }

                                BuildSampleDatabase builder = new BuildSampleDatabase();
                                isOK = builder.buildEmptyDatabase(driverInfo,
                                                                          HOSTNAME,
                                                                          dlg.getDbName(),
                                                                          dlg.getUsername(), 
                                                                          dlg.getPassword(), 
                                                                          dlg.getFirstName(), 
                                                                          dlg.getLastName(), 
                                                                          dlg.getEmail(),
                                                                          dlg.getDiscipline());
                                
                                
                                
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
                                
                                AppPreferences.getLocalPrefs().put("login.username", dlg.getUsername());
                                AppPreferences.getLocalPrefs().put("login.password", Encryption.encrypt(new String(dlg.getPassword())));
                                
                                AppPreferences.getLocalPrefs().put("login.databases", dlg.getDbName());
                                AppPreferences.getLocalPrefs().put("login.servers",   HOSTNAME);
                                
                                AppPreferences.getLocalPrefs().put("login.servers_selected", HOSTNAME);
                                AppPreferences.getLocalPrefs().put("login.dbdriver_selected", dlg.getDriver().getName());
                                AppPreferences.getLocalPrefs().put("login.databases_selected", dlg.getDbName());

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
*/
            } else
            {
                return false;
            }
        } else
        {
            HibernateUtil.shutdown();
            specify.startUp();
        }

        return true;
    }
    
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
        return txt;
    }
    
    interface SetupPanelIFace
    {
        
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
        
    }
    
    
    /**
     * This is the configuration window for create a new user and new database.
     */
    class NewAgentPanel extends BaseSetupPanel
    {
        protected JTextField         firstNameTxt;
        protected JTextField         lastNameTxt;
        protected JTextField         emailTxt;
        
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
    class NewUserPanel extends BaseSetupPanel
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
        public NewUserPanel(final JButton nextBtn)
        {
            super(nextBtn);
            
            String header = "Fill in following information so an empty database can be created:";

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
        
        /**
         * Creates a dialog for entering database name and selecting the appropriate driver.
         */
        public DBLocationPanel(final JButton nextBtn)
        {
            super(nextBtn);
            
            String header = "Fill in following information so an empty database can be created.";

            String comments = "<htmL>Specify creates a user data directory and this can be created on the current drive:<br>&nbsp;&nbsp;&nbsp;&nbsp;<b>%s</b><br>" + 
            "<br>or in your user data directory<br>&nbsp;&nbsp;&nbsp;&nbsp;<b>%s</b><br></html>";

            Vector<Discipline> dispList = new Vector<Discipline>();
            for (Discipline discipline : Discipline.getDisciplineList())
            {
                if (discipline.getType() == 0)
                {
                    dispList.add(discipline);
                }
            }
            
            useCurrentDrive = new JCheckBox("Use Current Drive", true);

            PanelBuilder    cmtBldr    = new PanelBuilder(new FormLayout("f:max(300px;p):g", "f:p:g,10px"));
            cmtBldr.add(new JLabel(String.format(comments, new Object[] { UICacheManager.getUserDataDir(true), UICacheManager.getUserDataDir(false)})), cc.xy(1,1));

            PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 4)+",p:g"), this);
            int row = 1;
            
            builder.add(new JLabel(header), cc.xywh(1,row,3,1));row += 2;
            
            builder.add(new JLabel(" ", JLabel.RIGHT), cc.xy(1, row));row += 2;
            builder.addSeparator("Data Location", cc.xywh(1,row,3,1));row += 2;
            builder.add(cmtBldr.getPanel(), cc.xywh(1,row,3,1));row += 2;
            
            builder.add(useCurrentDrive,               cc.xy(1, row));
            row += 2;
        }
        
        /**
         * Checks all the textfeilds to see if they have text
         * @return true of all fields have text
         */
        protected boolean isUIValid()
        {
            return true;
        }


        public boolean getUseCurrentDrive()
        {
            return useCurrentDrive.isSelected();
        }
    }

    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public class SetupDialog extends JDialog
    {
        protected JButton helpBtn;
        protected JButton backBtn;
        protected JButton nextBtn;
        protected JButton cancelBtn;
        protected JButton finishBtn;
        
        protected NewAgentPanel   agentPanel;
        protected NewUserPanel    userPanel;
        protected DBLocationPanel locationPanel;
        
        protected int             step = 0;
        protected int             LAST_STEP = 3;
        
        protected boolean                isCancelled;
        protected JPanel                 cardPanel;
        protected CardLayout             cardLayout = new CardLayout();
        protected Vector<BaseSetupPanel> panels     = new Vector<BaseSetupPanel>();
        
        public SetupDialog()
        {
            super();
            
            cardPanel = new JPanel(cardLayout);
            
            helpBtn    = new JButton("Help");    // XXX I18N
            backBtn    = new JButton("Back");
            nextBtn    = new JButton("Next");
            cancelBtn  = new JButton("Cancel");
            finishBtn  = new JButton("Finish");
            
            JPanel btnBar = ButtonBarFactory.buildWizardBar(helpBtn, backBtn, nextBtn, finishBtn, cancelBtn);
            
            helpBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                }
             });
             
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
                    if (step < LAST_STEP-1)
                    {
                        step++;
                        cardLayout.show(cardPanel, Integer.toString(step));
                    }
                    updateBtnBar();
                }
            });
            
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    isCancelled = true;
                    setVisible(false);
                }
             });
             
            finishBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    isCancelled = false;
                    setVisible(false);
                }
            });
            
            finishBtn.setEnabled(false);
            backBtn.setEnabled(false);
            
            panels.add(agentPanel    = new NewAgentPanel(nextBtn));
            panels.add(userPanel     = new NewUserPanel(nextBtn));
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
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setContentPane(builder.getPanel());
            pack();

        }
        
        protected void updateBtnBar()
        {
            if (step == LAST_STEP-1)
            {
                nextBtn.setEnabled(false);
                finishBtn.setEnabled(panels.get(step).isUIValid());
                
            } else
            {
                nextBtn.setEnabled(panels.get(step).isUIValid());
                finishBtn.setEnabled(false); 
            }
            
            backBtn.setEnabled(step > 0); 
        }
        
        // Getters 
        public boolean isCancelled()
        {
            return isCancelled;
        }

    }
}
