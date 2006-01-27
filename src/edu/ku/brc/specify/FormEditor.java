/*
 * Filename:    $RCSfile: FormEditor.java,v $
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObj;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.EMailHelper;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.tests.forms.TestDataObj;
import edu.ku.brc.specify.tests.forms.TestDataSubObj;
import edu.ku.brc.specify.ui.ChooseFromListDlg;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.FormViewable;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.forms.persist.ViewSet;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms) 
 * 
 * @author rods
 *
 */
/**
 * @author rods
 *
 */
public class FormEditor
{
    private static Log log = LogFactory.getLog(FormEditor.class);
    
    protected JPanel contentPane;
    protected JFrame mainFrame;
    
    protected String currViewSetName;
    protected int    currFormId;
    
    protected Object            dataObj     = null;
    protected TestDataObj       testDataObj = null;
    protected List<TestDataObj> list        = new ArrayList<TestDataObj>();

    
    public FormEditor()
    {
    }
    
    /**
     * Create a form
     * @param formView the definition of the form to create
     */
    protected void createForm(FormView formView)
    {       
        FormViewable form = ViewFactory.createView(formView);
        
        contentPane.removeAll();
        Component comp = form.getUIComponent();
        if (comp != null)
        {
            comp.invalidate();   
            contentPane.add(comp, BorderLayout.CENTER);
            
            //SwingUtilities.invokeLater(new Runnable() {
            //    public void run() 
            //    {
                    contentPane.doLayout();
                    UICacheManager.forceTopFrameRepaint();
                    
                    mainFrame.invalidate();
                    mainFrame.doLayout();
                    mainFrame.repaint();
                    
                    // XXX Why???
                    mainFrame.pack();
                    mainFrame.setSize(new Dimension(1024, 764));
                    //frame.pack();
                    UIHelper.centerAndShow(mainFrame);
                    
                    boolean doSend = false;
                    if (doSend)
                    {
                        String msg = "<!-- NUM_ITEMS 1 --><form name=\"form\"><table><tr><td><!-- ITEM0 101 -->[  ]</td><td>Megalotis</td></tr></table><form>";
                        EMailHelper.sendMsg("imap.ku.edu", "rods", "Inverness1601*", "rods@ku.edu",  
                                "rods@ku.edu",  
                                "Catalog Items You Requested", 
                                 msg, "text/html", null);
                    } else
                    {
                        //EMailHelper.findRepliesFromResearch("imap.ku.edu", "rods", "Inverness1601*");
                    }
                    
                    DBConnection.setUsernamePassword("rods", "rods");
                    DBConnection.setDriver("com.mysql.jdbc.Driver");
                    DBConnection.setDBName("jdbc:mysql://localhost/demo_fish2");
                    
                    if (currViewSetName.equals("view valid") && currFormId == 0)
                    {
                        form.setDataObj(dataObj);
                        form.setDataIntoUI();
                        
                    } else if (currViewSetName.equals("Fish Views") && currFormId == 1)
                    {
                    
                    
                        //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class);
                        //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setFetchMode(Accession.class.getName(), FetchMode.DEFAULT).setMaxResults(300);
                        //java.util.List list = criteria.list();//session.find("from collev");
                        boolean skip = false;
                        if (skip)
                        {
                            Query q = HibernateUtil.getCurrentSession().createQuery("from accession in class Accession where accession.accessionId in (74,68262114,508322272)");
                            java.util.List list = q.list();
                            for (Object obj : list)
                            {
                                Accession accession = (Accession)obj;
                                System.out.println(accession.getAccessionId());
                            }
                        }
                        
                        
                        boolean doCatalogItems = true;
                        if (doCatalogItems)
                        {
                            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObj.class).setMaxResults(300);
                            //criteria.add(Expression.isNull("derivedFromId"));
                            
                            
                            java.util.List data = criteria.list();
                            System.out.println("Items Returned: "+data.size());
                            
                            dataObj = data;
                        }
                        
                        
                        
                        boolean doit = false;
                        if (doit)
                        {
                            //Query q = HibernateUtil.getCurrentSession().createQuery("from accession in class Accession where accession.accessionId in (74,68262114,508322272)");
                            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(InfoRequest.class);
                            java.util.List list = criteria.list();
                            for (Object obj : list)
                            {
                                InfoRequest infoReq = (InfoRequest)obj;
                                JXPathContext context = JXPathContext.newContext(infoReq);
                                
                                System.out.println(context.getValue(""));
                            }
                            
                        }
                        
                        form.setDataObj(dataObj);
                        form.setDataIntoUI();

                    }

                }
          //});
            
