package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Institution;
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

    /**
     * Constructor. Attaches itself as tree selection and mouse listener.
     * 
     * @param tree Tree
     */
    public NavigationTreeContextMenuMgr(final NavigationTreeMgr treeMgr)
    {
        this.treeMgr = treeMgr;
        getTree().addTreeSelectionListener(this);
        getTree().addMouseListener(this);
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
        if (e.isPopupTrigger() && e.getClickCount() == 1)
        {
            doPopup(e.getX(), e.getY());
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger() && e.getClickCount() == 1)
        {
            doPopup(e.getX(), e.getY());
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

        if (dmObject instanceof SpPrincipal)
        {
            // object is a user group: offer to add new or existing users and to delete the group
            return getGroupNodeContextMenu(clickedElement);
            
        } else if (dmObject instanceof Collection)
        {
            // object is a collection: offer to add new group and to delete the collection
            return getCollectionNodeContextMenu(clickedElement);
            
        } else if (dmObject instanceof Discipline)
        {
            // object is a discipline: offer to add new collection and to delete the discipline
            return getDisciplineNodeContextMenu(clickedElement);
            
        } else if (dmObject instanceof Institution)
        {
            // object is a user group: offer to add new discipline
            return getInstitutionNodeContextMenu(clickedElement);
        }

        return null;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getGroupNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Group Context Menu");
        groupNodeContextMenu.add(new LabelAction("Operations on " + clickedElement.toString()));
        groupNodeContextMenu.add(new JPopupMenu.Separator());
        groupNodeContextMenu.add(new AddNewUserAction(clickedElement, getTreeMgr()));
        groupNodeContextMenu.add(new AddExistingUserAction(clickedElement, getTreeMgr()));
        groupNodeContextMenu.add(new DeleteItemAction("Group", clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getCollectionNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Collection Context Menu");
        groupNodeContextMenu.add(new LabelAction("Operations on " + clickedElement.toString()));
        groupNodeContextMenu.add(new JPopupMenu.Separator());
        groupNodeContextMenu.add(new AddNewGroupAction(clickedElement, getTreeMgr()));
        groupNodeContextMenu.add(new DeleteItemAction("Collection", clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getDisciplineNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Discipline Context Menu");
        groupNodeContextMenu.add(new LabelAction("Operations on " + clickedElement.toString()));
        groupNodeContextMenu.add(new JPopupMenu.Separator());
        groupNodeContextMenu.add(new AddNewCollectionAction(clickedElement, getTreeMgr()));
        groupNodeContextMenu.add(new DeleteItemAction("Discipline", clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }

    /**
     * @param clickedElement
     * @return
     */
    private JPopupMenu getInstitutionNodeContextMenu(final TreePath clickedElement)
    {

        JPopupMenu groupNodeContextMenu = new JPopupMenu("Institution Context Menu");
        groupNodeContextMenu.add(new LabelAction("Operations on " + clickedElement.toString()));
        groupNodeContextMenu.add(new JPopupMenu.Separator());
        groupNodeContextMenu.add(new AddNewDisciplineAction(clickedElement, getTreeMgr()));
        return groupNodeContextMenu;
    }

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
    public class LabelAction extends AbstractAction
    {
        LabelAction(final String label)
        {
            super(label);
        }

        public void actionPerformed(ActionEvent e)
        {
            // do nothing
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public abstract class NavigationTreeContextMenuAction extends AbstractAction
    {
        private TreePath          clickedObject;
        private NavigationTreeMgr treeManager;

        public NavigationTreeContextMenuAction(String label, TreePath clickedObject,
                NavigationTreeMgr treeManager)
        {
            super(label);
            this.clickedObject = clickedObject;
            this.treeManager = treeManager;
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
            super("Add New User", clickedObject, treeMgr);
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
            super("Add Existing User...", clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            DefaultMutableTreeNode  node  = (DefaultMutableTreeNode) getClickedObject().getLastPathComponent();
            DataModelObjBaseWrapper wrp   = (DataModelObjBaseWrapper) node.getUserObject();
            SpPrincipal             group = (SpPrincipal) wrp.getDataObj();
            AddExistingUserDlg      dlg   = new AddExistingUserDlg(null, group);
            dlg.setVisible(true);
            SpecifyUser[] user = dlg.getSelectedUsers();
            getTreeMgr().addExistingUser((DefaultMutableTreeNode) getClickedObject().getLastPathComponent(), user);
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
    public class AddNewGroupAction extends NavigationTreeContextMenuAction
    {
        public AddNewGroupAction(final TreePath clickedObject, final NavigationTreeMgr treeMgr)
        {
            super("Add New Group", clickedObject, treeMgr);
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
            super("Add New Collection", clickedObject, treeMgr);
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
            super("Add New Discipline", clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().addNewDiscipline(
                    (DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }

    // -------------------------------------------------------------------
    // -- 
    // -------------------------------------------------------------------
   public class DeleteItemAction extends NavigationTreeContextMenuAction
    {
        public DeleteItemAction(final String itemTypeName, final TreePath clickedObject,
                final NavigationTreeMgr treeMgr)
        {
            super("Delete " + itemTypeName + " " + clickedObject.toString(), clickedObject, treeMgr);
        }

        public void actionPerformed(ActionEvent e)
        {
            getTreeMgr().deleteItem(
                    (DefaultMutableTreeNode) getClickedObject().getLastPathComponent());
        }
    }
}
