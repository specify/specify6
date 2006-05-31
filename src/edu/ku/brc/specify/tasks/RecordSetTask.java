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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.core.NavBoxMgr;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.Trash;
import edu.ku.brc.specify.ui.UICacheManager;
/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 *
 * @author rods
 *
 */
public class RecordSetTask extends BaseTask
{
    // Static Data Members
    public static final String RECORD_SET = "Record_Set";
    public static final DataFlavor RECORDSET_FLAVOR = new DataFlavor(RecordSetTask.class, "RECORD_SET");

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


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
            List recordSets = criteria.list();

            navBox = new NavBox(title);

            for (Iterator iter=recordSets.iterator();iter.hasNext();)
            {
                RecordSet recordSet = (RecordSet)iter.next();
                addDraggableDataFlavors(addNavBoxItem(navBox, recordSet.getName(), RECORD_SET, "Delete", recordSet));
            }
            navBoxes.addElement(navBox);
            HibernateUtil.closeSession();

        }
    }

    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(NavBoxItemIFace nbi)
    {
        RolloverCommand roc = (RolloverCommand)nbi;
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        roc.addActionListener(new RecordSetSelectedAction((RecordSet)roc.getData()));
    }

    /**
     * Save a record set
     * @param recordSet the rs to be saved
     */
    public void saveRecordSet(final RecordSet recordSet)
    {
        NavBoxItemIFace nbi = addNavBoxItem(navBox, recordSet.getName(), "Record_Set", "Delete", recordSet);

        addDraggableDataFlavors(nbi);

        recordSet.setTimestampCreated(Calendar.getInstance().getTime());

        // save to database
        //HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().saveOrUpdate(recordSet);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

        NavBoxMgr.getInstance().addBox(navBox);

        // XXX This needs to be made generic
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
        Session session = HibernateUtil.getCurrentSession();
        session.lock(recordSet, LockMode.NONE);
        HibernateUtil.beginTransaction();

        HibernateUtil.getCurrentSession().delete(recordSet);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();
    }

    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace.
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteRecordSetFromUI(final NavBoxItemIFace boxItem, final RecordSet recordSet)
    {
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByTitle(navBox, recordSet.getName()).getUIComponent();
        if (comp != null)
        {
            navBox.remove(comp);

            // XXX this is pathetic and needs to be generized
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

    /**
     * Returns a list of RecordSets for a given Table Id (never returns null)
     * @param tableId the matching tableId or -1 if all the recordsets should be returned
     * @return a list of recordsets (never returns null)
     */
    public List<RecordSet> getRecordSets(final int tableId)
    {
        List<RecordSet> list = new ArrayList<RecordSet>();
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            RecordSet rs = (RecordSet)nbi.getData();
            if (tableId == -1 || tableId == rs.getTableId())
            {
                list.add(rs);
            }
        }
        return list;
    }

    /**
     * Returns all the recordsets (never returns null)
     * @return all the recordsets (never returns null)
     */
    public List<RecordSet> getRecordSets()
    {
        return getRecordSets(-1);
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
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        //ToolBarDropDownBtn btn = createToolbarButton(RECORD_SET,   "dataentry.gif",    "dataentry_hint");
        //list.add(new ToolBarItemDesc(btn));

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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                String rsName  = JOptionPane.showInputDialog(UICacheManager.get(UICacheManager.FRAME), getResourceString("AskForRSName"));
                if (isNotEmpty(rsName))
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
            deleteRecordSetFromUI(null, recordSet);

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
    class RecordSetSelectedAction implements ActionListener
    {
        private RecordSet rs;

        public RecordSetSelectedAction(final RecordSet rs)
        {
            this.rs = rs;
        }

        public void actionPerformed(ActionEvent e)
        {
            CommandDispatcher.dispatch(new CommandAction(RECORD_SET, "Selected", rs));
        }

    }


}
