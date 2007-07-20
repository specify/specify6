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

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * This task controls the data entry forms.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class DataEntryTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(DataEntryTask.class);

    public static final String     DATA_ENTRY        = "Data_Entry";
    public static final String     VIEW_WAS_OPENED   = "ViewWasOpened";
    public static final String     VIEW_WAS_SHOWN    = "ViewWasShown";
    public static final String     OPEN_NEW_VIEW     = "OpenNewView";
    public static final String     EDIT_DATA         = "Edit";
    public static final String     DATA              = "Data"; // Sent by FormHelper for when new DataObject are created
    
    public static final DataFlavor DATAENTRY_FLAVOR = new DataFlavor(DataEntryTask.class, "Data_Entry");
    
    protected static Hashtable<String, ImageIcon> iconForFormClass = new Hashtable<String, ImageIcon>();

    // Data Members
    protected Hashtable<Integer, ServiceInfo> services = new Hashtable<Integer, ServiceInfo>();
    
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();
    protected NavBox              viewsNavBox      = null;

    /**
     * Default Constructor
     *
     */
    public DataEntryTask()
    {
        super(DATA_ENTRY, getResourceString(DATA_ENTRY));
        
        CommandDispatcher.register(DATA_ENTRY, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(DATA, this);
        
        // Do this here instead of in initialize because the static method will need to access the icon mapping first
        viewsNavBox = new NavBox(getResourceString("CreateAndUpdate"));
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // Temporary
            NavBox navBox = new NavBox(getResourceString("Actions"));
            navBox.add(NavBox.createBtn(getResourceString("Series_Processing"), name, IconManager.IconSize.Std16));
            navBoxes.addElement(navBox);
           
            navBoxes.addElement(viewsNavBox);
        }
        isShowDefault = true;
    }
    
    /**
     * Common method for creating a consistent name for lookups in the Icon cache for forms.
     * @param viewSetName the viewSetName
     * @param viewName the view name
     * @return the full appended name
     */
    protected static String createFullName(final String viewSetName, final String viewName)
    {
        return viewSetName + "_" + viewName;
    }
    
    /**
     * Returns a icon defined by the view, if not found then it by the Class, if not found then it returns the one for the task
     * @param view the view 
     * @return the icon for the view
     */
    protected static ImageIcon getIconForView(final View view)
    {
        ImageIcon imgIcon = iconForFormClass.get(createFullName(view.getViewSetName(), view.getName()));
        if (imgIcon == null)
        {
            try
            {
                Class<?> clsObj = Class.forName(view.getClassName());
                imgIcon = IconManager.getIcon(clsObj.getSimpleName(), IconManager.IconSize.Std16);
                
            }
            catch (Exception ex)
            {
                // do nothing
            }
            
            if (imgIcon == null)
            {
                return IconManager.getIcon(DATA_ENTRY, IconManager.IconSize.Std16);
            }
        }
        return imgIcon;
    }

    /**
     * Opens a pane with a view to data. NOTE:If the data object is null and isNewForm = true then it will create a new dataObj.
     * @param task the owning Task
     * @param viewSetName the ViewSet Name
     * @param viewName the view's name 
     * @param mode the creation mode (View, Edit)
     * @param data the data to fill in , if data is null AND it is a "new" form than a new object is created and filled in
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public void openView(final Taskable         task, 
                         final String           viewSetName, 
                         final String           viewName, 
                         final String           mode, 
                         final FormDataObjIFace data,
                         final boolean          isNewForm)
    {
        View view = viewSetName == null ? SpecifyAppContextMgr.getInstance().getView(viewName, CollectionType.getCurrentCollectionType()) : 
                                          AppContextMgr.getInstance().getView(viewSetName, viewName);
        FormDataObjIFace  dataObj = data;
        if (dataObj == null)
        {
            if (isNewForm)
            {
                try
                {
                    dataObj = FormHelper.createAndNewDataObj(Class.forName(view.getClassName()));
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    throw new RuntimeException(ex);
                }
            }
            
        } else
        {
            DataProviderFactory.getInstance().evict(data.getClass());    
        }
        
        final FormPane formPane = new FormPane(view.getName(), task, view.getViewSetName(), viewName, mode, dataObj, 
                                         isNewForm ? (MultiView.IS_NEW_OBJECT |  MultiView.RESULTSET_CONTROLLER): 0);
        
        formPane.setIcon(getIconForView(view));
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                if (starterPane == null)
                {
                    addSubPaneToMgr(formPane);

                } else
                {
                    SubPaneMgr.getInstance().removePane(starterPane);
                    SubPaneMgr.getInstance().addPane(formPane);
                    starterPane = null;
                }

                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
            }
        });

    }

    /**
     * Opens a View and fills it with a single data object
     * @param mode the mode of how it is to be opened (View, Edit) 
     * @param idStr a string that contains the Integer Id (Primary Key) of the object to be shown
     */
    public static void openView(final Taskable task, final View view, final String mode, final String idStr)
    {
        int tableId = DBTableIdMgr.getInstance().getIdByClassName(view.getClassName());

        
        String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(tableId, Integer.parseInt(idStr));
        if (StringUtils.isNotEmpty(sqlStr))
        {
            try
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                List<?> data = session.getDataList(sqlStr);
                session.close();
                
                if (data != null && data.size() > 0)
                {
                    FormPane formPane = new FormPane(view.getName(), 
                                                     task, 
                                                     view.getViewSetName(), 
                                                     view.getName(), 
                                                     mode, 
                                                     data.get(0), 
                                                     MultiView.VIEW_SWITCHER);
                    formPane.setIcon(getIconForView(view));
    
                    CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                    
                } else
                {
                    // No Data Error
                }
    
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
        } else
        {
            log.error("Query String is empty for tableId["+tableId+"] idStr["+idStr+"]");
        }
    }

    /**
     * Create a form for a recordset.
     * @param task the task it belongs to
     * @param name the name
     * @param recordSet the record to create a form for
     * @return the FormPane
     */
    protected static FormPane createFormFor(final Taskable task, final String name, final RecordSetIFace recordSet)
    {
        FormPane formPane = null;
        
        // Look up and see if we already have a SubPane that is working on the RecordSet
        
        SubPaneIFace subPane = SubPaneMgr.getSubPaneWithRecordSet(recordSet);
        if (subPane == null)
        {
            String defaultFormName = DBTableIdMgr.getInstance().getDefaultFormNameById(recordSet.getDbTableId());

            if (StringUtils.isNotEmpty(defaultFormName))
            {
                SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
    
                
                View view = appContextMgr.getView(defaultFormName, CollectionType.getCurrentCollectionType());
                
                formPane = new FormPane(name, task, view, null, null, MultiView.VIEW_SWITCHER | MultiView.RESULTSET_CONTROLLER);
                formPane.setIcon(getIconForView(view));
                formPane.setRecordSet(recordSet);
                
                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                
            } else
            {
                log.error("No Default View for Table Id["+recordSet.getDbTableId()+"] from recordset");
                // XXX Need Error Dialog ??
            }

        } else
        {
            // formPane = subPane instanceof FormPane ? (FormPane) subPane : null;
            throw new RuntimeException("Ask Rod about getting here!");
        }
        
        return formPane;
    }
    
    /**
     * Reads the XML Definition of what Forms to load
     */
    protected void initializeViewsNavBox()
    {
        if (viewsNavBox.getCount() == 0)
        {
            try
            {
                SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();

                Element esDOM = AppContextMgr.getInstance().getResourceAsDOM("DataEntryTaskInit"); // Describes the definitions of the full text search
                if (esDOM != null)
                {
                    List<?> tables = esDOM.selectNodes("/views/view");
                    for ( Iterator<?> iter = tables.iterator(); iter.hasNext(); )
                    {
                        Element element     = (Element)iter.next();
                        String  nameStr     = getAttr(element, "name", "N/A");
                        String  iconname    = getAttr(element, "iconname", null);
                        
                        String  viewsetName = getAttr(element, "viewset", null);
                        String  viewName    = getAttr(element, "view", null);
                        
                        String  toolTip     = getAttr(element, "tooltip", null);
                        boolean sidebar     = getAttr(element, "sidebar", false);
                        
                        ImageIcon iconImage = IconManager.getIcon(iconname, IconManager.IconSize.Std16);
                        if (iconImage != null)
                        {
                            iconForFormClass.put(createFullName(viewsetName, viewName), iconImage);
                            
                        } else
                        {
                            log.error("Icon ["+iconname+"] could not be found.");
                        }
                        
                        View view = appContextMgr.getView(viewsetName, viewName);
                        if (view != null)
                        {
                            DBTableIdMgr.TableInfo ti = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                            if (ti != null)
                            {
                                CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                                cmdAction.setProperty("viewset", viewsetName);
                                cmdAction.setProperty("view",    viewName);
                                
                                ContextMgr.registerService(nameStr, ti.getTableId(), cmdAction, this, DATA_ENTRY, toolTip);
                                
                                if (sidebar)
                                {
                                    cmdAction = new CommandAction(DATA_ENTRY, OPEN_NEW_VIEW);
                                    cmdAction.setProperty("viewset", viewsetName);
                                    cmdAction.setProperty("view",    viewName);
                                    
                                    NavBoxAction nba = new NavBoxAction(cmdAction);
                                    
                                    NavBoxItemIFace nbi = NavBox.createBtnWithTT(nameStr, iconname, toolTip, IconManager.IconSize.Std16, nba);
                                    if (nbi instanceof NavBoxButton)
                                    {
                                        NavBoxButton roc = (NavBoxButton)nbi;
                                        
                                        // When Being Dragged
                                        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
                                        roc.addDragDataFlavor(DATAENTRY_FLAVOR);
                                
                                        // When something is dropped on it
                                        roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
                                    }
                
                                    viewsNavBox.add(nbi);
                                }
                                
                            } else
                            {
                                log.error("View's Class name["+view.getClassName()+"] was found in the DBTableIdMgr");
                            }
                            
                        } else
                        {
                            log.error("View doesn't exist viewset["+viewsetName+"] view["+viewName+"]");
                        }

                    }
                } else
                {
                    log.debug("Was unable to load resource [DataEntryTaskInit]");
                }
    
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();
        
        initializeViewsNavBox();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new DroppableFormRecordSetAccepter(title, this, "This is the Data Entry Pane");
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        String label = getResourceString(DATA_ENTRY);
        String iconName = name;
        String hint = getResourceString("dataentry_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label,iconName,hint);

        list.add(new ToolBarItemDesc(btn));

        return list;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    protected void openNewView(@SuppressWarnings("unused") final FormPane formPane)
    {
        // nothing
    }
    
    /**
     * Adjust a new Data object before it is set into a form.
     * @param dataObj the new data object.
     */
    protected void adjustNewDataObject(final Object dataObj)
    {
        if (dataObj != null && dataObj instanceof FormDataObjIFace)
        {
            if (dataObj instanceof CollectionObject)
            {
                CollectionObject colObj = (CollectionObject)dataObj;
                if (colObj.getCollection() == null)
                {
                    Collection catSeries = Collection.getCurrentCollection();
                    colObj.setCollection(catSeries); 
                }
                
                if (colObj.getCollectingEvent() == null)
                {
                    CollectingEvent ce = new CollectingEvent();
                    ce.initialize();
                    colObj.setCollectingEvent(ce);
                    ce.getCollectionObjects().add(colObj);
                    
                    Agent agent = SpecifyUser.getCurrentUser().getAgent();
                    if (agent != null)
                    {
                        colObj.setCataloger(agent);
                    }
                }

            } else if (dataObj instanceof Preparation)
            {
                Preparation prep = (Preparation)dataObj;
                if (prep.getPreparedByAgent() == null)
                {
                    prep.setPreparedByAgent(SpecifyUser.getCurrentUser().getAgent());
                    
                    SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                    String               prepTitle     = UIRegistry.getLocalizedMessage("CHOOSE_DEFAULT_OBJECT", PrepType.class.getSimpleName());
                    FormDataObjIFace     defPrepType   = appContextMgr.getDefaultObject(PrepType.class, "PrepType", prepTitle, true, true);
                    prep.setPrepType((PrepType)defPrepType);
                }
            }
        }
    }
    
    /**
     * Processes all Commands of type DATA_ENTRY.
     * @param cmdAction the command to be processed
     */
    protected void processDataEntryCommands(final CommandAction cmdAction)
    {
    
        if (cmdAction.isAction(OPEN_NEW_VIEW))
        {
            String viewSetName = cmdAction.getPropertyAsString("viewset");
            String viewName   = cmdAction.getPropertyAsString("view");
            openView(this, viewSetName, viewName, "edit", null, true);
            
        } else if (cmdAction.isAction(EDIT_DATA))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                addSubPaneToMgr(createFormFor(this, name, (RecordSetIFace)cmdAction.getData()));
                
            } else if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                if (dataList.length != 3)
                {
                    View   view = (View)dataList[0];
                    String mode = (String)dataList[1];
                    String idStr = (String)dataList[2];
                    openView(this, view, mode, idStr);
                    
                } else
                {
                    log.error("The Edit Command was sent with an object Array that was not 3 components!");
                }
            } else
            {
                log.error("The Edit Command was sent that didn't have data that was a RecordSet or an Object Array");
            }
            
        } else if (cmdAction.isAction("ShowView"))
        {
            if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                View   view = (View)dataList[0];
                String mode = (String)dataList[1];
                String idStr = (String)dataList[2];
                openView(this, view, mode, idStr);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(DATA_ENTRY))
        {
            processDataEntryCommands(cmdAction);
            
        } else if (cmdAction.isType(DataEntryTask.DATA) && cmdAction.isAction("NewObjDataCreated"))
        {
            adjustNewDataObject(cmdAction.getData());
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            viewsNavBox.clear();
            ContextMgr.removeServicesByTask(this);
            initializeViewsNavBox();
        }
            

    }
    
    // Class for accepting RecordSet Drops
    class DroppableFormRecordSetAccepter extends FormPane
    {
       
        public DroppableFormRecordSetAccepter(final String   name, 
                                              final Taskable task,
                                              final String   desc)
        {
            super(name, task, desc);
            
            dropFlavors.add(RecordSetTask.RECORDSET_FLAVOR);
            this.createMouseInputAdapter();
            this.icon = IconManager.getIcon(DATA_ENTRY, IconManager.IconSize.Std16);
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void doAction(GhostActionable src)
        {
            Object srcData = src.getData();
            if (srcData instanceof RecordSet)
            {
                addSubPaneToMgr(createFormFor(task, "XXXX", (RecordSetIFace)srcData));
            }
        }
        
        @Override
        public void doCommand(CommandAction cmdAction)
        {
            // nothing going on for now (should probably be removed)
        }
    }
    
}
