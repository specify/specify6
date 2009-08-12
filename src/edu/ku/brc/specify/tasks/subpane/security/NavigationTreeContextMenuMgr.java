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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * An instance of this class manages the creation of the context (pop-up or right-click) menu that
 * that lets the user perform operations on the contents of the security admin panel navigation
 * tree. It lets users add or delete items to the tree. 
 * 
 * @author Ricardo
 * 
 */
@SuppressWarnings("serial")
public class NavigationTreeContextMenuMgr extends MouseAdapter implements TreeSelectionListener
{
    private NavigationTreeMgr treeMgr;
    private JButton           addUserBtn;
    private JButton           addExtUserBtn;
    private JButton           delUserBtn;
    private JButton           addToAdminBtn;
    
    private DefaultMutableTreeNode lastClickComp = null;


    /**
     * Constructor. Attaches itself as tree selection and mouse listener.
     * 
     * @param treeRenderer Tree
     */
    public NavigationTreeContextMenuMgr(final NavigationTreeMgr treeMgr)
    {
        this.treeMgr = treeMgr;
        getTree().addTreeSelectionListener(this);
        getTree().addMouseListener(this);
    }
    
    /**
     * @param addBtn
     * @param addExtBtn
     * @param addToAdminBtn
     * @param delBtn
     */
    public void setBtn(final JButton addBtn, 
                       final JButton addExtBtn, 
                       final JButton addToAdminBtn, 
                       final JButton delBtn)
    {
        this.addUserBtn    = addBtn;
        this.addExtUserBtn = addExtBtn;
        this.delUserBtn    = delBtn;
        this.addToAdminBtn = addToAdminBtn;
        
        addUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lastClickComp = getTreeMgr().addNewUser(lastClickComp);
                updateBtnUI();
            }
        });
        
        addExtUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                lastClickComp = getTreeMgr().addExistingUser(lastClickComp);
                updateBtnUI();
            }
        });
        
        delUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (getTreeMgr().canDeleteUser(lastClickComp))
                {
                    getTreeMgr().deleteUser(lastClickComp);
                    
                } else if (getTreeMgr().canRemoveUserFromGroup(lastClickComp))
                {
                    getTreeMgr().removeUserFromGroup(lastClickComp);
                }
                lastClickComp = null;
                updateBtnUI();
            }
        });
        
        addToAdminBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addToAdminGroup(lastClickComp);
            }
        });
    }        
    
    /**
     * 
     */
    public void updateBtnUI()
    {
        if (lastClickComp != null)
        {
            Object           userObject = lastClickComp.getUserObject();
            FormDataObjIFace dmObject   = ((DataModelObjBaseWrapper) userObject).getDataObj();
    
            if (dmObject instanceof SpecifyUser)
            {
                SpecifyUser spu = (SpecifyUser)dmObject;
                
                addUserBtn.setEnabled(false);
                addExtUserBtn.setEnabled(false);
                
                boolean isLastAdminUser = false;
                boolean isParentMgrGrp  = false;
                boolean isUserInAdmGrp  = spu.isInAdminGroup();
                DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode)lastClickComp.getParent();
                if (parentTreeNode != null)
                {
                    DataModelObjBaseWrapper pWrapper = (DataModelObjBaseWrapper)(parentTreeNode.getUserObject() instanceof DataModelObjBaseWrapper ?  parentTreeNode.getUserObject() : null);
                    if (pWrapper != null)
                    {
                        SpPrincipal parentsPrin = (SpPrincipal)(pWrapper.getDataObj() instanceof SpPrincipal ? pWrapper.getDataObj() : null); 
                        if (parentsPrin != null)
                        {
                            boolean isInAdminGrp = AdminPrincipal.class.getCanonicalName().equals(parentsPrin.getGroupSubClass());
                            isLastAdminUser = parentsPrin.getSpecifyUsers().size() == 1 && isInAdminGrp;
                            isParentMgrGrp     = parentsPrin.getGroupType() != null && parentsPrin.getGroupType().equals("Manager");
                            //System.out.println("Parent is Admin Grp: " + isInAdminGrp + "  Is Last Admin: " + isLastAdminUser + "  Parent is Mgr: "+(parentsPrin.getGroupType() != null ? parentsPrin.getGroupType().equals("Manager") : "null") +"  User In AdminGrp: "+ spu.isInAdminGroup());
                        }
                    }
                }
                
                addToAdminBtn.setEnabled(isParentMgrGrp && !isUserInAdmGrp);
    
                if (!isLastAdminUser)
                {
                    String toolTip = "";
                    boolean canDelUser = getTreeMgr().canDeleteUser(lastClickComp);
                    boolean canRemUser = getTreeMgr().canRemoveUserFromGroup(lastClickComp);
                    if (canDelUser)
                    {
                        toolTip = "Delete User from Group"; // I18N
                        
                    } else if (canRemUser)
                    {
                        toolTip = "Remove User from Group (does not delete the user)";
                    }
                    delUserBtn.setEnabled(canDelUser || canRemUser);
                    delUserBtn.setToolTipText(toolTip);
                } else
                {
                    delUserBtn.setEnabled(false);
                    delUserBtn.setToolTipText(null);
                }
                
            } else if (dmObject instanceof SpPrincipal)
            {
                boolean enable = getTreeMgr().canAddNewUser(lastClickComp);
                addUserBtn.setEnabled(enable);
                addExtUserBtn.setEnabled(enable);
                delUserBtn.setEnabled(false);
                
            } else if (dmObject instanceof Collection)
            {
                // object is a collection: offer to add new group and to delete the collection
            }
        } else
        {
            addUserBtn.setEnabled(treeMgr.canAddNewUser(lastClickComp));
            addExtUserBtn.setEnabled(treeMgr.canAddNewUser(lastClickComp));
            delUserBtn.setEnabled(treeMgr.canDeleteUser(lastClickComp));
            addToAdminBtn.setEnabled(treeMgr.canAddToAdmin(lastClickComp));
        }
    }

    /**
     * @return
     */
    public JTree getTree()
    {
        return treeMgr.getTree();
    }

    /**
     * @return
     */
    private NavigationTreeMgr getTreeMgr()
    {
        return treeMgr;
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        TreePath clickedElement = getTree().getPathForLocation(e.getX(), e.getY());

        // Update the selection if necessary
        updateSelection(clickedElement);
        
        TreePath tp = clickedElement;
        if (tp != null)
        {
            lastClickComp = (DefaultMutableTreeNode)tp.getLastPathComponent();
            
            updateBtnUI();
        }
    }

    /**
     * @param node
     * @return
     */
    private DefaultMutableTreeNode getAdminTreeNode(final DefaultMutableTreeNode node)
    {
        for (int i=0;i<node.getChildCount();i++)
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
            FormDataObjIFace       dmObj = ((DataModelObjBaseWrapper) child.getUserObject()).getDataObj();
            
            if (dmObj instanceof SpPrincipal)
            {
                String name = ((SpPrincipal)dmObj).getName();
                if (name.equals("Administrator"))
                {
                    return (DefaultMutableTreeNode)child;
                }   
            }
            DefaultMutableTreeNode cNode = getAdminTreeNode(child);
            if (cNode != null)
            {
                return cNode;
            }
        }
        return null;
    }
    
    /**
     * @param node
     */
    private void addToAdminGroup(final DefaultMutableTreeNode node)
    {
        if (node != null)
        {
            Object userObject = node.getUserObject();
            if (userObject != null)
            {
                FormDataObjIFace dmObject = ((DataModelObjBaseWrapper) userObject).getDataObj();
                if (dmObject != null && dmObject instanceof SpecifyUser)
                {
                    SpPrincipal              adminPrin = null;
                    DataProviderSessionIFace session   = null;
                    try
                    {
                        session   = DataProviderFactory.getInstance().createSession();
                        adminPrin = (SpPrincipal)session.getData(SpPrincipal.class, "name", "Administrator", DataProviderSessionIFace.CompareType.Equals);
                        if (adminPrin != null)
                        {
                            SpecifyUser spUser = (SpecifyUser)dmObject;
                            spUser.addUserToSpPrincipalGroup(adminPrin);
                            
                            session.beginTransaction();
                            session.saveOrUpdate(spUser);
                            session.saveOrUpdate(adminPrin);
                            session.commit();
                            
                            DefaultMutableTreeNode adminNode = getAdminTreeNode((DefaultMutableTreeNode)treeMgr.getTree().getModel().getRoot());
                            DefaultMutableTreeNode newNode   = new DefaultMutableTreeNode(new DataModelObjBaseWrapper(spUser));
                            
                            DefaultTreeModel model = (DefaultTreeModel) getTree().getModel();
                            model.insertNodeInto(newNode, adminNode, adminNode.getChildCount());
                            model.nodeChanged(adminNode);
                            model.nodeChanged(newNode);
                            getTree().repaint();
                            
                            lastClickComp = null;
                            updateBtnUI();
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
                }
            }
        }
    }
    
    /**
     * @param clickedElement
     */
    private void updateSelection(TreePath clickedElement)
    {

        // Find out if the clicked on element is already selected
        boolean clickedElementSelected = false;
        TreePath[] selection = getTree().getSelectionPaths();
        if (clickedElement != null && selection != null)
        {
            // Determine if it one of the selected paths
            for (int index = 0; index < selection.length; ++index)
            {
                if (clickedElement.equals(selection[index]))
                {
                    clickedElementSelected = true;
                    break;
                }
            }
        }

        // Select the clicked on element or clear all selections
        if (!clickedElementSelected)
        {
            if (clickedElement != null)
            {
                // Clicked on unselected item - make it the selection
                getTree().setSelectionPath(clickedElement);
            } else
            {
                // clicked over nothing clear the selection
                getTree().clearSelection();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent event)
    {
    }

}
