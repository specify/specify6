/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.WindowConstants;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    protected SchemaLocalizerPanel schemaLocPanel;
    
    protected JStatusBar           statusBar     = new JStatusBar(new int[] {5});
    
    protected LocalizableIOIFace   localizableIO;

    /**
     * 
     */
    public SchemaLocalizerFrame()
    {
        new MacOSAppHandler(this);
        
        appName             = "Schema Localizer";
        appVersion          = "6.0";
        appBuildVersion     = "200706111309 (SVN: 2291)";
        
        setTitle(appName + " " + appVersion);// + "  -  "+ appBuildVersion);
    }
    
    public void createDisplay()
    {
        buildUI(); 
    }
    
    /**
     * 
     */
    protected void buildUI()
    {
        localizableIO = new SchemaLocalizerXMLHelper();
        localizableIO.load();
        
        LocalizerBasePanel.setLocalizableStrFactory(new LocalizableStrFactory() {
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
            
        });
        
        schemaLocPanel = new SchemaLocalizerPanel(null);
        schemaLocPanel.setLocalizableIO(localizableIO);
        schemaLocPanel.setStatusBar(statusBar);
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIO.didModelChangeDuringLoad());
        
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
        JMenu toolMenu = UIHelper.createMenu(menuBar, "Tools", "T");
        UIHelper.createMenuItem(toolMenu, "Create Resource Files", "C", "", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createResourceFiles();
            }
        });
        
        menuBar.add(schemaLocPanel.getLocaleMenu(this));
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(schemaLocPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setContentPane(mainPanel);
        
        statusBar.setSectionText(0, LocalizerBasePanel.getCurrLocale().getDisplayName());
        
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
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                SchemaLocalizerFrame sla = new SchemaLocalizerFrame();
                
                sla.createDisplay();
                UIHelper.centerAndShow(sla);
            }
        });

    }

}
