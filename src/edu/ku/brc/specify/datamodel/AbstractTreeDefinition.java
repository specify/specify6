/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Set;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public abstract class AbstractTreeDefinition implements TreeDefinitionIface
{
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given rank.
	 * 
	 * @param rank the rank of the returned def item
	 * @return the definition item
	 */
	@SuppressWarnings("unchecked")
    public TreeDefinitionItemIface getDefItemByRank(Integer rank)
	{
		Set<TreeDefinitionItemIface> defItems = (Set<TreeDefinitionItemIface>)getTreeDefItems();
		for( TreeDefinitionItemIface item: defItems )
		{
			if( item.getRankId().equals(rank) )
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given name.
	 * 
	 * @param name the name of the returned def item
	 * @return the definition item
	 */
	@SuppressWarnings("unchecked")
	public TreeDefinitionItemIface getDefItemByName(String name)
	{
		Set<TreeDefinitionItemIface> defItems = (Set<TreeDefinitionItemIface>)getTreeDefItems();
		for( TreeDefinitionItemIface item: defItems )
		{
			if( item.getName().equals(name) )
			{
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Determines if the child node can be reparented to newParent while not
	 * violating any of the business rules.  Currently, the only rule on
	 * reparenting is that the new parent must be of rank equal to or less than
	 * the next higher enforced rank in the child's tree definition.
	 * 
	 * @param child the node to be reparented
	 * @param newParent the prospective new parent node
	 * 
	 * @return <code>true</code> if the action will not violate any reparenting rules, false otherwise
	 */
	public boolean canChildBeReparentedToNode( Treeable child, Treeable newParent )
	{
		if( newParent.getRankId().intValue() >= child.getRankId().intValue() )
		{
			// a node cannot have a parent that is a peer or of lower rank (larger rank id)
			return false;
		}
		
		Integer nextEnforcedRank = getRankOfNextHighestEnforcedLevel(child);
		if( nextEnforcedRank == null )
		{
			// no higher ranks are being enforced
			// the node can be reparented all the way up to the root
			return true;
		}
		
		if( nextEnforcedRank.intValue() <= newParent.getRankId().intValue() )
		{
			// the next enforced rank is equal to or above the new parent rank
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the next highest rank in the tree that is enforced by the
	 * tree definition.
	 * 
	 * @param node the node to find the next highest enforced rank for
	 * @return the next highest rank
	 */
	public Integer getRankOfNextHighestEnforcedLevel( Treeable node )
	{
		TreeDefinitionItemIface defItem = node.getDefItem();
		while( defItem.getParentItem() != null )
		{
			defItem = defItem.getParentItem();
			if( defItem.getIsEnforced() != null && defItem.getIsEnforced().booleanValue() == true )
			{
				return defItem.getRankId();
			}
		}
		
		return null;
	}
}
