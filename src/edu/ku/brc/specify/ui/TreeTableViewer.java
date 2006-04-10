package edu.ku.brc.specify.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.TreeFactory;
import edu.ku.brc.specify.helpers.TreeTableUtils;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.ui.dnd.TransferableMutableTreeNode;
import edu.ku.brc.specify.ui.dnd.TreeNodeTransferHandler;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-based view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 * 
 * @author jstewart
 */
public class TreeTableViewer extends BaseSubPane implements TreeSelectionListener
{
	protected JPanel uiComp;

	protected JPanel northPanel;
	protected JPanel southPanel;
	protected JPanel buttonPanel;
	protected Vector<AbstractButton> buttons;
	protected JLabel statusBar;
	protected JLabel selectMessageLabel;
	
	protected JTree tree;
	protected FilteredDefaultTreeModel model;
	protected DefaultMutableTreeNode rootNode;
	protected RankBasedTreeCellRenderer mainRenderer;
	protected NameBasedTreeCellRenderer subRenderer;
	protected TreeNodeTransferHandler transferHandler;
	protected boolean subRendsEnabled;
	protected JPopupMenu popupMenu;
	
	protected Vector<Treeable> deletedNodes;
	
	protected JComboBox defsBox;
	
	protected Class treeableClass;

	/**
	 * Build a TreeTableViewer to view/edit the data found
	 * 
	 * @param treeDefClass a Class object representing the DB table to find definitions in
	 * @param name a String name for this viewer/editor
	 * @param task the owning Taskable
	 */
	public TreeTableViewer( final Class treeDefClass,
							final String name,
							final Taskable task )
	{
		super(name,task);
		Session session = HibernateUtil.getCurrentSession();
		Criteria c = session.createCriteria(treeDefClass);
		List results = c.list();
		HibernateUtil.closeSession();
				
		init(results);
	}
	
