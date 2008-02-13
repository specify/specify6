/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValComboBoxFromQuery;

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
        CommandDispatcher.register(APP_CMD_TYPE, this);
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

            currentDef   = getCurrentTreeDef();
            navBoxes     = Collections.emptyList();
            toolBarItems = Collections.emptyList();
            menuItems    = createMenus();

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
                        //log.debug(this.getClass().getSimpleName() + " is processing VIEW_WAS_SHOWN action for form " + formViewObj.getName());
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
                            //log.debug(this.getClass().getSimpleName() + " is processing VIEW_WAS_OPENED action for form " + formViewObj.getName());
                            adjustForm(formViewObj);
                        }
                    }
                }
            }
            else if (cmdAction.isAction("Save"))
            {
                // should we do anything here?
            }
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            currentDef = getCurrentTreeDef();
        }
    }
    
    public void adjustForm(@SuppressWarnings("unused") FormViewObj form)
    {
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
    protected void parentChanged(final FormViewObj form, 
                                 final ValComboBoxFromQuery parentComboBox, 
                                 final ValComboBox rankComboBox)
    {
        if (form.getAltView().getMode() != CreationMode.EDIT)
        {
            return;
        }
        
        //log.debug("form was validated: calling adjustRankComboBoxModel()");
        
        Object objInForm = form.getDataObj();
        //log.debug("form data object = " + objInForm);
        if (objInForm == null)
        {
            return;
        }
        
        T formNode = (T)objInForm;
        
        // set the contents of this combobox based on the value chosen as the parent
        adjustRankComboBoxModel(parentComboBox, rankComboBox, formNode);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                boolean rnkEnabled = rankComboBox.getComboBox().getModel().getSize() > 0;
                rankComboBox.setEnabled(rnkEnabled);
                JLabel label = form.getLabelFor(rankComboBox);
                if (label != null)
                {
                    label.setEnabled(rnkEnabled);
                }
                if (rankComboBox.hasFocus() && !rnkEnabled)
                {
                    parentComboBox.requestFocus();
                }
            }
        });

        T parent = null;
        if (parentComboBox.getValue() instanceof String)
        {
            // the data is still in the VIEW mode for some reason
            log.debug("Form is in mode (" + form.getAltView().getMode() + ") but the parent data is a String");
            parentComboBox.getValue();
            parent = formNode.getParent();
        }
        else
        {
            parent = (T)parentComboBox.getValue();
        }
        
        // set the tree def for the object being edited by using the parent node's tree def
        if (parent != null)
        {
            formNode.setDefinition(parent.getDefinition());
        }
    }
    
    /**
     * Adjust the Rank UI for a Parent node.
     * @param form the form in question.
     */
    @SuppressWarnings("unchecked")
    protected void adjustNodeForm(final FormViewObj form)
    {
        //log.debug("adjustNodeForm(" + form.getName() + ") in mode " + form.getAltView().getMode());

        if (form.getAltView().getMode() != CreationMode.EDIT)
        {
            // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
            //log.debug("form is not in edit mode: no special listeners will be attached");
            return;
        }

        final T nodeInForm = (T)form.getDataObj();

        GetSetValueIFace  parentField  = (GetSetValueIFace)form.getControlByName("parent");;
        final ValComboBox rankComboBox = (ValComboBox)form.getControlByName("definitionItem");

        if (parentField instanceof ValComboBoxFromQuery)
        {
            final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery)parentField;
            if (parentCBX != null && rankComboBox != null)
            {
                parentCBX.registerQueryBuilder(new SearchQueryBuilder(nodeInForm));
                
                parentCBX.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            parentChanged(form, parentCBX, rankComboBox);
                        }
                    }
                });
            }
        }
        
        if (nodeInForm != null && nodeInForm.getDefinitionItem() != null)
        {
            //log.debug("node in form already has a set rank: forcing a call to adjustRankComboBoxModel()");
            UIValidator.setIgnoreAllValidation(this, true);
            adjustRankComboBoxModel(parentField, rankComboBox, nodeInForm);
            UIValidator.setIgnoreAllValidation(this, false);
        }
        
        // TODO: the form system MUST require the accepted parent widget to be present if the isAccepted checkbox is present
        final JCheckBox        acceptedCheckBox     = (JCheckBox)form.getControlByName("isAccepted");
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
    
    /**
     * @param parentField
     * @param rankComboBox
     * @param nodeInForm
     */
    @SuppressWarnings("unchecked")
    protected void adjustRankComboBoxModel(final GetSetValueIFace parentField, 
                                           final ValComboBox rankComboBox, 
                                           final T nodeInForm)
    {
        log.debug("Adjusting the model for the 'rank' combo box in a tree node form");
        log.debug("nodeInForm = " + nodeInForm.getName());
        if (nodeInForm == null)
        {
            return;
        }
        
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
        log.debug("highest valid tree level: " + topItem);
        
        if (topItem == null)
        {
            // this only happens if a parent was chosen that cannot have children b/c it is at the
            // lowest defined level in the tree
            log.warn("Chosen node cannot be a parent node.  It is at the lowest defined level of the tree.");
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
        
        log.debug("lowest valid tree level:  " + bottomItem);

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
                    log.debug("setting rank selected value to " + modelItem);
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
    
    
    //----------------------------------------------------------------------------------
    //-- Inner Class that implements a special query builder.
    //----------------------------------------------------------------------------------
    class SearchQueryBuilder implements ViewBasedSearchQueryBuilderIFace
    {
        protected T                     nodeInForm;
        protected List<ERTICaptionInfo> cols        = new Vector<ERTICaptionInfo>();
        
        /**
         * @param nodeInForm
         */
        public SearchQueryBuilder(T nodeInForm)
        {
            this.nodeInForm = nodeInForm;
        }
        
        public String buildSQL(String searchText)
        {
            String queryStr = "";
            if (QueryAdjusterForDomain.getInstance().isUerInputNotInjectable(searchText))
            {
                int disciplineID = Discipline.getCurrentDiscipline().getId();
    
                // get node table and primary key column names
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
                String tableName = tableInfo.getName();
                String idColName = tableInfo.getIdColumnName();
                
                // get definition table and primary key column names
                DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
                String defTableName = defTableInfo.getName();
                String defIdColName = defTableInfo.getIdColumnName();
                
                String queryFormatStr = "SELECT n.FullName, n.%s from %s n INNER JOIN %s d ON n.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE lower(n.FullName) LIKE \'%s\' AND dsp.DisciplineID = %d ORDER BY n.FullName asc";
                queryStr = String.format(queryFormatStr, idColName, tableName, defTableName, defIdColName, defIdColName, defIdColName, defIdColName, searchText.toLowerCase() + "%", disciplineID);
                log.debug(queryStr);
            }
            return queryStr;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
         */
        public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
        {
            int disciplineId = Discipline.getCurrentDiscipline().getId();

            // get node table and primary key column names
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName());
            String tableName = tableInfo.getName();
            String idColName = tableInfo.getIdColumnName();
            
            // get definition table and primary key column names
            DBTableInfo defTableInfo = DBTableIdMgr.getInstance().getByClassName(tableInfo.getRelationshipByName("definition").getClassName());
            String defTableName = defTableInfo.getName();
            String defIdColName = defTableInfo.getIdColumnName();
            
            cols.clear();
            
            StringBuilder colNames = new StringBuilder();
            int dspCnt = 0;
            for (String colName : fieldNames)
            {
                if (dspCnt > 0) colNames.append(',');
                colNames.append("n.");
                colNames.append(colName);

                ERTICaptionInfo col = new ERTICaptionInfo("n."+colName, colName, true, null, dspCnt+1);
                cols.add(col);
                dspCnt++;
            }
            
            StringBuilder orderBy  = new StringBuilder();
            StringBuilder criteria = new StringBuilder();
            int criCnt = 0;
            for (String colName : dataMap.keySet())
            {
                String data = (String)dataMap.get(colName);
                if (StringUtils.isNotEmpty(data))
                {
                    if (criCnt > 0) criteria.append(" OR ");
                    criteria.append("lower(n.");
                    criteria.append(colName);
                    criteria.append(") LIKE ");
                    criteria.append("\'%");
                    criteria.append(data.toLowerCase());
                    criteria.append("%\'");
                    
                    if (criCnt > 0) orderBy.append(',');
                    orderBy.append("n."+colName);
                    
                    criCnt++;
                }
            }
            
            String queryFormatStr = "SELECT n.%s, %s from %s n INNER JOIN %s d ON n.%s = d.%s INNER JOIN discipline dsp ON d.%s = dsp.%s WHERE (%s) AND dsp.DisciplineID = %d ORDER BY %s";
            String queryStr = String.format(queryFormatStr, idColName, colNames.toString(), tableName, defTableName, defIdColName, defIdColName, 
                                            defIdColName, defIdColName, criteria.toString(), disciplineId, orderBy.toString());
            return queryStr;
        }

        public QueryForIdResultsIFace createQueryForIdResults()
        {
            return new TableSearchResults(DBTableIdMgr.getInstance().getByClassName(nodeInForm.getClass().getName()), cols);
        }
    }
    
    class TableSearchResults implements QueryForIdResultsIFace
    {
        protected List<ERTICaptionInfo> cols;
        protected String      sql;
        protected DBTableInfo tableInfo;
        
        /**
         * @param tableId
         */
        public TableSearchResults(final DBTableInfo tableInfo, 
                                  final List<ERTICaptionInfo> cols)
        {
            this.tableInfo  = tableInfo;
            this.cols     = cols;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#cleanUp()
         */
        public void cleanUp()
        {
            cols = null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getBannerColor()
         */
        public Color getBannerColor()
        {
            return new Color(30, 144, 255);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDescription()
         */
        public String getDescription()
        {
            return tableInfo.getDescription();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDisplayOrder()
         */
        public Integer getDisplayOrder()
        {
            return 0;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getIconName()
         */
        public String getIconName()
        {
            return tableInfo.getName();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getRecIds()
         */
        public Vector<Integer> getRecIds()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSearchTerm()
         */
        public String getSearchTerm()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSQL(java.lang.String, java.util.Vector)
         */
        public String getSQL(String searchTerm, Vector<Integer> ids)
        {
            return sql;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTableId()
         */
        public int getTableId()
        {
            return tableInfo.getTableId();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTitle()
         */
        public String getTitle()
        {
            return tableInfo.getTitle();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getVisibleCaptionInfo()
         */
        public List<ERTICaptionInfo> getVisibleCaptionInfo()
        {
            return cols;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
         */
        public boolean isExpanded()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isHQL()
         */
        public boolean isHQL()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setSQL(java.lang.String)
         */
        public void setSQL(String sql)
        {
            this.sql = sql;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
         */
        public boolean shouldInstallServices()
        {
            return true;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
         */
        public boolean enableEditing()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
         */
        public void removeIds(List<Integer> ids)
        {
            // no op
        }
    }
}