package edu.ku.brc.specify.helpers;

import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * This class provides many static methods that simplify the management
 * of tree-structured table data.  Many of the methods are used in
 * determining if business rules are being properly enforced.
 * 
 * @author jstewart
 *
 */
public class TreeTableUtils
{
    protected static Log log = LogFactory.getLog(TreeTableUtils.class);

	public static final int FORWARD = 1;
	public static final int REVERSE = -1;
	
	//TODO: move these to prefs
	//XXX: pref
	public static int getFullNameDirection( Class treeableClass )
	{
		if( treeableClass.equals(Geography.class) )
		{
			return REVERSE;
		}
		if( treeableClass.equals(GeologicTimePeriod.class) )
		{
			return REVERSE;
		}
		if( treeableClass.equals(Location.class) )
		{
			return REVERSE;
		}
		if( treeableClass.equals(Taxon.class) )
		{
			return FORWARD;
		}
		
		return 0;
	}
	
	//TODO: move these to prefs
	//XXX: pref
	public static String getFullNameSeparator( Class treeableClass )
	{
		if( treeableClass.equals(Geography.class) )
		{
			return ", ";
		}
		if( treeableClass.equals(GeologicTimePeriod.class) )
		{
			return ", ";
		}
		if( treeableClass.equals(Location.class) )
		{
			return ", ";
		}
		if( treeableClass.equals(Taxon.class) )
		{
			return " ";
		}
		
		return " ";
	}
	
	public static String getFullName( Treeable node )
	{
		Vector<String> parts = new Vector<String>();
		parts.add(node.getName());
		Treeable parent = node.getParentNode();
		while( parent != null )
		{
			Boolean include = parent.getDefItem().getIsInFullName();
			if( include != null && include.booleanValue() == true )
			{
				parts.add(parent.getName());
			}
			
			parent = parent.getParentNode();
		}
		
		int direction = TreeTableUtils.getFullNameDirection(node.getClass());
		String sep = TreeTableUtils.getFullNameSeparator(node.getClass());
		
		StringBuilder fullName = new StringBuilder(parts.size() * 10);
		
		switch( direction )
		{
			case FORWARD:
			{
				for( int i = parts.size()-1; i > -1; --i )
				{
					fullName.append(parts.get(i));
					fullName.append(sep);
				}
				break;
			}
			case REVERSE:
			{
				for( int i = 0; i < parts.size(); ++i )
				{
					fullName.append(parts.get(i));
					fullName.append(sep);
				}
				break;
			}
			default:
			{
				log.error("Invalid tree walk direction (for creating fullname field) found in tree definition");
				return null;
			}
		}
		
		fullName.delete(fullName.length()-sep.length(), fullName.length());
		return fullName.toString();
	}
	
	/**
	 * @param item the node to examine
	 * @return the rank of children of this node, or null if no children are allowed by the node's tree definition
	 */
	public static Integer getRankOfChildren( Treeable item )
	{
		TreeDefinitionItemIface defItem = item.getDefItem();
		if( defItem == null )
		{
			return null;
		}

		// Tree def items are NOT lazy loaded, so we don't need to setup a HBM session
		//Session session = HibernateUtil.getCurrentSession();
		//session.lock(defItem, LockMode.NONE);
		TreeDefinitionItemIface childDefItem = defItem.getChildItem();
		//Hibernate.initialize(childDefItem);
		//HibernateUtil.closeSession();

		if( childDefItem == null )
		{
			return null;
		}
		
		return childDefItem.getRankId();
	}
	
	/**
	 * @param item the node to examine
	 * @return true if children are allowed as defined by the node's tree definition, false otherwise
	 */
	public static boolean childrenAllowed( Treeable item )
	{
		return (getRankOfChildren(item) == null) ? false : true;
	}
	
	/**
	 * @param parent the parent node of the new child
	 * @param newChild the new child node
	 * @return the index in the array of parent's current children at which to insert newChild
	 * 			in order to keep the parent's child array sorted
	 */
	public static int findIndexOfNewChild( DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild )
	{
		// find out where to insert this node in order to keep the model sorted
		if( parent.getChildCount() == 0 )
		{
			return 0;
		}
		else
		{
			String newChildName = ((Treeable)newChild.getUserObject()).getName();

			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getFirstChild();
			String childName = ((Treeable)child.getUserObject()).getName();
			
			int index = 0;
			while (newChildName.toLowerCase().compareTo(childName.toLowerCase()) > 0 )
			{
				++index;
				child = (DefaultMutableTreeNode)child.getNextSibling();
				if( child == null )
				{
					break;
				}
				childName = ((Treeable)child.getUserObject()).getName();
			}
			return index;
		}
	}

