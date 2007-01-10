/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.ui.forms.BusinessRulesIFace;

/**
 * An implementation of @see {@link TreeDataService} that uses Hibernate
 * capabilities.
 *
 * @code_status Beta
 * @author jstewart
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

	//protected Session session;
	
	public HibernateTreeDataServiceImpl()
	{
		// do nothing
	}
	
	@SuppressWarnings("unchecked")
    public synchronized List<T> findByName(D treeDef, String name)
    {
        Vector<T> results = new Vector<T>();
        Class<T> nodeClass = treeDef.getNodeClass();
        
        Session session = getNewSession();
        Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.name LIKE :name");
        q.setParameter("name",name);
        for( Object o: q.list() )
        {
            T t = (T)o;
            results.add(t);
        }
        
        Collections.sort(results,new TreePathComparator<T,D,I>(true));
        session.close();
        return results;
    }
    
    public synchronized Set<T> getChildNodes(T parent)
    {
        Session session = getNewSession(parent);
        Set<T> children = parent.getChildren();
        // to force Set loading
        children.size();
        session.close();
        return children;
    }
    
	/**
	 *
	 *
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getRootNode(edu.ku.brc.specify.datamodel.TreeDefIface)
	 * @param treeDef
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getRootNode(D treeDef)
	{
		Class<T> nodeClass = treeDef.getNodeClass();
		T root = null;
		
        Session session = getNewSession(treeDef);
        Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.rankId = 0 AND node.definition = :def");
		q.setParameter("def",treeDef);
		root = (T)q.uniqueResult();
        // force loading of the def and items
        root.getDefinition().getTreeDefItems().size();
        session.close();
		return root;
	}

	public synchronized void saveTreeDef(D treeDef, List<I> deletedItems)
	{
        Session session = getNewSession(treeDef);

        Transaction tx = session.beginTransaction();
		
		// save the TreeDefinitionIface object itself
		session.saveOrUpdate(treeDef);
		
		// save all of the TreeDefinitionItemIface objects
		for(I o: treeDef.getTreeDefItems())
		{
			session.saveOrUpdate(o);
		}
		
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
        finally
        {
            if (session.isOpen())
            {
                session.close();
            }
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
	protected synchronized int fixNodeNumbersFromRoot( T root, Session session )
	{
        session.lock(root,LockMode.NONE);
        int nextNodeNumber = root.getNodeNumber();
		for( T child: root.getChildren() )
		{
			child.setNodeNumber(++nextNodeNumber);
			nextNodeNumber = fixNodeNumbersFromRoot(child,session);
		}
		root.setHighestChildNodeNumber(nextNodeNumber);
		return nextNodeNumber;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getAllTreeDefs(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<D> getAllTreeDefs(Class<D> treeDefClass)
	{
        Session session = getNewSession();
		Criteria crit = session.createCriteria(treeDefClass);
		List<?> results = crit.list();
		Vector<D> defs = new Vector<D>(results.size());
		for( Object o: results )
		{
			D def = (D)o;
			defs.add(def);
		}
        session.close();
		return defs;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getTreeDef(java.lang.Class, long)
	 */
	@SuppressWarnings("unchecked")
	public synchronized D getTreeDef(Class<D> defClass, long defId)
	{
        Session session = getNewSession();
		String className = defClass.getSimpleName();
		String idFieldName = className.toLowerCase().substring(0,1) + className.substring(1) + "Id";
		Query query = session.createQuery("FROM " + className + " WHERE " + idFieldName + "=:defId");
		query.setParameter("defId",defId);
		D def = (D)query.uniqueResult();
        
        def.getTreeDefItems().size();
        session.close();
		return def;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeDataService#canDeleteNode(edu.ku.brc.specify.datamodel.Treeable)
     */
    public synchronized boolean canDeleteNode(T node)
    {
        Session session = getNewSession(node);
        BusinessRulesIFace busRules = TreeFactory.createBusinessRules(node);
        boolean ok = busRules.okToDelete(node);
        session.close();
        return ok;
    }
    
    public synchronized boolean canAddChildToNode(T node)
    {
        if (node.getDefinitionItem().getChild() != null)
        {
            return true;
        }
        return false;
    }
    
    public synchronized int getDescendantCount(T node)
    {
        Session session = getNewSession(node);
        //session.refresh(node);
        Integer nodeNum = node.getNodeNumber();
        Integer highChild = node.getHighestChildNodeNumber();
        
        int descCnt = 0;
        if (nodeNum!=null && highChild!=null)
        {
            descCnt = highChild-nodeNum;
        }
        
        session.close();
        return descCnt;
   }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeDataService#deleteTreeNode(edu.ku.brc.specify.datamodel.Treeable)
     */
    @SuppressWarnings("null")
    public synchronized void deleteTreeNode(T node)
    {
        Session session = getNewSession(node);

        // refresh the node data so we have correct information for the following calculation
        session.refresh(node);

        Transaction tx = session.beginTransaction();
        
        // save the original nodeNumber and highestChildNodeNumber values
        Integer nodeNumberStart = node.getNodeNumber();
        Integer nodeNumberEnd = node.getHighestChildNodeNumber();
        
        doDeleteSubtree(node, session);

        boolean doNodeNumberUpdate = true;
        if (nodeNumberStart==null || nodeNumberEnd==null)
        {
            doNodeNumberUpdate = false;
        }
        
        if (doNodeNumberUpdate)
        {
            int nodesDeleted = nodeNumberEnd-nodeNumberStart+1;
            
            String className = node.getClass().getName();
            String updateNodeNumbersQueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber-" + nodesDeleted + " WHERE nodeNumber > " + nodeNumberStart;
            Query fixNodeNumQuery = session.createQuery(updateNodeNumbersQueryStr);
            int nodesUpdated = fixNodeNumQuery.executeUpdate();
            
            String updateHighChildQueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber-" + nodesDeleted + " WHERE highestChildNodeNumber >= " + nodeNumberStart;
            Query fixHighChildQuery = session.createQuery(updateHighChildQueryStr);
            int highChildNodesUpdated = fixHighChildQuery.executeUpdate();

            System.out.println("UPDATE queries to fix node number issues:");
            System.out.println(updateNodeNumbersQueryStr);
            System.out.println(updateHighChildQueryStr);
            System.out.println("Nodes updated: " + nodesUpdated + " and " + highChildNodesUpdated);
        }
        
        commitTransaction(session, tx);
    }
    
    protected synchronized void doDeleteSubtree(T topNode, Session session)
    {
        T parent = topNode.getParent();
        if (parent!=null)
        {
            parent.removeChild(topNode);
        }

        for (T child: topNode.getChildren())
        {
            doDeleteSubtree(child, session);
        }
        
        session.delete(topNode);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeDataService#moveTreeNode(edu.ku.brc.specify.datamodel.Treeable, edu.ku.brc.specify.datamodel.Treeable)
     */
    public synchronized void moveTreeNode(T node, T newParent)
    {
        // TODO: add node number change support to this method

        Session session = getNewSession(node,newParent);
        Transaction tx = session.beginTransaction();
        
        T oldParent = node.getParent();
        if (oldParent!=null)
        {
            oldParent.removeChild(node);
        }
        newParent.addChild(node);
        
        session.saveOrUpdate(node);
        if (oldParent!=null)
        {
            session.saveOrUpdate(oldParent);
        }
        commitTransaction(session, tx);
    }
    
    /**
     * Creates a new Hibernate session and associates the given objects with it.
     * 
     * @param objects the objects to associate with the new session
     * @return the newly created session
     */
    private Session getNewSession(Object... objects )
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        for (Object o: objects)
        {
            if (o!=null)
            {
                session.lock(o, LockMode.NONE);
            }
        }
        return session;
    }
    
    /**
     * Commits the given Hibernate transaction on the given session.  A rollback
     * is performed, and false is returned, on failure.
     * 
     * @param session the session
     * @param tx the transaction
     * @return true on success
     */
    private boolean commitTransaction(Session session, Transaction tx)
    {
        boolean result = true;
        try
        {
            tx.commit();
        }
        catch (Exception ex)
        {
            result = false;
            log.error("Error while committing transaction to DB");
            tx.rollback();
            log.error(ex);
        }
        finally
        {
            if (session.isOpen())
            {
                session.close();
            }
        }
        return result;
    }
}