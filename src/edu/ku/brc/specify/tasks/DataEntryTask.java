/* Copyright (C) 2021, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks;

import com.thoughtworks.xstream.XStream;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.*;
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
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.SCOPE;
import edu.ku.brc.specify.dbsupport.cleanuptools.LocalityDuplicateRemover;
import edu.ku.brc.specify.prefs.FormattingPrefsPanel;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.ui.BatchReidentifyPanel;
import edu.ku.brc.specify.ui.DBObjDialogFactory.FormLockStatus;
import edu.ku.brc.ui.*;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static edu.ku.brc.specify.ui.DBObjDialogFactory.isLockOK;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
    public static final String	   BATCH_EDIT_DATA   = "BatchEdit";
    //public static final String     EDIT_IN_DIALOG    = "EditInDialog";
    public static final String     DATA              = "Data"; // Sent by FormHelper for when new DataObject are created
    
    public static final DataFlavor DATAENTRY_FLAVOR = new DataFlavor(DataEntryTask.class, "Data_Entry");
    
    protected static final String resourceName = "DataEntryTaskInit";
    
    protected static Hashtable<String, ImageIcon> iconForFormClass = new Hashtable<String, ImageIcon>();

    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();
    protected NavBox              viewsNavBox      = null;
    protected NavBox              batchNavBox      = null;
    protected NavBox              containerNavBox  = null;
    
    protected Vector<DataEntryView> stdViews       = null;
    protected Vector<DataEntryView> miscViews      = null;
    protected Vector<DataEntryView> batchViews      = null;
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
        //batchNavBox = new NavBox(getResourceString("DataEntryTask.BatchEdit"));
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
            //navBoxes.add(batchNavBox);
            
            // Container Tree
            //NavBox navBox = new NavBox(getResourceString("Actions"));
            //navBox.add(NavBox.createBtn(getResourceString("ContainerTree"), "Container", IconManager.STD_ICON_SIZE));
            //navBoxes.add(navBox);
        }
        isShowDefault = true;
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
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
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
    public FormPane openView(final Taskable         task, 
                         final String           viewSetName, 
                         final String           viewName, 
                         final String           modeArg, 
                         final FormDataObjIFace data,
                         final boolean          isNewForm)
    {
        String mode = modeArg;
        
        Vector<Object> dataObjList = new Vector<Object>();
        
        ViewIFace view = viewSetName == null ? SpecifyAppContextMgr.getInstance().getView(viewName) : 
                                               SpecifyAppContextMgr.getInstance().getView(viewSetName, viewName);
        if (view == null)
        {
            UIRegistry.showError("Couldn't find default form for ["+viewName+"]");
            return null;
        }
        
        FormLockStatus lockStatus = isLockOK("LockTitle", view, isNewForm, mode == null || mode.equals("edit"));
        if (lockStatus != FormLockStatus.OK)
        {
            if (lockStatus == FormLockStatus.ViewOnly)
            {
                mode = "view";
                
            } else if (lockStatus == FormLockStatus.Skip)
            {
                return null;
            }
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
                            if (AppContextMgr.isSecurityOn())
                            {
                                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(dataClass.getSimpleName());
                                if (tblInfo != null)
                                {
                                    PermissionSettings perm = tblInfo.getPermissions();
                                    if (!perm.canAdd())
                                    {
                                        UIRegistry.showLocalizedMsg("DET_NO_ADD_PERM");
                                        return null;
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
        
        final FormPane formPane = new FormPane(view.getName(), task, view, mode, dataObj, 
                                 (isNewForm ? (MultiView.IS_NEW_OBJECT | MultiView.RESULTSET_CONTROLLER) : 0),
                                  true); // true -> suppress business rules (they get run later any way).
        
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
                SubPaneIFace taskStarterPane = task != null ? task.getStarterPane() : null;
                if (taskStarterPane == null)
                {
                    SubPaneIFace curPane = SubPaneMgr.getInstance().getCurrentSubPane();
                    if (curPane instanceof DroppableFormRecordSetAccepter)
                    {
                        SubPaneMgr.getInstance().replacePane(curPane, formPane);
                    }
                    addSubPaneToMgr(formPane);

                } else
                {
                    SubPaneMgr.getInstance().replacePane(taskStarterPane, formPane);
                    task.setStarterPane(null);
                }

                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                
                formPane.focusFirstFormControl();
            }
        });

        return formPane;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane instanceof FormPane)
        {
            FormPane  formPane  = (FormPane)subPane;
            MultiView multiView = formPane.getMultiView();
            //Bug 7691. TreeLocks get set when a form is first displayed, even when a recordset is being
            //displayed and the formview is not yet editable. So the isEditable() condition needs to be removed.
            //A better fix would be to check locks when view mode is switched.
            if (multiView != null/* && (multiView.isEditable()*/)
            {
                ViewIFace view = multiView.getView();
                
                Class<?> treeDefClass = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getTreeDefClass(view);
                if (treeDefClass != null)
                {
                    boolean hasDataOfTreeClass = false;
                    for (SubPaneIFace sp : SubPaneMgr.getInstance().getSubPanes())
                    {
                        if (sp instanceof FormPane)
                        {
                            FormPane fp = (FormPane)sp;
                            if (fp != formPane)
                            {
                                ViewIFace fpView = fp.getViewable() != null ? fp.getViewable().getView() : null;
                                if (view != fpView && fpView != null && fpView.getClassName().equals(view.getClassName()))
                                {
                                    hasDataOfTreeClass = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!hasDataOfTreeClass)
                    {
                        if (BaseTreeBusRules.ALLOW_CONCURRENT_FORM_ACCESS)
                        {
                        	//XXX treeviewer pane vs. data form pane???
                        	TaskSemaphoreMgr.decrementUsageCount(title, treeDefClass.getSimpleName(), SCOPE.Discipline);
                        } else
                        {
                        	TaskSemaphoreMgr.unlock("tabtitle", treeDefClass.getSimpleName()+"Form", TaskSemaphoreMgr.SCOPE.Discipline);
                        	TaskSemaphoreMgr.unlock("tabtitle", treeDefClass.getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline);
                        }
                    }
                }
            }
        }
    }
    
    /*protected boolean isSameFormPane(final FormPane fp1, final FormPane fp2)
    {
        if (fp1 == fp2)
        {
            return true;
            
        }
        Hashtable<Viewable, Boolean> hash = new Hashtable<Viewable, Boolean>();
        for (Viewable viewable : fp1.getMultiView().getViewables())
        {
            hash.put(viewable, true);
        }
        for (Viewable viewable : fp1.getMultiView().getViewables())
        {
            hash.put(viewable, true);
        }
        return false;
    }*/

    /**
     * Opens a View and fills it with a single data object
     * @param mode the mode of how it is to be opened (View, Edit) 
     * @param idStr a string that contains the Integer Id (Primary Key) of the object to be shown
     */
    public static void openView(final Taskable task, final ViewIFace view, final String mode, final String idStr)
    {
        int tableId = DBTableIdMgr.getInstance().getIdByClassName(view.getClassName());
        if (AppContextMgr.isSecurityOn())
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
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
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
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
                log.error(ex);
                ex.printStackTrace();
            }
        } else
        {
            log.error("Query String is empty for tableId["+tableId+"] idStr["+idStr+"]");
        }
    }

    /**
     * @param task
     * @param name
     * @param viewSetName
     * @param viewName
     * @param recordSet
     * @return
     */
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
                                            final boolean        readOnlyArg)
    {
        boolean readOnly = readOnlyArg;
        
        if (AppContextMgr.isSecurityOn())
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
                
                String mode = null;
                FormLockStatus lockStatus = isLockOK("LockTitle", view, false, !readOnlyArg);
                if (lockStatus != FormLockStatus.OK)
                {
                    if (lockStatus == FormLockStatus.ViewOnly)
                    {
                        mode = "view";
                        readOnly = true;
                        
                    } else if (lockStatus == FormLockStatus.Skip)
                    {
                        return null;
                    }
                }
                
                if (readOnly)
                {
                    options |= MultiView.HIDE_SAVE_BTN; 
                }
                else
                {
                    options |= MultiView.VIEW_SWITCHER;
                }
                formPane = new FormPane(name, task, view, mode, null, options);
                formPane.setIcon(getIconForView(view));
                formPane.setRecordSet(recordSet);
                
                CommandDispatcher.dispatch(new CommandAction(DATA_ENTRY, VIEW_WAS_OPENED, formPane));
                
            } else
            {
                throw new RuntimeException("The view was null and shouldn't be!");
            }
            
        } else
        {
            SubPaneMgr.getInstance().showPane(subPane);
            return null;
        }
        
        return formPane;
    }
    
    /**
     * @param devList
     * @param doRegister
     */
    protected void buildFormNavBoxes(final Vector<DataEntryView> devList,
                                     final boolean doRegister)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        boolean  isUsingInteractions = isUsingInteractions();
        Taskable interactionsTask    = TaskMgr.getTask("InteractionsTaskInit");

        for (DataEntryView dev : devList)
        {
            ViewIFace view = appContextMgr.getView(null, dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
            }
            
            //System.out.println(dev.getTableInfo()+"  "+(dev.getTableInfo() != null ? (dev.getTableInfo().getTableId()+"  "+CollectionObject.getClassTableId()) : ""));
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
            
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                if (AppContextMgr.isSecurityOn())
                {
                    if (!tableInfo.getPermissions().canView())
                    {
                        continue;
                    }
                }

                if (tableInfo != null)
                {
                    if (!tableInfo.isHidden())
                    {
                        CommandAction cmdAction = new CommandAction(DATA_ENTRY, EDIT_DATA);
                        //cmdAction.setProperty("viewset", dev.getViewSet());
                        cmdAction.setProperty("view",    dev.getView());
                        
                        if (doRegister)
                        {
                            // In the future we should check to see if Interactions is turned on
                            // and if it isn't then 
                            //log.debug("Registering: "+tableInfo.getTitle());
                            
                            int      tblId = tableInfo.getTableId();
                            Taskable task  = isUsingInteractions ? AppContextMgr.getInstance().getTaskFromTableId(tblId) : null;
                            if (task == interactionsTask && !isUsingInteractions)
                            {
                                task = this;
                            }
                            cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, task);
                            ContextMgr.registerService(10, dev.getView(), tblId, cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true); // the Name gets Hashed
                            if (isColObj) {
                                cmdAction.setProperty("displayTableId", 1);
                                ContextMgr.registerService(10, dev.getView(), 63, cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true); // the Name gets Hashed
                            }
                        }
                        
                        if (dev.isSideBar())
                        {
                            cmdAction = new CommandAction(DATA_ENTRY, OPEN_NEW_VIEW);
                            //cmdAction.setProperty("viewset",   dev.getViewSet());
                            cmdAction.setProperty("view",      dev.getView());
                            cmdAction.setProperty("tableInfo", dev.getTableInfo());
                            
                            NavBoxAction nba = new NavBoxAction(cmdAction);
                            
                            String          btnTitle = dev.getTableInfo() != null ? dev.getTableInfo().getTitle() : dev.getTitle();
                            NavBoxItemIFace nbi      = NavBox.createBtnWithTT(btnTitle, dev.getIconName(), dev.getToolTip(), IconManager.STD_ICON_SIZE, nba);
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
                                if (isColObj) {
                                    nbb.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, RecordSetTask.RECORD_SET, Preparation.getClassTableId()));
                                }
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
     * @param devList
     * @param doRegister
     */
    protected void buildBatchNavBoxes(final Vector<DataEntryView> devList,
                                     final boolean doRegister)
    {
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        boolean  isUsingInteractions = isUsingInteractions();
        Taskable interactionsTask    = TaskMgr.getTask("InteractionsTaskInit");

        for (DataEntryView dev : devList)
        {
            ViewIFace view = appContextMgr.getView(null, dev.getView());
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
            }
            
            //System.out.println(dev.getTableInfo()+"  "+(dev.getTableInfo() != null ? (dev.getTableInfo().getTableId()+"  "+CollectionObject.getClassTableId()) : ""));
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
            
            if (view != null)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                dev.setTableInfo(tableInfo);
                
                if (AppContextMgr.isSecurityOn())
                {
                    if (!tableInfo.getPermissions().canView())
                    {
                        continue;
                    }
                }

                if (tableInfo != null)
                {
                    if (!tableInfo.isHidden())
                    {
                        CommandAction cmdAction = new CommandAction(DATA_ENTRY, BATCH_EDIT_DATA);
                        //cmdAction.setProperty("viewset", dev.getViewSet());
                        cmdAction.setProperty("view",    dev.getView());
                        
                        if (doRegister)
                        {
                            // In the future we should check to see if Interactions is turned on
                            // and if it isn't then 
                            //log.debug("Registering: "+tableInfo.getTitle());
                            
                            int      tblId = tableInfo.getTableId();
                            Taskable task  = isUsingInteractions ? AppContextMgr.getInstance().getTaskFromTableId(tblId) : null;
                            if (task == interactionsTask && !isUsingInteractions)
                            {
                                task = this;
                            }
                            cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, task);
                            ContextMgr.registerService(10, dev.getView(), tblId, cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true); // the Name gets Hashed
                        }
                        
                        if (dev.isSideBar())
                        {
                            cmdAction = new CommandAction(DATA_ENTRY, BATCH_EDIT_DATA);
                            //cmdAction.setProperty("viewset",   dev.getViewSet());
                            cmdAction.setProperty("view",      dev.getView());
                            cmdAction.setProperty("tableInfo", dev.getTableInfo());
                            
                            NavBoxAction nba = new NavBoxAction(cmdAction);
                            
                            String          btnTitle = dev.getTableInfo() != null ? dev.getTableInfo().getTitle() : dev.getTitle();
                            NavBoxItemIFace nbi      = NavBox.createBtnWithTT(btnTitle, dev.getIconName(), dev.getToolTip(), IconManager.STD_ICON_SIZE, nba);
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
                            batchNavBox.add(nbi);
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
     * @return whether the Interactions task is being used.
     */
    protected boolean isUsingInteractions()
    {
        AppPreferences   remotePrefs         = AppPreferences.getRemote();
        String           discipline          = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        return           remotePrefs.getBoolean(InteractionsTask.IS_USING_INTERACTIONS_PREFNAME+discipline, true);
    }
    
    /**
     * Initializes the TableInfo data member.
     * @param list the list of DataEntryView
     */
    protected void initDataEntryViews(final Vector<DataEntryView> list,
                                      final boolean doRegister)
    {
        boolean  isUsingInteractions = isUsingInteractions();
        Taskable interactionsTask    = TaskMgr.getTask("InteractionsTaskInit");

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
                    int      tblId = tableInfo.getTableId();
                    Taskable task  = AppContextMgr.getInstance().getTaskFromTableId(tblId);
                    if (task == interactionsTask && !isUsingInteractions)
                    {
                        task = this;
                    }
                    cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, task);
                    ContextMgr.registerService(10, dev.getView(), tblId, cmdAction, this, DATA_ENTRY, tableInfo.getTitle(), true); // the Name gets Hashed
                }

            } else if (!dev.getView().startsWith("Exsi")) // XXX temporary FIX! for release 6.4.08
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
     * @param fromList
     * @param intoList
     */
    private void merge(final Vector<DataEntryView> fromList, 
                       final Vector<DataEntryView> intoList)
    {
        HashMap<String, DataEntryView> intoHash = new HashMap<String, DataEntryView>();
        for (DataEntryView dev : intoList)
        {
            intoHash.put(dev.getView(), dev);
        }
        
        for (DataEntryView dev : fromList)
        {
            if (intoHash.get(dev.getView()) == null)
            {
                intoList.add(dev);
            }
        }
    }
    
    private void mergeNewEntries(final DataEntryXML existingEntries)
    {
        SpecifyAppContextMgr spAppMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        AppResourceIFace appRes = spAppMgr.getDiskDisciplineResourceByName("DataEntryTaskInit", false);
        if (appRes != null)
        {
            try
            {
                XStream xstream = new XStream();
                
                config(xstream);
                DataEntryXML dataEntryXML = (DataEntryXML)xstream.fromXML(appRes.getDataAsString());
                merge(dataEntryXML.getStd(), existingEntries.getStd());
                merge(dataEntryXML.getMisc(), existingEntries.getMisc());
                
            } catch (Exception ex)
            {
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
                log.error(ex);
                ex.printStackTrace();
            }
        }
        
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
                mergeNewEntries(dataEntryXML);
                
                stdViews  = dataEntryXML.getStd();
                miscViews = dataEntryXML.getMisc();
                batchViews = dataEntryXML.getBatch();
                
                SpecifyAppContextMgr acm = (SpecifyAppContextMgr)AppContextMgr.getInstance();
                for (DataEntryView dev : stdViews)
                {
                    if (StringUtils.isEmpty(dev.getToolTip()))
                    {
                        ViewIFace view = acm.getView(dev.getView());
                        if (view != null)
                        {
                            dev.setToolTip(getLocalizedMessage("DET_OPEN_VIEW", view.getObjTitle()));
                        }
                    } else
                    {
                        //dev.setToolTip(getLocalizedMessage(dev.getToolTip()));
                        dev.setToolTip(dev.getToolTip());
                    }
                }

                for (DataEntryView dev : batchViews)
                {
                    if (StringUtils.isEmpty(dev.getToolTip()))
                    {
                        ViewIFace view = acm.getView(dev.getView());
                        if (view != null)
                        {
                            dev.setToolTip(getLocalizedMessage("DET_BATCH_EDIT", view.getObjTitle()));
                        }
                    } else
                    {
                        //dev.setToolTip(getLocalizedMessage(dev.getToolTip()));
                        dev.setToolTip(dev.getToolTip());
                    }
                }

                for (DataEntryView dev : miscViews)
                {
                    if (StringUtils.isEmpty(dev.getToolTip()))
                    {
                        ViewIFace view = acm.getView(dev.getView());
                        if (view != null)
                        {
                            dev.setToolTip(getLocalizedMessage("DET_OPEN_VIEW", view.getObjTitle()));
                        }
                    } else
                    {
                        //dev.setToolTip(getLocalizedMessage(dev.getToolTip()));
                        dev.setToolTip(dev.getToolTip());
                    }
                }
                
                // FOR DEBUGGING - Verify View Names
                /*
                List<ViewIFace> views = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getEntirelyAllViews();
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                for (ViewIFace view : views)
                {
                    log.error("Loading View Named["+view.getName()+"]");
                    hash.put(view.getName(), "X");
                }
                
                for (DataEntryView dev : stdViews)
                {
                    if (hash.get(dev.getView()) == null)
                    {
                        log.error("STD - For DEV Name["+dev.getName()+"] the view named ["+dev.getView()+"] doesn't exist!");
                    }
                }
                
                for (DataEntryView dev : miscViews)
                {
                    if (hash.get(dev.getView()) == null)
                    {
                        log.error("MSC - For DEV Name["+dev.getName()+"] the view named ["+dev.getView()+"] doesn't exist!");
                    }
                }
                */
                //initDataEntryViews(stdViews);
                //initDataEntryViews(miscViews);
                
                buildNavBoxes(stdViews, miscViews, batchViews, true);
                
            } catch (Exception ex)
            {
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataEntryTask.class, ex);
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
                                 final Vector<DataEntryView> batchList,
                                 final boolean doRegister)
    {
        buildFormNavBoxes(stdList, doRegister);
        //buildBatchNavBoxes(batchList, doRegister);
        
        if (miscList != null && !miscList.isEmpty())
        {
            availMiscViews.clear();
            for (DataEntryView dev : miscList)
            {
                ViewIFace view = AppContextMgr.getInstance().getView(null, dev.getView());
                if (view != null)
                {
                    DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                    dev.setTableInfo(tableInfo);
                    
                    if (AppContextMgr.isSecurityOn())
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
                    if (dev.getTableInfo() != null)
                    {
                        roc.addDropDataFlavor(new DataFlavorTableExt(DataEntryTask.class, RecordSetTask.RECORD_SET, dev.getTableInfo().getTableId()));
                    }
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
        UsageTracker.incrUsageCount("DE.SHOW.MISC");

        DataEntryView deView = null;
        // Bug 6433
        // I don't like commenting this out because it is good for usability.
        //if (miscViews.size() == 1)
        //{
        //    deView = miscViews.get(0);
        //    
        //} else
        //{
            ToggleButtonChooserDlg<DataEntryView> dlg = new ToggleButtonChooserDlg<DataEntryView>((Frame)UIRegistry.getTopWindow(),
                    "DET_CHOOSE_TITLE", availMiscViews, ToggleButtonChooserPanel.Type.RadioButton);
            dlg.setUseScrollPane(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                deView = dlg.getSelectedObject();
            }
        //}
        
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
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems() {
        String menuDesc = "Specify.DATA_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canModify()) {
            String    menuTitle = "DET_BTCH_REIDENT_MENU"; //$NON-NLS-1$
            String    mneu      = "DET_BTCH_REIDENT_MNEU"; //$NON-NLS-1$
            String    desc      = "DET_BTCH_REIDENT_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(ae -> doBatchReidentify());
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            rsMI.setPosition(MenuItemDesc.Position.After);
            menuItems.add(rsMI);

            //Remove Unused recs menu item
            if (false) {
                menuTitle = "REMOVE_UNUSED_RECS_MENU";
                mneu = "REMOVE_UNUSED_RECS_MNEU";
                desc = "REMOVE_UNUSED_RECS_DESC";
                mi = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
                mi.addActionListener(ae -> doRemoveUnusedRecords());
                rsMI = new MenuItemDesc(mi, menuDesc);
                rsMI.setPosition(MenuItemDesc.Position.After);
                menuItems.add(rsMI);
            }
            //Remove Duplicates menu item
            if (true && "manager".equalsIgnoreCase(AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getUserType())) {
               List<JMenuItem> dupRemovers = getDupRemoveMenuItems();
                if (dupRemovers.size() > 0 ) {
                    menuTitle = "DataEntryTask.REMOVE_DUPLICATE_RECS_MENU";
                    mneu = "DataEntryTask.REMOVE_DUPLICATE_RECS_MNEU";
                    desc = "DataEntryTask.REMOVE_DUPLICATE_RECS_DESC";
                    JMenu jm = UIHelper.createLocalizedMenu(menuTitle, mneu);
                    for (JMenuItem dr : dupRemovers) {
                        jm.add(dr);
                    }
                    rsMI = new MenuItemDesc(jm, menuDesc);
                    rsMI.setPosition(MenuItemDesc.Position.After);

                    menuItems.add(rsMI);
                }
            }
        }
        
        return menuItems;
    }

    protected List<DBTableInfo> getDupRemoveTbls() {
        List<DBTableInfo> result = new ArrayList<>();
        result.add(DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId()));
        if (!AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent()) {
            result.add(DBTableIdMgr.getInstance().getInfoById(CollectingEvent.getClassTableId()));
        }
        result.add(DBTableIdMgr.getInstance().getInfoById(CollectingTrip.getClassTableId()));
        if (AppContextMgr.getInstance().getClassObject(Discipline.class).getType().toLowerCase().contains("paleo")) {
            if (!AppContextMgr.getInstance().getClassObject(Discipline.class).getIsPaleoContextEmbedded()) {
                result.add(DBTableIdMgr.getInstance().getInfoById(PaleoContext.getClassTableId()));
            }
        }
        result.add(DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId()));
        result.add(DBTableIdMgr.getInstance().getInfoById(ReferenceWork.getClassTableId()));
        Collections.sort(result, new Comparator<DBTableInfo>() {
            public int compare(DBTableInfo ti1, DBTableInfo ti2) {
                return ti1.getTitle().compareTo(ti2.getTitle());
            }
        });
        return result;
    }

    protected List<JMenuItem> getDupRemoveMenuItems() {
        List<JMenuItem> result = new ArrayList<>();
        for (DBTableInfo tbl : getDupRemoveTbls()) {
            JMenuItem mi = UIHelper.createMenuItemWithAction((JMenu)null, String.format(getResourceString("DataEntryTask.RemoveDupsMenu"), tbl.getTitle()),
                    null, null, true, null);
            mi.addActionListener(ae -> doRemoveDuplicateRecs(tbl.getTableId()));
            result.add(mi);
        }
        return result;
    }

    protected void doRemoveDuplicateRecs(int tblId) {
        try {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(tblId);
            String sql = LocalityDuplicateRemover.getDistinctRecSql(tblInfo, false, true,
                    AppContextMgr.getInstance().getClassObject(Discipline.class).getId());
            List<Object[]> dupSets = BasicSQLUtils.query(sql);
            if (dupSets.size() == 0) {
                UIRegistry.showLocalizedMsg("NO_DUP_RECS_FOR_TABLE");
            } else {
                long totalDups = 0;
                for (Object[] row : dupSets) {
                    totalDups += (Long)row[row.length - 1] - 1L;
                }
                final DuplicateSetDisplay dusd = new DuplicateSetDisplay(tblInfo, dupSets);
                dusd.createUI();
                CustomDialog dlg = CustomDialog.create(getResourceString("DataEntryTask.DupDlgTitle"),
                        true, CustomDialog.OKCANCELAPPLY, dusd);
                dlg.createUI();
                dlg.setApplyLabel(getResourceString("DataEntryTask.RemoveBtn"));
                dlg.getOkBtn().setVisible(false);
                dlg.getApplyBtn().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SwingWorker<Pair<List<Integer>,List<Integer>>, Integer> w = new SwingWorker<Pair<List<Integer>, List<Integer>>, Integer>() {
                            @Override
                            protected Pair<List<Integer>,List<Integer>> doInBackground() throws Exception {
                                List<Integer> failedRowIdxs = new ArrayList<>();
                                List<Integer> deDupedRowIdxs = new ArrayList<>();
                                int prev = -1;
                                for (int r: dusd.getTable().getSelectedRows()) {
                                    publish(prev, r);
                                    Pair<Integer, Integer> result = null;
                                    try {
                                        result = LocalityDuplicateRemover.removeDuplicates(null, tblInfo.getName(), dupSets.get(r));
                                    } catch (Exception ex) {
                                        log.error(ex);
                                    }
                                    if (result == null || (result.getFirst() > 1 && result.getSecond() + 1 != result.getFirst())) {
                                        failedRowIdxs.add(r);
                                    } else {
                                        deDupedRowIdxs.add(r);
                                    }
                                    prev = r;
                                }
                                publish(prev, -1);
                                return new Pair<>(failedRowIdxs, deDupedRowIdxs);
                            }

                            @Override
                            protected void done() {
                                super.done();
                                try {
                                    dusd.deDupeDone(get());
                                    dlg.getOkBtn().setVisible(true);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                } catch (ExecutionException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            @Override
                            protected void process(List<Integer> chunks) {
                                super.process(chunks);
                                dusd.processProgress(chunks);
                            }
                        };
                        w.execute();
                    }
                });
                dlg.setVisible(true);
                if (dlg.getBtnPressed() == CustomDialog.OK_BTN) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void doRemoveUnusedRecords() {
        doRemoveUnusedLocalities();
    }

    protected void doRemoveUnusedLocalities() {
        try {
            RecordSet unused = getUnusedRecRs("locality");
            RecordSetIFace toDelete = chooseUnusedRecsToDelete(unused);
            for (RecordSetItemIFace item : toDelete.getItems()) {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected RecordSetIFace chooseUnusedRecsToDelete(final RecordSetIFace unused) {
        return unused;
    }

    protected String getUnusedRecSql(final String tableName) throws Exception {
        if ("locality".equals(tableName)) return "select l.localityid from locality l left join collectingevent ce on ce.localityid = l.localityid where ce.localityid is null";
        throw new Exception("getUnusedSql(): unhandled tablename: " + tableName);
    }

    protected List<Object> getUnusedRecList(final String tableName) throws Exception {
        return BasicSQLUtils.querySingleCol(getUnusedRecSql(tableName));
    }

    protected RecordSet getUnusedRecRs(final String tableName) throws Exception{
        List<Object> unused = getUnusedRecList(tableName);
        RecordSet rs = new RecordSet();
        rs.initialize();
        rs.setDbTableId(DBTableIdMgr.getInstance().getInfoByTableName(tableName).getTableId());
        for (Object obj : unused) {
            RecordSetItem rsi = new RecordSetItem();
            rsi.initialize();
            rsi.setRecordSet(rs);
            rsi.setRecordId((Integer)obj);
            rs.getRecordSetItems().add(rsi);
        }
        return rs;
    }
    /**
     * 
     */
    protected void doBatchReidentify()
    {
        final BatchReidentifyPanel panel = new BatchReidentifyPanel();
        if (panel.askForColObjs())
        {
            CustomDialog dlg = panel.createDlg();
            if (dlg != null)
            {
            	UIHelper.centerAndShow(dlg);
            }
        }
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
        //ContextMgr.dump();
        
        //stdViews  = getStdViews();
        //miscViews = getMiscViews();

        Vector<TaskConfigItemIFace> stdList  = new Vector<TaskConfigItemIFace>();
        Vector<TaskConfigItemIFace> miscList = new Vector<TaskConfigItemIFace>();
        Vector<TaskConfigItemIFace> batchList = new Vector<TaskConfigItemIFace>();
        
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
            for (DataEntryView de : batchViews)
            {
                batchList.add((DataEntryView)de.clone());
            }

        } catch (CloneNotSupportedException ex) {/*ignore*/}
        
        UsageTracker.incrUsageCount("DE.CONFIG");

        DataEntryConfigDlg dlg = new DataEntryConfigDlg(stdList, miscList, true,
                "DataEntryConfigure",
                "DET_CONFIGURE_VIEWS",
                "DET_STANDARD",
                "DET_MISC_FORMS",
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
            unregisterServices(stdList, miscList);

            // This re-registers the items
            buildNavBoxes(stdViews, miscViews, batchViews, true);
            
            viewsNavBox.validate();
            viewsNavBox.doLayout();
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            
            // Persist out to database
            DataEntryXML dataEntryXML = new DataEntryXML(stdViews, miscViews, null);
            
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
     * Unregister Form Services.
     * @param stdList the standard list
     * @param miscList the misc list
     */
    protected void unregisterServices(final Vector<? extends TaskConfigItemIFace> stdList, 
                                      final Vector<? extends TaskConfigItemIFace> miscList)
    {
        if (stdList != null)
        {
            for (TaskConfigItemIFace tii : stdList)
            {
                DataEntryView dev = (DataEntryView)tii;
                if (dev.getTableInfo() != null)
                {
                    String srvName = ServiceInfo.getHashKey(dev.getView(), this, dev.getTableInfo().getTableId());
                    ContextMgr.unregisterService(srvName);  // Must passed in the hashed Name
                }
            }
        }
        
        if (miscList != null)
        {
            for (TaskConfigItemIFace tii : miscList)
            {
                DataEntryView dev = (DataEntryView)tii;
                if (dev.getTableInfo() != null)
                {
                    String srvName = ServiceInfo.getHashKey(dev.getView(), this, dev.getTableInfo().getTableId());
                    ContextMgr.unregisterService(srvName); // Must passed in the hashed Name
                }
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
                
                Agent agent = Agent.getUserAgent();
                if (agent != null)
                {
                    colObj.setCataloger(agent);
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
                            final Object   data, 
                            final String   viewName,
                            final boolean  readOnly)
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
    @SuppressWarnings("unchecked")
    protected void processDataEntryCommands(final CommandAction cmdAction)
    {
    
        if (cmdAction.isAction(OPEN_NEW_VIEW))
        {
            String   viewName  = cmdAction.getPropertyAsString("view");
            Taskable task      = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
            Object   data      = cmdAction.getData();
            if (data instanceof RecordSetIFace)
            {
                data = getDataForEditing(cmdAction);
                editData(task != null ? task : this, data, viewName);
                
            } else if (data instanceof Pair<?, ?>)
            {
                Pair<Object, Object> p = (Pair<Object, Object>)data;
                if (p.first instanceof Taxon && p.second instanceof String && ((String)p.second).equals("createchild"))
                {
                    Taxon parentTaxon  = (Taxon)p.first;
                    Taxon childForForm = addChildToParentInForm(parentTaxon);
                    if (childForForm != null)
                    {
                        openView(task != null ? task : this, null, "Taxon", "edit", childForForm, true);
                    }
                }
            } else
            {
                openView(task != null ? task : this, null, viewName, "edit", null, true);
            }
            
        } else if (cmdAction.isAction(EDIT_DATA))
        {
            Taskable task = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
            editData(task != null ? task : this, getDataForEditing(cmdAction), null, cmdAction.getProperty("readonly") != null);
        } else if (cmdAction.isAction(BATCH_EDIT_DATA)) {
            Object   data      = cmdAction.getData();
            if (data instanceof RecordSetIFace) {
            	UIRegistry.displayInfoMsgDlg("Batch Edit this Recordset");
            	((WorkbenchTask)TaskMgr.getTask(WorkbenchTask.WORKBENCH)).batchEditRS((RecordSetIFace)data);
            } else {
            	UIRegistry.displayInfoMsgDlg("Drag a Recordset to batch edit.");
            }
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
        } else if (cmdAction.isAction("SaveBeforeSetData"))
        {
            //checkToPrintLabel(cmdAction, false);
            
        } else if (cmdAction.isAction("PrintColObjLabel"))
        {
            checkToPrintLabel(cmdAction, true);
        } else if (cmdAction.isAction("SpiceDigOcc") || cmdAction.isAction("SpiceDigTx")) {
            doSpiceDig(cmdAction);
        }
    }

    /**
     *
     * @param cmdAction
     * @param dobj
     * @return
     */
    private String getSpiceDigArg(String cmdAction, DataModelObjBase dobj) {
        if (cmdAction.equalsIgnoreCase("SpiceDigOcc")) {
            if (dobj instanceof CollectionObject && dobj.getId() != null) {
                return ((CollectionObject)dobj).getGuid().replaceAll(" ", "%20");
            }
        } else if (cmdAction.equalsIgnoreCase("SpiceDigTx")) {
            Taxon tx = null;
            if (dobj instanceof CollectionObject) {
                Determination currDet = ((CollectionObject)dobj).getCurrentDetermination();
                if (currDet != null) {
                    tx = currDet.getPreferredTaxon();
                }
            } else if (dobj instanceof Determination) {
                tx = ((Determination) dobj).getPreferredTaxon();
            } else if (dobj instanceof Taxon) {
                tx = (Taxon) dobj;
            }
            if (tx != null && tx.getIsAccepted() && tx.getFullName() != null) {
                return tx.getFullName().replaceAll(" ", "%20");
            } else if (tx != null && tx.getAcceptedTaxon() != null) {
                return tx.getAcceptedTaxon().getFullName().replaceAll(" ", "%20");
            }
        }
        return null;
    }

    private void doSpiceDig(CommandAction cmdAction) {
        Object data = cmdAction.getData();
        DataModelObjBase dobj = cmdAction.getData() instanceof DataModelObjBase ? (DataModelObjBase) cmdAction.getData() : null;
        if (dobj == null) {
            SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();
            if (subPane != null) {
                MultiView mv = subPane.getMultiView();
                if (mv != null) {
                    if (mv.getData() instanceof DataModelObjBase) {
                        dobj = (DataModelObjBase) mv.getData();
                    }
                }
            }
        }
        String spiceArg = getSpiceDigArg(cmdAction.getAction(), dobj);
        String occNameArg = cmdAction.isAction("SpiceDigOcc") ? getSpiceDigArg("SpiceDigTx", dobj) : null;
        if (spiceArg != null) {
            String url = "https://broker.spcoco.org/api/v1/";
            if (cmdAction.isAction("SpiceDigOcc")) {
                url += "frontend/?occid=" + spiceArg + (occNameArg != null ? "&namestr=" + occNameArg : "");
            } else {
                //no frontend for name only
                url += "frontend/?occid=&namestr=" + spiceArg;
            }
            try {
                URI uri = new URL(url).toURI();
                Desktop.getDesktop().browse(uri);
            } catch (Exception x) {
                log.error(x);
                x.printStackTrace();
            }
        } else {
            UIRegistry.displayInfoMsgDlg(getResourceString("CollectionObjectBusRules.RecordUnSpiceDiggable"));
        }
        cmdAction.setConsumed(true);
    }

    private Object getDataForEditing(CommandAction commandAction) {
        Object result = commandAction.getData();
        if ((commandAction.getProperties().containsKey("tableInfo") && (commandAction.getProperty("tableInfo") instanceof DBTableInfo)
            &&  ((DBTableInfo)commandAction.getProperty("tableInfo")).getTableId() == CollectionObject.getClassTableId())
            ||  "1".equals(commandAction.getPropertyAsString("displayTableId"))) {
            if ((result instanceof RecordSetIFace) && ((RecordSetIFace) result).getDbTableId() == Preparation.getClassTableId()) {
                result = getRelatedRecordset((RecordSetIFace)result, CollectionObject.getClassTableId());
            }
        }
        return result;
    }

    private RecordSetIFace getRelatedRecordset(RecordSetIFace rs, int toTableId) {
        RecordSetIFace result = rs;
        if (!(rs.getDbTableId() == Preparation.getClassTableId() && toTableId == CollectionObject.getClassTableId())) {
            log.warn("Unable to process " + rs.getDbTableId() + " to " + toTableId);
        } else {
            String sql;
            if (result.getRecordSetId() != null) {
                sql = "select distinct collectionobjectid from preparation p inner join recordsetitem rsi on rsi.recordid = p.preparationid" +
                        " where rsi.recordsetid = " + rs.getRecordSetId();
            } else {
                List<Integer> recIds = new ArrayList<>();
                for (RecordSetItemIFace item : rs.getItems()) {
                    recIds.add(item.getRecordId());
                }
                String recIdStr = recIds.toString().replace("[", "").replace("]", "");
                sql = "select distinct collectionobjectid from preparation where preparationid in(" + recIdStr + ")";
            }
            Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
            RecordSet resultRS = new RecordSet();
            resultRS.initialize();
            resultRS.setName(rs.getName() + " -> " + DBTableIdMgr.getInstance().getInfoById(toTableId).getTitle());
            resultRS.setDbTableId(toTableId);
            for (Integer id : ids) {
                RecordSetItem i = new RecordSetItem();
                i.initialize();
                i.setRecordSet(resultRS);
                i.setRecordId(id);
                resultRS.getRecordSetItems().add(i);
            }
            result = resultRS;
        }
        return result;
    }
    /**
     * @param parentTaxon
     * @return
     */
    private Taxon addChildToParentInForm(final Taxon parentTaxon)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Taxon parent = session.get(Taxon.class, parentTaxon.getId());
            Taxon newTaxon = new Taxon();
            newTaxon.initialize();
            newTaxon.setParent(parent);
            parent.getChildren().add(newTaxon);
            return newTaxon;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return null;
    }
    
    /**
     * @param cmdAction
     */
    protected void checkToPrintLabel(final CommandAction cmdAction, final boolean doOverride)
    {
        if (cmdAction.getData() instanceof CollectionObject)
        {
            CollectionObject colObj =(CollectionObject)cmdAction.getData();
                        
            Boolean doPrintLabel = null;
            
            if (!doOverride)
            {
                FormViewObj formViewObj    = getCurrentFormViewObj();
                if (formViewObj != null)
                {
                    Component comp = formViewObj.getControlByName("generateLabelChk");
                    if (comp instanceof JCheckBox)
                    {
                        doPrintLabel = ((JCheckBox)comp).isSelected();
                    }
                }
                
                if (doPrintLabel == null)
                {
                    return;
                }
            }
            
            if (doOverride || doPrintLabel)
            {
                InfoForTaskReport inforForPrinting = getLabelReportInfo();
                
                if (inforForPrinting == null)
                {
                    return;
                }
                
				if (inforForPrinting.getSpReport() == null) 
				{
					// do tests for data required by the sample labels.
					// If user customized jrxml, these tests may not be
					// necessary.
					if (colObj.getDeterminations().size() == 0
							|| colObj.getPreparations().size() == 0) 
					{
						UIRegistry.showLocalizedError("DET_NOLBL_CO", inforForPrinting.getSpAppResource().getName());
						return;
					}

				}

                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    String hql ="FROM CollectionObject WHERE id = "+colObj.getId();
                    
                    colObj = (CollectionObject)session.getData(hql);
                    
//                    Set<Determination> deters = colObj.getDeterminations();
//                    if (deters != null && deters.size() == 0)
//                    {
//                        UIRegistry.displayErrorDlg(getResourceString("NO_DETERS_ERROR"));
//                        
//                    } else
                    {
                        RecordSet rs = new RecordSet();
                        rs.initialize();
                        rs.setName(colObj.getIdentityTitle());
                        rs.setDbTableId(CollectionObject.getClassTableId());
                        rs.addItem(colObj.getId());
                        
                        dispatchReport(inforForPrinting, rs, "ColObjLabel");
                    }
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
        }
    }
    
    /**
     * @return a loan invoice if one exists.
     * 
     * If more than one report is defined for loan then user must choose.
     * 
     * Fairly goofy code. Eventually may want to add ui to allow labeling resources as "invoice" (see printLoan()).
     */
    public InfoForTaskReport getLabelReportInfo()
    {
        DataProviderSessionIFace session = null;
        ChooseFromListDlg<InfoForTaskReport> dlg = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<AppResourceIFace> reps = AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME);
            reps.addAll(AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME));
            Vector<InfoForTaskReport> repInfo = new Vector<InfoForTaskReport>();
            
            for (AppResourceIFace rep : reps)
            {
                Properties params = rep.getMetaDataMap();
                String     tableid = params.getProperty("tableid"); 
                SpReport   spReport = null;
                boolean    includeIt = false;
                try
                {
                    Integer tblId = null;
                    try
                    {
                        tblId = Integer.valueOf(tableid);
                    }
                    catch (NumberFormatException ex)
                    {
                        //continue;
                    }
                    if (tblId == null)
                    {
                        continue;
                    }
                    
                    if (tblId.equals(CollectionObject.getClassTableId()))
                    {
                        includeIt = true;
                    }
                    else if (tblId.equals(-1))
                    {
                        QueryIFace q = session.createQuery("from SpReport spr join spr.appResource apr "
                              + "join spr.query spq "
                              + "where apr.id = " + ((SpAppResource )rep).getId() 
                              + " and spq.contextTableId = " + CollectionObject.getClassTableId(), false);
                        List<?> spReps = q.list();
                        if (spReps.size() > 0)
                        {
                            includeIt = true;
                            spReport = (SpReport )((Object[] )spReps.get(0))[0];
                            spReport.forceLoad();
                            if (spReps.size() > 1)
                            {
                                //should never happen
                                log.error("More than SpReport exists for " + rep.getName());
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsTask.class, ex);
                    //skip this res
                }
                if (includeIt)
                {
                    repInfo.add(new InfoForTaskReport((SpAppResource )rep, spReport));
                }
            }
            
            if (repInfo.size() == 0)
            {
                UIRegistry.displayInfoMsgDlgLocalized("InteractionsTask.NoInvoiceFound", 
                            DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()));
                return null;
            }
            
            if (repInfo.size() == 1)
            {
                return repInfo.get(0);
            }
            
            dlg = new ChooseFromListDlg<InfoForTaskReport>((Frame) UIRegistry.getTopWindow(), getResourceString("REP_CHOOSE_INVOICE"), repInfo);
            dlg.setVisible(true);
            if (dlg.isCancelled()) 
            { 
                return null; 
            }
            return dlg.getSelectedObject();

        }
        finally
        {
            session.close();
            if (dlg != null)
            {
                dlg.dispose();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel() {
        //return new BasicPermisionPanel(null, "ENABLE");
        //return null to remove task from security ui
        return null;
    }

    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, true},
                                {true, true, true, true}};
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

    private String getDubiousFormPrefName(int tableId) {
        return "DubiousFormDisplay." + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getName() + "." + tableId;
    }

    private boolean confirmDubiousFormDisplay(int tableId) {
        boolean confirmed = tableId == CollectionObject.getClassTableId();//AppPreferences.getRemote().getBoolean(getDubiousFormPrefName(tableId), false);
//        if (!confirmed) {
//            confirmed = UIRegistry.displayConfirm(getResourceString("DataEntryTask.DUBIOUS_FORM_MSG_TITLE"),
//                    String.format(getResourceString("DataEntryTask.NO_SUITABLE_DATA_ENTRY_FORM_MSG"),
//                            DBTableIdMgr.getInstance().getInfoById(tableId).getTitle()),
//                    getResourceString("YES"), getResourceString("NO"), JOptionPane.WARNING_MESSAGE);
//        }
        return confirmed;
    }

    /**
     * @param cmdAction
     */
    protected void processRecordSetCommand(final CommandAction cmdAction)
    {
        if (!processRecordSetCommand(cmdAction, stdViews)) {
            if (!processRecordSetCommand(cmdAction, miscViews) && cmdAction.getDstObj() instanceof RecordSetIFace) {
                RecordSetIFace rs = (RecordSetIFace) cmdAction.getDstObj();
                if (rs != null && !InteractionsTask.isInteractionTable(rs.getDbTableId())) {
                    if (rs.getDbTableId() == Preparation.getClassTableId()) {
                        rs = getRelatedRecordset(rs, CollectionObject.getClassTableId());
                    }
                    try {
                        FormPane formPane = createFormFor(this, "", null, null, rs);
                        String tblTitle = DBTableIdMgr.getInstance().getInfoById(rs.getDbTableId()).getTitle();
                        boolean canShow = formPane != null ? confirmDubiousFormDisplay(rs.getDbTableId()) : false;
                        if (canShow) {
                            addSubPaneToMgr(formPane);
                        } else {
                            UIRegistry.displayInfoMsgDlg(String.format(getResourceString("DataEntryTask.NO_FORM_AVAILABLE"), tblTitle));
                        }

                    } catch (Exception x) {
                        //probably not a DataEntryTask action. ignore.
                        //log.warn(x);
                    }
                }
            }
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
            
            unregisterServices(stdViews, miscViews);
            
            viewsNavBox.clear();
            initializeViewsNavBoxFromXML();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#canRequestContext()
     */
    @Override
    protected boolean canRequestContext()
    {
        return Uploader.checkUploadLock(this) == Uploader.NO_LOCK;
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
        protected ImageIcon bgImg = IconManager.getIcon("SpecifySmallSplash");
        
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
        @Override
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
            bgImg = IconManager.getIcon("SpecifySmallSplash");
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

}
