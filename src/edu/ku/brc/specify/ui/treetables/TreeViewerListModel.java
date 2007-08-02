/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.ui.treetables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractListModel;

import org.apache.log4j.Logger;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class TreeViewerListModel extends AbstractListModel
{
    /** Logger for all messages emitted. */
    private static final Logger log = Logger.getLogger(TreeViewerListModel.class);

    protected Vector<TreeNode> nodes = new Vector<TreeNode>(100);
    protected TreeNode visibleRoot;
    protected int visibleSize;
    
    protected Set<Long> idsShowingChildren = new HashSet<Long>();
    
    public TreeViewerListModel(TreeNode rootNode)
    {
        nodes.add(rootNode);
        visibleRoot = rootNode;
        visibleSize = 1;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public synchronized TreeNode getElementAt(int index)
    {
        int visibleRootIndex = nodes.indexOf(visibleRoot);
        TreeNode node = nodes.get(visibleRootIndex + index);
        log.debug("getElementAt(" + index + ") = " + node);
        return node;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public synchronized int getSize()
    {
        // adjust the size for the nodes that are not visible (above the visible root)
        log.debug("getSize() = " + visibleSize);
        return visibleSize;
    }
    
    public synchronized TreeNode getVisibleRoot()
    {
        return visibleRoot;
    }
    
    public synchronized TreeNode getRoot()
    {
        return nodes.get(0);
    }
    
    public synchronized void setVisibleRoot(TreeNode node)
    {
        if (!nodes.contains(node))
        {
            throw new IllegalArgumentException("Passed in node must already be in the TreeViewerListModel");
        }
        
        if (node == visibleRoot)
        {
            // nothing needs to be done
            return;
        }
        
        int startSize = getSize();
        int startingVisRootIndex = nodes.indexOf(visibleRoot);
        int newVisRootIndex      = nodes.indexOf(node);
        int startingLastVisIndex = findLastVisibleIndex();
        TreeNode startingVisRoot = visibleRoot;

        // change the visible root
        visibleRoot = node;
        // and calculate the new size
        int newLastVisIndex = findLastVisibleIndex();
        visibleSize = newLastVisIndex - newVisRootIndex + 1;

        // if this is a "zoom in" operation...
        if (isDescendantOfNode(node, startingVisRoot))
        {
            // notify listeners of the two intervals removed (the one before the new vis root and the one after the new last vis node)

            // the interval at the beginning
            fireIntervalRemoved(this, 0, newVisRootIndex - startingVisRootIndex - 1);
            // the interval at the end
            if (newLastVisIndex != startingLastVisIndex)
            {
                fireIntervalRemoved(this, visibleSize, startSize - (newVisRootIndex - startingVisRootIndex) - 1);
            }
        }
        else // this was a "zoom out" operation
        {
            // notify listeners of the two intervals added (the one before the new vis root and the one after the new last vis node)

            // the interval at the beginning
            fireIntervalAdded(this, 0, startingVisRootIndex - newVisRootIndex -1);
            // the interval at the end
            if (newLastVisIndex != startingLastVisIndex)
            {
                fireIntervalAdded(this, startSize + (startingVisRootIndex - newVisRootIndex), visibleSize - 1);
            }
        }
    }
    
    public synchronized boolean parentHasChildrenAfterNode(TreeNode parent, TreeNode node)
    {
        int nodeIndex = nodes.indexOf(node);
        for (int i = nodeIndex+1; i < nodes.size(); ++i)
        {
            TreeNode n = nodes.get(i);
            if (parent.getId() == n.getParentId())
            {
                return true;
            }
        }
        return false;
    }
    
    public synchronized TreeNode getNodeById(long id)
    {
        for (TreeNode t: nodes)
        {
            if (t.getId() == id)
            {
                return t;
            }
        }
        return null;
    }
    
    /**
     * Determines if the given node is a descendant of the given ancestor.
     * 
     * @param node a possible descendant node
     * @param ancestor the ancestor node
     * @return true if the node is a descendant of the given ancestor
     */
    public synchronized boolean isDescendantOfNode(TreeNode node, TreeNode ancestor)
    {
        if (node == ancestor)
        {
            return true;
        }
        
        TreeNode n = node;
        
        // go until we get to the root node
        while (n.getParentId() != n.getId())
        {
            if (n.getParentId() == ancestor.getId())
            {
                log.debug("isDescendantOfNode( " + node + ", " + ancestor + " ) = true");
                return true;
            }
            n = getNodeById(n.getParentId());
        }
        log.debug("isDescendantOfNode( " + node + ", " + ancestor + " ) = false");
        return false;
    }
    
    /**
     * Based on the current <code>visibleRoot</code>, find the last index of <code>nodes</code> that
     * should be visible in the list.
     * 
     * @return the index in <code>nodes</code> of the last visible node
     */
    protected synchronized int findLastVisibleIndex()
    {
        for (int i = nodes.indexOf(visibleRoot); i < nodes.size(); ++i)
        {
            TreeNode nodeI = nodes.get(i);
            if (!isDescendantOfNode(nodeI, visibleRoot))
            {
                // we found a node that isn't descendant of the visible root
                // so this node shouldn't be visible
                // so the previous node was the last visible node
                return i-1;
            }
        }
        return nodes.size()-1;
    }
    
    /**
     * @param childNodes the set of child nodes to show attached to the given parent
     * @param parent a TreeNode that is currenlt visible below the visible root node
     */
    public synchronized void showChildNodes(List<TreeNode> childNodes, TreeNode parent)
    {
        log.debug("performing addChildNodes( " + childNodes + ", " + parent + ")");
        if (!isDescendantOfNode(parent, visibleRoot))
        {
            return;
        }
        
        if (idsShowingChildren.contains(parent.getId()))
        {
            // ignore calls to "reshow" the children
            return;
        }
        
        idsShowingChildren.add(parent.getId());
        
        int parentIndex = nodes.indexOf(parent);
        nodes.addAll(parentIndex+1, childNodes);
        visibleSize += childNodes.size();
        fireIntervalAdded(this, parentIndex+1, parentIndex+childNodes.size());
    }
    
    /**
     * Returns a sorted list of ranks currently represented in the model data.
     * 
     * @return a sorted list of ranks currently represented in the model data.
     */
    public synchronized List<Integer> getVisibleRanks()
    {
        ArrayList<Integer> visibleRanks = new ArrayList<Integer>();
        int startIndex = nodes.indexOf(visibleRoot);
        for (int i = startIndex; i < startIndex + visibleSize; ++i)
        {
            TreeNode node = nodes.get(i);
            int rank = node.getRank();
            if (!visibleRanks.contains(rank))
            {
                visibleRanks.add(rank);
            }
        }
        Collections.sort(visibleRanks);
        log.debug("getVisibleRanks() = " + visibleRanks);
        return visibleRanks;
    }
    
    public synchronized void removeChildNodes(TreeNode parent)
    {
        log.debug("performing removeChildNodes( " + parent + ")");
     
        int startingSize = nodes.size();
        int parentIndex = nodes.indexOf(parent);
        
        if (parentIndex == -1)
        {
            // this node isn't currently visible
            // ignore this call
            return;
        }
        
        recursivelyRemoveChildNodesInternal(parent);

        int sizeChange = startingSize - nodes.size();
        visibleSize -= sizeChange;
        fireIntervalRemoved(this, parentIndex+1, parentIndex+sizeChange);
    }
    
    protected synchronized void recursivelyRemoveChildNodesInternal(TreeNode parent)
    {
        // this algorithm should work even for nodes that are not showing children
        if (!idsShowingChildren.contains(parent.getId()))
        {
            // but we're going to skip it since it shouldn't need to be done
            return;
        }
        idsShowingChildren.remove(parent.getId());
        
        int parentIndex = nodes.indexOf(parent);
        long parentId = parent.getId();
        
        while (parentIndex+1 < nodes.size())
        {
            TreeNode nextNode = nodes.get(parentIndex+1);
            
            if (nextNode.getParentId() != parentId)
            {
                break;
                // we've moved past the children of the given parent
            }
            // else

            // recursively remove this child's children
            recursivelyRemoveChildNodesInternal(nextNode);
            nodes.remove(nextNode);
        }
        
        return;
    }
    
    public boolean showingChildrenOf(TreeNode node)
    {
        return idsShowingChildren.contains(node.getId());
    }
    
    protected void nodeValuesChanged(TreeNode node)
    {
        // make sure to correct for the fact that we might be viewing just a subtree right now
        int indexOfNode = nodes.indexOf(node) - nodes.indexOf(visibleRoot);
        
        if (indexOfNode >= 0)
        {
            fireContentsChanged(this, indexOfNode, indexOfNode);
        }
    }
}
