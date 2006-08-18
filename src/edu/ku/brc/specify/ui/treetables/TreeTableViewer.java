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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.DualViewSearchable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.EditFormDialog.EditDialogCallback;
import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UICacheManager;
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
public class TreeTableViewer extends BaseSubPane implements DragDropCallback, DualViewSearchable
{
	private static final int SINGLE_VIEW_MODE = 0;
	private static final int DUAL_VIEW_MODE = 1;
	
	/** Status message display widget. */
	protected JStatusBar statusBar;
	
	/** Model holding all <code>Treeable</code> nodes. */
	protected TreeDataListModel listModel;
	/** The tree display widget. */
	protected TreeDataGhostDropJList[] lists;
	/** Cell renderer for displaying individual nodes in the tree. */
	protected TreeDataListCellRenderer listCellRenderer;
	/** A header for the tree, displaying the names of the visible levels. */
	protected TreeDataListHeader[] listHeaders;
	
	protected JPanel[] treeListPanels;
	
	protected TreeDefinitionIface treeDef;
	
	/** Collection of all nodes deleted by user that have not yet been deleted from the DB. */
	protected SortedSet<Treeable> deletedNodes;
	
	protected String nameBeforeEditDialogShown;
	
    /** Logger for all messages emitted. */
    private static final Logger log = Logger.getLogger(TreeTableViewer.class);
    
    protected TreeDataService dataService;
    
    protected String findName;
    protected int resultsIndex;
    protected List<Treeable> findResults;
    
    protected boolean isInitialized;
    protected int mode;
    
	/**
	 * Build a TreeTableViewer to view/edit the data found.
	 * 
	 * @param treeDef handle to the tree to be displayed
	 * @param name a String name for this viewer/editor
	 * @param task the owning Taskable
	 */
	public TreeTableViewer( final TreeDefinitionIface treeDef,
							final String name,
							final Taskable task )
	{
		super(name,task);
		this.treeDef = treeDef;
		dataService = TreeDataServiceFactory.createService();
		deletedNodes = new TreeSet<Treeable>(new ReverseRankBasedComparator());
		statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
		
		removeAll();
		setLayout(new BorderLayout());
	}
	
	@Override
	public void showingPane(boolean show)
	{
		super.showingPane(show);
		if(show)
		{
			synchronized(this)
			{
				if(!isInitialized)
				{
					isInitialized=true;
					initTreeLists();
				}
			}
		}
	}

