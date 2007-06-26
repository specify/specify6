/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
import edu.ku.brc.ui.IconManager;

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
    
    protected String menuItemText;
    protected String menuItemMnemonic;
    protected String starterPaneText;
    
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
		}
	}
	
	/**
     * Creates the {@link NavBox}s returned by a call to 
     * 
	 * @param defs
	 */
	protected void createNavBoxes(@SuppressWarnings("unused") List<D> defs)
	{
        NavBox defsBox = new NavBox(getResourceString(name),false,true);
        
        for (final D def: defs)
        {
            // setup the click action for the tree def
            ActionListener al = new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    TreeTableViewer<T,D,I> ttv = showTree(def);
                    Object source = ae.getSource();
                    if (source instanceof NavBoxItemIFace)
                    {
                        NavBoxItemIFace srcButton = (NavBoxItemIFace)ae.getSource();
                        viewToButtonMap.put(ttv, srcButton);
                        srcButton.setEnabled(false);
                    }
                }
            };
            final NavBoxItemIFace navItem = NavBox.createBtn(def.getName(), name, IconManager.IconSize.Std16, al);
            
            // setup the popup menu for this tree def
            JPopupMenu popup = new JPopupMenu();
            JMenuItem editDefItem = new JMenuItem("Edit tree definition");
            editDefItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    TreeDefinitionEditor<T,D,I> defEditor = openTreeDefEditor(def);
                    navItem.setEnabled(false);
                    viewToButtonMap.put(defEditor, navItem);
                }
            });
            popup.add(editDefItem);
            ((NavBoxButton)navItem).setPopupMenu(popup);
            
            defsBox.add(navItem);
        }
        
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
        return defEditor;
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
}