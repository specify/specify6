/*
* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package edu.ku.brc.specify.ui.containers;

import java.awt.Point;
import java.awt.dnd.DnDConstants;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * Taken from: 
 *  http://forums.sun.com/thread.jspa?threadID=296255&start=0
 * 
 * @author denis
 * 
 * @code_status Alpha
 * 
 * Oct 25, 2010
 * 
 */
public class DefaultTreeTransferHandler extends AbstractTreeTransferHandler
{

    /**
     * @param tree
     * @param action
     */
    public DefaultTreeTransferHandler(GhostActionableTree tree, int action)
    {
        super(tree, action, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.containers.AbstractTreeTransferHandler#canPerformAction(edu.ku.brc.specify.ui.containers.GhostActionableTree, javax.swing.tree.DefaultMutableTreeNode, int, java.awt.Point)
     */
    public boolean canPerformActionX(GhostActionableTree target,
                                    DefaultMutableTreeNode draggedNode,
                                    int action,
                                    Point location)
    {
        TreePath pathTarget = target.getPathForLocation(location.x, location.y);
        if (pathTarget == null)
        {
            target.setSelectionPath(null);
            return (false);
        }
        
        target.setSelectionPath(pathTarget);
        if (action == DnDConstants.ACTION_COPY)
        {
            return (false);
        }
        
        if (action == DnDConstants.ACTION_MOVE)
        {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) pathTarget
                    .getLastPathComponent();
            if (draggedNode.isRoot() || parentNode == draggedNode.getParent()
                    || draggedNode.isNodeDescendant(parentNode))
            {
                return (false);
            } else
            {
                return (true);
            }
        } else
        {
            return (false);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.containers.AbstractTreeTransferHandler#canPerformAction(edu.ku.brc.specify.ui.containers.GhostActionableTree, javax.swing.tree.DefaultMutableTreeNode, int, java.awt.Point)
     */
    public boolean canPerformAction(GhostActionableTree target,
                                    DefaultMutableTreeNode draggedNode,
                                    int action,
                                    Point location)
    {
        TreePath pathTarget = target.getPathForLocation(location.x, location.y);
        if (pathTarget == null)
        {
            target.setSelectionPath(null);
            return (false);
        }
        
        DefaultMutableTreeNode hoverNode = (DefaultMutableTreeNode) pathTarget.getLastPathComponent();
        if (hoverNode.isLeaf())
        { // or ((DefaultMutableTreeNode)pathTarget.getLastPathComponent()).getChildCount()==0
            
            if (hoverNode.getUserObject() instanceof CollectionObject)
            {
                target.setSelectionPath(null);
                return (false);
            }
        }
        
        if (action == DnDConstants.ACTION_COPY)
        {
            //target.setSelectionPath(pathTarget);
            //return (true);
            target.setSelectionPath(null);
            return (false);
        }

        if (action == DnDConstants.ACTION_MOVE)
        {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)pathTarget.getLastPathComponent();
            if (draggedNode.isRoot() || parentNode == draggedNode.getParent()
                    || draggedNode.isNodeDescendant(parentNode))
            {
                target.setSelectionPath(null);
                return (false);
            } else
            {
                target.setSelectionPath(pathTarget);
                return (true);
            }
        } else
        {
            target.setSelectionPath(null);
            return (false);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.containers.AbstractTreeTransferHandler#executeDrop(edu.ku.brc.specify.ui.containers.GhostActionableTree, javax.swing.tree.DefaultMutableTreeNode, javax.swing.tree.DefaultMutableTreeNode, int)
     */
    public boolean executeDrop(GhostActionableTree target,
                               DefaultMutableTreeNode draggedNode,
                               DefaultMutableTreeNode newParentNode,
                               int action)
    {
        if (action == DnDConstants.ACTION_COPY)
        {
            DefaultMutableTreeNode newNode = target.makeDeepCopy(draggedNode);
            ((DefaultTreeModel) target.getModel()).insertNodeInto(newNode, newParentNode,
                    newParentNode.getChildCount());
            TreePath treePath = new TreePath(newNode.getPath());
            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);
            return (true);
        }
        if (action == DnDConstants.ACTION_MOVE)
        {
            draggedNode.removeFromParent();
            ((DefaultTreeModel) target.getModel()).insertNodeInto(draggedNode, newParentNode,
                    newParentNode.getChildCount());
            TreePath treePath = new TreePath(draggedNode.getPath());
            target.scrollPathToVisible(treePath);
            target.setSelectionPath(treePath);
            return (true);
        }
        return (false);
    }
    
}
