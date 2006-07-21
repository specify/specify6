package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
//import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.treeutils.ReverseRankBasedComparator;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
//import edu.ku.brc.specify.ui.UICacheManager;
//import edu.ku.brc.specify.ui.dnd.GhostGlassPane;
import edu.ku.brc.specify.ui.treetables.TreeNodeEditDialog.TreeNodeDialogCallback;
import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.TreeDataJList;
import edu.ku.brc.ui.listeners.ScrollBarLinkingListener;
import edu.ku.brc.ui.renderers.NameBasedListCellRenderer;
import edu.ku.brc.util.Pair;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-based view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 *
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeTableViewer extends BaseSubPane implements ListSelectionListener, DragDropCallback
{
	/** North section container. */
	protected JPanel northPanel;
	/** South section container. */
	protected JPanel southPanel;
	/** Button container. */
	protected JPanel buttonPanel;
	/** Tree widget container. */
	protected JPanel treeListPanel;
	/** Collection of all the buttons on the widget. */
	protected List<AbstractButton> selectionSensativeButtons;
	/** Status message display widget. */
	protected JLabel statusBar;
	/** Info message display widget. */
	protected JLabel messageLabel;
	/** An icon signifying that an error has occurred. */
	protected Icon errorIcon;
	
	/** Model holding all <code>Treeable</code> nodes. */
	protected TreeDataListModel listModel;
	/** The tree display widget. */
	protected TreeDataJList list;
	/** Cell renderer for displaying individual nodes in the tree. */
	protected TreeDataListCellRenderer listCellRenderer;
	/** A header for the tree, displaying the names of the visible levels. */
	protected TreeDataListHeader listHeader;
	
	/** Tree selection widget. */
	protected JComboBox defsBox;
	/** Button for adding new nodes. */
	protected JButton addNodeButton;
	/** Button for deleting nodes. */
	protected JButton deleteNodeButton;
	/** Button for editing nodes. */
	protected JButton editButton;
	/** Button for committing current tree structure to disk. */
	protected JButton commitTreeButton;
	
	protected JButton viewSubtreeButton;
	protected JButton showWholeTreeButton;
	protected JButton showAllDescendantsButton;
	
	/** Implementation class of <code>Treeable</code> nodes. */
	protected Class treeableClass;
	
	/** Collection of all nodes deleted by user that have not yet been deleted from persistent store (DB). */
	protected SortedSet<Treeable> deletedNodes;
	
//	protected boolean busy;
//	protected BusyComponentGlassPane glassPane;
	
    /** Logger for all messages emitted. */
    private static final Logger log = Logger.getLogger(TreeTableViewer.class);

	/**
	 * Build a TreeTableViewer to view/edit the data found.
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
		
//		busy = false;
//		glassPane = new BusyComponentGlassPane(this,viewSubtreeButton);
//		JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
//		topFrame.setGlassPane(glassPane);

		HibernateUtil.closeSession();
	}
	
	/**
	 * Initialize all of the UI components in the viewer/editor.
	 * 
	 * @param definitions a list of TreeDefinitionIface objects that can be handled in this TreeTableViewer
	 */
	protected void init( List<TreeDefinitionIface> definitions )
	{
		this.setLayout(new BorderLayout());

		messageLabel = new JLabel("Select a tree in the combobox above");
		this.add(messageLabel,BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		this.add(southPanel,BorderLayout.SOUTH);

		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		this.add(northPanel,BorderLayout.NORTH);
		
		Vector<Object> defs = new Vector<Object>(definitions);
		defs.add(0, "Choose a tree definition");
		defsBox = new JComboBox(defs);
		defsBox.setRenderer(new NameBasedListCellRenderer());
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
					add(messageLabel);
					repaint();
					
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

	/**
	 * Initialize the tree display component with the tree defined by the given
	 * {@link TreeDefinitionIface}.
	 *
	 * @param treeDef the tree definition
	 */
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
	
	/**
	 * Performs finalization work after {@link #initTreeList(TreeDefinitionIface)}
	 * successfully initializes a new tree display.
	 *
	 * @param root the root node of the new tree
	 */
	protected synchronized void initTreeListSucess( Treeable root )
	{
		log.debug("Successfully initialized tree editor");

		defsBox.setEnabled(false);
		
		listModel = new TreeDataListModel(root);
		list = new TreeDataJList(listModel,this);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCellRenderer = new TreeDataListCellRenderer(list,listModel);
		list.setCellRenderer(listCellRenderer);
		list.addListSelectionListener(this);
		listHeader = new TreeDataListHeader(list,listModel,listCellRenderer);
		
		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				handleMouseEvent(e);
			}
		});

		treeListPanel = new JPanel(new BorderLayout());
		JScrollPane bodyScroll = new JScrollPane(list);
		bodyScroll.setAutoscrolls(true);
		treeListPanel.add(bodyScroll, BorderLayout.CENTER);
		
		JScrollPane headerScroll = new JScrollPane(listHeader);
		headerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		headerScroll.setAutoscrolls(true);
		
		ScrollBarLinkingListener linkingListener = new ScrollBarLinkingListener();
		linkingListener.addScrollBar(bodyScroll.getHorizontalScrollBar());
		linkingListener.addScrollBar(headerScroll.getHorizontalScrollBar());
		
		treeListPanel.add(headerScroll, BorderLayout.NORTH);

		this.remove(messageLabel);
		this.add(treeListPanel, BorderLayout.CENTER);
		this.repaint();
		
		list.repaint();
		treeListPanel.repaint();
		listHeader.repaint();
	}
	
	/**
	 * Performs finalization work after {@link #initTreeList(TreeDefinitionIface)}
	 * fails to initialize a new tree display.
	 *
	 * @param failureMessage a string containing info about the reason for failure
	 */
	protected synchronized void initTreeListFailure( String failureMessage )
	{
		log.error("Error while initializing tree editor: " + failureMessage );
		
		messageLabel.setText(failureMessage);
		messageLabel.setIcon(errorIcon);
		this.add(messageLabel);
		this.repaint();
		return;
	}
	
	/**
	 * Performs all duties related to processing mouse events on the
	 * tree display.
	 *
	 * @param e a mouse event
	 */
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
	 * Builds the button panel and all contained buttons.
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

		viewSubtreeButton = new JButton("Subtree");
		viewSubtreeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showSubtreeOfSelection();
				}
			});

		showWholeTreeButton = new JButton("View Whole Tree");
		showWholeTreeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showWholeTree();
				}
			});
		
		showAllDescendantsButton = new JButton("Show All Descendants");
		showAllDescendantsButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					expandAllDescendantsOfSelection();
				}
			});
		
		selectionSensativeButtons = new Vector<AbstractButton>();
		selectionSensativeButtons.add(addNodeButton);
		selectionSensativeButtons.add(deleteNodeButton);
		selectionSensativeButtons.add(editButton);
		selectionSensativeButtons.add(viewSubtreeButton);
		selectionSensativeButtons.add(showAllDescendantsButton);
		
		disableAllButtons();
		commitTreeButton.setEnabled(true);
		showWholeTreeButton.setEnabled(true);
		
		buttonPanel = new JPanel();
		buttonPanel.add(addNodeButton);
		buttonPanel.add(deleteNodeButton);
		buttonPanel.add(editButton);
		buttonPanel.add(commitTreeButton);
		buttonPanel.add(viewSubtreeButton);
		buttonPanel.add(showWholeTreeButton);
		buttonPanel.add(showAllDescendantsButton);
	}
	
	/**
	 * Disables all selection sensative buttons in the button panel.
	 */
	protected void disableAllButtons()
	{
		for( AbstractButton b: selectionSensativeButtons )
		{
			b.setEnabled(false);
		}
	}
	

	/**
	 * Enables all selection sensative buttons in the button panel.
	 */
	protected void enableAllButtons()
	{
		for( AbstractButton b: selectionSensativeButtons )
		{
			b.setEnabled(true);
		}
		
		showWholeTreeButton.setEnabled(true);
	}


	/**
	 * Displays data entry form for creating a new node that will be
	 * a child of the selected node.  A new child is only permanently
	 * created if the user chooses to procede with the data entry.
	 */
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
	

	/**
	 * Display the data entry form for creating a new node.
	 *
	 * @param newNode the new node for which the user must enter data
	 */
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
	

	/**
	 * Peforms finalization of new node creation.  This method
	 * serves as a callback to the data entry form for when
	 * a user presses the 'OK' button on the form.  The new node
	 * is only made persistent if the user then selects to commit
	 * the tree structure to the DB.
	 *
	 * @param node the new node
	 */
	public void newNodeEntryComplete(Treeable node)
	{
		listModel.hideChildren(node.getParentNode());
		node.getParentNode().addChild(node);
		listModel.showChildren(node.getParentNode());
		
		TreeTableUtils.setTimestampsToNow(node);
		String fullname = TreeTableUtils.getFullName(node);
		node.setFullName(fullname);
		
		commitTreeButton.setEnabled(true);
	}
	

	/**
	 * Cancels new node creation process.  This method serves
	 * as a callback to the data entry form for when the user
	 * presses the 'Cancel' button on the form.
	 *
	 * @param node the new node that was in the process of being created
	 */
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
	

	/**
	 * Deletes the currently selected node and all descendants if and
	 * only if it is determined possible without violating any business
	 * rules.
	 */
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
	
	
	/**
	 * Deletes all descendants of the given node.  Deletion is only persisted
	 * if the user then chooses to commit the modified tree structure to the DB.
	 *
	 * @param parent the node for which to delete all descendant nodes
	 */
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
	

	/**
	 * Display a form for editing the data in the currently selected node.
	 */
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
	

	/**
	 * Performs finalization of node data editing process.  This
	 * method also signals the tree display widget to update its
	 * view.
	 *
	 * @param node the node being edited
	 */
	protected void editSelectedNodeOK(Treeable node)
	{
		log.info("User selected 'OK' from edit node dialog");
		commitTreeButton.setEnabled(true);
		listModel.nodeValuesChanged(node);
	}
	

	/**
	 * Performs cleanup tasks after the user cancels a node data
	 * editing process.
	 *
	 * @param node the node that was being edited
	 */
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
		commitTreeButton.setEnabled(false);
		return true;
	}
	
	/**
	 * Sets the visibleRoot property of the tree to the currently selected node.  This provides
	 * the ability to "zoom in" to a lower level of the tree.
	 */
	public void showSubtreeOfSelection()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable node = (Treeable)selection;

		listModel.setVisibleRoot(node);
		
		list.setSelectedValue(node,true);
		
