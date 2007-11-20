/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.BusinessRulesIFace;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.util.Pair;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
@SuppressWarnings("serial")
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

	//////////////
	// GUI widgets
	//////////////
	
	// panels
	protected JPanel northPanel;
	protected JPanel southPanel;
	
	// main user interaction widget
	protected JTable defItemsTable;
	protected TreeDefEditorTableModel<T,D,I> tableModel;
	
	// north panel widgets
	protected JLabel defNameLabel;
	protected JButton editDefButton;
	
	protected JStatusBar statusBar;
	
	// south panel widgets
	protected JButton deleteItemButton;
	protected JButton newItemButton;
    protected JButton editItemButton;
	
	/**
	 *
	 *
	 * @param name
	 * @param task
	 */
	@SuppressWarnings("unchecked")
	public TreeDefinitionEditor(D treeDef, String name, Taskable task)
	{
		super(name,task);
        
        if (treeDef==null)
        {
            throw new NullPointerException("treeDef must be non-null");
        }
        
		displayedDef = treeDef;
        businessRules = DBTableIdMgr.getInstance().getBusinessRule(treeDef.getNodeClass());
        
        UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24);
		initUI();
        
		repaint();
		
		initTreeDefEditorComponent(displayedDef);
        
        UIRegistry.clearGlassPaneMsg();
        
        selectionValueChanged();
	}
	
	public D getDisplayedTreeDef()
	{
		return displayedDef;
	}
	
	@Override
	public boolean aboutToShutdown()
	{
		return true;
	}

	@Override
	public void shutdown()
	{
		super.shutdown();
	}

	protected void initUI()
	{
		this.setLayout(new BorderLayout());
		
		Dimension horizSpacer = new Dimension(5,0);
		
		statusBar = UIRegistry.getStatusBar();
		
		// create north panel
		northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.LINE_AXIS));
		
		// create north panel widgets
		defNameLabel = new JLabel();
        Icon editIcon = IconManager.getIcon("EditIcon", IconSize.Std16);
		editDefButton = new JButton(editIcon);
		editDefButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				showDefEditDialog(displayedDef);	
			}
		});

		// add north panel widgets
		northPanel.add(Box.createHorizontalGlue());
		northPanel.add(defNameLabel);
		northPanel.add(Box.createRigidArea(horizSpacer));
		northPanel.add(editDefButton);
		northPanel.add(Box.createHorizontalGlue());

		// create south panel
		southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.LINE_AXIS));
		
        Icon delIcon = IconManager.getIcon("MinusSign", IconSize.Std16);
		deleteItemButton = new JButton("Delete", delIcon);
		deleteItemButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				deleteItem(defItemsTable.getSelectedRow());
			}
		});
        Icon newIcon = IconManager.getIcon("PlusSign", IconSize.Std16);
        newItemButton = new JButton("New", newIcon);
        newItemButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                newItem(defItemsTable.getSelectedRow());
            }
        });
        editItemButton = new JButton("Edit", editIcon);
        editItemButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                editTreeDefItem(defItemsTable.getSelectedRow());
            }
        });
		
		// add south panel widgets
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(deleteItemButton);
        southPanel.add(Box.createRigidArea(horizSpacer));
        southPanel.add(newItemButton);
        southPanel.add(Box.createRigidArea(horizSpacer));
        southPanel.add(editItemButton);
	}
    
	protected void initTreeDefEditorComponent(D treeDef)
	{
		Set<I> defItems = treeDef.getTreeDefItems();
		tableModel = new TreeDefEditorTableModel<T,D,I>(defItems);
		defItemsTable = new JTable(tableModel);
        defItemsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount()==2)
                {
                    int index = defItemsTable.getSelectedRow();
                    editTreeDefItem(index);
                }
            }
        });
		defItemsTable.setRowHeight(24);

		addSelectionListener();
		defItemsTable.setRowSelectionAllowed(true);
		defItemsTable.setColumnSelectionAllowed(false);
		defItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		defNameLabel.setText(treeDef.getName());
		Font f = defNameLabel.getFont();
		Font boldF = f.deriveFont(Font.BOLD);
		defNameLabel.setFont(boldF);

		// put everything in the main panel
		this.add(new JScrollPane(defItemsTable),BorderLayout.CENTER);
		this.add(northPanel,BorderLayout.NORTH);
		this.add(southPanel,BorderLayout.SOUTH);
		
		repaint();
	}

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
    
    protected void selectionValueChanged()
    {
        int selectionIndex = defItemsTable.getSelectedRow();
        
        if (selectionIndex==-1)
        {
            deleteItemButton.setEnabled(false);
            newItemButton.setEnabled(false);
            editItemButton.setEnabled(false);
        }
        else
        {
            if (businessRules.okToEnableDelete(tableModel.get(selectionIndex)))
            {
                deleteItemButton.setEnabled(true);
            }
            else
            {
                deleteItemButton.setEnabled(false);
            }
            newItemButton.setEnabled(true);
            editItemButton.setEnabled(true);
        }
            
    }
    
    public void clearStatus()
    {
        statusBar.setText(null);
    }
    
    public void showError(String error)
    {
        statusBar.setErrorMessage(error, null);
    }
    
    public void showMessage(String message)
    {
        statusBar.setText(message);
    }
	
    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods to handle the editing of an existing TreeDefinitionItemIface object.
    /////////////////////////////////////////////////////////////////////////////////////////
    
    @SuppressWarnings("unchecked")
    protected void editTreeDefItem(final int index)
    {
        if (index==-1)
        {
            return;
        }
        
        log.info("Edit row " + index);
        
        // load a fresh copy of the item from the DB
        I uiDefItem = tableModel.get(index);
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final I defItem = (I)tmpSession.load(uiDefItem.getClass(), uiDefItem.getTreeDefItemId());
        if (defItem == null)
        {
            statusBar.setErrorMessage("The tree def has been changed by another user.  The def editor must be reloaded.");
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24);
                    
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
        
        // keep track of what these values are before the edits happen
        final I beforeItem = (I)TreeFactory.createNewTreeDefItem(defItem.getClass(),null,null);
        beforeItem.setIsInFullName(boolVal(defItem.getIsInFullName(), false));
        beforeItem.setTextBefore(defItem.getTextBefore());
        beforeItem.setTextAfter(defItem.getTextAfter());
        beforeItem.setFullNameSeparator(defItem.getFullNameSeparator());
        beforeItem.setIsEnforced(boolVal(defItem.getIsEnforced(), false));
        
        // TODO: double check these choices
        // gather all the info needed to create a form in a dialog
        Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(defItem);
        Frame parentFrame = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String viewSetName = formsNames.first;
        String viewName = formsNames.second;
        String displayName = "NODE_EDIT_DISPLAY_NAME";
        boolean isEdit = true;
        String closeBtnText = (isEdit) ? getResourceString("Save") : getResourceString("Close");
        String className = defItem.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)defItem).getTableId());
        String idFieldName = nodeTableInfo.getIdFieldName();
        int options = MultiView.HIDE_SAVE_BTN;
        
        // create the form dialog
        String title = getResourceString("TreeDefEditDialogTitle");
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame,viewSetName,viewName,displayName,title,closeBtnText,className,idFieldName,isEdit,options);
        dialog.setModal(true);
        dialog.setData(defItem);
        dialog.preCreateUI();
        dialog.setVisible(true);
        
        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24);
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                I mergedItem;
                
                @SuppressWarnings({ "unchecked", "synthetic-access" })
                @Override
                public Object construct()
                {
                    // determine if the change can be made without requiring tree node changes
                    List<String> nodesToChange = getNodesThatMustBeFixedBeforeEdit(beforeItem, defItem);
                    if (nodesToChange != null && nodesToChange.size() > 0)
                    {
                        StringBuilder message = new StringBuilder("<html><h3><center>");
                        message.append(getResourceString("TDE_CantMakeChange"));
                        message.append("</center></h3><ul>");
                        for (String node: nodesToChange)
                        {
                            message.append("<li>" + node);
                        }
                        message.append("</ul></html>");
                        JLabel label = new JLabel();
                        label.setText(message.toString());
                        Window w = UIRegistry.getMostRecentWindow();
                        JFrame parent = null;
                        if (w instanceof JFrame)
                        {
                            parent = (JFrame)w;
                        }
                        CustomDialog errorDialog = new CustomDialog(parent,getResourceString("Error"),true,CustomDialog.OK_BTN, new JScrollPane(label));
                        errorDialog.createUI();
                        errorDialog.setSize(650, 200);
                        errorDialog.setVisible(true);
                        
                        success = false;
                        return success;
                    }
                        
                    // save the node and update the tree viewer appropriately
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        mergedItem = session.merge(defItem);
                    }
                    catch (StaleObjectException e1)
                    {
                        // another user or process has changed the data "underneath" us
                        JOptionPane.showMessageDialog(null, getResourceString("UPDATE_DATA_STALE"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 
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
                                throw new Exception("Business rules processing failed");
                            }
                        }
                        session.commit();
                        log.info("Successfully saved changes to " + mergedItem.getName());
                        
                    }
                    catch (Exception e)
                    {
                        success = false;
                        JOptionPane.showMessageDialog(null, getResourceString("UNRECOVERABLE_DB_ERROR"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 

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
                        businessRules.afterSaveCommit(defItem);
                    }
                    
                    return success;
                }

                @Override
                public void finished()
                {
                    // now refresh the tree viewer
                    if (success)
                    {
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
    
    protected List<String> getNodesThatMustBeFixedBeforeEdit(I itemBeforeEdits, I itemAfterEdits)
    {
        boolean fullnameBefore  = boolVal(itemBeforeEdits.getIsInFullName(), false);
        boolean enforcedBefore  = boolVal(itemBeforeEdits.getIsEnforced(), false);
        String textBeforeBefore = stringVal(itemBeforeEdits.getTextBefore(), "");
        String textAfterBefore  = stringVal(itemBeforeEdits.getTextAfter(), "");
        String separatorBefore  = stringVal(itemBeforeEdits.getFullNameSeparator(), "");
        
        boolean fullname  = boolVal(itemAfterEdits.getIsInFullName(), false);
        boolean enforced  = boolVal(itemAfterEdits.getIsEnforced(), false);
        String textBefore = stringVal(itemAfterEdits.getTextBefore(), "");
        String textAfter  = stringVal(itemAfterEdits.getTextAfter(), "");
        String separator  = stringVal(itemAfterEdits.getFullNameSeparator(), "");

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
                System.err.println(e);
            }
        }
        
        return problematicNodes;
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
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
            statusBar.setErrorMessage("The tree def has been changed by another user.  The def editor must be reloaded.");
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24);
                    
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
		final I newItem = (I)TreeFactory.createNewTreeDefItem(parent.getClass(),null,"New Level");

        // we can only set the pointers from the newItem side right now
        // otherwise, if the user cancels, we end up with 'dirty' collections in the other objects
        newItem.setTreeDef(displayedDef);
		newItem.setParent(parent);
        if (origChild != null)
        {
            newItem.setChild(origChild);
            newItem.setRankId( (int)(.5 * (origChild.getRankId() + parent.getRankId())));
            origChild.setParent(newItem);
        }
        else
        {
            newItem.setRankId(parent.getRankId() + 10);
        }
 
        // TODO: double check these choices
        // gather all the info needed to create a form in a dialog
        Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(newItem);
        Frame parentFrame = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String viewSetName = formsNames.first;
        String viewName = formsNames.second;
        String displayName = "NODE_EDIT_DISPLAY_NAME";
        boolean isEdit = true;
        String closeBtnText = (isEdit) ? getResourceString("Save") : getResourceString("Close");
        String className = newItem.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)newItem).getTableId());
        String idFieldName = nodeTableInfo.getIdFieldName();
        int options = MultiView.HIDE_SAVE_BTN | MultiView.IS_NEW_OBJECT;
        
        // create the form dialog
        String title = getResourceString("TreeDefEditDialogTitle");
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame,viewSetName,viewName,displayName,title,closeBtnText,className,idFieldName,isEdit,options);
        dialog.setModal(true);
        dialog.setData(newItem);
        dialog.preCreateUI();
        dialog.setVisible(true);

        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24);
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                
                @SuppressWarnings({ "unchecked", "synthetic-access" })
                @Override
                public Object construct()
                {
                    // determine if the change can be made without requiring tree node changes
                    if (newItem.getIsEnforced() != null && newItem.getIsEnforced().booleanValue() == true)
                    {
                        List<String> nodesToChange = getNodesThatMustBeFixedToEnforceNewLevel(newItem);
                        if (nodesToChange != null && nodesToChange.size() > 0)
                        {
                            StringBuilder message = new StringBuilder("<html><h3><center>");
                            message.append(getResourceString("TDE_CantEnforceNewLevel"));
                            message.append("</center></h3><ul>");
                            for (String node: nodesToChange)
                            {
                                message.append("<li>" + node);
                            }
                            message.append("</ul></html>");
                            JLabel label = new JLabel();
                            label.setText(message.toString());
                            Window w = UIRegistry.getMostRecentWindow();
                            JFrame parent = null;
                            if (w instanceof JFrame)
                            {
                                parent = (JFrame)w;
                            }
                            CustomDialog errorDialog = new CustomDialog(parent,getResourceString("Error"),true,CustomDialog.OK_BTN, new JScrollPane(label));
                            errorDialog.createUI();
                            errorDialog.setSize(650, 200);
                            errorDialog.setVisible(true);
                            
                            newItem.setIsEnforced(false);
                        }
                    }

                    // save the node and update the tree viewer appropriately
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    success = true;
                    
                    if (businessRules != null)
                    {
                        businessRules.beforeSave(newItem,session);
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
                            if (!businessRules.beforeSaveCommit(newItem, session))
                            {
                                throw new Exception("Business rules processing failed");
                            }
                            if (origChild != null)
                            {
                                if (!businessRules.beforeSaveCommit(origChild, session))
                                {
                                    throw new Exception("Business rules processing failed");
                                }
                            }
                        }
                        session.commit();
                        log.info("Successfully saved changes to " + newItem.getName());
                        
                    }
                    catch (Exception e)
                    {
                        success = false;
                        JOptionPane.showMessageDialog(null, getResourceString("UNRECOVERABLE_DB_ERROR"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 

                        log.error("Error while saving node changes.  Rolling back transaction.", e);
                        session.rollback();
                    }
                    finally
                    {
                        session.close();
                    }
                    
                    // at this point, the new node is in the DB (if success == true)

                    session = DataProviderFactory.getInstance().createSession();
                    session.refresh(newItem);
                    session.refresh(parent);
                    if (origChild != null)
                    {
                        session.refresh(origChild);
                    }
                    session.close();

                    if (businessRules != null && success == true)
                    {
                        businessRules.afterSaveCommit(newItem);
                        if (origChild != null)
                        {
                            businessRules.afterSaveCommit(origChild);
                        }
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
    
    protected List<String> getNodesThatMustBeFixedToEnforceNewLevel(I newItem)
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
	
	@SuppressWarnings("unchecked")
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
            statusBar.setErrorMessage("The tree def has been changed by another user.  The def editor must be reloaded.");
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Loading"), 24);
                    
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
        
        UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Deleting"), 24);
        
        SwingWorker bgThread = new SwingWorker()
        {
            boolean success;
            I mergedItem;
            
            @SuppressWarnings({ "unchecked", "synthetic-access" })
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
                    // another user or process has changed the data "underneath" us
                    JOptionPane.showMessageDialog(null, getResourceString("UPDATE_DATA_STALE"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 
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
                    businessRules.beforeDelete(mergedItem,session);
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
                            throw new Exception("Business rules processing failed");
                        }
                        if (!businessRules.beforeSaveCommit(parent, session))
                        {
                            throw new Exception("Business rules processing failed");
                        }
                        if (child != null)
                        {
                            if (!businessRules.beforeSaveCommit(child, session))
                            {
                                throw new Exception("Business rules processing failed");
                            }
                        }
                    }
                    session.commit();
                    log.info("Successfully deleted " + mergedItem.getName());
                    
                }
                catch (Exception e)
                {
                    success = false;
                    JOptionPane.showMessageDialog(null, getResourceString("UNRECOVERABLE_DB_ERROR"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 

                    log.error("Error while saving node changes.  Rolling back transaction.", e);
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
                session.close();

                if (businessRules != null && success == true)
                {
                    businessRules.afterDeleteCommit(mergedItem);
                    businessRules.afterSaveCommit(parent);
                    if (child != null)
                    {
                        businessRules.afterSaveCommit(child);
                    }
                }
                
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
    @SuppressWarnings("unchecked")
    protected void showDefEditDialog(final D treeDef)
    {
        // load a fresh copy of the parent from the DB
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        final D def = (D)tmpSession.load(treeDef.getClass(), treeDef.getTreeDefId());
        if (def == null)
        {
            statusBar.setErrorMessage("The tree currently being displayed has been deleted by another user.  It must now be closed.");
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
        Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(treeDef);
        Frame parentFrame = (Frame)UIRegistry.get(UIRegistry.FRAME);
        String viewSetName = formsNames.first;
        String viewName = formsNames.second;
        String displayName = "NODE_EDIT_DISPLAY_NAME";
        boolean isEdit = true;
        String closeBtnText = (isEdit) ? getResourceString("Save") : getResourceString("Close");
        String className = def.getClass().getName();
        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)def).getTableId());
        String idFieldName = nodeTableInfo.getIdFieldName();
        int options = MultiView.HIDE_SAVE_BTN;
        
        // create the form dialog
        String title = getResourceString("TreeDefEditDialogTitle");
        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame,viewSetName,viewName,displayName,title,closeBtnText,className,idFieldName,isEdit,options);
        dialog.setModal(true);
        dialog.setData(def);
        dialog.preCreateUI();
        dialog.setVisible(true);
        
        // the dialog has been dismissed by the user
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            UIRegistry.writeGlassPaneMsg(getResourceString("TTV_Saving"), 24);
            
            SwingWorker bgThread = new SwingWorker()
            {
                boolean success;
                D mergedDef;
                
                @SuppressWarnings({ "unchecked", "synthetic-access" })
                @Override
                public Object construct()
                {
                    // save the node and update the tree viewer appropriately
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        mergedDef = session.merge(def);
                    }
                    catch (StaleObjectException e1)
                    {
                        // another user or process has changed the data "underneath" us
                        JOptionPane.showMessageDialog(null, getResourceString("UPDATE_DATA_STALE"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 
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
                                throw new Exception("Business rules processing failed");
                            }
                        }
                        session.commit();
                        log.info("Successfully saved changes to " + mergedDef.getName());
                        
                    }
                    catch (Exception e)
                    {
                        success = false;
                        JOptionPane.showMessageDialog(null, getResourceString("UNRECOVERABLE_DB_ERROR"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 

                        log.error("Error while saving node changes.  Rolling back transaction.", e);
                        session.rollback();
                    }
                    finally
                    {
                        session.close();
                    }
                    
                    // at this point, the new node is in the DB (if success == true)

                    session = DataProviderFactory.getInstance().createSession();
                    session.refresh(def);
                    session.close();

                    if (businessRules != null && success == true)
                    {
                        businessRules.afterSaveCommit(def);
                    }
                    
                    return success;
                }

                @Override
                public void finished()
                {
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
