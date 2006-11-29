/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * An implementation of @see {@link TreeDataService} that uses Hibernate
 * capabilities.
 *
 * @code_status Beta
 * @author jstewart
 * @version %I% %G%
 */
public class HibernateTreeDataServiceImpl <T extends Treeable<T,D,I>,
											D extends TreeDefIface<T,D,I>,
											I extends TreeDefItemIface<T,D,I>>
											implements TreeDataService<T,D,I>
{
    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(HibernateTreeDataServiceImpl.class);

	protected Session session;
	
	public HibernateTreeDataServiceImpl()
	{
		// do nothing
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
	
	@SuppressWarnings("unchecked")
	public List<T> findByName(D treeDef, String name)
	{
		Vector<T> results = new Vector<T>();
		Class<T> nodeClass = treeDef.getNodeClass();
		Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.name = :name");
		q.setParameter("name",name);
		for( Object o: q.list() )
		{
			T t = (T)o;
			results.add(t);
		}
		
		Collections.sort(results,new TreePathComparator<T,D,I>(true));
		
		return results;
	}
	
	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getRootNode(edu.ku.brc.specify.datamodel.TreeDefIface)
	 * @param treeDef
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getRootNode(D treeDef)
	{
		Class<T> nodeClass = treeDef.getNodeClass();
		T root = null;
		
		Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.parent IS NULL AND node.definition = :def");
		q.setParameter("def",treeDef);
		root = (T)q.uniqueResult();
		return root;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getTreeNodes(edu.ku.brc.specify.datamodel.TreeDefItemIface)
	 * @param defItem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<T> getTreeNodes(I defItem)
	{
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
	public void saveTree(T rootNode, boolean fixNodeNumbers, Set<T> addedNodes, Set<T> deletedNodes)
	{
		if(fixNodeNumbers)
		{
			rootNode.setNodeNumber(1);
			fixNodeNumbersFromRoot(rootNode);
		}
		Transaction tx = session.beginTransaction();
		saveOrUpdateTree(rootNode);
		for( T node: deletedNodes )
		{
			if( node.getParent() != null )
			{
				node.setParent(null);
			}
            if (node.getTreeId()!=null)
            {
                session.delete(node);
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
	
	public void saveTreeDef(D treeDef, List<I> deletedItems)
	{
		Transaction tx = session.beginTransaction();
		
		// save the TreeDefinitionIface object itself
		session.saveOrUpdate(treeDef);
		
		// save all of the TreeDefinitionItemIface objects
		for(I o: treeDef.getTreeDefItems())
		{
			session.saveOrUpdate(o);
		}
		
//		// save all of the nodes
//		saveOrUpdateTree(rootNode);

		// delete all of the tree def items that were deleted by the user
		for(I item: deletedItems)
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
	protected int fixNodeNumbersFromRoot( T root )
	{
		int nextNodeNumber = root.getNodeNumber();
		for( T child: root.getChildren() )
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
	protected void saveOrUpdateTree( T root )
	{
		session.saveOrUpdate(root);
		if( Hibernate.isInitialized(root.getChildren()) )
		{
			for( T child: root.getChildren() )
			{
				saveOrUpdateTree(child);
			}
		}
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getAllTreeDefs(java.lang.Class)
	 * @param treeDefClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<D> getAllTreeDefs(Class<D> treeDefClass)
	{
		Criteria crit = session.createCriteria(treeDefClass);
		List<?> results = crit.list();
		Vector<D> defs = new Vector<D>(results.size());
		for( Object o: results )
		{
			D def = (D)o;
			defs.add(def);
		}
		return defs;
	}
	
	@SuppressWarnings("unchecked")
	public D getTreeDef(Class<D> defClass, long defId)
	{
		String className = defClass.getSimpleName();
		String idFieldName = className.toLowerCase().substring(0,1) + className.substring(1) + "Id";
		Query query = session.createQuery("FROM " + className + " WHERE " + idFieldName + "=:defId");
		query.setParameter("defId",defId);
		D def = (D)query.uniqueResult();
		return def;
	}

	public void loadAllDescendants(T node)
	{
		// This was the old, 'dumb' implementation
//		for(Treeable child: node.getChildNodes())
//		{
//			loadAllDescendants(child);
//		}
		
		// This impl loads more efficiently, I think
		String className = node.getClass().getSimpleName();
		Integer nodeNum = node.getNodeNumber();
		Integer highChild = node.getHighestChildNodeNumber();
		if( nodeNum == null || highChild == null )
		{
			// have to do this the inefficient way
			for(T child: node.getChildren())
			{
				loadAllDescendants(child);
			}
			return;
		}
		
		Query descend = session.createQuery("FROM " + className + " WHERE nodeNumber > " + nodeNum + " AND nodeNumber < " + highChild );
		int i = 0;
		for(@SuppressWarnings("unused")	Object o: descend.list())
		{
			i++;
		}
	}
}