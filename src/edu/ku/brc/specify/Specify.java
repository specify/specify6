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

import java.awt.*;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.ui.db.*;

import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.FetchMode;
import org.hibernate.cache.*;


import edu.ku.brc.specify.ui.*;

import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;
import com.jgoodies.looks.plastic.*;
import edu.ku.brc.specify.exceptions.UIException;


/**
 * Specify Main Application Class
 *
 * @author Rod Spears <rods@ku.edu>
 */
public class Specify extends JPanel implements MainPaneMgrIFace
{
    private static Log log = LogFactory.getLog(Specify.class);

    // The preferred size of the demo
    private static final int    PREFERRED_WIDTH  = 650;
    private static final int    PREFERRED_HEIGHT = 750;

    // Status Bar
    private JTextField          mStatusField       = null;
    private JMenuBar            mMenuBar           = null;
    private JFrame              mFrame             = null;
  
    protected  boolean          mHasChanged        = false;
  
    protected Configuration     mConfig            = null;
    protected SessionFactory    mSessionFactory    = null;
    protected Session           mSession           = null;
  
  
    private JLabel splashLabel = null;
  
    // Used only if swingset is an application 
    private JFrame frame = null;
    private JWindow splashScreen = null;
  
    private String databaseName = "";
    private String userName = "";
    private String password ="";
    private String hostName ="";
  
    private GraphicsConfiguration grc;
  
    private boolean useLogonDialog = false;
    final static Logger   _logger = Logger.getLogger(Specify.class);
  
    private SpecifyConfig config;
    //private JSplitPane    splitPane;
  
    //private CardLayout                       cardLayout  = new CardLayout();
    //private JPanel                           layoutPanel = null;
    private Hashtable<String, MainPaneIFace> panels      = new Hashtable<String, MainPaneIFace>();
    
    private JTabbedPane  tabbedPane = new JTabbedPane();
  
    private static Specify  specifyApp       = null;
  
     /**
     * Constructor with Applet
     * @param aApplet
     */
    public Specify(GraphicsConfiguration gc)
    {
        specifyApp = this;
        
        try 
        { 
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel()); 
            //UIManager.setLookAndFeel(new PlasticLookAndFeel()); 

        } 
        catch (Exception e) 
        { 
            _logger.error("Can't change L&F: ", e); 
        }    
      
        grc = gc;
        initialize(gc);
      
        frame = createFrame(gc);
        createSplashScreen();
      
        // do the following on the gui thread
        SwingUtilities.invokeLater(new Runnable() {
              public void run() 
              {
                  showSplashScreen();
              }
        });
      
