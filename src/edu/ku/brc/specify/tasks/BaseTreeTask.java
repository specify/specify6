/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

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
import edu.ku.brc.specify.ui.treetables.TreeNodeFindWidget;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UICacheManager;

/**
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
    protected List<ToolBarItemDesc> toolBarItems;
    protected List<MenuItemDesc> menuItems;
    protected TreeNodeFindWidget finderWidget;
    
    protected Vector<TreeTableViewer<T,D,I>> visibleTTVs;
    protected Vector<TreeDefinitionEditor<T,D,I>> visibleTreeDefEditors;
    
    protected Class<D> treeDefClass;
    	
	protected BaseTreeTask(final String name, final String title)
	{
		super(name,title);
        visibleTTVs = new Vector<TreeTableViewer<T,D,I>>();
        visibleTreeDefEditors = new Vector<TreeDefinitionEditor<T,D,I>>();
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
	}

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
	
	protected void createNavBoxes(@SuppressWarnings("unused") List<D> defs)
	{
		NavBox actions = new NavBox(getResourceString("Actions"));

		NavBox find = new NavBox(getResourceString("FindNode"));
		finderWidget = new TreeNodeFindWidget(this);
		find.add((NavBoxItemIFace)(finderWidget));
		
        NavBox admin = new NavBox(getResourceString("AdministrationTasks"));
        ActionListener openTreeDefEd = new ActionListener()
        {
        	public void actionPerformed(ActionEvent ae)
        	{
        		openTreeDefEditor();
        	}
        };
        String btnLabel = getResourceString("TreeDefEditor");
        admin.add(NavBox.createBtn(btnLabel,"TreeDefEditorIcon", IconManager.IconSize.Std16,openTreeDefEd));

        navBoxes.addElement(actions);
		navBoxes.addElement(find);
        navBoxes.addElement(admin);
	}
	
	protected void createMenus(@SuppressWarnings("unused") List<D> defs)
	{
		// do nothing
	}
	
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
    	SubPaneMgr.getInstance().addPane(ttv);
    	return ttv;
	}
	
	public TreeNodeFindWidget getFinderWidget()
	{
		return finderWidget;
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
		return new SimpleDescPane(title, this, "This is the taxonomy tree editor");
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
     *
     *
     * @param name
     * @param where in TOPVIEW, BOTTOMVIEW, or BOTHVIEWS views
     */
    public void find(String key,int where,boolean wrap)
    {
    	TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.find(key,where,wrap);
    }
    
    public void findNext(String key,int where,boolean wrap)
    {
    	TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.findNext(key,where,wrap);
    }
    
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
    		JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
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

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#subPaneShown(edu.ku.brc.af.core.SubPaneIFace)
	 */
	@Override
	public void subPaneShown(SubPaneIFace subPane)
	{
		// TODO Auto-generated method stub
		super.subPaneShown(subPane);
		
		if(subPane instanceof TreeTableViewer)
		{
			finderWidget.setEnabled(true);
		}
		else
		{
			finderWidget.setEnabled(false);
		}
	}
    
    
}