        //}

    }
    
    /**
     * 
     */
    protected void load()
    {
        // XXX Temporary load of form because now forma er being loaded right now
        try
        {
           ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("form.xml"));
           ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("fish_forms.xml"));
           ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("pref_forms.xml"));
            
        } catch (Exception ex)
        {
            log.fatal(ex);
            ex.printStackTrace();
        }
       
    
    }
    
    /**
     * 
     */
    protected void reload()
    {
        ViewMgr.clearAll();
        
        load();
        
        createForm(ViewMgr.getView(currViewSetName, currFormId));
    }
    
    /**
     * 
     */
    protected void selectForm()
    {
        List<FormView>    fullFormsList = new ArrayList<FormView>();
        
        for (ViewSet viewSet : ViewMgr.getViewSets())
        {

            List<FormView>    forms   = viewSet.getViews();
            fullFormsList.addAll(forms);
        }
        ChooseFromListDlg dlg = new ChooseFromListDlg("Choose Form", fullFormsList); // XXX I18N
        dlg.setVisible(true);
        
        
        FormView form = (FormView)dlg.getSelectedObject();
        if (form != null)
        {
            currViewSetName = form.getViewSetName();
            currFormId      = form.getId();
            
            createForm(form);
        }
    }
    
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize() 
    {
        AppPrefs.initialPrefs(); // Must be done first thing!
        
        for (int i=0;i<10;i++)
        {
            testDataObj = new TestDataObj();
            
            Set<Object> set = new HashSet<Object>();
            for (int j=0;j<4;j++)
            {
                TestDataSubObj subObj = new TestDataSubObj();
                subObj.setTextField("Sub Obj Item #"+Integer.toString(j));
                set.add(subObj);
            }
            
            if (i == 2)
            {
                testDataObj.setImagePathURL("");
            }
            
            testDataObj.setSubObjects(set);
            testDataObj.setTextField("Item #"+Integer.toString(i));
            list.add(testDataObj);
        }
       
        dataObj = list;
        
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
        contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true); //content panes must be opaque
        mainFrame.setContentPane(contentPane);

        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            //top.add(menuBar, BorderLayout.NORTH);
            mainFrame.setJMenuBar(menuBar);
        }

        load();
        
        // temp for testing 
        currFormId      = 0;
        currViewSetName =  "view valid";
        
        currFormId      = 1;
        currViewSetName =   "Fish Views";
        
       
        
        FormView form = ViewMgr.getView(currViewSetName, currFormId);

        if (form != null)
        {
            createForm(form);
        } else
        {
            log.info("Couldn't load form with name ["+currViewSetName+"] Id ["+currFormId+"]");
        }
         
    }
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;
        
        JMenu menu = createMenu(mb, "FileMenu", "FileMneu");
        mi = createMenuItem(menu, "Select Form", "s", "Select Form", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        selectForm();
                    }
                });
        
        mi = createMenuItem(menu, "Reload", "r", "Reload", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        reload();
                    }
                });
        
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
     * @param args
     */
    public static void main(String[] args) 
    {
        // TODO Auto-generated method stub
        FormEditor formEditor = new FormEditor();
        formEditor.initialize();

    }

}
