/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.TreeNodeEditDialog.TreeNodeDialogCallback;
import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.listeners.ScrollBarLinkingListener;
import edu.ku.brc.ui.renderers.NameBasedListCellRenderer;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.ReverseRankBasedComparator;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-based view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 
 * @code_status Unknown (auto-generated)
 **
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeTableViewer extends BaseSubPane implements ListSelectionListener, DragDropCallback, MouseListener
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
	protected TreeDataGhostDropJList list;
	/** Cell renderer for displaying individual nodes in the tree. */
	protected TreeDataListCellRenderer listCellRenderer;
	/** A header for the tree, displaying the names of the visible levels. */
	protected TreeDataListHeader listHeader;
	
	protected TreeDefinitionIface displayedTreeDef;
	
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
    
    protected TreeDataService dataService;
    
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
		
		dataService = TreeDataServiceFactory.createService();
		List<TreeDefinitionIface> defs = dataService.getAllTreeDefs(treeDefClass);
		
		errorIcon = IconManager.getIcon("Error", IconManager.IconSize.Std24);
		init(defs);

		deletedNodes = new TreeSet<Treeable>(new ReverseRankBasedComparator());
		
//		busy = false;
//		glassPane = new BusyComponentGlassPane(this,viewSubtreeButton);
//		JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
//		topFrame.setGlassPane(glassPane);
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
					displayedTreeDef = treeDef;
					
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
				final Treeable root = dataService.getRootNode(treeDef);
				
				if(root == null)
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
				
				// do the success callback on the Swing thread
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						initTreeListSucess(root);
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
		//list = new GhostDropJList(listModel,this);
		list = new TreeDataGhostDropJList(listModel,this);
		list.addMouseListener(this);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCellRenderer = new TreeDataListCellRenderer(list,listModel);
		list.setCellRenderer(listCellRenderer);
		list.addListSelectionListener(this);
		listHeader = new TreeDataListHeader(list,listModel,listCellRenderer);

		treeListPanel = buildTreeListPanel(list,listHeader);

		TreeDataGhostDropJList list2 = new TreeDataGhostDropJList(listModel,this);
		list2.addMouseListener(this);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TreeDataListCellRenderer rend2 = new TreeDataListCellRenderer(list2,listModel);
		list2.setCellRenderer(rend2);
		list.addListSelectionListener(this);
		TreeDataListHeader head2 = new TreeDataListHeader(list2,listModel,rend2);
		
		JPanel treeListPanel2 = buildTreeListPanel(list2,head2);
			
		
		this.remove(messageLabel);
//		this.add(treeListPanel, BorderLayout.CENTER);
		this.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,treeListPanel,treeListPanel2), BorderLayout.CENTER);
		this.repaint();
		
		list.repaint();
		treeListPanel.repaint();
		listHeader.repaint();
	}
	
	protected JPanel buildTreeListPanel(TreeDataGhostDropJList list,TreeDataListHeader header)
	{
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane bodyScroll = new JScrollPane(list);
		bodyScroll.setAutoscrolls(true);
		panel.add(bodyScroll, BorderLayout.CENTER);
		
		JScrollPane headerScroll = new JScrollPane(listHeader);
		headerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		headerScroll.setAutoscrolls(true);
		
		ScrollBarLinkingListener linkingListener = new ScrollBarLinkingListener();
		linkingListener.addScrollBar(bodyScroll.getHorizontalScrollBar());
		linkingListener.addScrollBar(headerScroll.getHorizontalScrollBar());
		
		panel.add(headerScroll, BorderLayout.NORTH);

		return panel;
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

		Treeable newT = TreeFactory.createNewTreeable(parent.getClass(),"New Node");

		// only set the parent pointer to point 'upstream'
		// if it also points 'downstream' from the parent,
		// the renderer will try to render the new node immediately
		newT.setParentNode(parent);
		newT.setTreeDef(parent.getTreeDef());

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
			public void editCompleted(Treeable node, boolean nameChanged)
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
	@SuppressWarnings("unchecked")
	public void newNodeEntryComplete(Treeable node)
	{
		// set the 'downstream' pointers from the parent and parent def items
		
		listModel.hideChildren(node.getParentNode());
		node.getParentNode().addChild(node);
		node.getDefItem().getTreeEntries().add(node);
		
		node.setTimestampsToNow();
		String fullname = node.getFullName();
		node.setFullName(fullname);
		
		commitTreeButton.setEnabled(true);

		listModel.showChildren(node.getParentNode());
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
		if( node == null )
		{
			return;
		}

		node.setParentNode(null);
		
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
		if( node.canBeDeleted() )
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
		List<Treeable> descendants = parent.getAllDescendants();
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
		
		final Treeable node = (Treeable)selection;
		
		TreeNodeDialogCallback callback = new TreeNodeDialogCallback()
		{
			public void editCompleted(Treeable node, boolean nameChanged)
			{
				editSelectedNodeOK(node,nameChanged);
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
	protected void editSelectedNodeOK(Treeable node, boolean nameChanged)
	{
		log.info("User selected 'OK' from edit node dialog: ");
		
        if( nameChanged )
        {
        	node.fixFullNameForAllDescendants();
        }

        node.updateModifiedTimeAndUser();
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
		dataService.saveTree(root,deletedNodes);
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
		
		// test code
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
		editDialog.setModal(true);
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
		
		System.out.println("Selection changed: " + t.getName());
		statusBar.setText(t.getFullName());
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
		
		if( displayedTreeDef.canChildBeReparentedToNode(child,newParent) )
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

	/**
	 *
	 *
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 * @param e
	 */
	public void mouseClicked(MouseEvent e)
	{
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if(index==-1)
		{
			return;
		}
		Treeable t = (Treeable)listModel.getElementAt(index);
		Integer rank = t.getRankId();
		Pair<Integer,Integer> anchorBounds = listCellRenderer.getAnchorBoundsForRank(rank);
		Pair<Integer,Integer> textBounds = listCellRenderer.getTextBoundsForRank(rank);
		
		if( anchorBounds.first < p.x && p.x < anchorBounds.second )
		{
			// mouse press is on anchor area
			boolean visible = listModel.allChildrenAreVisible(t);
			listModel.setChildrenVisible(t, !visible);
		}
		else if( textBounds.first < p.x && p.x < textBounds.second )
		{
			// mouse press is on text area
			if( e.getClickCount() == 2 )
			{
				boolean visible = listModel.allChildrenAreVisible(t);
				listModel.setChildrenVisible(t, !visible);
			}
		}
		else
		{
			e.consume();
		}
	}

	/**
	 *
	 *
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * @param e
	 */
	public void mouseEntered(MouseEvent e)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 * @param e
	 */
	public void mouseExited(MouseEvent e)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 * @param e
	 */
	public void mousePressed(MouseEvent e)
	{
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if(index==-1)
		{
			return;
		}
		Treeable t = (Treeable)listModel.getElementAt(index);
		Integer rank = t.getRankId();
		Pair<Integer,Integer> textBounds = listCellRenderer.getTextBoundsForRank(rank);
		
		if( textBounds.first < p.x && p.x < textBounds.second )
		{
			this.list.setClickOnText(true);
			// mouse press is on text area
		}
		else
		{
			this.list.setClickOnText(false);
		}
	}

	/**
	 *
	 *
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 * @param e
	 */
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void showingPane(boolean show)
	{
		super.showingPane(show);
		if(!show)
		{
			dataService.fini();
		}
	}
}
