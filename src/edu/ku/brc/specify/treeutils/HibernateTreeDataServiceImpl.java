/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
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

    static HibernateLoadLogger loadLogger = new HibernateLoadLogger();
    
	public HibernateTreeDataServiceImpl()
	{
        log.trace("enter");
        log.trace("exit");
	}
	
	@SuppressWarnings("unchecked")
    public synchronized List<T> findByName(D treeDef, String name)
    {
        log.trace("enter");
        Vector<T> results = new Vector<T>();
        Class<T> nodeClass = treeDef.getNodeClass();
        
        Session session = getNewSession(treeDef);
        Query q = session.createQuery("FROM "+nodeClass.getSimpleName()+" as node WHERE node.name LIKE :name");
        q.setParameter("name",name);
        for( Object o: q.list() )
        {
            T t = (T)o;
            
            // force loading on all ancestors
            T parent = t.getParent();
            while(parent!=null)
            {
                parent = parent.getParent();
            }
            
            results.add(t);
        }
        
        Collections.sort(results,new TreePathComparator<T,D,I>(true));
        session.close();
        log.trace("exit");
        return results;
    }
    
    public synchronized Set<T> getChildNodes(T parent)
    {
        log.trace("enter");
        if (Hibernate.isInitialized(parent.getChildren()))
        {
            log.trace("exit");
            return parent.getChildren();
        }
        
        Session session = getNewSession(parent);
        Set<T> children = parent.getChildren();
        // to force Set loading
        int childCount = children.size();
        log.debug(childCount + " child(ren) of " + parent.getName() + " loaded");
        for (T child: children)
        {
            log.debug("\t" + nodeDebugInfo(child));
        }
        session.close();
        log.trace("exit");
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
        log.trace("enter");
		T root = null;
		
        Session session = getNewSession(treeDef);
        
        for( I defItem: treeDef.getTreeDefItems() )
        {
            if (defItem.getRankId()==0)
            {
                root = defItem.getTreeEntries().iterator().next();
            }
        }
        
        session.close();
        if (root!=null)
        {
            log.debug("Root node: " + nodeDebugInfo(root));
        }
        else
        {
            log.debug("No root node");
        }
        log.trace("exit");
		return root;
	}

    public synchronized boolean addNewTreeDefItem(I newDefItem, I parent)
    {
        log.trace("enter");
        
        I origChild = parent.getChild();
        D treeDef = parent.getTreeDef();
        
        Session session = getNewSession(newDefItem,treeDef,parent,origChild);
        Transaction tx = session.beginTransaction();
        
        parent.setChild(newDefItem);
        if (origChild!=null)
        {
            origChild.setParent(newDefItem);
        }
        treeDef.getTreeDefItems().add(newDefItem);

        session.saveOrUpdate(newDefItem);
        session.save(treeDef);
        if (origChild!=null)
        {
        	session.saveOrUpdate(origChild);
        }
        session.saveOrUpdate(newDefItem.getParent());

        boolean success = commitTransaction(session, tx);
        log.trace("exit");
        return success;
    }
    
    /**
     * Determines, based on the registered business rules, if the given object
     * can be deleted safely.
     * 
     * @param o the object the check
     * @param s the Hibernate Session managing that Object
     * @return true if the Object is deletable
     */
    public synchronized boolean canDelete(Object o)
    {
        if (o==null)
        {
            return false;
        }
        
        BusinessRulesIFace busRule = DBTableIdMgr.getBusinessRule(o);
        
        // we assume that no business rules = no complaints about deleting the object
        if (busRule==null)
        {
            return true;
        }
        
        return busRule.okToDelete(o);
    }
    
    public synchronized boolean deleteTreeDefItem(I defItem)
    {
        log.trace("enter");
        if (!canDelete(defItem))
        {
            log.trace("exit");
            return false;
        }
        
        Session session = getNewSession(defItem);
        Transaction tx = session.beginTransaction();
        
        I parent = defItem.getParent();
        I child = defItem.getChild();
        defItem.setParent(null);
        defItem.setChild(null);
        
        parent.setChild(child);
        if (child!=null)
        {
            child.setParent(parent);
        }
        
        defItem.getTreeDef().getTreeDefItems().remove(defItem);
        defItem.setTreeDef(null);
        session.delete(defItem);
        session.saveOrUpdate(parent);
        session.saveOrUpdate(child);
        
        boolean success = commitTransaction(session, tx);
        log.trace("exit");
        return success;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getAllTreeDefs(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<D> getAllTreeDefs(Class<D> treeDefClass)
	{
        log.trace("enter");
        Session session = getNewSession();

        Query q = session.createQuery("FROM " + treeDefClass.getSimpleName());
        List<?> results = q.list();
        
		Vector<D> defs = new Vector<D>(results.size());
		for( Object o: results )
		{
			D def = (D)o;
            
            // force loading of all related def items
            def.getTreeDefItems().size();
            
			defs.add(def);
		}
        
        session.close();
        log.trace("exit");
		return defs;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.treeutils.TreeDataService#getTreeDef(java.lang.Class, long)
	 */
	@SuppressWarnings("unchecked")
	public synchronized D getTreeDef(Class<D> defClass, long defId)
	{
        log.trace("enter");
        Session session = getNewSession();
		String className = defClass.getSimpleName();
		String idFieldName = className.toLowerCase().substring(0,1) + className.substring(1) + "Id";
		Query query = session.createQuery("FROM " + className + " WHERE " + idFieldName + "=:defId");
		query.setParameter("defId",defId);
		D def = (D)query.uniqueResult();

        // force loading of all related def items
        def.getTreeDefItems().size();
        
        session.close();
        log.trace("exit");
		return def;
	}

    public synchronized boolean canAddChildToNode(T node)
    {
        log.trace("enter");
        if (node.getDefinitionItem().getChild() != null)
        {
            log.trace("exit");
            return true;
        }
        log.trace("exit");
        return false;
    }
    
    public synchronized int getDescendantCount(T node)
    { 
        log.trace("enter");
        Session session = getNewSession(node);
        log.debug("refreshing " + nodeDebugInfo(node));
        session.refresh(node);
        Integer nodeNum = node.getNodeNumber();
        Integer highChild = node.getHighestChildNodeNumber();
        
        int descCnt = 0;
        if (nodeNum!=null && highChild!=null)
        {
            descCnt = highChild-nodeNum;
        }
        else
        {
        	descCnt = node.getDescendantCount();
        }
        
        session.close();
        log.trace("exit");
        return descCnt;
   }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeDataService#deleteTreeNode(edu.ku.brc.specify.datamodel.Treeable)
     */
    @SuppressWarnings("null")
    public synchronized void deleteTreeNode(T node)
    {
        log.trace("enter");
        Session session = getNewSession(node);

        // refresh the node data so we have correct information for the following calculation
        session.refresh(node);
        T parent = node.getParent();
        if (parent!=null)
        {
            session.refresh(parent);
        }

        Transaction tx = session.beginTransaction();
        
        // save the original nodeNumber and highestChildNodeNumber values
        Integer delNodeNN = node.getNodeNumber();
        Integer delNodeHC = node.getHighestChildNodeNumber();
        
        // detach from the parent node
        if (parent!=null)
        {
            parent.removeChild(node);
        }
        // let Hibernate delete the subtree
        session.delete(node);
        
        // update the nodeNumber and highestChildNodeNumber fields for all effected nodes
        boolean doNodeNumberUpdate = true;
        if (delNodeNN==null || delNodeHC==null)
        {
            doNodeNumberUpdate = false;
        }
        
        if (doNodeNumberUpdate)
        {
            int nodesDeleted = delNodeHC-delNodeNN+1;
            
            String className = node.getClass().getName();
            TreeDefIface<T,D,I> def = node.getDefinition();
            
            String updateNodeNumbersQueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber-:nodesDeleted WHERE nodeNumber>=:delNodeNN AND definition=:def";
            Query fixNodeNumQuery = session.createQuery(updateNodeNumbersQueryStr);
            fixNodeNumQuery.setParameter("nodesDeleted", nodesDeleted);
            fixNodeNumQuery.setParameter("delNodeNN", delNodeNN);
            fixNodeNumQuery.setParameter("def", def);
            fixNodeNumQuery.executeUpdate();
            
            String updateHighChildQueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber-:nodesDeleted WHERE highestChildNodeNumber>=:delNodeHC AND definition=:def";
            Query fixHighChildQuery = session.createQuery(updateHighChildQueryStr);
            fixHighChildQuery.setParameter("nodesDeleted", nodesDeleted);
            fixHighChildQuery.setParameter("delNodeHC", delNodeHC);
            fixHighChildQuery.setParameter("def", def);
            fixHighChildQuery.executeUpdate();
        }
        
        commitTransaction(session, tx);
        log.trace("exit");
    }
    
    @SuppressWarnings("null")
    public synchronized void addNewChild(T parent, T child)
    {
        log.trace("enter");
        Session session = getNewSession(parent,child);
        Transaction tx = session.beginTransaction();
        
        session.refresh(parent);
        
        parent.addChild(child);
        
        child.fixFullNameForAllDescendants();
        
        // update the nodeNumber and highestChildNodeNumber fields for all effected nodes
        boolean doNodeNumberUpdate = true;
        
        Integer parentNN = parent.getNodeNumber();
        if (parentNN==null)
        {
            doNodeNumberUpdate = false;
        }
        
        if (doNodeNumberUpdate)
        {
            String className = parent.getClass().getName();
            TreeDefIface<T,D,I> def = parent.getDefinition();
            
            String updateNodeNumbersQueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber+1 WHERE nodeNumber>:parentNN AND definition=:def";
            Query fixNodeNumQuery = session.createQuery(updateNodeNumbersQueryStr);
            fixNodeNumQuery.setParameter("parentNN", parentNN);
            fixNodeNumQuery.setParameter("def", def);
            fixNodeNumQuery.executeUpdate();
            
            String updateHighChildQueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber+1 WHERE highestChildNodeNumber>=:parentNN AND definition=:def";
            Query fixHighChildQuery = session.createQuery(updateHighChildQueryStr);
            fixHighChildQuery.setParameter("parentNN", parentNN);
            fixHighChildQuery.setParameter("def", def);
            fixHighChildQuery.executeUpdate();

            // now set the initial values of the nodeNumber and highestChildNodeNumber fields for the new node
            int newChildNN = parentNN+1;
            String setChildNNQueryStr = "UPDATE " + className + " SET nodeNumber=:newChildNN WHERE nodeNumber IS NULL AND parentID=:parentID";
            Query setChildNNQuery = session.createQuery(setChildNNQueryStr);
            setChildNNQuery.setParameter("newChildNN", newChildNN);
            setChildNNQuery.setParameter("parentID", parent.getTreeId());
            setChildNNQuery.executeUpdate();
            
            String setChildHCQueryStr = "UPDATE " + className + " SET highestChildNodeNumber=:newChildNN WHERE highestChildNodeNumber IS NULL AND parentID=:parentID";
            Query setChildHCQuery = session.createQuery(setChildHCQueryStr);
            setChildHCQuery.setParameter("newChildNN", newChildNN);
            setChildHCQuery.setParameter("parentID", parent.getTreeId());
            setChildHCQuery.executeUpdate();
        }

        session.saveOrUpdate(parent);
        session.save(child);
        
        commitTransaction(session, tx);
        log.trace("exit");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.treeutils.TreeDataService#moveTreeNode(edu.ku.brc.specify.datamodel.Treeable, edu.ku.brc.specify.datamodel.Treeable)
     */
    public synchronized void moveTreeNode(T node, T newParent, T rootNode)
    {
        log.trace("enter");
        log.debug("Moving ["+nodeDebugInfo(node)+"] to ["+nodeDebugInfo(newParent)+"]");
        Session session = getNewSession(node,newParent,rootNode);
        Transaction tx = session.beginTransaction();
        
        log.debug("refreshing " + nodeDebugInfo(node));
        session.refresh(node);
        log.debug("refreshing " + nodeDebugInfo(newParent));
        session.refresh(newParent);
        log.debug("refreshing " + nodeDebugInfo(rootNode));
        session.refresh(rootNode);
        
        // fix up the parent/child pointers for the effected nodes
        T oldParent = node.getParent();
        if (oldParent!=null)
        {
            oldParent.removeChild(node);
        }
        newParent.addChild(node);
        node.setParent(newParent);
        
        session.saveOrUpdate(node);
        if (oldParent!=null)
        {
            session.saveOrUpdate(oldParent);
        }
        
        // fix all the node numbers for effected nodes
        // X will represent moving subtree's root node
        // Y will represent new parent of moving subtree
        int rootHC = rootNode.getHighestChildNodeNumber();
        int xNN = node.getNodeNumber();
        int xHC = node.getHighestChildNodeNumber();
        int yNN = newParent.getNodeNumber();
        D def = node.getDefinition();
        String className = node.getClass().getName();
        int numMoving = xHC-xNN+1;
        
        // the HQL update statements that need to happen now are dependant on the 'direction' of the move
        boolean downwardMove = true;
        if (xNN>yNN)
        {
            downwardMove = false;
        }
        
        if (downwardMove)
        {
            // change node numbers for the moving nodes to high values in order to temporarily 'move them out of the tree'
            String step1QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber+:rootHC, highestChildNodeNumber=highestChildNodeNumber+:rootHC WHERE nodeNumber>=:xNN AND nodeNumber<=:xHC AND definition=:def";
            Query step1Query = session.createQuery(step1QueryStr);
            step1Query.setParameter("def", def);
            step1Query.setParameter("xNN", xNN);
            step1Query.setParameter("xHC", xHC);
            step1Query.setParameter("rootHC", rootHC);
            step1Query.executeUpdate();

            String step2QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber-:numMoving WHERE nodeNumber>:xHC AND nodeNumber<=:yNN AND definition=:def";
            Query step2Query = session.createQuery(step2QueryStr);
            step2Query.setParameter("def", def);
            step2Query.setParameter("xHC", xHC);
            step2Query.setParameter("yNN", yNN);
            step2Query.setParameter("numMoving", numMoving);
            step2Query.executeUpdate();

            String step3QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber-:numMoving WHERE highestChildNodeNumber>=:xHC AND highestChildNodeNumber<:yNN AND definition=:def";
            Query step3Query = session.createQuery(step3QueryStr);
            step3Query.setParameter("def", def);
            step3Query.setParameter("xHC", xHC);
            step3Query.setParameter("yNN", yNN);
            step3Query.setParameter("numMoving", numMoving);
            step3Query.executeUpdate();

            String step4QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber-nodeNumber WHERE nodeNumber>:rootHC AND definition=:def";
            Query step4Query = session.createQuery(step4QueryStr);
            step4Query.setParameter("def", def);
            step4Query.setParameter("rootHC", rootHC);
            step4Query.executeUpdate();

            String step5QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber + :yNN - :xHC - :rootHC WHERE nodeNumber>:rootHC AND definition=:def";
            Query step5Query = session.createQuery(step5QueryStr);
            step5Query.setParameter("def", def);
            step5Query.setParameter("xHC", xHC);
            step5Query.setParameter("yNN", yNN);
            step5Query.setParameter("rootHC", rootHC);
            step5Query.executeUpdate();

            String step6QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=nodeNumber+highestChildNodeNumber WHERE nodeNumber >:yNN-:numMoving AND nodeNumber<=:yNN AND definition=:def";
            Query step6Query = session.createQuery(step6QueryStr);
            step6Query.setParameter("def", def);
            step6Query.setParameter("yNN", yNN);
            step6Query.setParameter("numMoving", numMoving);
            step6Query.executeUpdate();
        }
        else
        {
            // change node numbers for the moving nodes to high values in order to temporarily 'move them out of the tree'
            String step1QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber+:rootHC, highestChildNodeNumber=highestChildNodeNumber+:rootHC WHERE nodeNumber>=:xNN AND nodeNumber<=:xHC AND definition=:def";
            Query step1Query = session.createQuery(step1QueryStr);
            step1Query.setParameter("def", def);
            step1Query.setParameter("xNN", xNN);
            step1Query.setParameter("xHC", xHC);
            step1Query.setParameter("rootHC", rootHC);
            step1Query.executeUpdate();
            
            String step2QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber+:numMoving WHERE nodeNumber>:yNN AND nodeNumber<:xNN AND definition=:def";
            Query step2Query = session.createQuery(step2QueryStr);
            step2Query.setParameter("def", def);
            step2Query.setParameter("xNN", xNN);
            step2Query.setParameter("yNN", yNN);
            step2Query.setParameter("numMoving", numMoving);
            step2Query.executeUpdate();

            String step3QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber+:numMoving WHERE highestChildNodeNumber>=:yNN AND highestChildNodeNumber<:xHC AND definition=:def";
            Query step3Query = session.createQuery(step3QueryStr);
            step3Query.setParameter("def", def);
            step3Query.setParameter("yNN", yNN);
            step3Query.setParameter("xHC", xHC);
            step3Query.setParameter("numMoving", numMoving);
            step3Query.executeUpdate();

            String step4QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber-nodeNumber WHERE nodeNumber>:rootHC AND definition=:def";
            Query step4Query = session.createQuery(step4QueryStr);
            step4Query.setParameter("def", def);
            step4Query.setParameter("rootHC", rootHC);
            step4Query.executeUpdate();

            String step5QueryStr = "UPDATE " + className + " SET nodeNumber=nodeNumber+1+:yNN-:xNN-:rootHC WHERE nodeNumber>:rootHC AND definition=:def";
            Query step5Query = session.createQuery(step5QueryStr);
            step5Query.setParameter("def", def);
            step5Query.setParameter("xNN", xNN);
            step5Query.setParameter("yNN", yNN);
            step5Query.setParameter("rootHC", rootHC);
            step5Query.executeUpdate();

            String step6QueryStr = "UPDATE " + className + " SET highestChildNodeNumber=highestChildNodeNumber+nodeNumber WHERE nodeNumber>:yNN AND nodeNumber<=:yNN+:numMoving AND definition=:def";
            Query step6Query = session.createQuery(step6QueryStr);
            step6Query.setParameter("def", def);
            step6Query.setParameter("yNN", yNN);
            step6Query.setParameter("numMoving", numMoving);
            step6Query.executeUpdate();
        }
        
        log.debug("committing JDBC transaction to update node numbers");
        commitTransaction(session, tx);
        log.trace("exit");
    }
    
    /**
     * Creates a new Hibernate session and associates the given objects with it.
     * 
     * @param objects the objects to associate with the new session
     * @return the newly created session
     */
    private Session getNewSession(Object... objects )
    {
        log.trace("enter");

        Session session = HibernateUtil.getSessionFactory().openSession(loadLogger);
        for (Object o: objects)
        {
            if (o!=null)
            {
                // make sure not to attempt locking an unsaved object
                DataModelObjBase dmob = (DataModelObjBase)o;
                if (dmob.getId()!=null)
                {
                    session.lock(o, LockMode.NONE);
                }
            }
        }
        log.trace("exit");
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
        log.trace("enter");
        boolean result = true;
        try
        {
            tx.commit();
        }
        catch (Exception ex)
        {
            result = false;
            log.error("Error while committing transaction to DB",ex);
            tx.rollback();
        }
        finally
        {
            if (session.isOpen())
            {
                session.close();
            }
        }
        log.trace("exit");
        return result;
    }
    
    private String nodeDebugInfo(Object o)
    {
        if (o instanceof Treeable)
        {
            Treeable<?,?,?> t = (Treeable)o;
            return t.getTreeId() + " " + t.getName() + " 0x" + Integer.toHexString(t.hashCode());
        }
        return o.toString();
    }

    public static class HibernateLoadLogger extends EmptyInterceptor
    {
        @Override
        public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
        {
            String className = entity.getClass().getSimpleName();
            log.debug("loaded " + className + " (" + id + ") at 0x" + entity.hashCode());
            return false;
        }
    }
}