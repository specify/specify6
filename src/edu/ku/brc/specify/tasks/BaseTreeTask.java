/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
import edu.ku.brc.af.core.AppContextMgr;
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
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.ui.treetables.TreeBrowserPanel;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
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
@SuppressWarnings("serial")
public abstract class BaseTreeTask <T extends Treeable<T,D,I>,
							        D extends TreeDefIface<T,D,I>,
							        I extends TreeDefItemIface<T,D,I>>
							        extends BaseTask implements Comparable<BaseTreeTask<?,?,?>>
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
    protected NavBox                  browseNavBox     = null;
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
    protected Action browseAction      = null;
    
    protected static SubPaneIFace starterPaneTree = null;

    

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
        
        TreeTaskMgr.getInstance().add(this);
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
            
            currentDef   = getCurrentTreeDef();
            menuItems    = createMenus();

            if (commandTypeString != null)
            {
                CommandDispatcher.register(commandTypeString,this);
            }
            
            treeNavBox     = new NavBox(getResourceString("BaseTreeTask.EditTrees"));
            treeDefNavBox  = new NavBox(getResourceString("BaseTreeTask.TreeDefs"));
            unlockNavBox   = new NavBox(getResourceString("BaseTreeTask.UNLOCK"));
            browseNavBox   = new NavBox(getResourceString("BaseTreeTask.BROWSE"));
            
            navBoxes.add(browseNavBox);
            navBoxes.add(treeNavBox);
            navBoxes.add(treeDefNavBox);
            navBoxes.add(unlockNavBox);
            
            //if (isTreeOnByDefault())
            {
                DBTableInfo treeTI = DBTableIdMgr.getInstance().getByClassName(getTreeClass().getName());
                DBTableInfo tdTI   = DBTableIdMgr.getInstance().getByClassName(getTreeDefClass().getName());
                
                PermissionSettings treePerms = treeTI.getPermissions();
                PermissionSettings tdPerms   = tdTI.getPermissions();
                
                if (AppContextMgr.isSecurityOn())
                {
                    //System.out.println(treeTI.getTitle()+ " "+treePerms.toString());
                    if (treePerms.canView())
                    {
                        browseAction = createActionForTreeEditing(treeTI.getTitle(), false);
                        if (treePerms.canModify())
                        {
                            treeEditAction = createActionForTreeEditing(treeTI.getTitle(), treePerms.canModify());   
                        }
                    } 
                    
                    if (tdPerms.canView())
                    {
                        if (tdPerms.canModify())
                        {
                            treeDefEditAction = createActionForTreeDefEditing(treeTI.getTitle());
                            unlockAction      = createActionForTreeUnlocking(treeTI.getTitle(), true);
                            browseAction      = createActionForTreeEditing(treeTI.getTitle(), false);
                        } 
                    }
                } else
                {
                    treeEditAction    = createActionForTreeEditing(treeTI.getTitle(), true);
                    treeDefEditAction = createActionForTreeDefEditing(treeTI.getTitle());
                    unlockAction      = createActionForTreeUnlocking(treeTI.getTitle(), true);
                    browseAction      = createActionForTreeEditing(treeTI.getTitle(), false);
                }
                
            }
        }
        isShowDefault = true;
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
        browseNavBox.clear();
        
        boolean skip = AppContextMgr.isSecurityOn() && !DBTableIdMgr.getInstance().getByShortClassName(treeClass.getSimpleName()).getPermissions().canView();
        
        TreeTaskMgr.getInstance().fillNavBoxes(treeNavBox, treeDefNavBox, unlockNavBox, browseNavBox);
        
        //log.debug(treeClass.getSimpleName()+"  skip "+skip+"  cnt: "+treeNavBox.getComponentCount());
        if (!skip) //if (isTreeOnByDefault())
        {
            for (NavBoxItemIFace nbi : treeNavBox.getItems())
            {
                ((RolloverCommand)nbi.getUIComponent()).addDropDataFlavor(null);
            }
        } 
        
        //log.debug(treeClass.getSimpleName()+"  skip "+skip+"  cnt: "+treeNavBox.getComponentCount());
        browseNavBox.setVisible(browseNavBox.getComponentCount() > 0);
        treeNavBox.setVisible(treeNavBox.getComponentCount() > 0);
        treeDefNavBox.setVisible(treeDefNavBox.getComponentCount() > 0);
        unlockNavBox.setVisible(unlockNavBox.getComponentCount() > 0);
        TreeTaskMgr.checkLocks();
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
     * @return
     */
    public Action getTreeBrowseAction()
    {
        return browseAction;
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
            PermissionSettings perms = DBTableIdMgr.getInstance().getByShortClassName(treeClass.getSimpleName()).getPermissions();
            final boolean isViewOnly = perms != null && perms.isViewOnly();
            final boolean isViewable = perms == null || perms.canView();
            final boolean isEditable = perms == null || perms.canModify();
            
            final TaskSemaphoreMgr.USER_ACTION action = isViewOnly || !isEditModeArg ? TaskSemaphoreMgr.USER_ACTION.ViewMode 
            		: TaskSemaphoreMgr.lock(titleArg, treeDefClass.getSimpleName(), "def", TaskSemaphoreMgr.SCOPE.Discipline, true, null, true);
            final boolean isViewMode = action == TaskSemaphoreMgr.USER_ACTION.ViewMode;
            
            if ((isViewable && (action == TaskSemaphoreMgr.USER_ACTION.ViewMode || action == TaskSemaphoreMgr.USER_ACTION.OK)) || 
                (isEditable && (action == TaskSemaphoreMgr.USER_ACTION.OK || action == TaskSemaphoreMgr.USER_ACTION.ViewMode))) 
            {
        		isOpeningTree = true;
    	        SwingWorker bgWorker = new SwingWorker()
    	        {
    	            private TreeTableViewer<T,D,I> treeViewer;
    	            
    	            @Override
    	            public Object construct()
    	            {
    	                UsageTracker.incrUsageCount("TR.OPEN."+treeDefClass.getSimpleName());
    
    	                treeViewer = createTreeViewer(titleArg, !isViewMode && isEditable);
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
                    
                    if (TaskSemaphoreMgr.isLockedOrInUse(titleArg, lockName, TaskSemaphoreMgr.SCOPE.Discipline))
                    {
                        TaskSemaphoreMgr.unlock(titleArg, lockName, TaskSemaphoreMgr.SCOPE.Discipline);
                    } else
                    {
                        // Show Dialog ?? or Taskbar message ??
                        log.warn(titleArg + " was not locked.");
                    }
                    
                    TreeTaskMgr.checkLocks();
                }
            }
        };
    }
    
    /**
     * Creates an ActionListener for Editing the tree.
     * @param isEditMode whether it is in edit mode
     * @return the AL
     */
    protected Action createActionForTreeBrowse(final String titleArg)
    {
        return new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                String tableName = treeClass.getSimpleName().toLowerCase();
                String clsName   = treeClass.getSimpleName();
                
                String sql = String.format("SELECT %sID FROM %s WHERE RankID = 0 AND %sID = %d ", clsName, tableName, BaseTreeTask.this.currentDef.getClass().getSimpleName(), BaseTreeTask.this.currentDef.getTreeDefId());
                //log.debug(sql);
                
                Integer rootId = BasicSQLUtils.getCount(sql);
                if (rootId != null)
                {
                    TreeBrowserPanel browsePanel = new TreeBrowserPanel(tableName, treeClass, BaseTreeTask.this.currentDef.getTreeDefId(), rootId);
                    SubPaneMgr.getInstance().addPane(new SimpleDescPane(titleArg, BaseTreeTask.this, browsePanel));
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
            @Override
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
                        switchViewType(isEditMode, true);
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
                    TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(titleArg, treeDefClass.getSimpleName(), "def", TaskSemaphoreMgr.SCOPE.Discipline, false, null, true);
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
    protected TreeDefinitionEditor<T,D,I> createDefEditor(final String titleArg)
	{
        DataProviderSessionIFace session = null;
        try
        {
            // XXX SECURITY
            boolean canEditTreeDef = true;

            session = DataProviderFactory.getInstance().createSession();
            
            D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
    	    TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef, titleArg, this, canEditTreeDef);
    	    
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
	                    DBTableInfo treeTI  = DBTableIdMgr.getInstance().getByClassName(getTreeClass().getName());
	                    String      trTitle = treeTI.getTitle();
	                    String lockName     = treeDefClass.getSimpleName();
	                    String formLockName = lockName + "Form";
	                    
	                    if (!TaskSemaphoreMgr.isLockedOrInUse(trTitle, formLockName, TaskSemaphoreMgr.SCOPE.Discipline))
	                    {
    	                    ((TreeTableViewer<?,?,?>)visibleSubPane).setDoUnlock(false);
    	                    TreeDefinitionEditor<T,D,I> defEditor = createDefEditor(tabTitle);
    	                    oldPane         = visibleSubPane;
    	                    currentDefInUse = true;
    	                    visibleSubPane  = defEditor;
	                    } else
	                    {
	                        UIRegistry.showLocalizedError("BaseTreeTask.NO_EDT_TRDEF", treeTI.getTitle());
	                    }
	                }
	                else if (visibleSubPane instanceof TreeDefinitionEditor && switchMode)
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
	                
	                if (oldPane != null)
	                {
	                	SubPaneMgr.getInstance().replacePane(oldPane, visibleSubPane);
	                }
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
	    if (starterPaneTree == null)
        {
	        // This starter pane will only be visible for a brief moment while the tree loads.
	        // It doesn't need to be fancy.
	        starterPaneTree = StartUpTask.createFullImageSplashPanel(getResourceString("BaseTreeTask.Trees"), this);
        }
        
        return starterPaneTree;
        
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
        //log.debug(sql);
        
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
        return Uploader.checkUploadLock(this) == Uploader.NO_LOCK;
    }
    
    /**
     * Adds a SubPane to the Mgr and caches a pointer to it and clear the starterPane data member.
     * @param subPane the subpane in question
     */
    protected SubPaneIFace addSubPaneToMgr(final SubPaneIFace subPane)
    {
        if (starterPaneTree != null)
        {
            SubPaneMgr.getInstance().replacePane(starterPaneTree, subPane);
            starterPaneTree = null;
            
        } else
        {
            SubPaneMgr.getInstance().addPane(subPane);
        }
        return subPane;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#setStarterPane(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void setStarterPane(SubPaneIFace pane)
    {
        starterPaneTree = pane;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isStarterPane()
     */
    @Override
    public boolean isStarterPane()
    {
        return starterPaneTree != null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isPermissionsSettable()
     */
    @Override
    public boolean isPermissionsSettable()
    {
        return false;
    }
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(BaseTreeTask<?, ?, ?> obj)
    {
        if (getTreeClass() != null && obj.getTreeClass() != null)
        {
            DBTableInfo treeTI  = DBTableIdMgr.getInstance().getByClassName(getTreeClass().getName());
            DBTableInfo treeTI2 = DBTableIdMgr.getInstance().getByClassName(obj.getTreeClass().getName());
            return treeTI.getTitle().compareTo(treeTI2.getTitle());
        } 
        return 1;
    }

}
