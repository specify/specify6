/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
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

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
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
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;
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

    private JTree                                       tree;
    private JPanel                                      infoCards;
    private Hashtable<String, AdminInfoSubPanelWrapper> infoSubPanels;
    private Hashtable<String, EditorPanel>              editorPanels        = new Hashtable<String, EditorPanel>();
    private AdminInfoSubPanelWrapper                    currentDisplayPanel = null;
    private EditorPanel                                 currentEditorPanel  = null;
    private String                                      currentTitle        = null;
    private JAutoCompTextField                          searchText;

    private JButton[] navToolbarBtns;
    private JButton delBtn;
    private JButton addDiscBtn;
    private JButton addCollBtn;
    private JButton addGrpBtn;
    private JButton addUserBtn;
    
    // manages creation and deletion of items on the navigation tree
    private NavigationTreeMgr navTreeMgr;

    
    private boolean hasPermissionToAdd    = false;
    private boolean hasPermissionToModify = false;
    private boolean hasPermissionToDelete = false;
    
    private DataModelObjBaseWrapper objWrapper       = null;
    private DataModelObjBaseWrapper secondObjWrapper = null;
    
    private final int formOptions = MultiView.IS_EDITTING | MultiView.IS_NEW_OBJECT;
    
    /**
     * Constructor
     * @param name
     * @param task
     */
    public SecurityAdminPane(final String name, final Taskable task)
    {
        super(name, task);
        
        // check some admin permissions
        
        // check for permission to delete objects (users, collections, etc) on the security admin panel 
        hasPermissionToAdd    = SecurityMgr.getInstance().checkPermission("Task.SecurityAdmin", "add");
        hasPermissionToModify = SecurityMgr.getInstance().checkPermission("Task.SecurityAdmin", "modify");
        hasPermissionToDelete = SecurityMgr.getInstance().checkPermission("Task.SecurityAdmin", "delete");
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

            private void changed(@SuppressWarnings("unused") DocumentEvent e)
            { 
                FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                Filter filter = (StringUtils.isNotEmpty(searchText.getText())) ? 
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
        final int numBtns = 3;
        
        final PanelBuilder toolbarPB = new PanelBuilder(new FormLayout(UIHelper.createDuplicateJGoodiesDef("p", "2px", numBtns), "p"));
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
        
        navToolbarBtns = new JButton[numBtns];
        navToolbarBtns[0] = delBtn;
        navToolbarBtns[1] = addUserBtn;
        navToolbarBtns[2] = addGrpBtn;
        if (numBtns == 5)
        {
            navToolbarBtns[3] = addCollBtn;
            navToolbarBtns[4] = addDiscBtn;
        }
        
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
    private void addDiscipline()
    {
        // get parent institution from tree selection
        navTreeMgr.addNewDiscipline((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }

    /**
     * Adds a new collection to the selected discipline in the table model. 
     */
    private void addCollection()
    {
        // get parent institution from tree selection
        navTreeMgr.addNewCollection((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }

    /**
     * Adds a new user to the selected group in the table model. 
     */
    private void addUser()
    {
        // get parent group node from tree selection and add user to it
        navTreeMgr.addNewUser((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }
    
    /**
     * Adds a new group to the selected institution, discipline, or collection in the table model. 
     */
    private void addGroup()
    {
        // get parent (scope) of the group from tree selection
        navTreeMgr.addNewGroup((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }
    
    /**
     * 
     */
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

                showInfoPanel(dataWrp, secondObjWrp, node.toString());
                updateUIEnabled(dataWrp);
            }
        };

        DefaultTreeModel model = createNavigationTreeModel();
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.setCellRenderer(new MyTreeCellRenderer());
        tree.addTreeSelectionListener(tsl);

        // Expand the tree
        for (int i = 0; i < tree.getRowCount(); i++) 
        {
            tree.expandRow(i);
        }
        
        for (int i = tree.getRowCount() - 1; i >= 1; i--)
        {
            if (tree.getPathForRow(i).getPathCount() > 3)
            {
                tree.collapseRow(i);       
            }
        }
        
        navTreeMgr = new NavigationTreeMgr(tree);
        
        // create object that will control the creation of popups
        // constructor will take care of hooking up right listeners to the tree.
        new NavigationTreeContextMenuMgr(navTreeMgr);
        
        IconManager.IconSize iconSize = IconManager.IconSize.Std20;
        ImageIcon sysIcon = IconManager.getIcon("SystemSetup", iconSize);
        JLabel label = UIHelper.createLabel("XXXX");
        label.setIcon(sysIcon);
        label.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        
        tree.setRowHeight(label.getPreferredSize().height);
        
        // Why doesn't this work?
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                tree.expandRow(1);
            }
        });

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
    
    /**
     * @param session
     * @param instNode
     * @param institution
     */
    private void addDivisionsRecursively(final DataProviderSessionIFace session, final DefaultMutableTreeNode instNode, final Institution institution)
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

    /**
     * @param session
     * @param divNode
     * @param division
     */
    @SuppressWarnings("unused")  // will be used eventually when divisions can have more than one discipline 
    private void addDisciplinesRecursively(final DataProviderSessionIFace session, final DefaultMutableTreeNode divNode, final Division division)
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

    /**
     * @param session
     * @param discNode
     * @param discipline
     */
    private void addCollectionsRecursively(final DataProviderSessionIFace session, final DefaultMutableTreeNode discNode, final Discipline discipline)
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

    /**
     * @param session
     * @param node
     * @param scope
     */
    private void addGroup(@SuppressWarnings("unused")DataProviderSessionIFace session, 
                          final DefaultMutableTreeNode node, 
                          final UserGroupScope scope)
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
                user.getSpPrincipals().size();
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
        //JList userList = createUserList();
        
        JPanel addDeleteNavToolbarPanel = createAddDeleteNavToolbarPanel();
        
        String helpStr = "<html>To add an existing user to a group, just " +
                "right-click on the group and choose 'add existing user' " +
                "and select the user to add.</html>"; // I18N
        JLabel userDnDHelp = UIHelper.createLabel(helpStr);
        
        // adding the tree as f:p:g makes it grow too large
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout("min(210px;p):g", 
                                                                    "min(500px;p),p,15px,p,p,p")/*, new FormDebugPanel()*/);
        final CellConstraints cc = new CellConstraints();

        final PanelBuilder tbRightPB = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        tbRightPB.add(addDeleteNavToolbarPanel, cc.xy(2, 1));
        
        JScrollPane sp = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPB.add(sp,                        cc.xy(1, 1));
        mainPB.add(tbRightPB.getPanel(),      cc.xy(1, 2));
//        mainPB.addSeparator("Users",          cc.xy(1, 4)); // I18N
//        
//        sp = new JScrollPane(userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        mainPB.add(sp,                        cc.xy(1, 5));
        mainPB.add(userDnDHelp,               cc.xy(1, 6));

        return mainPB.getPanel();
    }
    
    //-------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------
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
    
    //-----------------------------------------------------------
    //-- 
    //-----------------------------------------------------------
    private class Filter
    {
        private String patternStr;
        private Pattern p;
        private Matcher m;
        

        /**
         * @param patternStr
         */
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
    
    /**
     * @return
     */
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
    
    /**
     * @param objWrapper
     * @param secondObjWrapper
     * @param selectedObjTitle
     */
    private void showInfoPanel(final DataModelObjBaseWrapper objWrapperArg, 
                               final DataModelObjBaseWrapper secondObjWrapperArg,
                               final String selectedObjTitle)
    {
        String className = objWrapperArg.getType();
        
        if (currentEditorPanel != null && currentEditorPanel.hasChanged())
        {
            String[] optionLabels = new String[] {getResourceString("SaveChangesBtn"), 
                                                  getResourceString("DiscardChangesBtn"), 
                                                  getResourceString("CANCEL")};
            int rv = JOptionPane.showOptionDialog(null,
                        UIRegistry.getLocalizedMessage("SaveChanges", currentTitle),
                        getResourceString("SaveChangesTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        optionLabels,
                        optionLabels[0]);
        
            if (rv == JOptionPane.YES_OPTION)
            {
                doSave(true);
            }
        }
        
        currentTitle = selectedObjTitle;
        
        // show info panel that corresponds to the type of object selected
        CardLayout               cardLayout   = (CardLayout)(infoCards.getLayout());
        AdminInfoSubPanelWrapper panelWrapper = infoSubPanels.get(className);
        
        currentEditorPanel  = editorPanels.get(className);
        if (currentEditorPanel != null)
        {
            currentEditorPanel.setHasChanged(false);
        }
        
        // fill form with object data
        if (panelWrapper != null)
        {
            currentDisplayPanel = panelWrapper;
            if (currentDisplayPanel.setData(objWrapperArg.getDataObj(), secondObjWrapperArg != null ? secondObjWrapperArg.getDataObj() : null) && 
                currentEditorPanel != null)
            {
                currentEditorPanel.setHasChanged(true);
            }
            cardLayout.show(infoCards, className);
        }
        
        objWrapper       = objWrapperArg;
        secondObjWrapper = secondObjWrapperArg;
    }
    
    /**
     * Creates one panel for each kind of form that may be used and stores them for later
     */
    private void createInitialInfoSubPanels()
    {
        boolean editing = hasPermissionToModify;

        infoSubPanels = new Hashtable<String, AdminInfoSubPanelWrapper>();
        
        createInfoSubPanel("SystemSetup", "Institution", "Institution", Institution.class, "institutionId", editing, formOptions);
        createInfoSubPanel("SystemSetup", "DisciplineWithoutCollections", "Discipline", Discipline.class, "disciplineId", editing, formOptions);
        createInfoSubPanel("SystemSetup", "Collection", "Collection", Collection.class, "collectionId", editing, formOptions);
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
    
    /**
     * 
     */
    private void createUserPanel()
    {
        final EditorPanel     infoPanel = new EditorPanel(this);
        final CellConstraints cc        = new CellConstraints();

        PermissionEditor prefsEdt = new PermissionEditor("Preferences",  new PrefsPermissionEnumerator(), infoPanel,
                                                         false, "SEC_NAME_TITLE", "SEC_ENABLE_PREF", null, null, null);
        
        PermissionPanelEditor generalEditor = new PermissionPanelEditor();
        generalEditor.addPanel(new PermissionEditor("Data Objects", new DataObjPermissionEnumerator(), infoPanel));
        generalEditor.addPanel(new IndvPanelPermEditor("Tasks",     new TaskPermissionEnumerator(),    infoPanel));
        generalEditor.addPanel(prefsEdt);
        
        PermissionPanelEditor objEditor = new PermissionPanelEditor();
        objEditor.addPanel(new IndvPanelPermEditor("Data Objects", new ObjectPermissionEnumerator(), infoPanel));
        
        // create user form
        ViewBasedDisplayPanel panel = createViewBasedDisplayPanelForUser(infoPanel);
        
        // create tabbed panel for different kinds of permission editing tables
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", generalEditor); // I18N
        tabbedPane.addTab("Objects", objEditor);  // I18N
        
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout("f:p:g", "t:p,4px,p,5px,f:p:g,2dlu,p"), infoPanel);
        
        // lay out controls on panel
        int y = 1;
        mainPB.add(panel,                  cc.xy(1, y)); y += 2;
        mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2; // I18N
        mainPB.add(tabbedPane,             cc.xy(1, y)); y += 2;

        PanelBuilder saveBtnPB = new PanelBuilder(new FormLayout("f:p:g,p,2px,p", "p")/*, new FormDebugPanel()*/);
        
        Viewable viewable = panel.getMultiView().getCurrentView();
        JButton  valBtn   = FormViewObj.createValidationIndicator(viewable.getUIComponent(), viewable.getValidator());
        panel.getMultiView().getCurrentValidator().setValidationBtn(valBtn);
        saveBtnPB.add(valBtn, cc.xy(2, 1)); 
        saveBtnPB.add(infoPanel.getSaveBtn(), cc.xy(4, 1));

        mainPB.add(saveBtnPB.getPanel(), cc.xy(1, y)); y += 2;
        
        String className = SpecifyUser.class.getCanonicalName();
        infoCards.add(infoPanel, className);
        
        AdminInfoSubPanelWrapper subPanel = new AdminInfoSubPanelWrapper(panel);
        subPanel.addPermissionEditor(generalEditor);
        subPanel.addPermissionEditor(objEditor);
        infoSubPanels.put(className, subPanel);
        editorPanels.put(className, infoPanel);
    }
    
    /**
     * 
     */
    private void createGroupPanel()
    {
        final EditorPanel     infoPanel = new EditorPanel(this);
        final CellConstraints cc       = new CellConstraints();

        PermissionPanelEditor generalEditor = new PermissionPanelEditor();
        generalEditor.addPanel(new PermissionEditor("Data Objects", new DataObjPermissionEnumerator(), infoPanel));
        generalEditor.addPanel(new IndvPanelPermEditor("Tasks",     new TaskPermissionEnumerator(),    infoPanel));
        generalEditor.addPanel(new PermissionEditor("Preferences",  new PrefsPermissionEnumerator(),   infoPanel));
        
        // create user form
        ViewBasedDisplayPanel panel = createViewBasedDisplayPanelForGroup(infoPanel);
        
        final PanelBuilder mainPB = new PanelBuilder(new FormLayout("f:p:g", "t:p,4px,p,5px,f:p:g,2dlu,p"), infoPanel);
        
        // lay out controls on panel
        int y = 1;
        mainPB.add(panel,                  cc.xy(1, y)); y += 2;
        mainPB.addSeparator("Permissions", cc.xy(1, y)); y += 2; // I18N
        mainPB.add(generalEditor,          cc.xy(1, y)); y += 2;

        PanelBuilder saveBtnPB = new PanelBuilder(new FormLayout("f:p:g,p,2px,p", "p"));
        
        Viewable viewable = panel.getMultiView().getCurrentView();
        JButton  valBtn   = FormViewObj.createValidationIndicator(viewable.getUIComponent(), viewable.getValidator());
        panel.getMultiView().getCurrentValidator().setValidationBtn(valBtn);
        saveBtnPB.add(valBtn, cc.xy(2, 1)); 
        saveBtnPB.add(infoPanel.getSaveBtn(), cc.xy(4, 1));

        mainPB.add(saveBtnPB.getPanel(), cc.xy(1, y)); y += 2;
        
        String className = SpPrincipal.class.getCanonicalName();
        infoCards.add(infoPanel, className);
        
        AdminInfoSubPanelWrapper subPanel = new AdminInfoSubPanelWrapper(panel);
        subPanel.addPermissionEditor(generalEditor);
        infoSubPanels.put(className, subPanel);
        editorPanels.put(className, infoPanel);
    }

    /**
     * @param refreshObj
     */
    protected void doSave(final boolean refreshObj)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            
            // then save permissions
            currentDisplayPanel.savePermissionData(session);
            currentEditorPanel.setHasChanged(false);
            
            session.commit();
            
           if (refreshObj)
           {
               refreshTreeNode();
           }
           
        } catch (Exception ex)
        {
            ex.printStackTrace();
            session.rollback();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
            
    }
    
    /**
     * 
     */
    protected void refreshTreeNode()
    {
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            FormDataObjIFace dbObj        = objWrapper.getDataObj();
            FormDataObjIFace refreshedObj = (FormDataObjIFace)session.get(dbObj.getDataClass(), dbObj.getId());
            objWrapper.setDataObj(refreshedObj);
            
            if (secondObjWrapper != null)
            {
                dbObj = secondObjWrapper.getDataObj();
                if (dbObj != null)
                {
                    refreshedObj = (FormDataObjIFace)session.get(dbObj.getDataClass(), dbObj.getId());
                    secondObjWrapper.setDataObj(refreshedObj);
                }
            }
            
            if (currentDisplayPanel.setData(objWrapper.getDataObj(), 
                                        secondObjWrapper != null ? secondObjWrapper.getDataObj() : null) && 
                currentEditorPanel != null)
            {
                currentEditorPanel.setHasChanged(true);
            }
        } 
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param edtPanel
     * @return
     */
    protected static ViewBasedDisplayPanel createViewBasedDisplayPanelForUser(final EditorPanel edtPanel)
    {
        ViewBasedDisplayPanel vbp = new ViewBasedDisplayPanel(null, "SystemSetup", 
                                                             "User", 
                                                             "User", 
                                                             SpecifyUser.class.getCanonicalName(), 
                                                             "specifyUserId", 
                                                             true, 
                                                             MultiView.HIDE_SAVE_BTN);
        vbp.getMultiView().getCurrentValidator().addDataChangeListener(edtPanel);
        edtPanel.setFormValidator(vbp.getMultiView().getCurrentValidator());
        return vbp;
    }
    
    /**
     * @param edtPanel
     * @return
     */
    protected static ViewBasedDisplayPanel createViewBasedDisplayPanelForGroup(final EditorPanel edtPanel)
    {
        ViewBasedDisplayPanel vbp = new ViewBasedDisplayPanel(null, 
                                                            "SystemSetup", 
                                                            "UserGroup", 
                                                            "User Group", 
                                                            SpPrincipal.class.getCanonicalName(), 
                                                            "spUserGroupId", 
                                                            true, 
                                                            MultiView.HIDE_SAVE_BTN);
        vbp.getMultiView().getCurrentValidator().addDataChangeListener(edtPanel);
        edtPanel.setFormValidator(vbp.getMultiView().getCurrentValidator());
        return vbp;
    }
    
    /**
     * @param objWrapperArg
     */
    private void updateUIEnabled(final DataModelObjBaseWrapper objWrapperArg)
    {
        boolean isInstitution = (objWrapperArg != null)? objWrapperArg.isInstitution() : false;
        boolean isDiscipline  = (objWrapperArg != null)? objWrapperArg.isDiscipline()  : false;
        boolean isCollection  = (objWrapperArg != null)? objWrapperArg.isCollection()  : false;
        boolean isGroup       = (objWrapperArg != null)? objWrapperArg.isGroup()       : false;
        
        delBtn.setEnabled(
                hasPermissionToDelete && 
                objWrapperArg != null && 
                !isInstitution &&
                !isCollection );
        
        addDiscBtn.setEnabled(
                hasPermissionToAdd &&
                objWrapperArg != null && 
                isInstitution );
        
        addCollBtn.setEnabled(
                hasPermissionToAdd &&
                objWrapperArg != null && 
                isDiscipline);
        
        addGrpBtn. setEnabled(
                hasPermissionToAdd &&
                objWrapperArg != null && 
                (isInstitution || isDiscipline || isCollection));

        addUserBtn.setEnabled(
                hasPermissionToAdd &&
                objWrapperArg != null && 
                isGroup);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
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
                doSave(false);
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

    private class MyTreeCellRenderer extends DefaultTreeCellRenderer
    {
        public MyTreeCellRenderer()
        {
        }
        
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
            Object                 obj  = node.getUserObject();
            if (obj instanceof DataModelObjBaseWrapper)
            {
                DataModelObjBaseWrapper wrp = (DataModelObjBaseWrapper) obj;
                String text = obj.toString();
                setText(text);
                setToolTipText(text);
                setIcon(wrp.getIcon());
            }
            return this;
        }
    }
}
