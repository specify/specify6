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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
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
import org.hibernate.LockMode;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
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
    
    public enum WizardType {Institution, Division, Discipline, Collection}
    
    protected WizardType             wizardType  = WizardType.Institution;
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
    protected GenericFormPanel       accessionPanel;
    protected FormatterPickerPanel   accessionPickerGbl;
    protected FormatterPickerPanel   accessionPickerCol;
    protected FormatterPickerPanel   catNumPicker;
    
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
    public SpecifyDBSetupWizard(final WizardType wizardType,
                                final WizardListener listener)
    {
        super();
        
        this.wizardType = wizardType;
        this.listener   = listener;
        
        UIRegistry.loadAndPushResourceBundle("specifydbsetupwiz");
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
            
            props.put("addtaxon",   true);
        } else
        {
            props.put("hostName",   "localhost");
            props.put("dbName",     "specify");
        }

        props.put("userType", SpecifyUserTypes.UserType.Manager.toString());
        
        UIFieldFormatterMgr.setDoingLocal(true);
        
        if (wizardType == WizardType.Institution)
        {
            dbPanel = new DatabasePanel(nextBtn, backBtn, "wizard_mysql_username", true);
            panels.add(dbPanel);
            HelpMgr.registerComponent(helpBtn, dbPanel.getHelpContext());
            
            panels.add(new MasterUserPanel("SA",
                    "ENTER_SA_INFO", 
                    "wizard_master_username",
                    new String[] { "SA_USERNAME", "SA_PASSWORD"}, 
                    new String[] { "saUserName", "saPassword"}, 
                    nextBtn, backBtn, true));
            
            panels.add(new GenericFormPanel("SECURITY", 
                    "SECURITY_INFO",
                    "wizard_security_on",
                    new String[] { "SECURITY_ON"}, 
                    new String[] { "security_on"},
                    new String[] { "checkbox"},
                    nextBtn, backBtn, true));

    
            userInfoPanel = new UserInfoPanel("AGENT", 
                    "ENTER_COLMGR_INFO", 
                    "wizard_create_it_user",
                    new String[] { "FIRSTNAME", "LASTNAME", "MIDNAME",       "EMAIL",  null,  "USERLOGININFO", "USERNAME",    "PASSWORD"}, 
                    new String[] { "firstName", "lastName", "middleInitial", "email",  " ",   "-",             "usrUsername",  "usrPassword"}, 
                    new boolean[] { true,       true,       false,            true,    true,  false,           true,           true},
                    nextBtn, backBtn);
            panels.add(userInfoPanel);
            
            panels.add(new GenericFormPanel("INST", 
                    "ENTER_INST_INFO",
                    "wizard_create_institution",
                    new String[]  { "NAME",     "ABBREV",     null,  "INST_ADDR", "ADDR1", "ADDR2", "CITY",  "STATE", "COUNTRY", "ZIP", "PHONE"}, 
                    new String[]  { "instName", "instAbbrev", " ",   "-",         "addr1", "addr2", "city",  "state", "country", "zip", "phone"}, 
                    new boolean[] { true,       true,         false,  false,      true,    false,   true,    true,    true,      true,  true},
                    nextBtn, backBtn, true));

            accessionPanel = new GenericFormPanel("ACCESSIONGLOBALLY", 
                    "ENTER_ACC_INFO",
                    "wizard_choose_accession_level",
                    new String[] { "ACCGLOBALLY"}, 
                    new String[] { "accglobal"},
                    new String[] { "checkbox"},
                    nextBtn, backBtn, true);
            panels.add(accessionPanel);
            
            if (wizardType == WizardType.Institution)
            {
                accessionPickerGbl = new FormatterPickerPanel("ACCNOFMT", "wizard_create_accession_number", nextBtn, backBtn, false);
                panels.add(accessionPickerGbl);
            }

            storageTDPanel = new TreeDefSetupPanel(StorageTreeDef.class, 
                                                    getResourceString("Storage"), 
                                                    "Storage", 
                                                    "wizard_configure_storage_tree",
                                                    "CONFIG_TREEDEF", 
                                                    nextBtn, 
                                                    backBtn, 
                                                    null);
            panels.add(storageTDPanel);
            
            panels.add(new InstSetupPanel("CREATEINST", 
                    "CREATEINST",
                    "wizard_configure_storage_tree",
                    new String[] { }, 
                    new String[] { },
                    nextBtn, backBtn, true));
        }
        
        if (wizardType == WizardType.Institution ||
            wizardType == WizardType.Division)
        {
            panels.add(new GenericFormPanel("DIV", 
                "ENTER_DIV_INFO",
                "wizard_enter_division",
                new String[] { "NAME",    "ABBREV"}, 
                new String[] { "divName", "divAbbrev"}, 
                nextBtn, backBtn, true));
        }

        if (wizardType == WizardType.Institution || 
            wizardType == WizardType.Division || 
            wizardType == WizardType.Discipline)
        {
            nextBtn.setEnabled(false);
            disciplinePanel = new DisciplinePanel("wizard_choose_discipline_type", nextBtn, backBtn);
            panels.add(disciplinePanel);

            taxonTDPanel = new TreeDefSetupPanel(TaxonTreeDef.class, 
                                                 getResourceString("Taxon"), 
                                                 "Taxon", 
                                                 "wizard_configure_taxon_tree",
                                                 "CONFIG_TREEDEF", 
                                                 nextBtn, 
                                                 backBtn,
                                                 disciplinePanel);
            panels.add(taxonTDPanel);
            
            panels.add(new TaxonLoadSetupPanel("wizard_preload_taxon", nextBtn, backBtn));
             
            geoTDPanel = new TreeDefSetupPanel(GeographyTreeDef.class, 
                                               getResourceString("Geography"), 
                                               "Geography", 
                                               "wizard_configure_geography_tree",
                                               "CONFIG_TREEDEF", 
                                               nextBtn, 
                                               backBtn,
                                               disciplinePanel);
            panels.add(geoTDPanel);
        }

        panels.add(new GenericFormPanel("COLLECTION", 
                    "ENTER_COL_INFO",
                    "wizard_create_collection",
                    new String[] { "NAME",     "PREFIX", }, 
                    new String[] { "collName", "collPrefix", }, 
                    nextBtn, backBtn, true));
        
        catNumPicker = new FormatterPickerPanel("CATNOFMT", "wizard_create_catalog_number", nextBtn, backBtn, true);
        panels.add(catNumPicker);
        
        if (wizardType != WizardType.Institution)
        {
            Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
            if (inst != null && !inst.getIsAccessionsGlobal())
            {
                accessionPickerCol = new FormatterPickerPanel("ACCNOFMT", "wizard_create_accession_number", nextBtn, backBtn, false); 
                panels.add(accessionPickerCol);
            }
        } else
        {
            accessionPickerCol = new FormatterPickerPanel("ACCNOFMT", "wizard_create_accession_number", nextBtn, backBtn, false); 
            panels.add(accessionPickerCol);
        }
        
        panels.add(new SummaryPanel("SUMMARY", "wizard_summary", nextBtn, backBtn, panels));
         
        lastStep = panels.size();
        
        if (backBtn != null)
        {
            backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step > 0)
                    {
                        if (disciplinePanel != null)
                        {
                            DisciplineType disciplineType = disciplinePanel.getDisciplineType();
                            if (disciplineType != null && disciplineType.isPaleo() && 
                                panels.get(step-1) instanceof TaxonLoadSetupPanel)
                            {
                                step--;
                            }
                        }
                        
                        if (panels.get(step-1) == accessionPickerGbl)
                        {
                            if (!((Boolean)props.get("accglobal")))
                            {
                                step--;
                            } 
                        }

                        if (panels.get(step-1) == accessionPickerCol)
                        {
                            boolean isAccGlobal;
                            if (accessionPanel != null)
                            {
                                accessionPanel.getValues(props);
                                isAccGlobal = (Boolean)props.get("accglobal");
                            } else
                            {
                                Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                                isAccGlobal = inst != null && !inst.getIsAccessionsGlobal();
                            }
                            if (isAccGlobal)
                            {
                                step--;
                            } 
                        }

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
                if (step == lastStep-2)
                {
            		SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Component c = SpecifyDBSetupWizard.this.getParent();
		                	while (!(c instanceof Window) && c != null)
		                	{
		                		c = c.getParent();
		                	}
		                	if (c != null)
		                	{
		                		((Window)c).pack();
		                	}
						}
            		});
                }

                if (step < lastStep-1)
                {
                    DisciplineType disciplineType = null;
                    if (disciplinePanel == null)
                    {
                        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
                        disciplineType = DisciplineType.getByName(discipline.getType());
                        
                    } else
                    {
                        disciplineType = disciplinePanel.getDisciplineType();
                    }
                    
                    panels.get(step).getValues(props);
                    panels.get(step).aboutToLeave();
                    
                    if (disciplineType != null && disciplineType.isPaleo() && 
                        panels.get(step) instanceof TreeDefSetupPanel &&
                        ((TreeDefSetupPanel)panels.get(step)).getClassType() == TaxonTreeDef.class)
                    {
                        step++;
                    }
                    
                    if (panels.get(step) == accessionPanel)
                    {
                        accessionPanel.getValues(props);
                        if (!((Boolean)props.get("accglobal")))
                        {
                            step++;
                        }
                    }
                    
                    if (panels.get(step) == catNumPicker)
                    {
                       
                        if (accessionPanel != null)
                        {
                            accessionPanel.getValues(props);
                            boolean isAccGlobal = (Boolean)props.get("accglobal");
                            if (isAccGlobal)
                            {
                                step++;
                            }
                        }
                    }
                    
                    advanceToNextPanel();
                    
                } else
                {
                    nextBtn.setEnabled(false);
                    
                    if (wizardType == WizardType.Institution)
                    {
                        configSetup();
                    
                        configureDatabase();
                    } else
                    {
                        //SpecifyDBSetupWizard.this.listener.hide();
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
                    if (step == lastStep)
                    {
                        SpecifyDBSetupWizard.this.listener.finished();
                    } else
                    {
                        SpecifyDBSetupWizard.this.listener.cancelled();
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
        JLabel        iconLbl   = new JLabel(IconManager.getIcon("WizardIcon"));
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
     * @return the Discipline Type
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
            String key;
            switch (wizardType)
            {
                case Institution : key = "FINISHED"; break;
                case Division    : key = "FINISHED_DIV"; break;
                case Discipline  : key = "FINISHED_DISP"; break;
                case Collection  : key = "FINISHED_COL"; break;
                default          : key = "FINISHED"; break;
            }
            nextBtn.setText(getResourceString(key));
            
        } else
        {
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
     * @param fmt
     * @param prefix
     * @param fileName
     * @return
     */
    protected boolean saveFormatters(final UIFieldFormatterIFace fmt, final String prefix, final String fileName)
    {
        if (fmt != null)
        {
            StringBuilder sb = new StringBuilder();
            fmt.toXML(sb);
            
            String path = UIRegistry.getAppDataDir() + File.separator + (prefix != null ? (prefix + "_") : "")  + fileName;
            try
            {
                FileUtils.writeStringToFile(new File(path), sb.toString());
                return true;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
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
        
        if (wizardType == WizardType.Institution)
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
     * Sets up initial preference settings.
     */
    protected void setupLoginPrefs()
    {
        String userName   = props.getProperty("usrUsername");
        String password   = props.getProperty("usrPassword");
        String saUserName = props.getProperty("saUserName");
        String saPassword = props.getProperty("saPassword");
        
        String encryptedMasterUP = UserAndMasterPasswordMgr.getInstance().encrypt(saUserName, saPassword, password);

        DatabaseDriverInfo driverInfo = dbPanel.getDriver();
        AppPreferences ap = AppPreferences.getLocalPrefs();
        ap.put(userName+"_master.islocal",  "true");
        ap.put(userName+"_master.path",     encryptedMasterUP);
        ap.put("login.dbdriver_selected",  driverInfo.getName());
        ap.put("login.username",           props.getProperty("usrUsername"));
        ap.put("login.databases_selected", dbPanel.getDbName());
        ap.put("login.databases",          dbPanel.getDbName());
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
    public void processDataForNonBuild()
    {
        saveFormatters(); 
    }
    
    /**
     * 
     */
    protected void saveFormatters()
    {
        Object catNumFmtObj = props.get("catnumfmt");
        Object accNumFmtObj = props.get("accnumfmt");
        
        String collectionName = props.getProperty("collName");
        
        Institution inst        = AppContextMgr.getInstance().getClassObject(Institution.class);
        boolean     isAccGlobal = inst != null && inst.getIsAccessionsGlobal();
        
        UIFieldFormatterIFace catNumFmt = catNumFmtObj instanceof UIFieldFormatterIFace ? (UIFieldFormatterIFace)catNumFmtObj : null;
        UIFieldFormatterIFace accNumFmt = accNumFmtObj instanceof UIFieldFormatterIFace ? (UIFieldFormatterIFace)accNumFmtObj : null;
        
        if (catNumFmt != null)
        {
            saveFormatters(catNumFmt, collectionName, "catnumfmt.xml");
        }
        if (accNumFmt != null)
        {
            saveFormatters(accNumFmt, isAccGlobal ? null : collectionName, "accnumfmt.xml");
        }
    }

    /**
     * 
     */
    public void configureDatabase()
    {
        if (wizardType == WizardType.Institution)
        {
            setupLoginPrefs();
        }
        
        if (SpecifyDBSetupWizard.this.listener != null)
        {
            SpecifyDBSetupWizard.this.listener.hide();
        }

        final SwingWorker worker = new SwingWorker()
        {
            protected boolean isOK = false;
            
            public Object construct()
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
                    
                    UIHelper.centerAndShow(progressFrame);
                    
                    
                    if (!UIHelper.tryLogin(driverInfo.getDriverClassName(), 
                                           driverInfo.getDialectClassName(), 
                                           dbName, 
                                           connStr, 
                                           saUserName, 
                                           saPassword))
                    {
                        return isOK = false;
                    }   
                     
                    Session session = HibernateUtil.getCurrentSession();
                    bsd.setSession(session);
                    
                    AppContextMgr  ac             = AppContextMgr.getInstance();
                    Institution    institution    = ac.getClassObject(Institution.class);
                    SpecifyUser    user           = ac.getClassObject(SpecifyUser.class);
                    DisciplineType disciplineType = (DisciplineType)props.get("disciplineType");
                    DataType       dataType       = AppContextMgr.getInstance().getClassObject(DataType.class);
                   
                    session.lock(institution, LockMode.NONE);
                    session.lock(dataType, LockMode.NONE);
                    
                    bsd.setDataType(dataType);
                    
                    @SuppressWarnings("unused")
                    Division division = bsd.createEmptyDivision(institution, disciplineType, user, props, true, true);
                    isOK = division != null;
                    
                    progressFrame.incOverall();
                    
                    if (isOK)
                    {
                        saveFormatters();
                    }
    
                    progressFrame.setVisible(false);
                    progressFrame.dispose();
                    
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                                  getLocalizedMessage("BLD_DONE", getResourceString(isOK ? "BLD_OK" :"BLD_NOTOK")),
                                                  getResourceString("COMPLETE"), JOptionPane.INFORMATION_MESSAGE);                                
                
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
