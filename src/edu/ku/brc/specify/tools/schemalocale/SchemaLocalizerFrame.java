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
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
import javax.swing.ImageIcon;
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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr;
import edu.ku.brc.specify.config.SpecifyWebLinkMgr;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

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
    
    // used to hold changes to formatters before committing them to DB
    protected DataObjFieldFormatMgr dataObjFieldFormatMgrCache = new DataObjFieldFormatMgr(DataObjFieldFormatMgr.getInstance());
    protected UIFieldFormatterMgr   uiFieldFormatterMgrCache   = new UIFieldFormatterMgr(UIFieldFormatterMgr.getInstance());
    protected SpecifyWebLinkMgr     webLinkMgrCache            = new SpecifyWebLinkMgr((SpecifyWebLinkMgr)SpecifyWebLinkMgr.getInstance());
    
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
        
        appName             = "Schema Localizer"; //$NON-NLS-1$
        appVersion          = UIHelper.getInstall4JInstallString();
        if (appVersion == null)
        {
            appVersion = "Unknown";
        }
        
        UIRegistry.loadAndPushResourceBundle("bld");
        appBuildVersion     = UIRegistry.getResourceString("buildtime");
        UIRegistry.popResourceBundle();
        
        setTitle(appName + " " + appVersion);// + "  -  "+ appBuildVersion); //$NON-NLS-1$
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
        
        schemaLocPanel = new SchemaLocalizerPanel(null, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, webLinkMgrCache);
        schemaLocPanel.setLocalizableIO(localizableIO);
        schemaLocPanel.setStatusBar(statusBar);
        // rods - for now 
        //schemaLocPanel.setIncludeHiddenUI(true);
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIO.didModelChangeDuringLoad());
        
        statusBar.setSectionText(1, schemaType == SpLocaleContainer.CORE_SCHEMA ? getResourceString("SchemaLocalizerFrame.FULL_SCHEMA") : getResourceString("SchemaLocalizerFrame.WB_SCHEMA")); //$NON-NLS-1$ //$NON-NLS-2$
        
        UIRegistry.setStatusBar(statusBar);
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        String title = "File"; //$NON-NLS-1$
        String mneu = "F"; //$NON-NLS-1$
        JMenu fileMenu = UIHelper.createLocalizedMenu(menuBar, title, mneu);
        
        title = "Save"; //$NON-NLS-1$
        mneu = "S"; //$NON-NLS-1$
        JMenuItem saveMenuItem = UIHelper.createLocalizedMenuItem(fileMenu, title, mneu, "", false, new ActionListener() //$NON-NLS-1$
        {
            public void actionPerformed(ActionEvent e)
            {
                write();
            }
        });
        saveMenuItem.setEnabled(false);
        
        title = "Export"; //$NON-NLS-1$
        mneu = "E"; //$NON-NLS-1$
        UIHelper.createLocalizedMenuItem(fileMenu, title, mneu,  "", true, new ActionListener() //$NON-NLS-1$
        {
            public void actionPerformed(ActionEvent e)
            {
                export();
            }
        });
        
        title = "Exit"; //$NON-NLS-1$
        mneu = "x"; //$NON-NLS-1$
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            
            UIHelper.createLocalizedMenuItem(fileMenu, title, mneu,  "", true, new ActionListener() //$NON-NLS-1$
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
                if (evt.getPropertyName().equals("locale")) //$NON-NLS-1$
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
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        ImageIcon helpIcon = IconManager.getIcon("AppIcon",IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$

        AppPrefsCache.setUseLocalOnly(true);
        SpecifyAppPrefs.loadColorAndFormatPrefs();
        
        if (localizableIO.didModelChangeDuringLoad())
        {
            saveMenuItem.setEnabled(true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    JFrame frame = new JFrame(getResourceString("SchemaLocalizerFrame.CHG_TO_SCHEMA")); //$NON-NLS-1$
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
            int rv = JOptionPane.showConfirmDialog(this, getResourceString("SchemaLocalizerFrame.SV_CHNGES"), getResourceString("SchemaLocalizerFrame.CHGS_SAVED"), JOptionPane.YES_NO_OPTION); // I18N //$NON-NLS-1$ //$NON-NLS-2$
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
     * 
     */
    protected void write()
    {
        statusBar.setText(getResourceString("SchemaLocalizerFrame.SAVING")); //$NON-NLS-1$
        statusBar.paintImmediately(statusBar.getBounds());
        
        schemaLocPanel.getAllDataFromUI();
        
        // apply changes to formatters and save them to db
        DataObjFieldFormatMgr.getInstance().applyChanges(dataObjFieldFormatMgrCache);
        UIFieldFormatterMgr.getInstance().applyChanges(uiFieldFormatterMgrCache);
        
        if (localizableIO.save())
        {
            schemaLocPanel.setHasChanged(false);
            statusBar.setText(getResourceString("SchemaLocalizerFrame.SAVED")); //$NON-NLS-1$
            
        } else
        {
            statusBar.setText(getResourceString("SchemaLocalizerFrame.ERROR_SAVING")); //$NON-NLS-1$
        }
    }
    
    /**
     * Export data 
     */
    protected void export()
    {
        statusBar.setText(getResourceString("SchemaLocalizerFrame.EXPORTING")); //$NON-NLS-1$
        statusBar.paintImmediately(statusBar.getBounds());
        
        schemaLocPanel.getAllDataFromUI();
        
        File outDir = new File(UIRegistry.getUserHomeDir() + File.separator + "schemas"); //$NON-NLS-1$
        if (outDir.exists())
        {
            if (!outDir.isDirectory())
            {
                JOptionPane.showMessageDialog(this, getFormattedResStr("SchemaLocalizerFrame.FILE_AT_ERR", outDir.getAbsolutePath()), //$NON-NLS-1$
                        getResourceString("SchemaLocalizerFrame.FILE_EXP"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                return;
            }
            
            
        } else
        {
            if (outDir.mkdir())
            {
                
                
            } else
            {
                JOptionPane.showMessageDialog(this, getFormattedResStr("SchemaLocalizerFrame.FILE_PROBLEM", outDir.getAbsolutePath()), //$NON-NLS-1$
                        getResourceString("SchemaLocalizerFrame.FILE_EXP"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                return;
            }
        }
        
        if (localizableIO.exportToDirectory(outDir))
        {
            JOptionPane.showMessageDialog(this, getFormattedResStr("SchemaLocalizerFrame.EXPORTED_TO", outDir.getAbsolutePath()), //$NON-NLS-1$
                    getResourceString("SchemaLocalizerFrame.EXPORTED"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
            statusBar.setText(getResourceString("SchemaLocalizerFrame.EXPORTED")); //$NON-NLS-1$
            
        } else
        {
            statusBar.setText(getResourceString("SchemaLocalizerFrame.EXPORTING_ERR")); //$NON-NLS-1$
        }
    }
    
    
    
    /**
     * 
     */
    protected void createResourceFiles()
    {
        localizableIO.createResourceFiles();
        statusBar.setText(getResourceString("SchemaLocalizerFrame.DONE_WRT_RES_FILES")); //$NON-NLS-1$
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
        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // This is for Windows and Exe4J, turn the args into System Properties
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                log.debug("["+pairs[0]+"]["+pairs[1]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            }
        }

        // Now check the System Properties
        String appDir = System.getProperty("appdir"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }

        String appdatadir = System.getProperty("appdatadir"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                
//              Set App Name, MUST be done very first thing!
                UIRegistry.setAppName("Specify");  //$NON-NLS-1$
                
                // Then set this
                IconManager.setApplicationClass(Specify.class);
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
                
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerFrame.class, e);
                    e.printStackTrace();
                }
                
                System.setProperty(AppContextMgr.factoryName,          "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
                System.setProperty(SchemaI18NService.factoryName,      "edu.ku.brc.specify.config.SpecifySchemaI18NService");  // Needed for Localization and Schema //$NON-NLS-1$
                System.setProperty(UIFieldFormatterMgr.factoryName,    "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumbering //$NON-NLS-1$
                System.setProperty(WebLinkMgr.factoryName,             "edu.ku.brc.specify.config.SpecifyWebLinkMgr");         // Needed for WebLnkButton //$NON-NLS-1$
                System.setProperty(DataObjFieldFormatMgr.factoryName,   "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$

                SpecifyDataObjFieldFormatMgr.setDoingLocal(true);
                SpecifyUIFieldFormatterMgr.setDoingLocal(true);
                SpecifyWebLinkMgr.setDoingLocal(true);
               
                Object[] options = { getResourceString("SchemaLocalizerFrame.FULL_SCHEMA"), //$NON-NLS-1$ 
                                     getResourceString("SchemaLocalizerFrame.WB_SCHEMA") }; //$NON-NLS-1$
                int retVal = JOptionPane.showOptionDialog(null, getResourceString("SchemaLocalizerFrame.WHICH_SCHEMA"), getResourceString("SchemaLocalizerFrame.CHOOSE_SCHEMA"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$ //$NON-NLS-2$
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                SchemaLocalizerFrame sla;
                if (retVal == JOptionPane.NO_OPTION)
                {
                    DBTableIdMgr schema = new DBTableIdMgr(false);
                    schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml"))); //$NON-NLS-1$
                    sla = new SchemaLocalizerFrame(SpLocaleContainer.WORKBENCH_SCHEMA, schema);
                    
                } else
                {
                    sla = new SchemaLocalizerFrame(SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
                }
                
                AppContextMgr.getInstance().setHasContext(true);
                
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
