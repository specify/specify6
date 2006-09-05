/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.helpers.UIHelper;
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
    protected TreeDataService<T,D,I> dataService;
    
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
        dataService = TreeDataServiceFactory.createService();
        toolBarItems = new Vector<ToolBarItemDesc>();
        menuItems = new Vector<MenuItemDesc>();
	}

	@Override
	public void initialize()
	{
		if(!isInitialized)
		{
			isInitialized = true;
			
			List<D> defs = dataService.getAllTreeDefs(treeDefClass);
			createMenus(defs);
			createNavBoxes(defs);
		}
	}
	
	protected void createNavBoxes(@SuppressWarnings("unused") List<D> defs)
	{
		ActionListener toggleViewAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				toggleViewMode();
			}
		};
		NavBox actions = new NavBox(getResourceString("Actions"));
		String label = getResourceString("ToggleViewMode");
		String iconName = "TTV_ToggleViewMode";
		NavBoxItemIFace toggleViewItem = NavBox.createBtn(label,iconName,IconManager.IconSize.Std16,toggleViewAction); 
		actions.add(toggleViewItem);

		ActionListener saveTreeAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				saveTree();
			}
		};
		label = getResourceString("SaveTree");
		iconName = "Save";
		NavBoxItemIFace saveTreeItem = NavBox.createBtn(label,iconName,IconManager.IconSize.Std16,saveTreeAction);
		actions.add(saveTreeItem);
		
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
	
	@SuppressWarnings({ "serial", "serial" })
	protected void createMenus(@SuppressWarnings("unused") List<D> defs)
	{
		String label    = getResourceString("TaxonMenuItem");
		String mnemonic = getResourceString("TaxonMnemonic");
		String accDescr = getResourceString("TaxonAccesDesc");
		AbstractAction action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				// do nothing
			}
		};
		JMenuItem sampleMenuItem = UIHelper.createMenuItem(null,label,mnemonic,accDescr,true,action);
		MenuItemDesc mid = new MenuItemDesc(sampleMenuItem,"AdvMenu");
		menuItems.add(mid);		
	}
	
	protected void showTree(D treeDef)
	{
		for(TreeTableViewer<T,D,I> ttv: visibleTTVs)
		{
			if(ttv.getTreeDef() == treeDef)
			{
				SubPaneMgr.getInstance().setSelectedComponent(ttv);
				return;
			}
		}
		
		for(TreeDefinitionEditor<T,D,I> defEd: visibleTreeDefEditors)
		{
			if(defEd.getDisplayedTreeDef().getTreeDefId().equals(treeDef.getTreeDefId()))
			{
				SubPaneMgr.getInstance().setSelectedComponent(defEd);
				return;
			}
		}
		
		ContextMgr.requestContext(this);
		String tabName = getResourceString(name) + ": " + treeDef.getName();
    	TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef,tabName,this);
    	visibleTTVs.add(ttv);
    	SubPaneMgr.getInstance().addPane(ttv);
	}
	
	public TreeNodeFindWidget getFinderWidget()
	{
		return finderWidget;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
	 * @return
	 */
	@Override
	public List<MenuItemDesc> getMenuItems()
	{
		return menuItems;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
	 * @return
	 */
	@Override
	public SubPaneIFace getStarterPane()
	{
		return new SimpleDescPane(title, this, "This is the taxonomy tree editor");
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
	 * @return
	 */
	@Override
	public List<ToolBarItemDesc> getToolBarItems()
	{
        return toolBarItems;
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    @SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseTreeTask> getTaskClass()
    {
        return this.getClass();
    }
    
	public void saveTree()
    {
		SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();
		if(subPane instanceof TreeTableViewer)
		{
			TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)subPane;
	    	ttv.commitStructureToDb();
		}
		else if(subPane instanceof TreeDefinitionEditor)
		{
			TreeDefinitionEditor<T,D,I> defEd = (TreeDefinitionEditor<T,D,I>)subPane;
			defEd.saveToDb();
		}
    }
    
    public void toggleViewMode()
    {
    	TreeTableViewer<T,D,I> ttv = (TreeTableViewer<T,D,I>)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.toggleViewMode();
    	ttv.repaint();
    	UICacheManager.forceTopFrameRepaint();
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