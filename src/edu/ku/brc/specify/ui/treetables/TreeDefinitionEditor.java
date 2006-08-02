/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.treeutils.TreeDataService;
import edu.ku.brc.specify.treeutils.TreeDataServiceFactory;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.ui.treetables.EditFormDialog.EditDialogCallback;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.renderers.NameBasedListCellRenderer;
import edu.ku.brc.util.Pair;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDefinitionEditor extends BaseSubPane implements ListSelectionListener
{
	protected Class treeDefClass;
	
	protected JPanel northPanel;
	protected JPanel southPanel;
	protected JPanel eastPanel;
	protected JLabel messageLabel;
	protected JComboBox defsBox;
	
	protected JList defItemsList;
	protected TreeDefEditorListModel listModel;
	protected JButton upButton;
	protected JButton downButton;
	
	protected TreeDataService dataService;
	protected TreeDefinitionIface displayedDef;
	
	/**
	 *
	 *
	 * @param name
	 * @param task
	 */
	public TreeDefinitionEditor(Class treeDefClass, String name, Taskable task)
	{
		super(name,task);
		this.treeDefClass = treeDefClass;
	
		dataService = TreeDataServiceFactory.createService();
		
		List<TreeDefinitionIface> treeDefs = dataService.getAllTreeDefs(treeDefClass);
		init(treeDefs);
	}
	
	protected void init(List<TreeDefinitionIface> treeDefs)
	{
		this.setLayout(new BorderLayout());
		
		messageLabel = new JLabel("Select a tree in the combobox above");
		this.add(messageLabel,BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		this.add(southPanel,BorderLayout.SOUTH);

		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		this.add(northPanel,BorderLayout.NORTH);
		
		eastPanel = new JPanel();
//		eastPanel.setLayout(mgr)
		
		Vector<Object> defs = new Vector<Object>(treeDefs);
		
		defs.add(0, "Choose a tree definition");
		defs.add("Create a new tree definition.");
		defsBox = new JComboBox(defs);
		defsBox.setRenderer(new NameBasedListCellRenderer());
		defsBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object selection = defsBox.getSelectedItem();
				if( selection instanceof TreeDefinitionIface )
				{
					TreeDefinitionIface treeDef = (TreeDefinitionIface)defsBox.getSelectedItem();
					displayedDef = treeDef;
					
					messageLabel.setText("Please wait while the tree is prepared");
					messageLabel.setIcon(null);
					add(messageLabel);
					repaint();
					
					defsBox.setEnabled(false);
					initTreeDefEditorComponent(treeDef);
				}
				else if( defsBox.getSelectedIndex() == defsBox.getModel().getSize()-1 )
				{
					// do new def stuff
				}
			}
		});
		northPanel.add(defsBox,BorderLayout.CENTER);
	}
	
	@SuppressWarnings("unchecked")
	protected void initTreeDefEditorComponent(TreeDefinitionIface treeDef)
	{
		Set<TreeDefinitionItemIface> defItems = treeDef.getTreeDefItems();
		listModel = new TreeDefEditorListModel(defItems);
		defItemsList = new JList(listModel);
		defItemsList.addListSelectionListener(this);
		Icon enforcedIcon = IconManager.getIcon("GoogleEarth",IconManager.IconSize.Std16);
		defItemsList.setCellRenderer(new TreeDefItemListCellRenderer(20,enforcedIcon));
		
		this.remove(messageLabel);
		this.add(defItemsList,BorderLayout.CENTER);
	}

	/**
	 *
	 *
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 * @param e
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		if(e.getValueIsAdjusting())
		{
			return;
		}
		repaint();
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

		showEditDialog(newItem, "New Definition Item Form", callback);
	}
	
	protected void newDefItemEditComplete(TreeDefinitionItemIface defItem)
	{
		
	}
	
	protected void newDefItemEditCancelled(TreeDefinitionItemIface defItem)
	{
		
	}
	
	/**
	 * Display the form for editing node data.
	 *
	 * @param node the node being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showEditDialog(TreeDefinitionItemIface defItem,String title,EditDialogCallback callback)
	{
		String shortClassName = defItem.getClass().getName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(defItem);
		EditFormDialog editDialog = new EditFormDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setModal(true);
		editDialog.setData(defItem);
		editDialog.setVisible(true);
	}

}
