/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.DualViewSearchable;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.EditFormDialog.EditDialogCallback;
import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-based view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 *
 * @code_status Beta
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeTableViewer <T extends Treeable<T,D,I>,
								D extends TreeDefIface<T,D,I>,
								I extends TreeDefItemIface<T,D,I>>
								extends BaseSubPane
								implements DragDropCallback, DualViewSearchable
{
	private static final int SINGLE_VIEW_MODE = 0;
	private static final int DUAL_VIEW_MODE = 1;
	
	/** Status message display widget. */
	protected JStatusBar statusBar;
	
	/** Model holding all <code>Treeable</code> nodes. */
	protected TreeDataListModel<T,D,I> listModel;
	/** The tree display widget. */
	protected TreeDataGhostDropJList[] lists;
	/** The scroll panes that contains the lists. */
	protected JScrollPane[] scrollers;
	/** Cell renderer for displaying individual nodes in the tree. */
	protected TreeDataListCellRenderer<T,D,I> listCellRenderer;
	/** A header for the tree, displaying the names of the visible levels. */
	@SuppressWarnings("unchecked")
    protected TreeDataListHeader[] listHeaders;
	
	protected JPanel[] treeListPanels;
    
    protected FindPanel findPanel;
	
	protected D treeDef;
	
	// to track name changes by the user
	protected String nameBeforeEditDialogShown;
	
    /** Logger for all messages emitted. */
    private static final Logger log = Logger.getLogger(TreeTableViewer.class);
    
    protected String findName;
    protected int resultsIndex;
    protected List<T> findResults;
    
    protected boolean isInitialized;
    protected int mode;
    
    protected TreeNodePopupMenu popupMenu;
    
    protected boolean busy;
    protected String busyReason;
    
    protected List<AbstractButton> allButtons;
    private JButton subtree0;
    private JButton wholeTree0;
    private JButton toParent0;
    private JButton syncViews0;
    private JButton toggle0;
    private JButton newChild0;
    private JButton editNode0;
    private JButton deleteNode0;
    private JButton subtree1;
    private JButton wholeTree1;
    private JButton toParent1;
    private JButton syncViews1;
    private JButton toggle1;
    private JButton newChild1;
    private JButton editNode1;
    private JButton deleteNode1;
    
	/**
	 * Build a TreeTableViewer to view/edit the data found.
	 * 
	 * @param treeDef handle to the tree to be displayed
	 * @param name a String name for this viewer/editor
	 * @param task the owning Taskable
	 */
	public TreeTableViewer( final D treeDef,
							final String name,
							final Taskable task )
	{
		super(name,task);
		this.treeDef = treeDef;
		allButtons = new Vector<AbstractButton>();
		statusBar = UIRegistry.getStatusBar();
		popupMenu = new TreeNodePopupMenu(this);
		
		getLayout().removeLayoutComponent(progressBarPanel);
		
		setBusy(false,null);
	}
	
	public D getTreeDef()
	{
		return this.treeDef;
	}
	
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
	 */
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

    protected boolean checkBusy()
    {
        if (statusBar!=null)
        {
            if(busy)
            {
                setStatusBarText("System busy: " + busyReason);
            }
        }
        return busy;
    }
    
    protected void setStatusBarText(String text)
    {
        if (statusBar!=null)
        {
            statusBar.setText(text);
        }
    }
    
    /**
     * Calls showTree().  Simply provides an easy override point for subclasses.
     */
    protected void initTreeLists()
    {
        listModel = new TreeDataListModel<T,D,I>(treeDef);
        showTree();
    }
    
	/**
	 * Initialize and show the tree display component.
	 */
	protected void showTree()
	{
		MouseListener mouseListener = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e){mouseButtonClicked(e);}
			@Override
			public void mousePressed(MouseEvent e){mouseButtonPressed(e);}
			@Override
			public void mouseReleased(MouseEvent e){mouseButtonReleased(e);}
		};
		//listModel = new TreeDataListModel<T,D,I>(treeDef);
		Color[] bgs = new Color[2];
		bgs[0] = new Color(202,238,255);
		bgs[1] = new Color(151,221,255);
		listCellRenderer = new TreeDataListCellRenderer<T,D,I>(listModel,bgs);
		ListSelectionListener listSelListener = new ListSelectionListener()
		{
			@SuppressWarnings("unchecked")
            public void valueChanged(ListSelectionEvent e)
			{
                TreeDataGhostDropJList sourceList = (TreeDataGhostDropJList)e.getSource();
                T node = (T)sourceList.getSelectedValue();

				newTreeableSelected(sourceList,node);
			}
		};

		// setup both views
		lists = new TreeDataGhostDropJList[2];
		scrollers = new JScrollPane[2];
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
		
		listHeaders[0] = new TreeDataListHeader<T,D,I>(lists[0],listModel,listCellRenderer);
		listHeaders[1] = new TreeDataListHeader<T,D,I>(lists[0],listModel,listCellRenderer);

		scrollers[0] = new JScrollPane(lists[0]);
		scrollers[0].setBackground(Color.WHITE);
		scrollers[0].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollers[0].setColumnHeaderView(listHeaders[0]);
		
		scrollers[1] = new JScrollPane(lists[1]);
		scrollers[1].setBackground(Color.WHITE);
		scrollers[1].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollers[1].setColumnHeaderView(listHeaders[1]);
		
        // icons for buttons
        Icon icon_subtree    = IconManager.getIcon("TTV_Subtree",   IconManager.IconSize.Std16);
        Icon icon_wholeTree  = IconManager.getIcon("TTV_WholeTree", IconManager.IconSize.Std16);
        Icon icon_syncViews  = IconManager.getIcon("TTV_SyncViews", IconManager.IconSize.Std16);
        Icon icon_newChild   = IconManager.getIcon("TTV_NewChild",  IconManager.IconSize.Std16);
        Icon icon_editNode   = IconManager.getIcon("TTV_EditNode",  IconManager.IconSize.Std16);
        Icon icon_delNode    = IconManager.getIcon("TTV_DelNode",   IconManager.IconSize.Std16);
        Icon icon_toParent   = IconManager.getIcon("TTV_ToParent",  IconManager.IconSize.Std16);
        Icon icon_toggle     = IconManager.getIcon("TTV_ToggleViewMode", IconManager.IconSize.Std16);
        
        // button panel for top tree list
        JPanel buttonPanel0 = new JPanel();
        buttonPanel0.setLayout(new BoxLayout(buttonPanel0,BoxLayout.PAGE_AXIS));
        
        subtree0 = new JButton(icon_subtree);
        subtree0.setSize(20,20);
        subtree0.setToolTipText("View Subtree");
        subtree0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showSubtreeOfSelection(lists[0]);
            }
        });
        
        wholeTree0 = new JButton(icon_wholeTree);
        wholeTree0.setSize(20,20);
        wholeTree0.setToolTipText("View Whole Tree");
        wholeTree0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showWholeTree(lists[0]);
            }
        });

        toParent0 = new JButton(icon_toParent);
        toParent0.setSize(20,20);
        toParent0.setToolTipText("Go To Parent");
        toParent0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectParentOfSelection(lists[0]);
            }
        });
        
        syncViews0 = new JButton(icon_syncViews);
        syncViews0.setSize(20,20);
        syncViews0.setToolTipText("Sync w/ Other View");
        syncViews0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                syncViewWithOtherView(lists[0]);
            }
        });
        
        toggle0 = new JButton(icon_toggle);
        toggle0.setSize(20,20);
        toggle0.setToolTipText("Toggle View Mode");
        toggle0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleViewMode();
            }
        });

        newChild0 = new JButton(icon_newChild);
        newChild0.setSize(20,20);
        newChild0.setToolTipText("Add Child to Selection");
        newChild0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addChildToSelectedNode(lists[0]);
            }
        });

        editNode0 = new JButton(icon_editNode);
        editNode0.setSize(20,20);
        editNode0.setToolTipText("Edit Selected Node");
        editNode0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                editSelectedNode(lists[0]);
            }
        });

        deleteNode0 = new JButton(icon_delNode);
        deleteNode0.setSize(20,20);
        deleteNode0.setToolTipText("Delete Selected Node");
        deleteNode0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                deleteSelectedNode(lists[0]);
            }
        });

        // view manipulation buttons
        JLabel viewLabel0 = new JLabel("View");
        viewLabel0.setSize(32,viewLabel0.getHeight());
        buttonPanel0.add(viewLabel0);
        viewLabel0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(subtree0);
        subtree0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(wholeTree0);
        wholeTree0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(toParent0);
        toParent0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(toggle0);
        toggle0.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel0.add(Box.createRigidArea(new Dimension(20,20)));
        
        // tree editing buttons
        JLabel editLabel0 = new JLabel("Edit");
        editLabel0.setSize(32,editLabel0.getHeight());
        buttonPanel0.add(editLabel0);
        editLabel0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(newChild0);
        newChild0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(editNode0);
        editNode0.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel0.add(deleteNode0);
        deleteNode0.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel0.add(Box.createVerticalGlue());
        buttonPanel0.add(syncViews0);
        syncViews0.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        allButtons.add(subtree0);
        allButtons.add(wholeTree0);
        allButtons.add(toParent0);
        allButtons.add(toggle0);
        allButtons.add(newChild0);
        allButtons.add(editNode0);
        allButtons.add(deleteNode0);
 
		treeListPanels[0] = new JPanel();
		treeListPanels[0].setLayout(new BorderLayout());
		treeListPanels[0].add(scrollers[0], BorderLayout.CENTER);
		treeListPanels[0].add(buttonPanel0,BorderLayout.EAST);
        findPanel = new FindPanel(this, FindPanel.CONTRACTED);
        treeListPanels[0].add(findPanel, BorderLayout.SOUTH);
        
        // button panel for bottom tree list
        JPanel buttonPanel1 = new JPanel();
        buttonPanel1.setLayout(new BoxLayout(buttonPanel1,BoxLayout.PAGE_AXIS));
        
        subtree1 = new JButton(icon_subtree);
        subtree1.setSize(20,20);
        subtree1.setToolTipText("View Subtree");
        subtree1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showSubtreeOfSelection(lists[1]);
            }
        });
        
        wholeTree1 = new JButton(icon_wholeTree);
        wholeTree1.setSize(20,20);
        wholeTree1.setToolTipText("View Whole Tree");
        wholeTree1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showWholeTree(lists[1]);
            }
        });

        toParent1 = new JButton(icon_toParent);
        toParent1.setSize(20,20);
        toParent1.setToolTipText("Go To Parent");
        toParent1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectParentOfSelection(lists[1]);
            }
        });
        
        syncViews1 = new JButton(icon_syncViews);
        syncViews1.setSize(20,20);
        syncViews1.setToolTipText("Sync w/ Other View");
        syncViews1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                syncViewWithOtherView(lists[1]);
            }
        });
        
        toggle1 = new JButton(icon_toggle);
        toggle1.setSize(20,20);
        toggle1.setToolTipText("Toggle View Mode");
        toggle1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleViewMode();
            }
        });

        newChild1 = new JButton(icon_newChild);
        newChild1.setSize(20,20);
        newChild1.setToolTipText("Add Child to Selection");
        newChild1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addChildToSelectedNode(lists[1]);
            }
        });
        
        editNode1 = new JButton(icon_editNode);
        editNode1.setSize(20,20);
        editNode1.setToolTipText("Edit Selected Node");
        editNode1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                editSelectedNode(lists[1]);
            }
        });

        deleteNode1 = new JButton(icon_delNode);
        deleteNode1.setSize(20,20);
        deleteNode1.setToolTipText("Delete Selected Node");
        deleteNode1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                deleteSelectedNode(lists[1]);
            }
        });

        // view manipulation buttons
        JLabel viewLabel1 = new JLabel("View");
        viewLabel1.setSize(32,viewLabel1.getHeight());
        buttonPanel1.add(viewLabel1);
        viewLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(subtree1);
        subtree1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(wholeTree1);
        wholeTree1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(toParent1);
        toParent1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(toggle1);
        toggle1.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel1.add(Box.createRigidArea(new Dimension(20,20)));
        
        // tree editing buttons
        JLabel editLabel1 = new JLabel("Edit");
        editLabel1.setSize(32,editLabel1.getHeight());
        buttonPanel1.add(editLabel1);
        editLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(newChild1);
        newChild1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(editNode1);
        editNode1.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel1.add(deleteNode1);
        deleteNode1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel1.add(Box.createVerticalGlue());
        buttonPanel1.add(syncViews1);
        syncViews1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        allButtons.add(subtree1);
        allButtons.add(wholeTree1);
        allButtons.add(toParent1);
        allButtons.add(toggle1);
        allButtons.add(newChild1);
        allButtons.add(editNode1);
        allButtons.add(deleteNode1);
 
		treeListPanels[1] = new JPanel();
		treeListPanels[1].setLayout(new BorderLayout());
		treeListPanels[1].add(scrollers[1], BorderLayout.CENTER);
		treeListPanels[1].add(buttonPanel1,BorderLayout.EAST);
		
		setViewMode(SINGLE_VIEW_MODE);
        
        Action findAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                findPanel.expand();
            }
        };
        
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "Find");
        this.getActionMap().put("Find", findAction);
        
        lists[0].getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), findAction);
        lists[0].getActionMap().put("Find", findAction);
        lists[1].getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), findAction);
        lists[1].getActionMap().put("Find", findAction);
        
        // force the selection-sensative buttons to initialize properly
        newTreeableSelected(lists[0],null);
        newTreeableSelected(lists[1],null);
        
        UIRegistry.forceTopFrameRepaint();
	}
	
	@SuppressWarnings("unchecked")
    protected void setBusy(boolean busy,String statusText)
	{
		if (busy)
		{
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}
		else
		{
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
        // enable/disable all the buttons
		for (AbstractButton ab: allButtons)
		{
			ab.setEnabled(!busy);
		}
        
        // now fix the add/delete buttons to be disabled if addition/deletion isn't possible
		if (lists != null)
        {
            if (lists[0] != null)
            {
                T node = (T)lists[0].getSelectedValue();
                newChild0.setEnabled(listModel.canAddChildToNode(node));
                deleteNode0.setEnabled(listModel.canDeleteNode(node));
            }
            if (lists[1] != null)
            {
                T node = (T)lists[1].getSelectedValue();
                newChild1.setEnabled(listModel.canAddChildToNode(node));
                deleteNode1.setEnabled(listModel.canDeleteNode(node));
            }
        }

		this.busy = busy;
		busyReason = statusText;
        if (statusBar!=null)
        {
            setStatusBarText(busyReason);
            statusBar.setIndeterminate(busy);
        }
	}
    
	
	protected void toggleViewMode()
	{
		if(checkBusy())
		{
			return;
		}
		
		if(mode == SINGLE_VIEW_MODE)
		{
			setViewMode(DUAL_VIEW_MODE);
		}
		else
		{
			setViewMode(SINGLE_VIEW_MODE);
		}
        
        syncViews0.setEnabled(mode == DUAL_VIEW_MODE);
        syncViews1.setEnabled(mode == DUAL_VIEW_MODE);
		
		repaint();
    	UIRegistry.forceTopFrameRepaint();
	}
    
	
	protected void setViewMode(int newMode)
	{
		removeAll();
		mode = newMode;
		if(mode == SINGLE_VIEW_MODE)
		{
			// set to single view mode
			this.add(treeListPanels[0],BorderLayout.CENTER);
		}
		else
		{
			// set to dual view mode
			this.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,treeListPanels[0],treeListPanels[1]),BorderLayout.CENTER);
		}
		repaint();
	}
    
	protected void syncViewWithOtherView(JList list)
	{
		if(list == lists[0])
		{
			// get the info from scrollers[1]
			// and set it into scrollers[0]
			Point p = scrollers[1].getViewport().getViewPosition();
			scrollers[0].getViewport().setViewPosition(p);
		}
		else
		{
			// get the info from scrollers[0]
			// and set it into scrollers[1]
			Point p = scrollers[0].getViewport().getViewPosition();
			scrollers[1].getViewport().setViewPosition(p);
		}
	}

	/**
	 * Displays data entry form for creating a new node that will be
	 * a child of the selected node.  A new child is only permanently
	 * created if the user chooses to procede with the data entry.
	 */
	@SuppressWarnings("unchecked")
	public void addChildToSelectedNode(JList list)
	{
        if (checkBusy())
        {
            return;
        }

		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		T parent = (T)selection;
		I parentDefItem = parent.getDefinitionItem();
		if( parentDefItem.getChild() == null )
		{
			log.info("Cannot add child node below this rank");
			return;
		}

		T newT = (T)TreeFactory.createNewTreeable(parent.getClass(),"");
		newT.setDefinitionItem(parentDefItem.getChild());
		newT.setDefinition(parent.getDefinition());

		// display a form for filling in child data
		showNewTreeableForm(newT,parent);
	}

	/**
	 * Display the data entry form for creating a new node.
	 *
	 * @param newNode the new node for which the user must enter data
	 */
	protected void showNewTreeableForm(final T newNode, final T parent)
	{
		EditDialogCallback<T> callback = new EditDialogCallback<T>()
		{
			public void editCompleted(T dataObj)
			{
				newNodeEntryComplete(dataObj,parent);
			}
			public void editCancelled(T dataObj)
			{
				newNodeEntryCancelled(dataObj);
			}
		};

		showEditDialog(newNode, "New Node Form", callback, true);
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
	public void newNodeEntryComplete(T node, T parent)
	{
        listModel.addNewChild(parent, node);
		
		listModel.showChildren(parent);
	}

	/**
	 * Cancels new node creation process.  This method serves
	 * as a callback to the data entry form for when the user
	 * presses the 'Cancel' button on the form.
	 *
	 * @param node the new node that was in the process of being created
	 */
	public void newNodeEntryCancelled(T node)
	{
		if( node == null )
		{
			return;
		}

		node.setParent(null);
		
		node.setDefinition(null);
        node.setDefinitionItem(null);
	}

	/**
	 * Deletes the currently selected node and all descendants if and
	 * only if it is determined possible without violating any business
	 * rules.
	 */
	@SuppressWarnings("unchecked")
    public void deleteSelectedNode(JList list)
	{
		if(checkBusy())
		{
			return;
		}

		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
        // unchecked cast, unavoidable
        T node = (T)selection;
        int numNodesToDelete = listModel.getDescendantCount(node)+1;
        int userChoice = JOptionPane.OK_OPTION;
        if (numNodesToDelete > 1)
        {
            userChoice = JOptionPane.showConfirmDialog(this, "This operation will delete "
                    + numNodesToDelete + " nodes", "Continue?", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
        }
        if (userChoice == JOptionPane.OK_OPTION)
        {
            T parent = node.getParent();
            listModel.hideChildren(parent);
            listModel.deleteNode(node);
            listModel.showChildren(parent);
            setStatusBarText(numNodesToDelete + " node(s) deleted");
        }
	}
    
    public void initializeNodeAssociations(T node)
    {
        listModel.initializeNodeAssociations(node);
    }
	
	/**
	 * Display a form for editing the data in the currently selected node.
	 */
	@SuppressWarnings("unchecked")
	public void editSelectedNode(JList list)
	{
		if(checkBusy())
		{
			return;
		}

		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		final T selectedNode = (T)selection;
		
		EditDialogCallback<T> callback = new EditDialogCallback<T>()
		{
			public void editCompleted(T dataObj)
			{
				editSelectedNodeOK(dataObj);
			}
			public void editCancelled(T dataObj)
			{
				// do nothing
			}
		};

		showEditDialog(selectedNode, "Edit Node Values", callback, false);
	}

	/**
	 * Performs finalization of node data editing process.  This
	 * method also signals the tree display widget to update its
	 * view.
	 *
	 * @param node the node being edited
	 */
	protected void editSelectedNodeOK(T node)
	{
        // TODO: implement this to work in a session-less environment
//		boolean nameChanged = !node.getName().equals(nameBeforeEditDialogShown);
//		Boolean levelIsInFullName = node.getDefinitionItem().getIsInFullName();
//		
//        if( nameChanged && levelIsInFullName != null && levelIsInFullName.booleanValue() )
//        {
//        	node.fixFullNameForAllDescendants();
//        }

		listModel.nodeValuesChanged(node);
	}
	
	/**
	 * Sets the visibleRoot property of the tree to the currently selected node.  This provides
	 * the ability to "zoom in" to a lower level of the tree.
	 */
	@SuppressWarnings("unchecked")
	public void showSubtreeOfSelection(JList list)
	{
		if(checkBusy())
		{
			return;
		}

		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		
		T node = (T)selection;

		listModel.setVisibleRoot(node);
		
		list.setSelectedValue(node,true);
	}
	
	/**
	 * Sets the visibleRoot property to the actual root of the tree.  This results in the 
	 * entire tree being made available to the user.
	 */
	public void showWholeTree(JList list)
	{
		if(checkBusy())
		{
			return;
		}

		Object selection = list.getSelectedValue();		

		listModel.setVisibleRoot(listModel.getRoot());
		
		if( selection != null )
		{
			list.setSelectedValue(selection,true);
		}
	}

	@SuppressWarnings("unchecked")
	public void selectParentOfSelection(JList list)
	{
		if(checkBusy())
		{
			return;
		}

		Object selection = list.getSelectedValue();
		if( selection == null )
		{
			return;
		}
		T node = (T)selection;
		T parent = node.getParent();
		list.setSelectedValue(parent,true);
	}
			
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.DualViewSearchable#find(java.lang.String, int, boolean)
	 */
	public void find(String nodeName,int where,boolean wrap)
	{
		if(checkBusy())
		{
			return;
		}

		findName = nodeName;
		findResults = listModel.findByName(findName);
		if(findResults.isEmpty())
		{
			//TODO: notify the user that no results were found
			log.info("Search for '"+nodeName+"' returned no results");
			setStatusBarText("Search for '"+nodeName+"' returned no results");
			return;
		}
		
		T firstMatch = findResults.get(0);
		resultsIndex = 0;
		if(!showPathToNode(firstMatch))
		{
			//TODO: notify the user that no results are below current visible root
			log.error("No results below current visible root");
			setStatusBarText("No results below current visible root");
			return;
		}
		
		if((where & DualViewSearchable.TOPVIEW) != 0)
		{
			lists[0].setSelectedValue(firstMatch,true);
		}
		if((where & DualViewSearchable.BOTTOMVIEW) != 0)
		{
            if (mode == SINGLE_VIEW_MODE)
            {
                toggleViewMode();
            }
            lists[1].setSelectedValue(firstMatch,true);
		}
	}
	
	public void find(String nodeName,JList where,boolean wrap)
	{
		if(checkBusy())
		{
			return;
		}

		if(where == lists[0])
		{
			find(nodeName,DualViewSearchable.TOPVIEW,wrap);
		}
		else if(where == lists[1])
		{
			find(nodeName,DualViewSearchable.BOTTOMVIEW,wrap);
		}
		else
		{
			// throw new IllegalArgumentException?
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.DualViewSearchable#findNext(java.lang.String, int, boolean)
	 */
	public void findNext(String key,int where,boolean wrap)
	{
		if(checkBusy())
		{
			return;
		}

		if(key != null && !key.equals(findName))
		{
			find(key,where,wrap);
			return;
		}
		
		if(findResults != null && findResults.size()>0 && findName != null)
		{
			log.info("Searching for next node from previous search: " + findName);
			// find the next node from the previous search
			if(findResults.size()-1 == resultsIndex && !wrap)
			{
				//TODO: notify the user that no more results
				log.info("No more results");
				setStatusBarText("No more results");
				return;
			}
			
			resultsIndex = (resultsIndex+1) % findResults.size();
			T nextNode = findResults.get(resultsIndex);
			if( !showPathToNode(nextNode) )
			{
				//TODO: notify the user that no results are below current visible root
				log.info("No more results below current visible root");
				setStatusBarText("No more results below current visible root");
				return;
			}

			if((where & DualViewSearchable.TOPVIEW) != 0)
			{
				lists[0].setSelectedValue(nextNode,true);
			}
			if((where & DualViewSearchable.BOTTOMVIEW) != 0)
			{
                if (mode == SINGLE_VIEW_MODE)
                {
                    toggleViewMode();
                }
				lists[1].setSelectedValue(nextNode,true);
			}
		}
	}
	
	public void findNext(int where,boolean wrap,T current)
	{
		List<T> matches = listModel.findByName(current.getName());
		if(matches.size()==1)
		{
			setStatusBarText("No more matches");
			return;
		}
		
		int curIndex = matches.indexOf(current);
		if(!wrap && curIndex == matches.size()-1)
		{
			setStatusBarText("No more matches");
			return;
		}
		
		T nextNode = matches.get((curIndex + 1)%matches.size());
		if((where & DualViewSearchable.TOPVIEW) != 0)
		{
			lists[0].setSelectedValue(nextNode,true);
		}
		if((where & DualViewSearchable.BOTTOMVIEW) != 0)
		{
            if (mode == SINGLE_VIEW_MODE)
            {
                toggleViewMode();
            }
			lists[1].setSelectedValue(nextNode,true);
		}
	}
	
	public void findNext(JList where,boolean wrap,T currentNode)
	{
		if(checkBusy())
		{
			return;
		}
		
		if(where == lists[0])
		{
			findNext(DualViewSearchable.TOPVIEW,wrap,currentNode);
		}
		else if(where == lists[1])
		{
			findNext(DualViewSearchable.BOTTOMVIEW,wrap,currentNode);
		}
		else
		{
			// throw new IllegalArgumentException?
		}
	}
	
	protected boolean showPathToNode(T node)
	{
		List<T> pathToNode = node.getAllAncestors();
		T visRoot = listModel.getVisibleRoot();
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
	protected void showEditDialog(T node,String title, EditDialogCallback<T> callback, boolean isNewObject)
	{
		nameBeforeEditDialogShown = node.getName();
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(node);
		TreeNodeEditDialog<T,D,I> editDialog = new TreeNodeEditDialog<T,D,I>(formsNames.first,formsNames.second,title,callback,isNewObject);
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
	@Override
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
	@SuppressWarnings("unchecked")
	protected void newTreeableSelected(JList sourceList, T selectedNode)
	{
        // these calls should work even if t is null
        boolean canDelete   = listModel.canDeleteNode(selectedNode);
        boolean canAddChild = listModel.canAddChildToNode(selectedNode);
        
        popupMenu.setDeleteEnabled(canDelete);
        popupMenu.setNewEnabled(canAddChild);

        boolean enable = (selectedNode != null);
        
        // update the state of all selection-sensative buttons
        if (sourceList == lists[0])
        {
            newChild0.setEnabled(canAddChild);
            deleteNode0.setEnabled(canDelete);
            
            editNode0.setEnabled(enable);
            subtree0.setEnabled(enable);
            toParent0.setEnabled(enable);
        }
        else
        {
            newChild1.setEnabled(canAddChild);
            deleteNode1.setEnabled(canDelete);
            
            editNode1.setEnabled(enable);
            subtree1.setEnabled(enable);
            toParent1.setEnabled(enable);
        }
        
        // clear the status bar if nothing is selected or show the fullname if a node is selected
        String fullname = null;
		if( selectedNode != null )
		{
            fullname = selectedNode.getFullName();
		}
		
		setStatusBarText(fullname);
	}

	/**
	 * Reparents <code>dragged</code> to <code>droppedOn</code> by calling
	 * {@link TreeDataListModel#reparent(Treeable, Treeable)}.
	 *
	 * @see edu.ku.brc.ui.DragDropCallback#dropOccurred(java.lang.Object, java.lang.Object)
	 * @param dragged the dragged tree node
	 * @param droppedOn the node the dragged node was dropped onto
	 */
	@SuppressWarnings("unchecked")
	public boolean dropOccurred( Object dragged, Object droppedOn, int dropAction )
	{
		if(checkBusy())
		{
			return false;
		}

		if( !(dragged instanceof Treeable && droppedOn instanceof Treeable) )
		{
			log.warn("Ignoring drag and drop of unhandled types of objects");
			return false;
		}

		T draggedNode = (T)dragged;
		T droppedOnNode = (T)droppedOn;

		if( dropAction == DnDConstants.ACTION_COPY || dropAction == DnDConstants.ACTION_NONE )
		{
			// TODO: at this point we need to add a new treeable relationship
			// between dragged and droppedOn
			
			// for Taxon: setup new TaxonomicRelationship
			// for Geog:  setup new GeographyNameRelationship
			// for ?
            
            // maybe this should be a pluggable interface?
            // have something like 'public boolean NodeRelationshipManager.createRelationship(T node1, T node2)'
            // that would allow for plugins to handle Taxon and Geography without implementing any for Location and GTP
            // something like...
            // if (pluginMgr != null)
            //{
            //  do stuff to create a relationship    
            //}
            
			log.warn("User requested new relationship be created between " + draggedNode.getName() + " and " + droppedOnNode.getName());
			return true;
		}
		else if( dropAction == DnDConstants.ACTION_MOVE )
		{
			T child = draggedNode;
			T newParent = droppedOnNode;
			
			if( !treeDef.canChildBeReparentedToNode(child,newParent) )
			{
				log.info("Cannot reparent " + child.getName() + " to " + newParent.getName());
				return false;
			}
			
			boolean changed = listModel.reparent(child,newParent);
			return changed;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.DragDropCallback#dropAcceptable(java.lang.Object, java.lang.Object, int)
	 */
	@SuppressWarnings("unchecked")
	public boolean dropAcceptable( Object dragged, Object droppedOn, int dropAction )
	{
		if(checkBusy())
		{
			return false;
		}

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
			if( !(dragged instanceof Treeable && droppedOn instanceof Treeable) )
			{
				return false;
			}
			
			T child = (T)dragged;
			T newParent = (T)droppedOn;
			
			if( !treeDef.canChildBeReparentedToNode(child,newParent) )
			{
				return false;
			}
			return true;
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	public void showPopup(MouseEvent e)
	{
		if(checkBusy())
		{
			return;
		}

		if(clickIsOnText(e))
		{
			// select this node and display popup for it
			final TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
			Point p = e.getPoint();
			int index = list.locationToIndex(p);
			if(index==-1)
			{
				return;
			}
			list.setSelectedIndex(index);
			popupMenu.setList(list);
			popupMenu.show(list,e.getX(),e.getY());
		}
		else
		{
			return;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected boolean clickIsOnText(MouseEvent e)
	{
		final TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if(index==-1)
		{
			return false;
		}
		T t = (T)listModel.getElementAt(index);
		Integer rank = t.getRankId();
		Pair<Integer,Integer> textBounds = listCellRenderer.getTextBoundsForRank(rank);
		
		if( textBounds.first < p.x && p.x < textBounds.second )
		{
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean clickIsOnExpansionIcon(MouseEvent e)
	{
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if(index==-1)
		{
			return false;
		}
		T t = (T)listModel.getElementAt(index);
		Integer rank = t.getRankId();
		Pair<Integer,Integer> anchorBounds = listCellRenderer.getAnchorBoundsForRank(rank);
		
		if( anchorBounds.first < p.x && p.x < anchorBounds.second )
		{
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void mouseButtonClicked(MouseEvent e)
	{
		if(checkBusy())
		{
			return;
		}

		if (e.isPopupTrigger())
		{
			showPopup(e);
		}
		
		if(e.getButton() != MouseEvent.BUTTON1)
		{
			return;
		}
				
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if(index==-1)
		{
			return;
		}
		T t = (T)listModel.getElementAt(index);

        // if the user clicked an expansion handle, expand the child nodes
		if( clickIsOnExpansionIcon(e) || (e.getClickCount()==2 && clickIsOnText(e)) )
		{
			// toggle the state of child node visibility
			boolean visible = listModel.allChildrenAreVisible(t);
			listModel.setChildrenVisible(t, !visible);
		}
        // otherwise, ignore the click
		else
		{
			e.consume();
		}
	}
	
	public void mouseButtonReleased(MouseEvent e)
	{
		if(checkBusy())
		{
			return;
		}

		if(e.isPopupTrigger())
		{
			showPopup(e);
		}
	}
	
	public void mouseButtonPressed(MouseEvent e)
	{
		if(checkBusy())
		{
			return;
		}

		if(e.isPopupTrigger())
		{
			showPopup(e);
		}

		if(e.getButton() != MouseEvent.BUTTON1)
		{
			return;
		}
		
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		if( clickIsOnText(e) )
		{
			list.setClickOnText(true);
		}
		else
		{
			list.setClickOnText(false);
		}
	}

	@Override
	public boolean aboutToShutdown()
	{
		if (checkBusy())
		{
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
	 */
	@Override
	public void shutdown()
	{
		super.shutdown();
	}
	
	@SuppressWarnings("unchecked")
	public T getSelectedNode(JList list)
	{
		if(lists[0] == list || lists[1] == list )
		{
			return (T)list.getSelectedValue();			
		}
		throw new IllegalArgumentException("Provided JList must be one of the TTV display lists");
	}
	
	public TreeNodePopupMenu getPopupMenu()
	{
		return popupMenu;
	}
}
