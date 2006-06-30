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
import java.util.SortedSet;
import java.util.TreeSet;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.treeutils.ReverseRankBasedComparator;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.treetables.TreeNodeEditDialog.TreeNodeDialogCallback;
import edu.ku.brc.util.Pair;

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
	protected JPanel treeListPanel;
	protected List<AbstractButton> buttons;
	protected JLabel statusBar;
	protected JLabel messageLabel;
	protected Icon errorIcon;
	
	protected TreeDataListModel listModel;
	protected TreeDataJList list;
	protected TreeDataListCellRenderer listCellRenderer;
	protected TreeDataListHeader listHeader;
	
	protected JComboBox defsBox;
	protected JButton addNodeButton;
	protected JButton deleteNodeButton;
	protected JButton editButton;
	protected JButton commitTreeButton;
	
	protected Class treeableClass;
	
	protected SortedSet<Treeable> deletedNodes;
	
	protected boolean unsavedChanges;
	
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

		deletedNodes = new TreeSet<Treeable>(new ReverseRankBasedComparator());
		unsavedChanges = false;
		
		HibernateUtil.closeSession();
	}
	
	/**
	 * Initialize all of the UI components in the viewer/editor.
	 * 
	 * @param definitions a list of TreeDefinitionIface objects that can be handled in this TreeTableViewer
	 */
	protected void init( List<TreeDefinitionIface> definitions )
	{
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

		defsBox.setEnabled(false);
		
		listModel = new TreeDataListModel(root);
		list = new TreeDataJList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCellRenderer = new TreeDataListCellRenderer(list,listModel);
		list.setCellRenderer(listCellRenderer);
		list.addListSelectionListener(this);
		listHeader = new TreeDataListHeader(list,listModel);
		
		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				handleMouseEvent(e);
			}
		});

		treeListPanel = new JPanel(new BorderLayout());
		uiComp.remove(messageLabel);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setAutoscrolls(true);
		treeListPanel.add(scroll, BorderLayout.CENTER);
		treeListPanel.add(listHeader, BorderLayout.NORTH);
		uiComp.add(treeListPanel, BorderLayout.CENTER);
		uiComp.repaint();
		list.repaint();
		treeListPanel.repaint();
		listHeader.repaint();
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
			if( t == null )
			{
				return;
			}
			boolean visible = listModel.allChildrenAreVisible(t);
			listModel.setChildrenVisible(t, !visible);
			e.consume();
		}
	}
	
	/**
	 * Build the JPanel to hold the control buttons
	 */
	protected void setupButtonPanel()
	{
		addNodeButton = new JButton("Add child");
		addNodeButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						addChildToSelectedNode();
					}
				});
		
		deleteNodeButton = new JButton("Delete node");
		deleteNodeButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						deleteSelectedNode();
					}
				});
		
		editButton = new JButton("Edit node");
		editButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						editSelectedNode();
					}
				});
		
		commitTreeButton = new JButton("Commit changes to DB");
		commitTreeButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{
						commitStructureToDb();
					}
				});
		
		buttonPanel = new JPanel();
				
		buttons.add(addNodeButton);
		buttons.add(deleteNodeButton);
		buttons.add(editButton);
		buttons.add(commitTreeButton);
		
		disableAllButtons();
		commitTreeButton.setEnabled(true);
		
		buttonPanel.add(addNodeButton);
		buttonPanel.add(deleteNodeButton);
		buttonPanel.add(editButton);
		buttonPanel.add(commitTreeButton);
	}
	
	protected void disableAllButtons()
	{
		for( AbstractButton b: buttons )
		{
			b.setEnabled(false);
		}

		commitTreeButton.setEnabled(unsavedChanges);
	}
	
	protected void enableAllButtons()
	{
		for( AbstractButton b: buttons )
		{
			b.setEnabled(true);
		}
		
		commitTreeButton.setEnabled(unsavedChanges);
	}

	public void addChildToSelectedNode()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable parent = (Treeable)selection;
		TreeDefinitionItemIface parentDefItem = parent.getDefItem();
		if( parentDefItem.getChildItem() == null )
		{
			log.info("Cannot add child node below this rank");
			return;
		}

		Treeable newT = TreeFactory.createNewTreeable(parent, "New Node");

		// display a form for filling in child data
		showNewTreeableForm(newT);
	}
	
	protected void showNewTreeableForm(Treeable newNode)
	{
		TreeNodeDialogCallback callback = new TreeNodeDialogCallback()
		{
			public void editCompleted(Treeable node)
			{
				newNodeEntryComplete(node);
			}
			public void editCancelled(Treeable node)
			{
				newNodeEntryCancelled(node);
			}
		};

		showEditDialog(newNode, "New Node Form", callback);
	}
	
	public void newNodeEntryComplete(Treeable node)
	{
		listModel.hideChildren(node.getParentNode());
		node.getParentNode().addChild(node);
		listModel.showChildren(node.getParentNode());
		
		TreeTableUtils.setTimestampsToNow(node);
		String fullname = TreeTableUtils.getFullName(node);
		node.setFullName(fullname);
		
		unsavedChanges = true;
		commitTreeButton.setEnabled(true);
	}
	
	public void newNodeEntryCancelled(Treeable node)
	{
		Treeable parent = node.getParentNode();
		boolean showing = listModel.allChildrenAreVisible(parent);
		listModel.hideChildren(parent);
		if( node == null )
		{
			return;
		}
		
		if( parent != null )
		{
			parent.removeChild(node);
		}
		
		TreeDefinitionIface def = node.getTreeDef();
		if( def != null )
		{
			def.getTreeDefItems().remove(node);
		}
		
		TreeDefinitionItemIface defItem = node.getDefItem();
		if( defItem != null )
		{
			defItem.getTreeEntries().remove(node);
		}
		
		listModel.setChildrenVisible(parent, showing);
	}
	
	public void deleteSelectedNode()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable node = (Treeable)selection;
		if( TreeTableUtils.canBeDeleted(node) )
		{
			Treeable parent = node.getParentNode();
			listModel.hideChildren(parent);
			parent.removeChild(node);
			listModel.showChildren(parent);
			deletedNodes.add(node);
			deleteAllDescendants(node);
			log.info("Deleted node");
		}
		else
		{
			log.info("Selected node cannot be deleted");
		}
	}
	
	protected void deleteAllDescendants(Treeable parent)
	{
		List<Treeable> descendants = TreeTableUtils.getAllDescendants(parent);
		for( Treeable node: descendants )
		{
			Treeable p = node.getParentNode();
			if( p != null )
			{
				p.removeChild(node);
			}
			deletedNodes.add(node);
		}
	}
	
	protected void editSelectedNode()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable node = (Treeable)selection;
		
		TreeNodeDialogCallback callback = new TreeNodeDialogCallback()
		{
			public void editCompleted(Treeable node)
			{
				editSelectedNodeOK(node);
			}
			public void editCancelled(Treeable node)
			{
				editSelectedNodeCancelled(node);
			}
		};

		showEditDialog(node, "Edit Node Values", callback);
	}
	
	protected void editSelectedNodeOK(Treeable node)
	{
		log.info("User selected 'OK' from edit node dialog");
		unsavedChanges = true;
		commitTreeButton.setEnabled(true);
		listModel.nodeValuesChanged(node);
	}
	
	protected void editSelectedNodeCancelled(Treeable node)
	{
		log.info("User selected 'Cancel' from edit node dialog");
	}
	
	/**
	 * Commit the current tree structure to the database.  This also reassigns the nodeNumber
	 * and highestChildNodeNumber fields to be consistent with the current tree layout.
	 * 
	 * @return true on success, false on failure
	 */
	public boolean commitStructureToDb()
	{
		// XXX: for testing
		// block all but the test taxon tree from saving
		TreeDefinitionIface treeDef = listModel.getRoot().getTreeDef();
		int treeDefId = treeDef.getTreeDefId().intValue();
		if( !treeDef.getClass().equals(TaxonTreeDef.class) || (treeDefId != 3 && treeDefId != 4) )
		{
			log.info("Currently only allowing commits to DB from the test taxon tree");
			return false;
		}
		
		Treeable root = listModel.getRoot();
		root.setNodeNumber(1);
		TreeTableUtils.fixNodeNumbersFromRoot(root);
		
		TreeTableUtils.saveTreeStructure(root,deletedNodes);
		unsavedChanges = false;
		commitTreeButton.setEnabled(false);
		return true;
	}
	
	protected void showEditDialog(Treeable node,String title,TreeNodeDialogCallback callback)
	{
		String shortClassName = node.getClass().getName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(node);
		TreeNodeEditDialog editDialog = new TreeNodeEditDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setData(node);
		editDialog.setVisible(true);
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
			deleteNodeButton.setEnabled(false);
			editButton.setEnabled(false);
			addNodeButton.setEnabled(false);
			return;
		}
		
		statusBar.setText(TreeTableUtils.getFullName(t));
		deleteNodeButton.setEnabled(true);
		editButton.setEnabled(true);
		addNodeButton.setEnabled(true);
	}
}
