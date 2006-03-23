package edu.ku.brc.specify.ui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;


/**
 * A DefaultTreeModel with capabilities for setting the effective root node and
 * hiding non-root nodes.
 * 
 * @author jstewart
 */
public class FilteredDefaultTreeModel extends DefaultTreeModel implements FilterableTreeModel
{
	protected TreeNode workingRoot;
	protected Vector<TreeNode> hiddenChildren;
	
	/**
	 * Creates a tree specifying whether any node can have children, or whether
	 * only certain nodes can have children.
	 * 
	 * @param root a TreeNode object that is the root of the tree
	 * @param asksAllowsChildren a boolean, false if any node can have children,
	 * 				true if each node is asked to see if it can have children
	 */
	public FilteredDefaultTreeModel(TreeNode root, boolean asksAllowsChildren)
	{
		super(root, asksAllowsChildren);
		init();
	}

	/**
	 * Creates a tree in which any node can have children.
	 * 
	 * @param root a TreeNode object that is the root of the tree
	 */
	public FilteredDefaultTreeModel(TreeNode root)
	{
		super(root);
		init();
	}

	private void init()
	{
		this.workingRoot = this.root;
		hiddenChildren = new Vector<TreeNode>();
	}
	
	/**
	 * Sets the effective root of this TreeModel to be the given node.
	 * 
	 * @param workingRoot the new effective root node
	 * 
	 * @see edu.ku.brc.specify.ui.FilterableTreeModel#setWorkingRoot(javax.swing.tree.TreeNode)
	 */
	public void setWorkingRoot( TreeNode workingRoot )
	{
		TreeNode prevWorkRoot = this.workingRoot;
		this.workingRoot = workingRoot;
		nodeStructureChanged(prevWorkRoot);
	}
	
	/**
	 * @return the effective root node
	 */
	public TreeNode getWorkingRoot()
	{
		return this.workingRoot;
	}
	
	/**
	 * Hides the given node
	 * 
	 * @param node the node to hide
	 * 
	 * @see edu.ku.brc.specify.ui.FilterableTreeModel#hideNode(javax.swing.tree.TreeNode)
	 */
	public void hideNode( TreeNode node )
	{
		hiddenChildren.add(node);
		nodeStructureChanged(node.getParent());
	}
	
	/**
	 * Unhides the given node
	 * 
	 * @param the node to unhide
	 * 
	 * @see edu.ku.brc.specify.ui.FilterableTreeModel#unhideNode(javax.swing.tree.TreeNode)
	 */
	public void unhideNode( TreeNode node )
	{
		hiddenChildren.remove(node);
		nodeStructureChanged(node.getParent());
	}

	/**
	 * Sets the effective root to be the actual root of the model.  This removes
	 * all root-node type filters.
	 * 
	 * @see edu.ku.brc.specify.ui.FilterableTreeModel#setWorkingRootToActualRoot()
	 */
	public void setWorkingRootToActualRoot()
	{
		this.workingRoot = this.root;
		nodeStructureChanged(this.root);
	}
	
	/**
	 * Sets the effective root to be the actual root of the model.  Reveals all
	 * nodes.
	 * 
	 * @see edu.ku.brc.specify.ui.FilterableTreeModel#clearAllFilters()
	 */
	public void clearAllFilters()
	{
		Iterator<TreeNode> nodes = hiddenChildren.iterator();
		while( nodes.hasNext() )
		{
			TreeNode n = nodes.next();
			nodes.remove();
			nodeStructureChanged(n);
		}
		setWorkingRootToActualRoot();
	}
	
	/**
	 * Returns the effective root of the model
	 * 
	 * @return the effective root of the model (a TreeNode)
	 * 
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot()
	{
		return workingRoot;
	}

	/**
	 * Returns the child of <i>parent</i> at index <i>index</i> in the parent's
	 * visible child array.
	 * 
	 * @return the child
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index)
	{
		return (getVisibleChildren((TreeNode)parent)).elementAt(index);
	}

	/**
	 * Returns the number of visible children of <i>parent</i>.  Returns 0
	 * if the node is a leaf or if it has no visible children (in which case
	 * it will be treated as a leaf).
	 * 
	 * @return the number of visible children
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent)
	{
		// TODO Auto-generated method stub
		Vector<TreeNode> visibleChildren = getVisibleChildren((TreeNode)parent);
		if( visibleChildren == null )
		{
			return 0;
		}
		
		return visibleChildren.size();
	}
	
	/**
	 * Creates and returns a vector of all visible children of <i>parent</i>.
	 * 
	 * @param parent the parent node
	 * @return a vector containing all of the visible children
	 */
	private Vector<TreeNode> getVisibleChildren( TreeNode parent )
	{
		if( hiddenChildren.contains(parent) )
		{
			return null;
		}
		
		Vector<TreeNode> visibles = new Vector<TreeNode>();
		for( int i = 0; i < parent.getChildCount(); ++i )
		{
			TreeNode child = parent.getChildAt(i);
			if( !hiddenChildren.contains(child) )
			{
				visibles.add(child);
			}
		}
		
		return visibles;
	}

	/**
	 * Returns true if the given node has no visible children.
	 * 
	 * @return true is <i>node</i> is a leaf, false otherwise
	 * 
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node)
	{
		return this.getChildCount(node) == 0 ? true : false;
	}

	/**
	 * Returns the index of <i>child</i> in the visible child array of <i>parent</i>
	 * 
	 * @param parent the parent node
	 * @param child the child node
	 * @return the index of <i>child</i> in the visible child array
	 * 
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		Vector<TreeNode> visibles = getVisibleChildren((TreeNode)parent);
		return visibles.indexOf(child);
	}
}
