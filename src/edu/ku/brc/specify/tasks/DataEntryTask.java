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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

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
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.persist.ViewLoader;

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
    //public static final String     EDIT_IN_DIALOG    = "EditInDialog";
    public static final String     DATA              = "Data"; // Sent by FormHelper for when new DataObject are created
    
    public static final DataFlavor DATAENTRY_FLAVOR = new DataFlavor(DataEntryTask.class, "Data_Entry");
    
    protected static Hashtable<String, ImageIcon> iconForFormClass = new Hashtable<String, ImageIcon>();

    // Data Members
    protected Hashtable<Integer, ServiceInfo> services = new Hashtable<Integer, ServiceInfo>();
    
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();
    protected NavBox              viewsNavBox      = null;
    
    protected Vector<DataEntryView> stdViews       = null;
    protected Vector<DataEntryView> miscViews      = null;
    
    // These are needed for changes with the DisciplineType icon
    protected NavBoxButton        colObjNavBtn        = null;
    protected String              iconClassLookUpName = "";

    /**
     * Default Constructor
     *
     */
    public DataEntryTask()
    {
        super(DATA_ENTRY, getResourceString(DATA_ENTRY));
        
        CommandDispatcher.register(DATA_ENTRY, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET , this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(DATA, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
        
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

            // No Series Processing
            //NavBox navBox = new NavBox(getResourceString("Actions"));
            //navBox.add(NavBox.createBtn(getResourceString("Series_Processing"), name, IconManager.IconSize.Std16));
            //navBoxes.add(navBox);
           
            navBoxes.add(viewsNavBox);
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
    protected static ImageIcon getIconForView(final ViewIFace view)
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
        ViewIFace view = viewSetName == null ? SpecifyAppContextMgr.getInstance().getView(viewName, Discipline.getCurrentDiscipline()) : 
                                          AppContextMgr.getInstance().getView(viewSetName, viewName);
        Object           dataObj     = data;
        FormDataObjIFace formDataObj = data;
        if (formDataObj == null)
        {
            if (isNewForm)
            {
                try
                {
                    Vector<Object> dataObjList = new Vector<Object>();
                    formDataObj = FormHelper.createAndNewDataObj(Class.forName(view.getClassName()));
                    dataObjList.add(formDataObj);
                    dataObj = dataObjList;
                    //dataObj = formDataObj;
                    
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
                                         isNewForm ? (MultiView.IS_NEW_OBJECT | MultiView.RESULTSET_CONTROLLER): 0);
        
        formPane.setIcon(getIconForView(view));
        
        if (isNewForm)
        {
            formPane.initSubViews();
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                if (starterPane == null)
                {
                    SubPaneIFace curPane = SubPaneMgr.getInstance().getCurrentSubPane();
                    if (curPane instanceof DroppableFormRecordSetAccepter)
                    {
                        SubPaneMgr.getInstance().replacePane(curPane, formPane);
                    }
                    addSubPaneToMgr(formPane);

                } else
                {

                    //SubPaneMgr.getInstance().removePane(starterPane);
                   //SubPaneMgr.getInstance().addPane(formPane);
                    SubPaneMgr.getInstance().replacePane(starterPane, formPane);
                    starterPane = null;
                }

                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                
                formPane.focusFirstFormControl();
            }
        });

    }

    /**
     * Opens a View and fills it with a single data object
     * @param mode the mode of how it is to be opened (View, Edit) 
     * @param idStr a string that contains the Integer Id (Primary Key) of the object to be shown
     */
    public static void openView(final Taskable task, final ViewIFace view, final String mode, final String idStr)
    {
        int tableId = DBTableIdMgr.getInstance().getIdByClassName(view.getClassName());
        String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(tableId, Integer.parseInt(idStr));
        if (StringUtils.isNotEmpty(sqlStr))
        {
            try
            {
                List<?> data = null;
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    data = session.getDataList(sqlStr);
                }
                catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                
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
    protected static FormPane createFormFor(final Taskable task, 
                                            final String name, 
                                            final String viewSetName,
                                            final String viewName,
                                            final RecordSetIFace recordSet)
    {
        FormPane formPane = null;
        
        // Look up and see if we already have a SubPane that is working on the RecordSet
        
        SubPaneIFace subPane = SubPaneMgr.getSubPaneWithRecordSet(recordSet);
        if (subPane == null)
        {
            ViewIFace view = null;
            
            if (StringUtils.isNotEmpty(viewSetName) && StringUtils.isNotEmpty(viewName))
            {
                view = viewSetName == null ? SpecifyAppContextMgr.getInstance().getView(viewName, Discipline.getCurrentDiscipline()) : 
                                             AppContextMgr.getInstance().getView(viewSetName, viewName);
            } else
            {
                String defaultFormName = DBTableIdMgr.getInstance().getDefaultFormNameById(recordSet.getDbTableId());
    
                if (StringUtils.isNotEmpty(defaultFormName))
                {
                    SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                    
                    view = appContextMgr.getView(defaultFormName, Discipline.getCurrentDiscipline());
                     
                } else
                {
                    log.error("No Default View for Table Id["+recordSet.getDbTableId()+"] from recordset");
                    // XXX Need Error Dialog ??
                }
            }
            
            if (view != null)
            {
                formPane = new FormPane(name, task, view, null, null, MultiView.VIEW_SWITCHER | MultiView.RESULTSET_CONTROLLER);
                formPane.setIcon(getIconForView(view));
                formPane.setRecordSet(recordSet);
                
                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                
            } else
            {
                throw new RuntimeException("The view was null and shouldn't be!");
            }
            
        } else
        {
            formPane = subPane instanceof FormPane ? (FormPane) subPane : null;
            //throw new RuntimeException("Ask Rod about getting here!");
        }
        
        return formPane;
    }
    
    /**
     * @param serviceList
     */
    protected void buildFormNavBoxes(final Vector<DataEntryView> serviceList)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        for (DataEntryView dev : serviceList)
        {
            boolean isColObj = dev.getName().equals("Collection Object");
            
            ImageIcon iconImage = IconManager.getIcon(dev.getIconName(), IconManager.IconSize.Std16);
            if (iconImage != null)
            {
                String iconName = createFullName(dev.getViewSet(), dev.getView());
                iconForFormClass.put(iconName, iconImage);
                if (isColObj)
                {
                    iconClassLookUpName = iconName;
                }
                
            } else
            {
                log.error("Icon ["+dev.getIconName()+"] could not be found.");
            }
            
            ViewIFace view = appContextMgr.getView(dev.getViewSet(), dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                //System.out.println(view.getClassName()+" "+ti);
                if (tableInfo != null)
                {
                    CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                    cmdAction.setProperty("viewset", dev.getViewSet());
                    cmdAction.setProperty("view",    dev.getView());
                    
                    ContextMgr.registerService(dev.getName(), tableInfo.getTableId(), cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true);
                    
                    if (dev.isSideBar())
                    {
                        cmdAction = new CommandAction(DATA_ENTRY, OPEN_NEW_VIEW);
                        cmdAction.setProperty("viewset",   dev.getViewSet());
                        cmdAction.setProperty("view",      dev.getView());
                        cmdAction.setProperty("tableInfo", dev.getTableInfo());
                        
                        NavBoxAction nba = new NavBoxAction(cmdAction);
                        
                        NavBoxItemIFace nbi = NavBox.createBtnWithTT(dev.getName(), dev.getIconName(), dev.getToolTip(), IconManager.IconSize.Std16, nba);
                        if (nbi instanceof NavBoxButton)
                        {
                            NavBoxButton nbb = (NavBoxButton)nbi;
                            if (isColObj)
                            {
                               colObjNavBtn = nbb; 
                            }
                            
                            //RolloverCommand roc = (RolloverCommand)nbb;
                            //roc.setData(cmdAction);
                            
                            // When Being Dragged
                            nbb.addDragDataFlavor(Trash.TRASH_FLAVOR);
                            //roc.addDragDataFlavor(DATAENTRY_FLAVOR);
                            nbb.addDragDataFlavor(new DataFlavorTableExt(DataEntryTask.class, "Data_Entry", tableInfo.getTableId()));
                    
                            // When something is dropped on it
                            //roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
                            nbb.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, "RECORD_SET", tableInfo.getTableId()));//RecordSetTask.RECORDSET_FLAVOR);

                        }
    
                        viewsNavBox.add(nbi);
                    }
                    
                } else
                {
                    log.error("View's Class name["+view.getClassName()+"] was found in the DBTableIdMgr");
                }
                
            } else
            {
                log.error("View doesn't exist viewset["+dev.getViewSet()+"] view["+dev.getView()+"]");
            }
        }
    }
    
    /**
     * Initializes the TableInfo data member.
     * @param list the list of DataEntryView
     */
    protected void initDataEntryViews(final Vector<DataEntryView> list)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        for (DataEntryView dev : list)
        {
            ViewIFace view = appContextMgr.getView(dev.getViewSet(), dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                cmdAction.setProperty("viewset", dev.getViewSet());
                cmdAction.setProperty("view",    dev.getView());
                
                ContextMgr.registerService(dev.getName(), tableInfo.getTableId(), cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true);

            } else
            {
                log.debug("Couldn't find["+dev.getViewSet()+"]["+dev.getView()+"]");
            }
        }
    }
    
    /**
     * @return an action listener for Misc Form button.
     */
    protected ActionListener createMiscActionListener()
    {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (e instanceof DataActionEvent)
                {
                    DataActionEvent dataActionEv = (DataActionEvent)e;
                    
                    if (dataActionEv.getSourceObj() != null)
                    {
                        Object data = dataActionEv.getSourceObj().getData();
                        if (data instanceof RecordSet)
                        {
                            RecordSet rs = (RecordSet)data;
                            for (DataEntryView deView : miscViews)
                            {
                                if (deView.getTableInfo().getTableId() == rs.getDbTableId())
                                {
                                    editData(rs, deView.getViewSet(), deView.getView());
                                    return;
                                }
                            }
                        } else
                        {
                            showMiscViewsDlg();
                        }
                    }
                } else
                {
                    showMiscViewsDlg();
                }
            }
        };
    }
    
    /**
     * Use XStream to read in the DataEntryViews and add them to the UI.
     */
    protected void initializeViewsNavBoxFromXML()
    {
        boolean cacheDoVerify = ViewLoader.isDoFieldVerification();
        ViewLoader.setDoFieldVerification(false);
        
        if (viewsNavBox.getCount() == 0)
        {
            try
            {
                XStream xstream = new XStream();
                
                config(xstream);
                
                DataEntryXML dataEntryXML = (DataEntryXML)xstream.fromXML(AppContextMgr.getInstance().getResourceAsXML("DataEntryTaskInit")); // Describes the definitions of the full text search);
                buildFormNavBoxes(dataEntryXML.getStd());
                stdViews  = dataEntryXML.getStd();
                miscViews = dataEntryXML.getMisc();
                
                if (miscViews != null && !miscViews.isEmpty())
                {
                    initDataEntryViews(miscViews);
                    
                    NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("DET_MISC_FORMS"),
                                                                 name, 
                                                                 getResourceString("DET_CHOOSE_TT"), 
                                                                 IconManager.IconSize.Std16, createMiscActionListener());
                    
                    NavBoxButton roc = (NavBoxButton)nbi;
                    for (DataEntryView dev : miscViews)
                    {
                        roc.addDropDataFlavor(new DataFlavorTableExt(DataEntryTask.class, "RECORD_SET", dev.getTableInfo().getTableId()));
                    }
                    viewsNavBox.add(nbi);
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
        }
        ViewLoader.setDoFieldVerification(cacheDoVerify);
    }
    
    /**
     * Show a dialog letting them choose from a list of available misc views.   
     */
    protected void showMiscViewsDlg()
    {
        DataEntryView deView = null;
        if (miscViews.size() == 1)
        {
            deView = miscViews.get(0);
            
        } else
        {
            ToggleButtonChooserDlg<DataEntryView> dlg = new ToggleButtonChooserDlg<DataEntryView>((Frame)UIRegistry.getTopWindow(), 
                    getResourceString("DET_CHOOSE_TITLE"), miscViews, ToggleButtonChooserPanel.Type.RadioButton);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                deView = dlg.getSelectedObject();
            }
        }
        
        if (deView != null)
        {
            openView(this, deView.getViewSet(), deView.getView(), "edit", null, true);
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
        
        initializeViewsNavBoxFromXML();

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
        
        for (SubPaneIFace sb : SubPaneMgr.getInstance().getSubPanes())
        {
            if (sb.getTask() == this)
            {
                if (sb instanceof DroppableFormRecordSetAccepter)
                {
                    return sb;
                }
            }
        }
        
        if (starterPane == null)
        {
            starterPane = new DroppableFormRecordSetAccepter(title, this, "Drop a Bundle here."); // I18N
        }
        
        return starterPane;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        String label     = getResourceString(DATA_ENTRY);
        String iconName = name;
        String hint     = getResourceString("dataentry_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPopupMenu()
     */
    @Override
    public JPopupMenu getPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(UIRegistry.getResourceString("Configure"));
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                DataEntryConfigureDlg dlg = new DataEntryConfigureDlg(DataEntryTask.this);
                UIHelper.centerAndShow(dlg);
            }
        });
        
        return popupMenu;
    }

    /**
     * 
     */
    protected void prefsChanged(final AppPreferences appPrefs)
    {
        if (appPrefs == AppPreferences.getRemote())
        {
            String    iconName  = appPrefs.get("ui.formatting.disciplineicon", "CollectionObject");
            ImageIcon iconImage = IconManager.getIcon(iconName, IconManager.IconSize.Std16);
            if (iconImage != null)
            {
                if (colObjNavBtn != null)
                {
                    colObjNavBtn.setIcon(iconImage);
                    colObjNavBtn.repaint();
                }
                if (iconForFormClass != null && StringUtils.isNotEmpty(iconClassLookUpName))
                {
                    iconForFormClass.put(iconClassLookUpName, iconImage);
                }
            }
        }
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
                    
                    Agent agent = Agent.getUserAgent();
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
                    prep.setPreparedByAgent(Agent.getUserAgent());
                    
                    SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                    String               prepTitle     = UIRegistry.getLocalizedMessage("CHOOSE_DEFAULT_OBJECT", PrepType.class.getSimpleName());
                    FormDataObjIFace     defPrepType   = appContextMgr.getDefaultObject(PrepType.class, "PrepType", prepTitle, true, true);
                    prep.setPrepType((PrepType)defPrepType);
                }
            }
        }
    }
    
    /**
     * Handles creating a form for viewing/editing and that will have data placed into it. The ViewSetName and View name are optional.
     * @param data the data to be placed into the form
     * @param viewSetName the optional viewset name (can be null)
     * @param viewName the optional view name (can be null)
     */
    protected void editData(final Object data, 
                            final String viewSetName,
                            final String viewName)
    {
        if (data instanceof RecordSet)
        {
            addSubPaneToMgr(createFormFor(this, name, viewSetName, viewName, (RecordSetIFace)data));
            
        } else if (data instanceof Object[])
        {
            Object[] dataList = (Object[])data;
            if (dataList.length == 3)
            {
                ViewIFace   view = (ViewIFace)dataList[0];
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
            String viewName    = cmdAction.getPropertyAsString("view");
            Object data        = cmdAction.getData();
            
            if (data instanceof RecordSetIFace)
            {
                editData(data, viewSetName, viewName);
                
            } else
            {
                openView(this, viewSetName, viewName, "edit", null, true);
            }
            
        } else if (cmdAction.isAction(EDIT_DATA))
        {
            editData(cmdAction.getData(), null, null);
        }
//        else if (cmdAction.isAction(EDIT_IN_DIALOG))
//        {
//            if (cmdAction.getData() instanceof RecordSet)
//            {
//                RecordSet recordSet = (RecordSet)cmdAction.getData();
//                FormPane form = createFormFor(this, name, recordSet);
//                
//                Window mostRecentWindow = UIRegistry.getMostRecentWindow();
//                String viewSetName = form.getViewSetName();
//                String viewName = form.getViewName();
//                String displayName = "????";
//                String formTitle = "????";
//                String closeBtnText = "????";
//                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getTableId());
//                String className = tableInfo.getClassName();
//                String idFieldName = tableInfo.getIdFieldName();
//                boolean isEdit = false;
//                int options = 0;
//                
//                ViewBasedDisplayDialog formDialog = new ViewBasedDisplayDialog((Frame)mostRecentWindow,viewSetName,viewName,displayName,formTitle,closeBtnText,className,idFieldName,isEdit,options);
//                formDialog.setModal(true);
//                formDialog.setVisible(true);
//            }
//        }
        else if (cmdAction.isAction("ShowView"))
        {
            if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                ViewIFace view  = (ViewIFace)dataList[0];
                String    mode  = (String)dataList[1];
                String    idStr = (String)dataList[2];
                openView(this, view, mode, idStr);
            }
            
        } else if (cmdAction.isAction("ViewWasShown"))
        {
            if (cmdAction.getData() instanceof FormViewObj)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        MultiView mv = ((FormViewObj)cmdAction.getData()).getMVParent();
                        if (mv != null && mv.isTopLevel())
                        {
                            mv.focus();
                        }
                    }
                });

            }
        }
    }
    
    /**
     * @param cmdAction
     * @param list
     * @return
     */
    protected boolean processRecordSetCommand(final CommandAction cmdAction, final List<DataEntryView> list)
    {
        if (ContextMgr.getCurrentContext() == this && cmdAction.getSrcObj() instanceof RecordSet)
        {
            RecordSet rs = (RecordSet)cmdAction.getSrcObj();
            for (DataEntryView dev : list)
            {
                if (dev.getTableInfo().getTableId() == rs.getDbTableId())
                {
                    editData(rs, dev.getViewSet(), dev.getView());
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void processRecordSetCommand(final CommandAction cmdAction)
    {
        if (!processRecordSetCommand(cmdAction, stdViews))
        {
            processRecordSetCommand(cmdAction, miscViews);
        }
        
        /*
        if (ContextMgr.getCurrentContext() == this && cmdAction.getSrcObj() instanceof RecordSet)
        {
            RecordSet rs = (RecordSet)cmdAction.getSrcObj();
            for (NavBoxItemIFace nbi : viewsNavBox.getItems())
            {
                RolloverCommand roc = (RolloverCommand)nbi;
                if (roc.getData() instanceof CommandAction)
                {
                    CommandAction ca = (CommandAction)roc.getData();
                    if (((DBTableInfo)ca.getProperty("tableInfo")).getTableId() == rs.getDbTableId())
                    {
                        editData(rs, ca.getPropertyAsString("viewset"), ca.getPropertyAsString("view"));
                        break;
                    }
                }
            }
        }*/
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
            
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET) && cmdAction.isAction("Clicked"))
        {
            processRecordSetCommand(cmdAction);
            
        } else if (cmdAction.isType(DataEntryTask.DATA) && cmdAction.isAction("NewObjDataCreated"))
        {
            adjustNewDataObject(cmdAction.getData());
            
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged((AppPreferences)cmdAction.getData());
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            viewsNavBox.clear();
            ContextMgr.removeServicesByTask(this);
            initializeViewsNavBoxFromXML();
        }
            

    }
    
    //-------------------------------------------------------------------------
    // Class for accepting RecordSet Drops
    //-------------------------------------------------------------------------
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
                addSubPaneToMgr(createFormFor(task, "XXXX", null, null, (RecordSetIFace)srcData));
            }
        }
        
        @Override
        public void doCommand(CommandAction cmdAction)
        {
            // nothing going on for now (should probably be removed)
        }
    }
    
    /**
     * Configures inner classes for XStream.
     * @param xstream the xstream
     */
    protected static void config(final XStream xstream)
    {
        DataEntryXML.config(xstream);
        DataEntryView.config(xstream);
    }

}
