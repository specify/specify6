/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jgoodies.looks.plastic.theme.SkyKrupp;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 4, 2007
 *
 */
public class SchemaLocalizerFrame extends LocalizableBaseApp
{
    private static final Logger  log                = Logger.getLogger(SchemaLocalizerFrame.class);
            
    protected SchemaLocalizerPanel schemaLocPanel;
    
    protected JStatusBar           statusBar     = new JStatusBar(new int[] {5,5});
    
    protected LocalizableIOIFace   localizableIO;
    protected Byte                 schemaType;
    protected DBTableIdMgr         tableMgr;

    /**
     * Constructor.
     */
    public SchemaLocalizerFrame(final Byte         schemaType, 
                                final DBTableIdMgr tableMgr)
    {
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
        
        new MacOSAppHandler(this);
        
        appName             = "Schema Localizer";
        appVersion          = "6.0";
        appBuildVersion     = "200706111309 (SVN: 2291)";
        
        setTitle(appName + " " + appVersion);// + "  -  "+ appBuildVersion);
    }
    
    /**
     * 
     */
    public void createDisplay()
    {
        buildUI(); 
    }
    
    /**
     * 
     */
    protected void buildUI()
    {
        localizableIO = new SchemaLocalizerXMLHelper(schemaType, tableMgr);
        localizableIO.load();
        
        LocalizableStrFactory localizableStrFactory = new LocalizableStrFactory() {
            public LocalizableStrIFace create()
            {
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                return str;
            }
            public LocalizableStrIFace create(String text, Locale locale)
            {
                return new SpLocaleItemStr(text, locale); // no initialize needed for this constructor
            }
        };
        
        LocalizerBasePanel.setLocalizableStrFactory(localizableStrFactory);
        SchemaLocalizerXMLHelper.setLocalizableStrFactory(localizableStrFactory);
        
        schemaLocPanel = new SchemaLocalizerPanel(null);
        schemaLocPanel.setLocalizableIO(localizableIO);
        schemaLocPanel.setStatusBar(statusBar);
        schemaLocPanel.setIncludeHiddenUI(true);
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIO.didModelChangeDuringLoad());
        
        statusBar.setSectionText(1, schemaType == SpLocaleContainer.CORE_SCHEMA ? "Full Schema" : "WorkBench Schema");
        
