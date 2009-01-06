package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.ESTermParser;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.core.expresssearch.SearchTermField;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroupScope;
import edu.ku.brc.specify.datamodel.busrules.SpecifyUserBusRules;
import edu.ku.brc.specify.datamodel.busrules.TableSearchResults;

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
    
    /**
     * @param tree
     */
    NavigationTreeMgr(final JTree tree)
    {
        this.tree = tree;
    }

    /**
     * @return
     */
    public final JTree getTree()
    {
        return tree;
    }

    /**
     * Indicates whether we can remove this user from group.
     * We cannot remove a user from a group if it's the only group the user belongs to. In this
     * case, we only offer to delete the user. If we do let the admin remove the user from the 
     * last group he belongs to, the user will disapear from all groups and so the admin won't be
     * able to get to the user acount again.
     * 
     * @param userNode
     * @return
     */
    public boolean canRemoveUserFromGroup(final DefaultMutableTreeNode userNode)
    {
        DataProviderSessionIFace session = null;
        boolean result = false;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
            Object object = wrapper.getDataObj();
            SpecifyUser user  = (SpecifyUser) object;
            int count = 0;
            for (SpPrincipal principal : user.getSpPrincipals())
            {
                if (GroupPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()) ||
                        AdminPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()))
                {
                    ++count;
                }
            }
            result = count > 1; 
        } 
        catch (final Exception e1)
        {
            session.rollback();
            
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        return result;
    }
    
    public void removeUserFromGroup(final DefaultMutableTreeNode userNode)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object object = wrapper.getDataObj();
        
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) userNode.getParent();
        DataModelObjBaseWrapper parentWrapper = (DataModelObjBaseWrapper) parent.getUserObject();
        Object parentObject = parentWrapper.getDataObj();

        if (!(object instanceof SpecifyUser) || 
                !(parentObject instanceof SpPrincipal) || 
                !canRemoveUserFromGroup(userNode))
        {
            // not a user, so bail out
            return;
        }

        SpecifyUser user  = (SpecifyUser) object;
        SpPrincipal group = (SpPrincipal) parentObject;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            user.getSpPrincipals().remove(group);
            group.getSpecifyUsers().remove(user);
            session.commit();
            
            // remove child from tree
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.removeNodeFromParent(userNode);
        } 
        catch (final Exception e1)
        {
            session.rollback();
            
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    public boolean canDeleteUser(final DefaultMutableTreeNode userNode)
    {
        // for now, we can always delete a user
        return true;
    }
    
    /**
     * Deletes a user from the database and from the navigation tree.
     * @param userNode
     */
    public void deleteUser(final DefaultMutableTreeNode userNode)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object object = wrapper.getDataObj();
        if (!(object instanceof SpecifyUser) || !canDeleteUser(userNode))
        {
            // for some reason, we cannot delete this user
            return;
        }

        SpecifyUser user = (SpecifyUser) object;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            // break the association between the user and all its agents, 
            // so the user can be later deleted
            user.getAgents().clear();
            // remove user from groups
            user.getSpPrincipals().clear();
            session.saveOrUpdate(user);
            user.setModifiedByAgent(null);
            session.delete(user);
            session.commit();
            
            // remove user from the group in the tree
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.removeNodeFromParent(userNode);
            
        } catch (final Exception e1)
        {
            session.rollback();
            
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    /**
     * Indicates whether we can delete this navigation tree item or not.
     * We can only delete an item if it doesn't have any children
     * @param node
     * @return
     */
    public boolean canDeleteItem(final DefaultMutableTreeNode node)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
        Object object = wrapper.getDataObj();

        if (!(object instanceof SpPrincipal) && 
            !(object instanceof Collection) &&
            !(object instanceof Discipline))
        {
            // cannot delete object that is not an instance of one the above types
            return false;
        }

        // only childless nodes can be deleted
        return node.getChildCount() == 0;
    }

    /**
     * Delete an item in the navigation tree. The item can be any instance of ...
     * @param node
     */
    public void deleteItem(DefaultMutableTreeNode node) 
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
        Object object = wrapper.getDataObj();
        
        if (!canDeleteItem(node))
        {
            // for some reason, we cannot delete this item
            // object type was already checked in the last call above 
            return;
        }

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            //session.attach(object);
            session.delete(object);
            session.commit();
            
            // remove user from the group in the tree
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.removeNodeFromParent(node);
            
        } catch (final Exception e1)
        {
            session.rollback();
            
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    

    /**
     * @param grpNode
     */
    public void addNewUser(final DefaultMutableTreeNode grpNode) 
    {
        if (grpNode == null || !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return; // Nothing is selected or object type isn't relevant 
        }
        
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)grpNode.getParent();
        
        Discipline disp = null;
        
        while (parent != null)
        {
            if (parent.getUserObject() instanceof DataModelObjBaseWrapper)
            {
                DataModelObjBaseWrapper wrp = (DataModelObjBaseWrapper)parent.getUserObject();
                System.out.println(wrp.getDataObj()+"  "+wrp.getDataObj());
                
                FormDataObjIFace obj = wrp.getDataObj();
                
                if (obj instanceof Discipline)
                {
                    disp = (Discipline)obj;
                }
            }
            parent = (DefaultMutableTreeNode)parent.getParent();
        }
        
        if (disp == null)
        {
            // error
            return;
        }
        
        final Division   division   = disp.getDivision();
        final Discipline discipline = disp;
        
        DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (grpNode.getUserObject());
        if (!parentWrp.isGroup())
        {
            return; // selection isn't a suitable parent for a group
        }
        
        SpPrincipal group = (SpPrincipal) parentWrp.getDataObj();
        SpecifyUser spUser = new SpecifyUser();
        spUser.initialize();
        spUser.setUserType(group.getGroupType());
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)getMostRecentWindow(),
                                                                null,
                                                                "User",
                                                                null,
                                                                DBTableIdMgr.getInstance().getTitleForId(SpecifyUser.getClassTableId()),
                                                                null,
                                                                spUser.getClass().getName(),
                                                                "specifyUserId",
                                                                true,
                                                                MultiView.HIDE_SAVE_BTN | 
                                                                   MultiView.DONT_ADD_ALL_ALTVIEWS | 
                                                                   MultiView.USE_ONLY_CREATION_MODE |
                                                                   MultiView.IS_NEW_OBJECT);
        dlg.setOkLabel(getResourceString("SAVE"));
        dlg.createUI();
        final ValComboBoxFromQuery cbx = (ValComboBoxFromQuery)dlg.getMultiView().getCurrentViewAsFormViewObj().getControlByName("agent");
        
        cbx.registerQueryBuilder(new ViewBasedSearchQueryBuilderIFace() {
            protected ExpressResultsTableInfo esTblInfo = null;
            
            @Override
            public String buildSQL(final Map<String, Object> dataMap, final List<String> fieldNames)
            {
                String searchName = cbx.getSearchName();
                if (searchName != null)
                {
                    esTblInfo = ExpressSearchConfigCache.getTableInfoByName(searchName);
                    if (esTblInfo != null)
                    {
                       String sqlStr = esTblInfo.getViewSql();
                       return buildSearchString(dataMap, fieldNames, StringUtils.replace(sqlStr, "DSPLNID", discipline.getId().toString()));
                    }
                }
                return null;
            }
            @Override
            public String buildSQL(String searchText, boolean isForCount)
            {
                String newEntryStr = searchText + '%';
                String sqlTemplate = "SELECT %s1 FROM Agent a LEFT JOIN a.specifyUser s INNER JOIN a.division d WHERE d.id = "+division.getId()+" AND s = null AND LOWER(a.lastName) LIKE '%s2' ORDER BY a.lastName";
                String sql = StringUtils.replace(sqlTemplate, "%s1", isForCount ? "count(*)" : "a.lastName, a.firstName, a.agentId"); //$NON-NLS-1$
                sql = StringUtils.replace(sql, "%s2", newEntryStr); //$NON-NLS-1$
                log.debug(sql);
                return sql;
            }
            @Override
            public QueryForIdResultsIFace createQueryForIdResults()
            {
                return new TableSearchResults(DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId()), esTblInfo.getCaptionInfo()); //true => is HQL
            }
            
        });
        
        Discipline currDiscipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        AppContextMgr.getInstance().setClassObject(Discipline.class, disp);
        
        // This is just an extra safety measure to make sure the current Disicpline gets set back
        try
        {
            dlg.setData(spUser);
            dlg.setVisible(true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            AppContextMgr.getInstance().setClassObject(Discipline.class, currDiscipline);    
        }
        
        if (!dlg.isCancelled())
        {
            addGroupToUser(group, spUser, currDiscipline); // XXX !!! I don't think this Discipline is the right one!
            
            Agent userAgent = (Agent)cbx.getValue();
            spUser.getAgents().add(userAgent);
            userAgent.setSpecifyUser(spUser);
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                session.beginTransaction();
                
                SpecifyUserBusRules busRules = new SpecifyUserBusRules();
                busRules.initialize(dlg.getMultiView().getCurrentView());
                busRules.beforeMerge(spUser, session);
                busRules.beforeSave(spUser, session);
                
                session.saveOrUpdate(spUser);
                session.saveOrUpdate(userAgent);
                
                session.commit();
                
                spUser = session.get(SpecifyUser.class, spUser.getId());
                
            } catch (final Exception e1)
            {
                session.rollback();
                
                e1.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(spUser);
            DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);
            
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
            
            tree.setSelectionPath(new TreePath(userNode.getPath()));
        }
    }
    
    /**
     * @param dataMap
     * @param fieldNames
     * @param sqlTemplate
     * @return
     */
    protected String buildSearchString(final Map<String, Object> dataMap, 
                                       final List<String>        fieldNames,
                                       final String              sqlTemplate)
    {
        StringBuilder orderBy  = new StringBuilder();
        StringBuilder criteria = new StringBuilder("agent.SpecifyUserID IS NULL AND (");
        int criCnt = 0;
        for (String colName : dataMap.keySet())
        {
            String data = (String)dataMap.get(colName);
            if (ESTermParser.parse(data.toLowerCase(), true))
            {
                if (StringUtils.isNotEmpty(data))
                {
                    List<SearchTermField> fields     = ESTermParser.getFields();
                    SearchTermField       firstTerm  = fields.get(0);
                    
                    if (criCnt > 0) criteria.append(" OR ");
                    
                    String clause = ESTermParser.createWhereClause(firstTerm, null, colName);
                    criteria.append(clause);
                    
                    if (criCnt > 0) orderBy.append(',');
                    
                    orderBy.append(colName);
                    
                    criCnt++;
                }
            }
        }
        
        criteria.append(")");
        
        StringBuffer sb = new StringBuffer();
        sb.append(criteria);
        sb.append(" ORDER BY ");
        sb.append(orderBy);
        
        String sqlStr = StringUtils.replace(sqlTemplate, "(%s)", sb.toString());
        
        log.debug(sqlStr);
        
        return sqlStr;
    }
    
    /**
     * @param grpNode
     * @param userArray
     */
    public void addExistingUser(final DefaultMutableTreeNode grpNode, 
                                final SpecifyUser[] userArray) 
    {
        if (userArray.length == 0 || grpNode == null || 
                !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return; // Nothing is selected or object type isn't relevant 
        }

        DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (grpNode.getUserObject());
        if (!parentWrp.isGroup())
        {
            return; // selection isn't a suitable parent for a group
        }
        
        SpPrincipal group = (SpPrincipal) parentWrp.getDataObj();
        
        Discipline discipline = getParentOfClass(grpNode, Discipline.class);
        
        addGroupToUser(group, userArray, discipline);
        
        DefaultMutableTreeNode lastUserNode = addUsersToTree(grpNode, userArray);
        
        tree.setSelectionPath(new TreePath(lastUserNode.getPath()));
    }
    
    /**
     * @param grpNode
     * @param userArray
     * @return
     */
    private DefaultMutableTreeNode addUsersToTree(final  DefaultMutableTreeNode grpNode, 
                                                  final SpecifyUser[] userArray)
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
    
    @SuppressWarnings("unchecked")
    private <T> T getParentOfClass(final  DefaultMutableTreeNode node, final Class<?> cls)
    {
        DefaultMutableTreeNode parent = node;
        while (parent != null)
        {
            DataModelObjBaseWrapper userData = (DataModelObjBaseWrapper)parent.getUserObject();
            if (userData.getDataObj().getClass() == cls)
            {
                return (T)userData.getDataObj();
            }
            parent = (DefaultMutableTreeNode)parent.getParent();
        }
        return null;
    }
    
    /**
     * @param parentNode
     */
    public void addNewGroup(final DefaultMutableTreeNode parentNode) 
    {
        if (parentNode == null || !(parentNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return; // Nothing is selected or object type isn't relevant    
        }

        DataModelObjBaseWrapper parentWrp  = (DataModelObjBaseWrapper) (parentNode.getUserObject());
        if (!parentWrp.isInstitution() && !parentWrp.isDiscipline() && !parentWrp.isCollection())
        {
            return; // selection isn't a suitable parent for a group
        }
        
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
    
    /**
     * @param discNode
     */
    public void addNewCollection(final DefaultMutableTreeNode discNode) 
    {
        if (discNode == null || !(discNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return;// Nothing is selected or object type isn't relevant    
        }

        DataModelObjBaseWrapper discWrp  = (DataModelObjBaseWrapper) (discNode.getUserObject());
        if (!discWrp.isDiscipline())
        {
            return; // selection isn't a discipline
        }
        
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
    
    /**
     * @param instNode
     */
    public void addNewDiscipline(final DefaultMutableTreeNode instNode) 
    {
        if (instNode == null || !(instNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return;// Nothing is selected or object type isn't relevant    
        }

        DataModelObjBaseWrapper instWrp  = (DataModelObjBaseWrapper) (instNode.getUserObject());
        if (!instWrp.isInstitution())
        {
            return; // selection isn't an institution
        }
        
        Institution institution = (Institution) instWrp.getDataObj();
        Division    division    = new Division();
        Discipline  discipline  = new Discipline();
        
        division.initialize();
        discipline.initialize();
        
        division.setInstitution(institution);
        discipline.setDivision(division);
        
        division.setName("Anonymous Division");
        discipline.setType("New Discipline");
        
        save(new Object[] { division, discipline }, false);
        
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
     * @param object
     */
    private final void save(final Object object) 
    {
        save(new Object[] {object}, false);
    }
    
    /**
     * @param objectArray
     */
    private final void save(final Object[] objectArray, final boolean doMerge) 
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (Object object : objectArray)
            {
                if (doMerge)
                {
                    object = session.merge(object);
                } else
                {
                    session.attach(object);
                }
                session.saveOrUpdate(object);
            }
            session.commit();
            
        } catch (final Exception e1)
        {
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * @param group
     * @param user
     */
    private final void addGroupToUser(final SpPrincipal group, 
                                      final SpecifyUser user,
                                      final Discipline  discipline)
    {
        addGroupToUser(group, new SpecifyUser[] { user }, discipline);
    }
    
    /**
     * @param group
     * @param users
     */
    private final void addGroupToUser(final SpPrincipal   group, 
                                      final SpecifyUser[] users,
                                      final Discipline    discipline)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (SpecifyUser user : users) 
            {
                if (user.getId() != null)
                {
                    session.attach(user);
                }
                
                session.attach(group);
                if (user.getSpPrincipals() == null)
                {
                    user.setSpPrincipals(new HashSet<SpPrincipal>());
                }
                
                SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(user);
                user.addUserToSpPrincipalGroup(userPrincipal);

                user.getSpPrincipals().add(group);
                group.getSpecifyUsers().add(user);
                
                Agent agentToClone = null;
                for (Agent agent : user.getAgents())
                {
                    session.saveOrUpdate(agent);
                    agentToClone = agent;
                }
                session.attach(discipline);
                
                Agent newAgent = (Agent)agentToClone.clone();
                newAgent.getDisciplines().add(discipline);
                discipline.getAgents().add(newAgent);
                user.getAgents().add(newAgent);
                
                session.saveOrUpdate(agentToClone);
                session.saveOrUpdate(discipline);
                
                session.saveOrUpdate(user);
                session.saveOrUpdate(userPrincipal);
                session.saveOrUpdate(group);
            }
            session.commit();
            
        } catch (final Exception e1)
        {
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
}
