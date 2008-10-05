package edu.ku.brc.specify.tasks.subpane.security;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroupScope;

/**
 * This class perform operations on the security administration navigation tree, such as 
 * the creation or deletion of an item on the tree. An items is one instance of Discipline,
 * Collection, SpPrincipal (user group) or SpecifyUser. 
 * 
 * @author Ricardo
 *
 */
public class NavigationTreeMgr
{
    private static final Logger log = Logger.getLogger(NavigationTreeMgr.class);

    private JTree tree;
    
    NavigationTreeMgr(JTree tree) {
        this.tree = tree;
    }
    
    public final JTree getTree() {
        return tree;
    }
    
    public void addNewUser(DefaultMutableTreeNode grpNode) {
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
        addGroupToUser(group, user);
        
        DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(user);
        DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
        
        tree.setSelectionPath(new TreePath(userNode.getPath()));
    }
    
    public void addExistingUser(DefaultMutableTreeNode grpNode, final SpecifyUser[] userArray) {
        if (userArray.length == 0 || grpNode == null || 
                !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
            // Nothing is selected or object type isn't relevant    
            return;

        DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (grpNode.getUserObject());
        if (!parentWrp.isGroup())
            // selection isn't a suitable parent for a group
            return;
        
        SpPrincipal group = (SpPrincipal) parentWrp.getDataObj();
        addGroupToUser(group, userArray);
        
        DefaultMutableTreeNode lastUserNode = addUsersToTree(grpNode, userArray);
        
        tree.setSelectionPath(new TreePath(lastUserNode.getPath()));
    }
    
    private DefaultMutableTreeNode addUsersToTree(
            DefaultMutableTreeNode grpNode, final SpecifyUser[] userArray)
    {
        DefaultMutableTreeNode lastUserNode = null;
        for (SpecifyUser user : userArray) 
        {
            DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(user);
            DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);

            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
            
            lastUserNode = userNode;
        }
        return lastUserNode;
    }
    
    public void addNewGroup(DefaultMutableTreeNode parentNode) {
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
        save(group);
        
        DataModelObjBaseWrapper grpWrp  = new DataModelObjBaseWrapper(group);
        DefaultMutableTreeNode  grpNode = new DefaultMutableTreeNode(grpWrp);
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(grpNode, parentNode, parentNode.getChildCount());
        
        tree.setSelectionPath(new TreePath(grpNode.getPath()));
    }
    
    public void addNewCollection(DefaultMutableTreeNode discNode) {
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
        save(collection);
        
        DataModelObjBaseWrapper collWrp  = new DataModelObjBaseWrapper(collection);
        DefaultMutableTreeNode  collNode = new DefaultMutableTreeNode(collWrp);
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(collNode, discNode, discNode.getChildCount());
        
        tree.setSelectionPath(new TreePath(collNode.getPath()));
    }
    
    public void addNewDiscipline(DefaultMutableTreeNode instNode) {
        if (instNode == null || !(instNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            // Nothing is selected or object type isn't relevant    
            return;
        }

        DataModelObjBaseWrapper instWrp  = (DataModelObjBaseWrapper) (instNode.getUserObject());
        if (!instWrp.isInstitution())
        {
            // selection isn't an institution
            return;
        }
        
        Institution institution = (Institution) instWrp.getDataObj();
        Division    division    = new Division();
        Discipline  discipline  = new Discipline();
        
        division.initialize();
        discipline.initialize();
        
        division.setInstitution(institution);
        discipline.setDivision(division);
        
        division.setName("Anonymous Division");
        discipline.setName("New Discipline");
        
        save(new Object[] { division, discipline });
        
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
    
    public void deleteItem(DefaultMutableTreeNode node) {
        
    }
    
    private final void save(Object object) {
        save(new Object[] {object});
    }
    
    private final void save(Object[] objectArray) 
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (Object object : objectArray)
            {
                session.attach(object);
                session.saveOrUpdate(object);
            }
            session.commit();
        }
        catch (final Exception e1)
        {
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
        } 
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    private final void addGroupToUser(SpPrincipal group, SpecifyUser user) {
        addGroupToUser(group, new SpecifyUser[] { user });
    }
    
    private final void addGroupToUser(SpPrincipal group, SpecifyUser[] users) {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (SpecifyUser user : users) 
            {
                session.attach(user);
                session.attach(group);
                if (user.getSpPrincipals() == null) {
                    user.setSpPrincipals(new HashSet<SpPrincipal>());
                }
                user.getSpPrincipals().add(group);
                session.saveOrUpdate(user);
            }
            session.commit();
        }
        catch (final Exception e1)
        {
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
        } 
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
}
