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
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
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
import edu.ku.brc.ui.JStatusBar;
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
							implements DualViewSearchable
{
    /** The toolbar items provided by this task. */
    protected List<ToolBarItemDesc> toolBarItems;
    
    /** The menu items provided by this task. */
    protected List<MenuItemDesc> menuItems;
    
    /** A list of {@link TreeTableViewer}s already visible that are handled by this task. */
    protected Vector<TreeTableViewer<T,D,I>> visibleTTVs;
    
    /** A list of {@link TreeDefinitionEditor}s already visible that are handled by this task. */
    protected Vector<TreeDefinitionEditor<T,D,I>> visibleTreeDefEditors;
    
    /** The class of {@link TreeDefIface} handled by this task. */
    protected Class<D> treeDefClass;
    
    /** A button that switches the view from tree view to tree definition view. */
    protected NavBoxItemIFace defEditorNavBox;

    /** A button that switches the view from tree definition view to tree view. */
    protected NavBoxItemIFace treeViewerNavBox;
    	
	/**
     * Constructor.
     * 
	 * @param name the name of the task
	 * @param title the visible name of the task
	 */
	protected BaseTreeTask(final String name, final String title)
	{
		super(name,title);
        visibleTTVs = new Vector<TreeTableViewer<T,D,I>>();
        visibleTreeDefEditors = new Vector<TreeDefinitionEditor<T,D,I>>();
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#initialize()
	 */
	@Override
	public void initialize()
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
		NavBox actions = new NavBox(getResourceString("Actions"));

        final NavBox admin = new NavBox(getResourceString("AdministrationTasks"));
        ActionListener openTreeDefEd = new ActionListener()
        {
        	public void actionPerformed(ActionEvent ae)
        	{
                // switch the visible nav box out for the other one
                if (admin.getItems().contains(defEditorNavBox))
                {
                    admin.remove(defEditorNavBox);
                    admin.add(treeViewerNavBox);
                }
                else
                {
                    admin.remove(treeViewerNavBox);
                    admin.add(defEditorNavBox);
                }
        		switchView();
        	}
        };
        String defEditorLabel = getResourceString("TreeDefEditor");
        defEditorNavBox = NavBox.createBtn(defEditorLabel,"TreeDefEditorIcon", IconManager.IconSize.Std16,openTreeDefEd);
        admin.add(defEditorNavBox);
        
        String treeViewerLabel = getResourceString("TreeViewer");
        treeViewerNavBox = NavBox.createBtn(treeViewerLabel, "TreeViewer", IconManager.IconSize.Std16,openTreeDefEd);

        navBoxes.addElement(actions);
        navBoxes.addElement(admin);
	}
	
	/**
     * Does nothing.  Subclasses can override this method to put menus into the application's menubar.
     * 
	 * @param defs a list of tree definitions handled by this task
	 */
	protected void createMenus(@SuppressWarnings("unused") List<D> defs)
	{
		// do nothing
	}
	
	/**
     * Displays the tree associated with the given {@link TreeDefIface}.
     * 
	 * @param treeDef the {@link TreeDefIface} corresponding to the tree to be displayed
	 * @return a {@link SubPaneIFace} for displaying the tree
	 */
	protected TreeTableViewer<T,D,I> showTree(D treeDef)
	{
		for(TreeTableViewer<T,D,I> ttv: visibleTTVs)
		{
			if(ttv.getTreeDef() == treeDef)
			{
				SubPaneMgr.getInstance().setSelectedComponent(ttv);
				return null;
			}
		}
		
		for(TreeDefinitionEditor<T,D,I> defEd: visibleTreeDefEditors)
		{
			if(defEd.getDisplayedTreeDef().getTreeDefId().equals(treeDef.getTreeDefId()))
			{
				SubPaneMgr.getInstance().setSelectedComponent(defEd);
				return null;
			}
		}
		
		ContextMgr.requestContext(this);
		String tabName = getResourceString(name) + ": " + treeDef.getName();
    	TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef,tabName,this);
    	visibleTTVs.add(ttv);
    	addSubPaneToMgr(ttv);
    	return ttv;
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
		return starterPane = new SimpleDescPane(title, this, "This is the taxonomy tree editor");
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
    
    /**
     * Finds the node with the given name in the current {@link TreeTableViewer}.
     * 
     * @param key the node name
     * @param where an indicator of which view (top or bottom) to show the result in (if any)
     * @param wrap whether or not to perform a wrap when doing the search
     */
    public void find(String key,int where,boolean wrap)
    {
    	TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.find(key,where,wrap);
    }
    
    /**
     * Finds the next node with the given name in the current {@link TreeTableViewer}.
     * 
     * @param key the node name
     * @param where an indicator of which view (top or bottom) to show the result in (if any)
     * @param wrap whether or not to perform a wrap when doing the search
     */
    public void findNext(String key,int where,boolean wrap)
    {
    	TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.findNext(key,where,wrap);
    }
    
    /**
     * Switches the view from a {@link TreeTableViewer} to the associated {@link TreeDefinitionEditor} and vice versa.
     */
    public void switchView()
    {
        SubPaneMgr paneMgr = SubPaneMgr.getInstance();
        SubPaneIFace curSubPane = paneMgr.getCurrentSubPane();
        if(curSubPane instanceof TreeTableViewer)
        {
            TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)paneMgr.getCurrentSubPane();
            if(ttv.aboutToShutdown())
            {
                paneMgr.removePane(ttv);

                String editorName = getResourceString("TreeDefEditor");
                TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(ttv.getTreeDef(),editorName,this);
                visibleTreeDefEditors.add(defEditor);
                paneMgr.addPane(defEditor);
            }
        }
        else if (curSubPane instanceof TreeDefinitionEditor)
        {
            TreeDefinitionEditor<T,D,I> tde = (TreeDefinitionEditor<T,D,I>)paneMgr.getCurrentSubPane();
            if (tde.aboutToShutdown())
            {
                paneMgr.removePane(tde);
                
                showTree(tde.getDisplayedTreeDef());
            }
        }
    }
    
    /**
     * Opens a {@link SubPaneIFace} for viewing/editing a {@link TreeDefIface} object.
     */
    public void openTreeDefEditor()
    {
    	SubPaneMgr paneMgr = SubPaneMgr.getInstance();
    	SubPaneIFace curSubPane = paneMgr.getCurrentSubPane();
    	if(curSubPane instanceof TreeTableViewer)
    	{
    		TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)paneMgr.getCurrentSubPane();
            if(ttv.aboutToShutdown())
            {
            	paneMgr.removePane(ttv);

            	String editorName = getResourceString("TreeDefEditor");
            	TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(ttv.getTreeDef(),editorName,this);
            	visibleTreeDefEditors.add(defEditor);
            	paneMgr.addPane(defEditor);
            }
    	}
    	else
    	{
    		JStatusBar statusBar = UIRegistry.getStatusBar();
    		//TODO localize
    		statusBar.setText("Tree definition editor already open");
    	}
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
	public void subPaneRemoved(SubPaneIFace subPane)
	{
    	if(subPane instanceof TreeTableViewer)
    	{
    		TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)subPane;
    		visibleTTVs.remove(ttv);
    	}
    	else if(subPane instanceof TreeDefinitionEditor)
    	{
    		TreeDefinitionEditor<T,D,I> defEd = (TreeDefinitionEditor<T,D,I>)subPane;
    		visibleTreeDefEditors.remove(defEd);
    	}
	}
}