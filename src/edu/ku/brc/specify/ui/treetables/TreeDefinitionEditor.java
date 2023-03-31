/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.TreeDefItemStandardEntry;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.tasks.TreeTaskMgr;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class TreeDefinitionEditor <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
									extends BaseSubPane
{
	private static final Logger log  = Logger.getLogger(TreeDefinitionEditor.class);
	

	//////////////////
	// Non-GUI members
	//////////////////
	
	protected D displayedDef;
    
    protected BusinessRulesIFace businessRules;

    public static final String TREE_DEF_EDIT = "treedefedit";
    public static final String TREE_DEF_EDITOR = "treedefeditor";
    public static final String TREE_DEF_EDIT_TYPE = "treedefedittype";
        
    public static final int NEW_ITEM = 0;
    public static final int DELETED_ITEM = 1;
    public static final int EDITED_ITEM = 2;
    public static final int EDITED_DEF = 3;
    
	//////////////
	// GUI widgets
	//////////////
	
	// panels
	protected JPanel             titlePanel;
	protected EditDeleteAddPanel edaPanel = null;

	// main user interaction widget
	protected JTable defItemsTable;
	protected TreeDefEditorTableModel<T,D,I> tableModel;
	
	// north panel widgets
	protected JLabel defNameLabel;
	protected JButton editDefButton;
	
	protected JStatusBar statusBar;
	
    protected boolean isEditMode;
    protected boolean doUnlock = true;
	
	/**
	 * @param treeDef
	 * @param name
	 * @param task
	 * @param isEditMode
	 */
	public TreeDefinitionEditor(final D treeDef, 
	                            final String name, 
	                            final Taskable task, 
	                            final boolean isEditMode)
	{
		super(name,task);
        
		this.isEditMode = isEditMode;
		
        if (treeDef == null)
        {
            throw new NullPointerException("treeDef must be non-null"); //$NON-NLS-1$
        }
        
		displayedDef = treeDef;
		
        businessRules = DBTableIdMgr.getInstance().getBusinessRule(treeDef.getNodeClass());
        UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24); //$NON-NLS-1$
		initUI();
        
		repaint();
		
		initTreeDefEditorComponent(displayedDef);
        
        UIRegistry.clearGlassPaneMsg();
        
        selectionValueChanged();
	}
	
	/**
	 * @return
	 */
	public D getDisplayedTreeDef()
	{
		return displayedDef;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
	 */
	@Override
	public boolean aboutToShutdown()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
	 */
	@Override
	public void shutdown()
	{
	    if (doUnlock)
	    {
	        TaskSemaphoreMgr.unlock(name, displayedDef.getClass().getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline);
	    }
		super.shutdown();
        
        TreeTaskMgr.checkLocks();
	}
    
    /*  *
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
	 * @param isEditMode whether to enable editing.
	 */
	protected void initUI()
	{
		this.setLayout(new BorderLayout());
		
		Dimension horizSpacer = new Dimension(5,0);
		
		statusBar = UIRegistry.getStatusBar();
		
		// create north panel
		titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel,BoxLayout.LINE_AXIS));
		
        defNameLabel = createLabel(""); //$NON-NLS-1$
        
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(defNameLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

		if (isEditMode)
		{
    		//editDefButton = createButton(editIcon);
		    editDefButton = UIHelper.createIconBtn("EditIcon", "TTV_EDIT_TREDEF_TITLE", new ActionListener() //$NON-NLS-1$ //$NON-NLS-2$
            {
                public void actionPerformed(ActionEvent ae)
                {
                    showDefEditDialog(displayedDef);    
                }
            });
		    editDefButton.setEnabled(true);
		    
    		// add north panel widgets
    		titlePanel.add(Box.createRigidArea(horizSpacer));
    		titlePanel.add(editDefButton);
    		
    		
		} else
		{
		    editDefButton = null;
		}
		titlePanel.add(Box.createHorizontalGlue());
		
		if (isEditMode)
        {
	        // create south panel
    		ActionListener deleteAction = new ActionListener()
    		{
    			public void actionPerformed(ActionEvent ae)
    			{
    				deleteItem(defItemsTable.getSelectedRow());
    			}
    		};
            ActionListener newItemAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    newItem(defItemsTable.getSelectedRow());
                }
            };
            
            ActionListener editItemAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    editTreeDefItem(defItemsTable.getSelectedRow());
                }
            };
		
            edaPanel = new EditDeleteAddPanel(editItemAction, deleteAction, newItemAction,
                                             "TTV_EDIT_TDI", "TTV_DEL_TDI", "TTV_NEW_TDI"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
	}
    
	/**
	 * @param treeDef
	 */
	protected void initTreeDefEditorComponent(final D treeDef)
	{
		Set<I> defItems = treeDef.getTreeDefItems();
		tableModel    = new TreeDefEditorTableModel<T,D,I>(defItems);
		defItemsTable = new JTable(tableModel);
		defItemsTable.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
		
        defItemsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount()==2)
                {
                     SwingUtilities.invokeLater(new Runnable(){

						/* (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run()
						{
		                    editTreeDefItem( defItemsTable.getSelectedRow());
						}
                    	
                    });
                }
            }
        });
		defItemsTable.setRowHeight(24);

		// Center the boolean Columns
		BiColorTableCellRenderer centeredRenderer = new BiColorTableCellRenderer();
		
		TableColumn tc = defItemsTable.getColumnModel().getColumn(2);
		tc.setCellRenderer(centeredRenderer);
        tc = defItemsTable.getColumnModel().getColumn(3);
        tc.setCellRenderer(centeredRenderer);
        
		if (isEditMode)
		{
		    defItemsTable.setRowSelectionAllowed(true);
		    defItemsTable.setColumnSelectionAllowed(false);
		    defItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else
		{
		    defItemsTable.setRowSelectionAllowed(false);
		}
		
		UIHelper.makeTableHeadersCentered(defItemsTable, false);
		
		defNameLabel.setText(treeDef.getName());
		Font f = defNameLabel.getFont();
		Font boldF = f.deriveFont(Font.BOLD);
		defNameLabel.setFont(boldF);

		// put everything in the main panel
		this.add(UIHelper.createScrollPane(defItemsTable), BorderLayout.CENTER);
		this.add(titlePanel,BorderLayout.NORTH);
		
		// Only add selection listener if the botton panel is there for editing
		if (edaPanel != null)
		{
		    PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,10px", "p")); //$NON-NLS-1$ //$NON-NLS-2$
		    pb.add(edaPanel, new CellConstraints().xy(2, 1));
		    add(pb.getPanel(), BorderLayout.SOUTH);
	        addSelectionListener();
		}
		
		repaint();
	}

    /**
     * Adds a selection listener.
     */
    protected void addSelectionListener()
    {
        ListSelectionListener sl = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                clearStatus();
                selectionValueChanged();
            }
        };
        defItemsTable.getSelectionModel().addListSelectionListener(sl);
    }
    
    /**
     * The selection changed.
     */
    protected void selectionValueChanged()
    {
        if (isEditMode)
        {
            int selectionIndex = defItemsTable.getSelectedRow();
            
            if (selectionIndex ==- 1)
            {
                edaPanel.getDelBtn().setEnabled(false);
                edaPanel.getAddBtn().setEnabled(false);
                edaPanel.getEditBtn().setEnabled(false);
            }
            else
            {
                // if there are no business rules associated with this item, we assume it is open for deletion
                if (businessRules == null || businessRules.okToEnableDelete(tableModel.get(selectionIndex)))
                {
                    edaPanel.getDelBtn().setEnabled(true);
                }
                else
                {
                    edaPanel.getDelBtn().setEnabled(false);
                }
                edaPanel.getAddBtn().setEnabled(true);
                edaPanel.getEditBtn().setEnabled(true);
            }
        }
    }
    
    /**
     * 
     */
    public void clearStatus()
    {
        statusBar.setText(null);
    }
    
    /**
     * @param error
     */
    public void showError(String error)
    {
        statusBar.setErrorMessage(error, null);
    }
    
    /**
     * @param message
     */
    public void showMessage(String message)
    {
        statusBar.setText(message);
    }
	
    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods to handle the editing of an existing TreeDefinitionItemIface object.
    /////////////////////////////////////////////////////////////////////////////////////////
    
    protected boolean equalsWithNullChecks(final Object obj1, final Object obj2)
    {
    	if (obj1 == null && obj2 == null)
    	{
    		return true;
    	}
    	if (obj1 == null && obj2 != null)
    	{
    		return false;
    	}
    	if (obj1 != null && obj2 == null)
    	{
    		return false;
    	}
    	return obj1.equals(obj2);
    }
    /**
     * @param before
     * @param after
     * @return true if changes to after require FullNames in the tree to be updated.
     */
    protected boolean needToRebuildFullNames(final I before, final I after)
    {
        if (!equalsWithNullChecks(before.getIsInFullName(), after.getIsInFullName()))
        {
        	return true;
        }
        if (!equalsWithNullChecks(before.getTextBefore(), after.getTextBefore()))
        {
        	return after.getIsInFullName();
        }
        if (!equalsWithNullChecks(before.getTextAfter(), after.getTextAfter()))
        {
        	return after.getIsInFullName();
        }
        if (!equalsWithNullChecks(before.getFullNameSeparator(), after.getFullNameSeparator()))
        {
        	return after.getIsInFullName();
        }
        return false;
    }
    /**
     * @param index index of item to edit
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    protected void editTreeDefItem(final int index)
    {
        if (index ==- 1 || !isEditMode)
        {
            return;
        }
        
        log.info("Edit row " + index); //$NON-NLS-1$
        
        // load a fresh copy of the item from the DB
        I uiDefItem = tableModel.get(index);
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final I defItem = (I)tmpSession.load(uiDefItem.getClass(), uiDefItem.getTreeDefItemId());
        log.info("loaded defItem for editing");
        if (defItem == null)
        {
            statusBar.setErrorMessage("The tree def has been changed by another user.  The def editor must be reloaded."); //$NON-NLS-1$
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    log.info("initializing tree editor"); //$NON-NLS-1$
                   UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24); //$NON-NLS-1$
                    
                    initTreeDefEditorComponent(displayedDef);
                    log.info("initialized tree editor");
                    repaint();
                    
                    UIRegistry.clearGlassPaneMsg();
                    
                    log.info("invoking selectionValueChanged()");
                    selectionValueChanged();
                }
            });
            tmpSession.close();
            return;
        }
        tmpSession.close();
        
        defItem.setDisplayText(defItem.getDisplayText());
        // keep track of what these values are before the edits happen
        final I beforeItem = (I)TreeFactory.createNewTreeDefItem(defItem.getClass(),null,null);
        log.info("created beforeItem"); //$NON-NLS-1$
        beforeItem.setIsInFullName(boolVal(defItem.getIsInFullName(), false));
        beforeItem.setTextBefore(defItem.getTextBefore());
        beforeItem.setTextAfter(defItem.getTextAfter());
        beforeItem.setFullNameSeparator(defItem.getFullNameSeparator());
        beforeItem.setIsEnforced(boolVal(defItem.getIsEnforced(), false));
        
        // TODO: double check these choices
        // gather all the info needed to create a form in a dialog
        String viewName = TreeFactory.getAppropriateViewName(defItem);
        Frame parentFrame = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String displayName = "NODE_EDIT_DISPLAY_NAME"; //$NON-NLS-1$
        boolean isEdit = true;
        String closeBtnText = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE"); //$NON-NLS-1$ //$NON-NLS-2$
        String className = defItem.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)defItem).getTableId());
        String idFieldName = nodeTableInfo.getIdFieldName();
        int options = MultiView.HIDE_SAVE_BTN;
        
        // create the form dialog
        String title = getResourceString("TreeRankEditDialogTitle"); //$NON-NLS-1$
        log.info("creating dialog"); //$NON-NLS-1$
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, title, 
                                                                   closeBtnText, className, idFieldName, isEdit, options);
        log.info("created dialog"); //$NON-NLS-1$
        dialog.setModal(true);
        dialog.setData(defItem);
        dialog.preCreateUI();
        dialog.setVisible(true);
        
        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24); //$NON-NLS-1$
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                I mergedItem;
                
                @SuppressWarnings("synthetic-access") //$NON-NLS-1$ //$NON-NLS-2$
                @Override
                public Object construct()
                {
                    // determine if the change can be made without requiring tree node changes
                	if (needToRebuildFullNames(beforeItem, defItem))
                	//May not be too hard to just pass the nodesToChange for full name updates...
                    //List<String> nodesToChange = getNodesThatMustBeFixedBeforeEdit(beforeItem, defItem);
                    //if (nodesToChange != null && nodesToChange.size() > 0)
                    {
                    	if (!UIRegistry.displayConfirmLocalized(UIRegistry.getResourceString("Confirm"), 
                                "TDE_ChangesRequireFullNameUpdate",
                                "OK",
                                "Cancel",
                                JOptionPane.INFORMATION_MESSAGE))
                    	{
                    		return false;
                    	}
                    }
                        
                    // save the node and update the tree viewer appropriately
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        mergedItem = session.merge(defItem);
                    }
                    catch (StaleObjectException e1)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e1);
                        // another user or process has changed the data "underneath" us
                        UIRegistry.showLocalizedError("UPDATE_DATA_STALE"); //$NON-NLS-1$
                        if (session != null)
                        {
                            session.close();
                        }
                        session = DataProviderFactory.getInstance().createSession();
                        mergedItem = (I)session.load(defItem.getClass(), defItem.getTreeDefItemId());
                        success = false;
                        return success;
                    }
                    success = true;
                    
                    if (businessRules != null)
                    {
                        businessRules.beforeSave(mergedItem,session);
                    }
                    
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(mergedItem);
                        if (businessRules != null)
                        {
                            if (!businessRules.beforeSaveCommit(mergedItem, session))
                            {
                                throw new Exception("Business rules processing failed"); //$NON-NLS-1$
                            }
                        }
                        
                        session.commit();
                        
                        notifyApplication(mergedItem, EDITED_ITEM);
                        log.info("Successfully saved changes to " + mergedItem.getName()); //$NON-NLS-1$
                        
                        // at this point, the new node is in the DB (if success == true)
                        if (businessRules != null && success == true)
                        {
                            businessRules.afterSaveCommit(defItem, session);
                        }

                    }
                    catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e);
                        success = false;
                        UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$

                        log.error("Error while saving node changes.  Rolling back transaction.", e); //$NON-NLS-1$
                        session.rollback();
                    }
                    finally
                    {
                        session.close();
                    }
                    
                    return success;
                }

                @Override
                public void finished()
                {
                    // now refresh the tree viewer
                    if (success)
                    {
                    	if (needToRebuildFullNames(beforeItem, defItem))
                        {
                        	try
                        	{
                        		displayedDef.updateAllFullNames(null, true, true, defItem.getRankId());
                        	}
                        	catch (Exception ex)
                        	{
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, ex);
                                UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$
                                log.error("Error while updating full names.  Full names may not correspond to tree definition.", ex); //$NON-NLS-1$
                        	}
                        }
                        tableModel.set(index, mergedItem);
                    }
                    
                    UIRegistry.clearGlassPaneMsg();
                }
            };
            
            bgThread.start();
        }
        else
        {
            // the user didn't save any edits (if there were any)
        }
    }
    
    /**
     * Gets latest version of displatedDef.
     * This is necessary to ensure that requests for Collection info from AppContextMgr 
     * (e.g "(SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass()") reflect all edits. 
     */
    @SuppressWarnings("unchecked")
    protected void refreshDef()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            displayedDef = (D )session.get(displayedDef.getClass(), displayedDef.getTreeDefId());
            ((DataModelObjBase )displayedDef).forceLoad();
        }
        finally
        {
            session.close();
        }
        
    }
    /**
     * @param editItem
     * 
     * 
     */
    protected void notifyApplication(Object editItem, int action)
    {
        //Make sure AppContextMgr has updated class object for the tree definition (could also make SpecifyAppContextMgr a CommandListener????)
        //AppContextMgr.getInstance().setClassObject(displayedDef.getClass(), displayedDef);
        refreshDef();
        AppContextMgr.getInstance().setClassObject(displayedDef.getClass(), displayedDef);
        
        //notify command listeners that tree was edited.
        final CommandAction cmd = new CommandAction(TREE_DEF_EDITOR, TREE_DEF_EDIT, editItem);
        cmd.setProperty(TREE_DEF_EDIT_TYPE, action);
        CommandDispatcher.dispatch(cmd);        
    }
    
    /**
     * @param itemBeforeEdits
     * @param itemAfterEdits
     * @return
     */
    protected List<String> getNodesThatMustBeFixedBeforeEdit(final I itemBeforeEdits, final I itemAfterEdits)
    {
        boolean fullnameBefore  = boolVal(itemBeforeEdits.getIsInFullName(), false);
        boolean enforcedBefore  = boolVal(itemBeforeEdits.getIsEnforced(), false);
        String textBeforeBefore = stringVal(itemBeforeEdits.getTextBefore(), ""); //$NON-NLS-1$
        String textAfterBefore  = stringVal(itemBeforeEdits.getTextAfter(), ""); //$NON-NLS-1$
        String separatorBefore  = stringVal(itemBeforeEdits.getFullNameSeparator(), ""); //$NON-NLS-1$
        
        boolean fullname  = boolVal(itemAfterEdits.getIsInFullName(), false);
        boolean enforced  = boolVal(itemAfterEdits.getIsEnforced(), false);
        String textBefore = stringVal(itemAfterEdits.getTextBefore(), ""); //$NON-NLS-1$
        String textAfter  = stringVal(itemAfterEdits.getTextAfter(), ""); //$NON-NLS-1$
        String separator  = stringVal(itemAfterEdits.getFullNameSeparator(), ""); //$NON-NLS-1$

        boolean fullnameChanged = (fullnameBefore != fullname);
        boolean enforcedChanged = (enforcedBefore != enforced);
        boolean textBeforeChanged = !textBeforeBefore.equals(textBefore);
        boolean textAfterChanged  = !textAfterBefore.equals(textAfter);
        boolean separatorChanged = !separatorBefore.equals(separator);
        
        List<String> problematicNodes = null;
        
        TreeDataService<T,D,I> treeDataServ = TreeDataServiceFactory.createService();

        if (fullnameChanged || textBeforeChanged || textAfterChanged || separatorChanged)
        {
            problematicNodes = treeDataServ.nodeNamesAtLevel(itemAfterEdits.getRankId(), itemAfterEdits.getTreeDef());
        }
        
        if (enforcedChanged && enforced == true)
        {
            try
            {
                problematicNodes = treeDataServ.nodesSkippingOverLevel(itemAfterEdits.getRankId(), itemAfterEdits.getTreeDef());
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e);
                System.err.println(e);
            }
        }
        
        return problematicNodes;
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
    protected TreeDefItemStandardEntry getNewItemStdLevel(final I parent, final I child)
    {
        List<TreeDefItemStandardEntry> stds = displayedDef.getStandardLevels();
        List<TreeDefItemStandardEntry> options = new LinkedList<TreeDefItemStandardEntry>();
        for (TreeDefItemStandardEntry std : stds)
        {
            if (std.getRank() > parent.getRankId()
                    && (child == null || std.getRank() < child.getRankId()))
            {
                options.add(std);
            }
        }
        if (options.size() == 0)
        {
            return new TreeDefItemStandardEntry("", -1);
        }
        //else
        ChooseFromListDlg<TreeDefItemStandardEntry> dlg = new ChooseFromListDlg<TreeDefItemStandardEntry>((Frame )UIRegistry.getTopWindow(),
                UIRegistry.getResourceString("TreeDefinitionEditor.41"), ChooseFromListDlg.OKCANCELAPPLY, options); //$NON-NLS-1$
        dlg.setApplyLabel(UIRegistry.getResourceString("TreeDefinitionEditor.42")); //$NON-NLS-1$
        dlg.setCloseOnApply(true);
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);
        TreeDefItemStandardEntry selection = null;
        if (dlg.getBtnPressed() == ChooseFromListDlg.OK_BTN)
        {
            selection = dlg.getSelectedObject();
        }
        else if (dlg.getBtnPressed() == ChooseFromListDlg.APPLY_BTN)
        {
        	selection = new TreeDefItemStandardEntry("", -1);
        }
        dlg.dispose();
        return selection;
    }
    
    protected Integer getNewItemRank(final TreeDefItemStandardEntry stdLevel, final I parent, final I child)
    {
        Integer result = null;
        if (stdLevel != null && stdLevel.getRank() != -1)
        {
            result = stdLevel.getRank();
        }
        
        else if (child != null)
        {
            result = (int)(.5 * (child.getRankId() + parent.getRankId()));
        }
        
        else 
        {
            TreeDefItemStandardEntry stdChild = null;
            List<TreeDefItemStandardEntry> stds = displayedDef.getStandardLevels();
            Collections.sort(stds);
            for (TreeDefItemStandardEntry std : displayedDef.getStandardLevels())
            {
               //if (std.getTitle().equals(nodeInForm.getName()) && std.getRank() == nodeInForm.getRankId())
               if (std.getRank() > parent.getRankId())
                {
            	   stdChild = std;
                    break;
                }
            }
            if (stdChild == null)
            {
            	result = parent.getRankId() + displayedDef.getRankIncrement();
            }
            else
            {
            	result = (int)(.5 * (stdChild.getRank() + parent.getRankId()));
            }
        }
        
        if (displayedDef.getDefItemByRank(result) != null)
        {
            result = null; 
            //rank is already in use. What to do now?
        }
        return result;
    }
    
	/**
	 * @param index index of new item
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void newItem(final int index)
	{
		if(index==-1)
		{
			return;
		}
		
        // load a fresh copy of the parent from the DB
        I uiParent = tableModel.get(index);
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final I parent = (I)tmpSession.load(uiParent.getClass(), uiParent.getTreeDefItemId());
        if (parent == null)
        {
            statusBar.setErrorMessage(UIRegistry.getResourceString("TreeDefinitionEditor.18")); //$NON-NLS-1$
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24); //$NON-NLS-1$
                    
                    initTreeDefEditorComponent(displayedDef);
                    repaint();
                    
                    UIRegistry.clearGlassPaneMsg();
                    
                    selectionValueChanged();
                }
            });
            tmpSession.close();
            return;
        }
        tmpSession.close();
        
        final I origChild = parent.getChild();
		final I newItem = (I)TreeFactory.createNewTreeDefItem(parent.getClass(),null,UIRegistry.getResourceString("TreeDefinitionEditor.46")); //$NON-NLS-1$
		final TreeDefItemStandardEntry stdLevel = getNewItemStdLevel(parent, origChild);
		if (stdLevel == null)
		{
			return;
		}
		final Integer rank = getNewItemRank(stdLevel, parent, origChild);
		
		if (rank == null)
		{
		    log.error("Unable to determine rank for new item. Parent=" + parent + ". Child=" + origChild + "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    UIRegistry.getStatusBar().setErrorMessage(UIRegistry.getResourceString("TreeDefinitionEditor.50")); //$NON-NLS-1$
		    return;
		}
        newItem.setRankId(rank);
        final boolean isRequiredLevel = displayedDef.isRequiredLevel(stdLevel.getRank());
        newItem.setIsEnforced(isRequiredLevel);
        if (stdLevel.getRank() != -1 )
        {
            newItem.setName(stdLevel.getTitle());
            newItem.setDisplayText(stdLevel.getTitle());
        }
        // we can only set the pointers from the newItem side right now
        // otherwise, if the user cancels, we end up with 'dirty' collections in the other objects
        newItem.setTreeDef(displayedDef);
		newItem.setParent(parent);
        if (origChild != null)
        {
            newItem.setChild(origChild);
            origChild.setParent(newItem);
        }

 
        // TODO: double check these choices
        // gather all the info needed to create a form in a dialog
        String viewName = TreeFactory.getAppropriateViewName(newItem);
        
        Frame parentFrame = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String displayName = "NODE_EDIT_DISPLAY_NAME"; //$NON-NLS-1$
        boolean isEdit = true;
        String closeBtnText = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE"); //$NON-NLS-1$ //$NON-NLS-2$
        String className = newItem.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)newItem).getTableId());
        String idFieldName = nodeTableInfo.getIdFieldName();
        int options = MultiView.HIDE_SAVE_BTN | MultiView.IS_NEW_OBJECT;
        
        // create the form dialog
        String title = getResourceString("TreeRankEditDialogTitle"); //$NON-NLS-1$
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, title, 
                                                                   closeBtnText, className, idFieldName, isEdit, options);
        dialog.setModal(true);
        dialog.setData(newItem);
        dialog.preCreateUI();
        if (stdLevel != null)
        {
            //don't force edits to standard level before enabling save button.
            //This is kind of cheap, but, currently, for new items, saveBtn is the same as the okBtn.
            dialog.getOkBtn().setEnabled(true);
        }
        dialog.setVisible(true);

        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24); //$NON-NLS-1$
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                
                @SuppressWarnings("synthetic-access") //$NON-NLS-1$ //$NON-NLS-2$
                @Override
                public Object construct()
                {
                    success = true;
                    // determine if the change can be made without requiring tree node changes
                    if (newItem.getIsEnforced() != null && newItem.getIsEnforced().booleanValue() == true)
                    {
                        List<String> nodesToChange = getNodesThatMustBeFixedToEnforceNewLevel(newItem);
                        if (nodesToChange != null && nodesToChange.size() > 0)
                        {
                            StringBuilder message = new StringBuilder("<html><h3><center>"); //$NON-NLS-1$
                            if (isRequiredLevel)
                            {
                            	//This should only be possible if required levels were not added when
                            	//a treeDef was originally created. 
                            	//Since new treeDefs cannot be created in Specify6.0, it should never happen,
                            	//as long as db wizards and converters ensure that all required levels
                            	//are present in treeDefs.
                            	message.append(getResourceString("TDE_CantAddNewEnforcedLevel"));
                            }
                            else
                            {
                            	message.append(getResourceString("TDE_CantEnforceNewLevel")); //$NON-NLS-1$
                            }
                            message.append("</center></h3><ul>"); //$NON-NLS-1$
                            for (String node: nodesToChange)
                            {
                                message.append("<li>" + node); //$NON-NLS-1$
                            }
                            message.append("</ul></html>"); //$NON-NLS-1$
                            UIRegistry.showLocalizedError(message.toString());
                            if (isRequiredLevel)
                            {
                            	success = false;
                            }
                            else
                            {
                            	newItem.setIsEnforced(false);
                            }
                        }
                    }

                    if (success)
					{
						// save the node and update the tree viewer
						// appropriately
						DataProviderSessionIFace session = DataProviderFactory
								.getInstance().createSession();

						if (businessRules != null)
						{
							businessRules.beforeSave(newItem, session);
							if (origChild != null)
							{
								businessRules.beforeSave(origChild, session);
							}
						}

						try
						{
							session.beginTransaction();
							session.saveOrUpdate(newItem);
							if (businessRules != null)
							{
								if (!businessRules.beforeSaveCommit(newItem,
										session))
								{
									throw new Exception(
											"Business rules processing failed"); //$NON-NLS-1$
								}
								if (origChild != null)
								{
									if (!businessRules.beforeSaveCommit(
											origChild, session))
									{
										throw new Exception(
												"Business rules processing failed"); //$NON-NLS-1$
									}
								}
							}
							session.commit();
							// Rank-checking requires displayedDef to up-to-date
							// wrt inserts and deletes:
							displayedDef.getTreeDefItems().add(newItem);

							notifyApplication(newItem, NEW_ITEM);

							log
									.info("Successfully saved changes to " + newItem.getName()); //$NON-NLS-1$

						} catch (Exception e)
						{
							edu.ku.brc.af.core.UsageTracker
									.incrHandledUsageCount();
							edu.ku.brc.exceptions.ExceptionTracker
									.getInstance().capture(
											TreeDefinitionEditor.class, e);
							success = false;
							UIRegistry
									.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$

							log
									.error(
											"Error while saving node changes.  Rolling back transaction.", e); //$NON-NLS-1$
							session.rollback();
						} finally
						{
							session.close();
						}

						// at this point, the new node is in the DB (if success
						// == true)

						session = DataProviderFactory.getInstance()
								.createSession();
						session.refresh(newItem);
						session.refresh(parent);
						if (origChild != null)
						{
							session.refresh(origChild);
						}

						if (businessRules != null && success == true)
						{
							businessRules.afterSaveCommit(newItem, session);
							if (origChild != null)
							{
								businessRules.afterSaveCommit(origChild,
										session);
							}
						}
						session.close();
					}   
                    return success;
                }

                @Override
                public void finished()
                {
                    // now refresh table row
                    if (success)
                    {
                        tableModel.set(index, parent);
                        tableModel.add(newItem, parent);
                        if (origChild != null)
                        {
                            tableModel.set(index+2, origChild);
                        }
                    }
                    
                    UIRegistry.clearGlassPaneMsg();
                }
            };
            
            bgThread.start();
        }
        else
        {
            // the user didn't save any edits (if there were any)
            newItem.setTreeDef(null);
            newItem.setParent(null);
            newItem.setChild(null);
        }
	}
    
    /**
     * @param newItem the new item
     * @return a list
     */
    protected List<String> getNodesThatMustBeFixedToEnforceNewLevel(final I newItem)
    {
        boolean enforced  = boolVal(newItem.getIsEnforced(), false);

        List<String> problematicNodes = null;

        if (enforced)
        {
            TreeDataService<T,D,I> treeDataServ = TreeDataServiceFactory.createService();
            problematicNodes = treeDataServ.nodesSkippingOverLevel(newItem.getRankId(), newItem.getTreeDef());
        }
        
        return problematicNodes;
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the deletion of an existing TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
    protected void deleteItem(final int index)
	{
        if(index==-1)
        {
            return;
        }

        // load this item from the DB
        I uiItem = tableModel.get(index);
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final I itemToDelete = (I)tmpSession.load(uiItem.getClass(), uiItem.getTreeDefItemId());
        if (itemToDelete == null)
        {
            statusBar.setErrorMessage(UIRegistry.getResourceString("TreeDefinitionEditor.17")); //$NON-NLS-1$
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24); //$NON-NLS-1$
                    
                    initTreeDefEditorComponent(displayedDef);
                    repaint();
                    
                    UIRegistry.clearGlassPaneMsg();
                    
                    selectionValueChanged();
                }
            });
            tmpSession.close();
            return;
        }
        tmpSession.close();
        
        UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Deleting"), 24); //$NON-NLS-1$
        
        SwingWorker bgThread = new SwingWorker()
        {
            boolean success;
            I mergedItem;
            
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            public Object construct()
            {
                // save the node and update the tree viewer appropriately
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    mergedItem = session.merge(itemToDelete);
                }
                catch (StaleObjectException e1)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e1);
                    // another user or process has changed the data "underneath" us
                    UIRegistry.showLocalizedError("UPDATE_DATA_STALE"); //$NON-NLS-1$

                    if (session != null)
                    {
                        session.close();
                    }
                    session = DataProviderFactory.getInstance().createSession();
                    mergedItem = (I)session.load(itemToDelete.getClass(), itemToDelete.getTreeDefItemId());
                    success = false;
                    return success;
                }
                success = true;

                // remove the item from it's relationships
                I parent = mergedItem.getParent();
                I child = mergedItem.getChild();
                mergedItem.setParent(null);
                mergedItem.setChild(null);
                
                parent.setChild(child);
                if (child!=null)
                {
                    child.setParent(parent);
                }
                
                mergedItem.getTreeDef().getTreeDefItems().remove(mergedItem);
                mergedItem.setTreeDef(null);

                if (businessRules != null)
                {
                    mergedItem = (I)businessRules.beforeDelete(mergedItem,session);
                    businessRules.beforeSave(parent, session);
                    if (child != null)
                    {
                        businessRules.beforeSave(child, session);
                    }
                }
                
                try
                {
                    session.beginTransaction();
                    
                    session.saveOrUpdate(parent);
                    if (child != null)
                    {
                        session.saveOrUpdate(child);
                    }
                    session.delete(mergedItem);
                    
                    if (businessRules != null)
                    {
                        if (!businessRules.beforeDeleteCommit(mergedItem, session))
                        {
                            throw new Exception("Business rules processing failed"); //$NON-NLS-1$
                        }
                        if (!businessRules.beforeSaveCommit(parent, session))
                        {
                            throw new Exception("Business rules processing failed"); //$NON-NLS-1$
                        }
                        if (child != null)
                        {
                            if (!businessRules.beforeSaveCommit(child, session))
                            {
                                throw new Exception("Business rules processing failed"); //$NON-NLS-1$
                            }
                        }
                    }
                    session.commit();
                    
                    //Rank-checking requires displayedDef to up-to-date wrt inserts and deletes:
                    //
                    //itemToDelete may not actually be the same Object as the TreeDefItem in getTreeDefItems so next line
                    //doesn't work (but maybe mergedItem would work instead of itemToDelete?) --
                    //displayedDef.getTreeDefItems().remove(itemToDelete);
                    //so...
                    DataModelObjBase deletedObj = (DataModelObjBase )itemToDelete;
                    for (I item : displayedDef.getTreeDefItems())
                    {
                        if (((DataModelObjBase )item).getId().equals(deletedObj.getId()))
                        {
                            displayedDef.getTreeDefItems().remove(item);
                            break;
                        }
                        
                    }
                    
                    notifyApplication(itemToDelete, DELETED_ITEM);
                    
                    log.info("Successfully deleted " + mergedItem.getName()); //$NON-NLS-1$
                    
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e);
                    success = false;
                    UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$

                    log.error("Error while saving node changes.  Rolling back transaction.", e); //$NON-NLS-1$
                    session.rollback();
                }
                finally
                {
                    session.close();
                }
                
                // at this point, the new node is in the DB (if success == true)

                session = DataProviderFactory.getInstance().createSession();
                session.refresh(parent);
                if (child != null)
                {
                    session.refresh(child);
                }

                if (businessRules != null && success == true)
                {
                    businessRules.afterDeleteCommit(mergedItem);
                    businessRules.afterSaveCommit(parent, session);
                    if (child != null)
                    {
                        businessRules.afterSaveCommit(child, session);
                    }
                }
                
                session.close();
                
                return success;
            }

            @Override
            public void finished()
            {
                // remove the corresponding item from the table model
                tableModel.remove(index);
                
                UIRegistry.clearGlassPaneMsg();
            }
        };
        
        bgThread.start();
	}
    
    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods to handle the editing of a TreeDefinitionIface object.
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Display the form for editing the given tree definition. 
     *
     * @param treeDef the tree definition being edited
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    protected void showDefEditDialog(final D treeDef)
    {
        // load a fresh copy of the parent from the DB
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final D def = (D)tmpSession.load(treeDef.getClass(), treeDef.getTreeDefId());
        if (def == null)
        {
            statusBar.setErrorMessage("The tree currently being displayed has been deleted by another user.  It must now be closed."); //$NON-NLS-1$
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    SubPaneMgr.getInstance().removePane(TreeDefinitionEditor.this);
                }
            });
            tmpSession.close();
            return;
        }
        tmpSession.close();

        // TODO: double check these choices
        // gather all the info needed to create a form in a dialog
        String viewName = TreeFactory.getAppropriateViewName(treeDef);
        Frame   parentFrame  = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String  displayName  = "NODE_EDIT_DISPLAY_NAME"; //$NON-NLS-1$
        boolean isEdit       = true;
        String  closeBtnText = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE"); //$NON-NLS-1$ //$NON-NLS-2$
        String  className    = def.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)def).getTableId());
        String  idFieldName  = nodeTableInfo.getIdFieldName();
        int     options      = MultiView.HIDE_SAVE_BTN;
        
        int fullNameDir = def.getFullNameDirection();
        
        // create the form dialog
        String title = getResourceString("TreeDefEditDialogTitle"); //$NON-NLS-1$
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, title, 
                                                                   closeBtnText, className, idFieldName, isEdit, options);
        dialog.setModal(true);
        dialog.setData(def);
        dialog.preCreateUI();
        dialog.setVisible(true);
        
        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
        	final boolean treeRebuildRequired = fullNameDir != def.getFullNameDirection();
        	UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24); //$NON-NLS-1$
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                D mergedDef;
                
                @SuppressWarnings("synthetic-access") //$NON-NLS-1$ //$NON-NLS-2$
                @Override
                public Object construct()
                {
                	
                	if (treeRebuildRequired)
					{
						if (!UIRegistry.displayConfirmLocalized(UIRegistry
								.getResourceString("Confirm"),
								"TDE_ChangesRequireFullNameUpdate", "OK",
								"Cancel", JOptionPane.INFORMATION_MESSAGE))
						{
							return false;
						}
					}

                	// save the node and update the tree viewer appropriately
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        mergedDef = session.merge(def);
                    }
                    catch (StaleObjectException e1)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e1);
                        // another user or process has changed the data "underneath" us
                        UIRegistry.showLocalizedError("UPDATE_DATA_STALE"); //$NON-NLS-1$

                        if (session != null)
                        {
                            session.close();
                        }
                        session = DataProviderFactory.getInstance().createSession();
                        mergedDef = (D)session.load(def.getClass(), def.getTreeDefId());
                        success = false;
                        return success;
                    }
                    success = true;
                    
                    if (businessRules != null)
                    {
                        businessRules.beforeSave(mergedDef,session);
                    }
                    
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(mergedDef);
                        if (businessRules != null)
                        {
                            if (!businessRules.beforeSaveCommit(mergedDef, session))
                            {
                                throw new Exception("Business rules processing failed"); //$NON-NLS-1$
                            }
                        }
                        session.commit();

                        notifyApplication(displayedDef, EDITED_DEF);
                        log.info("Successfully saved changes to " + mergedDef.getName()); //$NON-NLS-1$
                        
                    }
                    catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, e);
                        success = false;
                        UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$

                        log.error("Error while saving node changes.  Rolling back transaction.", e); //$NON-NLS-1$
                        session.rollback();
                    }
                    finally
                    {
                        session.close();
                    }
                    
                    // at this point, the new node is in the DB (if success == true)

                    session = DataProviderFactory.getInstance().createSession();
                    session.refresh(def);

                    if (businessRules != null && success == true)
                    {
                        businessRules.afterSaveCommit(def, session);
                    }
                    session.close();
                    
                    return success;
                }

                @Override
                public void finished()
                {
                	if (treeRebuildRequired)
                    {
                    	try
                    	{
                    		int minRank = -1;
                    		for (I item : def.getTreeDefItems())
                    		{
                    			if (item.getIsInFullName() != null && item.getIsInFullName() && (minRank == -1 || minRank > item.getRankId()))
                    			{
                    				minRank = item.getRankId();
                    			}
                    		}
                    		displayedDef.updateAllFullNames(null, true, true, minRank);
                    	}
                    	catch (Exception ex)
                    	{
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeDefinitionEditor.class, ex);
                            UIRegistry.showLocalizedError("UNRECOVERABLE_DB_ERROR"); //$NON-NLS-1$
                            log.error("Error while updating full names.  Full names may not correspond to tree definition.", ex); //$NON-NLS-1$
                    	}
                    }
                   // now refresh the tree viewer
                    if (success)
                    {
                        defNameLabel.setText(def.getName());
                    }
                    
                    UIRegistry.clearGlassPaneMsg();
                }
            };
            
            bgThread.start();
        }
        else
        {
            // the user didn't save any edits (if there were any)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Utility methods
    /////////////////////////////////////////////////////////////////////////////////////////

    private boolean boolVal(Boolean b, boolean defaultVal)
    {
        return (b != null) ? b.booleanValue() : defaultVal;
    }
    
    private String stringVal(String s, String defaultVal)
    {
        return (s != null) ? s : defaultVal;
    }
    
//    private <X> X objectVal(X x, X defaultVal)
//    {
//        return (x != null) ? x : defaultVal;
//    }
}
