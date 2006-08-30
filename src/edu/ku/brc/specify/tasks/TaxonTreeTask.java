/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
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
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.ui.treetables.TreeNodeFindWidget;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MemoryDropDownButton;
import edu.ku.brc.ui.UICacheManager;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TaxonTreeTask extends BaseTask implements DualViewSearchable
{
    //private static final Logger log = Logger.getLogger(TaxonTreeTask.class);

    public static final String TAXON = "Taxon_Tree";
    
    protected TreeDataService dataService;
    
    protected List<ToolBarItemDesc> toolBarItems;
    protected Hashtable<TreeDefinitionIface, JMenuItem> defToMenuItem;
    protected List<MenuItemDesc> menuItems;
    protected List<JComponent> toolBarBtnItems;
    protected TreeNodeFindWidget finderWidget;
    
    protected TreeTableViewer currentVisibleTTV;

    protected Vector<TreeTableViewer> visibleTTVs;
    
	public TaxonTreeTask()
	{
        super(TAXON, getResourceString(TAXON));
        this.icon = IconManager.getIcon(TAXON,IconManager.IconSize.Std24);
        visibleTTVs = new Vector<TreeTableViewer>();
        defToMenuItem = new Hashtable<TreeDefinitionIface, JMenuItem>();
        CommandDispatcher.register(TAXON, this);
        dataService = TreeDataServiceFactory.createService();
        initialize();
	}

	public void initialize()
	{
		if(!isInitialized)
		{
			isInitialized = true;
			
			List<TreeDefinitionIface> defs = dataService.getAllTreeDefs(TaxonTreeDef.class);
			createToolBarButton(defs);
			createMenus();
			
			ActionListener toggleViewAction = new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					toggleViewMode();
				}
			};

			ActionListener saveTreeAction = new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					saveTree();
				}
			};

			NavBox actions = new NavBox(getResourceString("Actions"));
			String label = getResourceString("ToggleViewMode");
			String iconName = "TTV_ToggleViewMode";
			NavBoxItemIFace toggleViewItem = NavBox.createBtn(label,iconName,IconManager.IconSize.Std16,toggleViewAction); 
			actions.add(toggleViewItem);
			
			label = getResourceString("SaveTree");
			iconName = "Save";
			NavBoxItemIFace saveTreeItem = NavBox.createBtn(label,iconName,IconManager.IconSize.Std16,saveTreeAction);
			actions.add(saveTreeItem);
			
			navBoxes.addElement(actions);

			NavBox find = new NavBox(getResourceString("FindNode"));
			finderWidget = new TreeNodeFindWidget(this);
			find.add((NavBoxItemIFace)(finderWidget));
			navBoxes.addElement(find);			
		}
	}
	
	protected void createToolBarButton(final List<TreeDefinitionIface> defs)
	{
		toolBarItems = new Vector<ToolBarItemDesc>();
		
		toolBarBtnItems = new ArrayList<JComponent>();
		for(TreeDefinitionIface def: defs)
		{
			final TreeDefinitionIface chosenDef = def;
			JMenuItem defMenuItem = new JMenuItem(def.getName());
			defToMenuItem.put(def,defMenuItem);
			defMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showTaxonTree(chosenDef);
				}
			});
			toolBarBtnItems.add(defMenuItem);
		}
		
		String label = getResourceString(TAXON);
		String iconName = TAXON;
		String hint = getResourceString("taxontree_hint");
        if (toolBarBtnItems.size() > 0)
        {
            MemoryDropDownButton btn = createMemoryToolbarButton(label,iconName,hint,toolBarBtnItems);
            toolBarItems.add(new ToolBarItemDesc(btn));
        }
	}
	
	protected void showTaxonTree(TreeDefinitionIface treeDef)
	{
		for(TreeTableViewer ttv: visibleTTVs)
		{
			if(ttv.getTreeDef() == treeDef)
			{
				SubPaneMgr.getInstance().setSelectedComponent(ttv);
				return;
			}
		}
		
		ContextMgr.requestContext(this);
		String tabName = getResourceString(name) + ": " + treeDef.getName();
    	TreeTableViewer ttv = new TreeTableViewer(treeDef,tabName,this);
    	visibleTTVs.add(ttv);
    	SubPaneMgr.getInstance().addPane(ttv);
	}
	
	protected void createMenus()
	{
		menuItems = new Vector<MenuItemDesc>();
		String label    = getResourceString("TaxonMenuItem");
		String mnemonic = getResourceString("TaxonMnemonic");
		String accDescr = getResourceString("TaxonAccesDesc");
		AbstractAction action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Menu item clicked");
			}
		};
		JMenuItem sampleMenuItem = UIHelper.createMenuItem(null,label,mnemonic,accDescr,true,action);
		MenuItemDesc mid = new MenuItemDesc(sampleMenuItem,"AdvMenu");
		menuItems.add(mid);		
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
    public Class getTaskClass()
    {
        return this.getClass();
    }
    
    @Override
	public void subPaneRemoved(SubPaneIFace subPane)
	{
    	if(subPane instanceof TreeTableViewer)
    	{
    		TreeTableViewer ttv = (TreeTableViewer)subPane;
    		visibleTTVs.remove(ttv);
    	}
	}

	public void saveTree()
    {
    	TreeTableViewer ttv = (TreeTableViewer)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.commitStructureToDb();
    }
    
    public void toggleViewMode()
    {
    	TreeTableViewer ttv = (TreeTableViewer)SubPaneMgr.getInstance().getCurrentSubPane();
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
    	TreeTableViewer ttv = (TreeTableViewer)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.find(key,where,wrap);
    }
    
    public void findNext(int where,boolean wrap)
    {
    	TreeTableViewer ttv = (TreeTableViewer)SubPaneMgr.getInstance().getCurrentSubPane();
    	ttv.findNext(where,wrap);
    }
}
