/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package edu.ku.brc.specify.tasks.subpane;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.SearchBox;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.JAutoCompTextField;

/**
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class SecurityAdminPane extends BaseSubPane
{
	protected JTree  tree;
	protected JPanel navCards;
	protected JPanel infoCards;
	protected Vector<SecurityPanelWrapper> navPanelWrappers;
	protected Vector<SecurityPanelWrapper> infoPanelWrappers;
	protected Hashtable<JButton, SecurityPanelWrapper> navPanelWrapperHash;
	protected Hashtable<String, SecurityPanelWrapper>  infoPanelWrapperHash;
	
	final protected int fieldSize = 40;

	/**
	 * Constructor
	 * @param name
	 * @param task
	 */
	public SecurityAdminPane(String name, final Taskable task)
	{
		super(name, task);
	}
	
	public JPanel createMainControlUI()
	{
		JPanel securityAdminPanel = new JPanel();
        //JPanel securityAdminPanel = new FormDebugPanel();
		
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"3dlu,p,3dlu,3dlu,3dlu,f:p:g,3dlu",
        		"3dlu,f:p:g,3dlu,p,3dlu,p,3dlu"), 
        		securityAdminPanel);
		final CellConstraints cc = new CellConstraints();
        
		mainPB.add(createNavigationPanel(),  cc.xy(2, 2));
		mainPB.add(createInformationPanel(), cc.xy(6, 2));
		
		this.add(securityAdminPanel);
		return securityAdminPanel;
	}
	
	/**
	 * 
	 * @return
	 */
	protected JPanel createNavigationPanel()
	{
		JPanel navToolbarPanel = new JPanel();
		//JPanel navToolbarPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"l:p:g", "p,3dlu,p,3dlu,t:p:g,3dlu,p,3dlu,p,3dlu,p"), navToolbarPanel);
		final CellConstraints cc = new CellConstraints();
        
		ActionListener buttonAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				// show navigation panel that corresponds to button pressed 
	            final CardLayout cl = (CardLayout)(navCards.getLayout());
	            final SecurityPanelWrapper wrp = navPanelWrapperHash.get(ae.getSource());
	            cl.show(navCards, wrp.getName());
			}
		};

		navPanelWrappers = new Vector<SecurityPanelWrapper>(4);
		navPanelWrappers.add(createFullTreeNavPanel(buttonAL));
		navPanelWrappers.add(createUserNavPanel(buttonAL));
		navPanelWrappers.add(createGroupNavPanel(buttonAL));
		navPanelWrappers.add(createObjectNavPanel(buttonAL));
		
		// creates a panel in card panel corresponding to each button 
		navCards = new JPanel();
		navCards.setLayout(new CardLayout());

		JAutoCompTextField searchText = new JAutoCompTextField(22);
		SearchBox searchBox = new SearchBox(searchText, null);
		
        final PanelBuilder toolbarPB = new PanelBuilder(new FormLayout("l:p,1dlu,p,1dlu,p,1dlu,p,15dlu,r:p", "p"));

		navPanelWrapperHash = new Hashtable<JButton, SecurityPanelWrapper>();
		int i = 0;
		for (SecurityPanelWrapper panelWrp : navPanelWrappers)
		{
			navPanelWrapperHash.put(panelWrp.getButton(), panelWrp);
			panelWrp.getButton().setEnabled(true);
			navCards.add(panelWrp.getPanel(), panelWrp.getName());
			//toolbarPB.add(panelWrp.getButton(), cc.xy(1 + 2*i, 1));
			i++;
		}
		toolbarPB.add(searchBox, cc.xy(1, 1));
		
		mainPB.add(toolbarPB.getPanel(), cc.xy(1, 1));
		mainPB.add(navCards,             cc.xy(1, 5));

		return navToolbarPanel;
	}

	protected SecurityPanelWrapper createFullTreeNavPanel(ActionListener buttonAL)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("institution", "The Natural History Museum"));
    	
    	DefaultMutableTreeNode globalGrp1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Administrators"));
    	DefaultMutableTreeNode globalGrp2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Guests"));
    	DefaultMutableTreeNode globalGrp3 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Web"));
    	DefaultMutableTreeNode globalGrp4 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "All Users"));
    	
    	DefaultMutableTreeNode div1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Bug", "Entomology Division"));
    	DefaultMutableTreeNode div2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Plants", "Botany Division"));
    	DefaultMutableTreeNode div3 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Fish", "Fish Division"));

    	root.add(div1);
    	root.add(div2);
    	root.add(div3);

    	root.add(globalGrp1);
    	root.add(globalGrp2);
    	root.add(globalGrp3);
    	root.add(globalGrp4);

    	DefaultMutableTreeNode col1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("collection", "Voucher Collection"));
    	DefaultMutableTreeNode col2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("collection", "Tissue Collection"));
    	DefaultMutableTreeNode grp4 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Collection Managers"));
    	DefaultMutableTreeNode usr1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Alice"));
    	DefaultMutableTreeNode usr2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Bob"));
    	DefaultMutableTreeNode usr3 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Charlie"));

    	DefaultMutableTreeNode wbg = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Workbench", "Workbenches"));
    	DefaultMutableTreeNode wb1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Workbench", "Field Trip 1"));
    	DefaultMutableTreeNode wb2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Workbench", "Field Trip 2"));
    	DefaultMutableTreeNode wb3 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Workbench", "Temp WB"));

    	usr1.add(wbg);
    	wbg.add(wb1);
    	wbg.add(wb2);
    	wbg.add(wb3);
    	
    	DefaultMutableTreeNode rs = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Record_Set", "Bundles"));
    	DefaultMutableTreeNode rs1 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Record_Set", "Record set 1"));
    	DefaultMutableTreeNode rs2 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Record_Set", "Record set 2"));
    	DefaultMutableTreeNode rs3 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("Record_Set", "Loan for Mr. Johnson"));

    	usr1.add(rs);
    	rs.add(rs1);
    	rs.add(rs2);
    	rs.add(rs3);

    	div1.add(col1);
    	div1.add(col2);
    	col1.add(grp4);
    	grp4.add(usr1);
    	grp4.add(usr2);
    	grp4.add(usr3);
    	
    	DefaultMutableTreeNode grp5 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Data Entry"));
    	DefaultMutableTreeNode usr4 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Denis"));
    	DefaultMutableTreeNode usr5 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Elliot"));
    	DefaultMutableTreeNode usr6 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Follie"));
    	
    	col1.add(grp5);
    	grp4.add(usr4);
    	grp4.add(usr5);
    	grp4.add(usr6);
    	
    	DefaultMutableTreeNode grp6 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("group", "Guest"));
    	DefaultMutableTreeNode usr7 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "George"));
    	DefaultMutableTreeNode usr8 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "John Doe"));
    	DefaultMutableTreeNode usr9 = new DefaultMutableTreeNode(new DataModelObjBaseWrapper("person", "Jane Doe"));

    	col1.add(grp6);
    	grp6.add(usr7);
    	grp6.add(usr8);
    	grp6.add(usr9);

