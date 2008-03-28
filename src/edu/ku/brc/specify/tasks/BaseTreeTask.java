/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 19, 2008
 *
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 19, 2008
 *
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 19, 2008
 *
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BusinessRulesIFace;

/**
 * A base task that provides functionality in common to all tasks
 * that provide UI for tree-structured data.
 *
 * @code_status Beta
 * @author jstewart
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 19, 2008
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
    
    /** The toolbar items provided by this task. */
    protected List<ToolBarItemDesc> toolBarItems;
    
    /** The menu items provided by this task. */
    protected List<MenuItemDesc> menuItems;
    
    /** The class of {@link TreeDefIface} handled by this task. */
    protected Class<D> treeDefClass;
    
    protected D currentDef;
    
    protected boolean currentDefInUse;
    protected SubPaneIFace visibleSubPane;
    
    protected JMenu subMenu;
    protected JMenuItem showTreeMenuItem;
    protected JMenuItem editTreeMenuItem;
    protected JMenuItem editDefMenuItem;
    
    protected String menuItemText;
    protected String menuItemMnemonic;
    protected String starterPaneText;
    protected String commandTypeString;
    
    protected BusinessRulesIFace businessRules = null;

    
	/**
     * Constructor.
     * 
	 * @param name the name of the task
	 * @param title the visible name of the task
	 */
	protected BaseTreeTask(final String name, final String title)
	{
		super(name,title);
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
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
            navBoxes     = Collections.emptyList();
            toolBarItems = Collections.emptyList();
            menuItems    = createMenus();

            if (commandTypeString != null)
            {
                CommandDispatcher.register(commandTypeString,this);
            }
        }
	}
    
    protected abstract D getCurrentTreeDef();
    
    /**
     * Opens a TreeViewer in a BG Thread.
     * @param isEditMode the mode
     */
    protected void openTreeViewerInBGThread(final boolean isEditMode)
    {
        SwingWorker bgWorker = new SwingWorker()
        {
            private TreeTableViewer<T,D,I> treeViewer;
            
            @Override
            public Object construct()
            {
                treeViewer = createTreeViewer(isEditMode);
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
            }
        };
        bgWorker.start();
    }
    
    /**
     * Creates an ActionListener for Editing the tree.
     * @param isEditMode whether it is in edit mode
     * @return the AL
     */
    protected ActionListener createALForTreeEditing(final boolean isEditMode)
    {
        return new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                if (!currentDefInUse)
                {
                    if (visibleSubPane == null || visibleSubPane instanceof TreeTableViewer<?,?,?>)
                    {
                        openTreeViewerInBGThread(isEditMode);
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
                }
            }
        };
    }
    
    /**
     * Returns whether the tree is on by default for a discipline.
     * @return whether the tree is on by default for a discipline.
     */
    protected boolean isTreeOnByDefault()
    {
        return true;
    }
    
    /**
     * Enables / Disables the menus depending on the discipline.
     */
    protected void adjustMenus()
    {
        String clsName    = treeDefClass.getSimpleName();
        String discipline = Discipline.getCurrentDiscipline().getName();
        
        String prefName = "Trees.Menu." + discipline + "." + clsName;
        
        boolean isMenuEnabled = AppPreferences.getRemote().getBoolean(prefName, isTreeOnByDefault(), true);
        subMenu.setEnabled(isMenuEnabled);
    }
	
	/**
     * Creates a simple menu item that brings this task into context.
     * 
	 * @param defs a list of tree definitions handled by this task
	 */
	protected List<MenuItemDesc> createMenus()
	{
        Vector<MenuItemDesc> menus = new Vector<MenuItemDesc>();
        subMenu = new JMenu(menuItemText);
        
        MenuItemDesc treeSubMenuMI = new MenuItemDesc(subMenu, "AdvMenu");
        menus.add(treeSubMenuMI);
        
        showTreeMenuItem = new JMenuItem(getResourceString("TTV_SHOW_TREE_MENU_ITEM"));
        showTreeMenuItem.addActionListener(createALForTreeEditing(false));
        
        // XXX SECURITY - Check to see if they can edit
        boolean canEdit = true;
        if (canEdit)
        {
            editTreeMenuItem = new JMenuItem(getResourceString("TTV_EDIT_TREE_MENU_ITEM"));
            editTreeMenuItem.addActionListener(createALForTreeEditing(true));
        } else
        {
            editTreeMenuItem = null;
        }
        
        // XXX SECURITY - Check to see if they can edit the tree def
        editDefMenuItem = new JMenuItem(getResourceString("TTV_EDIT_DEF_MENU_ITEM"));
        editDefMenuItem.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                if (!currentDefInUse)
                {
                    SwingWorker bgWorker = new SwingWorker()
                    {
                        private TreeDefinitionEditor<T,D,I> defEditor;
                        
                        @Override
                        public Object construct()
                        {
                            defEditor = createDefEditor();
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
                        }
                    };
                    bgWorker.start();
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
        });

        if (editTreeMenuItem != null)
        {
            subMenu.add(editTreeMenuItem);
        }
        subMenu.add(showTreeMenuItem);
        subMenu.add(editDefMenuItem);

        adjustMenus();
        
        return menus;
	}
    
    /**
     * @param isEditMode
     * @return
     */
    @SuppressWarnings("unchecked")
    protected TreeTableViewer<T,D,I> createTreeViewer(final boolean isEditMode)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
            String tabName = treeDef.getName();
            TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef, tabName, this, isEditMode);
            return ttv;
            
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
     * Opens a {@link SubPaneIFace} for viewing/editing the current {@link TreeDefIface} object.
     * @param isEditMode whether it is in edit mode or not
     * @return the editor
     */
    @SuppressWarnings("unchecked")
    protected TreeDefinitionEditor<T,D,I> createDefEditor()
	{
        DataProviderSessionIFace session = null;
        try
        {
            // XXX SECURITY
            boolean canEditTreeDef = true;

            session = DataProviderFactory.getInstance().createSession();
            
            D      treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
            String tabName = treeDef.getName();
    	    TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef, tabName, this, canEditTreeDef);
    	    
            return defEditor;
            
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
	 * Switches the current view from {@link TreeTableViewer} to {@link TreeDefinitionEditor}
     * or vice versa.  If the current view is neither of these, this does nothing.
	 */
    protected void switchViewType(final boolean isEditMode, final boolean switchMode)
	{
        SwingWorker bgWorker = new SwingWorker()
        {
            private SubPaneIFace oldPane = null;
            
            @Override
            public Object construct()
            {
                if (visibleSubPane instanceof TreeTableViewer && !switchMode)
                {
                    TreeDefinitionEditor<T,D,I> defEditor = createDefEditor();
                    oldPane         = visibleSubPane;
                    currentDefInUse = true;
                    visibleSubPane  = defEditor;
                }
                else if (visibleSubPane instanceof TreeDefinitionEditor || switchMode)
                {
                    
                    TreeTableViewer<T,D,I> treeViewer = createTreeViewer(isEditMode);
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
            }
        };
        bgWorker.start();

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
        return starterPane = new SimpleDescPane(title, this, starterPaneText);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
	 */
	@Override
	public List<ToolBarItemDesc> getToolBarItems()
	{
        return toolBarItems;
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
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        UIRegistry.getStatusBar().setText("");
        
        if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            currentDef = getCurrentTreeDef();
            adjustMenus();
        }
    }
}