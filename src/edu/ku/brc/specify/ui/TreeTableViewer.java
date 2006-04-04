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
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.hibernate.Session;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.TreeFactory;
import edu.ku.brc.specify.helpers.TreeTableUtils;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.ui.dnd.TransferableMutableTreeNode;
import edu.ku.brc.specify.ui.dnd.TreeNodeTransferHandler;

public class TreeTableViewer extends BaseSubPane implements TreeSelectionListener
{
	protected JPanel uiComp;

	protected JPanel southPanel;
	protected JPanel buttonPanel;
	protected JLabel statusBar;
	
	protected JTree tree;
	protected FilteredDefaultTreeModel model;
	protected DefaultMutableTreeNode rootNode;
	protected RankBasedTreeCellRenderer mainRenderer;
	protected NameBasedTreeCellRenderer subRenderer;
	protected boolean subRendsEnabled;
	protected JPopupMenu popupMenu;

	protected Class treeableClass;

	public TreeTableViewer( final Class treeableClass,
							final String name,
							final Taskable task )
	{
		super(name,task);
		Session session = HibernateUtil.getCurrentSession();
		Criteria c = session.createCriteria(treeableClass);
		List results = c.list();
		HibernateUtil.closeSession();
		
		init(results);
	}
	
	public TreeTableViewer( final List<Treeable> treeables,
							final String name,
							final Taskable task )
	{
		super(name,task);
		init(treeables);
	}
	
	protected void init( List<Treeable> treeables )
	{
		this.uiComp = new JPanel();
		
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
		tree = new JTree(model);
		
		tree.setDragEnabled(true);
		tree.setTransferHandler(new TreeNodeTransferHandler());
		
		Map<Integer,Icon> iconMap = getIconMapForClass(treeableClass);
		mainRenderer = new RankBasedTreeCellRenderer(iconMap);
		Icon defIcon = IconManager.getIcon("Blue Dot", IconManager.IconSize.Std16);
		mainRenderer.setDefaultIcon(defIcon);
		
		Map<String,Icon> nameIconMap = getNameIconMapForClass(treeableClass);		
		subRenderer = new NameBasedTreeCellRenderer(nameIconMap);
		mainRenderer.setSubRendererForRank(subRenderer, 200);
		subRendsEnabled = false;
		mainRenderer.setSubRenderersEnabled(subRendsEnabled);
		tree.setCellRenderer(mainRenderer);
		
//		tree.setCellEditor(new DefaultTreeCellEditor(tree,new DefaultTreeCellRenderer()));
//		tree.setEditable(true);
		
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
				if( e.isPopupTrigger() )
				{
					popupMenu.show(tree,e.getX(),e.getY());
				}
			}
		};

		tree.addMouseListener(ma);
		tree.addTreeSelectionListener(this);
		
		uiComp.setLayout(new BorderLayout());
		uiComp.add(new JScrollPane(tree),BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		uiComp.add(southPanel,BorderLayout.SOUTH);

		statusBar = new JLabel();
		statusBar.setPreferredSize(new Dimension(0,30));

		setupButtonPanel();
		
		southPanel.add(buttonPanel,BorderLayout.NORTH);
		southPanel.add(statusBar,BorderLayout.SOUTH);
	}
	
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
						boolean success = commitStructureToDb();
					}
				});
		
		buttonPanel = new JPanel();
		
		buttonPanel.add(displayFlags);
		buttonPanel.add(addNode);
		buttonPanel.add(deleteNode);
		buttonPanel.add(commitButton);
	}
	
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
	
	public void deleteSelectedNode()
	{
		TreePath path = tree.getSelectionPath();
		if( path == null )
		{
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		model.removeNodeFromParent(node);
	}
	
	public void enableSubRenderers( boolean enable )
	{
		mainRenderer.setSubRenderersEnabled(enable);
		tree.repaint();
	}
	
	public boolean commitStructureToDb()
	{
		// walk the entire model and fixup the parent/child pointers
		// in the Treeable user objects
		
		model.clearAllFilters();
		
		fixTreeables(rootNode);
		
		return updateAllTreeablesInDb(rootNode);
	}
	
	protected void fixTreeables( DefaultMutableTreeNode rootNode )
	{
		fixParentPointers(rootNode);
		Treeable rootT = (Treeable)rootNode.getUserObject();
		rootT.setNodeNumber(1);
		fixNodeNumbers(rootNode,2);
	}
	
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
	
	protected int fixNodeNumbers( DefaultMutableTreeNode parent, int nextNodeNumber )
	{
		Treeable parentT = (Treeable)parent.getUserObject();
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
	
	protected boolean updateAllTreeablesInDb( DefaultMutableTreeNode root )
	{
		Enumeration nodes = root.breadthFirstEnumeration();
		while( nodes.hasMoreElements() )
		{
			Session session = HibernateUtil.getCurrentSession();
			HibernateUtil.beginTransaction();
			session.saveOrUpdate(((DefaultMutableTreeNode)nodes.nextElement()).getUserObject() );
			HibernateUtil.commitTransaction();
			HibernateUtil.closeSession();
		}
		
		return true;
	}
	
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
	
	protected Map<Integer,Icon> getIconMapForClass( Class treeableClass )
	{
		// TODO: implement this with some sort of runtime settings thingy
		
		
		// setup custom tree cell renderer
		ImageIcon earthIcon = new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\earth32.png");
		ImageIcon contIcon = new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\cont.jpg");
		ImageIcon countryIcon = new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\country.gif");
		ImageIcon stateIcon = new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\state.jpg");

		Hashtable<Integer, Icon> iconMap = new Hashtable<Integer, Icon>();
		iconMap.put(0, earthIcon);
		//iconMap.put(100, contIcon);
		//iconMap.put(200, countryIcon);
		//iconMap.put(300, stateIcon);

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
	
	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath p = tree.getSelectionPath();
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