	@SuppressWarnings("unchecked")
    public static TreeDefinitionItemIface getDefItemByRank( TreeDefinitionIface treeDef, Integer rank )
	{
		Set<TreeDefinitionItemIface> defItems = (Set<TreeDefinitionItemIface>)treeDef.getTreeDefItems();
		for( TreeDefinitionItemIface item: defItems )
		{
			if( item.getRankId().equals(rank) )
			{
				return item;
			}
		}
		return null;
	}
	
	public static Integer getHighestAllowableParentRank( Treeable t )
	{
		// if this item represents the tree root level, return null
		if( t.getDefItem().getParentItem() == null )
		{
			return null;
		}
		
		TreeDefinitionItemIface defItem = t.getDefItem().getParentItem();
		while(true)
		{
			if( defItem.getIsEnforced().booleanValue() || defItem.getParentItem() == null )
			{
				return defItem.getRankId();
			}
		}
	}
	
	public static boolean isReparentAllowed( Treeable node, Treeable newParent )
	{
		if( newParent.getRankId().intValue() < node.getHighestChildNodeNumber().intValue() )
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the given Treeable can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, true is returned.
	 * 
	 * @param treeable the node to examine
	 * @return true if it has no FK contraints barring it from deletion, false otherwise
	 */
	public static boolean canBeDeleted( Treeable treeable )
	{
		if( treeable instanceof Geography )
		{
			return geographyCanBeDeleted((Geography)treeable);
		}
		else if( treeable instanceof GeologicTimePeriod )
		{
			return geologicTimePeriodCanBeDeleted((GeologicTimePeriod)treeable);
		}
		else if( treeable instanceof Location )
		{
			return locationCanBeDeleted((Location)treeable);
		}
		else if( treeable instanceof Taxon )
		{
			return taxonCanBeDeleted((Taxon)treeable);
		}
		
		return false;
	}
	
	protected static boolean geographyCanBeDeleted( Geography geo )
	{
		return false;
	}

	protected static boolean geologicTimePeriodCanBeDeleted( GeologicTimePeriod gtp )
	{
		return false;
	}

	protected static boolean locationCanBeDeleted( Location loc )
	{
		return false;
	}

	protected static boolean taxonCanBeDeleted( Taxon taxon )
	{
		return false;
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
	 * @return true if the action will not violate any reparenting rules, false otherwise
	 */
	public static boolean canChildBeReparentedToNode( Treeable child, Treeable newParent )
	{
		Integer nextEnforcedRank = getRankOfNextHighestEnforcedLevel(child);
		if( nextEnforcedRank == null )
		{
			// no higher ranks are being enforced
			// the node can be reparented all the way up to the root
			return true;
		}
		
		if( nextEnforcedRank.intValue() >= newParent.getRankId().intValue() )
		{
			// the next enforced rank is equal to or above the new parent rank
			return true;
		}
		
		return false;
	}
	
	public static Integer getRankOfNextHighestEnforcedLevel( Treeable node )
	{
		TreeDefinitionItemIface defItem = node.getDefItem();
		while( defItem.getParentItem() != null )
		{
			defItem = defItem.getParentItem();
			if( defItem.getIsEnforced().booleanValue() == true )
			{
				return defItem.getRankId();
			}
		}
		
		return null;
	}

	/**
	 * @param node 
	 * @return the number of nodes on the path from node (exclusive) to the root (inclusive if root!=node)
	 */
	public static int numberOfNodesToRoot( Treeable node )
	{
		if( node == null )
		{
			throw new NullPointerException();
		}
		
		int count = 0;
		Treeable t = node.getParentNode();
		while( t != null )
		{
			++count;
			t = node.getParentNode();
		}
		return count;
	}
	
	/**
	 * @param t the possible descendant node
	 * @param s the possible ancestor node
	 * @return true if t is a descendant of s
	 */
	public static boolean nodeIsDescendantOfNode( Treeable t, Treeable s )
	{
		if( t==null || s==null )
		{
			throw new NullPointerException();
		}
		
		Treeable i = t.getParentNode();
		while( i != null )
		{
			if( i == s )
			{
				return true;
			}
			
			i = i.getParentNode();
		}
		return false;
	}

	/**
	 * Regenerates all nodeNumber and highestChildNodeNumber field values for all
	 * nodes attached to the given root.  The nodeNumber field of the givne root
	 * must already be set.
	 * 
	 * @param root the top of the tree to be renumbered
	 */
	public static void fixNodeNumbersFromRoot( Treeable root )
	{
		int nextNodeNumber = root.getNodeNumber();
		for( Treeable child: root.getChildNodes() )
		{
			child.setNodeNumber(++nextNodeNumber);
			fixNodeNumbersFromRoot(child);
			nextNodeNumber = child.getHighestChildNodeNumber();
		}
		root.setHighestChildNodeNumber(nextNodeNumber);
	}
}
