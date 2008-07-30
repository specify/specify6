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

package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.SpecifySecurityMgr;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroupScope;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.SearchBox;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.ui.db.JAutoCompTextField;
import edu.ku.brc.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class SecurityAdminPane extends BaseSubPane
{
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(SecurityAdminPane.class);

	private JTree  tree;
	private JPanel infoCards;
	private Hashtable<String, AdminInfoSubPanelWrapper> infoSubPanels;
	private AdminInfoSubPanelWrapper currentDisplayPanel;
	private JAutoCompTextField searchText; 

	private JButton[] navToolbarBtns;
	private JButton delBtn;
	private JButton addDiscBtn;
	private JButton addCollBtn;
	private JButton addGrpBtn;
	private JButton addUserBtn;
	
	private boolean hasPermissionToAdd = false;
	private boolean hasPermissionToModify = false;
	private boolean hasPermissionToDelete = false;
	
	private final int formOptions = MultiView.IS_SINGLE_OBJ | MultiView.VIEW_SWITCHER;
	
	/**
	 * Constructor
	 * @param name
	 * @param task
	 */
	public SecurityAdminPane(String name, final Taskable task)
	{
		super(name, task);
		
		// check some admin permissions
		
		// check for permission to delete objects (users, collections, etc) on the security admin panel 
		hasPermissionToAdd    = SpecifySecurityMgr.checkPermission("Task.SecurityAdmin", "add");
		hasPermissionToModify = SpecifySecurityMgr.checkPermission("Task.SecurityAdmin", "modify");
		hasPermissionToDelete = SpecifySecurityMgr.checkPermission("Task.SecurityAdmin", "delete");
	}
	
	public JPanel createMainControlUI()
	{
		JPanel securityAdminPanel = new JPanel();
        //JPanel securityAdminPanel = new FormDebugPanel();
		
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"3dlu,p,4px,3dlu,4px,f:p:g,3dlu",
        		"3dlu,f:p:g,3dlu,p,3dlu,p,3dlu"), 
        		securityAdminPanel);
		final CellConstraints cc = new CellConstraints();
        
        mainPB.add(createNavigationPanel(),  cc.xy(2, 2));
        mainPB.add(new VerticalSeparator(new Color(224, 224, 224), new Color(124, 124, 124)),  cc.xy(4, 2));
		mainPB.add(createInformationPanel(), cc.xy(6, 2));
		
		updateUIEnabled(null);
		
		this.add(securityAdminPanel, BorderLayout.CENTER);
		return securityAdminPanel;
	}
	
	/**
	 * Creates the whole navigation panel which contains the navigation tree and the user list at the bottom.
	 * @return the navigation panel
	 */
	private JPanel createNavigationPanel()
	{
		JPanel navigationPanel = new JPanel();
		//JPanel navToolbarPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"f:p:g", "p,3dlu,p,3dlu,f:p:g,3dlu,p,3dlu,p,3dlu,p,3dlu,p"), navigationPanel);
		final CellConstraints cc = new CellConstraints();

		JPanel navTreePanel = createFullTreeNavPanel(); // navigation jTree gets created here 

		DocumentListener searchDL = new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)  { changed(e); }
			public void insertUpdate(DocumentEvent e)  { changed(e); }
			public void changedUpdate(DocumentEvent e) { changed(e); }

			private void changed(DocumentEvent e)
			{ 
				FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
				Filter filter = (StringUtils.isNotEmpty(searchText.getText()))? 
						new Filter(searchText.getText()) : null;
				model.setFilter(filter);
			}
		};
		
		searchText = new JAutoCompTextField(UIHelper.isMacOS() ? 15 : 22);
		searchText.getDocument().addDocumentListener(searchDL);
		SearchBox searchBox = new SearchBox(searchText, null);
		
        final PanelBuilder toolbarPB = new PanelBuilder(new FormLayout("l:p,1dlu,p,1dlu,p,1dlu,p,15dlu,r:p", "p"));
		toolbarPB.add(searchBox, cc.xy(1, 1));
		
		mainPB.add(toolbarPB.getPanel(),  cc.xy(1, 3));
		mainPB.add(navTreePanel,          cc.xy(1, 5));

		return navigationPanel;
	}

	/**
	 * @return
	 */
	private JPanel createAddDeleteNavToolbarPanel()
	{
        final PanelBuilder toolbarPB = new PanelBuilder(new FormLayout(UIHelper.createDuplicateJGoodiesDef("p", "2px", 5), "p"));
		final CellConstraints cc = new CellConstraints();
		
		ActionListener btnAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if      (ae.getSource().equals(addDiscBtn)) { addDiscipline(); }
				else if (ae.getSource().equals(addCollBtn)) { addCollection(); }
				else if (ae.getSource().equals(addUserBtn)) { addUser(); }
				else if (ae.getSource().equals(addGrpBtn))  { addGroup(); }
			}
		};
		
		IconManager.IconSize sz = IconManager.IconSize.NonStd;
		delBtn     = UIHelper.createButton(IconManager.getIcon("MinusSign", IconManager.IconSize.Std16));
		addUserBtn = UIHelper.createButton(IconManager.getIcon("add-person", sz));
		addGrpBtn  = UIHelper.createButton(IconManager.getIcon("add-group", sz));
		addCollBtn = UIHelper.createButton(IconManager.getIcon("add-collection", sz));
		addDiscBtn = UIHelper.createButton(IconManager.getIcon("add-discipline", sz));
		
		navToolbarBtns = new JButton[5];
		navToolbarBtns[0] = delBtn;
		navToolbarBtns[1] = addUserBtn;
		navToolbarBtns[2] = addGrpBtn;
		navToolbarBtns[3] = addCollBtn;
		navToolbarBtns[4] = addDiscBtn;
		
		int x = 1;
		for (JButton btn : navToolbarBtns)
		{
			btn.addActionListener(btnAL);
			toolbarPB.add(btn, cc.xy(x, 1));
			x += 2;
		}
		
		return toolbarPB.getPanel();
	}

	/**
	 * Adds a new discipline to the selected institution in the table model.
	 * Also adds an anonymous division as the parent of the discipline.
	 */
	public void addDiscipline()
	{
		// get parent institution from tree selection
		DefaultMutableTreeNode instNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (instNode == null || !(instNode.getUserObject() instanceof DataModelObjBaseWrapper))
			// Nothing is selected or object type isn't relevant	
			return;

		DataModelObjBaseWrapper instWrp  = (DataModelObjBaseWrapper) (instNode.getUserObject());
		if (!instWrp.isInstitution())
			// selection isn't an institution
			return;
		
		Institution institution = (Institution) instWrp.getDataObj();
		Division    division    = new Division();
		Discipline  discipline  = new Discipline();
		
		division.initialize();
		discipline.initialize();
		
		division.setInstitution(institution);
		discipline.setDivision(division);
		
		division.setName("Anonymous Division");
		discipline.setName("New Discipline");
		
		// The commented lines below insert a division into the tree with the discipline
		// It's there for reference only
		
		//DataModelObjBaseWrapper divWrp  = new DataModelObjBaseWrapper(division);
		DataModelObjBaseWrapper discWrp = new DataModelObjBaseWrapper(discipline);
		
		//DefaultMutableTreeNode divNode  = new DefaultMutableTreeNode(divWrp);
		DefaultMutableTreeNode discNode = new DefaultMutableTreeNode(discWrp);
		
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		//model.insertNodeInto(divNode,  instNode, instNode.getChildCount());
		//model.insertNodeInto(discNode, divNode,  divNode.getChildCount());
		model.insertNodeInto(discNode, instNode,  instNode.getChildCount());
		
		tree.setSelectionPath(new TreePath(discNode.getPath()));
	}

	/**
	 * Adds a new collection to the selected discipline in the table model. 
	 */
	public void addCollection()
	{
		// get parent institution from tree selection
		DefaultMutableTreeNode discNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (discNode == null || !(discNode.getUserObject() instanceof DataModelObjBaseWrapper))
			// Nothing is selected or object type isn't relevant	
			return;

		DataModelObjBaseWrapper discWrp  = (DataModelObjBaseWrapper) (discNode.getUserObject());
		if (!discWrp.isDiscipline())
			// selection isn't a discipline
			return;
		
		Discipline discipline = (Discipline) discWrp.getDataObj();
		Collection collection = new Collection();
		collection.initialize();
		collection.setDiscipline(discipline);
		collection.setCollectionName("New Collection");
		DataModelObjBaseWrapper collWrp  = new DataModelObjBaseWrapper(collection);
		DefaultMutableTreeNode  collNode = new DefaultMutableTreeNode(collWrp);
		
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.insertNodeInto(collNode, discNode, discNode.getChildCount());
		
		tree.setSelectionPath(new TreePath(collNode.getPath()));
	}

	/**
	 * Adds a new user to the selected group in the table model. 
	 */
	public void addUser()
	{
		// get parent institution from tree selection
		DefaultMutableTreeNode grpNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (grpNode == null || !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
			// Nothing is selected or object type isn't relevant	
			return;

		DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (grpNode.getUserObject());
		if (!parentWrp.isGroup())
			// selection isn't a suitable parent for a group
			return;
		
		SpPrincipal group = (SpPrincipal) parentWrp.getDataObj();
		SpecifyUser user = new SpecifyUser();
		user.initialize();
		Set<SpecifyUser> usersFromGroup = group.getSpecifyUsers();
		if (usersFromGroup == null) {
			usersFromGroup = new HashSet<SpecifyUser>();
		}
		usersFromGroup.add(user);
		user.setName("New User");
		DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(group);
		DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);
		
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
		
		tree.setSelectionPath(new TreePath(userNode.getPath()));
	}
	
	/**
	 * Adds a new group to the selected institution, discipline, or collection in the table model. 
	 */
	public void addGroup()
	{
		// get parent (scope) of the group from tree selection
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		if (parentNode == null || !(parentNode.getUserObject() instanceof DataModelObjBaseWrapper))
			// Nothing is selected or object type isn't relevant	
			return;

		DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (parentNode.getUserObject());
		if (!parentWrp.isInstitution() && !parentWrp.isDiscipline() && !parentWrp.isCollection())
			// selection isn't a suitable parent for a group
			return;
		
		UserGroupScope scope = (UserGroupScope) parentWrp.getDataObj();
		SpPrincipal group = new SpPrincipal();
		group.initialize();
		group.setGroupSubClass(GroupPrincipal.class.getCanonicalName());
		group.setScope(scope);
		group.setName("New Group");
		DataModelObjBaseWrapper grpWrp  = new DataModelObjBaseWrapper(group);
		DefaultMutableTreeNode  grpNode = new DefaultMutableTreeNode(grpWrp);
		
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		model.insertNodeInto(grpNode, parentNode, parentNode.getChildCount());
		
		tree.setSelectionPath(new TreePath(grpNode.getPath()));
	}
	
	public JList createUserList()
	{
    	DefaultListModel listModel = new DefaultListModel();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	List<SpecifyUser> users = session.getDataList(SpecifyUser.class);
        	for (SpecifyUser user : users)
        	{
        		listModel.addElement(user);
        	}
        } 
        finally
        {
        	session.close();
        }
        
    	JList userList = new JList(listModel);
        
		return userList;
	}
	    	
	private void createNavigationTree()
	{
    	TreeSelectionListener tsl = new TreeSelectionListener()
    	{
    		public void valueChanged(TreeSelectionEvent tse)
    		{
    			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

    			if (node == null || !(node.getUserObject() instanceof DataModelObjBaseWrapper))
    				// Nothing is selected or object type isn't relevant	
    				return;

    			// ask if user he wants to discard changes if that's the case
    			if (!aboutToShutdown())
    			{
    	    		return;
    			}
    			
    			DataModelObjBaseWrapper dataWrp  = (DataModelObjBaseWrapper) (node.getUserObject());
    			
    			// get parent if it is a user
    			DataModelObjBaseWrapper secondObjWrp = null;
    			if (dataWrp.getDataObj() instanceof SpecifyUser)
    			{
        			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        			secondObjWrp = (DataModelObjBaseWrapper) parent.getUserObject();
    			}

    			showInfoPanel(dataWrp, secondObjWrp);
    			updateUIEnabled(dataWrp);
    		}
    	};

    	DefaultTreeModel model = createNavigationTreeModel();
		tree = new JTree(model);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setCellRenderer(new MyTreeCellRenderer());
		tree.addTreeSelectionListener(tsl);
    	//expandAll(tree, true);
	}

	private boolean formHasChanged()
	{
		// check if there are unsaved data to save
    	if (currentDisplayPanel != null)
    	{
    		MultiView mv = currentDisplayPanel.getMultiView();
    		return mv.hasChanged();
    	}

    	return false;
	}
	
	/**
	 * Returns the root of the navigation tree model 
	 * @return the root of the navigation tree model 
	 */
	private DefaultTreeModel createNavigationTreeModel()
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(UIRegistry.getAppName());

        try
        {
        	// include all institutions
        	addInstitutionsRecursively(session, root);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            session.close();
        }
        
    	DefaultTreeModel model = new FilteredTreeModel(root, null);
		
		return model;
	}
	
	/**
	 * Adds all institutions as nodes to the navigation tree. Adds children nodes recursively (divisions,
	 * disciplines, collections, user groups, and users).
	 * @param session Data provider session to be used to retrieve data objects 
	 * @param root Root of the tree to add children (institutions) to
	 */
	private void addInstitutionsRecursively(DataProviderSessionIFace session, DefaultMutableTreeNode root)
	{
    	List<Institution> institutions = session.getDataList(Institution.class);
        Collections.sort(institutions, new ComparatorByStringRepresentation<Institution>()); 
    	for (Institution institution : institutions)
    	{
    		DefaultMutableTreeNode instNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(institution));
    		root.add(instNode);
    		addDivisionsRecursively(session, instNode, institution);
			addGroup(session, instNode, institution);
    	}
	}
	
	private void addDivisionsRecursively(DataProviderSessionIFace session, DefaultMutableTreeNode instNode, Institution institution)
	{
		// sort divisions
		TreeSet<Division> divisions = new TreeSet<Division>(institution.getDivisions()); 
		for (Division division : divisions)
		{
			// sort disciplines
			TreeSet<Discipline> disciplines = new TreeSet<Discipline>(division.getDisciplines()); 
			for (Discipline discipline : disciplines)
			{
	    		DefaultMutableTreeNode discNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(discipline));
	    		instNode.add(discNode);
				addCollectionsRecursively(session, discNode, discipline);
				addGroup(session, discNode, discipline);
			}
			
			// The code below is to add divisions when these are to be visible (in a future release)
    		//DefaultMutableTreeNode divNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(division));
			//instNode.add(divNode);
			//addDisciplinesRecursively(session, divNode, division);
		}
	}

	@SuppressWarnings("unused")  // will be used eventually when divisions can have more than one discipline 
	private void addDisciplinesRecursively(DataProviderSessionIFace session, DefaultMutableTreeNode divNode, Division division)
	{
		// sort disciplines
		TreeSet<Discipline> disciplines = new TreeSet<Discipline>(division.getDisciplines()); 
		for (Discipline discipline : disciplines)
		{
    		DefaultMutableTreeNode discNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(discipline));
			divNode.add(discNode);
			addCollectionsRecursively(session, discNode, discipline);
			addGroup(session, discNode, discipline);
		}
	}

	private void addCollectionsRecursively(DataProviderSessionIFace session, DefaultMutableTreeNode discNode, Discipline discipline)
	{
		// sort collections
		TreeSet<Collection> collections = new TreeSet<Collection>(discipline.getCollections()); 
		for (Collection collection : collections)
		{
    		DefaultMutableTreeNode collNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(collection));
    		discNode.add(collNode);
			addGroup(session, collNode, collection);
		}
	}

	private void addGroup(DataProviderSessionIFace session, 
							DefaultMutableTreeNode node, 
							UserGroupScope scope)
	{
		// sort groups
		TreeSet<SpPrincipal> groups = new TreeSet<SpPrincipal>(scope.getUserGroups()); 
		for (SpPrincipal group : groups)
		{
    		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(group));
    		node.add(groupNode);
			
    		// sort users
    		TreeSet<SpecifyUser> users = new TreeSet<SpecifyUser>(group.getSpecifyUsers());
			for (SpecifyUser user : users) 
			{
	    		DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(user));
	    		groupNode.add(userNode);
			}
		}
	}

	
	/**
	 * @return
	 */
	private JPanel createFullTreeNavPanel()
	{
    	createNavigationTree();
    	JList userList = createUserList();
    	
		JPanel addDeleteNavToolbarPanel = createAddDeleteNavToolbarPanel();
		
		String helpStr = "<html>To add an existing user to a group, just " +
				"drag the user from this list and drop it into the appropriate " +
				"group on the list above.</html>"; // I18N
		JLabel userDnDHelp = UIHelper.createLabel(helpStr);
		
		final PanelBuilder mainPB = new PanelBuilder(new FormLayout("min(210px;p):g", "f:min(100px;p):g,p,15px,p,p,p")/*, new FormDebugPanel()*/);
		final CellConstraints cc = new CellConstraints();
        
		JScrollPane sp = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	mainPB.add(sp,                        cc.xy(1, 1));
		mainPB.add(addDeleteNavToolbarPanel,  cc.xy(1, 2));
    	mainPB.addSeparator("Users",          cc.xy(1, 4)); // I18N
    	
    	sp = new JScrollPane(userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	mainPB.add(sp,                        cc.xy(1, 5));
    	mainPB.add(userDnDHelp,               cc.xy(1, 6));

    	return mainPB.getPanel();
	}
	
	private class FilteredTreeModel extends DefaultTreeModel
	{
		private Filter filter;
		
		public FilteredTreeModel(DefaultMutableTreeNode root, Filter filter)
		{
			super(root);
			this.filter = filter;
		}

		public Object getChild(Object parent, int index) 
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
			
			if (!(node.getUserObject() instanceof DataModelObjBaseWrapper))
				return node.getChildAt(index);
			
			DataModelObjBaseWrapper parentWrapper = (DataModelObjBaseWrapper) node.getUserObject();

			if (filter == null || !parentWrapper.isGroup())
			{
				return node.getChildAt(index);
			}
			
			// we only get here if parent is wrapping a user group
			int pos = 0;
			for (int i = 0, cnt = 0; i < node.getChildCount(); i++) 
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
				DataModelObjBaseWrapper childWrapper = (DataModelObjBaseWrapper) child.getUserObject();
				if(filter.accepts(childWrapper.getName())) 
				{
					if (cnt++ == index) 
					{
						pos = i;
						break;
					}
				}
			}
			
			// no need to check if children is a division, because parent is a user group, not an institution 
			return node.getChildAt(pos);
		}
		
		public int getChildCount(Object parent) 
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
		
			if (!(node.getUserObject() instanceof DataModelObjBaseWrapper))
				return node.getChildCount();
			
			DataModelObjBaseWrapper parentWrapper = (DataModelObjBaseWrapper) node.getUserObject();

			if (filter==null || !parentWrapper.isGroup())
				return node.getChildCount();

			int childCount = 0;
			Enumeration<?> children = node.children();
			while (children.hasMoreElements()) 
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
				DataModelObjBaseWrapper childWrapper = (DataModelObjBaseWrapper) child.getUserObject();
				if (filter.accepts(childWrapper.getName())) 
				{
					childCount++;
				}
			}
			return childCount;
		}
		
		public void setFilter(Filter filter) 
		{
			if (this.filter == null && filter == null)
				return;
			
			if (this.filter != null && this.filter.equals(filter))
				return;
			
			this.filter = filter;
			
			Object[] path = {root};
			int[] childIndices = new int[root.getChildCount()];
			Object[] children = new Object[root.getChildCount()];

			for (int i = 0; i < root.getChildCount(); i++) 
			{
				childIndices[i] = i;
				children[i] = root.getChildAt(i);
			}
			fireTreeStructureChanged(this, path, childIndices, children);
			
			// open all selected users
			openUserNodesRecursive(root);
		}

		public void openUserNodesRecursive(TreeNode node)
		{
			DefaultMutableTreeNode defNode = (DefaultMutableTreeNode) node;
			if (defNode.getUserObject() instanceof DataModelObjBaseWrapper)
			{
				DataModelObjBaseWrapper nodeWrapper = (DataModelObjBaseWrapper) defNode.getUserObject();

				if (filter != null && nodeWrapper.isUser())
				{
					tree.scrollPathToVisible(new TreePath(defNode.getPath()));
					return;
				}
			}
			
			for (int i = 0; i < getChildCount(node); i++) 
			{
				openUserNodesRecursive((TreeNode) getChild(node, i));
			}
			
		}

	}
	
	private class Filter
	{
		private String patternStr;
		private Pattern p;
		private Matcher m;
		
		public Filter(final String patternStr)
		{
			this.patternStr = patternStr.replaceAll("\\*", ".*");
			try 
			{
				p = Pattern.compile(this.patternStr);
			}
			catch (PatternSyntaxException pse)
			{
				p = null;
			}
		}
		
		public boolean accepts(final String subject)
		{
			if (StringUtils.isEmpty(patternStr))
			{
				return true;
			}
			
			if (p == null)
			{
				return false;
			}
			
			m = p.matcher(subject);
			return m.matches();
			//return Pattern.matches(patternStr, subject);
		}
	}
	
	private JPanel createInformationPanel()
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"f:p:g", "p,3dlu,p,3dlu,t:p:g,3dlu,p,3dlu,p,3dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		infoCards = new JPanel();
		infoCards.setLayout(new CardLayout());
		createInitialInfoSubPanels();
		mainPB.add(infoCards, cc.xy(1, 5));
		
		return infoPanel;
	}
	
	private void showInfoPanel(final DataModelObjBaseWrapper objWrapper, final DataModelObjBaseWrapper secondObjWrapper)
	{
		// show info panel that corresponds to the type of object selected
		String className = objWrapper.getType();
        CardLayout cl = (CardLayout)(infoCards.getLayout());
        AdminInfoSubPanelWrapper panelWrapper = infoSubPanels.get(className);
        // fill form with object data
        currentDisplayPanel = panelWrapper;
        currentDisplayPanel.setData(objWrapper.getDataObj(), secondObjWrapper != null? secondObjWrapper.getDataObj() : null);
        cl.show(infoCards, className);
	}
	
	/**
	 * Creates one panel for each kind of form that may be used and stores them for later
	 */
	private void createInitialInfoSubPanels()
	{
		boolean editing = hasPermissionToModify;

		infoSubPanels = new Hashtable<String, AdminInfoSubPanelWrapper>();
		
		createInfoSubPanel("SystemSetup", "Institution", "Institution", Institution.class, "institutionId", editing, formOptions | MultiView.IS_NEW_OBJECT);
		createInfoSubPanel("SystemSetup", "DisciplineWithoutCollections", "Discipline", Discipline.class, "disciplineId", editing, formOptions | MultiView.IS_NEW_OBJECT);
		createInfoSubPanel("SystemSetup", "Collection", "Collection", Collection.class, "collectionId", editing, formOptions | MultiView.IS_NEW_OBJECT);
		createUserPanel();
		createGroupPanel();
	}
	
	/**
	 * Creates an info sub panel for a given object type and adds it to the card layout panel  
	 * @param formViewSet Name of the form view set to use
	 * @param formView Name of the form within the specified set
	 * @param displayName
	 * @param clazz
	 * @param idFieldName
	 * @param editing
	 * @param formOptionsArg
	 */
	private void createInfoSubPanel(final String formViewSet, 
	                                final String formView, 
	                                final String displayName, 
	                                final Class<?> clazz, 
	                                final String idFieldName, 
	                                final boolean editing,
	                                final int formOptionsArg)
	{
		String className = clazz.getCanonicalName();
		
		ViewBasedDisplayPanel panel = new ViewBasedDisplayPanel(null, formViewSet, formView, displayName,
				className, idFieldName, editing, formOptionsArg);

		AdminInfoSubPanelWrapper panelWrapper = new AdminInfoSubPanelWrapper(panel);
		
    	infoCards.add(panel, className);
    	infoSubPanels.put(className, panelWrapper);
	}
	
	private void createUserPanel()
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"f:p:g", "t:80dlu,5dlu,p,2dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		// create general permission table
		JPanel generalPermissionsPanel = new JPanel();
		JTable generalPermissionsTable = new JTable();
		UIHelper.makeTableHeadersCentered(generalPermissionsTable, false);
		generalPermissionsPanel.add(new JScrollPane(generalPermissionsTable));
		PermissionEditor generalPermissionsEditor = createGeneralPermissionsEditor(generalPermissionsTable);

		// create object permission table
		JPanel objectPermissionsPanel = new JPanel();
		JTable objectPermissionsTable = new JTable();
		objectPermissionsPanel.add(new JScrollPane(objectPermissionsTable));
		PermissionEditor objectsPermissionEditor = createObjectPermissionsEditor(objectPermissionsTable);
		
		// create user form
		ViewBasedDisplayPanel panel = createViewBasedDisplayPanelForUser();
		
		// create tabbed panel for different kinds of permission editing tables
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General", generalPermissionsPanel); // I18N
		tabbedPane.addTab("Objects", objectPermissionsPanel);  // I18N
		
		// lay out controls on panel
		int y = 1;
		mainPB.add(panel, cc.xy(1, y)); y += 2;
		
		mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2; // I18N
		mainPB.add(tabbedPane, cc.xy(1, y)); y += 2;

		JButton saveBtn = UIHelper.createButton("Save"); // I18N
        PanelBuilder saveBtnPB = new PanelBuilder(new FormLayout("r:p", "p")/*, new FormDebugPanel()*/);
        saveBtnPB.add(saveBtn);
        mainPB.add(saveBtnPB.getPanel(), cc.xy(1, y)); y += 2;
        
        addSaveBtnActionListener(saveBtn);
		
		String className = SpecifyUser.class.getCanonicalName();
    	infoCards.add(infoPanel, className);
    	AdminInfoSubPanelWrapper subPanel = new AdminInfoSubPanelWrapper(panel);
    	subPanel.addPermissionEditor(generalPermissionsEditor);
    	subPanel.addPermissionEditor(objectsPermissionEditor);
    	infoSubPanels.put(className, subPanel);
	}
	
	private void createGroupPanel()
	{
		JPanel infoPanel = new JPanel();
		//JPanel infoPanel = new FormDebugPanel();
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout(
        		"f:p:g", "t:80dlu,5dlu,p,2dlu,p,2dlu,p,2dlu,p"), infoPanel);
		final CellConstraints cc = new CellConstraints();

		JTable table = new JTable();
		UIHelper.makeTableHeadersCentered(table, false);
		PermissionEditor editor = createGeneralPermissionsEditor(table);
		ViewBasedDisplayPanel panel = createViewBasedDisplayPanelForGroup();
		
		int y = 1;
		mainPB.add(panel, cc.xy(1, y)); y += 2;
		
		mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2; // I18N
		mainPB.add(new JScrollPane(table), cc.xy(1, y)); y += 2;

		JButton saveBtn = UIHelper.createButton("Save"); // I18N
        PanelBuilder saveBtnPB = new PanelBuilder(new FormLayout("r:p", "p")/*, new FormDebugPanel()*/);
        saveBtnPB.add(saveBtn);
        mainPB.add(saveBtnPB.getPanel(), cc.xy(1, y)); y += 2;
        
        addSaveBtnActionListener(saveBtn);
		
		String className = SpPrincipal.class.getCanonicalName();
    	infoCards.add(infoPanel, className);
    	AdminInfoSubPanelWrapper subPanel = new AdminInfoSubPanelWrapper(panel);
    	subPanel.addPermissionEditor(editor);
    	infoSubPanels.put(className, subPanel);
	}

	private void addSaveBtnActionListener(JButton saveBtn)
	{
		saveBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// save object in view 
				saveObjectInCurrentDisplayPanel();
				
				// then save permissions
				currentDisplayPanel.savePermissionData();
			}
		}
		);
	}
	
	/**
	 * @param table
	 * @return
	 */
	private PermissionEditor createGeneralPermissionsEditor(final JTable table)
	{
		PermissionEnumerator e1 = new FormPermissionEnumerator();
		PermissionEnumerator e2 = new TaskPermissionEnumerator();
		CompositePermissionEnumerator enumerator = new CompositePermissionEnumerator();
		enumerator.addEnumerator(e1);
		enumerator.addEnumerator(e2);
		return new PermissionEditor(table, enumerator);
	}

	/**
	 * @param table
	 * @return
	 */
	private PermissionEditor createObjectPermissionsEditor(JTable table)
	{
		return new ObjectPermissionEditor(table, new ObjectPermissionEnumerator());
	}

	private ViewBasedDisplayPanel createViewBasedDisplayPanelForUser()
	{
		return new ViewBasedDisplayPanel(null, "SystemSetup", "User", "User", 
				SpecifyUser.class.getCanonicalName(), "specifyUserId", true, MultiView.HIDE_SAVE_BTN);
	}
	
	private ViewBasedDisplayPanel createViewBasedDisplayPanelForGroup()
	{
		return new ViewBasedDisplayPanel(null, "SystemSetup", "UserGroup", "User Group", 
				SpPrincipal.class.getCanonicalName(), "spUserGroupId", true, MultiView.HIDE_SAVE_BTN);
	}
	
	private void updateUIEnabled(DataModelObjBaseWrapper objWrapper)
	{
		boolean isInstitution = (objWrapper != null)? objWrapper.isInstitution() : false;
		boolean isDiscipline  = (objWrapper != null)? objWrapper.isDiscipline()  : false;
		boolean isCollection  = (objWrapper != null)? objWrapper.isCollection()  : false;
		boolean isGroup       = (objWrapper != null)? objWrapper.isGroup()       : false;
		
		delBtn.setEnabled(
				hasPermissionToDelete && 
				objWrapper != null && 
				!isInstitution &&
				!isCollection );
		
		addDiscBtn.setEnabled(
				hasPermissionToAdd &&
				objWrapper != null && 
				isInstitution );
		
		addCollBtn.setEnabled(
				hasPermissionToAdd &&
				objWrapper != null && 
				isDiscipline);
		
		addGrpBtn. setEnabled(
				hasPermissionToAdd &&
				objWrapper != null && 
				(isInstitution || isDiscipline || isCollection));

		addUserBtn.setEnabled(
				hasPermissionToAdd &&
				objWrapper != null && 
				isGroup);
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    //@Override
    public boolean aboutToShutdown()
    {
        boolean result = true;
        if (formHasChanged())
        {
            String msg = String.format(getResourceString("SaveChanges"), getTitle());
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();

            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   msg,
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
            	saveObjectInCurrentDisplayPanel();
            }
            else if (rv == JOptionPane.CANCEL_OPTION || rv == JOptionPane.CLOSED_OPTION)
            {
                return false;
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                // nothing
            }
        }
        return result;
    }

    public void saveObjectInCurrentDisplayPanel()
    {
    	MultiView mv = currentDisplayPanel.getMultiView();
    	Viewable view = mv.getCurrentView();
    	if (view instanceof FormViewObj)
    	{
    		// this is needed because doSave() method is not part of Viewable interface (but it may have been one day)
    		FormViewObj vo = (FormViewObj) view;
    		vo.doSave();
    	}
    }
	
	
    private class MyTreeCellRenderer extends DefaultTreeCellRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(
        		JTree rnTree, 
        		Object value,
        		boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean rnHasFocus)
        {
        	super.getTreeCellRendererComponent(
                    rnTree, value, sel,
                    expanded, leaf, row,
                    rnHasFocus);
        	
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
