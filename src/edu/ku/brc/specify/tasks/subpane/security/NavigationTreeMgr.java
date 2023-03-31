/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.Component;
import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.config.Scriptlet;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroupScope;
import edu.ku.brc.specify.datamodel.busrules.SpecifyUserBusRules;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This class perform operations on the security administration navigation tree, such as 
 * the creation or deletion of an item on the tree. An items is one instance of Discipline,
 * Collection, SpPrincipal (user group) or SpecifyUser. 
 * 
 * @author Ricardo
 * @author rods
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
        
        //check to see if user owns a query used by a schema mapping
        List<Object[]> mappings = BasicSQLUtils.query("select distinct sm.SpExportSchemaMappingID, MappingName from spexportschemamapping sm inner join spexportschemaitemmapping sim on sim.spexportschemamappingid = sm.spexportschemamappingid"	
        		+ " inner join spqueryfield qf on qf.spqueryfieldid = sim.spqueryfieldid inner join spquery q on q.spqueryid = qf.spqueryid where q.specifyuserid=" + user.getSpecifyUserId() + " order by 2");
        if (mappings.size() > 0) {
        	UIRegistry.showLocalizedMsg(JOptionPane.WARNING_MESSAGE, "WARNING","NAVTREEMGR_NO_DEL_USR_W_MAPPINGS", mappings.get(0)[1].toString());
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
            
            Integer     spuId = ((SpecifyUser)object).getId();
            SpecifyUser user  = session.get(SpecifyUser.class, spuId);
            
            // break the association between the user and all its agents, 
            // so the user can be later deleted
            for (Agent agent : user.getAgents())
            {
                agent.setSpecifyUser(null);
            }
            user.getAgents().clear();
            
            BaseBusRules.removeById(spUsers, user);
            
            Vector<Integer> recSetIds = BasicSQLUtils.queryForInts("SELECT RecordSetID FROM recordset WHERE SpecifyUserID = "+spuId);
            for (Integer rsId : recSetIds)
            {
                RecordSet rs  = session.get(RecordSet.class, rsId);
                session.delete(rs);
            }
            
            // These should be done via Delete Orphan
            /*for (SpAppResourceDir apd : user.getSpAppResourceDirs())
            {
                session.delete(apd);
            }
            
            for (SpAppResource ap : user.getSpAppResources())
            {
                session.delete(ap);
            }
            user.getSpQuerys();
            */
            
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
                
                //log.debug(wrp.getDataObj()+"  "+wrp.getDataObj());
                
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
        Division   parentDivision   = parentDiscipline.getDivision();
        Collection collection       = getParentOfClass(grpNode, Collection.class);
       
        DataModelObjBaseWrapper parentWrp = (DataModelObjBaseWrapper)grpNode.getUserObject();
        if (!parentWrp.isGroup())
        {
            return null; // selection isn't a suitable parent for a group
        }
        
        SpPrincipal grpPrin = (SpPrincipal)parentWrp.getDataObj();
        
        SpecifyUser specifyUser  = new SpecifyUser();
        specifyUser.initialize();
        specifyUser.setUserType(grpPrin.getGroupType());
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)getMostRecentWindow(),
                                                                null,
                                                                "User",
                                                                null,
                                                                DBTableIdMgr.getInstance().getTitleForId(SpecifyUser.getClassTableId()),
                                                                null,
                                                                specifyUser.getClass().getName(),
                                                                "specifyUserId",
                                                                true,
                                                                MultiView.HIDE_SAVE_BTN | 
                                                                MultiView.DONT_ADD_ALL_ALTVIEWS | 
                                                                MultiView.USE_ONLY_CREATION_MODE |
                                                                MultiView.IS_NEW_OBJECT);
        dlg.createUI();
        
        Component cbx = (Component)dlg.getMultiView().getCurrentViewAsFormViewObj().getControlByName("agent");
        JLabel    lbl = dlg.getMultiView().getCurrentViewAsFormViewObj().getLabelFor(cbx);
        cbx.setEnabled(false);
        cbx.setVisible(false);
        lbl.setVisible(false);
        
        AppContextMgr acMgr          = AppContextMgr.getInstance();
        Discipline    currDiscipline = acMgr.getClassObject(Discipline.class);
        Division      currDivision   = acMgr.getClassObject(Division.class);
        
        acMgr.setClassObject(Discipline.class, parentDiscipline);
        acMgr.setClassObject(Division.class,   parentDiscipline.getDivision());
        
        // This is just an extra safety measure to make sure the current Discipline gets set back
        try
        {
            // Has no password here
            dlg.setData(specifyUser);
            dlg.setVisible(true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, ex);
            
        } finally
        {
            acMgr.setClassObject(Discipline.class, currDiscipline);    
            acMgr.setClassObject(Division.class,   currDivision);    
        }
        
        if (!dlg.isCancelled())
        {
            String textPwd = specifyUser.getPassword();
            specifyUser.setPassword(Encryption.encrypt(textPwd, textPwd));
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                session.attach(parentDiscipline);
                session.attach(parentDiscipline.getDivision());
                
                List<Agent> userAgents = getAgent(session, parentDiscipline.getDivision(), parentDiscipline, specifyUser, true);
                if (userAgents == null || userAgents.size() == 0)
                {
                    return null;
                }
                
                session.beginTransaction();

                SpecifyUserBusRules busRules = new SpecifyUserBusRules();
                busRules.initialize(dlg.getMultiView().getCurrentView());
                busRules.beforeMerge(specifyUser, session);
                busRules.beforeSave(specifyUser, session);

                // persist newly created user and agent
                session.save(specifyUser);

                // get fresh copies of parentDiscipline and group to make Hibernate happy
                //Discipline  localDiscipline = session.get(Discipline.class, parentDiscipline.getUserGroupScopeId());
                SpPrincipal localGroup      = session.get(SpPrincipal.class, grpPrin.getUserGroupId());

                // link user to its group
                specifyUser.getSpPrincipals().add(localGroup);
                localGroup.getSpecifyUsers().add(specifyUser);

                boolean fndParentDiv = false;
                for (Agent userAgent : userAgents)
                {
                    if (userAgent.getId() != null)
                    {
                        session.attach(userAgent);
                    }
                    
                    if (!fndParentDiv && parentDivision.getId().equals(userAgent.getDivision().getId()))
                    {
                        fndParentDiv = true;
                    }
                    
                    // link agent to user
                    userAgent.setSpecifyUser(specifyUser);
                    specifyUser.getAgents().add(userAgent);
                    
                    session.saveOrUpdate(userAgent);
                }
                
                if (userAgents.size() > 0 && !fndParentDiv)
                {
                    Agent userAgent = (Agent)userAgents.get(0).clone();
                    userAgent.setDivision(parentDiscipline.getDivision());
                    
                    userAgent.setSpecifyUser(specifyUser);
                    specifyUser.getAgents().add(userAgent);
                    
                    session.saveOrUpdate(userAgent);
                }
                
                // create a JAAS principal and associate it with the user
                SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(specifyUser, collection);
                session.save(userPrincipal);
                specifyUser.addUserToSpPrincipalGroup(userPrincipal);
                
                session.saveOrUpdate(specifyUser);
                
                // this next line is not needed in order for the relationship to be saved
                // and it is problematic when there are a lot of agents
                
                session.commit();
                
                parentWrp.setDataObj(localGroup);
                
                spUsers.add(specifyUser);
                specifyUser = session.get(SpecifyUser.class, specifyUser.getId());
                
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
            
            DataModelObjBaseWrapper userWrp  = new DataModelObjBaseWrapper(specifyUser);
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
        DataModelObjBaseWrapper wrp        = (DataModelObjBaseWrapper)grpNode.getUserObject();
        DefaultMutableTreeNode  parentNode = (DefaultMutableTreeNode)grpNode.getParent();
        
        ArrayList<SpPrincipal> groups = new ArrayList<SpPrincipal>();
        for (int i=0;i<parentNode.getChildCount();i++)
        {
            DefaultMutableTreeNode  childNode = (DefaultMutableTreeNode)parentNode.getChildAt(i);
            DataModelObjBaseWrapper childWrp  = (DataModelObjBaseWrapper)childNode.getUserObject();
            SpPrincipal             prin      = (SpPrincipal)childWrp.getDataObj();
            groups.add(prin);
        }
        
        SpPrincipal             prinGroup = (SpPrincipal) wrp.getDataObj();
        AddExistingUserDlg      dlg       = new AddExistingUserDlg(groups);
        dlg.createUI();
        dlg.pack();
        dlg.setSize(400, 300);
        UIHelper.centerAndShow(dlg);
        
        if (dlg.isCancelled())
        {
            return null;
        }

        // This is the existing User to be Added to the New Collection/Discipline
        SpecifyUser specifyUser = null;//dlg.getSelectedUser();
        SpecifyUser[] selUsers = dlg.getSelectedUsers();
        if (selUsers == null || selUsers.length == 0)
        {
            return null;
        }
        specifyUser = selUsers[0];
        
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
        
        AppContextMgr acMgr         = AppContextMgr.getInstance();
        
        // Set the Parent Discipline into the Context so it thinks we are in
        // that context when we add all the security info.
        Discipline    currDiscipline = acMgr.getClassObject(Discipline.class);
        Division      currDivision   = acMgr.getClassObject(Division.class);
        
        acMgr.setClassObject(Discipline.class, parentDiscipline);
        acMgr.setClassObject(Division.class, parentDiscipline.getDivision());
        
        Collection collection = getParentOfClass(grpNode, Collection.class);
        
        // This section cleans up any problems from removing a using.
        String sql = String.format("SELECT p.SpPrincipalID FROM collection c INNER JOIN spprincipal p ON c.UserGroupScopeId = p.userGroupScopeID " +
                "INNER JOIN specifyuser_spprincipal ss ON p.SpPrincipalID = ss.SpPrincipalID " +
                "INNER JOIN specifyuser su ON ss.SpecifyUserID = su.SpecifyUserID WHERE su.SpecifyUserID = %d AND c.CollectionID = %d", specifyUser.getId(), collection.getId());
        Integer prinId = BasicSQLUtils.getCount(sql);
        if (prinId != null)
        {
            sql = String.format("DELETE FROM specifyuser_spprincipal WHERE SpPrincipalID=%d AND SpecifyUserID=%d", prinId, specifyUser.getId());
            int rv = BasicSQLUtils.update(sql);
            log.debug("rv="+rv);
            
            sql = String.format("SELECT pp.SpPermissionID FROM spprincipal p INNER JOIN spprincipal_sppermission pp ON p.SpPrincipalID = pp.SpPrincipalID WHERE p.SpPrincipalID = %d", prinId);
            for (Integer id : BasicSQLUtils.queryForInts(sql))
            {
                sql = String.format("DELETE FROM sppermission WHERE SpPermissionID=%d", id);
                rv = BasicSQLUtils.update(sql);
                log.debug("rv="+rv);
            }
            sql = String.format("DELETE FROM spprincipal WHERE SpPrincipalID=%d", prinId);
        } // done with clean up
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
         
            prinGroup = session.merge(prinGroup);
            
            wrp.setDataObj(prinGroup);

            // Add users to Group
            specifyUser = session.get(SpecifyUser.class, specifyUser.getId());
                
            prinGroup.getSpecifyUsers().add(specifyUser);
            specifyUser.getSpPrincipals().add(prinGroup);
            
            List<Agent> userAgents = getAgent(session, parentDiscipline.getDivision(), parentDiscipline, specifyUser, false);
            if (userAgents == null || userAgents.size() == 0)
            {
                return null;
            }
            
            session.beginTransaction();

            for (Agent userAgent : userAgents)
            {
                userAgent.setSpecifyUser(specifyUser);
                specifyUser.getAgents().add(userAgent);
                session.saveOrUpdate(specifyUser);
                session.saveOrUpdate(userAgent);
            }
            
            // create a JAAS principal and associate it with the user
            SpPrincipal userPrincipal = DataBuilder.createUserPrincipal(specifyUser, collection);
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
            acMgr.setClassObject(Division.class,   currDivision); 
            
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
     * @param session
     * @param parentDivision
     * @param parentDiscipline
     * @param specifyUser
     * @return
     * @throws CloneNotSupportedException
     */
    private List<Agent> getAgent(final DataProviderSessionIFace session,
                                 final Division    parentDivision,
                                 final Discipline  parentDiscipline,
                                 final SpecifyUser specifyUser,
                                 final boolean     doAddNewUser) throws CloneNotSupportedException
    {
        ArrayList<Agent> agentsList = new ArrayList<Agent>();
        
        Agent   userAgent = null;
        Integer agentId   = null;
        String  lastName  = null;
        String  firstName = null;
        
        if (specifyUser.getId() != null)
        {
            String  sql = String.format("SELECT AgentID, LastName, FirstName FROM agent WHERE SpecifyUserID = %d", specifyUser.getId());
            Vector<Object[]> agentRow = BasicSQLUtils.query(sql);
            if (agentRow == null || agentRow.size() == 0)
            {
                UIRegistry.showError("Error finding an agent for the current division");
                return null;
            }
            
            Object[] row = agentRow.get(0);
            lastName   = (String)row[1];
            firstName  = (String)row[2];
        }
        
        int newAgentOption = JOptionPane.CANCEL_OPTION;
        if (doAddNewUser)
        {
            newAgentOption = UIRegistry.askYesNoLocalized("NVTM.NEW_AGT", "NVTM.EXT_AGT", getResourceString("NVTM.USRAGTMSGF"), "NVTM.USRAGTMSGF_TITLE");
            
			if (newAgentOption == JOptionPane.NO_OPTION) { // Search For Agent 
				Division currDivision = AppContextMgr.getInstance().getClassObject(Division.class);
				Discipline currDiscipline = AppContextMgr.getInstance().getClassObject(Discipline.class);


				boolean keepTrying = true;
				while (keepTrying) {
					agentsList.clear();
					AppContextMgr.getInstance().setClassObject(Division.class, parentDivision);
					AppContextMgr.getInstance().setClassObject(Discipline.class, parentDiscipline);
					ViewBasedSearchDialogIFace dlg = UIRegistry.getViewbasedFactory().createSearchDialog(null, "UserAgentSearch");
					try {
						dlg.registerQueryBuilder(null);
						dlg.setMultipleSelection(false);
						dlg.getDialog().setVisible(true);

					} catch (Exception ex) {
						ex.printStackTrace();

					} finally {
						AppContextMgr.getInstance().setClassObject(Division.class, currDivision);
						AppContextMgr.getInstance().setClassObject(Discipline.class, currDiscipline);
					}

					if (!dlg.isCancelled()) {
						Agent agt = session.get(Agent.class, ((Agent) dlg.getSelectedObject()).getAgentId());
						agt.getDivision().getId(); // forcing the Division to load
						if (agt.getSpecifyUser() == null) {
							agentsList.add(agt);
							keepTrying = false;
							break;
						} else {
							//System.out.println("The selected agent is already associated with user '" + agt.getSpecifyUser().getName() + "'.");
							UIRegistry.showLocalizedError("NVTM.AgentAlreadyHasUser", agt.getSpecifyUser().getName());
							keepTrying = true;
						}
					} else {
						keepTrying = false;
					}
				}
				return agentsList;
			}
        }
        
        if (!doAddNewUser)
        {
            Scriptlet scriptlet = new Scriptlet();
            ArrayList<AgentInfo> list = new ArrayList<AgentInfo>();

            boolean isFirstNull = firstName == null;
            String firstWhere = isFirstNull ? "FirstName IS NULL" : "FirstName = ?";
            String sql = String.format("SELECT DISTINCT AgentID, LastName, FirstName, MiddleInitial FROM agent WHERE LastName = ? AND %s AND DivisionID = ?", firstWhere);
            log.debug(sql);
            PreparedStatement pStmt = null;
            try
            {
                pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                int i = 1;
                pStmt.setString(i++, lastName);
                if (!isFirstNull)
                {
                    pStmt.setString(i++, firstName);
                }
                pStmt.setInt(i++, parentDivision.getId());
                ResultSet rs = pStmt.executeQuery();
                while (rs.next())
                {
                    Integer  aId    = rs.getInt(1);
                    String   lName  = rs.getString(2);
                    String   fName  = rs.getString(3);
                    String   mid    = rs.getString(4);
                    AgentInfo pair = new AgentInfo(aId, scriptlet.buildNameString(fName, lName, mid));
                    list.add(pair);
                }
                rs.close();
                pStmt.close();
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NavigationTreeMgr.class, ex);
            }
            
            if (!list.isEmpty());
            {
                ChooseFromListDlg<AgentInfo> agtDlg = new ChooseFromListDlg<AgentInfo>(
                        (Frame)UIRegistry.getMostRecentWindow(), getResourceString("NVTM.CHSE_AGT"), list);
                UIHelper.centerAndShow(agtDlg);
                if (!agtDlg.isCancelled())
                {
                    Pair<Integer, String> pair = agtDlg.getSelectedObject();
                    agentId = pair.first;
                } else
                {
                    return null;
                }
            }
        }
        
        if (agentId == null) // Couldn't find an existing agent
        {
            if (!doAddNewUser && specifyUser.getAgents().size() > 0)
            {
                // Clone existing agent
                Agent agent = specifyUser.getAgents().iterator().next();
                userAgent = (Agent)agent.clone();
                userAgent.setDivision(parentDivision);
                agentsList.add(userAgent);
                return agentsList;
            } 
            
            // create new Agent here
            userAgent = createNewAgent();
            if (userAgent != null)
            {
                userAgent.setDivision(parentDivision);
                agentsList.add(userAgent);
                return agentsList;
            }
            return null;
        }
        
        userAgent = (Agent)session.getData("FROM Agent agent WHERE id = " + agentId);
        agentsList.add(userAgent);
        return agentsList;
    }
    
    /**
     * @return
     */
    private Agent createNewAgent()
    {
        Agent agent = new Agent();
        agent.initialize();
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)getMostRecentWindow(),
                null,       // ViewSet Name
                "Agent",    // View Name
                null,       // Display Name
                DBTableIdMgr.getInstance().getTitleForId(Agent.getClassTableId()), // Title
                null,       // Close Btn
                Agent.class.getName(), // Class Name
                "agentId",  // idFieldName
                true,       // isEdit
                MultiView.HIDE_SAVE_BTN | 
                MultiView.DONT_ADD_ALL_ALTVIEWS | 
                MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_NEW_OBJECT);
        dlg.createUI();
        dlg.setData(agent);
        
        dlg.pack();
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            return agent;
        }
        return null;
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
    
    //------------------------------------------
    class AgentInfo extends Pair<Integer, String>
    {
        /**
         * 
         */
        public AgentInfo()
        {
            super();
        }

        /**
         * @param first
         * @param second
         */
        public AgentInfo(Integer first, String second)
        {
            super(first, second);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.util.Pair#toString()
         */
        @Override
        public String toString()
        {
            return second;
        }
        
    }
}