/*    	globalGrp1.add(usr1);
    	globalGrp1.add(usr2);
    	globalGrp2.add(usr3);
    	globalGrp2.add(usr4);
    	globalGrp3.add(usr5);
    	globalGrp3.add(usr6);
    	globalGrp3.add(usr7);
    	globalGrp3.add(usr8);
    	globalGrp3.add(usr9);

    	globalGrp4.add(usr1);
    	globalGrp4.add(usr2);
    	globalGrp4.add(usr3);
    	globalGrp4.add(usr4);
    	globalGrp4.add(usr5);
    	globalGrp4.add(usr6);
    	globalGrp4.add(usr7);
    	globalGrp4.add(usr8);
    	globalGrp4.add(usr9);
*/
    	TreeSelectionListener tsl = new TreeSelectionListener()
    	{
    		public void valueChanged(TreeSelectionEvent tse)
    		{
    			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

    			if (node == null)
    				// Nothing is selected.	
    				return;

    			DataModelObjBaseWrapper dataWrp  = (DataModelObjBaseWrapper) (node.getUserObject());

    			showInfoPanel(dataWrp);
    		}
    	};
    	
    	DefaultTreeModel model = new DefaultTreeModel(root);
    	tree = new JTree(model);
    	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    	tree.setRootVisible(true);
    	tree.setCellRenderer(new MyTreeCellRenderer());
    	tree.addTreeSelectionListener(tsl);
    	//expandAll(tree, true);
    	
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout("180dlu", "f:p:g"));
        JScrollPane sp= new JScrollPane(tree);
    	sp.setMinimumSize(new Dimension(150, 300));
    	sp.setPreferredSize(new Dimension(180, 500));
    	mainPB.add(sp);

		JButton button = UIHelper.createIconBtn("Record_Set", IconManager.IconSize.Std24, "View all objects in a tree", buttonAL);
    	
    	return new SecurityPanelWrapper(mainPB.getPanel(), button, "FullTreeNavPanel");
	}
	
	protected SecurityPanelWrapper createUserNavPanel(ActionListener buttonAL)
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("This is the user nav panel"));
		JButton button = UIHelper.createIconBtn("person", IconManager.IconSize.Std24, "Manage users", buttonAL);
		return new SecurityPanelWrapper(panel, button, "UserNavPanel");
	}
	
	protected SecurityPanelWrapper createGroupNavPanel(ActionListener buttonAL)
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("This is the group nav panel"));
		JButton button = UIHelper.createIconBtn("group",  IconManager.IconSize.Std24, "Manage groups", buttonAL);
		return new SecurityPanelWrapper(panel, button, "GroupNavPanel");
	}
	
	protected SecurityPanelWrapper createObjectNavPanel(ActionListener buttonAL)
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("This is the object nav panel"));
		JButton button = UIHelper.createIconBtn("Record_Set", IconManager.IconSize.Std24, "Manage user objects", buttonAL);
		return new SecurityPanelWrapper(panel, button, "ObjectNavPanel");
	}

	protected JPanel createInformationPanel()
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"l:p:g", "p,3dlu,p,3dlu,t:p:g,3dlu,p,3dlu,p,3dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		infoPanelWrapperHash = new Hashtable<String, SecurityPanelWrapper>();

		infoCards = new JPanel();
		infoCards.setLayout(new CardLayout());
		infoCards.add(createInfoSubPanel(new DataModelObjBaseWrapper("Initial", "Initial")), "Initial");
		mainPB.add(infoCards, cc.xy(1, 5));
		
		return infoPanel;
	}
	
	protected void showInfoPanel(DataModelObjBaseWrapper objWrapper)
	{
		// show info panel that corresponds to the type of object selected
		String type = objWrapper.getType();
        CardLayout cl = (CardLayout)(infoCards.getLayout());
        SecurityPanelWrapper wrp = infoPanelWrapperHash.get(type);
        
        if (wrp == null)
        {
        	wrp = new SecurityPanelWrapper(createInfoSubPanel(objWrapper), null, type);
        	infoCards.add(wrp.getPanel(), type);
        	infoPanelWrapperHash.put(type, wrp);
        }
        
        cl.show(infoCards, wrp.getName());
	}
	
	protected JPanel createInfoSubPanel(DataModelObjBaseWrapper objWrapper)
	{
		String type = objWrapper.getType();
		if (StringUtils.isEmpty(type))
		{
			JPanel panel = new JPanel();
			panel.add(new JLabel("Empty Panel"));
			return panel;
		}
		
		if (type.equals("Bug") || type.equals("Plants") || type.equals("Fish") )
		{
			return createDisciplinePanel(objWrapper);
		}
		else if (type.equals("collection"))
		{
			return createCollectionPanel(objWrapper);
		}
		else if (type.equals("group"))
		{
			return createGroupPanel(objWrapper);
		}
		else if (type.equals("person"))
		{
			return createUserPanel(objWrapper);
		}
		else if (type.equals("Record_Set") || type.equals("Workbench"))
		{
			return createDataObjectPanel(objWrapper);
		}
		else
		{
			JPanel panel = new JPanel();
			panel.add(new JLabel("Panel for unknown type: " + type));
			return panel;
		}
	}
	
	protected JPanel createDisciplinePanel(DataModelObjBaseWrapper objWrapper)
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"r:p,2dlu,f:p:g", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		int y = 1;
		mainPB.addSeparator("Discipline", cc.xy(1, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Name"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Data Type"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getType(), fieldSize), cc.xy(3, y)); y += 2;
		//mainPB.add(UIHelper.createLabel(""), cc.xy(1, y));
		//mainPB.add(UIHelper.createTextField("", fieldSize), cc.xy(3, y)); Y += 2;
		
		return infoPanel;
	}

	protected JPanel createCollectionPanel(DataModelObjBaseWrapper objWrapper)
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"r:p,2dlu,f:p:g", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		int y = 1;
		mainPB.addSeparator("Collection", cc.xy(1, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Name"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 2;
		//mainPB.add(UIHelper.createLabel(""), cc.xy(1, y));
		//mainPB.add(UIHelper.createTextField("", fieldSize), cc.xy(3, y)); Y += 2;
		
		return infoPanel;
	}

	protected JPanel createGroupPanel(DataModelObjBaseWrapper objWrapper)
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"r:p,2dlu,l:p:g", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,15dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		JTable table = createGroupPermissionsTable();
		
		int y = 1;
		mainPB.addSeparator(objWrapper.getType(), cc.xy(1, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Group Name"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Group Type"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 8;
		
		mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2;
		mainPB.add(new JScrollPane(table), cc.xyw(1, y, 3)); y += 2;
		
		return infoPanel;
	}

	protected JTable createGroupPermissionsTable()
	{
		DefaultTableModel model = new DefaultTableModel()
		{
			public Class<?> getColumnClass(int columnIndex)
			{
				switch (columnIndex)
				{
					case 0: return ImageIcon.class;
					case 1: return String.class;
					default: return Boolean.class;
				}
			}
			
			public boolean isCellEditable(int row, int column) 
			{
				return (column >= 2);
			}
			
		};
		
		model.addColumn("");
		model.addColumn("Resource");
		model.addColumn("View");
		model.addColumn("Add");
		model.addColumn("Modify");
		model.addColumn("Delete");
		
		ImageIcon sysIcon = IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16);

		model.addRow(new Object[] { sysIcon, "Accession Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Collection Object Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Collecting Event Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Accession Reports", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Determinations Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Taxonomic Tree", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Accession Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Collection Object Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Collecting Event Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Accession Reports", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Determinations Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Taxonomic Tree", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});

		JTable table = new JTable(model);

		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMinWidth(16);
		column.setMaxWidth(16);
		column.setPreferredWidth(16);

		column = table.getColumnModel().getColumn(1);
		column.setMinWidth(100);
		column.setMaxWidth(400);
		column.setPreferredWidth(200);

//		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
//		column.setCellRenderer(renderer);

		return table;
	}

	protected JPanel createUserPanel(DataModelObjBaseWrapper objWrapper)
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"r:p,2dlu,l:p:g", "p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,15dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		JTable table = createUserPermissionsTable();
		
		int y = 1;
		mainPB.addSeparator(objWrapper.getType(), cc.xy(1, y)); y += 2;
		mainPB.add(UIHelper.createLabel("User Name"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Full Name"), cc.xy(1, y));
		mainPB.add(UIHelper.createTextField(objWrapper.getName(), fieldSize), cc.xy(3, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Password"), cc.xy(1, y));
		mainPB.add(UIHelper.createPasswordField(objWrapper.getName()), cc.xy(3, y)); y += 2;
		mainPB.add(UIHelper.createLabel("Verify Password"), cc.xy(1, y));
		mainPB.add(UIHelper.createPasswordField(objWrapper.getName()), cc.xy(3, y)); y += 2;

		mainPB.add(UIHelper.createCheckBox("account blocked"), cc.xy(3, y)); y += 2;

		mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2;
		mainPB.add(new JScrollPane(table), cc.xyw(1, y, 3)); y += 2;
		
		return infoPanel;
	}

	protected JTable createUserPermissionsTable()
	{
		DefaultTableModel model = new DefaultTableModel()
		{
			public Class<?> getColumnClass(int columnIndex)
			{
				switch (columnIndex)
				{
					case 0: return ImageIcon.class;
					case 1: return String.class;
					default: return Boolean.class;
				}
			}
			
			public boolean isCellEditable(int row, int column) 
			{
				return (column >= 2);
			}
			
		};
		
		model.addColumn("");
		model.addColumn("Title");
		model.addColumn("Owner");
		model.addColumn("Group");
		model.addColumn("Everyone");
		model.addColumn("Custom");
		
		ImageIcon wbIcon  = IconManager.getIcon("Workbench", IconManager.IconSize.Std16);
		ImageIcon rsIcon  = IconManager.getIcon("Record_Set", IconManager.IconSize.Std16);
		ImageIcon sysIcon = IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16);

		model.addRow(new Object[] { sysIcon, "Accession Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Determinations Form", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { sysIcon, "Taxonomic Tree", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { wbIcon,  "Workbench 1", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { wbIcon,  "Workbench 2", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { wbIcon,  "Field Trip 23", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { rsIcon,  "Bundle 1", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});
		model.addRow(new Object[] { rsIcon,  "Loan for Mr. Johnson", new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true), new Boolean(true)});

		JTable table = new JTable(model);

		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMinWidth(16);
		column.setMaxWidth(16);
		column.setPreferredWidth(16);

		column = table.getColumnModel().getColumn(1);
		column.setMinWidth(100);
		column.setMaxWidth(400);
		column.setPreferredWidth(200);

//		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
//		column.setCellRenderer(renderer);

		
		return table;
	}
	
	protected JPanel createDataObjectPanel(DataModelObjBaseWrapper objWrapper)
	{
		return new JPanel();
	}

	protected class DataModelObjBaseWrapper 
	{
		protected String iconName;
		protected String name;
		
		public DataModelObjBaseWrapper(String iconName, String name)
		{
			this.iconName = iconName;
			this.name = name;
		}

		public ImageIcon getIcon()
		{
			return IconManager.getIcon(iconName, IconManager.IconSize.Std16);
		}

		public String getName()
		{
			return name;
		}
		
		public String getType()
		{
			return iconName;
		}
		
		public String toString()
		{
			return name;
		}
	}
	
    protected class MyTreeCellRenderer extends DefaultTreeCellRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(
        		JTree tree, 
        		Object value,
        		boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus)
        {
        	super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
        	
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        	Object obj = node.getUserObject();
            if (obj instanceof DataModelObjBaseWrapper)
            {
            	DataModelObjBaseWrapper wrp = (DataModelObjBaseWrapper) obj;
                String text = obj.toString();
                setText(text);
                setToolTipText(text);
                ImageIcon icon = wrp.getIcon();
                setIcon(icon);
            }
        	return this;
        }
    }

}