        try
        {
            config = SpecifyConfig.getInstance();
            config.init(this); // do this once
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
      
      
        // Create and throw the splash screen up. Since this will
        // physically throw bits on the screen, we need to do this
        // on the GUI thread using invokeLater.

        
      
      
        _logger.info("Creating configuration "); 
        
        // Create a configuration based on the properties file we've put
        // in the standard place.
        //mConfig = new Configuration();


        try {
            
            // Tell it about the classes we want mapped, taking advantage of
            // the way we've named their mapping documents.
            //mConfig.addClass(Accession.class).addClass(CollectingEvent.class).addClass(CollectionObject.class);
            //mConfig.addClass(Geography.class).addClass(TaxonName.class).addClass(Locality.class);
            //_logger.info("Adding mapped classes "); 
            
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
            
            _logger.info("-----------");
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
                    //  _logger.error("Error:", e);
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
                _logger.error("Error Loading main_ui.xml - "+e);
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
                    _logger.error("Couldn't locate cache item["+cacheName+"]");
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
                            showApp();

                            //showCard("Main");
                            validate();
                            hideSplash();
                            
                            add(tabbedPane, BorderLayout.CENTER);
                            
                            showPane(SQLQueryPane.paneName);
                        }
                    });         
        } catch (Exception e)
        {
            _logger.error("Error",e);
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
        splashLabel = new JLabel(new ImageIcon("images/splash.jpg"));
        
        if(!isApplet()) 
        {
            splashScreen = new JWindow(getFrame());
            splashScreen.getContentPane().add(splashLabel);
            splashScreen.pack();
            Rectangle screenRect = getFrame().getGraphicsConfiguration().getBounds();
            splashScreen.setLocation(screenRect.x + screenRect.width/2 - splashScreen.getSize().width/2,
                                     screenRect.y + screenRect.height/2 - splashScreen.getSize().height/2);
        } 
    }
  
    public void showSplashScreen()
    {
        if (!isApplet())
        {
            splashScreen.setVisible(true);
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
            splashScreen.setVisible(false);
            splashScreen = null;
            splashLabel = null;
        }
    }

    /**
     * Create a frame for SwingSet2 to reside in if brought up as an
     * application.
     */
    public static JFrame createFrame(GraphicsConfiguration gc)
    {
        JFrame frame = new JFrame(gc);
        /*if (numSSs == 0)
        {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else
        {
            WindowListener l = new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    numSSs--;
                    swingSets.remove(this);
                }
            };
            frame.addWindowListener(l);
        }*/
        return frame;
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
    public void initializeUI(GraphicsConfiguration gc)
    {
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        
        mFrame = new JFrame(gc);
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        
        mMenuBar = createMenus();
        if (mMenuBar != null)
        {
            top.add(mMenuBar, BorderLayout.NORTH);
        }
        
        JToolBar toolBar = createToolBar();
        if (toolBar != null)
        {
            top.add(toolBar, BorderLayout.CENTER);
        }
        
        //layoutPanel = new JPanel(cardLayout);
        //layoutPanel.setBackground(Color.WHITE);
        //add(layoutPanel, BorderLayout.CENTER);
        
        // Special Temp code
        addPane(SQLQueryPane.paneName, new SQLQueryPane());
        
        /*GenericFrame frame = new GenericFrame();
        frame.setTitle("Preferences");
        frame.getContentPane().add(new SQLQueryPane(), BorderLayout.CENTER);
        centerAndShow(frame);
         */
        
        mStatusField = new JTextField("");
        mStatusField.setEditable(false);
        UICacheManager.getInstance().setStatusBarTextField(mStatusField);
        
        add(mStatusField, BorderLayout.SOUTH);
        
    }
    
    /**
     * 
     * @return
     */
    public JToolBar createToolBar()
    {
        JToolBar toolBar = new JToolBar();
        String[] iconsToLoad = {"Work_Bench",   "newworkbench.gif", "workbench_hint",
                                "Data_Entry",   "dataentry.gif",    "dataentry_hint",
                                "Labels",       "labels.gif",       "labels_hint",
                                "Reports",      "reports.gif",      "reports_hint",
                                "Interactions", "loans.gif",        "interactions_hint",
                                "Search",        "queryIt.gif",     "search_hint"};
        for (int i=0;i<iconsToLoad.length;i+=3)
        {
            IconManager.getInstance().createAndPutIconAndScale(iconsToLoad[i], iconsToLoad[i+1]);
            ToolBarDropDownBtn btn = new ToolBarDropDownBtn(UICacheManager.getResourceString(iconsToLoad[i]), IconManager.getInstance().getIcon(iconsToLoad[i], IconManager.ICON_SIZE.ICON_NORMAL16), JButton.BOTTOM);
            btn.setStatusBarHintText(UICacheManager.getResourceString(iconsToLoad[i+2]));
            btn.addToToolBar(toolBar);
        }
        
        
        toolBar.addSeparator();
        toolBar.add(new JLabel(" "));
        
        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        JPanel     searchPanel = new JPanel(gridbag);
        JLabel     spacer      = new JLabel(" ");
        JTextField searchText  = new JTextField(10);
        JButton    searchBtn   = new JButton(UICacheManager.getResourceString("Search"));
        
        searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));
        
        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        searchPanel.add(spacer);
        
        c.weightx = 0.0;
        gridbag.setConstraints(searchText, c);
        searchPanel.add(searchText);
        
        gridbag.setConstraints(searchBtn, c);
        searchPanel.add(searchBtn);
        
        toolBar.add(searchPanel);
        
        
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
        //frame.pack();

        centerAndShow(frame);

    }
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar menuBar = null;
        menuBar = new JMenuBar();
        JMenuItem mi;
        JMenu fileMenu = (JMenu) menuBar.add(new JMenu("File"));
        fileMenu.setMnemonic('F');
        mi = createMenuItem(fileMenu, "Exit", "x", "Exit Appication", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        doExit();
                    }
                });   
        
        JMenu editMenu = (JMenu) menuBar.add(new JMenu("Edit"));
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
        JMenu fileMenu2 = (JMenu) menuBar.add(new JMenu("Log off"));
        
        fileMenu2.setMnemonic('O');       
        mi2 = createMenuItem(fileMenu2, "Log off", "O", "Log off database", false, null);
        mi2.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        if (mHasChanged) 
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
                            _logger.error("createMenus - ", e);
                        }                       
                        //frame.dispose();
                        final Window parentWindow = SwingUtilities.getWindowAncestor(Specify.this);
                        parentWindow.dispose();
                        Specify ha = new Specify(grc);
                    }
                });  
        
        
        return menuBar;
    }
   
    /**
     * Checks to see if cache has changed before exiting
     *
     */
    protected void doExit()
    {
        if (mHasChanged) 
        {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "The Cache has changed, do you wish to save?", "Cache has changed.", JOptionPane.YES_NO_OPTION))
            {
                //mCacheMgr.saveCache();
            }
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
            _logger.error("doExit - ",e);
        }
        System.exit(0);
    }

    /**
     * @param b
     * @return
     */
    protected PropertyChangeListener createActionChangeListener(JMenuItem b)
    {
        return new ActionChangedListener(b);
    }
    
    /**
     * 
     * @author globus
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
    public JMenuItem createMenuItem(JMenu aMenu,
                                    String aLabel,
                                    String aMnemonic,
                                    String aAccessibleDescription,
                                    boolean aEnabled,
                                    AbstractAction aAction)
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
        f.setTitle("Hyla Test App");
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        centerAndShow(f);
    }
  
    protected void centerAndShow(JFrame aFrame)
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
      return mFrame;
    }

    /**
     * Returns the menubar
     */
    public JMenuBar getMenuBar()
    {
      return mMenuBar;
    }

    /**
     * Set the status
     */
    public void setStatus(String s)
    {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SpecifyRunnable(this, s)
        {
          public void run()
          {
            mApp.mStatusField.setText((String) obj);
          }
        });
    }
    
    // *******************************************************
    // *****************   Static Methods  *******************
    // *******************************************************
    
    /**
     * 
     * @return
     */
    public static Specify getSpecify()
    {
        return specifyApp;
    }
  
    /**
     * 
     * @return
     */
    public static MainPaneMgrIFace getMainPanelMgr()
    {
        return specifyApp;
    }
  
  

    // *******************************************************
    // *****************   Interfaces  ***********************
    // *******************************************************

    //-------------------------------
    // MainPaneMgrIFace
    //-------------------------------
    
    /**
     * 
     */
    public MainPaneIFace addPane(String aName, MainPaneIFace aPanel) throws UIException
    {
        MainPaneIFace panel = panels.get(aName);
        if (panel != null)
        {
            throw new UIException("Duplicate Pane name["+aName+"]");
        }
        panels.put(aName, aPanel);
        tabbedPane.addTab(aName, (JComponent)aPanel);
        return aPanel;
    }
  
    public MainPaneIFace removePane(String aName, MainPaneIFace aPanel) throws UIException
    {
        MainPaneIFace panel = panels.get(aName);
        if (panel == null)
        {
            throw new UIException("Pane name["+aName+"] does not exist.");
        }
        panels.remove(aName);
        return aPanel;
    }
  
    public MainPaneIFace showPane(String aName) throws UIException
    {
        MainPaneIFace panel = panels.get(aName);
        if (panel == null)
        {
            throw new UIException("Pane name["+aName+"] does not exist.");
        }
        ((JPanel)panel).setVisible(true);
        //tabbedPane.show(layoutPanel, aName);
        
        return panel;
    }
  
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
              //_logger.info("XXX:"+accession.getLastEditedBy());
              return true;
        } catch (Exception e)
        {
            _logger.error("save - ", e);
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