//		glassPane.setBusy(!glassPane.isBusy());
	}
	
	/**
	 * Sets the visibleRoot property to the actual root of the tree.  This results in the 
	 * entire tree being made available to the user.
	 */
	public void showWholeTree()
	{
		Object selection = list.getSelectedValue();		

		listModel.setVisibleRoot(listModel.getRoot());
		
		if( selection != null )
		{
			list.setSelectedValue(selection,true);
		}
	}

	/** 
	 * Expands all of the nodes below the currently selected node.
	 */
	public void expandAllDescendantsOfSelection()
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable node = (Treeable)selection;
		listModel.showDescendants(node);
	}
	
	/**
	 * Display the form for editing node data.
	 *
	 * @param node the node being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showEditDialog(Treeable node,String title,TreeNodeDialogCallback callback)
	{
		String shortClassName = node.getClass().getName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(node);
		TreeNodeEditDialog editDialog = new TreeNodeEditDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setData(node);
		editDialog.setVisible(true);
	}
	
	

	/**
	 * Returns the top-level UI component of the tree viewer.
	 *
	 * @see edu.ku.brc.specify.tasks.subpane.BaseSubPane#getUIComponent()
	 * @return the top-level UI component
	 */
	public JComponent getUIComponent()
	{
		return this;
	}


	/**
	 * Updates the status bar text to display the full name of the currently
	 * selected nodes and updates the enabled/disabled status of the buttons.
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 * @param e a selection event on the tree display
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		Treeable t = (Treeable)list.getSelectedValue();
		if( t == null )
		{
			statusBar.setText(null);
			disableAllButtons();
			return;
		}
		
		statusBar.setText(TreeTableUtils.getFullName(t));
		enableAllButtons();
	}

	/**
	 * Reparents <code>dragged</code> to <code>droppedOn</code> by calling
	 * {@link TreeDataListModel#reparent(Treeable, Treeable)}.
	 *
	 * @see edu.ku.brc.ui.DragDropCallback#dropOccurred(java.lang.Object, java.lang.Object)
	 * @param dragged the dragged tree node
	 * @param droppedOn the node the dragged node was dropped onto
	 */
	public void dropOccurred( Object dragged, Object droppedOn )
	{
		if( !(dragged instanceof Treeable && droppedOn instanceof Treeable) )
		{
			log.warn("Ignoring drag and drop of unhandled types of objects");
			return;
		}
		
		Treeable child = (Treeable)dragged;
		Treeable newParent = (Treeable)droppedOn;
		
		if( !TreeTableUtils.canChildBeReparentedToNode(child,newParent) )
		{
			log.info("Cannot reparent " + child.getName() + " to " + newParent.getName());
			return;
		}
		
		boolean changed = listModel.reparent(child,newParent);
		if( changed )
		{
			commitTreeButton.setEnabled(true);
		}
	}
}
