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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
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
    private JButton           rmvUserBtn;
    
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
     * @param delBtn
     * @param rmvBtn
     */
    public void setBtn(final JButton addBtn, 
                       final JButton addExtBtn, 
                       final JButton delBtn, 
                       final JButton rmvBtn)
    {
        this.addUserBtn    = addBtn;
        this.addExtUserBtn = addExtBtn;
        this.delUserBtn    = delBtn;
        this.rmvUserBtn    = rmvBtn;
        
        addUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getTreeMgr().addNewUser(lastClickComp);
                updateBtnUI();
            }
        });
        
        addExtUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getTreeMgr().addExistingUser(lastClickComp);
                updateBtnUI();
            }
        });
        
        delUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getTreeMgr().deleteUser(lastClickComp);
                updateBtnUI();
            }
        });
        
        rmvUserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getTreeMgr().removeUserFromGroup(lastClickComp);
                updateBtnUI();
            }
        });
    }        
    
    /**
     * 
     */
    protected void updateBtnUI()
    {
         addUserBtn.setEnabled(false);
         addExtUserBtn.setEnabled(false);
         delUserBtn.setEnabled(false);
         rmvUserBtn.setEnabled(false);

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
            
            Object           userObject = ((DefaultMutableTreeNode) clickedElement.getLastPathComponent()).getUserObject();
            FormDataObjIFace dmObject   = ((DataModelObjBaseWrapper) userObject).getDataObj();
    
            if (dmObject instanceof SpecifyUser)
            {
                addUserBtn.setEnabled(false);
                addExtUserBtn.setEnabled(false);
                delUserBtn.setEnabled(getTreeMgr().canDeleteUser(lastClickComp));
                rmvUserBtn.setEnabled(getTreeMgr().canRemoveUserFromGroup(lastClickComp));
                
            }
            else if (dmObject instanceof SpPrincipal)
            {
                boolean enable = getTreeMgr().canAddNewUser(lastClickComp);
                addUserBtn.setEnabled(enable);
                addExtUserBtn.setEnabled(enable);
                delUserBtn.setEnabled(false);
                rmvUserBtn.setEnabled(false);
                
            } else if (dmObject instanceof Collection)
            {
                // object is a collection: offer to add new group and to delete the collection
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger() && e.getClickCount() == 1)
        {
            //doPopup(e.getX(), e.getY());
        }
    }

    /**
     * @param x
     * @param y
     */
    public void doPopup(int x, int y)
    {
        // Get the tree element under the mouse
        TreePath clickedElement = getTree().getPathForLocation(x, y);

        // Update the selection if necessary
        updateSelection(clickedElement);

        // Get the desired context menu and show it
        JPopupMenu contextMenu = retrieveContextMenu(clickedElement);
        if (contextMenu != null)
        {
            contextMenu.show(getTree(), x, y);
        }
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu retrieveContextMenu(TreePath clickedElement)
    {
        JPopupMenu contextMenu;

        if (clickedElement != null)
        {
            contextMenu = retrieveElementContextMenu(clickedElement);
        } else
        {
            contextMenu = retrieveTreeContextMenu();
        }

        if (contextMenu != null)
        {
            // This is the code that attempts but fails to shrink the menu to fit the current commands
            // Make sure the size of the menu is up-to-date with any changes made to its actions
            // before display
            contextMenu.invalidate();
            contextMenu.pack();
        }

        return contextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu retrieveElementContextMenu(TreePath clickedElement)
    {
        if (clickedElement == null)
        {
            return null;
        }
        
        Object userObject = ((DefaultMutableTreeNode) clickedElement.getLastPathComponent()).getUserObject();
        FormDataObjIFace dmObject = ((DataModelObjBaseWrapper) userObject).getDataObj();

        if (dmObject instanceof SpecifyUser)
        {
            // object is a user: offer to delete or block the user
            return getUserNodeContextMenu(clickedElement);
        }
        else if (dmObject instanceof SpPrincipal)
        {
            // object is a user group: offer to add new or existing users and to delete the group
            return getGroupNodeContextMenu(clickedElement);
            
        } else if (dmObject instanceof Collection)
        {
            // object is a collection: offer to add new group and to delete the collection
            return getCollectionNodeContextMenu(clickedElement);
            
        }

        return null;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getUserNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Group Context Menu");
        DeleteUserAction deleteUserAction = new DeleteUserAction("User", clickedElement, getTreeMgr());
        deleteUserAction.setEnabled(getTreeMgr().canDeleteUser((DefaultMutableTreeNode) clickedElement.getLastPathComponent()));
        //groupNodeContextMenu.add(deleteUserAction);
        
        delUserBtn.setAction(deleteUserAction);
        //delUserBtn.setEnabled(getTreeMgr().canDeleteUser((DefaultMutableTreeNode) clickedElement.getLastPathComponent()));
        //rmvUserBtn.setEnabled(getTreeMgr().canRemoveUserFromGroup((DefaultMutableTreeNode) clickedElement.getLastPathComponent()));

        RemoveUserFromGroupAction action = new RemoveUserFromGroupAction("User", clickedElement, getTreeMgr());
        action.setEnabled(getTreeMgr().canRemoveUserFromGroup((DefaultMutableTreeNode) clickedElement.getLastPathComponent()));
        //groupNodeContextMenu.add(action);
        rmvUserBtn.setAction(action);

        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getGroupNodeContextMenu(final TreePath clickedElement)
    {
        JPopupMenu groupNodeContextMenu = new JPopupMenu("Group Context Menu");
        
        boolean canAddNewUser = getTreeMgr().canAddNewUser((DefaultMutableTreeNode) clickedElement.getLastPathComponent());
        AddNewUserAction addNewUserAction = new AddNewUserAction(clickedElement, getTreeMgr());
        addNewUserAction.setEnabled(canAddNewUser);
        //groupNodeContextMenu.add(addNewUserAction);
        
        addUserBtn.setAction(addNewUserAction);
        //addUserBtn.setEnabled(canAddNewUser);

        groupNodeContextMenu.add(new AddExistingUserAction(clickedElement, getTreeMgr()));
        
        boolean canDelete = getTreeMgr().canDeleteItem((DefaultMutableTreeNode) clickedElement.getLastPathComponent());
        DeleteUserGroupScopeAction deleteAction = new DeleteUserGroupScopeAction("Group", clickedElement, getTreeMgr());
        deleteAction.setEnabled(canDelete);
        //groupNodeContextMenu.add(deleteAction);
        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getCollectionNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Collection Context Menu");
        // add new group feature disabled temporarily
        //groupNodeContextMenu.add(new AddNewGroupAction(clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    /*private JPopupMenu getDisciplineNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Discipline Context Menu");
        groupNodeContextMenu.add(new AddNewCollectionAction(clickedElement, getTreeMgr()));
        boolean canDelete = getTreeMgr().canDeleteItem((DefaultMutableTreeNode) clickedElement.getLastPathComponent());
        DeleteUserGroupScopeAction deleteAction = new DeleteUserGroupScopeAction("Discipline", clickedElement, getTreeMgr());
        deleteAction.setEnabled(canDelete);
        groupNodeContextMenu.add(deleteAction);
        return groupNodeContextMenu;
    }*/

    /**
     * @param clickedElement
     * @return
     */
    /*private JPopupMenu getInstitutionNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Institution Context Menu");
        groupNodeContextMenu.add(new AddNewDisciplineAction(clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }*/

    /**
     * @return
     */
    private JPopupMenu retrieveTreeContextMenu()
    {
        // no tree context menu for now
        return null;
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

    // -------------------------------------------------------------------
    // -- Inner Classes
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public abstract class NavigationTreeContextMenuAction extends AbstractAction
    {
        private TreePath          clickedObject;
        private NavigationTreeMgr treeManager;

        public NavigationTreeContextMenuAction(String label, 
                                               TreePath clickedObject,
                                               NavigationTreeMgr treeManager)
        {
            super(null);
            this.clickedObject = clickedObject;
            this.treeManager  = treeManager;
        }

        public TreePath getClickedObject()
        {
            return clickedObject;
        }

        public NavigationTreeMgr getTreeMgr()
        {
            return treeManager;
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddNewUserAction extends NavigationTreeContextMenuAction
    {
        public AddNewUserAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super(null, clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addNewUser((DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddExistingUserAction extends NavigationTreeContextMenuAction
    {
        public AddExistingUserAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super(null, clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addExistingUser((DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddNewGroupAction extends NavigationTreeContextMenuAction
    {
        public AddNewGroupAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super(null, clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addNewGroup(
                    (DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddNewCollectionAction extends NavigationTreeContextMenuAction
    {
        public AddNewCollectionAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super(null, clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addNewCollection(
                    (DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddNewDisciplineAction extends NavigationTreeContextMenuAction
    {
        public AddNewDisciplineAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super(null, clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addNewDiscipline((DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
   public class DeleteUserGroupScopeAction extends NavigationTreeContextMenuAction
    {
        public DeleteUserGroupScopeAction(final String itemTypeName, final TreePath clickedObject,
                final NavigationTreeMgr treeMgr)
        {
            // will come up with label at the end of this constructor
            super(null, clickedObject, treeMgr);
            //DefaultMutableTreeNode node = (DefaultMutableTreeNode) getClickedObject().getLastPathComponent();
            //DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
            //putValue(Action.NAME, "Delete " + itemTypeName + " " + wrapper.getName());
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().deleteItem(
                    (DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }
   
   // -------------------------------------------------------------------
   // -- 
   // -------------------------------------------------------------------
   public class DeleteUserAction extends NavigationTreeContextMenuAction
   {
       public DeleteUserAction(final String userName, final TreePath clickedObject,
               final NavigationTreeMgr treeMgr)
       {
           // will come up with label at the end of this constructor
           super(null, clickedObject, treeMgr);
           //DefaultMutableTreeNode node = (DefaultMutableTreeNode) getClickedObject().getLastPathComponent();
           //DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
           //putValue(Action.NAME, "Delete user " + wrapper.getName());
       }

       public void actionPerformed(ActionEvent e)
       {
           getTreeMgr().deleteUser((DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
       }
   }
   
   // -------------------------------------------------------------------
   // -- 
   // -------------------------------------------------------------------
   public class RemoveUserFromGroupAction extends NavigationTreeContextMenuAction
   {
       public RemoveUserFromGroupAction(final String userName, final TreePath clickedObject,
               final NavigationTreeMgr treeMgr)
       {
           // will come up with label at the end of this constructor
           super(null, clickedObject, treeMgr);
           //DefaultMutableTreeNode node = (DefaultMutableTreeNode) getClickedObject().getLastPathComponent();
           //DataModelObjBaseWrapper wrapper = (DataModelObjBaseWrapper) node.getUserObject();
           //putValue(Action.NAME, "Remove user " + wrapper.getName() + " from group");
       }

       public void actionPerformed(ActionEvent e)
       {
           getTreeMgr().removeUserFromGroup((DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
       }
   }
}
