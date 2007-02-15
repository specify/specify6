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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.persist.View;

/**
 *
 * This is used for launching editors for Database Objects that are at the "core" of the data model.
 * 
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class SystemSetupTask extends BaseTask
{
    // Static Data Members
    private static final Logger log  = Logger.getLogger(SystemSetupTask.class);

    public static final String     SYSTEMSETUPTASK        = "SystemSetup";
    public static final DataFlavor SYSTEMSETUPTASK_FLAVOR = new DataFlavor(SystemSetupTask.class, SYSTEMSETUPTASK);

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

            // Temporary
            NavBox sysNavBox = new NavBox("Core Data Objects");
            sysNavBox.add(NavBox.createBtnWithTT("Data Type", SYSTEMSETUPTASK, "", IconManager.IconSize.Std16, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(DataType.class, SYSTEMSETUPTASK, "DataType");
                }
            })); // I18N
            sysNavBox.add(NavBox.createBtnWithTT("Collection Obj Def", SYSTEMSETUPTASK, "", IconManager.IconSize.Std16, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(CollectionObjDef.class, SYSTEMSETUPTASK, "CollectionObjDef");
                }
            })); // I18N
            sysNavBox.add(NavBox.createBtnWithTT("Prep Type", "PrepType", "", IconManager.IconSize.Std16, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(PrepType.class, "PrepType", "PrepType");
                }
            })); // I18N
            sysNavBox.add(NavBox.createBtnWithTT("Determination Status", name, "", IconManager.IconSize.Std16, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(DeterminationStatus.class, name, "DeterminationStatus");
                }
            })); // I18N
            navBoxes.addElement(sysNavBox);
           

            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List pickLists = session.getDataList(PickList.class);

            sysNavBox = new NavBox(getResourceString("picklists"));

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
                addPickList(getTitle(pickList), pickList, "DeletePickList", sysNavBox.getItems().size()-1);

            }

            navBoxes.addElement(sysNavBox);
            
            session.close();
        }
    }
    
    /**
     * Searches for a SubPaneIFace that has the same class of data as the argument and then "shows" that Pane and returns true. 
     * If it can't be found then it shows false.
     * @param clazz the class of data to be searched for
     * @return true if found, false if not
     */
    protected boolean checkForPaneWithData(final Class clazz)
    {
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            Object uiComp = pane.getUIComponent();
            if (uiComp instanceof FormPane)
            {
                Object dataObj = ((FormPane)uiComp).getData();
                if (dataObj instanceof Collection<?>)
                {
                    Collection<?> collection = (Collection<?>)dataObj;
                    if (collection.size() > 0)
                    {
                        dataObj = collection.iterator().next();
                    }
                }
                if (dataObj != null && dataObj.getClass() == clazz)
                {
                    SubPaneMgr.getInstance().showPane(pane);
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void startEditor(final Class<?> clazz, final String iconName, final String viewName)
    {
        if (!checkForPaneWithData(clazz))
        {
            DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
            List<?>                  collection = session.getDataList(clazz);
            session.close();
            
            View view = AppContextMgr.getInstance().getView("SystemSetup", viewName);
            
            createFormPanel(view.getViewSetName(), 
                            view.getName(), 
                            "edit", 
                            collection, 
                            MultiView.RESULTSET_CONTROLLER,
                            IconManager.getIcon(iconName, IconManager.IconSize.Std16));
        } 
    }
    
    /**
     * Creates the edit form for Prep Types. 
     */
    protected void editDataTypes()
    {
        if (!checkForPaneWithData(DataType.class))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List prepTypes = session.getDataList(DataType.class);
            session.close();
            
            View view = AppContextMgr.getInstance().getView("SystemSetup", "PrepType");
            
            createFormPanel(view.getViewSetName(), 
                            view.getName(), 
                            "edit", 
                            prepTypes, 
                            MultiView.RESULTSET_CONTROLLER,
                            IconManager.getIcon(name, IconManager.IconSize.Std16));
        }
     }

    /**
     * Creates the edit form for Prep Types. 
     */
    protected void editCollectionObjDefs()
    {
        if (!checkForPaneWithData(CollectionObjDef.class))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List prepTypes = session.getDataList(CollectionObjDef.class);
            session.close();
            
            View view = AppContextMgr.getInstance().getView("SystemSetup", "PrepType");
            
            createFormPanel(view.getViewSetName(), 
                            view.getName(), 
                            "edit", 
                            prepTypes, 
                            MultiView.RESULTSET_CONTROLLER,
                            IconManager.getIcon(name, IconManager.IconSize.Std16));
        }
     }
    
    /**
     * Creates the edit form for Prep Types. 
     */
    protected void editPrepTypes()
    {
        if (!checkForPaneWithData(DeterminationStatus.class))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List prepTypes = session.getDataList(PrepType.class);
            session.close();
            
            View view = AppContextMgr.getInstance().getView("SystemSetup", "PrepType");
            
            createFormPanel(view.getViewSetName(), 
                            view.getName(), 
                            "edit", 
                            prepTypes, 
                            MultiView.RESULTSET_CONTROLLER,
                            IconManager.getIcon("PrepType", IconManager.IconSize.Std16));
        }
     }

    /**
     * Creates the edit form for Prep Types. 
     */
    protected void editDeterminationStatus()
    {
        if (!checkForPaneWithData(DeterminationStatus.class))
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            List determinationStatus = session.getDataList(DeterminationStatus.class);
            session.close();
            
            View view = AppContextMgr.getInstance().getView("SystemSetup", "DeterminationStatus");
            if (view != null)
            {
                createFormPanel(view.getViewSetName(), 
                                view.getName(), 
                                "edit", 
                                determinationStatus, 
                                MultiView.RESULTSET_CONTROLLER,
                                IconManager.getIcon(name, IconManager.IconSize.Std16));
            } else
            {
                // show error dialog
            }
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
        NavBoxButton     roc = (NavBoxButton)nbi;
        roc.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                NavBoxButton     roc = (NavBoxButton)ae.getSource();
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
    protected void addDraggableDataFlavors(NavBoxButton roc)
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
            log.warn(ex);
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
            log.warn(ex);
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
            if (((NavBoxButton)nbi).getLabelText().equals(boxName))
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
       // View view = appContextMgr.getView("SystemSetup", CollectionObjDef.getCurrentCollectionObjDef());
        //createFormPanel(view.getViewSetName(), view.getName(), "edit", infoRequest, MultiView.IS_NEW_OBJECT);

        //recentFormPane = new FormPane(null, name, this, "");
        //return recentFormPane;
        
        return starterPane = new SimpleDescPane(SYSTEMSETUPTASK, this, "System Tools");
    }

     /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
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
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        JMenuItem mi = UIHelper.createMenuItem(null, getResourceString("PickListsMenu"), getResourceString("PickListsMenu"), "", true, null);
        list.add(new MenuItemDesc(mi, "AdvMenu/SystemMenu"));
        
        final Taskable thisTask = this;
        mi = UIHelper.createMenuItem(null, "System Tools", "S", "", true, null);
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                thisTask.requestContext();
            }
        });
        list.add(new MenuItemDesc(mi, "AdvMenu/SystemMenu"));
        return list;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type LABELS.
     * @param cmdAction the command to be processed
     */
    protected void processSysSetupCommands(final CommandAction cmdAction)
    {
        Object data = cmdAction.getData() instanceof DroppableFormObject ? ((DroppableFormObject)cmdAction.getData()).getData() : cmdAction.getData();

        if (data instanceof PickList)
        {
            PickList pickList = (PickList)data;
            if (cmdAction.isAction("SavePickList"))
            {
                if (pickList.getTimestampCreated() == null)
                {
                    pickList.setTimestampCreated(new Date());
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

                String viewName = DBTableIdMgr.getDefaultFormNameById(DBTableIdMgr.getIdByShortName("picklist"));
                removePanelForData(SYSTEMSETUPTASK, viewName, pickList);

            } else if (cmdAction.isAction("DeletePickList"))
            {
                deletePickList(pickList);
                deletePickListFromUI(null, pickList);

                if (recentFormPane != null && recentFormPane.getData() != null)
                {
                    String viewName = DBTableIdMgr.getDefaultFormNameById(DBTableIdMgr.getIdByShortName("picklist"));
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(SYSTEMSETUPTASK))
        {
            processSysSetupCommands(cmdAction);
        }
    }

}
