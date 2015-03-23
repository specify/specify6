/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui.containers;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 19, 2010
 *
 */
public class GhostActionableTree extends JTree implements GhostActionable, TreeExpansionListener
{
    private List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    private DefaultMutableTreeNode selectedNode = null;
    private Color                  clearColor = new Color(255, 255, 255, 255);
    private ContainerTreePanel     treePanel;
    
    // For Tracking Collaping and Expanding
    private HashSet<Object> expandedTreeObjects   = new HashSet<Object>();
    private boolean         supressExpansionEvent = false;
    
    private Insets autoscrollInsets = new Insets(20, 20, 20, 20); // insets
    
    /**
     * 
     */
    public GhostActionableTree(final ContainerTreePanel treePanel)
    {
        super();
        this.treePanel = treePanel;
        init();
    }

    /**
     * @param newModel
     */
    public GhostActionableTree(final ContainerTreePanel treePanel,
                               final TreeModel newModel)
    {
        super(newModel);
        this.treePanel = treePanel;
        init();
    }
    
    /**
     * 
     */
    private void init()
    {
        dropFlavors.add(new DataFlavorTableExt(GhostActionableTree.class, RecordSetTask.RECORD_SET, CollectionObject.getClassTableId()));
        
        getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                if (e.getPath() != null)
                {
                    selectedNode = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
                }
            }
        });
        
        addTreeExpansionListener(this);
        setBorder(BorderFactory.createLineBorder(clearColor, 2));
        
        setAutoscrolls(true);
        setShowsRootHandles(false);//to show the root icon
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); //set single selection for the Tree
        setEditable(false);
        
        new DefaultTreeTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE);

    }

    /* (non-Javadoc)
     * @see javax.swing.JTree#setModel(javax.swing.tree.TreeModel)
     */
    @Override
    public void setModel(TreeModel newModel)
    {
        if (expandedTreeObjects != null)
        {
            expandedTreeObjects.clear();
        }
        super.setModel(newModel);
    }

    /**
     * @param clearColor the clearColor to set
     */
    public void setClearColor(Color clearColor)
    {
        this.clearColor = clearColor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    @Override
    public void createMouseInputAdapter()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    @Override
    public void doAction(final GhostActionable source)
    {
        Object dataObj = source.getData();
        if (dataObj instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)dataObj;
            treePanel.addRecordSet(rs);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    @Override
    public BufferedImage getBufferedImage()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    @Override
    public Object getData()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    @Override
    public Object getDataForClass(final Class<?> classObj)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    @Override
    public List<DataFlavor> getDragDataFlavors()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDropDataFlavors()
     */
    @Override
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getMouseInputAdapter()
     */
    @Override
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    @Override
    public void setActive(boolean isActive)
    {
        boolean isSelectedOK = selectedNode != null && (selectedNode.getUserObject() instanceof Container);
        boolean isTreeOK     = selectedNode == null && this.getRowCount() == 1;
        boolean isOK = isActive && (isTreeOK || isSelectedOK);
        
        
        
        // GhostActionableTree Use the RolloverCommand active color
        setBorder(BorderFactory.createLineBorder(isOK ? RolloverCommand.getActiveColor() : clearColor, 2));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    @Override
    public void setData(Object data)
    {
    }
    
    /**
     * @return the selectedNode
     */
    public DefaultMutableTreeNode getSelectedNode()
    {
        return selectedNode;
    }

    /**
     * 
     */
    public void cleanUp()
    {
        treePanel = null;
    }
    
    //---------------------------------------------------------------------------------
    //-- DnD Tree Code from Denis
    //   http://forums.sun.com/thread.jspa?threadID=296255&start=0
    //---------------------------------------------------------------------------------

    public void autoscroll(Point cursorLocation)
    {
        Insets insets = getAutoscrollInsets();
        Rectangle outer = getVisibleRect();
        Rectangle inner = new Rectangle(outer.x + insets.left, outer.y + insets.top, outer.width
                - (insets.left + insets.right), outer.height - (insets.top + insets.bottom));
        if (!inner.contains(cursorLocation))
        {
            Rectangle scrollRect = new Rectangle(cursorLocation.x - insets.left, cursorLocation.y
                    - insets.top, insets.left + insets.right, insets.top + insets.bottom);
            scrollRectToVisible(scrollRect);
        }
    }

    public Insets getAutoscrollInsets()
    {
        return (autoscrollInsets);
    }

    public static DefaultMutableTreeNode makeDeepCopy(DefaultMutableTreeNode node)
    {
        DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node.getUserObject());
        for (Enumeration<?> e = node.children(); e.hasMoreElements();)
        {
            copy.add(makeDeepCopy((DefaultMutableTreeNode) e.nextElement()));
        }
        return (copy);
    }
    
    //---------------------------------------------------------------------------------
    //-- Tracking Expanding and Collapsing tree
    //---------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
     */
    @Override
    public void treeCollapsed(TreeExpansionEvent e)
    {
        TreePath path     = (TreePath)e.getPath(); // Get the tree path
        Object[] objsPath = path.getPath(); // Get all the objects within that path
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) objsPath[objsPath.length - 1]; // Derive a node
        Object userObj = (Object) node.getUserObject(); // This will always be a String
        expandedTreeObjects.remove(userObj);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
     */
    @Override
    public void treeExpanded(TreeExpansionEvent e)
    {
        if (!supressExpansionEvent) { // Not interested in this event if we are currently restoring the tree
            
            TreePath path     = (TreePath)e.getPath(); // Get the tree path
            Object[] objsPath = path.getPath();        // Get all the objects within that path
            if (objsPath.length > 0)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)objsPath[objsPath.length - 1]; // Derive a node
                Object userObject = (Object)node .getUserObject(); // This will always be a Object Class object            
                expandedTreeObjects.add(userObject); // Place the object in my list
            }
        }
    }
    
    /*private void refreshTree()
    {
        System.out.println("Refresh Tree Started");
        
        // The refresh tree will cause expansion events to occur.
        // This flag will control if we respond to such an expansion event. If we are refreshing the tree, we don't want to.'
        supressExpansionEvent = true;
        rootNode.removeAllChildren();
        
        loadTree();
        treeModel.reload();
        restoreTree();
        
        supressExpansionEvent = false; // Now we can go back to responding normally to expansion events
        
        System.out.println("Refresh Tree End");
        
        
    }*/

    /**
     * 
     */
    public void restoreTree()
    {
        // Process tree nodes from root node
        restoreTreeNode(this, new TreePath(getModel().getRoot()), null); // Not surprisingly, rootNode is my
                                                                         // rootNode
    }
    
    /**
     * @param tree
     * @param parent
     * @param treeNode
     */
    @SuppressWarnings("unchecked")
    private void restoreTreeNode(JTree tree, TreePath parent, DefaultMutableTreeNode treeNode) 
    {
        // Traverse down through the children
        TreeNode node = (TreeNode) parent.getLastPathComponent(); // Get the last TreeNode component for this path
        
        if (node.getChildCount() >= 0) { // If the node have children?
            
            // Create a child numerator over the node
            Enumeration<TreeNode> en = node.children();            
            while (en.hasMoreElements()) // While we have children 
            { 
                DefaultMutableTreeNode dmTreeNode = (DefaultMutableTreeNode)en.nextElement(); // Derive the node
                TreePath path = parent.pathByAddingChild(dmTreeNode); // Derive the path
                restoreTreeNode(tree, path, dmTreeNode); // Recursive call with new path
                
            } // End While we have more children
            
        } // End If the node have children?
        
        // Nodes need to be expanded from last branch node up
        
        
        if (treeNode != null) { // If true, this is the root node - ignore it
            
            Object myUserObject = (Object) treeNode.getUserObject(); // Get the user object from the node
                                                                       // Note - all the objects I place in tree nodes 
                                                                       // belong to the same class - Object
            
            if (expandedTreeObjects.contains(myUserObject)) { // Is this present on the previously expanded list?
                
                tree.expandPath(parent); // et viola
            } 
            
        } // End If - root node
        
    }
}
