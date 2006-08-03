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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MainPanel;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.plugins.PluginMgr;
import edu.ku.brc.af.prefs.AppPrefsMgr;
import edu.ku.brc.af.prefs.PrefMainPanel;
import edu.ku.brc.af.tasks.StartUpTask;
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.ui.DBObjDialogFactory;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.forms.ViewMgr;
import edu.ku.brc.util.FileCache;
/**
 * Specify Main Application Class

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */
@SuppressWarnings("serial")
public class Specify extends JPanel implements DatabaseLoginListener
{
    private static final Logger log = Logger.getLogger(Specify.class);

    // The preferred size of the demo
    private static final int    PREFERRED_WIDTH  = 900;
    private static final int    PREFERRED_HEIGHT = 800;

    private static Specify      specifyApp       = null; // needed for ActionListeners etc.

    // Status Bar
    private JStatusBar          statusField        = null;
    private JMenuBar            menuBar            = null;
    private JFrame              topFrame           = null;
    private MainPanel           mainPanel          = null;

    protected  boolean          hasChanged         = false;

    protected Configuration     hibernateConfig    = null;
    protected String            currentDatabaseName = null;

    protected GhostGlassPane    glassPane;

    private JLabel splashLabel = null;

    // Used only if swingset is an application
    private JFrame    frame               = null;
    private JWindow   splashWindow        = null;
    private ImageIcon specifyImageIcon    = null;
    //private ImageIcon userSplashImageIcon = null;


    @SuppressWarnings("unused")
    private SpecifyConfig config;


     /**
     * Constructor with GraphicsConfiguration
     * @param gc the GraphicsConfiguration
     */
    public Specify(GraphicsConfiguration gc)
    {
    	// we simply need to create this class, not use it
        @SuppressWarnings("unused") MacOSAppHandler macoshandler = new MacOSAppHandler(this);

        UICacheManager.getInstance(); // initializes it first thing

        UICacheManager.setAppPrefs(AppPrefsMgr.getInstance().load(System.getProperty("user.home")));
        SpecifyAppPrefs.initialPrefs();

        FileCache.setDefaultPath(UICacheManager.getInstance().getDefaultWorkingPath());

        UICacheManager.register(UICacheManager.MAINPANE, this); // important to be done immediately
        UICacheManager.setViewbasedFactory(DBObjDialogFactory.getInstance());

        initPrefs();

        // Create and throw the splash screen up. Since this will
        // physically throw bits on the screen, we need to do this
        // on the GUI thread using invokeLater.

        if (false)
        {
            createSplashScreen();
            // do the following on the gui thread
            SwingUtilities.invokeLater(new Runnable() {
                  public void run()
                  {
                      showSplashScreen();
                  }
            });
        }

        specifyApp = this;

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

        log.info("Creating Database configuration ");

        UIHelper.doLogin(true, false, this); // true means do auto login if it can, second bool means use dialog instead of frame


    }

