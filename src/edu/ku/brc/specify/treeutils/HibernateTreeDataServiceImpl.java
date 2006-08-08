/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class HibernateTreeDataServiceImpl implements TreeDataService
{
    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(HibernateTreeDataServiceImpl.class);

	protected Session session;
	
	public HibernateTreeDataServiceImpl()
	{
	}
	
	public void setSession(Session session)
	{
		this.session = session;
	}
	
	public Session getSession()
	{
		return session;
	}
	
	public void init()
	{
		session = HibernateUtil.getSessionFactory().openSession();
	}
	
	public void fini()
	{
		try
		{
			session.close();
		}
		catch( Exception ex )
		{
			log.warn("Exception caught while closing Hibernate session", ex);
		}
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getRootNode(edu.ku.brc.specify.datamodel.TreeDefinitionIface)
	 * @param treeDef
	 * @return
	 */
	public Treeable getRootNode(TreeDefinitionIface treeDef)
	{
		Class nodeClass = treeDef.getNodeClass();
		Treeable root = null;
		
		Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.parent IS NULL AND node.definition = :def");
		q.setParameter("def",treeDef);
		root = (Treeable)q.uniqueResult();
		return root;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getTreeNodes(edu.ku.brc.specify.datamodel.TreeDefinitionItemIface)
	 * @param defItem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Treeable> getTreeNodes(TreeDefinitionItemIface defItem)
	{
		if( !session.contains(defItem) )
		{
			session.lock(defItem,LockMode.NONE);
		}
		Hibernate.initialize(defItem.getTreeEntries());
		return defItem.getTreeEntries();
	}

	/**
	 * Persists the current subtree structure, rooted at <code>root</code> to the
	 * persistent store.  Also deletes all nodes found in <code>deletedNodes</code>.
	 * 
	 * @param root the root of the subtree to save
	 * @param deletedNodes the <code>Set</code> of nodes to delete
	 */
	public void saveTree(Treeable rootNode, Set<Treeable> deletedNodes)
	{
		rootNode.setNodeNumber(1);
		fixNodeNumbersFromRoot(rootNode);
		Transaction tx = session.beginTransaction();
		saveOrUpdateTree(rootNode);
		for( Treeable node: deletedNodes )
		{
			if( node.getParentNode() != null )
			{
				node.setParentNode(null);
			}
			session.delete(node);
		}
		try
		{
			tx.commit();
		}
		catch( Exception ex )
		{
			log.error("Failed to save tree data to DB",ex);
			tx.rollback();
		}
	}
	
	public void saveTreeDef(TreeDefinitionIface treeDef, List<TreeDefinitionItemIface> deletedItems)
	{
		// get the root node of the tree
		Treeable rootNode = (Treeable)treeDef.getTreeEntries().iterator().next();
		while(rootNode.getParentNode()!=null)
		{
			rootNode = rootNode.getParentNode();
		}
		
		rootNode.setNodeNumber(1);
		fixNodeNumbersFromRoot(rootNode);
		Transaction tx = session.beginTransaction();
		
		// save the TreeDefinitionIface object itself
		session.saveOrUpdate(treeDef);
		
		// save all of the TreeDefinitionItemIface objects
		for(Object o: treeDef.getTreeDefItems())
		{
			session.saveOrUpdate(o);
		}
		
		// save all of the nodes
		saveOrUpdateTree(rootNode);

		// delete all of the tree def items that were deleted by the user
		for(TreeDefinitionItemIface item: deletedItems)
		{
			// ignore the items with null ID
			// they were probably created, then deleted, before ever being persisted
			if(item.getTreeDefItemId() != null)
			{
				session.delete(item);
			}
		}
		
		try
		{
			tx.commit();
		}
		catch( Exception ex )
		{
			log.error("Failed to save tree data to DB",ex);
			tx.rollback();
		}
	}

	/**
	 * Regenerates all nodeNumber and highestChildNodeNumber field values for all
	 * nodes attached to the given root.  The nodeNumber field of the given root
	 * must already be set.
	 * 
	 * @param root the top of the tree to be renumbered
	 * @return the highest node number value present in the subtree rooted at <code>root</code>
	 */
	protected int fixNodeNumbersFromRoot( Treeable root )
	{
		int nextNodeNumber = root.getNodeNumber();
		for( Treeable child: root.getChildNodes() )
		{
			child.setNodeNumber(++nextNodeNumber);
			nextNodeNumber = fixNodeNumbersFromRoot(child);
		}
		root.setHighestChildNodeNumber(nextNodeNumber);
		return nextNodeNumber;
	}

	/**
	 * Persists the current subtree structure, rooted at <code>root</code> to the
	 * persistent store, using the given <code>Session</code>.
	 * 
	 * @param root the root of the subtree to save
	 * @param session the {@link Session} to use when persisting
	 */
	protected void saveOrUpdateTree( Treeable root )
	{
		session.saveOrUpdate(root);
		for( Treeable child: root.getChildNodes() )
		{
			saveOrUpdateTree(child);
		}
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getAllTreeDefs(java.lang.Class)
	 * @param treeDefClass
	 * @return
	 */
	public List<TreeDefinitionIface> getAllTreeDefs(Class treeDefClass)
	{
		Criteria crit = session.createCriteria(treeDefClass);
		List results = crit.list();
		Vector<TreeDefinitionIface> defs = new Vector<TreeDefinitionIface>(results.size());
		for( Object o: results )
		{
			TreeDefinitionIface def = (TreeDefinitionIface)o;
			defs.add(def);
		}
		return defs;
	}
}