        UIRegistry.setStatusBar(statusBar);
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = UIHelper.createMenu(menuBar, "File", "F");
        JMenuItem saveMenuItem = UIHelper.createMenuItem(fileMenu, "Save", "S", "", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                write();
            }
        });
        saveMenuItem.setEnabled(false);
        
        UIHelper.createMenuItem(fileMenu, "Export", "E", "", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                export();
            }
        });
        
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            
            UIHelper.createMenuItem(fileMenu, "Exit", "x", "", true, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    shutdown();
                }
            });
        }
        /*
        JMenu toolMenu = UIHelper.createMenu(menuBar, "Tools", "T");
        UIHelper.createMenuItem(toolMenu, "Create Resource Files", "C", "", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createResourceFiles();
            }
        });
        */
        
        menuBar.add(SchemaI18NService.getInstance().createLocaleMenu(this, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals("locale"))
                {
                    schemaLocPanel.localeChanged((Locale)evt.getNewValue());
                }
            }
        }));
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(schemaLocPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setContentPane(mainPanel);
        
        statusBar.setSectionText(0, SchemaI18NService.getCurrentLocale().getDisplayName());
        
        schemaLocPanel.setSaveMenuItem(saveMenuItem);
        
        schemaLocPanel.getContainerList().setEnabled(true);
        
        if (localizableIO.didModelChangeDuringLoad())
        {
            saveMenuItem.setEnabled(true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    JFrame frame = new JFrame("Changes To the Schema");
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    
                    JTextPane   tp = new JTextPane();
                    JScrollPane js = new JScrollPane();
                    js.getViewport().add(tp);
                    
                    tp.setContentType("text/html");
                    tp.setText(((SchemaLocalizerXMLHelper)localizableIO).getChangesBuffer());
                    
                    frame.setContentPane(js);
                    frame.pack();
                    frame.setSize(400,500);
                    frame.setVisible(true); 
                }
                
            });
        }
    }
    
    /**
     * 
     */
    protected void shutdown()
    {
        if (schemaLocPanel.hasChanged())
        {
            int rv = JOptionPane.showConfirmDialog(this, "Save changes?", "Save Changes", JOptionPane.YES_NO_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                write();
            }
        }
        
        //helper.dumpAsNew(panel.getTables());
        
        setVisible(false);
        System.exit(0);
    }
    
    /**
     * @return
     */
    protected void write()
    {
        statusBar.setText("Saving...");
        statusBar.paintImmediately(statusBar.getBounds());
        
        schemaLocPanel.getAllDataFromUI();
        
        if (localizableIO.save())
        {
            schemaLocPanel.setHasChanged(false);
            statusBar.setText("Saved.");
            
        } else
        {
            statusBar.setText("There was an error saving.");
        }
    }
    
    /**
     * Export data 
     */
    protected void export()
    {
        statusBar.setText("Exporting...");
        statusBar.paintImmediately(statusBar.getBounds());
        
        schemaLocPanel.getAllDataFromUI();
        
        File outDir = new File(UIRegistry.getUserHomeDir() + File.separator + "schemas");
        if (outDir.exists())
        {
            if (!outDir.isDirectory())
            {
                JOptionPane.showMessageDialog(this, "There is a file named at: "+outDir.getAbsolutePath() + " is not a directory. Please deleted it and try again.",
                        "Error Exporting", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            
        } else
        {
            if (outDir.mkdir())
            {
                
                
            } else
            {
                JOptionPane.showMessageDialog(this, "There was a problem creating directory: "+outDir.getAbsolutePath() + " Please check your permissions and make sure this can be created, or create it yourself.",
                        "Error Exporting", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        if (localizableIO.exportToDirectory(outDir))
        {
            JOptionPane.showMessageDialog(this, "The Schema was exported to: "+outDir.getAbsolutePath(),
                    "Exported", JOptionPane.INFORMATION_MESSAGE);
            statusBar.setText("Exported.");
            
        } else
        {
            statusBar.setText("There was an error exporting.");
        }
    }
    
    
    
    /**
     * 
     */
    protected void createResourceFiles()
    {
        localizableIO.createResourceFiles();
        statusBar.setText("Done writing resource file(s)");
    }
    
    /**
     * 
     */
    public class MacOSAppHandler extends Application
    {
        protected WeakReference<SchemaLocalizerFrame> app;

        public MacOSAppHandler(final SchemaLocalizerFrame app)
        {
            this.app = new WeakReference<SchemaLocalizerFrame>(app);

            addApplicationListener(new AppHandler());

            setEnabledPreferencesMenu(false);
        }

        class AppHandler extends ApplicationAdapter
        {
            public void handleAbout(ApplicationEvent event)
            {
                app.get().doAbout();
                event.setHandled(true);
            }

            public void handleAppPrefsMgr(ApplicationEvent event)
            {
                event.setHandled(true);
            }
            
            public void handlePreferences(ApplicationEvent event) 
            {
                event.setHandled(true);
            }

            public void handleQuit(ApplicationEvent event)
            {
                app.get().shutdown();
                event.setHandled(false);  // This is so bizarre that this needs to be set to false
                                          // It seems to work backwards compared to the other calls
             }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]");
        // This is for Windows and Exe4J, turn the args into System Properties
        for (String s : args)
        {
            String[] pairs = s.split("=");
            if (pairs.length == 2)
            {
                log.debug("["+pairs[0]+"]["+pairs[1]+"]");
                if (pairs[0].startsWith("-D"))
                {
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            }
        }

        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }

        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }

        String javadbdir = System.getProperty("javadbdir");
        if (StringUtils.isNotEmpty(javadbdir))
        {
            UIRegistry.setJavaDBDir(javadbdir);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                
//              Set App Name, MUST be done very first thing!
                UIRegistry.setAppName("Specify"); 
                
                // Then set this
              IconManager.setApplicationClass(Specify.class);
              IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml"));
              IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml"));
              IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml"));
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        //UIManager.setLookAndFeel(new GTKLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceRoyale());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                UIFieldFormatterMgr.setDoingLocal(true);

                System.setProperty(SchemaI18NService.factoryName, "edu.ku.brc.specify.config.SpecifySchemaI18NService");    // Needed for Localization and Schema
                System.setProperty(UIFieldFormatterMgr.factoryName, "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumbering
                
                UIFieldFormatterMgr.setDoingLocal(true); // reads from local disk
               
                Object[] options = { "Full Specify Schema", "WorkBench Schema" };
                int retVal = JOptionPane.showOptionDialog(null, "Which Schema would you like to localize?", "Choose a Schema", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                SchemaLocalizerFrame sla;
                if (retVal == JOptionPane.NO_OPTION)
                {
                    DBTableIdMgr schema = new DBTableIdMgr(false);
                    schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
                    sla = new SchemaLocalizerFrame(SpLocaleContainer.WORKBENCH_SCHEMA, schema);
                    
                } else
                {
                    sla = new SchemaLocalizerFrame(SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
                }
                
                sla.createDisplay();
                sla.pack();
                Dimension size = sla.getSize();
                size.width += 250;
                sla.setSize(size);
                UIHelper.centerAndShow(sla);
            }
        });

    }

}
