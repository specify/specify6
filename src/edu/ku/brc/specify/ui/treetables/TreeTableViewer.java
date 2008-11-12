/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
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
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.EditViewCompSwitcherPanel;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.tasks.DualViewSearchable;
import edu.ku.brc.specify.treeutils.ChildNodeCounter;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DragDropCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * The TreeTableViewer is a SubPaneIface implementation that provides a
 * JTree-like view/editor for tree-based data tables.  It should work
 * with any tree of objects implementing the Treeable interface and defined
 * by an object implementing the TreeDefinitionIface interface.
 *
 * @code_status Gamma
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeTableViewer <T extends Treeable<T,D,I>,
								D extends TreeDefIface<T,D,I>,
								I extends TreeDefItemIface<T,D,I>>
								extends BaseSubPane
								implements DragDropCallback, DualViewSearchable
{
    /** Logger for all messages emitted. */
    protected static final Logger log = Logger.getLogger(TreeTableViewer.class);
    
	private static final int SINGLE_VIEW_MODE = 0;
	private static final int DUAL_VIEW_MODE = 1;
	
	private static final boolean debugFind = false;
	
	private enum NODE_DROPTYPE {MOVE_NODE, SYNONIMIZE_NODE, CANCEL_DROP}
	
	/** Status message display widget. */
	protected JStatusBar statusBar;
	
	/** Model holding all <code>TreeNode</code> nodes. */
	protected TreeViewerListModel listModel;
	/** The tree display widget. */
	protected TreeDataGhostDropJList[] lists;
	/** The scroll panes that contains the lists. */
	protected JScrollPane[] scrollers;
	/** Cell renderer for displaying individual nodes in the tree. */
	protected TreeViewerNodeRenderer listCellRenderer;
	/** A header for the tree, displaying the names of the visible levels. */
	@SuppressWarnings("unchecked")
    protected TreeViewerListHeader[] listHeaders;
	
	protected JPanel[] treeListPanels;
    
    protected FindPanel findPanel;
	
	protected D treeDef;
	
    protected String  findName;
    protected int     resultsIndex;
    protected List<T> findResults;
    protected Integer lastFoundNodeId = null;
    
    protected boolean isInitialized;
    protected int mode;
    
    protected TreeNodePopupMenu popupMenu;
    
    protected boolean busy;
    protected String  busyReason;
    
    protected List<AbstractButton> allButtons;
    
    protected JButton subtree0;
    protected JButton wholeTree0;
    protected JButton toParent0;
    protected JButton syncViews0;
    protected JButton toggle0;
    protected JButton newChild0     = null;
    protected JButton editNode0     = null;
    protected JButton deleteNode0   = null;
    
    protected boolean isEditMode;
    
    protected JButton subtree1;
    protected JButton wholeTree1;
    protected JButton toParent1;
    protected JButton syncViews1;
    protected JButton toggle1;
    protected JButton newChild1     = null;
    protected JButton editNode1     = null;
    protected JButton deleteNode1   = null;
     
    protected TreeDataService<T,D,I> dataService;
    
    protected BusinessRulesIFace businessRules;
    
    protected Set<Integer> idsToReexpand = new HashSet<Integer>();
    
    protected boolean restoreTreeState = false;
    
    protected String selNodePrefName;
    
    protected Icon icon_split;
    protected Icon icon_single;
    
    protected boolean doUnlock = true;

    
    // tools to help figure the number of "related" records for a node in the background
    protected ExecutorService countGrabberExecutor;
    
	/**
	 * Build a TreeTableViewer to view/edit the data found.
	 * 
	 * @param treeDef handle to the tree to be displayed
	 * @param name a String name for this viewer/editor
	 * @param task the owning Taskable
	 */
	public TreeTableViewer( final D treeDef,
							final String name,
							final Taskable task,
							final boolean isEditMode)
	{
		super(name,task);
		
		this.isEditMode = isEditMode;
		this.treeDef = treeDef;
		allButtons = new Vector<AbstractButton>();
		statusBar = UIRegistry.getStatusBar();
		popupMenu = new TreeNodePopupMenu(this, isEditMode);
		
		getLayout().removeLayoutComponent(progressBarPanel);
		
        this.dataService = TreeDataServiceFactory.createService();
        
        businessRules = DBTableIdMgr.getInstance().getBusinessRule(treeDef.getNodeClass());
        
        // TODO: implement some UI to let the user set this pref
        restoreTreeState = AppPreferences.getRemote().getBoolean("TreeEditor.RestoreTreeExpansionState", false);
        
        selNodePrefName = "selected_node:" + treeDef.getClass().getSimpleName() + ":" + treeDef.getTreeDefId();
        
        countGrabberExecutor = Executors.newFixedThreadPool(10);
	}
	
	//XXX - move renumber and verify code somewhere else (possibly debug menu) or dump it.
	public void renumberNodes()
	{
//	    final NodeNumberer<T,D,I> nodeNumberer = new NodeNumberer<T,D,I>(treeDef);
//        final JStatusBar nStatusBar = UIRegistry.getStatusBar();
//        nStatusBar.setProgressRange(nodeNumberer.getProgressName(), 0, 100);
//        
//        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("Updating Tree Structure"), 24);
//        
//        nodeNumberer.addPropertyChangeListener(
//                new PropertyChangeListener() {
//                    public  void propertyChange(final PropertyChangeEvent evt) {
//                        if ("progress".equals(evt.getPropertyName())) 
//                        {
//                            nStatusBar.setValue(nodeNumberer.getProgressName(), (Integer)evt.getNewValue());
//                        }
//                    }
//                });
//        try
//        {
//            nodeNumberer.execute();
//            nodeNumberer.get();
//        }
//        catch (Exception ex)
//        {
//            System.out.println(ex);
//        }
//        
//        UIRegistry.clearSimpleGlassPaneMsg();
//        nStatusBar.setProgressDone(nodeNumberer.getProgressName());

	    try
	    {
	        treeDef.updateAllNodes(null);
	    }
	    catch (Exception ex)
	    {
	        System.out.println(ex);
	    }
	    
//        final NodeNumberVerifier<T,D,I> nodeVerifier = new NodeNumberVerifier<T,D,I>(treeDef);
//        nStatusBar.setProgressRange(nodeVerifier.getProgressName(), 0, 100);
//        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("Checking Tree Structure"), 24);
//        
//        nodeVerifier.addPropertyChangeListener(
//                new PropertyChangeListener() {
//                    public  void propertyChange(final PropertyChangeEvent evt) {
//                        if ("progress".equals(evt.getPropertyName())) 
//                        {
//                            nStatusBar.setValue(nodeNumberer.getProgressName(), (Integer)evt.getNewValue());
//                        }
//                    }
//                });
//        try
//        {
//            nodeVerifier.execute();
//            nodeVerifier.get();
//        }
//        catch (Exception ex)
//        {
//            System.out.println(ex);
//        }
//        
//        UIRegistry.clearSimpleGlassPaneMsg();
//        nStatusBar.setProgressDone(nodeVerifier.getProgressName());

	    
	}
	public D getTreeDef()
	{
		return this.treeDef;
	}
	
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
	 */
	@Override
	public void showingPane(final boolean show)
	{
		super.showingPane(show);
		if (show)
		{
			synchronized(this)
			{
				if (!isInitialized)
				{
					isInitialized = true;
					
					UIRegistry.writeGlassPaneMsg(getResourceString("TTV_OpeningTreeViewer"), 24);
		            SwingWorker bgThread = new SwingWorker()
		            {
		                @SuppressWarnings({ "unchecked", "synthetic-access" })
		                @Override
		                public Object construct()
		                {
		                    initTreeLists();
		                    
		                    return true;
		                }

		                @Override
		                public void finished()
		                {
		                    UIRegistry.clearGlassPaneMsg();
		                }
		            };
		            
		            bgThread.start();
				}
			}
		}
	}

    /**
     * Sets StatusBar Message.
     * @param text  the msg text
     */
    protected void setStatusBarText(final String key)
    {
        if (statusBar != null)
        {
            statusBar.setText(key == null ? "" : getResourceString(key));
        }
    }
    
    /**
     * Sets StatusBar Message.
     * @param text  the msg text
     */
    protected void setStatusBarText(final String key, final Object ... params)
    {
        if (statusBar != null)
        {
            statusBar.setText(key == null ? "" : String.format(getResourceString(key), params));
        }
    }
    
    
    /**
     * Sets simple text into the status bar.
     * @param text  the msg text
     */
    protected void setNameIntoStatusBarText(final String name)
    {
        if (statusBar != null)
        {
            statusBar.setText(name);
        }
    }
    

    /**
     * Calls showTree().  Simply provides an easy override point for subclasses.
     */
    protected void initTreeLists()
    {
        T rootRecord = dataService.getRootNode(treeDef);
        TreeNode rootNode = createNode(rootRecord);
        listModel = new TreeViewerListModel(rootNode);
        idsToReexpand.add(rootNode.getId());

        List<TreeNode> childNodes = showChildren(rootRecord);

        showTree();
        
        showCounts(rootRecord, childNodes);
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
		AppPreferences remotePrefs = AppPreferences.getRemote();
		
		Class<?> clazz = treeDef.getNodeClass();
		
		Color[] bgs = new Color[2];
		bgs[0] = remotePrefs.getColor("Treeeditor.TreeColColor1."+clazz.getSimpleName(), new Color(202, 238, 255));
		bgs[1] = remotePrefs.getColor("Treeeditor.TreeColColor2."+clazz.getSimpleName(), new Color(151, 221, 255));
		Color synonomyColor = remotePrefs.getColor("Treeeditor.SynonymyColor."+clazz.getSimpleName(), Color.BLUE);
		
        Color lineColor = new Color(0x00, 0x00, 0x00, 0x66);
		listCellRenderer = new TreeViewerNodeRenderer(this, listModel, bgs, lineColor, synonomyColor);
		ListSelectionListener listSelListener = new ListSelectionListener()
		{
			@SuppressWarnings("unchecked")
            public void valueChanged(ListSelectionEvent e)
			{
                if (e.getValueIsAdjusting())
                {
                    // ignore these events
                    return;
                }
                
                TreeDataGhostDropJList sourceList = (TreeDataGhostDropJList)e.getSource();
                TreeNode node = (TreeNode)sourceList.getSelectedValue();

				newNodeSelected(sourceList,node);
                listModel.setDropLocationNode(null);
			}
		};

		// setup both views
		lists          = new TreeDataGhostDropJList[2];
		scrollers      = new JScrollPane[2];
		listHeaders    = new TreeViewerListHeader[2];
		treeListPanels = new JPanel[2];
        
        int rowHeight = 20;
        
		lists[0] = new TreeDataGhostDropJList(listModel, this, isEditMode);
        
        // we need our MouseListener to be the first one called, so we detach the other MouseListeners, attach ours, then reattach the others.
        MouseListener[] mouseListeners = lists[0].getMouseListeners();
        for (MouseListener ml: mouseListeners)
        {
            lists[0].removeMouseListener(ml);
        }
		lists[0].addMouseListener(mouseListener);
        for (MouseListener ml: mouseListeners)
        {
            lists[0].addMouseListener(ml);
        }
        
		lists[0].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lists[0].setCellRenderer(listCellRenderer);
		lists[0].addListSelectionListener(listSelListener);
        lists[0].setFixedCellHeight(rowHeight);
		
		lists[1] = new TreeDataGhostDropJList(listModel, this, isEditMode);
        
        // we need our MouseListener to be the first one called, so we detach the other MouseListeners, attach ours, then reattach the others.
        mouseListeners = lists[1].getMouseListeners();
        for (MouseListener ml: mouseListeners)
        {
            lists[1].removeMouseListener(ml);
        }
        lists[1].addMouseListener(mouseListener);
        for (MouseListener ml: mouseListeners)
        {
            lists[1].addMouseListener(ml);
        }
        
		lists[1].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lists[1].setCellRenderer(listCellRenderer);
		lists[1].addListSelectionListener(listSelListener);
        lists[1].setFixedCellHeight(rowHeight);
        
        Map<Integer,String> rankNamesMap = new HashMap<Integer, String>();
        for (I defItem: treeDef.getTreeDefItems())
        {
            rankNamesMap.put(defItem.getRankId(), defItem.getName());
        }
        
		listHeaders[0] = new TreeViewerListHeader(lists[0],listModel,listCellRenderer,rankNamesMap);
		listHeaders[1] = new TreeViewerListHeader(lists[1],listModel,listCellRenderer,rankNamesMap);

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
        icon_split      = IconManager.getIcon("TTV_SplitViewMode",  IconManager.IconSize.Std16);
        icon_single     = IconManager.getIcon("TTV_SingleViewMode", IconManager.IconSize.Std16);
        
        // button panel for top tree list
        JPanel buttonPanel0 = new JPanel();
        buttonPanel0.setLayout(new BoxLayout(buttonPanel0,BoxLayout.PAGE_AXIS));
        
        subtree0 = new JButton(icon_subtree);
        subtree0.setSize(20,20);
        subtree0.setToolTipText(getResourceString("TTV_ZOOM_IN"));
        subtree0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showSubtreeOfSelection(lists[0]);
            }
        });
        
        wholeTree0 = new JButton(icon_wholeTree);
        wholeTree0.setSize(20,20);
        wholeTree0.setToolTipText(getResourceString("TTV_ZOOM_OUT"));
        wholeTree0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //showWholeTree(lists[0]);
                zoomOutOneLevel(lists[0]);
            }
        });
        wholeTree0.setEnabled(false);

        toParent0 = new JButton(icon_toParent);
        toParent0.setSize(20,20);
        toParent0.setToolTipText("Select Parent");
        toParent0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectParentOfSelection(lists[0]);
            }
        });
        
        syncViews0 = new JButton(icon_syncViews);
        syncViews0.setSize(20,20);
        syncViews0.setToolTipText(getResourceString("TTV_SYNC_WITH_BOTTOM"));
        syncViews0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                syncViewWithOtherView(lists[0]);
            }
        });
        syncViews0.setEnabled(false);
        
        toggle0 = new JButton(icon_split);
        toggle0.setSize(20,20);
        toggle0.setToolTipText(getResourceString("TTV_SPLIT_VIEW"));
        toggle0.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleViewMode();
            }
        });

        if (isEditMode)
        {
            newChild0 = new JButton(icon_newChild);
            newChild0.setSize(20,20);
            newChild0.setToolTipText("Add Child to Selection"); //XXX i18n
            newChild0.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    addChildToSelectedNode(lists[0]);
                }
            });
    
            editNode0 = new JButton(icon_editNode);
            editNode0.setSize(20,20);
            editNode0.setToolTipText("Edit Selected Node"); //XXX i18n
            editNode0.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    editSelectedNode(lists[0]);
                }
            });
    
            deleteNode0 = new JButton(icon_delNode);
            deleteNode0.setSize(20,20);
            //XXX i18n            
            deleteNode0.setToolTipText("Delete Selected Node"); 
            deleteNode0.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    deleteSelectedNode(lists[0]);
                }
            });
            
        }

        // view manipulation buttons
        JLabel viewLabel0 = createLabel(getResourceString("View"));
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
        if (isEditMode)
        {
            JLabel editLabel0 = createLabel(getResourceString("EDIT"));
            editLabel0.setSize(32,editLabel0.getHeight());
            buttonPanel0.add(editLabel0);
            editLabel0.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel0.add(editNode0);
            editNode0.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel0.add(newChild0);
            newChild0.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel0.add(deleteNode0);
            deleteNode0.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel0.add(Box.createVerticalGlue());
        }
        
        buttonPanel0.add(syncViews0);
        syncViews0.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        allButtons.add(subtree0);
        allButtons.add(wholeTree0);
        allButtons.add(toParent0);
        allButtons.add(toggle0);
        
        if (isEditMode)
        {
            allButtons.add(newChild0);
            allButtons.add(editNode0);
            allButtons.add(deleteNode0);
        }
 
		treeListPanels[0] = new JPanel();
		treeListPanels[0].setLayout(new BorderLayout());
		treeListPanels[0].add(scrollers[0], BorderLayout.CENTER);
		treeListPanels[0].add(buttonPanel0,BorderLayout.EAST);
        findPanel = new FindPanel(this, FindPanel.EXPANDED);
        treeListPanels[0].add(findPanel, BorderLayout.SOUTH);
        
        // button panel for bottom tree list
        JPanel buttonPanel1 = new JPanel();
        buttonPanel1.setLayout(new BoxLayout(buttonPanel1,BoxLayout.PAGE_AXIS));
        
        subtree1 = new JButton(icon_subtree);
        subtree1.setSize(20,20);
        subtree1.setToolTipText(getResourceString("TTV_ZOOM_IN"));
        subtree1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showSubtreeOfSelection(lists[1]);
            }
        });
        
        wholeTree1 = new JButton(icon_wholeTree);
        wholeTree1.setSize(20,20);
        wholeTree1.setToolTipText(getResourceString("TTV_ZOOM_OUT"));
        wholeTree1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //showWholeTree(lists[1]);
                zoomOutOneLevel(lists[1]);
            }
        });
        wholeTree1.setEnabled(false);

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
        syncViews1.setToolTipText(getResourceString("TTV_SYNC_WITH_TOP"));
        syncViews1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                syncViewWithOtherView(lists[1]);
            }
        });
        
        toggle1 = new JButton(icon_single);
        toggle1.setSize(20,20);
        toggle1.setToolTipText(getResourceString("TTV_SINGLE_VIEW"));
        toggle1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleViewMode();
            }
        });

        if (isEditMode)
        {
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
            
        }
        
        // view manipulation buttons
        JLabel viewLabel1 = createLabel("View");
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
        
        if (isEditMode)
        {
            // tree editing buttons
            JLabel editLabel1 = createLabel("Edit");
            editLabel1.setSize(32,editLabel1.getHeight());
            buttonPanel1.add(editLabel1);
            editLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel1.add(editNode1);
            editNode1.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel1.add(newChild1);
            newChild1.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel1.add(deleteNode1);
            deleteNode1.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel1.add(Box.createVerticalGlue());
        }

        buttonPanel1.add(syncViews1);
        syncViews1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        allButtons.add(subtree1);
        allButtons.add(wholeTree1);
        allButtons.add(toParent1);
        allButtons.add(toggle1);
        
        if (isEditMode)
        {
            allButtons.add(newChild1);
            allButtons.add(editNode1);
            allButtons.add(deleteNode1);
        }
 
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
        
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, "Find");
        this.getActionMap().put("Find", findAction);
        
        lists[0].getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, findAction);
        lists[0].getActionMap().put("Find", findAction);
        lists[1].getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, findAction);
        lists[1].getActionMap().put("Find", findAction);
        
        // force the selection-sensitive buttons to initialize properly
        newNodeSelected(lists[0],null);
        newNodeSelected(lists[1],null);
        
        AppPreferences prefs = AppPreferences.getRemote();
        if (prefs != null)
        {
            String selectedNodeId = prefs.get(selNodePrefName, "null");
            if (!selectedNodeId.equals("null"))
            {
                Integer nodeId = null;
                try
                {
                    nodeId = Integer.parseInt(selectedNodeId);
                }
                catch (NumberFormatException nfe)
                {
                    nodeId = null;
                    log.warn("'selected_node' preference contained unparsable value.  Removing value.");
                    prefs.remove(selNodePrefName);
                }
                if (nodeId != null)
                {
                    try
                    {
                        T nodeRecord = dataService.getNodeById(treeDef.getNodeClass(), nodeId);
                        showPathToNode(nodeRecord);
                        TreeNode node = listModel.getNodeById(nodeRecord.getTreeId());
                        lists[0].setSelectedValue(node, true);
                        lists[0].setSelectedValue(node, true);
                        scrollToShowNode(node, 0);
                        //log.info("Showing and selecting previously selected node: " + nodeRecord.getFullName());
                    }
                    catch (Exception e)
                    {
                        log.warn("Failed to show and select previously selected node");
                    }
                }
            }
        }
        
        UIRegistry.forceTopFrameRepaint();
        
        MouseMotionAdapter tooltipRendererListener = new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                Point p = e.getPoint();
                JList sourceList = (JList)e.getSource();
                
                int cellIndex = sourceList.locationToIndex(p);
                if (cellIndex == -1)
                {
                    return;
                }
                
                TreeNode nodeUnderMouse = listModel.getElementAt(cellIndex);
                if (nodeUnderMouse == null)
                {
                    return;
                }
                
                Pair<Integer,Integer> textBounds = listCellRenderer.getTextBoundsForRank(nodeUnderMouse.getRank());
                if (textBounds == null)
                {
                    return;
                }
                
                if (p.x >= textBounds.first && p.x <= textBounds.second)
                {
                    listCellRenderer.setRenderTooltip(true);
                }
                else
                {
                    listCellRenderer.setRenderTooltip(false);
                }
            }
        };
        
        lists[0].addMouseMotionListener(tooltipRendererListener);
        lists[1].addMouseMotionListener(tooltipRendererListener);
	}
    
    /**
     * 
     */
    public void updateAllUI()
    {
        listModel.layoutChanged();
        
//        listHeaders[0].invalidate();
//        listHeaders[0].repaint();
//        listHeaders[1].invalidate();
//        listHeaders[1].repaint();
//        
//        lists[0].invalidate();
//        lists[0].repaint();
//        lists[1].invalidate();
//        lists[1].repaint();
//        
//        scrollers[0].invalidate();
//        scrollers[0].repaint();
//        scrollers[1].invalidate();
//        scrollers[1].invalidate();
//        
//        invalidate();
//        repaint();        
    }
	
	/**
	 * 
	 */
	protected void toggleViewMode()
	{
		if (mode == SINGLE_VIEW_MODE)
		{
			setViewMode(DUAL_VIEW_MODE);
		}
		else
		{
			setViewMode(SINGLE_VIEW_MODE);
		}
        
        syncViews0.setEnabled(mode == DUAL_VIEW_MODE);
        syncViews1.setEnabled(mode == DUAL_VIEW_MODE);
		
        updateAllUI();
    	UIRegistry.forceTopFrameRepaint();
	}
    
	/**
	 * @param newMode
	 */
	protected void setViewMode(final int newMode)
	{
		removeAll();
		mode = newMode;
		if (mode == SINGLE_VIEW_MODE)
		{
			// set to single view mode
			this.add(treeListPanels[0],BorderLayout.CENTER);
            toggle0.setToolTipText(getResourceString("TTV_SPLIT_VIEW"));
            toggle0.setIcon(icon_split);
		}
		else
		{
			// set to dual view mode
			this.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,treeListPanels[0],treeListPanels[1]),BorderLayout.CENTER);
            toggle0.setToolTipText(getResourceString("TTV_SINGLE_VIEW"));
            toggle0.setIcon(icon_single);
		}
        updateAllUI();
	}
    
	/**
	 * @param list
	 */
	protected void syncViewWithOtherView(final JList list)
	{
		if (list == lists[0])
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
	 * created if the user chooses to proceed with the data entry.
	 */
	@SuppressWarnings("unchecked")
    public void addChildToSelectedNode(final JList list)
	{
		Object selection = list.getSelectedValue();
		if ( selection == null )
		{
			return;
		}
		
        TreeNode parent = (TreeNode)selection;
        T parentRecord = getRecordForNode(parent);
		I parentDefItem = parentRecord.getDefinitionItem();
		if ( parentDefItem.getChild() == null )
		{
			log.info("Cannot add child node below this rank");
            String msg = getResourceString("TTV_NO_KIDS_ADD_BELOW");
            statusBar.setErrorMessage(msg);
            UIRegistry.displayErrorDlg(msg);
			return;
		}

		// setup a bunch of links that are needed for the form to correctly display the new node
		T newT = (T)TreeFactory.createNewTreeable(parentRecord.getClass(),"");
		newT.setDefinitionItem(parentDefItem.getChild());
		newT.setDefinition(parentRecord.getDefinition());
		newT.setParent(parentRecord);

        statusBar.setText(null);
		// display a form for filling in child data
		showEditDialog(newT, "New Node Form", true);
	}

	public void unSynSelectedNode(final JList list)
	{
        // get the selected TreeNode
        Object selection = list.getSelectedValue();
        if ( selection == null )
        {
            return;
        }
        TreeNode node = (TreeNode)selection;
        TreeNode acceptedNode = ((TreeViewerListModel )list.getModel()).getNodeById(node.getAcceptedParentId());
        
        T nodeRecord = getRecordForNode(node);
        
        log.debug("Node selected for Un-synonymize: " + node);

        String statusMsg = dataService.unSynonymize(nodeRecord);
        node.setAcceptedParentId(null);
        node.setAcceptedParentFullName(null);
        
        T synParent = nodeRecord.getParent();
        T acceptedNodeParent = null;
        
        node.setCalcCount(false);
        node.setCalcCount2(false);
        node.setHasCalcCount(false);
        node.setHasCalcCount2(false);
        if (acceptedNode != null)
        {
            acceptedNode.setCalcCount(false);
            acceptedNode.setCalcCount2(false);
            acceptedNode.setHasCalcCount(false);
            acceptedNode.setHasCalcCount2(false);
            
            T acceptedRecord = getRecordForNode(acceptedNode);
            acceptedNodeParent = acceptedRecord.getParent();
            
            acceptedNode.removeSynonym(node.getId());
        }
        
        
        Vector<TreeNode> draggedChildren = new Vector<TreeNode>();
        Vector<TreeNode> droppedChildren = new Vector<TreeNode>();
        if (acceptedNodeParent == null || synParent.getTreeId().equals(acceptedNodeParent.getTreeId()))
        {
            draggedChildren.add(node);
            if (acceptedNodeParent != null)
            draggedChildren.add(acceptedNode);
            showCounts(synParent, draggedChildren);
        }
        else if (acceptedNodeParent != null)
        {
            draggedChildren.add(node);
            droppedChildren.add(acceptedNode);
            showCounts(synParent, draggedChildren);
            showCounts(acceptedNodeParent, droppedChildren);
        }
        
        UIRegistry.displayStatusBarText(statusMsg);
        updateAllUI();
        
	}
	
	
	/**
	 * Deletes the currently selected node and all descendants if and
	 * only if it is determined possible without violating any business
	 * rules.
	 */
    public void deleteSelectedNode(final JList list)
	{
        // get the selected TreeNode
		Object selection = list.getSelectedValue();
		if ( selection == null )
		{
			return;
		}
        TreeNode node = (TreeNode)selection;
        
        // get the DB record that corresponds to the TreeNode
        T nodeRecord = getRecordForNode(node);
        int numNodesToDelete = dataService.getDescendantCount(nodeRecord)+1;
        int userChoice = JOptionPane.OK_OPTION;
        
        // if this node has children, ask the user if it is okay to delete multiple nodes
        if (numNodesToDelete > 1)
        {
            String msg = String.format(getResourceString("TTV_CONFIRM_DELETE"), numNodesToDelete);
            userChoice = JOptionPane.showConfirmDialog(this, msg, 
                    getResourceString("TTV_CONFIRM_DELETE_TITLE"), 
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
        }
        
        if (userChoice == JOptionPane.OK_OPTION)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Deleting"), 24);
            
            TreeNode parent = listModel.getNodeById(node.getParentId());
            
            // hide the children of the parent node (which will hide the node we're going to delete)
            hideChildren(parent);
            
            // delete the record from the DB
            if (dataService.deleteTreeNode(nodeRecord))
            {
                setStatusBarText("TTV_NODES_DELETED", numNodesToDelete);
            }
            else
            {
                statusBar.setErrorMessage(getResourceString("TTV_PROBLEM_DELETING"));
            }

            // re-show the children of the parent node
            showChildren(parent);
            
            // remove the glasspane overlay text
            UIRegistry.clearGlassPaneMsg();
        }
	}
    
    /**
     * @param node
     */
    public void initializeNodeAssociations(final T node)
    {
        dataService.initializeRelatedObjects(node);
    }
	
	/**
	 * Display a form for editing the data in the currently selected node.
	 */
	@SuppressWarnings("unchecked")
	public void editSelectedNode(final JList list)
	{
		Object selection = list.getSelectedValue();
		if ( selection == null )
		{
			return;
		}
		
        T nodeRecord = getRecordForNode((TreeNode)selection);
		showEditDialog(nodeRecord, "Edit Node Values", false);
	}

	/**
	 * Sets the visibleRoot property of the tree to the currently selected node.  This provides
	 * the ability to "zoom in" to a lower level of the tree.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void showSubtreeOfSelection(final JList list)
	{
        TreeNode selectedNode = (TreeNode)list.getSelectedValue();
		if ( selectedNode == null )
		{
			return;
		}

		listModel.setVisibleRoot(selectedNode);
		
        list.setSelectedValue(selectedNode,true);
        list.setSelectedValue(selectedNode,true);
        
        wholeTree0.setEnabled(true);
        wholeTree1.setEnabled(true);
        
        updateAllUI();
	}
	
    /**
     * @param list
     */
    public synchronized void zoomOutOneLevel(final JList list)
    {
        TreeNode selectedNode = (TreeNode)list.getSelectedValue();
        
        TreeNode visibleRoot = listModel.getVisibleRoot();
        
        TreeNode parentNode = listModel.getNodeById(visibleRoot.getParentId());
        
        listModel.setVisibleRoot(parentNode);
        
        // I doubled this call b/c Swing wasn't doing this unless I put it in here twice
        list.setSelectedValue(selectedNode, true);
        list.setSelectedValue(selectedNode, true);
        
        if (listModel.getVisibleRoot().getRank() == 0)
        {
            wholeTree0.setEnabled(false);
            wholeTree1.setEnabled(false);
        }
        
        updateAllUI();
    }
    
	/**
	 * Sets the visibleRoot property to the actual root of the tree.  This results in the 
	 * entire tree being made available to the user.
	 */
	public synchronized void showWholeTree(final JList list)
	{
		Object selection = list.getSelectedValue();		

		listModel.setVisibleRoot(listModel.getRoot());
		
		if ( selection != null )
		{
		    // I doubled this call b/c Swing wasn't doing this unless I put it in here twice
            list.setSelectedValue(selection,true);
            list.setSelectedValue(selection,true);
		}
	}

	/**
	 * @param list
	 */
	@SuppressWarnings("unchecked")
	public void selectParentOfSelection(final TreeDataGhostDropJList list)
	{
		Object selection = list.getSelectedValue();
		if ( selection == null )
		{
			return;
		}
		
        TreeNode node = (TreeNode)selection;
        int parentId = node.getParentId();
        if (parentId != node.getId())
        {
            TreeNode parentNode = listModel.getNodeById(parentId);
            
            // I doubled this call b/c Swing wasn't doing this unless I put it in here twice
            list.setClickOnText(true);
            list.setSelectedValue(parentNode,true);
            list.setSelectedValue(parentNode,true);
            list.setClickOnText(false);
            
            int listIndex = (list == lists[0]) ? 0 : 1;
            scrollToShowNode(parentNode, listIndex);
        }
	}
			
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.DualViewSearchable#find(java.lang.String, int, boolean)
	 */
	public void find(final String nodeName, final int where, final boolean wrap)
	{
        setStatusBarText(null);
		findName    = nodeName;
		findResults = dataService.findByName(treeDef, nodeName);
		if (findResults.isEmpty())
		{
			//TODO: notify the user that no results were found
			log.info("Search for '"+nodeName+"' returned no results");
			setStatusBarText("TTV_FIND_NO_RESULTS", nodeName);
			return;
		}
		
		findPanel.getNextButton().setEnabled(findResults.size() > 1);
		
		T firstMatch = findResults.get(0);
		resultsIndex = 0;
		showPathToNodeForFind(firstMatch);
		
		final TreeNode firstMatchNode = listModel.getNodeById(firstMatch.getTreeId());
		
		if (firstMatchNode != null)
		{
            lastFoundNodeId = firstMatchNode.getId();
    		if ((where & DualViewSearchable.TOPVIEW) != 0)
    		{
    		    setSelectedNode(0, firstMatchNode);
                scrollToShowNode(firstMatchNode, 0);
                
    		}
    		
    		if ((where & DualViewSearchable.BOTTOMVIEW) != 0)
    		{
                if (mode == SINGLE_VIEW_MODE)
                {
                    toggleViewMode();
                }
                setSelectedNode(1, firstMatchNode);
                
                scrollToShowNode(firstMatchNode, 1);
            }
		} else
		{
		    // We only get here is something serious has gone wrong
		    setStatusBarText("TTV_FIND_NO_RESULTS", nodeName);
		}
	}
    
	protected void scrollToShowNode(@SuppressWarnings("unused") final TreeNode node, 
	                                @SuppressWarnings("unused") final int listIndex)
	{
//	    int nodeIndex = listModel.indexOf(node);
//	    if (nodeIndex != -1)
//	    {
//	        Rectangle listCellBounds = lists[listIndex].getCellBounds(nodeIndex, nodeIndex);
//	        Pair<Integer, Integer> textBounds = listCellRenderer.getTextBoundsForRank(node.getRank());
//	        if (textBounds != null)
//	        {
//	            Rectangle textRectangle = new Rectangle();
//	            textRectangle.x = textBounds.first;
//	            textRectangle.y = listCellBounds.y;
//	            textRectangle.width = textBounds.second - textBounds.first;
//	            textRectangle.height = listCellBounds.height;
//
//	            scrollers[listIndex].scrollRectToVisible(textRectangle);
//	            scrollers[listIndex].getViewport().scrollRectToVisible(textRectangle);
//	        }
//	    }
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.DualViewSearchable#findNext(java.lang.String, int, boolean)
	 */
	public void findNext(final String key, final int where, final boolean wrap)
	{
        setStatusBarText(null);

		if (key != null && !key.equals(findName))
		{
			find(key, where, wrap);
			return;
		}
		
		if (findResults != null && findResults.size() > 0 && findName != null)
		{
			log.info("Searching for next node from previous search: " + findName);
			
			// find the next node from the previous search
			if ((findResults.size() - 1) == resultsIndex && !wrap)
			{
				setStatusBarText("TTV_FIND_NO_RESULTS", key);
				return;
			}
			
			resultsIndex = (resultsIndex + 1) % findResults.size();
			T nextNode = findResults.get(resultsIndex);
			
			showPathToNodeForFind(nextNode);
			
            TreeNode nextMatchNode = listModel.getNodeById(nextNode.getTreeId());
            
			if ((where & DualViewSearchable.TOPVIEW) != 0)
			{
			    setSelectedNode(0, nextMatchNode);
                
                scrollToShowNode(nextMatchNode, 0);
			}
			
			if ((where & DualViewSearchable.BOTTOMVIEW) != 0)
			{
                if (mode == SINGLE_VIEW_MODE)
                {
                    toggleViewMode();
                }
                
                setSelectedNode(1, nextMatchNode);
                scrollToShowNode(nextMatchNode, 1);
            }
		}
	}
	
	/**
	 * @param where
	 * @param wrap
	 * @param current
	 */
	public void findNext(final int where, final boolean wrap, final T current)
	{
        setStatusBarText(null);

		List<T> matches = dataService.findByName(treeDef, current.getName());
		if (matches.size()==1)
		{
			setStatusBarText("TTV_FIND_NO_MORE_MATCHES");
			return;
		}
		
        int curIndex = -1;
        for (int i = 0; i < matches.size(); ++i)
        {
            T match = matches.get(i);
            if (match.getTreeId().equals(current.getTreeId()))
            {
                curIndex = i;
            }
        }
        
		if (!wrap && curIndex == matches.size()-1)
		{
		    setStatusBarText("TTV_FIND_NO_MORE_MATCHES");
			return;
		}
        
        T nextNode = matches.get((curIndex + 1)%matches.size());

        showPathToNodeForFind(nextNode);
        
        TreeNode nextMatchNode = listModel.getNodeById(nextNode.getTreeId());
        
		if ((where & DualViewSearchable.TOPVIEW) != 0)
		{
		    setSelectedNode(0, nextMatchNode);
            
            scrollToShowNode(nextMatchNode, 0);
		}
		if ((where & DualViewSearchable.BOTTOMVIEW) != 0)
		{
            if (mode == SINGLE_VIEW_MODE)
            {
                toggleViewMode();
            }
            setSelectedNode(1, nextMatchNode);
            scrollToShowNode(nextMatchNode, 1);
        }
	}
	
	/**
	 * Selects the node in the given list.
	 * @param listInx the index to the list to use
	 * @param node the node to select.
	 */
	protected void setSelectedNode(final int listInx, final TreeNode node)
	{
	    // I doubled this call b/c Swing wasn't doing this unless I put it in here twice - JDS
        lists[listInx].setClickOnText(true);
        lists[listInx].setSelectedValue(node, true);
        lists[listInx].setSelectedValue(node, true);
        lists[listInx].setClickOnText(false);
	}
	
	/**
	 * @param where
	 * @param wrap
	 * @param currentNode
	 */
	public void findNext(final JList where, final boolean wrap, final TreeNode currentNode)
	{
        setStatusBarText(null);

        T currentRecord = getRecordForNode(currentNode);
        
		if (where == lists[0])
		{
			findNext(DualViewSearchable.TOPVIEW,wrap,currentRecord);
		}
		else if (where == lists[1])
		{
			findNext(DualViewSearchable.BOTTOMVIEW,wrap,currentRecord);
		}
		else
		{
			// throw new IllegalArgumentException?
		}
    }

    /**
     * This looks down the list model and the pathToNode and figures which node in the path
     * was the last one to be visible.
     * @param pathToNode the path to the node
     * @param visRoot the visible root.
     * @return the last node that was visible from the pathToNode
     */
    protected T findLastVisibleNode(final List<T> pathToNode, final TreeNode visRoot)
    {
	    T visNode = null;
	    
	    // Start by looking for the visual root Node in the returned list
	    // It should always find it as the first node.
	    int visNodeIndex = 0;
	    for (T node : pathToNode)
	    {
	        if (node.getTreeId().intValue() == visRoot.getId())
	        {
	            visNode = node;
	            break;
	        }
	        visNodeIndex++;
	    }

	    // Now that we have the Visible Root start walking the list of a Ancestors (pathToNode)
	    // and the visual data model until we can't find a match.
	    if (visNode != null)
	    {
	        int lastFndIndex = -1;
	        int startIndex   = listModel.indexOf(visRoot); 
	        for (int i=visNodeIndex;i<pathToNode.size();i++)
	        {
	            boolean fnd = false;
	            for (int j=startIndex;j<listModel.getSize();j++)
	            {
	                if (debugFind) System.err.println("["+pathToNode.get(i).getFullName()+"]["+listModel.getElementAt(j).getFullName()+"]");
	                
	                if (pathToNode.get(i).getTreeId().intValue() == listModel.getElementAt(j).getId())
	                {
	                    fnd        = true;
	                    startIndex = j+1;
	                    break;
	                }
	            }
	            
	            // if this is true then we found a node in the pathToNode that isn't
	            // visible, yet so return that first node that wasn't found
	            if (!fnd)
	            {
	                if (debugFind) System.err.println("Last Node that was visible: "+pathToNode.get(i-1).getFullName());
	                lastFndIndex = i;
	                return pathToNode.get(i-1);
	            }
	        }

	        // if this is true then we got all the way to the end and they were
	        // visible in the tree
	        if (lastFndIndex == -1)
	        {
	            if (debugFind) System.err.println("*** Last Node that was visible: "+pathToNode.get(pathToNode.size()-1).getFullName()); 
	            return pathToNode.get(pathToNode.size()-1);
	        }
	    }
	    
	    // This means something bad happened and we couldn't find anything visible.
	    return null;
	}

	/**
	 * @param node
	 * @return
	 */
	protected boolean showPathToNodeForFind(final T node)
	{
	    if (debugFind) System.err.println("showPathToNodeForFind "+node.getFullName());
	    
	    // Get the list of parents from the found node up to the root
	    List<T> pathToNode = node.getAllAncestors();
	    
	    // Now add the 'found' node as the last node in the list
	    List<T> fullList = new Vector<T>(pathToNode);
	    fullList.add(node);
	    
	    // Check the list of nodes from the found node to against the
	    // visual list model to see of the entire list of node is visible
	    T lastVisiblenode = findLastVisibleNode(fullList, listModel.getVisibleRoot());
	    if (node == lastVisiblenode)
	    {
	        // This means the 'found' node was visible
	        return false;
	    }

	    // Now expose the rest of the nodes from the last one that was visible
	    // down to the 'found' node
	    if (lastVisiblenode != null)
	    {
	        for( int i = pathToNode.indexOf(lastVisiblenode); i < pathToNode.size(); ++i )
    	    {
    	        T pathRecord = pathToNode.get(i);
    	        List<TreeNode> childNodes = showChildren(pathRecord);
    	        showCounts(pathRecord, childNodes);
    	    }
	    }
	    return true;
	}

	/**
	 * @param node
	 * @return
	 */
	protected boolean showPathToNode(final T node)
	{
	    //System.out.println("Showing Path to "+node.getFullName());
	    
		List<T> pathToNode = node.getAllAncestors();
		TreeNode visRoot = listModel.getVisibleRoot();
        
        boolean pathContainsVisRoot = false;
        T       visRootRecord = null;
        for (T pathItem: pathToNode)
        {
            if (pathItem.getTreeId().intValue() == visRoot.getId())
            {
                pathContainsVisRoot = true;
                visRootRecord = pathItem;
                break;
            }
        }
		if (!pathContainsVisRoot)
		{
			return false;
		}
		
		for( int i = pathToNode.indexOf(visRootRecord); i < pathToNode.size(); ++i )
		{
            T pathRecord = pathToNode.get(i);
            List<TreeNode> childNodes = showChildren(pathRecord);
            showCounts(pathRecord, childNodes);

		}
		return true;
	}
	
	/**
	 * Display the form for editing node data.
	 *
	 * @param node the node being edited
	 * @param title the title of the dialog window
     * @param isNewObject indicates that the object is new
     */
    protected void showEditDialog(final T node, final String title, final boolean isNewObject)
	{
	    // TODO: double check these choices
	    // gather all the info needed to create a form in a dialog
	    String      viewName      = TreeFactory.getAppropriateViewName(node);
		Frame       parentFrame   = (Frame)UIRegistry.get(UIRegistry.FRAME);
		String      displayName   = "NODE_EDIT_DISPLAY_NAME";
        boolean     isEdit        = true;
		String      closeBtnText  = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE");
		String      className     = node.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(node.getTableId());
		String      idFieldName   = nodeTableInfo.getIdFieldName();
		int         options       = MultiView.HIDE_SAVE_BTN;
		if (isNewObject)
		{
		    options |= MultiView.IS_NEW_OBJECT;
		}
		
		// create the form dialog
		ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, title, 
		                                                           closeBtnText, className, idFieldName, isEdit, options);
		dialog.setModal(true);
		dialog.setData(node);
		
		// build the dialog UI so I can adjust some of the controls
		dialog.preCreateUI();
		for (Viewable viewable: dialog.getMultiView().getViewables())
		{
		    if (viewable instanceof FormViewObj)
		    {
                FormViewObj form = (FormViewObj)viewable;
                UIValidator.setIgnoreAllValidation(this, true);
		        
                if (isNewObject)
                {
                    Component parentComp = form.getControlByName("parent");
                    
                    if (parentComp != null)
                    {
                        while (!(parentComp instanceof EditViewCompSwitcherPanel) && parentComp != null)
                        {
                            parentComp = parentComp.getParent();
                        }
                        if (parentComp != null)
                        {
                            ((EditViewCompSwitcherPanel)parentComp).putIntoViewMode();
                        }
                    }
                }
                UIValidator.setIgnoreAllValidation(this, false);
		    }
		}
		
        // note some node values so we can see if they change
		String nodeNameBefore               = node.getName();
		Integer parentIdBefore              = (node.getParent() != null) ? node.getParent().getTreeId() : null;
        final Integer acceptedNodeIdBefore  = (node.getAcceptedParent() != null) ? node.getAcceptedParent().getTreeId() : null;
		
        // NOTE: Because of when the combobox gets set we need to call the validatSr here
        // so the it all get validated correctly.
        dialog.getMultiView().getCurrentView().getValidator().validateForm();
        
		dialog.pack();
		// show the dialog (which allows all user edits to happen)
		dialog.setVisible(true);
		
		// see what important stuff was modified
		String nodeNameAfter          = node.getName();
		Integer parentIdAfter         = (node.getParent() != null) ? node.getParent().getTreeId() : null;
        final boolean acceptedAfter   = (node.getIsAccepted() != null) ? node.getIsAccepted() : true;
		final boolean nameChanged     = !nodeNameBefore.equals(nodeNameAfter);
		boolean parentChanged         = (parentIdBefore == null && parentIdAfter != null) ||
		                                (parentIdBefore != null && parentIdAfter == null) ||
		                                (parentIdBefore != null && parentIdAfter != null && parentIdBefore.longValue() != parentIdAfter.longValue());

		log.debug("nameChange:    " + nameChanged);
        log.debug("parentChanged: " + parentChanged);
        log.debug("acceptedAfter: " + acceptedAfter);
        log.debug("acceptedNodeIdBefore: " + acceptedNodeIdBefore);
		
		// the dialog has been dismissed by the user
		if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
		{
		    final FormViewObj fvo = dialog.getMultiView().getCurrentViewAsFormViewObj();
		    if (fvo != null)
		    {
		        fvo.traverseToGetDataFromForms();
		    }
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24);
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                boolean afterSaveSuccess;
                T mergedNode;
                
                @SuppressWarnings({ "unchecked", "synthetic-access" })
                @Override
                public Object construct()
                {
                    if (true)
                    {
                        success          = fvo.saveObject();
                        afterSaveSuccess = success;
                        mergedNode       = (T)fvo.getDataObj();
                        
                    } else
                    {
                        // save the node and update the tree viewer appropriately
                        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                        try
                        {
                            mergedNode = session.merge(node);
                        }
                        catch (StaleObjectException e1)
                        {
                            // another user or process has changed the data "underneath" us
                            UIRegistry.showLocalizedError("UPDATE_DATA_STALE");
                            if (session != null)
                            {
                                session.close();
                            }
                            session = DataProviderFactory.getInstance().createSession();
                            mergedNode = (T)session.load(node.getClass(), node.getTreeId());
                            success = false;
                            return success;
                        }
                        success = true;
                        afterSaveSuccess = false;
                        
                        if (businessRules != null)
                        {
                            businessRules.beforeSave(mergedNode, session);
                        }
                        
                        try
                        {
                            session.beginTransaction();
                            
                            MultiView mvParent = fvo.getMVParent();
                            Vector<Object> deletedItems = mvParent != null ? mvParent.getDeletedItems() : null;
                            if (deletedItems != null)
                            {
                                // Ok, at this point we have all of the deleted data objects,
                                // but Josh has already done a merge. So we need to used the record Id
                                // and build a new list with the merged objects.
                                
                                Hashtable<Integer, Boolean> idHash = new Hashtable<Integer, Boolean>();
                                for (Object obj : deletedItems)
                                {
                                    Integer idInt = ((FormDataObjIFace)obj).getId(); // should never be null, but just in case
                                    if (idInt != null)
                                    {
                                        int id = idInt;
                                        idHash.put(id, true);
                                    }
                                    
                                }
                                
                                FormViewObj.deleteItemsInDelList(session, deletedItems);
                            }
                            
                            Vector<Object> toBeSavedItems = mvParent != null ? mvParent.getToBeSavedItems() : null;
                            if (toBeSavedItems != null)
                            {
                                FormViewObj.saveItemsInToBeSavedList(session, toBeSavedItems);
                            }
                            
                            session.saveOrUpdate(mergedNode);
                            if (businessRules != null)
                            {
                                if (!businessRules.beforeSaveCommit(mergedNode, session))
                                {
                                    throw new Exception("Business rules processing failed");
                                }
                            }
                            session.commit();
                            //log.info("Successfully saved changes to " + mergedNode.getFullName());
                        }
                        catch (Exception e)
                        {
                            success = false;
                            UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR");
    
                            log.error("Error while saving node changes.  Rolling back transaction.", e);
                            session.rollback();
                        }
                        finally
                        {
                            session.close();
                        }
                        
                        // at this point, the new node is in the DB (if success == true)
    
                        if (businessRules != null && success == true)
                        {
                            afterSaveSuccess = businessRules.afterSaveCommit(mergedNode);
                        }
                    }
                    return success;
                }

                @Override
                public void finished()
                {
                    // now refresh the tree viewer
                    if (success)
                    {
                        TreeNode editedNode = listModel.getNodeById(mergedNode.getTreeId());
                        
                        if (isNewObject)
                        {
                            // show the children of the 
                            T parent = mergedNode.getParent();
                            if (parent != null)
                            {
                                TreeNode parentNode = listModel.getNodeById(parent.getTreeId());
                                parentNode.setHasChildren(true);
                                hideChildren(parentNode);
                                List<TreeNode> childNodes = showChildren(parent);
                                showCounts(parent, childNodes);
                            }
                        }
                        else
                        {
                            // this was an existing node being edited
                            editedNode.setName(mergedNode.getName());
                            editedNode.setFullName(mergedNode.getFullName());
                            editedNode.setRank(mergedNode.getRankId());
                            listModel.nodeValuesChanged(editedNode);
                        }
                        
                        if (acceptedAfter && acceptedNodeIdBefore != null)
                        {
                            // This node used to be synonymized to another node, but now it isn't.
                            // Update the UI of both nodes.
                            TreeNode acceptedParentBefore = listModel.getNodeById(acceptedNodeIdBefore);
                            if (acceptedParentBefore != null)
                            {
                                acceptedParentBefore.removeSynonym(editedNode.getId());
                                editedNode.setAcceptedParentFullName(null);
                                editedNode.setAcceptedParentId(null);
                                listModel.nodeValuesChanged(acceptedParentBefore);
                                listModel.nodeValuesChanged(editedNode);
                            }
                        }
                        
                        if (!afterSaveSuccess)
                        {
                            String msg = getResourceString("TTV_METADATA_PROBLEM");
                            UIRegistry.displayErrorDlg(msg);
                            statusBar.setErrorMessage(msg);
                        }
                    }
                    else
                    {
                        String msg = getResourceString("TTV_TRANS_PROBLEM");
                        UIRegistry.displayErrorDlg(msg);
                        statusBar.setErrorMessage(msg);
                    }
                    
                    UIRegistry.clearGlassPaneMsg();
                 
                    updateAllUI();
                }
            };
            
            bgThread.start();
		}
		else
		{
		    // the user didn't save any edits (if there were any)
		}
	}
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getUIComponent()
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
	 * @param t the newly selected TreeNode
	 */
	@SuppressWarnings("unchecked")
	protected void newNodeSelected(final JList sourceList, final TreeNode selectedNode)
	{
        //log.debug("newNodeSelected()  selectedNode: " + selectedNode);
        
        // clear the status bar if nothing is selected or show the fullname if a node is selected
        String statusBarText = null;
        if ( selectedNode != null)
        {
            statusBarText = selectedNode.getFullName();
        }
        
        setNameIntoStatusBarText(statusBarText);

        boolean nonNullSelection = (selectedNode != null);
        boolean canAddChild      = (selectedNode != null) ? (selectedNode.getRank() < getHighestPossibleNodeRank()) : false;
        boolean isVisibleRoot    = (selectedNode != null) ? (selectedNode.getId() == listModel.getVisibleRoot().getId()) : false;

        popupMenu.setSelectionSensativeButtonsEnabled(nonNullSelection);
        
        // disable the buttons so the user can't click them until the background task verifies if they should be enabled
        if (sourceList == lists[0])
        {
            popupMenu.setNewEnabled(canAddChild && nonNullSelection);
            if (isEditMode)
            {
                newChild0.setEnabled(canAddChild && nonNullSelection);
                editNode0.setEnabled(nonNullSelection);
            }
            subtree0.setEnabled(!isVisibleRoot && nonNullSelection);
            toParent0.setEnabled(!isVisibleRoot && nonNullSelection);

            // turn these off until the bg thread can find out if user can delete this node
            if (isEditMode)
            {
                deleteNode0.setEnabled(false);
            }
            popupMenu.setDeleteEnabled(false);
        }
        else
        {
            popupMenu.setNewEnabled(canAddChild && nonNullSelection); 
            if (isEditMode)
            {
                newChild1.setEnabled(canAddChild && nonNullSelection);
                editNode1.setEnabled(nonNullSelection);
            }
            subtree1.setEnabled(!isVisibleRoot && nonNullSelection);
            toParent1.setEnabled(!isVisibleRoot && nonNullSelection);
            
            // turn these off until the bg thread can find out if user can delete this node
            if (isEditMode)
            {
                deleteNode1.setEnabled(false);
            }
            popupMenu.setDeleteEnabled(false);
        }

        popupMenu.setUnSynEnabled(selectedNode != null && selectedNode.getAcceptedParentId() != null);      
        //popupMenu.setUnSynEnabled(false);

        SwingWorker bgWork = new SwingWorker()
        {
            private T nodeRecord;
            private boolean canDelete = false;
            
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                nodeRecord = null;
                if (selectedNode != null)
                {
                    //log.debug("getting DB record for node " + selectedNode.getId());
                    nodeRecord = getRecordForNode(selectedNode);
                    //log.debug("retrieved DB record for node " + selectedNode.getId());
                }
                
                // these calls should work even if nodeRecord is null
                //log.debug("processing business rules to determine if node " + (selectedNode != null ? selectedNode.getId() : "null") + " is deleteable");
                canDelete = (businessRules != null) ? businessRules.okToEnableDelete(nodeRecord) : false;
                canDelete = canDelete && (selectedNode != listModel.getVisibleRoot());
                return null;
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public void finished()
            {
                super.finished();
                
                if (selectedNode != sourceList.getSelectedValue())
                {
                    // the node selection changed after we started the thread
                    // ignore the results
                    //log.info("selection changed before updating status bar");
                    return;
                }

                boolean validSelection = (selectedNode != null);
                
                // update the state of all selection-sensative buttons
                if (sourceList == lists[0])
                {
                    if (isEditMode)
                    {
                        deleteNode0.setEnabled(canDelete && validSelection);
                    }
                    popupMenu.setDeleteEnabled(canDelete && validSelection);
                }
                else
                {
                    if (isEditMode)
                    {
                        deleteNode1.setEnabled(canDelete && validSelection);
                    }
                    popupMenu.setDeleteEnabled(canDelete && validSelection);
                }
            }            
        };
        
        //log.debug("starting background task to see if new selected node is deleteable");
        bgWork.start();
	}

	/**
     * @return the isEditMode
     */
    public boolean isEditMode()
    {
        return isEditMode;
    }
    
    /**
     * @param draggedRecord
     * @param droppedRecord
     * @return
     */
    private NODE_DROPTYPE askForDropAction(final T        draggedRecord,
                                           final T        droppedOnRecord,
                                           final TreeNode droppedOnNode, 
                                           final TreeNode draggedNode)
    {
        boolean isSynonymizeOK = isSynonymizeOK(droppedOnNode, draggedNode, treeDef.getSynonymizedLevel());
        boolean isMoveOK      = isMoveOK(droppedOnNode, draggedNode);
        
        if (treeDef.getSynonymizedLevel() == -1)
        {
            return NODE_DROPTYPE.MOVE_NODE;
        }
        
        if (false)
        {
            int numOptions = 1 + (isSynonymizeOK ? 1 : 0) + (isMoveOK ? 1 : 0);
            
            Object[] options = new Object[numOptions];
            numOptions = 0;
            
            int dlgOption = JOptionPane.CANCEL_OPTION;
            if (isSynonymizeOK)
            {
                options[numOptions++] = getResourceString("TreeTableView.SYNONIMIZE_NODE");
                dlgOption = JOptionPane.OK_CANCEL_OPTION;
            }
            
            if (isMoveOK)
            {
                options[numOptions++] = getResourceString("TreeTableView.MOVE_NODE");
                dlgOption = JOptionPane.YES_NO_CANCEL_OPTION;
            }
            
            options[numOptions++] = getResourceString("CANCEL");
            
            String msg = UIRegistry.getLocalizedMessage("TreeTableView.NODE_MSG", draggedRecord.getFullName(), droppedOnRecord.getFullName());
            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                         msg, 
                                                         getResourceString("TreeTableView.NODE_ACTION_TITLE"), 
                                                         dlgOption,
                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (userChoice == JOptionPane.YES_OPTION || userChoice == JOptionPane.OK_OPTION)
            {
                return NODE_DROPTYPE.SYNONIMIZE_NODE;
                
            } else if (userChoice == JOptionPane.NO_OPTION)
            {
                return NODE_DROPTYPE.MOVE_NODE;
            }
        } else
        {
            int numOptions = 2 + (isSynonymizeOK ? 1 : 0) + (isMoveOK ? 1 : 0);

            String msg = UIRegistry.getLocalizedMessage("TreeTableView.NODE_MSG", draggedRecord.getFullName(), droppedOnRecord.getFullName());
            
            JTextArea ta = createTextArea();
            ta.setEditable(false);
            ta.setText(msg);
            JScrollPane sp = new JScrollPane(ta, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setBorder(BorderFactory.createEmptyBorder());
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g"));
            //pb.add(createLabel(getResourceString("TreeTableView.NODE_ACTION")), cc.xy(1,1));
            pb.add(sp, cc.xy(1,3));
            pb.setDefaultDialogBorder();
            
            sp.setBackground(pb.getPanel().getBackground());
            ta.setOpaque(false);
            sp.getViewport().setBackground(pb.getPanel().getBackground());
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                                                getResourceString("TreeTableView.NODE_ACTION_TITLE"),
                                                true,
                                                numOptions == 4 ? CustomDialog.OKCANCELAPPLYHELP : CustomDialog.OKCANCELHELP,
                                                pb.getPanel());
            dlg.setHelpContext("SYNONIMIZE_NODE");
            if (isSynonymizeOK)
            {
                dlg.setOkLabel(getResourceString("TreeTableView.SYNONIMIZE_NODE"));
            }
            
            if (isMoveOK)
            {
                if (isSynonymizeOK)
                {
                    dlg.setApplyLabel(getResourceString("TreeTableView.MOVE_NODE"));   
                    dlg.setCloseOnApplyClk(true);
                } else
                {
                    dlg.setOkLabel(getResourceString("TreeTableView.MOVE_NODE"));
                }
            }
            
            dlg.setVisible(true);
            
            int btn = dlg.getBtnPressed();
            if (!dlg.isCancelled() && btn != CustomDialog.HELP_BTN)
            {
                if (isSynonymizeOK)
                {
                    if (isMoveOK)
                    {
                        return btn == CustomDialog.OK_BTN ? NODE_DROPTYPE.SYNONIMIZE_NODE : NODE_DROPTYPE.MOVE_NODE;
                    }
                    
                    return NODE_DROPTYPE.SYNONIMIZE_NODE;
                }
                return NODE_DROPTYPE.MOVE_NODE;
            }
        }
        
        return NODE_DROPTYPE.CANCEL_DROP;
    }

    /**
	 * Reparents <code>dragged</code> to <code>droppedOn</code> by calling
	 * {@link TreeDataListModel#reparent(T, T)}.
	 *
	 * @see edu.ku.brc.ui.DragDropCallback#dropOccurred(java.lang.Object, java.lang.Object)
	 * @param dragged the dragged tree node
	 * @param droppedOn the node the dragged node was dropped onto
	 */
	@SuppressWarnings("unchecked")
	public boolean dropOccurred(final Object dragged, final Object droppedOn, final int dropAction )
	{
        setStatusBarText(null);

		if (dragged == droppedOn)
		{
			return false;
		}
		
		listModel.setDropLocationNode(null);
		repaint();

		if ( !(dragged instanceof TreeNode && droppedOn instanceof TreeNode) )
		{
			log.warn("Ignoring drag and drop of unhandled types of objects");
			return false;
		}
		
        TreeNode draggedNode   = (TreeNode)dragged;
        TreeNode droppedOnNode = (TreeNode)droppedOn;
        T        draggedRecord = getRecordForNode(draggedNode);
        T        droppedRecord = getRecordForNode(droppedOnNode);

        NODE_DROPTYPE nodeDropAction = askForDropAction(draggedRecord, droppedRecord, droppedOnNode, draggedNode);

		if (nodeDropAction == NODE_DROPTYPE.SYNONIMIZE_NODE)
		{
			log.info("User requested new link be created between " + draggedNode.getName() + " and " + droppedOnNode.getName());
            
            if ((draggedNode.getAcceptedParentId() != null) && (droppedOnNode.getId() == draggedNode.getAcceptedParentId()))
            {
                //log.info("User request to synonymize a node with it's current accepted parent.  Nothing to do.");
                return false;
            }
            
			String statusMsg = dataService.synonymize(draggedRecord, droppedRecord);
            draggedNode.setAcceptedParentId(droppedOnNode.getId());
            
            // fix all synonyms of the new synonym to point at the "final" accepted name in the chain
            for (Pair<Integer, String> idAndName: draggedNode.getSynonymIdsAndNames())
            {
                if (idAndName.first != null)
                {
                    int synNodeID = idAndName.first;
                    TreeNode synNode = listModel.getNodeById(synNodeID);
                    if (synNode != null)
                    {
                        synNode.setAcceptedParentId(droppedOnNode.getId());
                        synNode.setAcceptedParentFullName(droppedOnNode.getFullName());
                        droppedOnNode.getSynonymIdsAndNames().add(new Pair<Integer,String>(synNode.getId(),synNode.getFullName()));
                        
                    } else
                    {
                        // I don't think this is actually an error - rods 05/21/08
                        //String msg = "** - JDS - ** synNode was null and shouldn't have been for ID["+synNodeID+"]";
                        //log.error(msg);
                        //UIRegistry.displayErrorDlg(msg);
                    }
                } else
                {
                    String msg = "** - JDS - ** idAndName.first was null and shouldn't have been.";
                    log.error(msg);
                    UIRegistry.displayErrorDlg(msg);
                }
            }
            
            draggedNode.getSynonymIdsAndNames().clear();
            
            draggedNode.setAcceptedParentFullName(droppedOnNode.getFullName());
            droppedOnNode.getSynonymIdsAndNames().add(new Pair<Integer,String>(draggedNode.getId(),draggedNode.getFullName()));
            draggedNode.setCalcCount(false);
            droppedOnNode.setCalcCount(false);
            draggedNode.setCalcCount2(false);
            droppedOnNode.setCalcCount2(false);
            draggedNode.setHasCalcCount(false);
            droppedOnNode.setHasCalcCount(false);
            draggedNode.setHasCalcCount2(false);
            droppedOnNode.setHasCalcCount2(false);
            
            T draggedParent = draggedRecord.getParent();
            T droppedParent = droppedRecord.getParent();
            Vector<TreeNode> draggedChildren = new Vector<TreeNode>();
            Vector<TreeNode> droppedChildren = new Vector<TreeNode>();
            if (droppedParent.getTreeId().equals(draggedParent.getTreeId()))
            {
                draggedChildren.add(draggedNode);
                draggedChildren.add(droppedOnNode);
                showCounts(draggedParent, draggedChildren);
            }
            else
            {
                draggedChildren.add(draggedNode);
                droppedChildren.add(droppedOnNode);
                showCounts(draggedParent, draggedChildren);
                showCounts(droppedParent, droppedChildren);
            }
            updateAllUI();
			if (statusMsg != null)
			{
			    statusBar.setText(statusMsg);
			}
			return true;
		}
		else if (nodeDropAction == NODE_DROPTYPE.MOVE_NODE)
		{
			T child = draggedRecord;
			T newParent = droppedRecord;
            TreeNode oldParentNode = listModel.getNodeById(draggedNode.getParentId());
            TreeNode newParentNode = droppedOnNode;
            
			if ( !TreeHelper.canChildBeReparentedToNode(child,newParent) )
			{
				log.info("Cannot reparent " + child.getName() + " to " + newParent.getName());
				return false;
			}
			
            hideChildren(oldParentNode);
            hideChildren(droppedOnNode);
            // Removing the children of these nodes may have resulted in a node being removed from the model.
            // This happens when one of these nodes is a descendant of the other.  The lower ranked node will
            // no longer be in the model at all.
			
            // do the DB work to reparent the nodes
            boolean changed = dataService.moveTreeNode(child, newParent);
            if (!changed)
            {
                String msg = getResourceString("TTV_UNKOWN_MOVE_ERROR");
                statusBar.setErrorMessage(msg);
                UIRegistry.displayErrorDlg(msg);
            }
            
            // reshow the nodes' children, if the nodes are still in the tree (see comment above in this method)
            oldParentNode = listModel.getNodeById(oldParentNode.getId());
            newParentNode = listModel.getNodeById(newParentNode.getId());
            
            if (oldParentNode != null)
            {
                showChildren(oldParentNode);
            }
            if (newParentNode != null)
            {
                newParentNode.setHasChildren(true);
                showChildren(newParentNode);
            }
            
			return changed;
		}
		return false;
	}
	
	/**
	 * Checks to see if the dragged node can be synonymized with the dropped on node.
	 * @param droppedOnNode the destination
	 * @param draggedNode the source
	 * @return true if it can be
	 */
	private boolean isSynonymizeOK(final TreeNode droppedOnNode, 
	                              final TreeNode draggedNode,
	                              final int      rankLevel)
	{
	    if (droppedOnNode.getAcceptedParentId() == null)
        {
	        if (draggedNode.isHasChildren())
	        {
	            return false;
	        }
	        
	        int draggedRankId = draggedNode.getRank();
	        int droppedRankId = droppedOnNode.getRank();
	        //System.out.println("draggedRankId "+draggedRankId+"  droppedRankId "+droppedRankId+"  rankLevel "+rankLevel);
	        
    	    if (rankLevel == -1 || (draggedRankId >= rankLevel && droppedRankId >= rankLevel))
            {
                boolean descendant = listModel.isDescendantOfNode(droppedOnNode, draggedNode);
                if (!descendant)
                {
                    // check the other way as well
                    descendant = listModel.isDescendantOfNode(draggedNode, droppedOnNode);
                }
                // log.debug("Synonymization request IS acceptable.");
                return !descendant;
            }
        }
	    return false;
	}
	
	/**
     * @param droppedOnNode
     * @param draggedNode
     * @return
     */
	private boolean isMoveOK(final TreeNode droppedOnNode, final TreeNode draggedNode)
	{
	    return 
	        TreeHelper.canChildBeReparentedToNode(draggedNode.getRank(), droppedOnNode.getRank(), treeDef)
	        && draggedNode.getParentId() != droppedOnNode.getId();
	}
	
    /* (non-Javadoc)
	 * @see edu.ku.brc.ui.DragDropCallback#dropAcceptable(java.lang.Object, java.lang.Object, int)
	 */
	@SuppressWarnings("unchecked")
	public boolean dropAcceptable(final Object dragged, final Object droppedOn, final int dropAction )
	{
		if (dragged == droppedOn)
		{
		    listModel.setDropLocationNode(null);
	        setStatusBarText(null);
	        repaint();
			return false;
		}

		//log.debug(dragged + " is being dragged over " + droppedOn);
		
		repaint(); // this schedules a repaint
		
		//log.debug("determining if request to synonymize node " + dragged + " to node " + droppedOn + " is acceptable");
        // this is a request to make a node relationship (e.g. synonym on a Taxon record)
		if ( !(dragged instanceof TreeNode && droppedOn instanceof TreeNode))
		{
			//log.debug("Synonymization request IS NOT acceptable.  One or both objects are not TreeNodes.");
		    listModel.setDropLocationNode(null);
		    setStatusBarText(null);
			return false;
		}
		
		TreeNode droppedOnNode = (TreeNode)droppedOn;
        TreeNode draggedNode   = (TreeNode)dragged;
		
        boolean isNodeDiff = listModel.getDropLocationNode() != droppedOnNode;
        
        listModel.setDropLocationNode(null);
        
        String msg = "";
        
        // Check to see if it can be Synonmized
        boolean isOK = isSynonymizeOK(droppedOnNode, draggedNode, treeDef.getSynonymizedLevel());
		if (isOK)
		{
		    listModel.setDropLocationNode(droppedOn);
		    if (isNodeDiff)
            {
		        msg = getResourceString("TreeViewer.SYN_IS_OK");
            }
		}
		//log.debug("Synonymization request IS NOT acceptable.  Drop target is not an accepted name.");

		// Check to see if it can be moved
		if (isMoveOK(droppedOnNode, draggedNode))
		{
        	//log.debug("Reparent request IS acceptable.");
    		listModel.setDropLocationNode(droppedOn);
    		isOK = true;
    		
    		if (isNodeDiff)
    		{
    		    msg += (msg.length() > 0 ? ", " : "") + getResourceString("TreeViewer.MOVE_IS_OK");
    		}
		}
		
		if (isNodeDiff)
		{
		    UIRegistry.getStatusBar().setText(isOK ? msg : "");
		}
		
		return isOK;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.DragDropCallback#dragDropEnded(boolean)
	 */
	public void dragDropEnded(boolean success)
    {
        listModel.setDropLocationNode(null);
    }

    /**
     * @param e
     */
    @SuppressWarnings("unchecked")
	public void showPopup(MouseEvent e)
	{
		if (clickIsOnText(e))
		{
			// select this node and display popup for it
			final TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
			Point p     = e.getPoint();
			int   index = list.locationToIndex(p);
			if (index == -1)
			{
				return;
			}
            list.setSelectedIndex(index);
            list.setSelectedIndex(index);
			popupMenu.setList(list);
			popupMenu.show(list,e.getX(),e.getY());
		}
		else
		{
			return;
		}
	}
	
	/**
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean clickIsOnText(MouseEvent e)
	{
		final TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if (index==-1)
		{
			return false;
		}
		TreeNode t = listModel.getElementAt(index);
		Integer rank = t.getRank();
		Pair<Integer,Integer> textBounds = listCellRenderer.getTextBoundsForRank(rank);
		
		if ( textBounds.first < p.x && p.x < textBounds.second )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean clickIsOnExpansionIcon(MouseEvent e)
	{
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if (index==-1)
		{
			return false;
		}
        ListModel model = list.getModel();
        TreeNode t = (TreeNode)model.getElementAt(index);
		Integer rank = t.getRank();
		Pair<Integer,Integer> anchorBounds = listCellRenderer.getAnchorBoundsForRank(rank);
		
		if ( anchorBounds.first < p.x && p.x < anchorBounds.second )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	public void mouseButtonClicked(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			showPopup(e);
		}
		
		if (e.getButton() != MouseEvent.BUTTON1)
		{
			return;
		}
				
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		if (index == -1)
		{
			return;
		}
        ListModel model = list.getModel();
		TreeNode treeNode = (TreeNode)model.getElementAt(index);

        // if the user clicked an expansion handle, expand the child nodes
		if ( clickIsOnExpansionIcon(e) || (e.getClickCount() == 2 && clickIsOnText(e)) )
		{
            if (listModel.showingChildrenOf(treeNode))
            {
                hideChildren(treeNode);
            }
            else
            {
                List<TreeNode> childrenShown = showChildren(treeNode);
                if (childrenShown.size() > 0)
                {
                    TreeNode firstChild = childrenShown.get(0);
                    int listIndex = (list == lists[0]) ? 0 : 1;
                    scrollToShowNode(firstChild, listIndex);
                }
            }
		}
        // otherwise, ignore the click
		else
		{
			e.consume();
		}
	}
    
    /**
     * Returns the rank of the leaf level of the tree.  Remember, it's deeper in the tree, but has a
     * larger rank value.
     * 
     * @return
     */
    protected int getHighestPossibleNodeRank()
    {
        int highestRank = Integer.MIN_VALUE;
        for (I defItem: treeDef.getTreeDefItems())
        {
            Integer rank = defItem.getRankId();
            if (rank != null && rank > highestRank)
            {
                highestRank = rank;
            }
        }
        return highestRank;
    }
    
	/**
	 * @param e
	 */
	public void mouseButtonReleased(final MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			showPopup(e);
		}
	}
	
	/**
	 * @param e
	 */
	public void mouseButtonPressed(final MouseEvent e)
	{
        //log.debug("mouse button pressed on " + e.getSource());

		if (e.isPopupTrigger())
		{
			showPopup(e);
		}

		if (e.getButton() != MouseEvent.BUTTON1)
		{
			return;
		}
		
		TreeDataGhostDropJList list = (TreeDataGhostDropJList)e.getSource();
		boolean clickIsOnText = clickIsOnText(e);
        //log.debug("mouse click was on text: " + clickIsOnText);
        if ( clickIsOnText )
		{
			list.setClickOnText(true);
		}
		else
		{
			list.setClickOnText(false);
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
	 */
	@Override
	public boolean aboutToShutdown()
	{
        try
        {
            T selectedNode = getSelectedNode(lists[0]);
            AppPreferences appPrefs = AppPreferences.getRemote();
            if (appPrefs != null)
            {
                if (selectedNode != null)
                {
                    appPrefs.put(selNodePrefName, selectedNode.getTreeId().toString());
                }
                else
                {
                    appPrefs.remove(selNodePrefName);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unknown error when trying to store the selected node id during tree viewer shutdown.", e);
        }
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
	 */
	@Override
	public void shutdown()
	{
        countGrabberExecutor.shutdownNow();
        if (doUnlock)
        {
            TaskSemaphoreMgr.unlock(name, treeDef.getClass().getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline);
        }
		super.shutdown();
	}
	
	/**
     * @param doUnlock the doUnlock to set
     */
    public void setDoUnlock(boolean doUnlock)
    {
        this.doUnlock = doUnlock;
    }

    /**
     * @return the doUnlock
     */
    public boolean isDoUnlock()
    {
        return doUnlock;
    }

    /**
	 * @param list
	 * @return
	 */
	public T getSelectedNode(final JList list)
	{
		if (lists[0] == list || lists[1] == list )
		{
			TreeNode node = (TreeNode)list.getSelectedValue();
            if (node == null)
            {
                return null;
            }
            
            return getRecordForNode(node);
		}
		throw new IllegalArgumentException("Provided JList must be one of the TTV display lists");
	}
	
	/**
	 * @return
	 */
	public TreeNodePopupMenu getPopupMenu()
	{
		return popupMenu;
	}
    
    /**
     * Creates a TreeNode object representing the given Treeable database record.  This method
     * assumes that the data record has a non-null ID and rank.  It further assumes that, if the
     * parent record is non-null, the parent has non-null ID and rank.
     * 
     * @param dataRecord the database record
     * @return a TreeNode object representing the given Treeable database record
     */
    private TreeNode createNode(final T dataRecord)
    {
        String nodeName  = dataRecord.getName();
        String fullName  = dataRecord.getFullName();
        int    id        = dataRecord.getTreeId();
        int    rank      = dataRecord.getRankId();
        T acceptedParent = dataRecord.getAcceptedParent();

        int parentId;
        int parentRank;
        
        T parentRecord = dataRecord.getParent();
        if (parentRecord == null)
        {
            parentId = id;
            parentRank = -1;
        }
        else
        {
            parentId = parentRecord.getTreeId();
            parentRank = parentRecord.getRankId();
        }
        
        int descCount = dataService.getDescendantCount(dataRecord);
        
        Set<T> synonyms = dataService.getSynonyms(dataRecord);
        Set<Integer> synIds = new HashSet<Integer>();
        Set<String> synNames = new HashSet<String>();
        for (T syn: synonyms)
        {
            synIds.add(syn.getTreeId());
            synNames.add(syn.getFullName());
        }
        
        Set<Pair<Integer,String>> synIdsAndNames = dataService.getSynonymIdsAndNames(dataRecord.getClass(), id);
        
        Integer acceptParentId      = (acceptedParent != null) ? acceptedParent.getTreeId() : null;
        String acceptParentFullName = (acceptedParent != null) ? acceptedParent.getFullName() : null;
        TreeNode node = new TreeNode(nodeName,fullName,id,parentId,rank,parentRank, (descCount != 0), acceptParentId, acceptParentFullName, synIdsAndNames);
        
        return node;
    }
    
    /**
     * 
     */
    public void repaintLists()
    {
        if (lists != null)
        {
            for (TreeDataGhostDropJList lst :  lists)
            {
                if (lst != null)
                {
                    lst.repaint();
                }
            }
        }
    }
    
    /**
     * @param dbRecord
     * @param childNodes
     */
    protected synchronized void showCounts(final T dbRecord, 
                                           final List<TreeNode> childNodes)
    {
        String propName = "TreeEditor.Rank.Threshold."+dbRecord.getClass().getSimpleName();
        final int rankThreshold = AppPreferences.getRemote().getInt(propName, -1);

        final Class<?> dbRecClass = dbRecord.getClass();
        final boolean isHQL = TreeFactory.isQueryHQL(dbRecClass);
        
        for (TreeNode newNode: childNodes)
        {
            if (!newNode.shouldCalcCount())
            {
                continue;
            }

            final TreeNode node = newNode;
            Runnable getAssocRecCount = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        if (rankThreshold == -1 || dbRecord.getRankId() >= rankThreshold)
                        {
                            //System.out.println("Calc: "+node.getFullName()+" "+node.hashCode());
                            new ChildNodeCounter(TreeTableViewer.this, 1, node,  TreeFactory.getRelatedRecordCountSQLSingleNode(dbRecClass, node), null, isHQL);
                            
                            new ChildNodeCounter(TreeTableViewer.this, 2, node, 
                                    TreeFactory.getNodeNumberQuery(dbRecClass), 
                                    TreeFactory.getRelatedRecordCountSQLForRange(dbRecClass, node), isHQL);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error(e);
                        e.printStackTrace();
                    }
                }
            };
            
            countGrabberExecutor.submit(getAssocRecCount);
         }
    }
    
    /**
     * @param dbRecord
     * @return the list of children shown
     */
    protected synchronized List<TreeNode> showChildren(final T dbRecord)
    {
        // get the child nodes
        List<TreeNode> childNodes = dataService.getChildTreeNodes(dbRecord);
        
        /*
        String propName = "TreeEditor.Rank.Threshold."+dbRecord.getClass().getSimpleName();
        final int rankThreshold = AppPreferences.getRemote().getInt(propName, -1);

        final Class<?> dbRecClass = dbRecord.getClass();
        final boolean isHQL = TreeFactory.isQueryHQL(dbRecClass);
        
        for (TreeNode newNode: childNodes)
        {
            final TreeNode node = newNode;
            Runnable getAssocRecCount = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        if (rankThreshold == -1 || dbRecord.getRankId() >= rankThreshold)
                        {
                            new ChildNodeCounter(TreeTableViewer.this, 1, node,  TreeFactory.getRelatedRecordCountSQLSingleNode(dbRecClass), null, isHQL);
                            
                            new ChildNodeCounter(TreeTableViewer.this, 2, node, 
                                    TreeFactory.getNodeNumberQuery(dbRecClass), 
                                    TreeFactory.getRelatedRecordCountSQLForRange(dbRecClass), isHQL);
                        }
                    }
                    catch (Exception e)
                    {
                        log.error(e);
                        e.printStackTrace();
                    }
                }
            };
            
            countGrabberExecutor.submit(getAssocRecCount);
         }*/
        
        // get the node representing the parent DB record
        TreeNode parentNode = listModel.getNodeById(dbRecord.getTreeId());

        // add the nodes to the model
        if (childNodes.size() == 0)
        {
            parentNode.setHasChildren(false);
            listModel.nodeValuesChanged(parentNode);
            return childNodes;
        }
        listModel.showChildNodes(childNodes, parentNode);

        if (parentNode != null)
        {
            idsToReexpand.add(parentNode.getId());
        }

        if (restoreTreeState)
        {
            // recursively expand the tree back to it's previous state
            for (TreeNode childNode: childNodes)
            {
                if (idsToReexpand.contains(childNode.getId()))
                {
                    idsToReexpand.remove(childNode.getId());
                    
                    // maybe start a Swing Timer for this work
                    showChildrenInTimer(childNode);
                    //showChildren(childNode);
                }
            }
        }
        
        return childNodes;
    }
    
    /**
     * Show children delayed.
     * @param childNode the parent node
     */
    protected void showChildrenInTimer(final TreeNode childNode)
    {
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showChildren(childNode);
            }
        };
        Timer swingTimer = new Timer(200, al);
        swingTimer.setRepeats(false);
        swingTimer.start();
    }

    /**
     * Show children for parent.
     * @param parent the parent
     * @return the list of kids
     */
    protected synchronized List<TreeNode> showChildren(final TreeNode parent)
    {
        // get the DB record that corresponds to this TreeNode
        T dbRecord = getRecordForNode(parent);

        List<TreeNode> childNodes = showChildren(dbRecord);
        showCounts(dbRecord, childNodes);
        return childNodes;
    }

    /**
     * Hide children for parent.
     * @param parent the parent
     */
    protected synchronized void hideChildren(final TreeNode parent)
    {
        idsToReexpand.remove(parent.getId());
        listModel.removeChildNodes(parent);
    }
    
    /**
     * Get record for node.
     * @param node the node
     * @return the record
     */
    private T getRecordForNode(final TreeNode node)
    {
        T record = dataService.getNodeById(treeDef.getNodeClass(), node.getId());
        return record;
    }
    
}
