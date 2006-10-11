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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.PickList;



/**
 *
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class SystemSetupTask extends BaseTask
{
    // Static Data Members
    //private static final Logger log  = Logger.getLogger(SystemSetupTask.class);

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

            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List pickLists = session.getDataList(PickList.class);

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
            
            session.close();
        }
    }

    /**
     * Add an PickList Item to the box
     * @param pickList the pickList to be added
     */
    protected void addPickList(final String titleArg, final PickList pickList, final String delCmd, final int position)
    {/*
        // These value should not be hard coded here
        int                 pickListTableId = DBTableIdMgr.lookupIdByShortName("picklist");
        DroppableFormObject dfo = new DroppableFormObject(SYSTEMSETUPTASK, pickListTableId, pickList);
        NavBoxItemIFace     nbi = addNavBoxItem(navBox, titleArg, SYSTEMSETUPTASK, delCmd, dfo, position);
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
                    pickList.initialize();
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
        addDraggableDataFlavors(roc);*/
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
     * Save a ipickList
     * @param pickList the pickList to be saved
      */
    public void savePickList(final PickList pickList)
    {

        //pickList.setTimestampModified(Calendar.getInstance().getTime());

        // save to database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.attach(pickList);
            session.saveOrUpdate(pickList);
            session.commit();
            
        } catch (Exception ex)
        {
            System.err.println(ex);
        }
        session.close();

    }

    /**
     * Delete a picklist
     * @param pickList the pickList to be deleted
     */
    protected void deletePickList(final PickList pickList)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.attach(pickList);
            session.delete(pickList);
            session.commit();
            
        } catch (Exception ex)
        {
            System.err.println(ex);
        }
        session.close();

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
    @Override
    public SubPaneIFace getStarterPane()
    {
        recentFormPane = new FormPane(null, name, this, "");
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
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
