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
package edu.ku.brc.specify.tests;


import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.tests.forms.TestDataObj;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.JAutoCompComboBox;
import edu.ku.brc.ui.db.JAutoCompTextField;
import edu.ku.brc.ui.validation.ValComboBoxFromQuery;
/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class PickListTestApp
{
    private static final Logger log = Logger.getLogger(PickListTestApp.class);
    
    protected JPanel contentPane;
    protected JFrame mainFrame;
    
    protected String currViewSetName;
    protected int    currFormId;
    
    protected Object            dataObj     = null;
    protected TestDataObj       testDataObj = null;
    protected List<TestDataObj> list        = new ArrayList<TestDataObj>();

    
    public PickListTestApp()
    {
        HibernateUtil.initialize();
        
        /*
        try
        {
            
            Connection dbConnection = DBConnection.getConnection();
            Statement  dbStatement  = dbConnection.createStatement();
    
            ResultSet rs = dbStatement.executeQuery("select LocalityName from locality where localityID = 1858960246;");
            if (rs.first())
            {
                do
                {
                    System.out.println(rs.getObject(1));
                    System.out.println(rs.getString(1));
                    
                } while(rs.next());
            }
            
            dbStatement.close();
            dbConnection.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }*/

        /*
        Session session = HibernateUtil.getCurrentSession();
        Locality l = new Locality();
        l.setLocalityId(387349873);
        l.setLocalityName("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        Transaction trans = session.beginTransaction();
        session.save(l);
        trans.commit();
        session.close();
        */
        
        /*
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Locality.class);
        for (Object obj :  criteria.list())
        {
            Locality locality = (Locality)obj;
            if (locality.getLocalityName().charAt(0) != '[')
            {
                System.out.println(locality.getLocalityId() + " getLocalityName: "+locality.getLocalityName());
            }
        }
        list.clear();
        

        org.hibernate.Query q = HibernateUtil.getCurrentSession().createQuery("from locality in class Locality where locality.localityId = 387349873");
        java.util.List list2 = q.list();
        
        for (Object obj : list2)
        {
            Locality locality = (Locality)obj;
            System.out.println("getLocalityName: "+locality.getLocalityName());
        }
        */
    }

    
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize() 
    {
        SpecifyAppPrefs.initialPrefs(); // Must be done first thing!


        try 
        { 
            //System.out.println(System.getProperty("os.name"));
            
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
            }
                
            //UIManager.setLookAndFeel(new PlasticLookAndFeel()); 
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } 
        catch (Exception e) 
        { 
            log.error("Can't change L&F: ", e); 
        }    
      

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        mainFrame = new JFrame("Specify Form Editor");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UICacheManager.register(UICacheManager.TOPFRAME, mainFrame);
        
        // Create and set up the content pane.
        contentPane = buildContentPane();
        contentPane.setOpaque(true); //content panes must be opaque
        mainFrame.setContentPane(contentPane);

        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            mainFrame.setJMenuBar(menuBar);
        }
        
        mainFrame.pack();
        //mainFrame.setSize(new Dimension(1024, 764));
        //frame.pack();
        UIHelper.centerAndShow(mainFrame);
         
    }
    
      private JPanel buildContentPane()
      {
            FormLayout      formLayout = new FormLayout("p,1dlu,p,1dlu,p", "p,2dlu,p,2dlu,p,2dlu,p");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
          JAutoCompComboBox cb = JAutoCompComboBox.create("test_combobox", false, 50, true);
          
          // Create and register the key listener
          cb.setEditable(true); 
          
          int y = 1;
          builder.add(new JLabel("Enter Value:"), cc.xy(1,y));
          builder.add(cb, cc.xy(3,y));
          builder.add(new JLabel(" (Editable)"), cc.xy(5,y));
          y += 2;
          
          cb = JAutoCompComboBox.create("AccessionStatus", true, 50, false);
          
          // Create and register the key listener
          cb.setEditable(true); 
          cb.setCaseInsensitive(true);
          cb.setEnableAdditions(false);
          
          builder.add(new JLabel("Enter Status:"), cc.xy(1,y));
          builder.add(cb, cc.xy(3,y));
          builder.add(new JLabel(" (Not Editable)"), cc.xy(5,y));
          y += 2;
          
          JAutoCompTextField text = JAutoCompTextField.create("test_textfield", false, 2, true);
          
          text.setAskBeforeSave(false);
          
          builder.add(new JLabel("Enter Text:"), cc.xy(1,y));
          builder.add(text, cc.xy(3,y));
          builder.add(new JLabel(" (Editable)"), cc.xy(5,y));
          y += 2;
          
          ValComboBoxFromQuery valCBX = new ValComboBoxFromQuery("agent", "AgentID", "LastName", "LastName", 
                                                                 "edu.ku.brc.specify.datamodel.Agent", "agentId", "lastName", 
                                                                 "%s", null, null, "AgentDisplay", "Agent");
          
          builder.add(new JLabel("Enter ValCBX:"), cc.xy(1,y));
          builder.add(valCBX, cc.xy(3,y));
          builder.add(new JLabel(" (Editable)"), cc.xy(5,y));
          y += 2;
          
          builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
          return builder.getPanel();
          
      }
  
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;
        
        JMenu menu = createMenu(mb, "FileMenu", "FileMneu");

        
        mi = createMenuItem(menu, "Exit", "x", "Exit Appication", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        doExit();
                    }
                });   
        
        menu = createMenu(mb, "EditMenu", "EditMneu");
        mi = createMenuItem(menu, "Preferences", "P", "Preferences", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        //preferences();
                    }
                });    


         return mb;
    }
    

    /**
     * Create a menu 
     * @param mennuBar the menubar
     * @param labelKey the label key to be localized
     * @param mneuKey the mneu key to be localized
     * @return returns a menu
     */
    protected JMenu createMenu(final JMenuBar menuBar, final String labelKey, final String mneuKey)
    {
        JMenu menu = null;
        try
        {
            menu = menuBar.add(new JMenu(getResourceString(labelKey)));
            menu.setMnemonic(getResourceString(mneuKey).charAt(0));
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.info("Couldn't create menu for " + labelKey + "  " + mneuKey);
        }
        return menu;
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
    protected JMenuItem createMenuItem(final JMenu aMenu,
                                       final String aLabel,
                                       final String aMnemonic,
                                       final String aAccessibleDescription,
                                       final boolean aEnabled,
                                       final AbstractAction aAction)
    {
        JMenuItem mi = aMenu.add(new JMenuItem(aLabel));
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
     * 
     */
    public static void loadData()
    {
       
        Session session = HibernateUtil.getCurrentSession();
        
        try 
        {
            HibernateUtil.beginTransaction();
            
            String[] states = {"AL", "Alabama - AL", 
                              "AK", "Alaska - AK", 
                              "AZ", "Arizona - AZ", 
                              "AR", "Arkansas - AR", 
                              "CA", "California - CA", 
                              "CO", "Colorado - CO", 
                              "CT", "Connecticut - CT", 
                              "DC", "Dist. of Columbia - DC", 
                              "DE", "Delaware - DE", 
                              "FL", "Florida - FL", 
                              "GA", "Georgia - GA", 
                              "HI", "Hawaii - HI", 
                              "ID", "Idaho - ID", 
                              "IL", "Illinois - IL", 
                              "IN", "Indiana - IN", 
                              "IA", "Iowa - IA", 
                              "KS", "Kansas - KS", 
                              "KY", "Kentucky - KY", 
                              "LA", "Louisiana - LA", 
                              "ME", "Maine - ME", 
                              "MD", "Maryland - MD", 
                              "MA", "Massachusetts - MA ", 
                              "MI", "Michigan - MI", 
                              "MN", "Minnesota - MN", 
                              "MS", "Mississippi - MS", 
                              "MO", "Missouri - MO", 
                              "MT", "Montana - MT", 
                              "NE", "Nebraska - NE", 
                              "NV", "Nevada - NV", 
                              "NH", "New Hampshire - NH", 
                              "NJ", "New Jersey - NJ", 
                              "NM", "New Mexico - NM", 
                              "NY", "New York - NY", 
                              "NC", "North Carolina - NC", 
                              "ND", "North Dakota - ND", 
                              "OH", "Ohio - OH", 
                              "OK", "Oklahoma - OK", 
                              "OR", "Oregon - OR", 
                              "PA", "Pennsylvania - PA", 
                              "PR", "Puerto Rico - PR", 
                              "RI", "Rhode Island - RI", 
                              "SC", "South Carolina - SC", 
                              "SD", "South Dakota - SD", 
                              "TN", "Tennessee - TN", 
                              "TX", "Texas - TX", 
                              "UT", "Utah - UT", 
                              "VT", "Vermont - VT", 
                              "VA", "Virginia - VA", 
                              "WA", "Washington - WA", 
                              "WV", "West Virginia - WV", 
                              "WI", "Wisconsin - WI", 
                              "WY", "Wyoming - WY"};
          
            PickList pl = new PickList();
            pl.initialize();
            pl.setName("states");
            pl.setSizeLimit(50); // doesn't matter when readonly
            pl.setReadOnly(true);
            
            for (int i=0;i<states.length;i++)
            {
                pl.addPickListItem(states[i], states[i+1]);
                i++;
                System.out.println("["+states[i]+"]["+states[i+1]+"]");
            }

          session.save(pl);

          HibernateUtil.commitTransaction();
          
        } catch (Exception e) 
        {
            HibernateUtil.rollbackTransaction();
            e.printStackTrace();
            
        } finally {
            // No matter what, close the session
            HibernateUtil.closeSession();
        }
       
    }
    
    //-----------------------------------------------------------------------
    // Taxon Stuff
    //-----------------------------------------------------------------------
    
    protected static void buildTaxaSearch()
    {
        /*
        File lucenePath = getIndexDirPath(); // must be initialized here
        try
        {
            Directory   dir    = FSDirectory.getDirectory(lucenePath, true);
            IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);
            writer.mergeFactor   = 1000;
            writer.maxMergeDocs  = 9999999;
            writer.minMergeDocs  = 1000;
            
            Connection dbConnection = DBConnection.getInstance().createConnection();
            Statement  dbStatement  = dbConnection.createStatement();

            ResultSet rs = dbStatement.executeQuery("SELECT DISTINCT tx.taxonName FROM taxonname AS tx where tx.taxonName is not null;");
            rs.first();
            do
            {
                Document doc = new Document();
                doc.add(Field.Keyword("id", rs.getString(1)));
                //doc.add(Field.UnIndexed("table", Integer.toString(tableId)));
                
                writer.addDocument(doc);
                
            } while(rs.next());
            
            dbStatement.close();
            dbConnection.close();
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        */
    }
    
    protected static void testLookUpDB()
    {
        try
        {
            Connection dbConnection = DBConnection.getInstance().createConnection();
            Statement  dbStatement  = dbConnection.createStatement();
    
            Vector<String> list = new Vector<String>();
            long start = System.currentTimeMillis();
            ResultSet rs = dbStatement.executeQuery(" select taxon from taxon where taxon like 's%'");
            rs.first();
            do
            {
                list.addElement(rs.getString(1));
                
            } while(rs.next());
            long end = System.currentTimeMillis();
            
            System.out.println(end - start + " Items: "+list.size());
            
            dbStatement.close();
            dbConnection.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    protected static void testLookUpLucene()
    {
        File lucenePath = getIndexDirPath(); // must be initialized here
        try
        {
            Vector<String> list     = new Vector<String>();
            IndexSearcher  searcher = new IndexSearcher(FSDirectory.getDirectory(lucenePath, false));
            
            long           start    = System.currentTimeMillis();
            Query query = new WildcardQuery(new Term("id", "s*"));
            
            //Query query = QueryParser.parse("", "id", new SimpleAnalyzer());
            Hits  hits  = searcher.search(query);
            
            for (int i=0;i<hits.length();i++)
            {
                list.addElement(hits.doc(i).get("id"));
            }
            long end = System.currentTimeMillis();
            
            System.out.println(end - start + " Items: "+list.size());
            

            
        //} catch (ParseException ex)
        //{
        //    ex.printStackTrace();
        //    
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * Helper function to return the path to the express search directory
     * @return return the path to the express search directory
     */
    public static File getIndexDirPath()
    {
        File path = new File(System.getProperty("user.home")+File.separator+"Specify"+File.separator+"taxa-index-dir");
        if (!path.exists())
        {
            if (!path.mkdirs())
            {
                String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                log.error(msg); 
                throw new RuntimeException(msg);
            }
        }
        return path;
    }    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        DBConnection dbConn = DBConnection.getInstance();
        dbConn.setUsernamePassword("rods", "rods");
        dbConn.setDriver("com.mysql.jdbc.Driver");
        dbConn.setConnectionStr("jdbc:mysql://localhost/");
        dbConn.setDatabaseName("demo_fish3");
        
        //buildTaxaSearch();
        
        /*
        testLookUpDB();
        testLookUpLucene();
        */
        
        //loadData();

        PickListTestApp pickListTestApp = new PickListTestApp();
        pickListTestApp.initialize();

    }

}
