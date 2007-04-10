/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.EditFormDialog.EditDialogCallback;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UICacheManager;
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

	//////////////
	// GUI widgets
	//////////////
	
	// panels
	protected JPanel northPanel;
	protected JPanel southPanel;
	
	protected JLabel messageLabel;
	
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
	
	protected Color errorColor;
	
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
        
		initUI();
        
		messageLabel.setText("Please wait while the tree is prepared");
		messageLabel.setIcon(null);
		add(messageLabel);
		repaint();
		
		initTreeDefEditorComponent(displayedDef);
        
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
		
		statusBar = UICacheManager.getStatusBar();
		
		errorColor = Color.RED;
		
		messageLabel = new JLabel();

		// create north panel
		northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.LINE_AXIS));
		
		// create north panel widgets
		defNameLabel = new JLabel();
		editDefButton = new JButton("Edit");
		editDefButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				showDefEditDialog(displayedDef,false);	
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
		
		deleteItemButton = new JButton("Delete");
		deleteItemButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				deleteItem(defItemsTable.getSelectedRow());
			}
		});
        newItemButton = new JButton("New");
        newItemButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                newItem(defItemsTable.getSelectedRow());
            }
        });
        editItemButton = new JButton("Edit");
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
		this.remove(messageLabel);
		this.add(new JScrollPane(defItemsTable),BorderLayout.CENTER);
		this.add(northPanel,BorderLayout.NORTH);
		this.add(southPanel,BorderLayout.SOUTH);
		
		repaint();
	}
	
    protected void editTreeDefItem(int index)
    {
        if (index==-1)
        {
            return;
        }
        
        log.info("Edit row " + index);
        I defItem = tableModel.get(index);
        
        showItemEditDialog(defItem, "Edit Tree Level", null, false);
        
        tableModel.fireTableRowsUpdated(index, index);
    }

    protected void showItemEditDialog(I defItem, String title, EditDialogCallback<I> callback, boolean isNewItem )
    {
        log.debug("showing item edit dialog");
        
        Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(defItem);
        EditFormDialog<I> editDialog = new EditFormDialog<I>(formsNames.first,formsNames.second,title,callback,isNewItem);
        editDialog.setModal(true);
        editDialog.setData(defItem);
        editDialog.setVisible(true);
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
            if (tableModel.isDeletable(selectionIndex))
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
	// Methods to handle the creation and editing of a new TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected void newItem(int index)
	{
		if(index==-1)
		{
			return;
		}
		
		final I parent = tableModel.get(index);
        final I origChild = parent.getChild();
		final I newItem = (I)TreeFactory.createNewTreeDefItem(parent.getClass(),null,"New Level");

        // we can only set the pointers from the newItem side right now
        // otherwise, if the user cancels, we end up with 'dirty' collections in the other objects
        newItem.setTreeDef(displayedDef);
		newItem.setParent(parent);
        if (origChild!=null)
        {
            newItem.setChild(origChild);
            newItem.setRankId( (int)(.5 * (origChild.getRankId() + parent.getRankId())));
        }
        else
        {
            newItem.setRankId( parent.getRankId() + 20);
        }
        
        EditDialogCallback<I> callback = new EditDialogCallback<I>()
        {
            public void editCancelled(I dataObj)
            {
                // undo the parent/child changes that we made
                newItem.setTreeDef(null);
                newItem.setParent(null);
                newItem.setChild(null);
            }

            public void editCompleted(I dataObj)
            {
                int addedIndex = tableModel.add(newItem,parent);
                
                if (addedIndex != -1)
                {
                    showMessage("New tree level added");
                    defItemsTable.getSelectionModel().setSelectionInterval(addedIndex,addedIndex);
                }
            }
        };
        
        showItemEditDialog(newItem,"Edit Tree Level",callback,true);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the editing of a new TreeDefinitionIface object.
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Display the form for editing the given tree definition. 
	 *
	 * @param treeDef the tree definition being edited
	 */
	protected void showDefEditDialog(D treeDef, boolean isNewObject)
	{
		EditDialogCallback<D> callback = new EditDialogCallback<D>()
		{
			public void editCompleted(D dataObj)
			{
				defNameLabel.setText(dataObj.getName());
			}
			public void editCancelled(D dataObj)
			{
				// nothing to do here
			}
		};
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(treeDef);
		if(formsNames==null)
		{
			log.error("Unable to locate appropriate forms for editing");
			showError("Cannot locate forms for editing object");
			return;
		}
		String defEditDialogTitle = getResourceString("TreeDefEditDialogTitle");
		EditFormDialog<D> editDialog = new EditFormDialog<D>(formsNames.first,formsNames.second,defEditDialogTitle,callback,isNewObject);
		editDialog.setModal(true);
		editDialog.setData(treeDef);
		editDialog.setVisible(true);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the deletion of an existing TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	protected void deleteItem(int index)
	{
		if(index==-1)
		{
			return;
		}
        
        boolean removed = tableModel.remove(index);
		if (removed)
        {
		    showMessage("Tree level deleted");
        }
        else
        {
            showMessage("Unable to delete selected tree level");
        }
	}
}
