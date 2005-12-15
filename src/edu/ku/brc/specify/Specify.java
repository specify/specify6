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
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.*;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.DataEntryTask;
import edu.ku.brc.specify.core.ExpressSearchTask;
import edu.ku.brc.specify.core.InteractionsTask;
import edu.ku.brc.specify.core.LabelsTask;
import edu.ku.brc.specify.core.QueryTask;
import edu.ku.brc.specify.core.RecordSetTask;
import edu.ku.brc.specify.core.ReportsTask;
import edu.ku.brc.specify.core.StatsTask;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.plugins.PluginMgr;
import edu.ku.brc.specify.ui.GenericFrame;
import edu.ku.brc.specify.ui.MainPanel;
import edu.ku.brc.specify.ui.PropertyViewer;
import edu.ku.brc.specify.ui.ToolbarLayoutManager;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.dnd.GhostGlassPane;
/**
 * Specify Main Application Class
 *
 * @author Rod Spears <rods@ku.edu>
 */
public class Specify extends JPanel
{
    private static Log log = LogFactory.getLog(Specify.class);

    // The preferred size of the demo
    private static final int    PREFERRED_WIDTH  = 800;
    private static final int    PREFERRED_HEIGHT = 750;

    // Status Bar
    private JTextField          statusField       = null;
    private JMenuBar            menuBar          = null;
    private JFrame              topFrame            = null;
    private MainPanel           mainPanel         = null;
    
    protected  boolean          hasChanged        = false;
  
    protected Configuration     mConfig            = null;
    protected SessionFactory    mSessionFactory    = null;
    protected Session           mSession           = null;
  
    protected GhostGlassPane    glassPane;
  
    private JLabel splashLabel = null;
  
    // Used only if swingset is an application 
    private JFrame frame = null;
    private JWindow splashWindow = null;
    //private TransparentBackground splashScreen = null;
    private ImageIcon specifySplashImageIcon   = null;
  
    private String databaseName = "";
    private String userName = "";
    private String password ="";
    private String hostName ="";
  
    private GraphicsConfiguration grc;
  
    private boolean useLogonDialog = false;

  
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
        
        DBConnection.getInstance().setUsernamePassword("rods", "rods");
        DBConnection.getInstance().setDriver("com.mysql.jdbc.Driver");
        DBConnection.getInstance().setDBName("jdbc:mysql://localhost/demo_fish");

