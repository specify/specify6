/* Filename:    $RCSfile: SystemSetupTask.java,v $
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

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.hibernate.Criteria;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.core.NavBoxMgr;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.DroppableFormObject;
import edu.ku.brc.specify.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.specify.tasks.subpane.FormPane;
import edu.ku.brc.specify.tasks.subpane.PickListProcessor;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.Trash;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.PickList;



/**
 *
 *
 * @author rods
 *
 */
public class SystemSetupTask extends BaseTask
{
    // Static Data Members
    //private static Log log  = LogFactory.getLog(SystemSetupTask.class);

    public static final String     SYSTEMSETUPTASK        = "SystemSetup";
    public static final DataFlavor SYSTEMSETUPTASK_FLAVOR = new DataFlavor(SystemSetupTask.class, SYSTEMSETUPTASK);

    public static final String INFO_REQ_MESSAGE = "Specify Info Request";

    List<String> pickListNames = new ArrayList<String>();

    // Data Members
    protected NavBox navBox = null;

    /**
     * Default Constructor
     *
     */
    public SystemSetupTask()
    {
        super(SYSTEMSETUPTASK, getResourceString(SYSTEMSETUPTASK));
        CommandDispatcher.register(SYSTEMSETUPTASK, this);
    }

    /**
     * Returns a title for the PickList
     * @param pickList the pickList to construct a title for
     * @return Returns a title for the pickList
     */
    protected String getTitle(final PickList pickList)
    {
        return pickList.getName();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(PickList.class);
            List pickLists = criteria.list();

            navBox = new NavBox(getResourceString("picklists"));

            addPickList(getResourceString("newpicklist"), null, null, 0);

            // Get all the pickList names
            for (Iterator iter=pickLists.iterator();iter.hasNext();)
            {
                PickList pickList = (PickList)iter.next();
                pickListNames.add(pickList.getName());

            }


            for (Iterator iter=pickLists.iterator();iter.hasNext();)
            {
                PickList pickList = (PickList)iter.next();
                addPickList(getTitle(pickList), pickList, "DeletePickList", navBox.getItems().size()-1);

            }

            navBoxes.addElement(navBox);
            HibernateUtil.closeSession();
        }
    }

    /**
     * Add an PickList Item to the box
     * @param pickList the pickList to be added
     */
    protected void addPickList(final String title, final PickList pickList, final String delCmd, final int position)
    {
        // These value should not be hard coded here
        int                 pickListTableId = DBTableIdMgr.lookupIdByShortName("picklist");
        DroppableFormObject dfo = new DroppableFormObject(SYSTEMSETUPTASK, pickListTableId, pickList);
        NavBoxItemIFace     nbi = addNavBoxItem(navBox, title, SYSTEMSETUPTASK, delCmd, dfo, position);
        RolloverCommand     roc = (RolloverCommand)nbi;
        roc.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                RolloverCommand     roc = (RolloverCommand)ae.getSource();
                DroppableFormObject dfo = (DroppableFormObject)roc.getData();

                FormPane formPane;
                if (dfo.getData() == null) // null means we add a new one
                {
                    PickList pickList = new PickList();
                    pickList.setCreated(null); // this tells us later that it is a new object
                    pickList.setItems(new HashSet());
                    formPane = createFormPanel(dfo.getViewSetName(), DBTableIdMgr.lookupDefaultFormNameById(dfo.getFormId()), null, pickList);

                } else
                {
                    formPane = createFormPanel(roc);
                }

                if (formPane != null && formPane.getFormProcessor() == null)
                {
                    formPane.setFormProcessor(new PickListProcessor(pickListNames));
                }
            }
        });
        addDraggableDataFlavors(roc);
    }

    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(RolloverCommand roc)
    {
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(DroppableTaskPane.DROPPABLE_PANE_FLAVOR);
    }

    /**
     * Save a info request
     * @param pickList the ir to be saved
     */
    /**
     * Save a info request
     * @param pickList the ir to be saved
      */
    public void savePickList(final PickList pickList)
    {

        //pickList.setTimestampModified(Calendar.getInstance().getTime());

        // save to database
        HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().saveOrUpdate(pickList);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

    }

    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deletePickList(final PickList pickList)
    {
        // delete from database
        HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();
        HibernateUtil.getCurrentSession().delete(pickList);
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();

    }

    /**
     * Return a NavBoxItem by name
     * @param boxName the name of the NavBoxItem
     * @return Return a NavBoxItem by name
     */
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

    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace.
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deletePickListFromUI(final NavBoxItemIFace boxItem, final PickList pickList)
    {

        NavBoxItemIFace nb = boxItem != null ? boxItem : getBoxByName(getTitle(pickList));
        if (nb != null)
        {
            navBox.remove(nb);

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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        recentFormPane = new FormPane(name, this, "Drop Me");
        return recentFormPane;
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

        //ToolBarDropDownBtn btn = createToolbarButton(SYSTEMSETUPTASK, "information.gif", "inforequest_hint");
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

        JMenuItem mi = UIHelper.createMenuItem(null, getResourceString("PickListsMenu"), getResourceString("PickListsMenu"), "", true, null);
        list.add(new MenuItemDesc(mi, "AdvMenu/SystemMenu"));
        return list;

    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {

        Object data = cmdAction.getData() instanceof DroppableFormObject ? ((DroppableFormObject)cmdAction.getData()).getData() : cmdAction.getData();

        if (data instanceof PickList)
        {
            PickList pickList = (PickList)data;
            if (cmdAction.getAction().equals("SavePickList"))
            {
                if (pickList.getCreated() == null)
                {
                    pickList.setCreated(new Date());
                    addPickList(getTitle(pickList), pickList, "DeletePickList", navBox.getItems().size()-1);

                    navBox.invalidate();
                    navBox.setSize(navBox.getPreferredSize());
                    navBox.doLayout();
                    navBox.repaint();
                    NavBoxMgr.getInstance().invalidate();
                    NavBoxMgr.getInstance().doLayout();
                    NavBoxMgr.getInstance().repaint();
                    UICacheManager.forceTopFrameRepaint();
                    pickListNames.add(pickList.getName());
                }

                savePickList(pickList);

                String viewName = DBTableIdMgr.lookupDefaultFormNameById(DBTableIdMgr.lookupIdByShortName("picklist"));
                removePanelForData(SYSTEMSETUPTASK, viewName, pickList);

            } else if (cmdAction.getAction().equals("DeletePickList"))
            {
                deletePickList(pickList);
                deletePickListFromUI(null, pickList);

                if (recentFormPane != null && recentFormPane.getData() != null)
                {
                    String viewName = DBTableIdMgr.lookupDefaultFormNameById(DBTableIdMgr.lookupIdByShortName("picklist"));
                    removePanelForData(SYSTEMSETUPTASK, viewName, pickList);
                }

                int inx = Collections.binarySearch(pickListNames, pickList.getName());
                if (inx > -1)
                {
                    pickListNames.remove(inx);
                }

            }
        }
    }

}
