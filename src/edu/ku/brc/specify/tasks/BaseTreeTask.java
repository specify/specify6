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
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;

/**
 * A base task that provides functionality in common to all tasks
 * that provide UI for tree-structured data.
 *
 * @code_status Beta
 * @author jstewart
 */
public class BaseTreeTask <T extends Treeable<T,D,I>,
							        D extends TreeDefIface<T,D,I>,
							        I extends TreeDefItemIface<T,D,I>>
							        extends BaseTask
{
    /** The toolbar items provided by this task. */
    protected List<ToolBarItemDesc> toolBarItems;
    
    /** The menu items provided by this task. */
    protected List<MenuItemDesc> menuItems;
    
    /** The class of {@link TreeDefIface} handled by this task. */
    protected Class<D> treeDefClass;
    
    protected Hashtable<SubPaneIFace, NavBoxItemIFace> viewToButtonMap = new Hashtable<SubPaneIFace, NavBoxItemIFace>();
    protected Hashtable<D,NavBoxItemIFace> defToButtonMap = new Hashtable<D, NavBoxItemIFace>();
    
    protected String menuItemText;
    protected String menuItemMnemonic;
    protected String starterPaneText;
    protected String commandTypeString;
    
    public static final String OPEN_TREE        = "OpenTree";
    public static final String EDIT_TREE_DEF    = "EditTreeDef";
    public static final String SWITCH_VIEW_TYPE = "SwitchViewType";
    
    protected static DataFlavor TREE_DEF_FLAVOR = new DataFlavor(TreeDefIface.class,TreeDefIface.class.getName());
    
	/**
     * Constructor.
     * 
	 * @param name the name of the task
	 * @param title the visible name of the task
	 */
	protected BaseTreeTask(final String name, final String title)
	{
		super(name,title);
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#initialize()
	 */
	@Override
	public synchronized void initialize()
	{
		if(!isInitialized)
		{
			isInitialized = true;
            TreeDataService<T,D,I> dataService = TreeDataServiceFactory.createService();
			List<D> defs = dataService.getAllTreeDefs(treeDefClass);
			createMenus(defs);
			createNavBoxes(defs);
            
            if (commandTypeString != null)
            {
                CommandDispatcher.register(commandTypeString,this);
            }
		}
	}
	
	/**
     * Creates the {@link NavBox}s returned by a call to 
     * 
	 * @param defs
	 */
	protected void createNavBoxes(@SuppressWarnings("unused") List<D> defs)
	{
        NavBox actionsBox = new NavBox(getResourceString("Actions"),false,false);
        
        RolloverCommand openTreeCmdBtn    = (RolloverCommand)makeDnDNavBtn(actionsBox, "view tree", null, new CommandAction(commandTypeString,OPEN_TREE), null, false, false);
        RolloverCommand editDefCmdBtn     = (RolloverCommand)makeDnDNavBtn(actionsBox, "edit tree def", null, new CommandAction(commandTypeString,EDIT_TREE_DEF), null, false, false);
        NavBoxItemIFace switchViewTypeBtn = NavBox.createBtn("swithc view type", null, null, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                switchViewType();
            }
        });
        actionsBox.add(switchViewTypeBtn);
        
        openTreeCmdBtn.addDropDataFlavor(TREE_DEF_FLAVOR);
        editDefCmdBtn.addDropDataFlavor(TREE_DEF_FLAVOR);
        
        NavBox defsBox = new NavBox(getResourceString(name),false,true);
        
        for (final D def: defs)
        {
            CommandAction cmd = new CommandAction(commandTypeString,OPEN_TREE);
            cmd.setProperty(OPEN_TREE, def);
            
            final NavBoxButton navItem = (NavBoxButton)makeDnDNavBtn(defsBox, def.getName(), null, cmd, null, true, false);
            navItem.addDragDataFlavor(TREE_DEF_FLAVOR);
            navItem.addDropDataFlavor(TREE_DEF_FLAVOR);
            
            defToButtonMap.put(def, navItem);
            
            // setup the popup menu for this tree def
            JPopupMenu popup = new JPopupMenu();
            JMenuItem editDefItem = new JMenuItem("Edit tree definition");
            editDefItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    openTreeDefEditor(def);
                }
            });
            popup.add(editDefItem);
            navItem.setPopupMenu(popup);
            
            defsBox.add((NavBoxItemIFace)navItem);
        }
        
        navBoxes.add(actionsBox);
        navBoxes.add(defsBox);
	}
	
	/**
     * Creates a simple menu item that brings this task into context.
     * 
	 * @param defs a list of tree definitions handled by this task
	 */
	protected void createMenus(@SuppressWarnings("unused") List<D> defs)
	{
        JMenuItem menuItem = new JMenuItem(menuItemText);
        menuItem.setMnemonic(menuItemMnemonic.charAt(0));
        MenuItemDesc miDesc = new MenuItemDesc(menuItem, "AdvMenu");
        menuItems.add(miDesc);
        
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                requestContext();
            }
        });
	}
    
	/**
     * Displays the tree associated with the given {@link TreeDefIface}.
     * 
	 * @param treeDef the {@link TreeDefIface} corresponding to the tree to be displayed
	 * @return a {@link SubPaneIFace} for displaying the tree
	 */
	protected TreeTableViewer<T,D,I> showTree(D treeDef)
	{
		ContextMgr.requestContext(this);
		String tabName = getResourceString(name) + ": " + treeDef.getName();
    	TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef,tabName,this);
    	addSubPaneToMgr(ttv);
        
        NavBoxItemIFace button = defToButtonMap.get(treeDef);
        if (button != null)
        {
            button.setEnabled(false);
            viewToButtonMap.put(ttv, button);
        }
        
    	return ttv;
	}
    
    /**
     * Opens a {@link SubPaneIFace} for viewing/editing a {@link TreeDefIface} object.
     */
	public TreeDefinitionEditor<T,D,I> openTreeDefEditor(D treeDef)
	{
        ContextMgr.requestContext(this);
	    String tabName = getResourceString("TreeDefEditor");
	    TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef,tabName,this);
        addSubPaneToMgr(defEditor);
        
        NavBoxItemIFace button = defToButtonMap.get(treeDef);
        if (button != null)
        {
            button.setEnabled(false);
            viewToButtonMap.put(defEditor, button);
        }

        return defEditor;
	}
    
	@SuppressWarnings("unchecked")
    protected void switchViewType()
	{
	    // find out what def this refers to
	    SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();

	    if (subPane instanceof TreeTableViewer)
	    {
	        TreeDefIface treeDef = ((TreeTableViewer)subPane).getTreeDef();
	        SubPaneMgr.getInstance().closeCurrent();
	        openTreeDefEditor((D)treeDef);
	        return;
	    }
        else if (subPane instanceof TreeDefinitionEditor)
        {
            TreeDefIface treeDef = ((TreeDefinitionEditor)subPane).getDisplayedTreeDef();
            SubPaneMgr.getInstance().closeCurrent();
            showTree((D)treeDef);
            return;
        }
	    return;
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
        // update the nav box buttons for the tree defs
        NavBoxItemIFace button = viewToButtonMap.get(subPane);
        if (button != null)
        {
            button.setEnabled(true);
        }
	}

    @Override
    public void doCommand(CommandAction cmdAction)
    {
        UIRegistry.getStatusBar().setText("");
        
        if (cmdAction.isType(commandTypeString))
        {
            D treeDef = getTreeDefFromCommand(cmdAction.getData());
            if (cmdAction.getAction().equals(OPEN_TREE) && treeDef != null)
            {
                showTree(treeDef);
            }
            else if (cmdAction.getAction().equals(EDIT_TREE_DEF) && treeDef != null)
            {
                openTreeDefEditor(treeDef);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected D getTreeDefFromCommand(Object cmdActionObj)
    {
        if (cmdActionObj != null && cmdActionObj instanceof CommandAction)
        {
            Object def = ((CommandAction)cmdActionObj).getProperty(OPEN_TREE);
            if (def != null && def.getClass().isAssignableFrom(treeDefClass))
            {
                return (D)def;
            }
        }
        
        // TODO: ask for a tree def
        // this happens when the user simply clicks the "open tree" or "edit def" buttons
        System.out.println("Ask user for a def");
        
        return null;
    }
}