/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ListPopupDialog;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.ListPopupDialog.ListPopupCallback;
import edu.ku.brc.ui.renderers.NameableListItemCellRenderer;
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
	
	protected Class treeDefClass;
	
	protected JPanel southPanel;
	protected JPanel eastPanel;
	protected JLabel messageLabel;
	
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
	public TreeDefinitionEditor(Class treeDefClass, String name, Taskable task, boolean showSelectionDialog)
	{
		super(name,task);
		this.treeDefClass = treeDefClass;
	
		dataService = TreeDataServiceFactory.createService();
		
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
	
	protected void initUI()
	{
		this.setLayout(new BorderLayout());
		
		messageLabel = new JLabel("Select a tree in the combobox above");
		this.add(messageLabel,BorderLayout.CENTER);
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		this.add(southPanel,BorderLayout.SOUTH);

		eastPanel = new JPanel();
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
		JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
		TreeDefSelectionDialog d = new TreeDefSelectionDialog(topFrame,treeDefs,callback);
		d.setModal(true);
		d.setSize(300,150);
		UIHelper.centerAndShow(d);
	}
	
	protected void treeDefSelected(Object selection)
	{
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
		listModel = new TreeDefEditorListModel(defItems);
		defItemsList = new JList(listModel);
		defItemsList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount()==2)
				{
					int index = defItemsList.getUI().locationToIndex(defItemsList,e.getPoint());
					mouseDoubleClick(index);
				}
			}
		});
		ImageIcon enforcedIcon = IconManager.getIcon("Checkmark",IconManager.IconSize.Std16);
		enforcedIcon = IconManager.getScaledIcon(enforcedIcon,IconSize.Std32,IconSize.Std16);
		defItemsList.setCellRenderer(new TreeDefItemListCellRenderer(20,enforcedIcon));
		
		this.remove(messageLabel);
		this.add(defItemsList,BorderLayout.CENTER);
		this.add(new JLabel(treeDef.getName()),BorderLayout.NORTH);
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

		showItemEditDialog(newItem, "New Definition Item Form", callback);
	}
	
	protected void newDefItemEditComplete(TreeDefinitionItemIface defItem)
	{
		log.info("newDefItemEditComplete called");
	}
	
	protected void newDefItemEditCancelled(TreeDefinitionItemIface defItem)
	{
		log.info("newDefItemEditCancelled called");
	}
	
	/**
	 * Display the form for editing node data.
	 *
	 * @param node the node being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showItemEditDialog(TreeDefinitionItemIface defItem,String title,EditDialogCallback callback)
	{
		String shortClassName = defItem.getClass().getSimpleName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(defItem);
		EditFormDialog editDialog = new EditFormDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setModal(true);
		editDialog.setData(defItem);
		editDialog.setVisible(true);
	}
	
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
				TreeDefinitionIface def = (TreeDefinitionIface)dataObj;
				newDefEditCancelled(def);
			}
		};

		showDefEditDialog(newDef, "New Definition Form", callback);
	}

	protected void newDefEditComplete(TreeDefinitionIface def)
	{
		TreeDefinitionIface newDef = TreeFactory.setupNewTreeDef(def);
		treeDefSelected(newDef);
	}
	
	protected void newDefEditCancelled(TreeDefinitionIface def)
	{
		log.info("newDefEditCancelled called");
	}
	
	/**
	 * Display the form for editing tree definition data.
	 *
	 * @param def the def being edited
	 * @param title the title of the dialog window
	 * @param callback the 'complete' and 'cancel' callbacks for the 'OK' and 'Cancel' buttons
	 */
	protected void showDefEditDialog(TreeDefinitionIface def,String title,EditDialogCallback callback)
	{
		String shortClassName = def.getClass().getSimpleName();
		String idFieldName = shortClassName.substring(0,1).toLowerCase() + shortClassName.substring(1) + "Id";
		Pair<String,String> formsNames = TreeFactory.getAppropriateFormsetAndViewNames(def);
		EditFormDialog editDialog = new EditFormDialog(formsNames.first,formsNames.second,title,shortClassName,idFieldName,callback);
		editDialog.setModal(true);
		editDialog.setData(def);
		editDialog.setVisible(true);
	}

	protected void mouseDoubleClick(int index)
	{
		TreeDefinitionItemIface defItem = (TreeDefinitionItemIface)listModel.getElementAt(index);
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
		showItemEditDialog(defItem,"Edit Definition Item",callback);
	}
	
	protected void itemEditComplete(TreeDefinitionItemIface defItem)
	{
		log.info("itemEditComplete called");
	}
	
	protected void itemEditCancelled(TreeDefinitionItemIface defItem)
	{
		log.info("itemEditCancelled called");
	}
}
