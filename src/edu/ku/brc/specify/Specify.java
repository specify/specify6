/*
 * Filename:    $RCSfile: Specify.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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

import static edu.ku.brc.specify.helpers.UIHelper.centerAndShow;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static edu.ku.brc.specify.ui.UICacheManager.appendChildPrefName;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;import static edu.ku.brc.specify.ui.UICacheManager.appendChildPrefName;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.plugins.PluginMgr;
import edu.ku.brc.specify.prefs.PrefMainPanel;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.InteractionsTask;
import edu.ku.brc.specify.tasks.LabelsTask;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.tasks.ReportsTask;
import edu.ku.brc.specify.tasks.StartUpTask;
import edu.ku.brc.specify.tasks.StatsTask;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GenericFrame;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.MainPanel;
import edu.ku.brc.specify.ui.PropertyViewer;
import edu.ku.brc.specify.ui.ToolbarLayoutManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostGlassPane;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.helpers.UIHelper;

import org.dom4j.*;
/**
 * Specify Main Application Class
 *
 * @author Rod Spears <rods@ku.edu>
 */
@SuppressWarnings("serial")
public class Specify extends JPanel
{
    private static Log log = LogFactory.getLog(Specify.class);

    // The preferred size of the demo
    private static final int    PREFERRED_WIDTH  = 800;
    private static final int    PREFERRED_HEIGHT = 750;

    // Status Bar
    private JTextField          statusField        = null;
    private JMenuBar            menuBar            = null;
    private JFrame              topFrame           = null;
    private MainPanel           mainPanel          = null;
    
    protected  boolean          hasChanged         = false;
  
    protected Configuration     mConfig            = null;
    protected SessionFactory    mSessionFactory    = null;
    protected Session           mSession           = null;
  
    protected GhostGlassPane    glassPane;
  
    private JLabel splashLabel = null;
  
    // Used only if swingset is an application 
    private JFrame frame = null;
    private JWindow splashWindow = null;
    //private TransparentBackground splashScreen = null;
    //private ImageIcon specifySplashImageIcon   = null;
  
    /*
    private String databaseName = "";
    private String userName = "";
    private String password ="";
    private String hostName ="";
    private boolean useLogonDialog = false;
    */  
    private GraphicsConfiguration grc;
    
    // Global Prefs Registered into the Cache

    

  
    private SpecifyConfig config;
     
    private static Specify  specifyApp       = null;
    
    /*static {
        System.setProperty ("apple.awt.antialiasing", "true");
        System.setProperty ("apple.awt.textantialiasing", "true");
        System.setProperty ("apple.laf.useScreenMenuBar", "true");
        System.setProperty ("apple.awt.brushMetalLook", "true");
        System.setProperty ("com.apple.mrj.application.apple.menu.about.name", "Specify");
        
    }*/

  
     /**
     * Constructor with GraphicsConfiguration
     * @param gc the GraphicsConfiguration
     */
    public Specify(GraphicsConfiguration gc)
    {
        AppPrefs.initialPrefs();
        
        UICacheManager.register(UICacheManager.MAINPANE, this); // important to be done immediately
        
        initPrefs();
        
        
        // Create and throw the splash screen up. Since this will
        // physically throw bits on the screen, we need to do this
        // on the GUI thread using invokeLater.

        createSplashScreen();
        // do the following on the gui thread
        SwingUtilities.invokeLater(new Runnable() {
              public void run() 
              {
                  showSplashScreen();
              }
        });
        
        specifyApp = this;
        
        DBConnection.setUsernamePassword("rods", "rods");
        DBConnection.setDriver("com.mysql.jdbc.Driver");
        DBConnection.setDBName("jdbc:mysql://localhost/demo_fish3");

        try 
        { 
            //System.out.println(System.getProperty("os.name"));
            
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
            }
                
            //UIManager.setLookAndFeel(new PlasticLookAndFeel()); 
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } 
        catch (Exception e) 
        { 
            log.error("Can't change L&F: ", e); 
        }    
      
