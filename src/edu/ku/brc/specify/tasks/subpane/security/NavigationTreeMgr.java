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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroupScope;
import edu.ku.brc.specify.datamodel.busrules.SpecifyUserBusRules;
import edu.ku.brc.ui.UIRegistry;

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
    public NavigationTreeMgr(final JTree tree, final Set<SpecifyUser> spUsers)
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
            SpecifyUser             user    = (SpecifyUser)object;
            
            result = user.canRemoveFromGroup();
            
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
     * @return
     */
    public boolean canAddToAdmin(final DefaultMutableTreeNode userNode)
    {
        if (userNode != null)
        {
            SpecifyUser      spu        = null;
            Object           userObject = userNode.getUserObject();
            FormDataObjIFace dmObject   = ((DataModelObjBaseWrapper) userObject).getDataObj();
            if (dmObject instanceof SpecifyUser)
            {
                spu = (SpecifyUser)dmObject;
                
                DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode)userNode.getParent();
                if (parentTreeNode != null)
                {
                    DataModelObjBaseWrapper pWrapper = (DataModelObjBaseWrapper)(parentTreeNode.getUserObject() instanceof DataModelObjBaseWrapper ?  parentTreeNode.getUserObject() : null);
                    if (pWrapper != null)
                    {
                        SpPrincipal principal = (SpPrincipal)(pWrapper.getDataObj() instanceof SpPrincipal ? pWrapper.getDataObj() : null); 
                        if (principal != null)
                        {
                            return principal.getGroupType().equals("Manager") && !spu.isInAdminGroup();
                        }
                    }
                }
            }
        }
        return false;
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
            
            user = session.get(SpecifyUser.class, user.getId());
            
            ArrayList<UserGroupScope> dspDivList = new ArrayList<UserGroupScope>();
            
            for (SpPrincipal p : new Vector<SpPrincipal>(user.getSpPrincipals()))
            {
                session.attach(p);
                if (p.getId().equals(group.getId()))
                {
                    UserGroupScope ugs = p.getScope();
                    if (ugs.getDataClass() == Discipline.class || ugs.getDataClass() == Division.class || ugs.getDataClass() == Collection.class)
                    {
                        dspDivList.add(ugs);
                    }
                    user.getSpPrincipals().remove(p);        
                }
            }
            
            // Get a Set of all the Disciplines the user can no longer access
            // And get the DivisionID so we can get the correct Agent for this SpecifyUser
            Integer          divIdForAgent = null;
            HashSet<Integer> dispSet       = new HashSet<Integer>();
            for (UserGroupScope ugs : dspDivList)
            {
                if (ugs.getDataClass() == Division.class)
                {
                    for (Integer id : BasicSQLUtils.queryForInts("SELECT DisciplineID FROM discipline WHERE DivisionID = "+ugs.getId()))
                    {
                        dispSet.add(id);
                    }
                    divIdForAgent = ugs.getId();
                    
                } else if (ugs.getDataClass() == Discipline.class)
                {
                    dispSet.add(ugs.getId());
                    divIdForAgent = BasicSQLUtils.getCount("SELECT DivisionID FROM discipline WHERE DisciplineID = "+ugs.getId());
                    
                } else if (ugs.getDataClass() == Collection.class)
                {
                    dispSet.add(ugs.getId());
                    Vector<Object[]> ids = BasicSQLUtils.query("SELECT d.DivisionID, d.UserGroupScopeId FROM collection c Inner Join discipline d ON c.DisciplineID = d.UserGroupScopeId WHERE c.CollectionID = "+ugs.getId());
                    if (ids.size() == 1)
                    {
                        dispSet.add((Integer)ids.get(0)[1]);
                        divIdForAgent = (Integer)ids.get(0)[0];
                    }
                }
            }
            
            // Remove all the agent_discipline
            if (divIdForAgent != null && dispSet.size() > 0)
            {
                String sql = "SELECT a.AgentID, a.DivisionID FROM specifyuser s Inner Join agent a ON s.SpecifyUserID = a.SpecifyUserID WHERE s.SpecifyUserID = " + user.getId();
                for (Object[] cols : BasicSQLUtils.query(sql))
                {
                    Integer agtId = (Integer)cols[0];
                    Integer divId = (Integer)cols[1];
                    
                    if (divId.equals(divIdForAgent))
                    {
                        for (Integer disciplineId : dispSet)
                        {
                            int cnt = BasicSQLUtils.getCount(String.format("SELECT COUNT(*) FROM agent_discipline WHERE DisciplineID = %d AND AgentID = %d", disciplineId, agtId));
                            if (cnt > 0)
                            {
                                sql = String.format("DELETE FROM agent_discipline WHERE DisciplineID = %d AND AgentID = %d", disciplineId, agtId);
                                if (cnt != BasicSQLUtils.update(sql))
                                {
                                    log.error("Error deleting: " + sql);
                                }
                            }
                            
                        }
                    }
                }
            } else
            {
                // Error 
            }
            
            session.saveOrUpdate(user);
            session.commit();
            
            replaceUserInOtherNodes(user, (DefaultMutableTreeNode)tree.getModel().getRoot());
            
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
     * @param updatedUser
     * @param node
     */
    private void replaceUserInOtherNodes(final SpecifyUser updatedUser, 
                                         final DefaultMutableTreeNode node)
    {
        for (int i=0;i<node.getChildCount();i++)
        {
            DefaultMutableTreeNode  child   = (DefaultMutableTreeNode)node.getChildAt(i);
            DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper)child.getUserObject();
            FormDataObjIFace        dmObj   = wrapper.getDataObj();
            
            if (dmObj instanceof SpecifyUser)
            {
                SpecifyUser spu = (SpecifyUser)dmObj;
                if (updatedUser.getId().equals(spu.getId()))
                {
                    wrapper.setDataObj(updatedUser);
                }
            }
            replaceUserInOtherNodes(updatedUser, child);
        }
    }
    
    /**
     * @param userNode
     * @return
     */
    public boolean canDeleteUser(final DefaultMutableTreeNode userNode)
    {
        if (userNode == null) return false;
        
        // get the user who's logged in
        final SpecifyUser currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);

        // get the user from the selected tree node
        DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) userNode.getUserObject();
        Object                  object  = wrapper.getDataObj();
        
        if (!(object instanceof SpecifyUser))
        {
            return false;
        }
        
        SpecifyUser user = (SpecifyUser)object;

        if (currentUser.getSpecifyUserId().equals(user.getSpecifyUserId()))
        {
            UIRegistry.showLocalizedMsg("NAVTREEMGR_NO_DEL_SELF");
            return false;
        }
        
        DataProviderSessionIFace session = null;
        boolean result = false;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            user = (SpecifyUser)session.getData("FROM SpecifyUser WHERE id = "+user.getId());
            if (user != null)
            {
                wrapper.setDataObj(user);
                
                int    numOfGrpsUserBelonedTo = user.getUserGroupCount(); // the number of groups this user belongs to
                return numOfGrpsUserBelonedTo == 1;
                //return user.canRemoveFromGroup();
                    
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
        // Ask here to delete user
        
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
            
            BaseBusRules.removeById(spUsers, user);
            
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
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
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
        if (node != null)
        {
            DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper)node.getUserObject();
            Object                  object  = wrapper.getDataObj();
    
            if (object instanceof SpPrincipal)
            {
                SpPrincipal principal = (SpPrincipal) object;
                return !AdminPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass());
            }
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
        if (node != null)
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
        return false;
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
    public DefaultMutableTreeNode addNewUser(final DefaultMutableTreeNode grpNode) 
    {
        if (grpNode == null || !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return null; // Nothing is selected or object type isn't relevant 
        }
        
        // discipline to which the user's being added
        Discipline parentDiscipline = getParentDiscipline(grpNode);
       
        //final Division   division   = parentDiscipline.getDivision();
        //final Discipline discipline = parentDiscipline;
        
        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper)grpNode.getUserObject();
        if (!parentWrp.isGroup())
        {
            return null; // selection isn't a suitable parent for a group
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
        
        int divCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM division");
        ValComboBoxFromQuery cbx = (ValComboBoxFromQuery)dlg.getMultiView().getCurrentViewAsFormViewObj().getControlByName("agent");
        if (cbx != null && divCnt > 1)
        {
            cbx.registerQueryBuilder(new UserAgentVSQBldr(cbx));
            cbx.setReadOnlyMode();
        }
        
        AppContextMgr acMgr          = AppContextMgr.getInstance();
        Discipline    currDiscipline = acMgr.getClassObject(Discipline.class);
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
                
                // this next line is not needed in order for the relationship to be saved
                // and it is problematic when there are a lot of agents
                //localDiscipline.getAgents().add(userAgent);
                
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
            if (userWrp != null)
            {
                DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);
                if (userNode != null)
                {
                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                    model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
                    
                    tree.setSelectionPath(new TreePath(model.getPathToRoot(userNode)));
                }
                return userNode;
            }
        }
        return null;
    }
    
    /**
     * @param grpNode
     * @return
     */
    public DefaultMutableTreeNode addExistingUser(final DefaultMutableTreeNode grpNode) 
    {
        DataModelObjBaseWrapper wrp       = (DataModelObjBaseWrapper) grpNode.getUserObject();
        SpPrincipal             prinGroup = (SpPrincipal) wrp.getDataObj();
        AddExistingUserDlg      dlg       = new AddExistingUserDlg(null, prinGroup);
        dlg.setVisible(true);
        
        if (dlg.isCancelled())
        {
            return null;
        }

        // This is the existing User to be Added to the New Collection/Discipline
        SpecifyUser specifyUser = dlg.getSelectedUser();
        
        if (specifyUser == null || grpNode == null || 
            !(grpNode.getUserObject() instanceof DataModelObjBaseWrapper))
        {
            return null; // Nothing is selected or object type isn't relevant 
        }

        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper) (grpNode.getUserObject());
        if (!parentWrp.isGroup())
        {
            return null; // selection isn't a suitable parent for a group
        }
        // Discipline to which the user's being added by walking up the tree
        // to find the appropriate Discipline Node
        Discipline parentDiscipline = getParentDiscipline(grpNode);
        
        AppContextMgr acMgr          = AppContextMgr.getInstance();
        
        // Set the Parent Discipline into the Context so it thinks we are in
        // that context when we add all the security info.
        Discipline    currDiscipline = acMgr.getClassObject(Discipline.class);
        acMgr.setClassObject(Discipline.class, parentDiscipline);
        
        Division currDivision = parentDiscipline.getDivision();

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            
            prinGroup = session.merge(prinGroup);
            
            wrp.setDataObj(prinGroup);

            // Add users to Group
            specifyUser = (SpecifyUser)session.getData("FROM SpecifyUser WHERE id = "+specifyUser.getId());
                
            prinGroup.getSpecifyUsers().add(specifyUser);
            specifyUser.getSpPrincipals().add(prinGroup);
            
            Agent  clonedAgent;
            String sql = String.format("SELECT AgentID FROM agent a WHERE a.SpecifyUserID = %d AND DivisionID = %d", specifyUser.getId(), currDivision.getId());
            Integer existingAgentID = BasicSQLUtils.getCount(sql);
            if (existingAgentID == null)
            {
                Agent agent = specifyUser.getAgents().iterator().next();
                clonedAgent = (Agent)agent.clone();
                clonedAgent.setDivision(currDivision);
                clonedAgent.getDisciplines().clear();
                
            } else
            {
                clonedAgent = (Agent)session.getData("FROM Agent agent WHERE id = " + existingAgentID);
            }
            
            // Add the New Agent or Existing Agent to the New Discipline.
            sql = String.format("SELECT COUNT(*) FROM agent_discipline WHERE AgentID = %d AND DisciplineID = %d", clonedAgent.getId(), parentDiscipline.getId());
            int agtDspCnt = BasicSQLUtils.getCountAsInt(sql);
            if (agtDspCnt < 1)
            {
                clonedAgent.getDisciplines().add(parentDiscipline);    
            }
            
            clonedAgent.setSpecifyUser(specifyUser);
            specifyUser.getAgents().add(clonedAgent);
            
            session.saveOrUpdate(specifyUser);
            session.saveOrUpdate(clonedAgent);
            
            // create a JAAS principal and associate it with the user
            SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(specifyUser);
            session.save(userPrincipal);
            specifyUser.addUserToSpPrincipalGroup(userPrincipal);
            
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
            acMgr.setClassObject(Discipline.class, currDiscipline); 
            
            if (session != null)
            {
                session.close();
            }
        }
        
        DefaultMutableTreeNode lastUserNode = addUsersToTree(grpNode, specifyUser);
        
        tree.setSelectionPath(new TreePath(lastUserNode.getPath()));
        
        return lastUserNode;
    }
    
    /**
     * @param grpNode
     * @param userArray
     * @return
     */
    private DefaultMutableTreeNode addUsersToTree(final DefaultMutableTreeNode grpNode, 
                                                  final SpecifyUser spUser)
    {
        DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(spUser);
        DefaultMutableTreeNode  userNode = new DefaultMutableTreeNode(userWrp);

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(userNode, grpNode, grpNode.getChildCount());
        
        return userNode;
    }
    
    /**
     * @param <T>
     * @param node
     * @param cls
     * @return
     */
    @SuppressWarnings( { "unchecked"})
    protected <T> T getParentOfClass(final DefaultMutableTreeNode node, final Class<?> cls)
    {
        DefaultMutableTreeNode parent = node;
        while (parent != null)
        {
            if (parent.getUserObject() instanceof DataModelObjBaseWrapper)
            {
                DataModelObjBaseWrapper userData = (DataModelObjBaseWrapper)parent.getUserObject();
                if (userData.getDataObj().getClass() == cls)
                {
                    return (T)userData.getDataObj();
                }
            }
            parent = (DefaultMutableTreeNode)parent.getParent();
        }
        return null;
    }
}
