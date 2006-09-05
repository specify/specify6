/**
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
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
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
	
	protected TreeDataService<T,D,I> dataService;
	protected D displayedDef;
	protected Vector<I> deletedItems;

	//////////////
	// GUI widgets
	//////////////
	
	// panels
	protected JPanel northPanel;
	protected JPanel southPanel;
	
	protected JLabel messageLabel;
	
	// main user interaction widget
	protected JTable defItemsTable;
	protected TreeDefEditorTableModel<I> tableModel;
	
	// north panel widgets
	protected JLabel defNameLabel;
	protected JButton editDefButton;
	
	protected JStatusBar statusBar;
	
	// south panel widgets
	protected JButton deleteItemButton;
	protected JButton newItemButton;
	
	protected Color errorColor;
	
	protected boolean unsavedChanges;
	
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
		dataService = TreeDataServiceFactory.createService();
		displayedDef = dataService.getTreeDef((Class<D>)treeDef.getClass(),treeDef.getTreeDefId());
		if(displayedDef == null)
		{
			throw new RuntimeException("Tree data service unable to locate the specified tree defintion in the DB");
		}
		deletedItems = new Vector<I>();
		
		initUI();
		messageLabel.setText("Please wait while the tree is prepared");
		messageLabel.setIcon(null);
		add(messageLabel);
		repaint();
		
		unsavedChanges = false;
		
		initTreeDefEditorComponent(displayedDef);
	}
	
	public D getDisplayedTreeDef()
	{
		return displayedDef;
	}
	
	@Override
	public boolean aboutToShutdown()
	{
		if(!unsavedChanges)
		{
			return true;
		}
		
		// show a popup to ask user about unsaved changes
		String save = getResourceString("Save");
		String discard = getResourceString("Discard");
		String cancel = getResourceString("Cancel");
		JOptionPane popup = new JOptionPane("Save changes before closing?",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION,null,new String[] {cancel,discard,save});
		JDialog dialog = popup.createDialog(this,"Unsaved Changes");
		SubPaneMgr.getInstance().showPane(this);
		dialog.setVisible(true);
		Object userOpt = popup.getValue();
		if(userOpt == save)
		{
			saveToDb();
			return true;
		}
		else if(userOpt == cancel)
		{
			return false;
		}

		return true;
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
		
		statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
		
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
		southPanel.add(Box.createHorizontalGlue());
		southPanel.add(deleteItemButton);
		southPanel.add(Box.createRigidArea(horizSpacer));
		southPanel.add(newItemButton);
		
		enableSelectionSensativeButtons(false);
	}
	
	protected void initTreeDefEditorComponent(D treeDef)
	{
		Set<I> defItems = treeDef.getTreeDefItems();
		tableModel = new TreeDefEditorTableModel<I>(defItems);
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
	}
	
	public void clearStatus()
	{
		statusBar.setText(null);
	}
	
	public void showError(String error)
	{
		statusBar.setText(error);
		statusBar.setAsError();
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
		
		I parent = tableModel.get(index);
		I newItem = TreeFactory.createNewTreeDefItem(parent.getClass(),null,"New Level");

		// just set the parent item to have it available after the edit form is gone
		newItem.setParent(parent);

		I child = parent.getChild();
		
		// fix up the rest of the parent/child pointers
		if( child != null )
		{
			child.setParent(newItem);
			newItem.setChild(child);
			newItem.setRankId( (int)(.5 * (child.getRankId() + parent.getRankId())) );
		}
		else
		{
			newItem.setRankId( parent.getRankId() + 200 );
		}
		parent.setChild(newItem);
		
		displayedDef.getTreeDefItems().add(newItem);
		newItem.setTreeDef(displayedDef);
		
		int insertIndex = tableModel.indexOf(parent)+1;
		tableModel.add(insertIndex,newItem);
		
		showMessage("New def item added");
		defItemsTable.getSelectionModel().setSelectionInterval(insertIndex,insertIndex);
		unsavedChanges = true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the creation and editing of a new TreeDefinitionIface object.
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Display the form for editing the given tree definition. 
	 *
	 * @param treeDef the tree definition being edited
	 */
	protected void showDefEditDialog(D treeDef)
	{
		EditDialogCallback<D> callback = new EditDialogCallback<D>()
		{
			public void editCompleted(D dataObj)
			{
				defNameLabel.setText(dataObj.getName());
				unsavedChanges = true;
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
		EditFormDialog<D> editDialog = new EditFormDialog<D>(formsNames.first,formsNames.second,defEditDialogTitle,callback);
		editDialog.setModal(true);
		editDialog.setData(treeDef);
		editDialog.setVisible(true);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle the editing of an existing TreeDefinitionItemIface object.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	protected void deleteItem(int index)
	{
		if(index==-1)
		{
			return;
		}
		
		
		I item = tableModel.get(index);

		if(!item.canBeDeleted())
		{
			//TODO: make this some sort of message on the GUI
			log.warn("Tree definition item cannot be deleted.  Tree nodes reference this def item.");
			showError("Item cannot be deleted");
			return;
		}
		
		I parent = item.getParent();
		I child = item.getChild();

		// detach this object from the rest of the def items
		item.setParent(null);
		item.setChild(null);
		if(parent!=null)
		{
			parent.setChild(null);
		}
		if(child!=null)
		{
			child.setParent(null);
		}
		
		// if both parent and child existed, tie them together.
		if(parent!=null && child!=null)
		{
			parent.setChild(child);
			child.setParent(parent);
		}
		
		item.setTreeDef(null);
		displayedDef.getTreeDefItems().remove(item);
		deletedItems.add(item);
		
		tableModel.remove(item);
		showMessage("Item deleted");
		
		unsavedChanges = true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Methods to handle saving to DB
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public void saveToDb()
	{
		dataService.saveTreeDef(displayedDef,deletedItems);
		showMessage("Tree Definition Saved");
		unsavedChanges = false;
	}
}