    /**
     *
     */
    protected void initStartUpPanels(final String databaseName)
    {
        PluginMgr.initializePlugins();

        if( !SwingUtilities.isEventDispatchThread() )
        {
            SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        initStartUpPanels(databaseName);
                    }
                });
            return;
        }

        validate();
        hideSplash();

        add(mainPanel, BorderLayout.CENTER);
        doLayout();

        mainPanel.setBackground(Color.WHITE);

        SubPaneMgr.getInstance().removeAllPanes();

        Taskable startUpTask = ContextMgr.getTaskByClass(StartUpTask.class);
        if (startUpTask != null)
        {
            startUpTask.requestContext();
        }

        showApp();
    }

    /**
     *
     */
    protected void initPrefs()
    {
/*
        boolean skip = false;
        if (skip)
        {
            return;
        }

        AppPrefsIFace appPrefs = UICacheManager.getAppPrefs();

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
                org.dom4j.Element section = (org.dom4j.Element)iter.next();

                String      title       = section.attributeValue("title");
                AppPrefsIFace sectionNode = appPrefs.node(title);
                if (!sectionNode.getBoolean("isApp", false))
                {
                    sectionNode.put("title", title);
                    sectionNode.putBoolean("isApp", true);
                }

                List prefs = section.selectNodes("pref");
                for ( Iterator iterPrefs = prefs.iterator(); iterPrefs.hasNext(); )
                {
                    org.dom4j.Element pref = (org.dom4j.Element)iterPrefs.next();

                    String prefTitle  = pref.attributeValue("title");
                    String iconName   = pref.attributeValue("icon");
                    String panelClass = pref.attributeValue("panelClass");

                    AppPrefsIFace prefNode     = sectionNode.node(prefTitle);
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
*/
    }

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
        specifyImageIcon = new ImageIcon(Specify.class.getResource("images/specify_splash.gif"));
        splashLabel = new JLabel(specifyImageIcon);
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
            specifyspecifyImageIcon = new ImageIcon(Specify.class.getResource("images/specify_splash.gif"));
            JPanel panel = new JPanel() {

                public void paintComponent(Graphics g)
                {
                    if (specifyspecifyImageIcon != null)
                    {
                        g.drawImage(specifyspecifyImageIcon.getImage(),0,0,null);
                        //g.setColor(Color.BLACK);
                       // g.draw3DRect(0,0,199,199, true);
                        //System.out.println("Paint RECT");

                    }
                }
                public Dimension getSize()
                {
                    //return new Dimension(200,200);
                    return new Dimension(specifyspecifyImageIcon.getIconWidth(), specifyspecifyImageIcon.getIconHeight());
                }
                public Dimension getPreferredSize()
                {
                    return getSize();
                }
            };
            panel.setOpaque(false);
            JFrame splashFrame = new JFrame("Transparent Window");
            splashScreen = new TransparentBackground(splashFrame, specifyspecifyImageIcon);
            splashFrame.setSize(specifyspecifyImageIcon.getIconWidth(), specifyspecifyImageIcon.getIconHeight());
            splashScreen.setSize(specifyspecifyImageIcon.getIconWidth(), specifyspecifyImageIcon.getIconHeight());

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
            if (splashWindow != null)
            {
                splashWindow.setVisible(true);
            }
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
        if (!isApplet() && splashWindow != null)
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

        statusField = new JStatusBar();
        UICacheManager.register(UICacheManager.STATUSBAR, statusField);

        add(statusField, BorderLayout.SOUTH);

        PluginMgr.readRegistry();


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
        mi = UIHelper.createMenuItem(menu, "Login", "L", "Database Login", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        class DBListener implements DatabaseLoginListener
                        {
                            public void loggedIn(final String databaseName, final String userName)
                            {
                                specifyApp.loggedIn(databaseName, userName);
                            }

                            public void cancelled()
                            {
                                // Do not call this it will exit the application
                                //specifyApp.cancelled();
                            }
                        }

                        UIHelper.doLogin(false, true, new DBListener()); // true means do auto login if it can, second bool means use dialog instead of frame
                    }
                });
        menu.addSeparator();
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
                        SubPaneMgr.getInstance().closeCurrent();
                    }
                });*/

        mi = UIHelper.createMenuItem(menu, "Close All", "A", "Close All", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        SubPaneMgr.getInstance().closeAll();
                    }
                });


         return mb;
    }

    /**
     * Checks to see if cache has changed before exiting
     *
     */
    protected void doAbout()
    {

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p:g,30px,r:p:g", "f:p:g"));
        CellConstraints cc         = new CellConstraints();

        builder.add(new JLabel("Specify 6.0"), cc.xy(1,1));
        builder.add(new JLabel(IconManager.getImage("SpecifyLargeIcon")), cc.xy(3,1));

        final JDialog dialog = new JDialog(frame, "About Specify 6.0", true);
        //dialog.setContentPane(builder.getPanel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Specify 6.0"), BorderLayout.WEST);
        panel.add(new JLabel(IconManager.getImage("SpecifyLargeIcon")), BorderLayout.EAST);
        dialog.setContentPane(panel);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //dialog.validate();
        //dialog.setSize(dialog.getPreferredSize());
        dialog.pack();
        UIHelper.centerAndShow(dialog);
    }

    /**
     * Checks to see if cache has changed before exiting
     *
     */
    protected void doExit()
    {
		log.info("Application shutdown");

		// save the long term cache mapping info
		try
		{
			UICacheManager.getLongTermFileCache().saveCacheMapping();
			log.info("Successfully saved long term cache mapping");
		}
		catch( IOException e1 )
		{
			log.warn("Error while saving long term cache mapping.",e1);
		}

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

        f.addWindowListener(new WindowAdapter()
        		{
        			public void windowClosing(WindowEvent e)
        			{
        				doExit();
        			}
        		});
        UIHelper.centerAndShow(f);
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

    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.lang.String, java.lang.String)
     */
    public void loggedIn(final String databaseName, final String userName)
    {
        ViewMgr.setAsDefaultViewSet("Fish Views");

        initStartUpPanels(databaseName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
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
   *
   */
  public static void main(String[] args)
  {


      // Create Specify Application
      SwingUtilities.invokeLater(new Runnable() {
          public void run()
          {
              @SuppressWarnings("unused") Specify specify = new Specify(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
          }
    });


  }
}