	/**
	 * Initialize all of the UI components in the viewer/editor.
	 * 
	 * @param definitions a list of TreeDefinitionIface objects that can be handled in this TreeTableViewer
	 */
	protected void init( List<TreeDefinitionIface> definitions )
	{
		deletedNodes = new Vector<Treeable>();
		buttons = new Vector<AbstractButton>();
		
		this.uiComp = new JPanel();
		uiComp.setLayout(new BorderLayout());

		selectMessageLabel = new JLabel("Select a tree in the combobox above");
		uiComp.add(selectMessageLabel,BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		uiComp.add(southPanel,BorderLayout.SOUTH);

		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		uiComp.add(northPanel,BorderLayout.NORTH);
		
		Vector<Object> defs = new Vector<Object>(definitions);
		defs.add(0, "Choose a tree definition");
		defsBox = new JComboBox(defs);
		defsBox.setRenderer(new TreeDefListCellRenderer());
		defsBox.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Object selection = defsBox.getSelectedItem();
						if( selection instanceof TreeDefinitionIface )
						{
							TreeDefinitionIface treeDef = (TreeDefinitionIface)defsBox.getSelectedItem();
							initTreeData(treeDef);
						}
					}
				});
		northPanel.add(defsBox,BorderLayout.CENTER);

		statusBar = new JLabel();
		statusBar.setPreferredSize(new Dimension(0,30));

		setupButtonPanel();
		
		southPanel.add(buttonPanel,BorderLayout.NORTH);
		southPanel.add(statusBar,BorderLayout.SOUTH);
	}

	/**
	 * Initialize the tree data (nodes and model) and insert the tree into the main panel
	 * 
	 * @param treeDef the TreeDefinitionIface defining the tree to be viewed/edited
	 */
	protected synchronized void initTreeData(TreeDefinitionIface treeDef)
	{
		Session session = HibernateUtil.getCurrentSession();
		session.lock(treeDef, LockMode.NONE);
		Set<Treeable> treeables = treeDef.getTreeEntries();
		Hibernate.initialize(treeables);
		HibernateUtil.closeSession();
		
		rootNode = null;
		
		Vector<TransferableMutableTreeNode> nodes = new Vector<TransferableMutableTreeNode>();
		for( Treeable t: treeables )
		{
			nodes.add( new TransferableMutableTreeNode(t) );
		}
		
		// setup TransferableMutableTreeNode objects for each userObject
		for (TransferableMutableTreeNode nodeI: nodes)
		{
			for( TransferableMutableTreeNode nodeJ: nodes )
			{
				Treeable treeableJ = (Treeable)nodeJ.getUserObject();
				Treeable treeableI = (Treeable)nodeI.getUserObject();
				if( treeableJ.getParentNode() == treeableI )
				{
					int newChildPos = TreeTableUtils.findIndexOfNewChild(nodeI, nodeJ);
					nodeI.insert(nodeJ, newChildPos);
				}
				if( treeableJ.getParentNode() == null )
				{
					rootNode = nodeJ;
				}
			}
		}
		
		model = new FilteredDefaultTreeModel(rootNode);
		
		if( tree == null )
		{
			tree = new JTree(model);
			initTreeComponent();

			uiComp.remove(selectMessageLabel);
			uiComp.add(new JScrollPane(tree),BorderLayout.CENTER);
			uiComp.repaint();
		}
		else
		{
			tree.setModel(model);
		}
		
		enableAllButtons();
	}
	
	/**
	 * Configure the JTree's drag-and-drop handler, cell renderers,
	 * and UI options.  This should only ever be called once per
	 * TreeTableViewer instance.  To display a different tree,
	 * JTree.setModel(TreeModel) should be called.
	 */
	protected void initTreeComponent()
	{
		tree.setDragEnabled(true);
		
		tree.setScrollsOnExpand(false);
		
		transferHandler = new TreeNodeTransferHandler();
		
		tree.setTransferHandler(transferHandler);
		
		//Map<Integer,Icon> iconMap = getIconMapForClass(treeableClass);
		Map<Integer,Icon> iconMap = new Hashtable<Integer,Icon>();
		mainRenderer = new RankBasedTreeCellRenderer(iconMap);
		Icon defIcon = IconManager.getIcon("Blue Dot", IconManager.IconSize.Std16);
		mainRenderer.setDefaultIcon(defIcon);
		
		//Map<String,Icon> nameIconMap = getNameIconMapForClass(treeableClass);		
		Map<String,Icon> nameIconMap = new Hashtable<String,Icon>();
		subRenderer = new NameBasedTreeCellRenderer(nameIconMap);
		mainRenderer.setSubRendererForRank(subRenderer, 200);
		subRendsEnabled = false;
		mainRenderer.setSubRenderersEnabled(subRendsEnabled);
		tree.setCellRenderer(mainRenderer);
		
		// setup other tree rendering options
		TreeUI treeUI = tree.getUI();
		BasicTreeUI tui = (BasicTreeUI)treeUI;
		tui.setCollapsedIcon(IconManager.getIcon("Forward", IconManager.IconSize.Std8));
		tui.setExpandedIcon(IconManager.getIcon("Down", IconManager.IconSize.Std8));
		tui.setLeftChildIndent(50);
		tree.putClientProperty("JTree.lineStyle", "None");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setupPopupMenu();
		
		MouseAdapter ma = new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				checkForTrigger(e);
			}
			
			public void mousePressed(MouseEvent e)
			{
				checkForTrigger(e);
			}
			
			protected void checkForTrigger(MouseEvent e)
			{
				if( e.isPopupTrigger() )
				{
					popupMenu.show(tree,e.getX(),e.getY());
				}
			}
		};

		tree.addMouseListener(ma);
		tree.addTreeSelectionListener(this);
		
		disableAllButtons();
	}
	
	/**
	 * Build the JPanel to hold the control buttons
	 */
	protected void setupButtonPanel()
	{
		JButton addNode = new JButton("Add child");
		addNode.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						addChildToSelectedNode();
					}
				});
		
		JButton deleteNode = new JButton("Delete node");
		deleteNode.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						deleteSelectedNode();
					}
				});
		
		final JToggleButton displayFlags = new JCheckBox("Display Flags");
		displayFlags.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						enableSubRenderers(displayFlags.isSelected());
					}
				});
		displayFlags.setSelected(subRendsEnabled);
		
		JButton commitButton = new JButton("Commit changes to DB");
		commitButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						commitStructureToDb();
					}
				});
		
		buttonPanel = new JPanel();
				
		buttons.add(displayFlags);
		buttons.add(addNode);
		buttons.add(deleteNode);
		buttons.add(commitButton);
		
		disableAllButtons();
		
		buttonPanel.add(displayFlags);
		buttonPanel.add(addNode);
		buttonPanel.add(deleteNode);
		buttonPanel.add(commitButton);
	}
	
	protected void disableAllButtons()
	{
		for( AbstractButton b: buttons )
		{
			b.setEnabled(false);
		}
	}
	
	protected void enableAllButtons()
	{
		for( AbstractButton b: buttons )
		{
			b.setEnabled(true);
		}
	}

	/**
	 * Creates a new TransferbleMutableTreeNode and inserts it in the tree as a child
	 * of the currently selected node.  If the current selection is empty, this does
	 * nothing.
	 */
	public void addChildToSelectedNode()
	{
		TreePath path = tree.getSelectionPath();
		if( path == null )
		{
			return;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)path.getLastPathComponent();
		Treeable t = (Treeable)parent.getUserObject();
		if( TreeTableUtils.childrenAllowed(t) == false )
		{
			statusBar.setText("Children not allowed for this rank");
			return;
		}
		
		String name = JOptionPane.showInputDialog(tree, "enter a name for the new node");
		
		Integer childRank = TreeTableUtils.getRankOfChildren(t);
		Treeable newChild = TreeFactory.createNewTreeable(t,name);
		if( childRank != null )
		{
			newChild.setRankId(childRank);
		}
		TransferableMutableTreeNode newNode = new TransferableMutableTreeNode(newChild);
		int newChildPos = TreeTableUtils.findIndexOfNewChild(parent, newNode);
		model.insertNodeInto(newNode,parent,newChildPos);
		
		tree.expandPath(new TreePath(model.getPathToRoot(parent)));
	}

	/**
	 * Delete the selected node from the tree.  If nothing is selected, do nothing.
	 */
	public void deleteSelectedNode()
	{
		TreePath path = tree.getSelectionPath();
		if( path == null )
		{
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		model.removeNodeFromParent(node);
		Treeable t = (Treeable)node.getUserObject();
		t.setParentNode(null);
		//removeAllChildrenOfDeletedNode(node);
		deletedNodes.add((Treeable)node.getUserObject());
	}
	
//	protected void removeAllChildrenOfDeletedNode(DefaultMutableTreeNode parent)
//	{
//		while( parent.getChildCount() > 0 )
//		{
//			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getFirstChild();
//			Treeable childT = (Treeable)child.getUserObject();
//
//			childT.setParentNode(null);
//			
//			parent.remove(child);
//			removeAllChildrenOfDeletedNode(child);
//		}
//	}
	
	/**
	 * Enables or disables the sub-renderers that have been registered with the main
	 * renderer
	 * 
	 * @param enable turn on sub-renderers if true, turn off otherwise
	 */
	public void enableSubRenderers( boolean enable )
	{
		mainRenderer.setSubRenderersEnabled(enable);
		tree.repaint();
	}
	
	/**
	 * Commit the current tree structure to the database.  This also reassigns the nodeNumber
	 * and highestChildNodeNumber fields to be consistent with the current tree layout.
	 * 
	 * @return true on success, false on failure
	 */
	public boolean commitStructureToDb()
	{
		// walk the entire model and fixup the parent/child pointers
		// in the Treeable user objects
		
		model.clearAllFilters();
		
		fixTreeables(rootNode);
		
		return updateAllTreeablesInDb();
	}
	
	/**
	 * Traverses the current tree structure as defined by the tree model, reassigning
	 * the parent field in each contained Treeable item.  After that, the nodeNumber
	 * and highestChildNodeNumber fields are updated.
	 * 
	 * @param rootNode the root of the current tree structure
	 */
	protected void fixTreeables( DefaultMutableTreeNode rootNode )
	{
		fixParentPointers(rootNode);
		Treeable rootT = (Treeable)rootNode.getUserObject();
		rootT.setNodeNumber(1);
		fixNodeNumbers(rootNode,2);
	}
	
	/**
	 * Visits each child of the givne parent and assigns the parent field.  This
	 * method recursively calls itself, passing in each child it encounters.
	 * 
	 * @param parent the tree node with which to start the recursive process
	 */
	protected void fixParentPointers( DefaultMutableTreeNode parent )
	{
		Treeable parentT = (Treeable)parent.getUserObject();
		if( parent.isLeaf() )
		{
			return;
		}
		
		for( int i = 0; i < parent.getChildCount(); ++i )
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(i);
			Treeable childT = (Treeable)child.getUserObject();
			childT.setParentNode(parentT);
			fixParentPointers(child);
		}
	}
	
	/**
	 * Walk the subtree starting with <code>parent</code>, numbering each descendant,
	 * staring with <code>nextNodeNumber</code> for the first encountered child.
	 * 
	 * @param parent the node to start the process with
	 * @param nextNodeNumber the node number to assign to the first child found
	 * @return the next available node number
	 */
	protected int fixNodeNumbers( DefaultMutableTreeNode parent, int nextNodeNumber )
	{
		for( int i = 0; i < parent.getChildCount(); ++i )
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(i);
			Treeable childT = (Treeable)child.getUserObject();
			childT.setNodeNumber(nextNodeNumber++);
			if(child.isLeaf())
			{
				childT.setHighestChildNodeNumber(childT.getNodeNumber());
				fixParentHighChildNumbers(child);
			}
			nextNodeNumber = fixNodeNumbers(child,nextNodeNumber);
		}
		return nextNodeNumber;
	}
	
	/**
	 * Walks the path from <code>child</code> up to the tree root, fixing
	 * the highestChildNodeNumber field along the way
	 * 
	 * @param child the node whos ancestors should be fixed
	 */
	protected void fixParentHighChildNumbers( DefaultMutableTreeNode child )
	{
		int nodeNum = ((Treeable)child.getUserObject()).getNodeNumber();

		DefaultMutableTreeNode node = child;
		while( node.getParent() != null )
		{
			node = (DefaultMutableTreeNode)node.getParent();
			Treeable t = (Treeable)node.getUserObject();
			Integer highestSoFar = t.getHighestChildNodeNumber();
			if( highestSoFar == null || highestSoFar < nodeNum )
			{
				t.setHighestChildNodeNumber(nodeNum);
			}
		}
	}
	
	/**
	 * Saves the current tree structure to the database
	 * 
	 * @return true on success, false on failure
	 */
	protected boolean updateAllTreeablesInDb()
	{
		Session session = HibernateUtil.getCurrentSession();
		HibernateUtil.beginTransaction();

		// call saveOrUpdate for all the nodes still in the tree
		Enumeration nodes = rootNode.breadthFirstEnumeration();
		while( nodes.hasMoreElements() )
		{
			session.saveOrUpdate(((DefaultMutableTreeNode)nodes.nextElement()).getUserObject() );
		}
		
		// call delete for all nodes that have been removed from the tree
		for( Treeable t: deletedNodes )
		{
			session.delete(t);
		}

		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
		
		return true;
	}
	
	/**
	 * Setup the right-click popup menu
	 */
	protected void setupPopupMenu()
	{
		popupMenu = new JPopupMenu();
		JMenuItem setAsRoot = new JMenuItem("Set as root");
		JMenuItem hideNode = new JMenuItem("Hide");
		JMenuItem showAll = new JMenuItem("Show all nodes");
		JMenuItem createNewChild = new JMenuItem("New child");
		setAsRoot = popupMenu.add(setAsRoot);
		hideNode = popupMenu.add(hideNode);
		showAll = popupMenu.add(showAll);
		createNewChild = popupMenu.add(createNewChild);
		
		setAsRoot.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						System.out.println("set root");
						SwingUtilities.invokeLater(new Runnable()
								{
									public void run()
									{
										TreeNode node = (TreeNode)tree.getSelectionPath().getLastPathComponent();
										model.setWorkingRoot(node);
									}
								});
					}
				});
		
		hideNode.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						System.out.println("hide node");
						SwingUtilities.invokeLater(new Runnable()
								{
									public void run()
									{
										TreeNode node = (TreeNode)tree.getSelectionPath().getLastPathComponent();
										model.hideNode(node);
									}
								});
					}
				});
		
		showAll.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						System.out.println("show all");
						SwingUtilities.invokeLater(new Runnable()
								{
									public void run()
									{
										model.clearAllFilters();
									}
								});
					}
				});
		
	}

	/**
	 * @return the main, rank-based tree cell renderer
	 */
	public RankBasedTreeCellRenderer getCellRenderer()
	{
		return mainRenderer;
	}
	
	protected Map<Integer,Icon> getIconMapForClass( Class treeableClass )
	{
		// TODO: implement this with some sort of runtime settings thingy
		
		Hashtable<Integer, Icon> iconMap = new Hashtable<Integer, Icon>();
		
		return iconMap;
	}
	
	protected Map<String,Icon> getNameIconMapForClass( Class treeableClass )
	{
		Hashtable<String,Icon> iconMap = new Hashtable<String,Icon>();
		
		File flagDir = new File("C:\\Documents and Settings\\jstewart\\Desktop\\svg flags\\PNG");
		File[] flagFiles = flagDir.listFiles(new FilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						if( name.endsWith(".png") )
						{
								return true;
						}
						return false;
					}
				});
		
		if( flagFiles == null )
		{
			flagFiles = new File[0];
		}
		
		for( File flagFile: flagFiles )
		{
			System.out.println(flagFile);
			String countryName = flagFile.getName().replace('_', ' ');
			countryName = countryName.substring(0,countryName.length()-4);
			countryName = countryName.toLowerCase();
			iconMap.put(countryName, new ImageIcon(flagFile.getAbsolutePath()));
		}

		return iconMap;
	}
	
	public JComponent getUIComponent()
	{
		return uiComp;
	}

	public JTree getTreeComponent()
	{
		return tree;
	}
	
	/**
	 * Updates the status bar text to contain the text representation of the path
	 * from the tree root to the selected node
	 * 
	 * @param e the associated TreeSelectionEvent
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath p = e.getPath();
		if( p == null )
		{
			statusBar.setText(null);
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)p.getLastPathComponent();
		Treeable t = (Treeable)node.getUserObject();
		sb.insert(0, t.getName());
		while(node.getParent() != null )
		{
			node = (DefaultMutableTreeNode)node.getParent();
			t = (Treeable)node.getUserObject();
			sb.insert(0,t.getName()+" : ");
		}
		statusBar.setText(sb.toString());
	}
}
