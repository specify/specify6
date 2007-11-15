/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * A base task that provides functionality in common to all tasks
 * that provide UI for tree-structured data.
 *
 * @code_status Beta
 * @author jstewart
 */
public abstract class BaseTreeTask <T extends Treeable<T,D,I>,
							        D extends TreeDefIface<T,D,I>,
							        I extends TreeDefItemIface<T,D,I>>
							        extends BaseTask
{
    protected static final Logger log = Logger.getLogger(BaseTreeTask.class);
            
    /** The toolbar items provided by this task. */
    protected List<ToolBarItemDesc> toolBarItems;
    
    /** The menu items provided by this task. */
    protected List<MenuItemDesc> menuItems;
    
    /** The class of {@link TreeDefIface} handled by this task. */
    protected Class<D> treeDefClass;
    
    protected D currentDef;
    
    protected boolean currentDefInUse;
    protected SubPaneIFace visibleSubPane;
    
    protected JMenu subMenu;
    protected JMenuItem showTreeMenuItem;
    protected JMenuItem editDefMenuItem;
    
    protected String menuItemText;
    protected String menuItemMnemonic;
    protected String starterPaneText;
    protected String commandTypeString;
    
    public static final String OPEN_TREE        = "OpenTree";
    public static final String EDIT_TREE_DEF    = "EditTreeDef";
    public static final String SWITCH_VIEW_TYPE = "SwitchViewType";
    
	/**
     * Constructor.
     * 
	 * @param name the name of the task
	 * @param title the visible name of the task
	 */
	protected BaseTreeTask(final String name, final String title)
	{
		super(name,title);
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#initialize()
	 */
	@Override
	public synchronized void initialize()
	{
        if (!isInitialized)
        {
            isInitialized = true;

            currentDef = getCurrentTreeDef();
            navBoxes = Collections.emptyList();
            toolBarItems = Collections.emptyList();
            menuItems = createMenus();

            if (commandTypeString != null)
            {
                CommandDispatcher.register(commandTypeString,this);
            }
        }
	}
    
    protected abstract D getCurrentTreeDef();
	
	/**
     * Creates a simple menu item that brings this task into context.
     * 
	 * @param defs a list of tree definitions handled by this task
	 */
	protected List<MenuItemDesc> createMenus()
	{
        Vector<MenuItemDesc> menus = new Vector<MenuItemDesc>();
        subMenu = new JMenu(menuItemText);
        
        MenuItemDesc treeSubMenuMI = new MenuItemDesc(subMenu, "AdvMenu");
        menus.add(treeSubMenuMI);
        
        showTreeMenuItem = new JMenuItem(getResourceString("TTV_SHOW_TREE_MENU_ITEM"));
        showTreeMenuItem.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                if (!currentDefInUse)
                {
                    SwingWorker bgWorker = new SwingWorker()
                    {
                        private TreeTableViewer<T,D,I> treeViewer;
                        
                        @Override
                        public Object construct()
                        {
                            treeViewer = createTreeViewer();
                            return treeViewer;
                        }

                        @Override
                        public void finished()
                        {
                            super.finished();
                            ContextMgr.requestContext(BaseTreeTask.this);
                            currentDefInUse = true;
                            visibleSubPane = treeViewer;
                            addSubPaneToMgr(treeViewer);
                        }
                    };
                    bgWorker.start();
                }
                else
                {
                    // If the TTV is already open, show it.
                    if (visibleSubPane instanceof TreeTableViewer<?,?,?>)
                    {
                        SubPaneMgr.getInstance().showPane(visibleSubPane);
                    }
                    else // Otherwise a def editor must be open.  Close it an open a TTV.
                    {
                        switchViewType();
                    }
                }
            }
        });
        
        editDefMenuItem = new JMenuItem(getResourceString("TTV_EDIT_DEF_MENU_ITEM"));
        editDefMenuItem.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                if (!currentDefInUse)
                {
                    SwingWorker bgWorker = new SwingWorker()
                    {
                        private TreeDefinitionEditor<T,D,I> defEditor;
                        
                        @Override
                        public Object construct()
                        {
                            defEditor = createDefEditor();
                            return defEditor;
                        }

                        @Override
                        public void finished()
                        {
                            super.finished();
                            ContextMgr.requestContext(BaseTreeTask.this);
                            currentDefInUse = true;
                            visibleSubPane = defEditor;
                            addSubPaneToMgr(defEditor);
                        }
                    };
                    bgWorker.start();
                }
                else
                {
                    // If the def editor is already open, show it.
                    if (visibleSubPane instanceof TreeDefinitionEditor<?,?,?>)
                    {
                        SubPaneMgr.getInstance().showPane(visibleSubPane);
                    }
                    else // Otherwise a TTV must be open.  Close it an open a def editor.
                    {
                        switchViewType();
                    }
                }
            }
        });

        subMenu.add(showTreeMenuItem);
        subMenu.add(editDefMenuItem);

        return menus;
	}
    
    @SuppressWarnings("unchecked")
    protected TreeTableViewer<T,D,I> createTreeViewer()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
        session.close();
        String tabName = treeDef.getName();
        TreeTableViewer<T,D,I> ttv = new TreeTableViewer<T,D,I>(treeDef,tabName,this);
        return ttv;
    }

    /**
     * Opens a {@link SubPaneIFace} for viewing/editing the current {@link TreeDefIface} object.
     */
    @SuppressWarnings("unchecked")
    protected TreeDefinitionEditor<T,D,I> createDefEditor()
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        D treeDef = (D)session.load(currentDef.getClass(), currentDef.getTreeDefId());
        session.close();
        String tabName = treeDef.getName();
	    TreeDefinitionEditor<T,D,I> defEditor = new TreeDefinitionEditor<T,D,I>(treeDef,tabName,this);
        return defEditor;
	}
    
	/**
	 * Switches the current view from {@link TreeTableViewer} to {@link TreeDefinitionEditor}
     * or vice versa.  If the current view is neither of these, this does nothing.
	 */
	@SuppressWarnings("unchecked")
    protected void switchViewType()
	{
	    if (visibleSubPane instanceof TreeTableViewer)
	    {
            TreeDefinitionEditor<T,D,I> defEditor = createDefEditor();
            SubPaneMgr.getInstance().replacePane(visibleSubPane, defEditor);
            currentDefInUse = true;
            visibleSubPane = defEditor;
	        return;
	    }
        else if (visibleSubPane instanceof TreeDefinitionEditor)
        {
            TreeTableViewer<T,D,I> treeViewer = createTreeViewer();
            SubPaneMgr.getInstance().replacePane(visibleSubPane, treeViewer);
            currentDefInUse = true;
            visibleSubPane = treeViewer;
            return;
        }
	    return;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
	 */
	@Override
	public List<MenuItemDesc> getMenuItems()
	{
		return menuItems;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
	 */
	@Override
    public SubPaneIFace getStarterPane()
    {
        // This starter pane will only be visible for a brief moment while the tree loads.
        // It doesn't need to be fancy.
        return starterPane = new SimpleDescPane(title, this, starterPaneText);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
	 */
	@Override
	public List<ToolBarItemDesc> getToolBarItems()
	{
        return toolBarItems;
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
	@SuppressWarnings("unchecked")
    @Override
	public Class<? extends BaseTreeTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @SuppressWarnings("unchecked")
    @Override
	public void subPaneRemoved(SubPaneIFace subPane)
	{
        if (!subPane.getTask().equals(this))
        {
            // we don't care about this subpane being closed
            // it's not one of ours
            return;
        }
        currentDefInUse = false;
        visibleSubPane = null;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        UIRegistry.getStatusBar().setText("");
        
        if (cmdAction.isType(DataEntryTask.DATA_ENTRY))
        {
            // catch when the form switches modes
            if (cmdAction.isAction(DataEntryTask.VIEW_WAS_SHOWN))
            {
                if (cmdAction.getData() instanceof FormViewObj)
                {
                    FormViewObj formViewObj = (FormViewObj)cmdAction.getData();
                    if (formViewObj != null)
                    {
                        adjustForm(formViewObj);
                    }
                }
            }
            // catch when the form is initially opened (but after the object is set into it)
            else if (cmdAction.isAction(DataEntryTask.VIEW_WAS_OPENED))
            {
                if (cmdAction.getData() instanceof FormPane)
                {
                    FormPane fp = (FormPane)cmdAction.getData();
                    
                    if (fp.getViewable() instanceof FormViewObj)
                    {
                        FormViewObj formViewObj = (FormViewObj)fp.getViewable();
                        if (formViewObj != null)
                        {
                            adjustForm(formViewObj);
                        }
                    }
                }
            }
            else if (cmdAction.isAction("Save"))
            {
                // should we do anything here?
            }
            
        }
    }
    
    public void adjustForm(FormViewObj form)
    {
        log.debug("adjustForm(" + form.getName() + ")"); 
        
        // this method should look a lot like ...
        
        /*
        if (form.getDataObj() instanceof T)
        {
            adjustNodeForm(form);
        }
        else if (form.getDataObj() instanceof D)
        {
            adjustTreeDefForm(form);
        }
        else if (form.getDataObj() instanceof I)
        {
            adjustTreeDefItemForm(form);
        }
        */
        
        // however, instanceof won't work with generics, so we have to implement this method in the subclasses
    }
    
    @SuppressWarnings("unchecked")
    protected void adjustNodeForm(final FormViewObj form)
    {
        log.debug("adjustNodeForm(" + form.getName() + ")");

        if (form.getAltView().getMode() != CreationMode.EDIT)
        {
            // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
            return;
        }

        final T nodeInForm = (T)form.getDataObj();

        final Component parentComboBox = form.getControlByName("parent");
        final ValComboBox rankComboBox = (ValComboBox)form.getControlByName("definitionItem");
        log.debug("parentComboBox = " + parentComboBox);
        log.debug("rankComboBox   = " + rankComboBox);

        if (parentComboBox != null)
        {
            log.debug("adding focus listener to parentComboBox");
            parentComboBox.addFocusListener(new FocusListener()
            {
                public void focusGained(FocusEvent e)
                {
                    // ignore this event
                }
                public void focusLost(FocusEvent e)
                {
                    log.debug("parentComboBox lost focus: calling adjustRankComboBoxModel()");
                    // set the contents of this combobox based on the value chosen as the parent
                    GetSetValueIFace parentField = (GetSetValueIFace)parentComboBox;
                    adjustRankComboBoxModel(parentField, rankComboBox, nodeInForm);

                    // set the tree def for the object being edited by using the parent node's tree def
                    T parent = (T)parentField.getValue();
                    if (parent != null)
                    {
                        nodeInForm.setDefinition(parent.getDefinition());
                    }
                }
            });
        }
        
        if (nodeInForm.getDefinitionItem() != null)
        {
            adjustRankComboBoxModel((GetSetValueIFace)parentComboBox, rankComboBox, nodeInForm);
        }
        
        // TODO: the form system MUST require the accepted parent widget to be present if the isAccepted checkbox is present
        final JCheckBox acceptedCheckBox = (JCheckBox)form.getControlByName("isAccepted");
        final GetSetValueIFace acceptedParentWidget = (GetSetValueIFace)form.getControlByName("acceptedParent");
        if (acceptedCheckBox != null && acceptedParentWidget != null)
        {
            acceptedCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (acceptedCheckBox.isSelected())
                    {
                        acceptedParentWidget.setValue(null, null);
                    }
                }
            });
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void adjustRankComboBoxModel(GetSetValueIFace parentField, ValComboBox rankComboBox, T nodeInForm)
    {
        log.debug("Adjusting the model for the 'rank' combo box in a tree node form");
        DefaultComboBoxModel model = (DefaultComboBoxModel)rankComboBox.getModel();
        model.removeAllElements();

        // this is the highest rank the edited item can possibly be
        I topItem = null;
        // this is the lowest rank the edited item can possibly be
        I bottomItem = null;

        Object value = parentField.getValue();
        T parent = null;
        if (value instanceof String)
        {
            // this happens when the combobox is in view mode, which means it's really a textfield
            // in that case, the parent of the node in the form will do, since the user can't change the parents
            parent = nodeInForm.getParent();
        }
        else
        {
            parent = (T)parentField.getValue();
        }
        
        if (parent == null)
        {
            return;
        }

        // grab all the def items from just below the parent's item all the way to the next enforced level
        // or to the level of the highest ranked child
        topItem = parent.getDefinitionItem().getChild();
        
        if (topItem == null)
        {
            // this only happens if a parent was chosen that cannot have children b/c it is at the
            // lowest defined level in the tree
            log.warn("Chosen node cannot be a parent node.  It is at the lowest defined level of the tree.");
            log.warn("Figure out how to restrict the searching in the combobox from allowing non-parent nodes from appearing.");
            return;
        }

        // find the child with the highest rank and set that child's def item as the bottom of the range
        if (!nodeInForm.getChildren().isEmpty())
        {
            for (T child: nodeInForm.getChildren())
            {
                if (bottomItem==null || child.getRankId()>bottomItem.getRankId())
                {
                    bottomItem = child.getDefinitionItem().getParent();
                }
            }
        }

        I item = topItem;
        boolean done = false;
        while (!done)
        {
            model.addElement(item);

            if (item.getChild()==null || item.getIsEnforced()==Boolean.TRUE || (bottomItem != null && item.getRankId().intValue()==bottomItem.getRankId().intValue()) )
            {
                done = true;
            }
            item = item.getChild();
        }
        
        if (nodeInForm.getDefinitionItem() != null)
        {
            I defItem = nodeInForm.getDefinitionItem();
            for (int i = 0; i < model.getSize(); ++i)
            {
                I modelItem = (I)model.getElementAt(i);
                if (modelItem.getRankId().equals(defItem.getRankId()))
                {
                    model.setSelectedItem(modelItem);
                }
            }
//            if (model.getIndexOf(defItem) != -1)
//            {
//                model.setSelectedItem(defItem);
//            }
        }
        else if (model.getSize() == 1)
        {
            Object defItem = model.getElementAt(0);
            log.debug("setting rank selected value to the only available option: " + defItem);
            model.setSelectedItem(defItem);
        }
    }
}