        grc = gc;
        initialize(gc);
      
        frame = new JFrame(gc);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        UICacheManager.register(UICacheManager.FRAME, frame);

      
        try
        {
            config = SpecifyConfig.getInstance();
            //config.init(this); // do this once
        } catch (Exception e)
        {
            log.error("Error with Configuration", e);
            JOptionPane.showMessageDialog(this, e.toString(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
        }
      
        /*
        if(useLogonDialog){
          DatabaseLogon dbl = new DatabaseLogon();
          databaseName = dbl.getDatabaseName();
          userName = dbl.getUserName();
          password = dbl.getPassword();
          hostName = dbl.getHostName();
        }
       */
        
        

        /*
        HibernateUtil.beginTransaction();
        
        RecordSet recordSet = new RecordSet();
        //recordSet.setId(1L);
        
        recordSet.setName("Catalog Items");
        recordSet.setTableId(1);
        recordSet.setCreated(Calendar.getInstance().getTime());
        
        Set<RecordSetItem> items = new HashSet<RecordSetItem>();
        for (int i=0;i<10;i++)
        {
            RecordSetItem rsi = new RecordSetItem();
            rsi.setRecordId(Integer.toString(i));
            items.add(rsi);
        }
        recordSet.setItems(items);
        
        HibernateUtil.getCurrentSession().saveOrUpdate(recordSet);
        
        //HibernateUtil.getCurrentSession().delete(recordSet);
        
        HibernateUtil.commitTransaction();
        */
        
      
        log.info("Creating configuration "); 
        
        // Create a configuration based on the properties file we've put
        // in the standard place.
        //mConfig = new Configuration();


        try {
            
            SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            validate();
                            hideSplash();
                            
                            add(mainPanel, BorderLayout.CENTER);
                            ContextMgr.getTaskByClass(StartUpTask.class).requestContext();
                            showApp();
                            
                        }
                    });         
         
        } catch (Exception e)
        {
            log.error("Error",e);
        }

    
    }
    
    /**
     * 
     */
    protected void initPrefs()
    {

        boolean skip = false;
        if (skip)
        {
            return;
        }
        
        Preferences appPrefs = UICacheManager.getAppPrefs();
        
        try
        {
            Element root = XMLHelper.readDOMFromConfigDir("prefsInit.xml");
            if (root == null)
            {
                return; // XXX FIXME
            }
            
            List sections = root.selectNodes("/prefs/section");
            for ( Iterator iter = sections.iterator(); iter.hasNext(); ) 
            {
                boolean isNew = false;
                org.dom4j.Element section = (org.dom4j.Element)iter.next();
                
                String      title       = section.attributeValue("title");
                Preferences sectionNode = appPrefs.node(title);
                if (!sectionNode.getBoolean("isApp", false))
                {
                    sectionNode.put("title", title);
                    sectionNode.putBoolean("isApp", true);
                    isNew = true;
                }
                
                List prefs = section.selectNodes("pref");
                for ( Iterator iterPrefs = prefs.iterator(); iterPrefs.hasNext(); ) 
                {
                    org.dom4j.Element pref = (org.dom4j.Element)iterPrefs.next();
                    
                    String prefTitle  = pref.attributeValue("title");
                    String iconName   = pref.attributeValue("icon");
                    String panelClass = pref.attributeValue("panelClass");
                    
                    Preferences prefNode     = sectionNode.node(prefTitle);
                    String      prefTitleStr = prefNode.get("title", null);
                    if (prefTitleStr == null)
                    {
                        prefNode.put("title", prefTitle);
                        prefNode.put("panelClass", panelClass);
                        
                        URL url = IconManager.getImagePath(iconName);
                        if (url != null)
                        {
                            prefNode.put("iconPath", url.toString());
                            
                        } else
                        {
                            log.error("Image name["+iconName+"] not found.");
                        }
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            // XXX FIXME
        }
       
    }
    
    /* 
    public void setHibernateLogonConfig()
    {
        if(useLogonDialog){
            mConfig.setProperty("hibernate.connection.username",userName);
            mConfig.setProperty("hibernate.connection.password",password);
            
          
            if(hostName.indexOf("mysql")!=-1){
                mConfig.setProperty("hibernate.connection.url",hostName + databaseName+ "?useServerPrepStmts=false");//&useOldUTF8Behavior=true");
                mConfig.setProperty("hibernate.dialect","net.sf.hibernate.dialect.MySQLDialect");
                mConfig.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
                }  
            else if(hostName.indexOf("inetdae7")!=-1){//jdbc:inetdae7:localhost?database=KS_fish
                mConfig.setProperty("hibernate.connection.url",hostName + "?database="+databaseName);
                mConfig.setProperty("hibernate.dialect","net.sf.hibernate.dialect.SQLServerDialect");
                mConfig.setProperty("hibernate.connection.driver_class","com.inet.tds.TdsDriver");
            }           
            //else if(hostName.indexOf("sqlserver")!=-1){//jdbc:inetdae7:localhost?database=KS_fish
            //  mConfig.setProperty("hibernate.connection.url",hostName + ";DatabaseName="+databaseName);
            //  mConfig.setProperty("hibernate.dialect","net.sf.hibernate.dialect.SQLServerDialect");
            //  mConfig.setProperty("hibernate.connection.driver_class","com.microsoft.jdbc.sqlserver.SQLServerDriver");
            //} 
                    
          } 
    }
  */
  
  
    /**
     * Determines if this is an applet or application
     */
    public boolean isApplet() 
    {
        return false;
    }
  
    /**
     * Show the spash screen while the rest of the demo loads
     */
    public void createSplashScreen() 
    {
        splashLabel = new JLabel(new ImageIcon(Specify.class.getResource("images/specify_splash.gif")));        
        if(!isApplet()) 
        {
            splashWindow = new JWindow(getFrame());
            splashWindow.getContentPane().add(splashLabel);
            splashWindow.getContentPane().setBackground(Color.WHITE);
            splashWindow.pack();
            Dimension scrSize = getToolkit().getScreenSize();// getFrame().getGraphicsConfiguration().getBounds();
            splashWindow.setLocation(scrSize.width/2 - splashWindow.getSize().width/2,
                                     scrSize.height/2 - splashWindow.getSize().height/2);
            /*
            specifySplashImageIcon = new ImageIcon(Specify.class.getResource("images/specify_splash.gif"));
            JPanel panel = new JPanel() {
                
                public void paintComponent(Graphics g) 
                {
                    if (specifySplashImageIcon != null)
                    {
                        g.drawImage(specifySplashImageIcon.getImage(),0,0,null);
                        //g.setColor(Color.BLACK);
                       // g.draw3DRect(0,0,199,199, true);
                        //System.out.println("Paint RECT");

                    }
                }
                public Dimension getSize()
                {
                    //return new Dimension(200,200);
                    return new Dimension(specifySplashImageIcon.getIconWidth(), specifySplashImageIcon.getIconHeight());
                }
                public Dimension getPreferredSize()
                {
                    return getSize();
                }
            };
            panel.setOpaque(false);
            JFrame splashFrame = new JFrame("Transparent Window");
            splashScreen = new TransparentBackground(splashFrame, specifySplashImageIcon);
            splashFrame.setSize(specifySplashImageIcon.getIconWidth(), specifySplashImageIcon.getIconHeight());
            splashScreen.setSize(specifySplashImageIcon.getIconWidth(), specifySplashImageIcon.getIconHeight());
            
            //splashScreen.setLayout(new BorderLayout());
            //splashScreen.add(panel, BorderLayout.CENTER);
            
            splashFrame.setUndecorated(true);
            //splashFrame.getContentPane().setLayout(new BorderLayout());
            //splashFrame.getContentPane().add(splashScreen, BorderLayout.CENTER);
            splashFrame.pack();
            Dimension scrSize = getToolkit().getScreenSize();// getFrame().getGraphicsConfiguration().getBounds();
            splashFrame.setLocation(scrSize.width/2 - splashFrame.getSize().width/2,
                                    scrSize.height/2 - splashFrame.getSize().height/2);
            System.out.println(splashFrame.getLocation());
            System.out.println(splashFrame.getSize());
            splashFrame.setVisible(true);
            */
        }

    }
  
    public void showSplashScreen()
    {
        if (!isApplet())
        {
            splashWindow.setVisible(true);
            //splashScreen.getFrame().setVisible(true);
        } else
        {
            add(splashLabel, BorderLayout.CENTER);
            validate();
            repaint();
        }
    }

    /**
     * pop down the spash screen
     */
    public void hideSplash()
    {
        if (!isApplet())
        {
            //splashScreen.hideAll();
            splashWindow.setVisible(false);
            splashWindow = null;
            splashLabel = null;
        }
    }

    /**
     * General Method for initializing the class
     *
     */
    private void initialize(GraphicsConfiguration gc)
    {
        setLayout(new BorderLayout());

        // set the preferred size of the demo
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

        initializeUI(gc);
    }

 
    // *******************************************************
    // *************** Load UI ******************
    // *******************************************************
    public void initializeUI(final GraphicsConfiguration gc)
    {        
        topFrame = new JFrame(gc);
        topFrame.setIconImage( IconManager.getImage("Specify16", IconManager.IconSize.Std16).getImage() );
        
        topFrame.setGlassPane(glassPane = new GhostGlassPane());
        topFrame.setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UICacheManager.register(UICacheManager.GLASSPANE, glassPane);
        
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        
        UICacheManager.register(UICacheManager.TOPFRAME, topFrame);
        
        menuBar = createMenus();
        if (menuBar != null)
        {
            //top.add(menuBar, BorderLayout.NORTH);
            topFrame.setJMenuBar(menuBar);
        }
        UICacheManager.register(UICacheManager.MENUBAR, menuBar);

        
        JToolBar toolBar = createToolBar();
        if (toolBar != null)
        {
            top.add(toolBar, BorderLayout.CENTER);
        }
        UICacheManager.register(UICacheManager.TOOLBAR, toolBar);
        
        mainPanel = new MainPanel();

        statusField = new JTextField("");
        statusField.setEditable(false);
        UICacheManager.register(UICacheManager.STATUSBAR, statusField);
        
        add(statusField, BorderLayout.SOUTH);
        
        PluginMgr.readRegistry();
        
        /*PluginMgr.register(new StartUpTask());
        PluginMgr.register(new DataEntryTask());
        PluginMgr.register(new LabelsTask());
        PluginMgr.register(new ReportsTask());
        PluginMgr.register(new InteractionsTask());
        PluginMgr.register(new StatsTask());
        PluginMgr.register(new QueryTask());    
        PluginMgr.register(new RecordSetTask());
        PluginMgr.register(new ExpressSearchTask());
        */
        PluginMgr.initializePlugins();
        
       
    }
    
    /**
     * 
     * @return the toolbar for the app
     */
    public JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new ToolbarLayoutManager(2,2));
        
        return toolBar;
    }
    
    
    /**
     * Create menus
     */
    public void preferences()
    {
        
        JDialog dlg = new JDialog();
        dlg.setModal(true);
        PrefMainPanel pane = new PrefMainPanel(dlg);
        dlg.setContentPane(pane);
        dlg.pack();
        dlg.doLayout();
        System.out.println(dlg.getPreferredSize());
        dlg.setPreferredSize(dlg.getPreferredSize());
        dlg.setSize(dlg.getPreferredSize());
        UIHelper.centerAndShow(dlg); 
    }
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;
        
        JMenu menu = UIHelper.createMenu(mb, "FileMenu", "FileMneu");
        mi = UIHelper.createMenuItem(menu, "Exit", "x", "Exit Appication", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        doExit();
                    }
                });   
        
        menu = UIHelper.createMenu(mb, "EditMenu", "EditMneu");
        mi = UIHelper.createMenuItem(menu, "Preferences", "P", "Preferences", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        preferences();
                    }
                });    

        
        /*JMenuItem mi2;
        JMenu fileMenu2 = (JMenu) mb.add(new JMenu("Log off"));
        

        fileMenu2.setMnemonic('O');       
        mi2 = UIHelper.createMenuItem(fileMenu2, "Log off", "O", "Log off database", false, null);
        mi2.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        if (hasChanged) 
                        {

                        }
                        try {
                            if (mSessionFactory != null)
                            {
                                mSessionFactory.close();
                            }
                            if (mSession != null)
                            {
                                mSession.close();
                            }
                        } catch (Exception e)
                        {
                            log.error("UIHelper.createMenus - ", e);
                        }                       
                        //frame.dispose();
                        final Window parentWindow = SwingUtilities.getWindowAncestor(Specify.this);
                        parentWindow.dispose();
                        Specify ha = new Specify(grc);
                    }
                });  
        */
        menu = UIHelper.createMenu(mb, "AdvMenu", "AdvMneu");
        mi = UIHelper.createMenuItem(menu, getResourceString("ESConfig"), getResourceString("ESConfig_Mn"), getResourceString("ESConfig"), false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        ExpressSearchTask expressSearchTask = (ExpressSearchTask)ContextMgr.getTaskByName(ExpressSearchTask.EXPRESSSEARCH);
                        expressSearchTask.showIndexerPane();
                    }
                }); 
        
        menu.add(UIHelper.createMenu(mb, "SystemMenu", "SystemMneu"));
       
        menu = UIHelper.createMenu(mb, "TabsMenu", "TabsMneu");
        /*mi = UIHelper.createMenuItem(menu, "Close Current", "C", "Close C", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        UICacheManager.getSubPaneMgr().closeCurrent();
                    }
                });*/  

        mi = UIHelper.createMenuItem(menu, "Close All", "A", "Close All", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        UICacheManager.getSubPaneMgr().closeAll();
                    }
                });  


         return mb;
    }
   
    /**
     * Checks to see if cache has changed before exiting
     *
     */
    protected void doExit()
    {
        System.exit(0);
    }


  
    /**
     * Bring up the PPApp demo by showing the frame (only applicable if coming up
     * as an application, not an applet);
     */
    public void showApp()
    {
        // put PPApp in a frame and show it
        JFrame f = getFrame();
        f.setTitle("Specify 6.0");
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        centerAndShow(f);
    }
  
    /**
     * Returns the frame instance
     */
    public JFrame getFrame()
    {
      return topFrame;
    }

    /**
     * Returns the menubar
     */
    public JMenuBar getMenuBar()
    {
      return menuBar;
    }

    /**
     * Set the status
     */
    public void setStatus(final String s)
    {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SpecifyRunnable(this, s)
        {
          public void run()
          {
            mApp.statusField.setText((String) obj);
          }
        });
    }
    
    // *******************************************************
    // *****************   Static Methods  *******************
    // *******************************************************
    
    /**
     * 
     * @return the specify app object
     */
    public static Specify getSpecify()
    {
        return specifyApp;
    }
  

  // *******************************************************
  // ******************   Runnables  ***********************
  // *******************************************************

  /**
   * Generic PPApp runnable. This is intended to run on the
   * AWT gui event thread so as not to muck things up by doing
   * gui work off the gui thread. Accepts a PPApp and an Object
   * as arguments, which gives subtypes of this class the two
   * "must haves" needed in most runnables for this demo.
   */
  class SpecifyRunnable implements Runnable
  {

    protected Specify mApp;

    protected Object    obj;

    public SpecifyRunnable(Specify aApp, Object obj)
    {
      this.mApp = aApp;
      this.obj = obj;
    }

    public void run()
    {
    }
  }
  
  //-----------------------------------------------------------------------------
  //-- Application MAIN
  //-----------------------------------------------------------------------------

  /**
   * PPApp Main. Called only if we're an application, not an applet.
   */
  public static void main(String[] args)
  {


      // Create Specify Application
      
      Specify specify = new Specify(GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration());
  }

}

