package edu.ku.brc.specify.treeutils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

/**
 * Provides many static methods that simplify the management of
 * tree-structured table data.  Many of the methods are used in
 * determining if business rules are being properly enforced.
 * 
 * @author jstewart
 */
public class TreeTableUtils
{
    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(TreeTableUtils.class);

	/**
	 * An indicator that node full names should start with highest order
	 * nodes and continue to the lowest order nodes.
	 * @see #REVERSE
	 */
	public static final int FORWARD = 1;
	/**
	 * An indicator that node full names should start with lowest order
	 * nodes and continue to the highest order nodes.
	 * @see #FORWARD
	 */
	public static final int REVERSE = -1;
	
	//TODO: move these to prefs
	//XXX: pref
	/**
	 * Determines the proper full name direction for the given class.
	 * 
	 * @param treeableClass the {@link Class} to inspect
	 * @return {@link #FORWARD} or {@link #REVERSE}
	 */
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
	/**
	 * Determines the proper separator string to put between node
	 * names in full names for the given class.
	 * 
	 * @param treeableClass the class for which to return the separator string
	 * @return the separator string
	 */
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
	
	/**
	 * Generates the 'full name' of a node using the <code>IsInFullName</code> field from the tree
	 * definition items and following the parent pointer until we hit the root node.  Also used
	 * in the process is a "direction indicator" for the tree determining whether the name
	 * should start with the higher nodes and work down to the given node or vice versa.
	 * 
	 * @param node the node to get the full name for
	 * @return the full name
	 */
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
	 * Gets the rank of the next lower tree level.
	 * 
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
		TreeDefinitionItemIface childDefItem = defItem.getChildItem();

		if( childDefItem == null )
		{
			return null;
		}
		
