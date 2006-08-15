/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.EditFormDialog.EditDialogCallback;
import edu.ku.brc.specify.ui.treetables.TreeDefSelectionDialog.TreeSelectionDialogCallback;
import edu.ku.brc.util.Pair;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDefinitionEditor extends BaseSubPane
{
	private static final Logger log  = Logger.getLogger(TreeDefinitionEditor.class);

	//////////////////
	// Non-GUI members
	//////////////////
	
	protected TreeDataService dataService;
	protected TreeDefinitionIface displayedDef;
	protected Vector<TreeDefinitionItemIface> deletedItems;
	protected Class treeDefClass;

	//////////////
	// GUI widgets
	//////////////
	
	// panels
	protected JPanel northPanel;
	protected JPanel southPanel;
	
	protected JLabel messageLabel;
	
	// main user interaction widget
	protected JTable defItemsTable;
	protected TreeDefEditorTableModel tableModel;
	
	// north panel widgets
	protected JLabel defNameLabel;
	protected JButton editDefButton;
	
	// south panel widgets
	protected JButton commitToDbButton;
	protected JLabel  statusLabel;
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
	public TreeDefinitionEditor(Class treeDefClass, String name, Taskable task, boolean showSelectionDialog)
	{
		super(name,task);
		this.treeDefClass = treeDefClass;
	
		dataService = TreeDataServiceFactory.createService();
		deletedItems = new Vector<TreeDefinitionItemIface>();
		
		final List<TreeDefinitionIface> treeDefs = dataService.getAllTreeDefs(treeDefClass);
		initUI();
		
		if( showSelectionDialog )
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					showTreeDefSelectionDialog(treeDefs);
				}
			});
		}
	}
	
	@Override
	public void shutdown()
	{
		super.shutdown();
		dataService.fini();
	}

	protected void initUI()
	{
		this.setLayout(new BorderLayout());
		
		Dimension horizSpacer = new Dimension(5,0);
		
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
				showDefEditForm(displayedDef);	
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
		
		// create south panel widgets
		commitToDbButton = new JButton("Save");
		commitToDbButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				saveToDb();
			}
		});
		statusLabel = new JLabel();
		editItemButton = new JButton("Edit");
		editItemButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				editItem(defItemsTable.getSelectedRow());
			}
		});
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
		
		// add south panel widgets
		southPanel.add(commitToDbButton);
		southPanel.add(Box.createRigidArea(horizSpacer));
		southPanel.add(statusLabel);
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(editItemButton);
		southPanel.add(Box.createRigidArea(horizSpacer));
		southPanel.add(deleteItemButton);
		southPanel.add(Box.createRigidArea(horizSpacer));
		southPanel.add(newItemButton);
		
		enableSelectionSensativeButtons(false);
	}
	
	protected void showTreeDefSelectionDialog(List<TreeDefinitionIface> treeDefs)
	{
		// show the dialog with all trees listed
		// if the user hits "Cancel", close this copy of the TDE
		// if the user hits "OK", either create a new def or edit the chosen def (depending on selection)

		TreeSelectionDialogCallback callback = new TreeSelectionDialogCallback()
		{
			public void cancelled()
			{
				SubPaneMgr.getInstance().removePane(TreeDefinitionEditor.this);
			}
			public void defSelected(TreeDefinitionIface def)
			{
				treeDefSelected(def);
			}
			public void newDefOptionSelected()
			{
				TreeDefinitionIface newDef = TreeFactory.createNewTreeDef(treeDefClass,"New Def",null);
				showNewDefForm(newDef);				
			}
		};
		TreeDefSelectionDialog d = new TreeDefSelectionDialog(null,treeDefs,callback,false);
		d.setModal(true);
		d.setSize(300,150);
		UIHelper.centerAndShow(d);
	}
	
	protected void treeDefSelected(Object selection)
	{
		if(selection == null)
		{
			messageLabel.setText("You must select a valid tree defintion.\nClose this panel and start over.");
			return;
		}
		
		TreeDefinitionIface treeDef = (TreeDefinitionIface)selection;
		displayedDef = treeDef;
		
		messageLabel.setText("Please wait while the tree is prepared");
		messageLabel.setIcon(null);
		add(messageLabel);
		repaint();
		
		initTreeDefEditorComponent(treeDef);
	}
	
	@SuppressWarnings("unchecked")
	protected void initTreeDefEditorComponent(TreeDefinitionIface treeDef)
	{
		Set<TreeDefinitionItemIface> defItems = treeDef.getTreeDefItems();
		tableModel = new TreeDefEditorTableModel(defItems);
		defItemsTable = new JTable(tableModel);
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
	
	protected void addSelectionListener()
	{
		ListSelectionListener sl = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				clearStatus();
				
				if(defItemsTable.getSelectedRow() == -1)
				{
					enableSelectionSensativeButtons(false);
				}
				else
				{
					enableSelectionSensativeButtons(true);
				}
			}
		};
		defItemsTable.getSelectionModel().addListSelectionListener(sl);
	}
	
	protected void enableSelectionSensativeButtons(boolean enable)
	{
		deleteItemButton.setEnabled(enable);
		newItemButton.setEnabled(enable);
		editItemButton.setEnabled(enable);
	}
	
	/**
	 * Display the form for editing the given object.  This is a generic method for displaying
	 * a form for editing values in any object where TreeFactory.getAppropriateFormsetAndViewNames
	 * returns non-null.
	 *
	 * @param obj the obj being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showObjectEditDialog(Object obj,String title,EditDialogCallback callback)
	{
		String shortClassName = obj.getClass().getSimpleName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(obj);
		if(formsNames==null)
		{
			log.error("Unable to locate appropriate forms for editing");
			showError("Cannot locate forms for editing object");
			return;
		}
		EditFormDialog editDialog = new EditFormDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setModal(true);
		editDialog.setData(obj);
		editDialog.setVisible(true);
	}

	public void clearStatus()
	{
		statusLabel.setText(null);
	}
	
	public void showError(String error)
	{
		statusLabel.setText(error);
		statusLabel.setForeground(errorColor);
	}
	
	public void showMessage(String message)
	{
		statusLabel.setText(message);
		statusLabel.setForeground(this.getForeground());
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	protected void newItem(int index)
	{
		if(index==-1)
		{
			return;
		}
		
		TreeDefinitionItemIface parent = tableModel.get(index);
		TreeDefinitionItemIface newItem = 
			TreeFactory.createNewTreeDefinitionItem(parent.getClass(),"New Level");

		// just set the parent item to have it available after the edit form is gone
		newItem.setParentItem(parent);

		showNewItemForm(newItem);
	}
	
	/**
	 * Display the data entry form for creating a new node.
	 *
	 * @param newNode the new node for which the user must enter data
	 */
	protected void showNewItemForm(TreeDefinitionItemIface newItem)
	{
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				TreeDefinitionItemIface item = (TreeDefinitionItemIface)dataObj;
				newDefItemEditComplete(item);
			}
			public void editCancelled(Object dataObj)
			{
				TreeDefinitionItemIface item = (TreeDefinitionItemIface)dataObj;
				newDefItemEditCancelled(item);
			}
		};

		showObjectEditDialog(newItem, "New Definition Item Form", callback);
	}
	
	@SuppressWarnings("unchecked")
	protected void newDefItemEditComplete(TreeDefinitionItemIface newItem)
	{
		TreeDefinitionItemIface parent = newItem.getParentItem();
		TreeDefinitionItemIface child = parent.getChildItem();
		
		// fix up the rest of the parent/child pointers
		if( child != null )
		{
			child.setParentItem(newItem);
			newItem.setChildItem(child);
			newItem.setRankId( (int)(.5 * (child.getRankId() + parent.getRankId())) );
		}
		else
		{
			newItem.setRankId( parent.getRankId() + 200 );
		}
		parent.setChildItem(newItem);
		
		displayedDef.getTreeDefItems().add(newItem);
		newItem.setTreeDefinition(displayedDef);
		
		int insertIndex = tableModel.indexOf(parent)+1;
		tableModel.add(insertIndex,newItem);
		
		clearStatus();
	}
	
	protected void newDefItemEditCancelled(TreeDefinitionItemIface defItem)
	{
		log.info("newDefItemEditCancelled called");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Display the data entry form for creating a new tree definition.
	 *
	 * @param newNode the new node for which the user must enter data
	 */
	public void showNewDefForm(TreeDefinitionIface newDef)
	{
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				TreeDefinitionIface def = (TreeDefinitionIface)dataObj;
				newDefEditComplete(def);
			}
			public void editCancelled(Object dataObj)
			{
				SubPaneMgr.getInstance().removePane(TreeDefinitionEditor.this);
			}
		};

		showObjectEditDialog(newDef, "New Definition Form", callback);
	}

	protected void newDefEditComplete(TreeDefinitionIface def)
	{
//		TreeDefinitionIface newDef = TreeFactory.setupNewTreeDef(def);
		TreeDefinitionIface newDef = TreeFactory.setupNewStdTreeDef(def);
		treeDefSelected(newDef);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionIface object.
	/////////////////////////////////////////////////////////////////////////////////////////

	protected void showDefEditForm(TreeDefinitionIface treeDef)
	{
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				TreeDefinitionIface def = (TreeDefinitionIface)dataObj;
				defNameLabel.setText(def.getName());
			}
			public void editCancelled(Object dataObj)
			{
				// nothing to do here
			}
		};
		showObjectEditDialog(treeDef,"Tree Definition Data Entry",callback);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the editing of an existing TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	protected void editItem(int index)
	{
		if(index==-1)
		{
			return;
		}
		
		TreeDefinitionItemIface defItem = tableModel.get(index);
		EditDialogCallback callback = new EditDialogCallback()
		{
			public void editCompleted(Object dataObj)
			{
				TreeDefinitionItemIface item = (TreeDefinitionItemIface)dataObj;
				itemEditComplete(item);
			}
			public void editCancelled(Object dataObj)
			{
				TreeDefinitionItemIface item = (TreeDefinitionItemIface)dataObj;
				itemEditCancelled(item);
			}
		};
		showObjectEditDialog(defItem,"Edit Definition Item",callback);
	}
	
	protected void itemEditComplete(TreeDefinitionItemIface defItem)
	{
		repaint();
	}
	
	protected void itemEditCancelled(TreeDefinitionItemIface defItem)
	{
		repaint();
	}
	
	protected void deleteItem(int index)
	{
		if(index==-1)
		{
			return;
		}
		
		
		TreeDefinitionItemIface item = tableModel.get(index);

		if(!item.canBeDeleted())
		{
			//TODO: make this some sort of message on the GUI
			log.warn("Tree definition item cannot be deleted.  Tree nodes reference this def item.");
			showError("Item cannot be deleted");
			return;
		}
		
		TreeDefinitionItemIface parent = item.getParentItem();
		TreeDefinitionItemIface child = item.getChildItem();

		// detach this object from the rest of the def items
		item.setParentItem(null);
		item.setChildItem(null);
		if(parent!=null)
		{
			parent.setChildItem(null);
		}
		if(child!=null)
		{
			child.setParentItem(null);
		}
		
		// if both parent and child existed, tie them together.
		if(parent!=null && child!=null)
		{
			parent.setChildItem(child);
			child.setParentItem(parent);
		}
		
		item.setTreeDefinition(null);
		displayedDef.getTreeDefItems().remove(item);
		deletedItems.add(item);
		
		tableModel.remove(item);
		showMessage("Item deleted");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle saving to DB
	/////////////////////////////////////////////////////////////////////////////////////////
	
	protected void saveToDb()
	{
		dataService.saveTreeDef(displayedDef,deletedItems);
		showMessage("Tree Definition Saved");
	}
}