        try 
        { 
            System.out.println(System.getProperty("os.name"));
            
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
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
        
        UICacheManager.getInstance().register(UICacheManager.FRAME, frame);
      
      
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
        recordSet.setCreated(new Date());
        
        Set items = new HashSet();
        RecordSetItem rsi = new RecordSetItem();
        rsi.setRecordId(1);
        
        items.add(rsi);
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
            
            // Tell it about the classes we want mapped, taking advantage of
            // the way we've named their mapping documents.
            //mConfig.addClass(Accession.class).addClass(CollectingEvent.class).addClass(CollectionObject.class);
            //mConfig.addClass(Geography.class).addClass(TaxonName.class).addClass(Locality.class);
            //log.info("Adding mapped classes "); 
            
            /*
            mConfig.addClass(Accession.class);
            mConfig.addClass(CollectingEvent.class);
            mConfig.addClass(CollectionObject.class);
            mConfig.addClass(Geography.class);
            mConfig.addClass(TaxonName.class);
            mConfig.addClass(Locality.class);
            mConfig.addClass(CollectionObjectCatalog.class);
            mConfig.addClass(AccessionAgent.class);
            mConfig.addClass(AccessionAuthorization.class);
            mConfig.addClass(Address.class);
            mConfig.addClass(Agent.class);
            mConfig.addClass(AgentAddress.class);
            mConfig.addClass(BiologicalObjectAttribute.class);
            mConfig.addClass(BiologicalObjectRelation.class);
            mConfig.addClass(BiologicalObjectRelationType.class);
            mConfig.addClass(Borrow.class);
            mConfig.addClass(BorrowAgent.class);
            mConfig.addClass(BorrowMaterial.class);
            mConfig.addClass(BorrowReturnMaterial.class);
            mConfig.addClass(BorrowShipment.class);
            mConfig.addClass(CatalogSeriesDefinition.class);
            mConfig.addClass(CatalogSery.class);
            //mConfig.addClass(CollectingEvent.class);
            mConfig.addClass(Collection.class);
            //mConfig.addClass(CollectionObject.class);
            //mConfig.addClass(CollectionObjectCatalog.class);
            mConfig.addClass(CollectionObjectCitation.class);
            mConfig.addClass(CollectionObjectType.class);
            mConfig.addClass(CollectionTaxonomyType.class);
            mConfig.addClass(Deaccession.class);
            mConfig.addClass(DeaccessionAgent.class);
            mConfig.addClass(DeaccessionCollectionObject.class);
            mConfig.addClass(Determination.class);
            mConfig.addClass(DeterminationCitation.class);
            mConfig.addClass(ExchangeIn.class);
            mConfig.addClass(ExchangeOut.class);
            //mConfig.addClass(Geography.class);
            mConfig.addClass(GeologicTimeBoundary.class);
            mConfig.addClass(GeologicTimePeriod.class);
            mConfig.addClass(Habitat.class);
            mConfig.addClass(Image.class);
            mConfig.addClass(ImageAgent.class);
            mConfig.addClass(ImageCollectionObject.class);
            mConfig.addClass(ImageLocality.class);
            mConfig.addClass(Journal.class);
            mConfig.addClass(Loan.class);
            mConfig.addClass(LoanAgent.class);
            mConfig.addClass(LoanPhysicalObject.class);
            mConfig.addClass(LoanReturnPhysicalObject.class);
            //mConfig.addClass(Locality.class);
            mConfig.addClass(LocalityCitation.class);
            mConfig.addClass(Observation.class);
            mConfig.addClass(OtherIdentifier.class);
            mConfig.addClass(Permit.class);
            mConfig.addClass(Preparation.class);
            mConfig.addClass(Project.class);
            mConfig.addClass(ProjectCollectionObject.class);
            mConfig.addClass(ReferenceWork.class);
            mConfig.addClass(Shipment.class);
            mConfig.addClass(Sound.class);
            mConfig.addClass(SoundEventStorage.class);
            mConfig.addClass(TaxonCitation.class);
            //mConfig.addClass(TaxonName.class);
            mConfig.addClass(TaxonomicUnitType.class);
            mConfig.addClass(TaxonomyType.class);
            
            log.info("-----------");
            // New Parts of the Schema
            mConfig.addClass(CollectionObj.class);
            mConfig.addClass(PrepsObj.class);
            mConfig.addClass(PrepAttrs.class);
            mConfig.addClass(PrepTypes.class);
            mConfig.addClass(HabitatAttrs.class);
            mConfig.addClass(BioAttrs.class);
            mConfig.addClass(AttrsDef.class);
        */
            
//          mConfig.addClass(CollectingEvent.class);
            //mConfig.addClass(CollectionObject.class);
            //mConfig.addClass(CollectionObjectCatalog.class);
            //mConfig.addClass(Geography.class);
//          mConfig.addClass(Locality.class)
            //mConfig.addClass(TaxonName.class);        
            //mConfig.addClass(Accession.class).addClass(CollectingEvent.class).addClass(CollectionObject.class).addClass(Geography.class).addClass(TaxonName.class).addClass(Locality.class).addClass(CollectionObjectCatalog.class).addClass(AccessionAgent.class).addClass(AccessionAuthorization.class).addClass(Address.class).addClass(Agent.class).addClass(AgentAddress.class).addClass(BiologicalObjectAttribute.class).addClass(BiologicalObjectRelation.class).addClass(BiologicalObjectRelationType.class).addClass(Borrow.class).addClass(BorrowAgent.class).addClass(BorrowMaterial.class).addClass(BorrowReturnMaterial.class).addClass(BorrowShipment.class).addClass(CatalogSeriesDefinition.class).addClass(CatalogSery.class).addClass(Collection.class).addClass(CollectionObjectCitation.class).addClass(CollectionObjectType.class).addClass(CollectionTaxonomyType.class).addClass(Deaccession.class).addClass(DeaccessionAgent.class).addClass(DeaccessionCollectionObject.class).addClass(Determination.class).addClass(DeterminationCitation.class).addClass(ExchangeIn.class).addClass(ExchangeOut.class).addClass(GeologicTimeBoundary.class).addClass(GeologicTimePeriod.class).addClass(Habitat.class).addClass(Image.class).addClass(ImageAgent.class).addClass(ImageCollectionObject.class).addClass(ImageLocality.class).addClass(Journal.class).addClass(Loan.class).addClass(LoanAgent.class).addClass(LoanPhysicalObject.class).addClass(LoanReturnPhysicalObject.class).addClass(LocalityCitation.class).addClass(Observation.class).addClass(OtherIdentifier.class).addClass(Permit.class).addClass(Preparation.class).addClass(Project.class).addClass(ProjectCollectionObject.class).addClass(ReferenceWork.class).addClass(Shipment.class).addClass(Sound.class).addClass(SoundEventStorage.class).addClass(TaxonCitation.class).addClass(TaxonomicUnitType.class).addClass(TaxonomyType.class);
            
            //mConfig.setProperty("batch_size", "25");
            //mConfig.setProperty("fetch_size", "25");
            //mConfig.setProperty("use_scrollable_resultset", "false");
            //mConfig.setProperty("show_sql", "true");
            // Get the session factory we can use for persistence
            //mConfig.set

            //setHibernateLogonConfig();

           // }
            //mSessionFactory = mConfig.buildSessionFactory();
        
            // Ask for a session using the JDBC information we've configured
            //mSession = mSessionFactory.openSession(); 
            //mSession.//
            

            /*
            Criteria criteria = mSession.createCriteria(Accession.class);
            _data = criteria.list();//session.find("from collev");
             */
            
            /*mLayoutPanel = new JPanel(mCardLayout);
            add(mLayoutPanel, BorderLayout.CENTER);
            
            ActionMgr.resetAll();
           
            ActionListener backAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    //try{mSession.flush();}
                    //catch(Exception e){
                    //  log.error("Error:", e);
                        /////////////meg added
                    //}
                    showCard("Main");
                }
            };
            
            mMainPanel = new MainPanel(this);
            mMainPanel.setMinimumSize(new Dimension(500,500));
            mLayoutPanel.add(mMainPanel, "Main");
            
            Document doc = null;
            try
            {
                doc = XMLHelper.readXMLFile2DOM("main_ui.xml");
            } catch (Exception e)
            {
                log.error("Error Loading main_ui.xml - "+e);
            }
            
            // Load the view files
            NodeList nodeList = XPathAPI.selectNodeList(doc, "definition/viewCache");
            for (int i=0;i<nodeList.getLength();i++)
            {
                Node node = nodeList.item(i);
                String name = XMLHelper.findAttrValue(node, "name");
                String file = XMLHelper.findAttrValue(node, "file");
                ViewMgrCacheItem viewItem = ViewMgr.add(name, file);
            }
            
            DataGetter dataGetter = new DataGetterForDB();

            // load the views
            IconManager iconMgr = IconManager.getInst();
            nodeList = XPathAPI.selectNodeList(doc, "definition/view");
            for (int i=0;i<nodeList.getLength();i++)
            {
                Node node = nodeList.item(i);
                String cacheName = XMLHelper.findAttrValue(node, "cacheName");
                String name      = XMLHelper.findAttrValue(node, "name");
                String className = XMLHelper.findAttrValue(node, "class");
                int    id        = XMLHelper.getIntFromAttr(node, "id");
                String edit      = XMLHelper.findAttrValue(node, "edit");
                
                Class classObj = Class.forName(className);
                
                ViewMgrCacheItem viewItem = ViewMgr.get(cacheName);
                if (viewItem != null)
                {
                    boolean isEdit = edit.equals("true");
                    ViewPanel viewPanel = new ViewPanel(null, viewItem, id, null, classObj, isEdit, true, false, dataGetter);
                    mLayoutPanel.add(name, viewPanel);
                    if (isEdit)
                    {
                        viewPanel.registerSaveAction(this);
                    }
                    viewPanel.registerBackAction(backAction);
                    mViewPanels.put(name, viewPanel);
                    viewPanel.validate();
                } else 
                {
                    log.error("Couldn't locate cache item["+cacheName+"]");
                }

            }

            ActionMgr.registerListenerByType(this, "generic");

            //ViewMgrCacheItem viewItem = ViewMgr.add("Accession", "Accessions_view.xml");
            //_viewPanel = new ViewPanel(viewItem, 1, _data.get(0), Accession.class);
            //add(_viewPanel, BorderLayout.CENTER);
            
            */
            
            SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {

                            //showCard("Main");
                            validate();
                            hideSplash();
                            
                            add(mainPanel, BorderLayout.CENTER);
                             //mainPanel.showPane(SQLQueryPane.paneName);
                            showApp();
                            
                        }
                    });         
         
        } catch (Exception e)
        {
            log.error("Error",e);
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
        splashLabel = new JLabel(new ImageIcon(Specify.class.getResource("images/specify_splash.gif")));        if(!isApplet()) 
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

        // Note that
        // we again must do this on the GUI thread using invokeLater.
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            //showApp();
           }
        });
    }

 
    // *******************************************************
    // *************** Load UI ******************
    // *******************************************************
    public void initializeUI(final GraphicsConfiguration gc)
    {        
        topFrame = new JFrame(gc);
        topFrame.setGlassPane(glassPane = new GhostGlassPane());
        topFrame.setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UICacheManager.getInstance().register(UICacheManager.GLASSPANE, glassPane);
        
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        
        UICacheManager.getInstance().register(UICacheManager.TOPFRAME, topFrame);
        
        menuBar = createMenus();
        if (menuBar != null)
        {
            //top.add(menuBar, BorderLayout.NORTH);
            topFrame.setJMenuBar(menuBar);
        }
        UICacheManager.getInstance().register(UICacheManager.MENUBAR, menuBar);

        
        JToolBar toolBar = createToolBar();
        if (toolBar != null)
        {
            top.add(toolBar, BorderLayout.CENTER);
        }
        UICacheManager.getInstance().register(UICacheManager.TOOLBAR, toolBar);
        
        //layoutPanel = new JPanel(cardLayout);
        //layoutPanel.setBackground(Color.WHITE);
        //add(layoutPanel, BorderLayout.CENTER);
        
        // Special Temp code
        //addPane(SQLQueryPane.paneName, new SQLQueryPane());
        mainPanel = new MainPanel();
        //mainPanel.addSubPanel(new SQLQueryPane());
        
        /*GenericFrame frame = new GenericFrame();
        frame.setTitle("Preferences");
        frame.getContentPane().add(new SQLQueryPane(), BorderLayout.CENTER);
        centerAndShow(frame);
         */
        
        statusField = new JTextField("");
        statusField.setEditable(false);
        UICacheManager.getInstance().register(UICacheManager.STATUSBAR, statusField);
        
        add(statusField, BorderLayout.SOUTH);
        
        PluginMgr.getInstance().register(new DataEntryTask());
        PluginMgr.getInstance().register(new LabelsTask());
        PluginMgr.getInstance().register(new ReportsTask());
        PluginMgr.getInstance().register(new InteractionsTask());
        PluginMgr.getInstance().register(new StatsTask());
        PluginMgr.getInstance().register(new QueryTask());
        
        RecordSetTask rst = new RecordSetTask();
        rst.initialize();
        PluginMgr.getInstance().register(rst);
        
        // Express Search (Invisble Task)
        ExpressSearchTask est = new ExpressSearchTask();
        est.initialize();
        PluginMgr.getInstance().register(est);
       
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
        GenericFrame frame = new GenericFrame();
        frame.setTitle("Preferences");
        frame.getContentPane().add(new PropertyViewer(), BorderLayout.CENTER);
        centerAndShow(frame);

    }
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = null;
        mb = new JMenuBar();
        JMenuItem mi;
        JMenu fileMenu = (JMenu) mb.add(new JMenu("File"));
        fileMenu.setMnemonic('F');
        mi = createMenuItem(fileMenu, "Exit", "x", "Exit Appication", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        doExit();
                    }
                });   
        
        JMenu editMenu = (JMenu) mb.add(new JMenu("Edit"));
        editMenu.setMnemonic('E');
        mi = createMenuItem(editMenu, "Preferences", "P", "Preferences", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        preferences();
                    }
                });    

        
        JMenuItem mi2;
        JMenu fileMenu2 = (JMenu) mb.add(new JMenu("Log off"));
        

        fileMenu2.setMnemonic('O');       
        mi2 = createMenuItem(fileMenu2, "Log off", "O", "Log off database", false, null);
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
                            log.error("createMenus - ", e);
                        }                       
                        //frame.dispose();
                        final Window parentWindow = SwingUtilities.getWindowAncestor(Specify.this);
                        parentWindow.dispose();
                        Specify ha = new Specify(grc);
                    }
                });  
        
        JMenuItem mi3;
        JMenu fileMenu3 = (JMenu) mb.add(new JMenu("Windows"));
        fileMenu3.setMnemonic('W');       
        mi3 = createMenuItem(fileMenu3, "Close All", "A", "Close All", false, null);
        mi3.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        UICacheManager.getInstance().getSubPaneMgr().closeAll();
                    }
                });  

        mi3 = createMenuItem(fileMenu3, "Close Current", "C", "Close C", false, null);
        mi3.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        UICacheManager.getInstance().getSubPaneMgr().closeCurrent();
                    }
                });  

        fileMenu = (JMenu) mb.add(new JMenu(getResourceString("Advanced")));
        fileMenu.setMnemonic('A');     
        String label = getResourceString("ESConfig");
        mi = createMenuItem(fileMenu, label, getResourceString("ESConfig_Mn"), label, false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        ExpressSearchTask expressSearchTask = (ExpressSearchTask)ContextMgr.getInstance().getTaskByName(ExpressSearchTask.EXPRESSSEARCH);
                        expressSearchTask.showIndexerPane();
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
     * @param b
     * @return
     */
    protected PropertyChangeListener createActionChangeListener(final JMenuItem b)
    {
        return new ActionChangedListener(b);
    }
    
    /**
     * 
     *
     * TODO To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Generation - Code and Comments
     */
    private class ActionChangedListener implements PropertyChangeListener
    {
        JMenuItem menuItem;
        ActionChangedListener(JMenuItem mi)
        {
            super();
            this.menuItem = mi;
        }
        public void propertyChange(PropertyChangeEvent e)
        {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME))
            {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled"))
            {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }
    
  /**
   * Creates a generic menu item
   */
    public JMenuItem createMenuItem(final JMenu aMenu,
                                    final String aLabel,
                                    final String aMnemonic,
                                    final String aAccessibleDescription,
                                    final boolean aEnabled,
                                    final AbstractAction aAction)
    {
        JMenuItem mi = (JMenuItem) aMenu.add(new JMenuItem(aLabel));
        if (aMnemonic.length() > 0)
        {
            mi.setMnemonic(aMnemonic.charAt(0));
        }
        mi.getAccessibleContext().setAccessibleDescription(aAccessibleDescription);
        mi.addActionListener(aAction);
        if (aAction != null)
        {
          aAction.addPropertyChangeListener(createActionChangeListener(mi));
          aAction.setEnabled(aEnabled);
        }
        //mi.setEnabled(aEnabled);
        return mi;
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
     * Center and make the frame visible
     * @param aFrame
     */
    public static void centerAndShow(JFrame aFrame)
    {
        Rectangle screenRect = aFrame.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(aFrame.getGraphicsConfiguration());

        // Make sure we don't place the demo off the screen.
        int centerWidth = screenRect.width < aFrame.getSize().width ? screenRect.x : screenRect.x
            + screenRect.width / 2 - aFrame.getSize().width / 2;
        int centerHeight = screenRect.height < aFrame.getSize().height ? screenRect.y : screenRect.y
            + screenRect.height / 2 - aFrame.getSize().height / 2;

        centerHeight = centerHeight < screenInsets.top ? screenInsets.top : centerHeight;

        aFrame.setLocation(centerWidth, centerHeight);
        aFrame.setVisible(true);      
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
    // *****************   Interfaces  ***********************
    // *******************************************************

   
    //-------------------------------
    // SaveDataObjectIFace
    //-------------------------------
    public boolean save(Object aObj)
    {
        if (aObj == null) return false;
        
        try 
        {
              Transaction tx = mSession.beginTransaction();
              mSession.save(aObj);
              tx.commit();
              //Criteria criteria = mSession.createCriteria(Accession.class);
              //_data = criteria.list();//session.find("from collev");
              //accession = (Accession)_data.get(0);
              //log.info("XXX:"+accession.getLastEditedBy());
              return true;
        } catch (Exception e)
        {
            log.error("save - ", e);
        }
        return false;
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
    // Create PPApp on the default monitor
      Specify hyla = new Specify(GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice().getDefaultConfiguration());
  }

}