		return childDefItem.getRankId();
	}
	
	/**
	 * Determines if children are allowed for the given node.
	 * 
	 * @param item the node to examine
	 * @return <code>true</code> if children are allowed as defined by the node's tree definition, false otherwise
	 */
	public static boolean childrenAllowed( Treeable item )
	{
		return (getRankOfChildren(item) == null) ? false : true;
	}
	
	/**
	 * Finds the index of <code>newChild</code> within the <code>parent</code>'s set
	 * of child nodes.
	 * 
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

	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the given
	 * <code>TreeDefinitionIface</code> object and having the given rank.
	 * 
	 * @param treeDef the associated tree definition
	 * @param rank the rank of the returned def item
	 * @return the definition item
	 */
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
	
	@SuppressWarnings("unchecked")
	public static TreeDefinitionItemIface getDefItemByName( TreeDefinitionIface treeDef, String name )
	{
		Set<TreeDefinitionItemIface> defItems = (Set<TreeDefinitionItemIface>)treeDef.getTreeDefItems();
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
	 * Returns the highest rank allowed as a parent of the given <code>Treeable</code> object.  This
	 * is determined using the <code>isEnforced</code> field of the associated
	 * <code>TreeDefinitionItemIface</code> objects.
	 * 
	 * @param t the node
	 * @return the highest rank value allowed for a parent
	 */
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
	
	/**
	 * Determines if the given Treeable can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @param treeable the node to examine
	 * @return <code>true</code> if it has no FK contraints barring it from deletion, false otherwise
	 */
	public static boolean canBeDeleted( Treeable treeable )
	{
		// first of all, never allow deletion of a root node
		if( treeable.getParentNode() == null )
		{
			return false;
		}
		
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
	
	/**
	 * Determines if the given Geography can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param geo the node to check
	 * @return <code>true</code> if deletable
	 */
	protected static boolean geographyCanBeDeleted( Geography geo )
	{
		boolean noLocs = geo.getLocalities().isEmpty();
		
		if( noLocs && allDescendantsDeletable(geo) )
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Determines if the given GeologicTimePeriod can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param gtp the node to check
	 * @return <code>true</code> if deletable
	 */
	protected static boolean geologicTimePeriodCanBeDeleted( GeologicTimePeriod gtp )
	{
		boolean noStrats = gtp.getStratigraphies().isEmpty();
		
		if( noStrats && allDescendantsDeletable(gtp) )
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Determines if the given Location can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param loc the node to check
	 * @return <code>true</code> if deletable
	 */
	protected static boolean locationCanBeDeleted( Location loc )
	{
		boolean noConts = loc.getContainers().isEmpty();
		boolean noPreps = loc.getPreparations().isEmpty();
		
		if( noConts && noPreps && allDescendantsDeletable(loc) )
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Determines if the given Taxon can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param taxon the node to check
	 * @return <code>true</code> if deletable
	 */
	protected static boolean taxonCanBeDeleted( Taxon taxon )
	{
		boolean noCitations = taxon.getTaxonCitations().isEmpty();
		boolean noAcceptedChildren = taxon.getAcceptedChildren().isEmpty();
		boolean noExtRes = taxon.getExternalResources().isEmpty();
		boolean noDeter = taxon.getDeterminations().isEmpty();
		
		if( noCitations && noAcceptedChildren && noExtRes && noDeter && allDescendantsDeletable(taxon) )
		{
			return true;
		}
		
		return false;
	}
	

	/**
	 * Determines if all descendants of the given node can be successfully
	 * deleted without violating any business rules.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param parent the root of the subtree to inspect
	 * @return <code>true</code> if all descendants are deletable
	 */
	protected static boolean allDescendantsDeletable(Treeable parent)
	{
		boolean deletable = true;
		
		for( Treeable child: getChildNodes(parent) )
		{
			if( !canBeDeleted(child) )
			{
				return false;
			}
		}
		
		return deletable;
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
	public static boolean canChildBeReparentedToNode( Treeable child, Treeable newParent )
	{
		if( nodeIsDescendantOfNode(newParent,child) )
		{
			return false;
		}
		
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
	
	/**
	 * Returns the next highest rank in the tree that is enforced by the
	 * tree definition.
	 * 
	 * @param node the node to find the next highest enforced rank for
	 * @return the next highest rank
	 */
	public static Integer getRankOfNextHighestEnforcedLevel( Treeable node )
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


	/**
	 * Counts the number of nodes from <code>node</code> to the root.
	 * 
	 * @param node the node to inspect
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
	 * Determines if <code>low</code> is a descendant of <code>high</code>.
	 * 
	 * @param low the possible descendant node
	 * @param high the possible ancestor node
	 * @return <code>true</code> if <code>low</code> is a descendant of <code>high</code>
	 */
	public static boolean nodeIsDescendantOfNode( Treeable low, Treeable high )
	{
		if( low==null || high==null )
		{
			throw new NullPointerException();
		}
		
		Treeable i = low.getParentNode();
		while( i != null )
		{
			if( i == high )
			{
				return true;
			}
			
			i = i.getParentNode();
		}
		return false;
	}

	/**
	 * Regenerates all nodeNumber and highestChildNodeNumber field values for all
	 * nodes attached to the given root.  The nodeNumber field of the given root
	 * must already be set.
	 * 
	 * @param root the top of the tree to be renumbered
	 * @return the highest node number value present in the subtree rooted at <code>root</code>
	 */
	public static int fixNodeNumbersFromRoot( Treeable root )
	{
		int nextNodeNumber = root.getNodeNumber();
		for( Treeable child: getChildNodes(root) )
		{
			child.setNodeNumber(++nextNodeNumber);
			nextNodeNumber = fixNodeNumbersFromRoot(child);
		}
		root.setHighestChildNodeNumber(nextNodeNumber);
		return nextNodeNumber;
	}
	


	/**
	 * Fixes the fullname for the given node and all of its descendants.
	 * 
	 * @param node the root of the subtree to fix
	 */
	public static void fixFullNames( Treeable node )
	{
		node.setFullName(getFullName(node));
		for( Treeable child: getChildNodes(node) )
		{
			fixFullNames(child);
		}
	}
	


	/**
	 * Deletes the given node and all of its descendants from the persistent store.
	 * 
	 * @param node the root of the subtree to delete
	 */
	public static void deleteNodeAndChildren( Treeable node )
	{
		HibernateUtil.beginTransaction();
		
		recursivelyDeleteNodes(node);
		
		HibernateUtil.commitTransaction();
	}
	
	

	/**
	 * Deletes the given node and all of its descendants from the persistent store.
	 * This method simply locates all of the nodes, detaches each from its parent
	 * and children, and calls {@link Session#delete(Object)} on the node.
	 * 
	 * @param start the root of the subtree to delete
	 */
	protected static void recursivelyDeleteNodes( Treeable start )
	{
		start.getParentNode().removeChild(start);

		for( Treeable child: getChildNodes(start) )
		{
			recursivelyDeleteNodes(child);
		}
		
		HibernateUtil.getCurrentSession().delete(start);
	}
	

	/**
	 * Returns the number of proper descendants for node.
	 * 
	 * @param node the node to count descendants for
	 * @return the number of proper descendants
	 */
	public static int getDescendantCount( Treeable node )
	{
		int totalDescendants = 0;
		for( Treeable child: getChildNodes(node) )
		{
			totalDescendants += 1 + getDescendantCount(child);
		}
		return totalDescendants;
	}


	/**
	 * Persists the current subtree structure, rooted at <code>root</code> to the
	 * persistent store.  Also deletes all nodes found in <code>deletedNodes</code>.
	 * 
	 * @param root the root of the subtree to save
	 * @param deletedNodes the <code>Set</code> of nodes to delete
	 */
	public static void saveTreeStructure( Treeable root, Set<Treeable> deletedNodes )
	{
		Session session = HibernateUtil.getCurrentSession();
		HibernateUtil.beginTransaction();
		saveOrUpdateTree(root,session);
		for( Treeable node: deletedNodes )
		{
			if( node.getParentNode() != null )
			{
				node.setParentNode(null);
			}
			session.delete(node);
		}
		HibernateUtil.commitTransaction();
	}
	

	/**
	 * Persists the current subtree structure, rooted at <code>root</code> to the
	 * persistent store, using the given <code>Session</code>.
	 * 
	 * @see #saveTreeStructure(Treeable, Set)
	 * @param root the root of the subtree to save
	 * @param session the {@link Session} to use when persisting
	 */
	public static void saveOrUpdateTree( Treeable root, Session session )
	{
		session.saveOrUpdate(root);
		saveOrUpdateDescendants(root,session);
	}
	

	/**
	 * Persists the subtree structures rooted at the children of <code>node</code> to the
	 * persistent store, using the given <code>Session</code>.
	 * 
	 * @see #saveTreeStructure(Treeable, Set)
	 * @param node the ancestor for which to save all descendants
	 * @param session the {@link Session} to use when persisting
	 */
	private static void saveOrUpdateDescendants( Treeable node, Session session )
	{
		for( Treeable child: getChildNodes(node) )
		{
			session.saveOrUpdate(child);
			saveOrUpdateDescendants(child, session);
		}
	}
	

	/**
	 * Returns a <code>List</code> of all descendants of the given <code>node</code>.
	 * 
	 * @param node the ancestor node to examine
	 * @return all descendants of <code>node</code>
	 */
	public static List<Treeable> getAllDescendants(Treeable node)
	{
		Vector<Treeable> descendants = new Vector<Treeable>();
		for( Treeable child: getChildNodes(node) )
		{
			descendants.add(child);
			descendants.addAll(getAllDescendants(child));
		}
		return descendants;
	}


	/**
	 * Updates the created and modified timestamps to now.  Also
	 * updates the <code>lastEditedBy</code> field to the current
	 * value of the <code>user.name</code> system property.
	 * 
	 * @param node the node to update
	 */
	public static void setTimestampsToNow(Treeable node)
	{
		Date now = new Date();
		node.setTimestampCreated(now);
		node.setTimestampModified(now);

		//TODO: fix this somehow
		log.info("update this implementation");
		String user = System.getProperty("user.name");
		node.setLastEditedBy(user);
	}
	

	/**
	 * Updates the modified timestamp to now.  Also updates the
	 * <code>lastEditedBy</code> field to the current value
	 * of the <code>user.name</code> system property.
	 * 
	 * @param node the node to update
	 */
	public static void updateModifiedTimeAndUser(Treeable node)
	{
		Date now = new Date();
		node.setTimestampModified(now);
		
		//TODO: fix this somehow
		log.info("update this implementation");
		String user = System.getProperty("user.name");
		node.setLastEditedBy(user);
	}

	public static Set<Treeable> getChildNodes(Treeable node)
	{
		//HibernateUtil.getCurrentSession().lock(node,LockMode.NONE);		
		Set<Treeable> children = node.getChildNodes();
		//children.size();
		//HibernateUtil.closeSession();
		return children;
	}
	
	public static Class getNodeClassForTreeDef( TreeDefinitionIface treeDef )
	{
		if( treeDef.getClass().equals(TaxonTreeDef.class) )
		{
			return Taxon.class;
		}
		else if( treeDef.getClass().equals(GeographyTreeDef.class) )
		{
			return Geography.class;
		}
		else if( treeDef.getClass().equals(GeologicTimePeriodTreeDef.class) )
		{
			return GeologicTimePeriod.class;
		}
		else if( treeDef.getClass().equals(LocationTreeDef.class) )
		{
			return Location.class;
		}
		else
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Treeable getRootNodeOfTree(TreeDefinitionIface treeDef)
	{
		Session s = HibernateUtil.getCurrentSession();
		s.lock(treeDef,LockMode.NONE);
		
		Set<TreeDefinitionItemIface> defItems = (Set<TreeDefinitionItemIface>)treeDef.getTreeDefItems();
		TreeDefinitionItemIface item = defItems.iterator().next();
		while(item.getParentItem() != null)
		{
			item = item.getParentItem();
		}
		Treeable root = (Treeable)item.getTreeEntries().iterator().next();
		
		HibernateUtil.closeSession();
		return root;
	}

	public static List loadAllDefsByClass(Class treeDefClass)
	{
		Session session = HibernateUtil.getCurrentSession();
		Criteria c = session.createCriteria(treeDefClass);
		List results = c.list();
		HibernateUtil.closeSession();
		return results;
	}
}