	/**
	 * Initialize the tree display component with the tree defined by the given
	 * {@link TreeDefinitionIface}.
	 *
	 * @param treeDef the tree definition
	 */
	protected void initTreeLists()
	{
		// setup a thread to load the objects from the DB
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				final Treeable root = dataService.getRootNode(treeDef);
				
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						showTree(root);
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
	protected void showTree( Treeable root )
	{
		if(root==null)
		{
			String error = "Error while initializing tree editor: No root node found";
			log.error(error);
			statusBar.setText(error);
			SubPaneMgr.getInstance().closeCurrent();
			return;
		}
		
		log.debug("Successfully initialized tree editor");

		MouseListener mouseListener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e){mouseButtonClicked(e);}
			public void mousePressed(MouseEvent e){mouseButtonPressed(e);}
		};
		listModel = new TreeDataListModel(root);
		Color[] bgs = new Color[2];
		bgs[0] = new Color(202,238,255);
		bgs[1] = new Color(151,221,255);
		listCellRenderer = new TreeDataListCellRenderer(listModel,bgs);
		ListSelectionListener listSelListener = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				newTreeableSelected(e);
			}
		};

		// setup both views
		lists = new TreeDataGhostDropJList[2];
		listHeaders = new TreeDataListHeader[2];
		treeListPanels = new JPanel[2];
		
		lists[0] = new TreeDataGhostDropJList(listModel,this);
		lists[0].addMouseListener(mouseListener);
		lists[0].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lists[0].setCellRenderer(listCellRenderer);
		lists[0].addListSelectionListener(listSelListener);
		
		lists[1] = new TreeDataGhostDropJList(listModel,this);
		lists[1].addMouseListener(mouseListener);
		lists[1].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lists[1].setCellRenderer(listCellRenderer);
		lists[1].addListSelectionListener(listSelListener);
		
		listHeaders[0] = new TreeDataListHeader(lists[0],listModel,listCellRenderer);
		listHeaders[1] = new TreeDataListHeader(lists[0],listModel,listCellRenderer);

		treeListPanels[0] = buildTreeListPanel(lists[0],listHeaders[0]);
		treeListPanels[1] = buildTreeListPanel(lists[1],listHeaders[1]);

		this.add(treeListPanels[0],BorderLayout.CENTER);
		repaint();
	}
	
	public void toggleViewMode()
	{
		removeAll();

		if(mode == SINGLE_VIEW_MODE)
		{
			// set to dual view mode
			this.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,treeListPanels[0],treeListPanels[1]),BorderLayout.CENTER);
			mode = DUAL_VIEW_MODE;
		}
		else
		{
			// set to single view mode
			this.add(treeListPanels[0],BorderLayout.CENTER);
			mode = SINGLE_VIEW_MODE;
		}
		repaint();
	}
	
	protected JPanel buildTreeListPanel(final TreeDataGhostDropJList treeList,TreeDataListHeader header)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
		
		JScrollPane listScroll = new JScrollPane(treeList);
		listScroll.setBackground(Color.WHITE);
		listScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroll.setColumnHeaderView(header);
		
		JPanel buttonPanel = setupButtonPanel(treeList);
		
		panel.add(listScroll, BorderLayout.CENTER);
		panel.add(buttonPanel,BorderLayout.EAST);

		return panel;
	}
	
	protected JPanel setupButtonPanel(final JList list)
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.PAGE_AXIS));
		
		Icon icon_subtree    = IconManager.getIcon("TTV_Subtree",   IconManager.IconSize.Std16);
		Icon icon_wholeTree  = IconManager.getIcon("TTV_WholeTree", IconManager.IconSize.Std16);
		Icon icon_allDescend = IconManager.getIcon("TTV_AllDescend",IconManager.IconSize.Std16);
		Icon icon_syncViews  = IconManager.getIcon("TTV_SyncViews", IconManager.IconSize.Std16);
		Icon icon_newChild   = IconManager.getIcon("TTV_NewChild",  IconManager.IconSize.Std16);
		Icon icon_editNode   = IconManager.getIcon("TTV_EditNode",  IconManager.IconSize.Std16);
		Icon icon_delNode    = IconManager.getIcon("TTV_DelNode",   IconManager.IconSize.Std16);
		
		JButton subtree = new JButton(icon_subtree);
		subtree.setSize(20,20);
		subtree.setToolTipText("View Subtree");
		subtree.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				showSubtreeOfSelection(list);
			}
		});
		
		JButton wholeTree = new JButton(icon_wholeTree);
		wholeTree.setSize(20,20);
		wholeTree.setToolTipText("View Whole Tree");
		wholeTree.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				showWholeTree(list);
			}
		});
		
		JButton showDescend = new JButton(icon_allDescend);
		showDescend.setSize(20,20);
		showDescend.setToolTipText("Show All Descendants");
		showDescend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				expandAllDescendantsOfSelection(list);
			}
		});
		
		JButton syncViews = new JButton(icon_syncViews);
		syncViews.setSize(20,20);
		syncViews.setToolTipText("Sync w/ Other View");
		syncViews.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				syncViewWithOtherView(list);
			}
		});
		
		JButton newChild = new JButton(icon_newChild);
		newChild.setSize(20,20);
		newChild.setToolTipText("Add Child to Selection");
		newChild.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				addChildToSelectedNode(list);
			}
		});

		JButton editNode = new JButton(icon_editNode);
		editNode.setSize(20,20);
		editNode.setToolTipText("Edit Selected Node");
		editNode.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				editSelectedNode(list);
			}
		});

		JButton deleteNode = new JButton(icon_delNode);
		deleteNode.setSize(20,20);
		deleteNode.setToolTipText("Delete Selected Node");
		deleteNode.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				deleteSelectedNode(list);
			}
		});

		// view manipulation buttons
		JLabel viewLabel = new JLabel("View");
		viewLabel.setSize(32,viewLabel.getHeight());
		buttonPanel.add(viewLabel);
		viewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(subtree);
		subtree.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(wholeTree);
		wholeTree.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(showDescend);
		showDescend.setAlignmentX(Component.CENTER_ALIGNMENT);

		buttonPanel.add(Box.createRigidArea(new Dimension(20,20)));
		
		// tree editing buttons
		JLabel editLabel = new JLabel("Edit");
		editLabel.setSize(32,editLabel.getHeight());
		buttonPanel.add(editLabel);
		editLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(newChild);
		newChild.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(editNode);
		editNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(deleteNode);
		deleteNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(syncViews);
		syncViews.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		return buttonPanel;
	}
	
	protected void syncViewWithOtherView(JList list)
	{
		log.error("Not yet implemented");
		if(list == lists[0])
		{
			// change the top list view to look like the bottom one
		}
		else
		{
			// change the bottom list view to look like the top one
		}
	}

	/**
	 * Displays data entry form for creating a new node that will be
	 * a child of the selected node.  A new child is only permanently
	 * created if the user chooses to procede with the data entry.
	 */
	public void addChildToSelectedNode(JList list)
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
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				Treeable node = (Treeable)dataObj;
				newNodeEntryComplete(node);
			}
			public void editCancelled(Object dataObj)
			{
				Treeable node = (Treeable)dataObj;
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
	public void deleteSelectedNode(JList list)
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
	protected void editSelectedNode(JList list)
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		final Treeable selectedNode = (Treeable)selection;
		
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				Treeable node = (Treeable)dataObj;
				editSelectedNodeOK(node);
			}
			public void editCancelled(Object dataObj)
			{
				Treeable node = (Treeable)dataObj;
				editSelectedNodeCancelled(node);
			}
		};

		showEditDialog(selectedNode, "Edit Node Values", callback);
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
		log.info("User selected 'OK' from edit node dialog: ");
		
        if( !node.getName().equals(nameBeforeEditDialogShown) )
        {
        	node.fixFullNameForAllDescendants();
        }

        node.updateModifiedTimeAndUser();
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
		Treeable root = listModel.getRoot();
		dataService.saveTree(root,deletedNodes);
		return true;
	}
	
	
	/**
	 * Sets the visibleRoot property of the tree to the currently selected node.  This provides
	 * the ability to "zoom in" to a lower level of the tree.
	 */
	public void showSubtreeOfSelection(JList list)
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
		// glassPane.setBusy(!glassPane.isBusy());
	}
	
	
	/**
	 * Sets the visibleRoot property to the actual root of the tree.  This results in the 
	 * entire tree being made available to the user.
	 */
	public void showWholeTree(JList list)
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
	public void expandAllDescendantsOfSelection(JList list)
	{
		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		Treeable node = (Treeable)selection;
		listModel.showDescendants(node);
	}
	
	
	public void find(String nodeName,int where,boolean wrap)
	{
		findName = nodeName;
		findResults = dataService.findByName(treeDef,findName);
		if(findResults.isEmpty())
		{
			//TODO: notify the user that no results were found
			log.error("Search returned no results.");
			return;
		}
		
		Treeable firstMatch = findResults.get(0);
		resultsIndex = 0;
		if(!showPathToNode(firstMatch))
		{
			//TODO: notify the user that no results are below current visible root
			log.error("No results below current visible root.");
			return;
		}
		
		if((where & DualViewSearchable.TOPVIEW) != 0)
		{
			lists[0].setSelectedValue(firstMatch,true);
		}
		if((where & DualViewSearchable.BOTTOMVIEW) != 0)
		{
			lists[1].setSelectedValue(firstMatch,true);
		}
	}
	
	
	public void findNext(int where,boolean wrap)
	{
		if(findResults != null && findName != null)
		{
			log.error("Searching for next node from previous search: " + findName);
			// find the next node from the previous search
			if(findResults.size()-1 == resultsIndex && !wrap)
			{
				//TODO: notify the user that no more results
				log.error("No more results");
				return;
			}
			
			resultsIndex = (resultsIndex+1) % findResults.size();
			Treeable nextNode = findResults.get(resultsIndex);
			if( !showPathToNode(nextNode) )
			{
				//TODO: notify the user that no results are below current visible root
				log.error("No more results below current visible root.");
				return;
			}

			if((where & DualViewSearchable.TOPVIEW) != 0)
			{
				lists[0].setSelectedValue(nextNode,true);
			}
			if((where & DualViewSearchable.BOTTOMVIEW) != 0)
			{
				lists[1].setSelectedValue(nextNode,true);
			}
		}
	}
	
	 
	
	protected boolean showPathToNode(Treeable node)
	{
		List<Treeable> pathToNode = node.getAllAncestors();
		Treeable visRoot = listModel.getVisibleRoot();
		if(!pathToNode.contains(visRoot))
		{
			return false;
		}
		
		for( int i = pathToNode.indexOf(visRoot); i < pathToNode.size(); ++i )
		{
			listModel.setChildrenVisible(pathToNode.get(i),true);
		}
		return true;
	}
	
	
	/**
	 * Display the form for editing node data.
	 *
	 * @param node the node being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showEditDialog(Treeable node,String title,EditDialogCallback callback)
	{
		nameBeforeEditDialogShown = node.getName();
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
	 * @param t the newly selected Treeable
	 */
	protected void newTreeableSelected(ListSelectionEvent e)
	{
		TreeDataGhostDropJList sourceList = (TreeDataGhostDropJList)e.getSource();
		Treeable t = (Treeable)sourceList.getSelectedValue();
		if( t == null )
		{
			statusBar.setText(null);
			return;
		}
		
		statusBar.setText(t.getFullName());
	}
	

	/**
	 * Reparents <code>dragged</code> to <code>droppedOn</code> by calling
	 * {@link TreeDataListModel#reparent(Treeable, Treeable)}.
	 *
	 * @see edu.ku.brc.ui.DragDropCallback#dropOccurred(java.lang.Object, java.lang.Object)
	 * @param dragged the dragged tree node
	 * @param droppedOn the node the dragged node was dropped onto
	 */
	public boolean dropOccurred( Object dragged, Object droppedOn, int dropAction )
	{
		if( !(dragged instanceof Treeable && droppedOn instanceof Treeable) )
		{
			log.warn("Ignoring drag and drop of unhandled types of objects");
			return false;
		}

		Treeable draggedNode = (Treeable)dragged;
		Treeable droppedOnNode = (Treeable)droppedOn;

		if( dropAction == DnDConstants.ACTION_COPY || dropAction == DnDConstants.ACTION_NONE )
		{
			// TODO: at this point we need to add a new treeable relationship
			// between dragged and droppedOn
			
			// for Taxon: setup new TaxonomicRelationship
			// for Geog:  setup new GeographyNameRelationship
			// for ?
			log.warn("User requested new relationship be created between " + draggedNode.getName() + " and " + droppedOnNode.getName());
			return true;
		}
		else if( dropAction == DnDConstants.ACTION_MOVE )
		{
			Treeable child = draggedNode;
			Treeable newParent = droppedOnNode;
			
			if( !treeDef.canChildBeReparentedToNode(child,newParent) )
			{
				log.info("Cannot reparent " + child.getName() + " to " + newParent.getName());
				return false;
			}
			
			@SuppressWarnings("unused")
			boolean changed = listModel.reparent(child,newParent);
			return true;
		}
		return false;
	}
	
	
	public boolean dropAcceptable( Object dragged, Object droppedOn, int dropAction )
	{
		// TODO: fully implement this
		// XXX
		if(dropAction == DnDConstants.ACTION_COPY  || dropAction == DnDConstants.ACTION_NONE)
		{
			if(listModel.indexOf(dragged) != -1 && listModel.indexOf(droppedOn) != -1 )
			{
				return true;
			}
		}
		else if(dropAction == DnDConstants.ACTION_MOVE)
		{
			//TODO: check if reparenting is allowed
			if( !(dragged instanceof Treeable && droppedOn instanceof Treeable) )
			{
				return false;
			}
			
			Treeable child = (Treeable)dragged;
			Treeable newParent = (Treeable)droppedOn;
			
			if( !treeDef.canChildBeReparentedToNode(child,newParent) )
			{
				return false;
			}
			return true;
		}
		
		return false;
	}

	
	public void mouseButtonClicked(MouseEvent e)
	{
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
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

	
	public void mouseButtonPressed(MouseEvent e)
	{
		final TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
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
			list.setClickOnText(true);
			// mouse press is on text area
		}
		else
		{
			list.setClickOnText(false);
		}
	}

	
	@Override
	public void shutdown()
	{
		super.shutdown();
		dataService.fini();
	}
}
