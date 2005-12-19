/* Filename:    $RCSfile: QueryTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.core;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.hibernate.Criteria;

import edu.ku.brc.specify.core.subpane.SimpleDescPane;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostActionableDropManager;
import edu.ku.brc.specify.ui.dnd.GhostMouseDropAdapter;
/**
 * 
 * @author rods
 *
 */
public class RecordSetTask extends BaseTask
{
    // Static Data Members
    public static final String RECORD_SET = "Record_Set";
    
    // Data Members
    protected NavBox navBox = null;
    
    /**
     * Default Constructor
     *
     */
    public RecordSetTask()
    {
        super(RECORD_SET, getResourceString(RECORD_SET));
        CommandDispatcher.register(RECORD_SET, this);
    }
    
    /**
     * Helper method to add an item to the navbox
     * @param recordSet the recordset to be added
     */
    protected NavBoxItemIFace addNavBoxItem(final RecordSet recordSet)
    {
        NavBoxItemIFace nb = NavBox.createBtn(recordSet.getName(), name, IconManager.IconSize.Std16);
        RolloverCommand rb = (RolloverCommand)nb;
        
        JPopupMenu popupMenu = rb.getPopupMenu();
        
        JMenuItem renameMenuItem = new JMenuItem(getResourceString("Delete"));
        renameMenuItem.addActionListener(new RSAction("delete", nb, recordSet));
        popupMenu.add(renameMenuItem);
        
        navBox.add(nb);
        
        if (nb instanceof GhostActionable)
        {
            GhostActionable ga = (GhostActionable)nb;
            ga.createMouseDropAdapter();
            ga.setData(recordSet);
            GhostMouseDropAdapter gpa = ga.getMouseDropAdapter();  
            gpa.addGhostDropListener(new GhostActionableDropManager(UICacheManager.getGlassPane(), NavBoxMgr.getTrash(), ga));

        }
        return nb;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize();super.initialize(); // sets isInitialized to false
            
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
            List recordSets = criteria.list();
              
            navBox = new NavBox(title);
            
            for (Iterator iter=recordSets.iterator();iter.hasNext();)
            {
                RecordSet recordSet = (RecordSet)iter.next();
                addNavBoxItem(recordSet);
                
            }          
            navBoxes.addElement(navBox);
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * Save a record set
     * @param recordSet the rs to be saved
     */
    public void saveRecordSet(final RecordSet recordSet)
    {
        NavBoxItemIFace nbi = addNavBoxItem(recordSet);
        
        recordSet.setCreated(Calendar.getInstance().getTime());
        
        // save to database
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().saveOrUpdate(recordSet);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();
        
        NavBoxMgr.getInstance().addBox(navBox);
        
        navBox.invalidate();
        navBox.doLayout();
        navBox.repaint();
        
        
        CommandDispatcher.dispatch(new CommandAction("Labels", "NewRecordSet", nbi));
    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteRecordSet(final RecordSet recordSet)
    {
        // delete from database
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().delete(recordSet);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();       
    }
    
    protected NavBoxItemIFace getBoxByName(final String boxName)
    {
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            if (((RolloverCommand)nbi).getLabelText().equals(boxName))
            {
                return nbi;
            }
        }
        return null;
    }
    
    protected void deleteRecordSetFromUI(final RecordSet recordSet, final NavBoxItemIFace boxItem)
    {
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByName(recordSet.getName()).getUIComponent(); 
        if (comp != null)
        {
            navBox.remove(comp);
            
            navBox.invalidate();
            navBox.setSize(navBox.getPreferredSize());
            navBox.doLayout();
            navBox.repaint();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UICacheManager.forceTopFrameRepaint();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "This is the Data Entry Pane");
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public String getName()
    {
        return RECORD_SET; // XXX Localize, Hmmm maybe not????
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        
        //ToolBarDropDownBtn btn = createToolbarButton(RECORD_SET,   "dataentry.gif",    "dataentry_hint");
        //list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        
        return list;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;
        
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                String rsName  = JOptionPane.showInputDialog(UICacheManager.getInstance().get(UICacheManager.FRAME), getResourceString("AskForRSName"));
                if (rsName != null)
                {
                    RecordSet rs = (RecordSet)data;
                    rs.setName(rsName);
                    saveRecordSet(rs);
                }
            }
        } else if (cmdAction.getAction().equals("Delete") && cmdAction.getData() instanceof RecordSet)
        {
            RecordSet recordSet = (RecordSet)cmdAction.getData();
            deleteRecordSet(recordSet);
            deleteRecordSetFromUI(recordSet, null);

        }
    }
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
 
     /**
     * 
     * @author rods
     *
     */
    class RSAction implements ActionListener 
    {
        private String   cmd;
        private RecordSet       recordSet = null;
        private NavBoxItemIFace boxItem   = null;
        
        
        public RSAction(final String cmd, final NavBoxItemIFace boxItem, final RecordSet recordSet)
        {
            this.cmd       = cmd;
            this.recordSet = recordSet;
            this.boxItem   = boxItem;
        }
        
        public void actionPerformed(ActionEvent e) 
        {
            if (cmd.equals("delete"))
            {
                deleteRecordSet(recordSet);
                deleteRecordSetFromUI(recordSet, null);
            }
        }
        
        public RecordSet getRecordSet()
        {
            return recordSet;
        }
    }
 
   
}
