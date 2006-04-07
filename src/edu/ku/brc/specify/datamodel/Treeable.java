package edu.ku.brc.specify.datamodel;

/**
 * Describes any class where a collection of its objects can be modeled as
 * a tree.  Each instance of the implementing class represents a single node
 * in a tree.  Database tables can contain multiple trees simultaneously as
 * long as each node in a given tree has a common identifier, the series id.
 * Each node in the tree must have a unique ID, which is the primary key of
 * the corresponding database table.  Each node must also be numbered (the
 * node number) using a depth-first traversal of the tree.  The highest child
 * node number field contains the largest node number in the tree that is a
 * descendant of the given node.  The rank id represents the nodes depth in
 * the tree.  Possible depths are defined in the tree definition.
 * 
 * @author jstewart
 */
public interface Treeable extends Comparable<Treeable>
{
	/**
	 * @return the ID (primary key) of this node
	 */
	public Integer getTreeId();
	
	/**
	 * Sets the ID of this node
	 * 
	 * @param id the new ID value
	 */
	public void setTreeId(Integer id);
	
	/**
	 * Returns the parent node object.  If called on the root node of
	 * the tree, returns null.
	 * 
	 * @return the parent node object
	 */
	public Treeable getParentNode();
	
	/**
	 * Re-parents the node by setting its parent to <code>node</code>.
	 * 
	 * @param node the new parent
	 */
	public void setParentNode(Treeable node);
	
	/**
	 * @return the node number as determined by a depth-first traversal of the containing tree
	 */
	public Integer getNodeNumber();
	
	/**
	 * Sets the depth-first traversal node number of this object
	 * 
	 * @param nodeNumber
	 */
	public void setNodeNumber(Integer nodeNumber);
	
	/**
	 * @return the node number of the descdendant having the largest node number
	 */
	public Integer getHighestChildNodeNumber();
	
	/**
	 * @param nodeNumber the node number of the descdendant having the largest node number
	 */
	public void setHighestChildNodeNumber(Integer nodeNumber);
	
	/**
	 * @return the name of this node
	 */
	public String getName();
	
	/**
	 * @param name the new name of the node
	 */
	public void setName(String name);
	
	/**
	 * @return the rank (tree level) of this node
	 */
	public Integer getRankId();
	
	/**
	 * @param id the new rank (tree level) of this node
	 */
	public void setRankId(Integer id);
	
	/**
	 * @return the series ID of the tree containing this node
	 */
	public TreeDefinitionIface getTreeDef();
	
	/**
	 * @param id the new series ID of the tree that this node is contained in
	 */
	public void setTreeDef(TreeDefinitionIface id);
}
