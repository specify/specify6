/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * A base task that provides functionality in common to all tasks
 * that provide UI for tree-structured data.
 *
 * @code_status Beta
 * @author jstewart
 *
 * @param <T>
 * @param <D>
 * @param <I>
 */
public abstract class BaseTreeTask <T extends Treeable<T,D,I>,
							        D extends TreeDefIface<T,D,I>,
							        I extends TreeDefItemIface<T,D,I>>
							        extends BaseTask
{
    protected static final Logger log = Logger.getLogger(BaseTreeTask.class);
    protected static DataFlavor TREE_DEF_FLAVOR = new DataFlavor(TreeDefIface.class,TreeDefIface.class.getName());

    public static final String OPEN_TREE        = "OpenTree";
    public static final String EDIT_TREE_DEF    = "EditTreeDef";

    
    /** The toolbar items provided by this task. */
    protected static Vector<ToolBarItemDesc> treeToolBarItems = null;
    
    protected NavBox                  treeNavBox       = null;
    protected NavBox                  treeDefNavBox    = null;
    protected NavBox                  unlockNavBox     = null;
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    
    /** The class of {@link TreeDefIface} handled by this task. */
    protected Class<D> treeDefClass;
    protected Class<T> treeClass;
    
    protected D currentDef;
    
    protected boolean currentDefInUse;
    protected boolean isOpeningTree        = false;
    protected SubPaneIFace visibleSubPane;
    
    protected String commandTypeString;
    
    protected BusinessRulesIFace businessRules = null;
    
    protected Action treeEditAction    = null;
    protected Action treeDefEditAction = null;
    protected Action unlockAction      = null;
    

	/**
     * Constructor.
     * 
	 * @param name the name of the task
	 * @param title the visible name of the task
	 */
	protected BaseTreeTask(final String name, final String title)
	{
		super(name, title);
		
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
        
        setIconName("TreePref");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#initialize()
	 */
	@Override
	public synchronized void initialize()
	{
        if (!isInitialized)
        {
            isInitialized = true;
            
            TreeTaskMgr.getInstance().add(this);

            currentDef   = getCurrentTreeDef();
            menuItems    = createMenus();

            if (commandTypeString != null)
            {
                CommandDispatcher.register(commandTypeString,this);
            }
            
            treeNavBox    = new NavBox(getResourceString("BaseTreeTask.EditTrees"));
            treeDefNavBox = new NavBox(getResourceString("BaseTreeTask.TreeDefs"));
            unlockNavBox  = new NavBox(getResourceString("BaseTreeTask.UNLOCK"));
            
            navBoxes.add(treeNavBox);
            navBoxes.add(treeDefNavBox);
            navBoxes.add(unlockNavBox);
            
            if (isTreeOnByDefault())
            {
                DBTableInfo treeTI = DBTableIdMgr.getInstance().getByClassName(getTreeClass().getName());
                DBTableInfo tdTI   = DBTableIdMgr.getInstance().getByClassName(getTreeDefClass().getName());
                
                PermissionSettings treePerms = treeTI.getPermissions();
                PermissionSettings tdPerms   = tdTI.getPermissions();
                
                if (UIHelper.isSecurityOn())
                {
                    System.out.println(treeTI.getTitle()+ " "+treePerms.toString());
                    if (treePerms.canView())
                    {
                        treeEditAction = createActionForTreeEditing(treeTI.getTitle(), treePerms.canModify());                        
                    } 
                    
                    if (tdPerms.canView() && tdPerms.canModify())
                    {
                        treeDefEditAction = createActionForTreeDefEditing(treeTI.getTitle());
                        unlockAction      = createActionForTreeUnlocking(treeTI.getTitle(), true);
                    } 
                } else
                {
                    treeEditAction    = createActionForTreeEditing(treeTI.getTitle(), true);
                    treeDefEditAction = createActionForTreeDefEditing(treeTI.getTitle());
                    unlockAction      = createActionForTreeUnlocking(treeTI.getTitle(), true);
                }
            }
        }
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getNavBoxes()
     */
    @Override
    public List<NavBoxIFace> getNavBoxes()
    {
        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        /*RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }*/

        return extendedNavBoxes;

    }

    /**
     * Loads the appropriate tree NavBtns into the UI.
     */
    protected void loadTreeNavBoxes()
    {
        treeNavBox.clear();
        treeDefNavBox.clear();
        unlockNavBox.clear();
        
        if (UIHelper.isSecurityOn())
        {
            if (!DBTableIdMgr.getInstance().getByShortClassName(treeClass.getSimpleName()).getPermissions().canView())
            {
                return;
            }
        }
        
        if (isTreeOnByDefault())
        {
            TreeTaskMgr.getInstance().fillNavBoxes(treeNavBox, treeDefNavBox, unlockNavBox);
            for (NavBoxItemIFace nbi : treeNavBox.getItems())
            {
                ((RolloverCommand)nbi.getUIComponent()).addDropDataFlavor(null);
            }
        } 
    }
    
    /**
     * @return
     */
    public Action getTreeEditAction()
    {
        return treeEditAction;
    }
    
    /**
     * @return
     */
    public Action getTreeDefEditAction()
    {
        return treeDefEditAction;
    }
    
    /**
     * @return
     */
    public Action getTreeUnlockAction()
    {
        return unlockAction;
    }
    
    /**
     * @return the Class for the Tree Def
     */
    public Class<D> getTreeDefClass()
    {
        return treeDefClass;
    }

    /**
     * @return the class for tree
     */
    public Class<T> getTreeClass()
    {
        return treeClass;
    }

    protected abstract D getCurrentTreeDef();
    
    /**
     * Opens a TreeViewer in a BG Thread.
     * @param isEditModeArg the mode
     */
    protected void openTreeViewerInBGThread(final String titleArg, final boolean isEditModeArg)
    {
        
    	if (!isOpeningTree) // as oppose to opening the TreeDef
    	{
            final TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(titleArg, treeDefClass.getSimpleName(), "def", TaskSemaphoreMgr.SCOPE.Discipline, true);
            final boolean isViewMode = action == TaskSemaphoreMgr.USER_ACTION.ViewMode;
            
            if (action == TaskSemaphoreMgr.USER_ACTION.ViewMode || action == TaskSemaphoreMgr.USER_ACTION.OK)
            {
        		isOpeningTree = true;
    	        SwingWorker bgWorker = new SwingWorker()
    	        {
    	            private TreeTableViewer<T,D,I> treeViewer;
    	            
    	            @Override
    	            public Object construct()
    	            {
    	                UsageTracker.incrUsageCount("TR.OPEN."+treeDefClass.getSimpleName());
    
    	                treeViewer = createTreeViewer(titleArg, !isViewMode);
    	                if (isViewMode)
    	                {
    	                    treeViewer.setDoUnlock(false);
    	                }
    	                return treeViewer;
    	            }
    	
    	            @Override
    	            public void finished()
    	            {
    	                super.finished();
    	                ContextMgr.requestContext(BaseTreeTask.this);
    	                currentDefInUse = true;
    	                visibleSubPane = treeViewer;
    	                addSubPaneToMgr(treeViewer);
    	                isOpeningTree = false;
    	                
    	                TreeTaskMgr.checkLocks();
    	            }
    	        };
    	        bgWorker.start();
            }
    	}
    }
    
    /**
     * Creates an ActionListener for Editing the tree.
     * @param isEditMode whether it is in edit mode
     * @return the AL
     */
    protected Action createActionForTreeUnlocking(final String titleArg, final boolean isEditMode)
    {
        return new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String lockName     = treeDefClass.getSimpleName();
                String formLockName = lockName + "Form";
                
                UsageTracker.incrUsageCount("TD.UNLOCK."+treeDefClass.getSimpleName());

                boolean okToUnlock = TaskSemaphoreMgr.askUserToUnlock(titleArg, lockName, TaskSemaphoreMgr.SCOPE.Discipline);
                if (okToUnlock)
                {
                    if (TaskSemaphoreMgr.isLocked(titleArg, formLockName, TaskSemaphoreMgr.SCOPE.Discipline))
                    {
                        TaskSemaphoreMgr.unlock(titleArg, formLockName, TaskSemaphoreMgr.SCOPE.Discipline);
                    } else
                    {
                        // Show Dialog ?? or Taskbar message ??
                        log.warn(titleArg + " form was not locked.");
                    }
                    
                    if (TaskSemaphoreMgr.isLocked(titleArg, lockName, TaskSemaphoreMgr.SCOPE.Discipline))
                    {
                        TaskSemaphoreMgr.unlock(titleArg, lockName, TaskSemaphoreMgr.SCOPE.Discipline);
                    } else
                    {
                        // Show Dialog ?? or Taskbar message ??
                        log.warn(titleArg + " was not locked.");
                    }
                }
            }
        };
    }
    
    /**
     * Creates an ActionListener for Editing the tree.
     * @param isEditMode whether it is in edit mode
     * @return the AL
     */
    protected Action createActionForTreeEditing(final String titleArg, final boolean isEditMode)
    {
        return new AbstractAction()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                if (!currentDefInUse)
                {
                    if (visibleSubPane == null || visibleSubPane instanceof TreeTableViewer<?,?,?>)
                    {
                        openTreeViewerInBGThread(titleArg, isEditMode);
                    } else
                    {
                        switchViewType(isEditMode, true);
                    }
                }
                else
                {
                    // If the TTV is already open, show it.
                    if (visibleSubPane instanceof TreeTableViewer<?,?,?>)
                    {
                        TreeTableViewer<?,?,?> treeViewer = (TreeTableViewer<?,?,?>)visibleSubPane;
                        if (treeViewer.isEditMode() == isEditMode)
                        {
                            SubPaneMgr.getInstance().showPane(visibleSubPane);
                        } else
                        {
                            switchViewType(isEditMode, true);
                        }
                    }
                    else // Otherwise a def editor must be open.  Close it and open a TTV.
                    {
                        switchViewType(isEditMode, false);
                    }
                    TreeTaskMgr.checkLocks();
                }
            }
        };
    }
    
    /**
     * @return the action for tree editing
     */
    private Action createActionForTreeDefEditing(final String titleArg)
    {
        return new AbstractAction()
        {
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                if (!currentDefInUse)
                {
                    TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(titleArg, treeDefClass.getSimpleName(), "def", TaskSemaphoreMgr.SCOPE.Discipline, false);
                    if (action == TaskSemaphoreMgr.USER_ACTION.OK)
                    {
                        SwingWorker bgWorker = new SwingWorker()
                        {
                            private TreeDefinitionEditor<T,D,I> defEditor;
                            
                            @Override
                            public Object construct()
                            {
                                UsageTracker.incrUsageCount("TD.OPEN."+treeDefClass.getSimpleName());
                                defEditor = createDefEditor(titleArg);
                                return defEditor;
                            }
    
                            @Override
                            public void finished()
                            {
                                super.finished();
                                ContextMgr.requestContext(BaseTreeTask.this);
                                currentDefInUse = true;
                                visibleSubPane = defEditor;
                                addSubPaneToMgr(defEditor);
                                TreeTaskMgr.checkLocks();
                            }
                        };
                        bgWorker.start();
                    }
                }
                else
                {
                    // If the def editor is already open, show it.
                    if (visibleSubPane instanceof TreeDefinitionEditor<?,?,?>)
                    {
                        SubPaneMgr.getInstance().showPane(visibleSubPane);
                    }
                    else // Otherwise a TTV must be open.  Close it an open a def editor.
                    {
                        // NOTE: The "false" is meaningless for the Tree Def Editor
                        switchViewType(false, false);
                    }
                }
            }
        };
    }
    
    /**
     * Returns whether the tree is on by default for a discipline.
     * @return whether the tree is on by default for a discipline.
     */
    public boolean isTreeOnByDefault()
    {
        return true;
    }
    
    /**
     * Creates a simple menu item that brings this task into context.
     * 
	 * @param defs a list of tree definitions handled by this task
	 */
	protected Vector<MenuItemDesc> createMenus()
	{
        Vector<MenuItemDesc> menus = new Vector<MenuItemDesc>();
        
        return menus;
	}
    
    /**
     * @param isEditMode
     * @return
     */
    @SuppressWarnings("unchecked")
    protected TreeTableViewer<T,D,I> createTreeViewer(final String titleArg, final boolean isEditMode)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
            TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef, titleArg, this, isEditMode);
            return ttv;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
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
     * Opens a {@link SubPaneIFace} for viewing/editing a {@link TreeDefIface} object.
     */
    public TreeDefinitionEditor<T,D,I> openTreeDefEditor(D treeDef)
    {
       ContextMgr.requestContext(this);
        String tabName = getResourceString("TreeDefEditor") + ": " + treeDef.getName();
        TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef, tabName, this, true);
        addSubPaneToMgr(defEditor);
        return defEditor;
    }
    
    /**
     * Opens a {@link SubPaneIFace} for viewing/editing the current {@link TreeDefIface} object.
     * @param isEditMode whether it is in edit mode or not
     * @return the editor
     */
    @SuppressWarnings("unchecked")
    protected TreeDefinitionEditor<T,D,I> createDefEditor(@SuppressWarnings("unused") final String titleArg)
	{
        DataProviderSessionIFace session = null;
        try
        {
            // XXX SECURITY
            boolean canEditTreeDef = true;

            session = DataProviderFactory.getInstance().createSession();
            
            D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
    	    TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef, title, this, canEditTreeDef);
    	    
            return defEditor;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
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
	 * Switches the current view from {@link TreeTableViewer} to {@link TreeDefinitionEditor}
     * or vice versa.  If the current view is neither of these, this does nothing.
	 */
    protected void switchViewType(final boolean isEditMode, final boolean switchMode)
	{
    	if (!isOpeningTree)
    	{
    		isOpeningTree = true;
	        SwingWorker bgWorker = new SwingWorker()
	        {
	            private SubPaneIFace oldPane = null;
	            
	            @Override
	            public Object construct()
	            {
	            	String tabTitle = UIRegistry.getResourceString(name);
	            	
	                if (visibleSubPane instanceof TreeTableViewer && !switchMode)
	                {
	                    ((TreeTableViewer<?,?,?>)visibleSubPane).setDoUnlock(false);
	                    TreeDefinitionEditor<T,D,I> defEditor = createDefEditor(tabTitle);
	                    oldPane         = visibleSubPane;
	                    currentDefInUse = true;
	                    visibleSubPane  = defEditor;
	                }
	                else if (visibleSubPane instanceof TreeDefinitionEditor || switchMode)
	                {
	                    ((TreeDefinitionEditor<?,?,?>)visibleSubPane).setDoUnlock(false);
	                    TreeTableViewer<T,D,I> treeViewer = createTreeViewer(tabTitle, isEditMode);
	                    oldPane         = visibleSubPane;
	                    currentDefInUse = true;
	                    visibleSubPane  = treeViewer;
	                }
	                return visibleSubPane;
	            }
	
	            @Override
	            public void finished()
	            {
	                super.finished();
	                
	                SubPaneMgr.getInstance().replacePane(oldPane, visibleSubPane);
	                isOpeningTree = false;
	            }
	        };
	        bgWorker.start();
    	}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
	 */
	@Override
	public List<MenuItemDesc> getMenuItems()
	{
		return menuItems;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
	 */
	@Override
    public SubPaneIFace getStarterPane()
    {
        // This starter pane will only be visible for a brief moment while the tree loads.
        // It doesn't need to be fancy.
        return starterPane = StartUpTask.createFullImageSplashPanel(getResourceString("BaseTreeTask.Trees"), this);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
	 */
	@Override
	public List<ToolBarItemDesc> getToolBarItems()
	{
	    if (treeToolBarItems == null)
	    {
	        treeToolBarItems = new Vector<ToolBarItemDesc>();
    
            String label     = getResourceString("BaseTreeTask.Trees");
            String hint     = getResourceString("tree_hint");
            ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);
            treeToolBarItems.add(new ToolBarItemDesc(btn));
	    }
        toolbarItems = treeToolBarItems;
        
        return toolbarItems;
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
	@SuppressWarnings("unchecked")
    @Override
	public Class<? extends BaseTreeTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @SuppressWarnings("unchecked")
    @Override
	public void subPaneRemoved(SubPaneIFace subPane)
	{
        if (subPane.getTask().equals(this) && subPane == visibleSubPane)
        {
            currentDefInUse = false;
            visibleSubPane = null;
        }
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(final CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        currentDef = getCurrentTreeDef();
        
        if (cmdAction.isAction(APP_RESTART_ACT) || cmdAction.isAction(APP_START_ACT))
        {
            loadTreeNavBoxes();
        }
    }
    
    

    /**
     * Runs the query synchronously and filles the vector.
     * @param sqlStr the SQL string
     * @param list the list to fill
     */
    protected void fillListWithIds(final String sqlStr, final Vector<Integer> list)
    {
        list.clear();
        
        String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sqlStr);
        log.debug(sql);
        
        Connection conn = null;        
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                list.add(rs.getInt(1));
            }
            rs.close();

        } 
        catch (SQLException ex)
        {
            UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
            log.error("SQLException: " + ex.toString()); //$NON-NLS-1$
            log.error(ex.getMessage());
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
                ex.printStackTrace();
            }
        }
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#canRequestContext()
     */
    @Override
    protected boolean canRequestContext()
    {
        /*
         * Probably can't get here when uploading but just in case
         */
        return Uploader.checkUploadLock();
    }

}
