package edu.ku.brc.specify.ui;

import javax.swing.tree.TreeNode;


/**
 * A minimal interface that must be met by any TreeModel filter designed
 * to hide child nodes or display only a subtree of the model.
 * 
 * @author jstewart
 */
public interface FilterableTreeModel
{
	/**
	 * Sets the effective root of this TreeModel to be the given node.
	 * 
	 * @param workingRoot the new effective root node
	 */
	public void setWorkingRoot( TreeNode workingRoot );

	/**
	 * Sets the effective root to be the actual root of the model.  This removes
	 * all root-node type filters.
	 */
	public void setWorkingRootToActualRoot();
	
	/**
	 * Hides the given node
	 * 
	 * @param node the node to hide
	 */
	public void hideNode( TreeNode node );
	
	/**
	 * Unhides the given node
	 * 
	 * @param the node to unhide
	 */
	public void unhideNode( TreeNode hiddenNode );
	
	/**
	 * Sets the effective root to be the actual root of the model.  Reveals all
	 * nodes.
	 */
	public void clearAllFilters();
}
