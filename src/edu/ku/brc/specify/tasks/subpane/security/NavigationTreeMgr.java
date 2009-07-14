/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.SearchTermField;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
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

    private JTree            tree;
    private Set<SpecifyUser> spUsers;
    
    /**
     * @param tree
     * @param spUsers
     */
    NavigationTreeMgr(final JTree tree, final Set<SpecifyUser> spUsers)
    {
        this.tree    = tree;
        this.spUsers = spUsers;
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
            Object                  object  = wrapper.getDataObj();
            SpecifyUser             user    = (SpecifyUser) object;
            
            int cnt = user.getUserGroupCount();
            if (cnt > 1)
            {
                result = cnt > 2 || !user.isInAdminGroup();
                
            } else
            {
                result = true;
            }
            
        } catch (final Exception e1)
        {
            e1.printStackTrace();
            
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
            session.rollback();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        return result;
    }
    
    /**
     * @param userNode
     */
    public void removeUserFromGroup(final DefaultMutableTreeNode userNode)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object                  object  = wrapper.getDataObj();
        
        DefaultMutableTreeNode  parent        = (DefaultMutableTreeNode) userNode.getParent();
        DataModelObjBaseWrapper parentWrapper = (DataModelObjBaseWrapper) parent.getUserObject();
        Object                  parentObject  = parentWrapper.getDataObj();

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
            
            for (SpPrincipal p : new Vector<SpPrincipal>(user.getSpPrincipals()))
            {
                if (p.getId().equals(group.getId()))
                {
                    user.getSpPrincipals().remove(p);        
                }
            }
            
            session.saveOrUpdate(user);
            session.commit();
            
            // remove child from tree
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.removeNodeFromParent(userNode);
            tree.clearSelection();
            
        } catch (final Exception e1)
        {
            e1.printStackTrace();
            
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
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
     * @param userNode
     * @return
     */
    public boolean canDeleteUser(final DefaultMutableTreeNode userNode)
    {
        // get the user who's logged in
        final SpecifyUser currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);

        // get the user from the selected tree node
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object                  object  = wrapper.getDataObj();
        SpecifyUser             user    = (SpecifyUser)object;

        if (currentUser.getSpecifyUserId().equals(user.getSpecifyUserId()))
        {
            // no one can delete the user who's logged in.
            return false;
        }
        
        DataProviderSessionIFace session = null;
        boolean result = false;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            user = (SpecifyUser)session.getData("FROM SpecifyUser WHERE id = "+user.getId());
            wrapper.setDataObj(user);
            
            // XXX do we need a session here? 
            // We need it in the next call to get SpPrincipals, but they have probably been 
            // loaded by then. Notice we don't attach the user to the session anywhere in this code... 

            // We can delete a user if that's the only group it belongs to
            int cnt = user.getUserGroupCount();
            if (cnt > 1)
            {
                result = cnt > 2 || !user.isInAdminGroup();
                
            } else
            {
                result = true;
            }
        } 
        catch (final Exception e1)
        {
            e1.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
            session.rollback();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        return result;
    }
    
    /**
     * Deletes a user from the database and from the navigation tree.
     * @param userNode
     */
    public void deleteUser(final DefaultMutableTreeNode userNode)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object                  object  = wrapper.getDataObj();
        if (!(object instanceof SpecifyUser) || !canDeleteUser(userNode))
        {
            // for some reason, we cannot delete this user
            return;
        }
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            
            SpecifyUser user = session.get(SpecifyUser.class, ((SpecifyUser)object).getId());
            
            // break the association between the user and all its agents, 
            // so the user can be later deleted
            for (Agent agent : user.getAgents())
            {
                agent.setSpecifyUser(null);
            }
            user.getAgents().clear();
            
            // delete related user principal (but leave other principals (admin & regular groups) intact
            for (SpPrincipal principal : user.getSpPrincipals())
            {
                if (UserPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()))
                {
                    // delete user principal: permissions will be deleted together because of 
                    // Hibernate cascade is setup for deleting orphans
                    session.delete(principal);
                }
            }

            // the code below deletes the agent associated with the user and the discipline
            // it should probably be disabled so that the agent, and the link to its data is not lost.
            // at very least, an admin should be asked whether to delete the agent, but that can be added later
            // Discipline discipline = session.get(Discipline.class, getParentDiscipline(userNode).getUserGroupScopeId());
            // deleteUserAgentFromDiscipline(user, discipline, session);

            // remove user from groups
            user.getSpPrincipals().clear();
            user.setModifiedByAgent(null);
            session.delete(user);
            
            session.commit();
            
            // remove user from the group in the tree
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.removeNodeFromParent(userNode);
            tree.clearSelection();

        } catch (final Exception e1)
        {
            e1.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
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
     * @param node
     * @return
     */
    public boolean canAddNewUser(final DefaultMutableTreeNode node)
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper)node.getUserObject();
        Object                  object  = wrapper.getDataObj();

        if (object instanceof SpPrincipal)
        {
            SpPrincipal principal = (SpPrincipal) object;
            return !AdminPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass());
        }
        
        return false;
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
        Object                  object  = wrapper.getDataObj();

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
    public void deleteItem(final DefaultMutableTreeNode node) 
    {
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
        Object                  object  = wrapper.getDataObj();
        
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
            e1.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
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
     * Get discipline to which the group is attached.
     * @param grpNode
     * @return Discipline to which the group is attached. Null if node isn't from a group.
     */
    private Discipline getParentDiscipline(final DefaultMutableTreeNode grpNode)
    {
        Discipline             parentDiscipline = null;
        DefaultMutableTreeNode parent           = (DefaultMutableTreeNode)grpNode.getParent();

        while (parent != null)
        {
            if (parent.getUserObject() instanceof DataModelObjBaseWrapper)
            {
                DataModelObjBaseWrapper wrp = (DataModelObjBaseWrapper)parent.getUserObject();
                
                log.debug(wrp.getDataObj()+"  "+wrp.getDataObj());
                
                FormDataObjIFace obj = wrp.getDataObj();
                
                if (obj instanceof Discipline)
                {
                    parentDiscipline = (Discipline) obj;
                }
            }
            parent = (DefaultMutableTreeNode) parent.getParent();
        }
        
        return parentDiscipline;
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
        
        // discipline to which the user's being added
        Discipline parentDiscipline = getParentDiscipline(grpNode);
       
        final Division   division   = parentDiscipline.getDivision();
        final Discipline discipline = parentDiscipline;
        
        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper)grpNode.getUserObject();
        if (!parentWrp.isGroup())
        {
            return; // selection isn't a suitable parent for a group
        }
        
        SpPrincipal grpPrin = (SpPrincipal)parentWrp.getDataObj();
        
        SpecifyUser spUser  = new SpecifyUser();
        spUser.initialize();
        spUser.setUserType(grpPrin.getGroupType());
        
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
        
        cbx.registerQueryBuilder(new ViewBasedSearchQueryBuilderIFace() 
        {
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
                String sql         = StringUtils.replace(sqlTemplate, "%s1", isForCount ? "count(*)" : "a.lastName, a.firstName, a.agentId"); //$NON-NLS-1$
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
        
        AppContextMgr acMgr = AppContextMgr.getInstance();
        
        Discipline currDiscipline = acMgr.getClassObject(Discipline.class);
        acMgr.setClassObject(Discipline.class, parentDiscipline);
        
        // This is just an extra safety measure to make sure the current Discipline gets set back
        try
        {
            // Has no password here
            dlg.setData(spUser);
            dlg.setVisible(true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, ex);
            
        } finally
        {
            acMgr.setClassObject(Discipline.class, currDiscipline);    
        }
        
        if (!dlg.isCancelled())
        {
            String textPwd    = spUser.getPassword();
            spUser.setPassword(Encryption.encrypt(textPwd, textPwd));
            
            Agent userAgent = (Agent)cbx.getValue();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                session.beginTransaction();
                
                SpecifyUserBusRules busRules = new SpecifyUserBusRules();
                busRules.initialize(dlg.getMultiView().getCurrentView());
                busRules.beforeMerge(spUser, session);
                busRules.beforeSave(spUser, session);

                // persist newly created user and agent
                session.save(spUser);

                // get fresh copies of parentDiscipline and group to make Hibernate happy
                Discipline  localDiscipline = session.get(Discipline.class, parentDiscipline.getUserGroupScopeId());
                SpPrincipal localGroup      = session.get(SpPrincipal.class, grpPrin.getUserGroupId());

                // link user to its group
                spUser.getSpPrincipals().add(localGroup);
                localGroup.getSpecifyUsers().add(spUser);

                // link agent to user
                session.attach(userAgent);
                spUser.getAgents().add(userAgent);
                userAgent.setSpecifyUser(spUser);

                // create a JAAS principal and associate it with the user
                SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(spUser);
                session.save(userPrincipal);
                spUser.addUserToSpPrincipalGroup(userPrincipal);
                
                // link newly create agent to discipline
                userAgent.getDisciplines().add(localDiscipline);
                localDiscipline.getAgents().add(userAgent);
                session.commit();
                
                parentWrp.setDataObj(localGroup);
                
                spUsers.add(spUser);
                spUser = session.get(SpecifyUser.class, spUser.getId());
                
            } catch (final Exception e1)
            {
                e1.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
                session.rollback();
                
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
            if (ESTermParser.getInstance().parse(data.toLowerCase(), true))
            {
                if (StringUtils.isNotEmpty(data))
                {
                    List<SearchTermField> fields     = ESTermParser.getInstance().getFields();
                    SearchTermField       firstTerm  = fields.get(0);
                    
                    if (criCnt > 0) criteria.append(" OR ");
                    
                    String clause = ESTermParser.getInstance().createWhereClause(firstTerm, null, colName);
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
    public void addExistingUser(final DefaultMutableTreeNode grpNode) 
    {
        DataModelObjBaseWrapper wrp   = (DataModelObjBaseWrapper) grpNode.getUserObject();
        SpPrincipal             group = (SpPrincipal) wrp.getDataObj();
        AddExistingUserDlg      dlg   = new AddExistingUserDlg(null, group, spUsers);
        dlg.setVisible(true);
        
        if (dlg.isCancelled())
        {
            return;
        }

        SpecifyUser[] userArray = dlg.getSelectedUsers();
        
        if (userArray.length == 0 || grpNode == null || 
            !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return; // Nothing is selected or object type isn't relevant 
        }

        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper) (grpNode.getUserObject());
        if (!parentWrp.isGroup())
        {
            return; // selection isn't a suitable parent for a group
        }
        
        //Discipline discipline = getParentOfClass(grpNode, Discipline.class);
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();

            // TODO: add user agent to discipline 
            //discipline = session.merge(discipline);
            
            // add users to group
            for (SpecifyUser specifyUser : userArray)
            {
                session.update(specifyUser);
                specifyUser.getSpPrincipals().add(group);

                // add first user agent to discipline (if not already there)
                //Iterator<Agent> it = specifyUser.getAgents().iterator();
                //discipline.getAgents().add(it.next());
            }
            session.commit();
            
        } catch (final Exception e1)
        {
            e1.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        DefaultMutableTreeNode lastUserNode = addUsersToTree(grpNode, userArray);
        
        tree.setSelectionPath(new TreePath(lastUserNode.getPath()));
    }
    
    /**
     * @param grpNode
     * @param userArray
     * @return
     */
    private DefaultMutableTreeNode addUsersToTree(final DefaultMutableTreeNode grpNode, 
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
    
    /**
     * @param <T>
     * @param node
     * @param cls
     * @return
     */
    @SuppressWarnings( { "unchecked", "unused" })
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

        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper) (parentNode.getUserObject());
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
        
        division.setName("Anonymous Division"); // I18N
        discipline.setType("New Discipline");   // I18N
        
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
            e1.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, e1);
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
}
