package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.specify.ui.IconManager;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-based view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 * 
 * @author jstewart
 */
public class TreeTableViewer extends BaseSubPane implements ListSelectionListener
{
	protected JPanel uiComp;

	protected JPanel northPanel;
	protected JPanel southPanel;
	protected JPanel buttonPanel;
	protected Vector<AbstractButton> buttons;
	protected JLabel statusBar;
	protected JLabel messageLabel;
	protected Icon errorIcon;
	
	protected TreeDataListModel listModel;
	protected TreeDataJList list;
	protected TreeDataListCellRenderer listCellRenderer;
	protected TreeDataListHeader listHeader;
	
	protected Vector<Treeable> deletedNodes;
	
	protected JComboBox defsBox;
	
	protected Class treeableClass;
	
    private static final Logger log = Logger.getLogger(TreeTableViewer.class);


	/**
	 * Build a TreeTableViewer to view/edit the data found
	 * 
	 * @param treeDefClass a Class object representing the DB table to find definitions in
	 * @param name a String name for this viewer/editor
	 * @param task the owning Taskable
	 */
	@SuppressWarnings("unchecked")
	public TreeTableViewer( final Class treeDefClass,
							final String name,
							final Taskable task )
	{
		super(name,task);
		Session session = HibernateUtil.getCurrentSession();
		Criteria c = session.createCriteria(treeDefClass);
		List results = c.list();
		
		errorIcon = IconManager.getIcon("Error", IconManager.IconSize.Std24);
		init(results);

		HibernateUtil.closeSession();
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

		messageLabel = new JLabel("Select a tree in the combobox above");
		uiComp.add(messageLabel,BorderLayout.CENTER);
		
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
					
					messageLabel.setText("Please wait while the tree is prepared");
					messageLabel.setIcon(null);
					uiComp.add(messageLabel);
					uiComp.repaint();
					
					initTreeList(treeDef);
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

	protected synchronized void initTreeList( final TreeDefinitionIface treeDef )
	{
		// setup a thread to load the objects from the DB
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				Session session = HibernateUtil.getCurrentSession();
				session.lock(treeDef, LockMode.NONE);
				
				Set treeNodes = treeDef.getTreeEntries();
				if (treeNodes.isEmpty())
				{
					// do the failure callback on the Swing thread
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							initTreeListFailure("Selected tree is empty");
						}
					});
					return;
				}
				
				// grab a node and walk to the root
				Treeable node = (Treeable)treeNodes.iterator().next();
				while( node.getParentNode() != null )
				{
					node = node.getParentNode();
				}
				
				// now 'node' should be the root node of the tree
				final Treeable root = node;
				HibernateUtil.closeSession();

				// do the success callback on the Swing thread
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						Session session = HibernateUtil.getCurrentSession();
						session.lock(root,LockMode.NONE);
						initTreeListSucess(root);
						HibernateUtil.closeSession();
					}
				});
			}
		};
		
		Thread t = new Thread(runnable);
		t.start();
	}
	
	protected synchronized void initTreeListSucess( Treeable root )
	{
		log.debug("Successfully initialized tree editor");

		listModel = new TreeDataListModel(root);
		list = new TreeDataJList(listModel);
		listCellRenderer = new TreeDataListCellRenderer(list,listModel);
		list.setCellRenderer(listCellRenderer);
		list.addListSelectionListener(TreeTableViewer.this);
		listHeader = new TreeDataListHeader(list,listModel);
		
		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				handleMouseEvent(e);
			}
		});

		JPanel treeListPanel = new JPanel(new BorderLayout());
		uiComp.remove(messageLabel);
		treeListPanel.add(new JScrollPane(list), BorderLayout.CENTER);
		treeListPanel.add(listHeader, BorderLayout.NORTH);
		uiComp.add(treeListPanel, BorderLayout.CENTER);
		uiComp.repaint();
		list.repaint();
		treeListPanel.repaint();
		listHeader.repaint();
		
		enableAllButtons();
	}
	
	protected synchronized void initTreeListFailure( String failureMessage )
	{
		log.error("Error while initializing tree editor: " + failureMessage );
		
		messageLabel.setText(failureMessage);
		messageLabel.setIcon(errorIcon);
		uiComp.add(messageLabel);
		uiComp.repaint();
		return;
	}
	
	protected void handleMouseEvent( MouseEvent e )
	{
		int clickCount = e.getClickCount();
		if( clickCount == 2 )
		{
			Treeable t = (Treeable)list.getSelectedValue();
			boolean visible = listModel.childrenAreVisible(t);
			listModel.setChildrenVisible(t, !visible);
			e.consume();
		}
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
						addChildToSelection();
					}
				});
		
		JButton deleteNode = new JButton("Delete node");
		deleteNode.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						deleteSelection();
					}
				});
		
		JButton commitButton = new JButton("Commit changes to DB");
		commitButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						commitStructureToDb();
					}
				});
		
		buttonPanel = new JPanel();
				
		buttons.add(addNode);
		buttons.add(deleteNode);
		buttons.add(commitButton);
		
		disableAllButtons();
		
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

	public void addChildToSelection()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable parent = (Treeable)selection;
		
		// display a form for filling in child data
		Treeable child = showNewTreeableForm(parent);
		listModel.addChild(child, parent);
	}
	
	protected Treeable showNewTreeableForm(Treeable parent)
	{
		Treeable newT = TreeFactory.createNewTreeable(parent, "new treeable w/o a name");
		return newT;
	}
	
	public void deleteSelection()
	{
		
	}
	
	/**
	 * Commit the current tree structure to the database.  This also reassigns the nodeNumber
	 * and highestChildNodeNumber fields to be consistent with the current tree layout.
	 * 
	 * @return true on success, false on failure
	 */
	public boolean commitStructureToDb()
	{
		return false;
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

	public void valueChanged(ListSelectionEvent e)
	{
		Treeable t = (Treeable)list.getSelectedValue();
		if( t == null )
		{
			statusBar.setText(null);
			return;
		}
		
//		StringBuilder sb = new StringBuilder(128);
//		while( t != null )
//		{
//			sb.insert(0, t.getName() + " : ");			
//			t = t.getParentNode();
//		}
//		sb.delete(sb.length()-2, sb.length()-1);
//		statusBar.setText(sb.toString());
		statusBar.setText(TreeTableUtils.getFullName(t));
	}
}
