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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewLoader;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.prefs.FormattingPrefsPanel;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
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

/**
 * This task controls the data entry forms. 
 * 
 * NOTE: The doConfigure method and the dialog it uses still has a problem in that: 
 * When a form is moved from the hidden (but registered) list it doesn't get from from 
 * the hidden list so it gets register with the ContextMgr twice. The ContextMgr throws away the second
 * registration so that is ok for now. But the this does need to be fixed at some point.
 *
 * @code_status Beta
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
    
    protected static final String resourceName = "DataEntryTaskInit";
    
    protected static Hashtable<String, ImageIcon> iconForFormClass = new Hashtable<String, ImageIcon>();

    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();
    protected NavBox              viewsNavBox      = null;
    protected NavBox              treeNavBox       = null;
    
    protected Vector<DataEntryView> stdViews       = null;
    protected Vector<DataEntryView> miscViews      = null;
    protected Vector<DataEntryView> availMiscViews = new Vector<DataEntryView>();
    
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
        CommandDispatcher.register(DATA, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
        
        // Do this here instead of in initialize because the static method will need to access the icon mapping first
        viewsNavBox = new NavBox(getResourceString("CreateAndUpdate"));
        treeNavBox  = new NavBox(getResourceString("DataEntryTask.Trees"));
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
            
            navBoxes.add(viewsNavBox);
            
            // No Series Processing
            //NavBox navBox = new NavBox(getResourceString("Actions"));
            //navBox.add(NavBox.createBtn(getResourceString("Series_Processing"), name, IconManager.STD_ICON_SIZE));
            //navBoxes.add(navBox);
            
            loadTreeNavBoxes();
            if (treeNavBox.getItems().size() > 0)
            {
                navBoxes.add(treeNavBox);
            }
           
        }
        isShowDefault = true;
    }
    
    /**
     * Loads the appropriate tree NavBtns into the UI.
     */
    protected void loadTreeNavBoxes()
    {
        // Add Tree NavBoxes
        createTreeEditNB((BaseTreeTask<?,?,?>)TaskMgr.getTask(TaxonTreeTask.TAXON));
        createTreeEditNB((BaseTreeTask<?,?,?>)TaskMgr.getTask(GeographyTreeTask.GEOGRAPHY));
        createTreeEditNB((BaseTreeTask<?,?,?>)TaskMgr.getTask(LithoStratTreeTask.LITHO));
        createTreeEditNB((BaseTreeTask<?,?,?>)TaskMgr.getTask(GtpTreeTask.GTP));
        createTreeEditNB((BaseTreeTask<?,?,?>)TaskMgr.getTask(StorageTreeTask.STORAGE));
    }
    
    /**
     * @param treeTask
     */
    private void createTreeEditNB(final BaseTreeTask<?,?,?> treeTask)
    {
        if (UIHelper.isSecurityOn())
        {
            if (!DBTableIdMgr.getInstance().getByShortClassName(treeTask.getTreeClass().getSimpleName()).getPermissions().canView())
            {
                return;
            }
        }
        
        if (treeTask != null && treeTask.isTreeOnByDefault())
        {
            Action treeEditAction = UIRegistry.getAction("TreeEditing_" + treeTask.getTreeClass().getSimpleName());
            NavBoxItemIFace nbi = NavBox.createBtnWithTT(treeTask.getMenuItemText(), "TreePref", "", IconManager.STD_ICON_SIZE, treeEditAction);
            treeNavBox.add(nbi);
        } 
    }
    
    /**
     * Returns a icon defined by the view, if not found then it by the Class, if not found then it returns the one for the task
     * @param view the view 
     * @return the icon for the view
     */
    protected static ImageIcon getIconForView(final ViewIFace view)
    {
        ImageIcon imgIcon = null;
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
        Vector<Object> dataObjList = new Vector<Object>();
        
        ViewIFace view = viewSetName == null ? SpecifyAppContextMgr.getInstance().getView(viewName) : 
                                          AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (view == null)
        {
            UIRegistry.showError("Couldn't find default form for ["+viewName+"]");
            return;
        }
        
        Object           dataObj     = data;
        FormDataObjIFace formDataObj = data;
        if (formDataObj == null)
        {
            if (isNewForm)
            {
                try
                {
                    String className = view.getClassName();
                    if (StringUtils.isNotEmpty(className))
                    {
                        try
                        {
                            Class<?> dataClass = Class.forName(className);
                            if (UIHelper.isSecurityOn())
                            {
                                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(dataClass.getSimpleName());
                                if (tblInfo != null)
                                {
                                    PermissionSettings perm = tblInfo.getPermissions();
                                    if (!perm.canAdd())
                                    {
                                        return;
                                    }
                                }
                            }
                            formDataObj = FormHelper.createAndNewDataObj(dataClass);
                            dataObjList.add(formDataObj);
                            dataObj = dataObjList;
                            
                        } catch (Exception ex)
                        {
                            log.error("The Class["+className+"] couldn't be created.");
                        }
                        
                    } else
                    {
                        log.error("Class name is empty for view["+view.getName()+"]");
                    }
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
            
        } else
        {
            DataProviderFactory.getInstance().evict(data.getClass());
            
            dataObjList.add(data);
            dataObj = dataObjList;
        }
        
        FormPane tmpFP;
        if (view != null)
        {
            tmpFP = new FormPane(view.getName(), task, view.getViewSetName(), viewName, mode, dataObj, 
                                 isNewForm ? (MultiView.IS_NEW_OBJECT | MultiView.RESULTSET_CONTROLLER): 0);
        } else
        {
            UIRegistry.showError("Couldn't find default form for ["+viewName+"]");
            return;
        }
        
        final FormPane formPane = tmpFP;
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
        if (UIHelper.isSecurityOn())
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
            if (tblInfo != null)
            {
                PermissionSettings perm = tblInfo.getPermissions();
                if (!perm.canView())
                {
                    return;
                }
            }
        }
        String sqlStr  = DBTableIdMgr.getInstance().getQueryForTable(tableId, Integer.parseInt(idStr));
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

    protected static FormPane createFormFor(final Taskable       task, 
                                            final String         name, 
                                            final String         viewSetName,
                                            final String         viewName,
                                            final RecordSetIFace recordSet)
    {
        return createFormFor(task, name, viewSetName, viewName, recordSet, false);
    }
    /**
     * Create a form for a recordset.
     * @param task the task it belongs to
     * @param name the name
     * @param recordSet the record to create a form for
     * @return the FormPane
     */
    protected static FormPane createFormFor(final Taskable       task, 
                                            final String         name, 
                                            final String         viewSetName,
                                            final String         viewName,
                                            final RecordSetIFace recordSet,
                                            final boolean readOnly)
    {
        if (UIHelper.isSecurityOn())
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
            if (tblInfo.getPermissions().hasNoPerm())
            {
                return null;
            }
        }
        
        FormPane formPane = null;
        // Look up and see if we already have a SubPane that is working on the RecordSet
        
        SubPaneIFace subPane = SubPaneMgr.getSubPaneWithRecordSet(recordSet);
        if (subPane == null)
        {
            ViewIFace view = null;
            
            if (StringUtils.isNotEmpty(viewName))
            {
                view = AppContextMgr.getInstance().getView(viewSetName, viewName);
            } else
            {
                String defaultFormName = DBTableIdMgr.getInstance().getDefaultFormNameById(recordSet.getDbTableId());
    
                if (StringUtils.isNotEmpty(defaultFormName))
                {
                    view = AppContextMgr.getInstance().getView(defaultFormName);
                     
                } else
                {
                    log.error("No Default View for Table Id["+recordSet.getDbTableId()+"] from recordset");
                    // XXX Need Error Dialog ??
                }
            }
            
            if (view != null)
            {
                int options = MultiView.RESULTSET_CONTROLLER;
                if (readOnly)
                {
                    options |= MultiView.HIDE_SAVE_BTN; 
                }
                else
                {
                    options |= MultiView.VIEW_SWITCHER;
                }
                formPane = new FormPane(name, task, view, null, null, options);
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
     * @param devList
     */
    protected void buildFormNavBoxes(final Vector<DataEntryView> devList,
                                     final boolean doRegister)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        for (DataEntryView dev : devList)
        {
            boolean isColObj = dev.getTableInfo() != null && dev.getTableInfo().getTableId() == CollectionObject.getClassTableId();
            
            ImageIcon iconImage = IconManager.getIcon(dev.getIconName(), IconManager.STD_ICON_SIZE);
            if (iconImage != null)
            {
                String iconNameStr;
                if (isColObj)
                {
                    iconNameStr            = AppPreferences.getRemote().get(FormattingPrefsPanel.getDisciplineImageName(), "CollectionObject");
                    ImageIcon colIconImage = IconManager.getIcon(iconNameStr, IconManager.STD_ICON_SIZE);
                    if (colIconImage != null)
                    {
                        iconImage = colIconImage;
                    }
                    iconNameStr            = "CollectionObject";
                    iconClassLookUpName = iconNameStr;
                } else
                {
                    iconNameStr = dev.getView();
                }
                iconForFormClass.put(iconNameStr, iconImage);
            } else
            {
                log.error("Icon ["+dev.getIconName()+"] could not be found.");
            }
            
            ViewIFace view = appContextMgr.getView(null, dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                if (UIHelper.isSecurityOn())
                {
                    if (!tableInfo.getPermissions().canView())
                    {
                        continue;
                    }
                }

                //System.err.println(tableInfo.getName()+"  "+tableInfo.isHidden());
                
                if (tableInfo != null)
                {
                    if (!tableInfo.isHidden())
                    {
                        CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                        //cmdAction.setProperty("viewset", dev.getViewSet());
                        cmdAction.setProperty("view",    dev.getView());
                        
                        if (doRegister)
                        {
                            ContextMgr.registerService(10, dev.getName(), tableInfo.getTableId(), cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true);
                        }
                        
                        if (dev.isSideBar())
                        {
                            cmdAction = new CommandAction(DATA_ENTRY, OPEN_NEW_VIEW);
                            //cmdAction.setProperty("viewset",   dev.getViewSet());
                            cmdAction.setProperty("view",      dev.getView());
                            cmdAction.setProperty("tableInfo", dev.getTableInfo());
                            
                            NavBoxAction nba = new NavBoxAction(cmdAction);
                            
                            NavBoxItemIFace nbi = NavBox.createBtnWithTT(tableInfo.getTitle(), dev.getIconName(), dev.getToolTip(), IconManager.STD_ICON_SIZE, nba);
                            if (nbi instanceof NavBoxButton)
                            {
                                NavBoxButton nbb = (NavBoxButton)nbi;
                                if (isColObj)
                                {
                                   colObjNavBtn = nbb; 
                                }
                                
                                // When Being Dragged
                                nbb.addDragDataFlavor(Trash.TRASH_FLAVOR);
                                nbb.addDragDataFlavor(new DataFlavorTableExt(DataEntryTask.class, "Data_Entry", tableInfo.getTableId()));
                        
                                // When something is dropped on it
                                nbb.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, RecordSetTask.RECORD_SET, tableInfo.getTableId()));//RecordSetTask.RECORDSET_FLAVOR);
                            }
        
                            viewsNavBox.add(nbi);
                        }
                    }
                } else 
                {
                    UIRegistry.showError("View's Class name["+view.getClassName()+"] was not found in the DBTableIdMgr");
                }
                
            } else
            {
                UIRegistry.showError("View doesn't exist view["+dev.getView()+"] for entry in dataentry_task.xml");
            }
        }
    }
    
    /**
     * Initializes the TableInfo data member.
     * @param list the list of DataEntryView
     */
    protected void initDataEntryViews(final Vector<DataEntryView> list,
                                      final boolean doRegister)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        for (DataEntryView dev : list)
        {
            ViewIFace view = appContextMgr.getView(null, dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                cmdAction.setProperty("view", dev.getView());
                
                if (doRegister)
                {
                    ContextMgr.registerService(10, dev.getName(), tableInfo.getTableId(), cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true);
                }

            } else
            {
                UIRegistry.showError("Couldn't find view["+dev.getView()+"] for entry in dataentry_task.xml");
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
                        if (data instanceof RecordSetIFace)
                        {
                            RecordSetIFace rs = (RecordSetIFace)data;
                            for (DataEntryView deView : miscViews)
                            {
                                if (deView.getTableInfo().getTableId() == rs.getDbTableId())
                                {
                                    editData(DataEntryTask.this, rs, deView.getView());
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
                
                String           xmlStr    = null;
                AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir(SpecifyAppContextMgr.PERSONALDIR, resourceName);
                if (escAppRes != null)
                {
                    xmlStr = escAppRes.getDataAsString();
                    
                } else
                {
                    // Get the default resource by name and copy it to a new User Area Resource
                    AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes(SpecifyAppContextMgr.PERSONALDIR, resourceName);
                    if (newAppRes != null)
                    {
                        // Save it in the User Area
                        //AppContextMgr.getInstance().saveResource(newAppRes);
                        xmlStr = newAppRes.getDataAsString();
                    } else
                    {
                        return;
                    }
                }
                //log.debug(xmlStr);
                DataEntryXML dataEntryXML = (DataEntryXML)xstream.fromXML(xmlStr); // Describes the definitions of the full text search);
                
                stdViews  = dataEntryXML.getStd();
                miscViews = dataEntryXML.getMisc();
                
                //initDataEntryViews(stdViews);
                //initDataEntryViews(miscViews);
                
                buildNavBoxes(stdViews, miscViews, true);
                
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
        }
        ViewLoader.setDoFieldVerification(cacheDoVerify);
    }
    
    /**
     * @param stdList
     * @param miscList
     */
    protected void buildNavBoxes(final Vector<DataEntryView> stdList,
                                 final Vector<DataEntryView> miscList,
                                 final boolean doRegister)
    {
        buildFormNavBoxes(stdList, doRegister);
        
        if (miscList != null && !miscList.isEmpty())
        {
            availMiscViews.clear();
            for (DataEntryView dev : miscList)
            {
                ViewIFace view = AppContextMgr.getInstance().getView(null, dev.getView());
                if (view != null)
                {
                    DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                    if (UIHelper.isSecurityOn())
                    {
                        if (!tableInfo.getPermissions().canView())
                        {
                            continue;
                        }
                    }
                }
                availMiscViews.add(dev);
            }

            if (availMiscViews.size() > 0)
            {
                initDataEntryViews(availMiscViews, doRegister);
                
                NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("DET_MISC_FORMS"),
                                                             "MoreForms", 
                                                             getResourceString("DET_CHOOSE_TT"), 
                                                             IconManager.STD_ICON_SIZE, createMiscActionListener());
                
                NavBoxButton roc = (NavBoxButton)nbi;
                for (DataEntryView dev : availMiscViews)
                {
                    roc.addDropDataFlavor(new DataFlavorTableExt(DataEntryTask.class, RecordSetTask.RECORD_SET, dev.getTableInfo().getTableId()));
                }
                viewsNavBox.add(nbi);
            }
        }
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
                    "DET_CHOOSE_TITLE", availMiscViews, ToggleButtonChooserPanel.Type.RadioButton);
            dlg.setUseScrollPane(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                deView = dlg.getSelectedObject();
            }
        }
        
        if (deView != null)
        {
            openView(this, null, deView.getView(), "edit", null, true);
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

        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }

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
            starterPane = new DroppableFormRecordSetAccepter(title, this, "");//getResourceString("DET_DROP_BUNDLE"));
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
        toolbarItems = new Vector<ToolBarItemDesc>();

        String label     = getResourceString(DATA_ENTRY);
        String hint     = getResourceString("dataentry_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));

        return toolbarItems;
    }

    /**
     * @return the stdViews
     */
    public Vector<DataEntryView> getStdViews()
    {
        return stdViews;
    }

    /**
     * @return the miscViews
     */
    public Vector<DataEntryView> getMiscViews()
    {
        return miscViews;
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
     * @see edu.ku.brc.af.tasks.BaseTask#isConfigurable()
     */
    @Override
    public boolean isConfigurable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doConfigure()
     */
    @Override
    public void doConfigure()
    {
        ContextMgr.dump();
        
        //stdViews  = getStdViews();
        //miscViews = getMiscViews();

        Vector<TaskConfigItemIFace> stdList  = new Vector<TaskConfigItemIFace>();
        Vector<TaskConfigItemIFace> miscList = new Vector<TaskConfigItemIFace>();
        
        // Clone for undo (Cancel)
        try
        {
            for (DataEntryView de : stdViews)
            {
                stdList.add((DataEntryView)de.clone());
            }
            for (DataEntryView de : miscViews)
            {
                miscList.add((DataEntryView)de.clone());
            }
        } catch (CloneNotSupportedException ex) {}
        
        DataEntryConfigDlg dlg = new DataEntryConfigDlg(stdList, miscList, true,
                "DataEntryConfigure",
                "DET_CONFIGURE_VIEWS",
                "DET_STANDARD",
                "DET_MISC",
                "DET_MOVE_TO_MISC_TT",
                "DET_MOVE_TO_STD_TT");
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            // Clear the current lists
            stdViews.clear();
            miscViews.clear();
            
            // Copy the Dlg List to the actual lists
            for (TaskConfigItemIFace entry : stdList)
            {
                stdViews.add((DataEntryView)entry);
            }
            
            for (TaskConfigItemIFace entry : miscList)
            {
                miscViews.add((DataEntryView)entry);
            }
            
            viewsNavBox.clear();
            
            // unregister all
            
            for (DataEntryView dev : stdViews)
            {
                System.out.println(dev.getTableInfo());
                if (dev.getTableInfo() != null)
                {
                    String srvName = ServiceInfo.getHashKey(dev.getName(), this, dev.getTableInfo().getTableId());
                    ContextMgr.unregisterService(srvName);
                }
            }
            
            for (DataEntryView dev : miscViews)
            {
                if (dev.getTableInfo() != null)
                {
                    String srvName = ServiceInfo.getHashKey(dev.getName(), this, dev.getTableInfo().getTableId());
                    ContextMgr.unregisterService(srvName);
                }
            }
            
            // This re-registers the items
            buildNavBoxes(stdViews, miscViews, true);
            
            viewsNavBox.validate();
            viewsNavBox.doLayout();
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            
            // Persist out to database
            DataEntryXML dataEntryXML = new DataEntryXML(stdViews, miscViews);
            
            XStream xstream = new XStream();
            config(xstream);
            
            AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir(SpecifyAppContextMgr.PERSONALDIR, resourceName);
            if (escAppRes != null)
            {
                escAppRes.setDataAsString(xstream.toXML(dataEntryXML));
                AppContextMgr.getInstance().saveResource(escAppRes);
                
            } else
            {
                AppContextMgr.getInstance().putResourceAsXML(resourceName, xstream.toXML(dataEntryXML));     
            }
        }
    }

    /**
     * 
     */
    protected void prefsChanged(final AppPreferences appPrefs)
    {
        if (appPrefs == AppPreferences.getRemote())
        {
            String    iconNameStr  = appPrefs.get(FormattingPrefsPanel.getDisciplineImageName(), "CollectionObject");
            ImageIcon iconImage = IconManager.getIcon(iconNameStr, IconManager.STD_ICON_SIZE);
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
            
            if (starterPane != null && starterPane instanceof DroppableFormRecordSetAccepter)
            {
                ((DroppableFormRecordSetAccepter)starterPane).resetSplashIcon();
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
                    Collection catSeries = AppContextMgr.getInstance().getClassObject(Collection.class);
                    colObj.setCollection(catSeries); 
                }
                
                if (colObj.getCollectingEvent() == null)
                {
                    Agent agent = Agent.getUserAgent();
                    if (agent != null)
                    {
                        colObj.setCataloger(agent);
                    }
                }

            } 
            // Commenting out for now because we have no way to manage the default.
            /*
            else if (dataObj instanceof Preparation)
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
            }*/
        }
    }
    
    protected void editData(final Taskable task,
                            final Object data,
                            final String viewName)
    {
        editData(task, data, viewName, false);
    }
    
    /**
     * Handles creating a form for viewing/editing and that will have data placed into it. The ViewSetName and View name are optional.
     * @param task the originating task
     * @param data the data to be placed into the form
     * @param viewSetName the optional viewset name (can be null)
     * @param viewName the optional view name (can be null)
     */
    protected void editData(final Taskable task,
                            final Object data, 
                            final String viewName,
                            final boolean readOnly)
    {
        if (data instanceof RecordSetIFace)
        {
            FormPane formPane = createFormFor(task, name, null, viewName, (RecordSetIFace)data, readOnly);
            if (formPane != null)
            {
                addSubPaneToMgr(formPane);
            }
            
        } else if (data instanceof Object[])
        {
            Object[] dataList = (Object[])data;
            if (dataList.length == 3)
            {
                ViewIFace view  = (ViewIFace)dataList[0];
                String    mode  = (String)dataList[1];
                String    idStr = (String)dataList[2];
                
                openView(task, view, mode, idStr);
                
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
            String   viewName  = cmdAction.getPropertyAsString("view");
            Taskable task      = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
            Object   data      = cmdAction.getData();
            if (data instanceof RecordSetIFace)
            {
                editData(task != null ? task : this, data, viewName);
                
            } else
            {
                openView(task != null ? task : this, null, viewName, "edit", null, true);
            }
            
        } else if (cmdAction.isAction(EDIT_DATA))
        {
            editData(this, cmdAction.getData(), null, cmdAction.getProperty("readonly") != null);
        }
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
        if (ContextMgr.getCurrentContext() == this && cmdAction.getSrcObj() instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)cmdAction.getSrcObj();
            for (DataEntryView dev : list)
            {
                if (dev.getTableInfo().getTableId() == rs.getDbTableId())
                {
                    editData(this, rs, dev.getView());
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @param cmdAction
     */
    protected void processRecordSetCommand(final CommandAction cmdAction)
    {
        if (!processRecordSetCommand(cmdAction, stdViews))
        {
            processRecordSetCommand(cmdAction, miscViews);
        }
        
        /*
        if (ContextMgr.getCurrentContext() == this && cmdAction.getSrcObj() instanceof RecordSetIFaced)
        {
            RecordSetIFace rs = (RecordSetIFace)cmdAction.getSrcObj();
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
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT) ||
            cmdAction.isAction(APP_START_ACT))
        {
            viewsNavBox.clear();
            initializeViewsNavBoxFromXML();
            
            treeNavBox.clear();
            loadTreeNavBoxes();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
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
        }
    }
    
    /**
     * @param nameArg
     * @param task
     * @param desc
     * @return
     */
    public DroppableFormRecordSetAccepter createDroppableFormRecordSetAccepter(final String   nameArg, 
                                                                               final Taskable task,
                                                                               final String   desc)
    {
        return new DroppableFormRecordSetAccepter(nameArg, task, desc);
    }
    
    //-------------------------------------------------------------------------
    // Class for accepting RecordSet Drops
    //-------------------------------------------------------------------------
    public class DroppableFormRecordSetAccepter extends FormPane
    {
        protected ImageIcon bgImg = IconManager.getIcon("SpecifySplash");
        
        /**
         * @param name
         * @param task
         * @param desc
         */
        public DroppableFormRecordSetAccepter(final String   name, 
                                              final Taskable task,
                                              final String   desc)
        {
            super(name, task, desc);
            
            dropFlavors.add(RecordSetTask.RECORDSET_FLAVOR);
            this.createMouseInputAdapter();
            this.icon = IconManager.getIcon(DATA_ENTRY, IconManager.IconSize.Std16);
            setBackground(Color.WHITE);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            if (bgImg != null)
            {
                Dimension size = getSize();
                int imgW = Math.min(size.width, bgImg.getIconWidth());
                int imgH = Math.min(size.height, bgImg.getIconHeight());
                int x = (size.width - imgW) / 2;
                int y = (size.height - imgH) / 2;
                g.drawImage(bgImg.getImage(), x, y, imgW, imgH, null);
            }
        }

        public void addDropFlavor(final DataFlavor df)
        {
            dropFlavors.add(df); 
        }
        
        /**
         * 
         */
        public void resetSplashIcon()
        {
            bgImg = IconManager.getIcon("SpecifySplash");
            repaint();
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.tasks.subpane.FormPane#doAction(edu.ku.brc.ui.dnd.GhostActionable)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void doAction(GhostActionable src)
        {
            Object srcData = src.getData();
            if (srcData instanceof RecordSetIFace)
            {
                FormPane formPane = createFormFor(task, "XXXX", null, null, (RecordSetIFace)srcData);
                if (formPane != null)
                {
                    addSubPaneToMgr(formPane);
                }
                
            }/* else if (srcData instanceof CommandActionForDB)
            {
                CommandActionForDB cmdAction = (CommandActionForDB)srcData;
                int tableId = cmdAction.getTableId();
                int id      = cmdAction.getId();
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.set("", tableId, RecordSet.GLOBAL);
                rs.addItem(id);
                addSubPaneToMgr(createFormFor(task, "XXXX", null, null, (RecordSetIFace)rs));
            }*/
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.tasks.subpane.FormPane#doCommand(edu.ku.brc.ui.CommandAction)
         */
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#canRequestContext()
     */
    @Override
    protected boolean canRequestContext()
    {
        return Uploader.checkUploadLock();
    }
}
