/*
 * Filename:    $RCSfile: PickListTestApp.java,v $
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.tests.forms.TestDataObj;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.JAutoCompComboBox;
import edu.ku.brc.specify.ui.db.JAutoCompTextField;
import edu.ku.brc.specify.ui.db.PickList;
import edu.ku.brc.specify.ui.db.PickListItem;

/**
 * The stand alone part of the PickListTestApp (this is a prototype at the moment that is used for viewing forms) 
 * 
 * @author rods
 *
 */
/**
 * @author rods
 *
 */
public class PickListTestApp
{
    private static Log log = LogFactory.getLog(PickListTestApp.class);
    
    protected JPanel contentPane;
    protected JFrame mainFrame;
    
    protected String currViewSetName;
    protected int    currFormId;
    
    protected Object            dataObj     = null;
    protected TestDataObj       testDataObj = null;
    protected List<TestDataObj> list        = new ArrayList<TestDataObj>();

    
    public PickListTestApp()
    {
    }

    
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize() 
    {
        AppPrefs.initialPrefs(); // Must be done first thing!
        
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
            FormLayout      formLayout = new FormLayout("p,1dlu,p,1dlu,p", "p,2dlu,p,2dlu,p");
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
          
          cb = JAutoCompComboBox.create("states", true, 50, false);
          
          // Create and register the key listener
          cb.setEditable(true); 
          cb.setCaseInsensitive(true);
          cb.setEnableAdditions(false);
          
          builder.add(new JLabel("Enter State:"), cc.xy(1,y));
          builder.add(cb, cc.xy(3,y));
          builder.add(new JLabel(" (Not Editable)"), cc.xy(5,y));
          y += 2;
          
          JAutoCompTextField text = JAutoCompTextField.create("test_textfield", false, 2, true);
          
          text.setAskBeforeSave(false);
          
          builder.add(new JLabel("Enter Text:"), cc.xy(1,y));
          builder.add(text, cc.xy(3,y));
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
            menu = (JMenu) menuBar.add(new JMenu(getResourceString(labelKey)));
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
     * 
     */
    public static void loadData()
    {
       
        Session session = HibernateUtil.getCurrentSession();
        
        try 
        {
            HibernateUtil.beginTransaction();
            
            Set<PickListItem> list = new HashSet<PickListItem>();
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
          
          for (int i=0;i<states.length;i++)
          {
              PickListItem pli = new PickListItem();
              System.out.println("["+states[i]+"]["+states[i+1]+"]");
              pli.setValue(states[i++]);
              pli.setTitle(states[i]);
              pli.setCreatedDate(new Date());
              list.add(pli);
          }
               
          PickList pl = new PickList();
          pl.setName("states");
          pl.setItems(list);
          pl.setSizeLimit(50); // doesn't matter when readonly
          pl.setReadOnly(true);
          pl.setCreated(new Date());
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
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        DBConnection.setUsernamePassword("rods", "rods");
        DBConnection.setDriver("com.mysql.jdbc.Driver");
        DBConnection.setDBName("jdbc:mysql://localhost/demo_fish2");
        
        //loadData();

        PickListTestApp pickListTestApp = new PickListTestApp();
        pickListTestApp.initialize();

    }

}
