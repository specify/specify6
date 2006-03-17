package edu.ku.brc.specify.ui.db;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

/**
 * Implements a TreeModel that is appropriate for use in handling items from a
 * database table that is holding hierarchical data.  The table must have the
 * following 6 fields: <code>[TableName]Id</code> (int), <code>ParentId</code>
 * (int), <code>NodeNumber</code> (int), <code>HighestChildNumber</code> (int),
 * <code>Name</code> (string), <code>RankId</code> (int), and <code>SeriesId</code>
 * (int).  The <code>[TableName]Id</code> field must be unique across the rows
 * of the table.  The <code>NodeNumber</code> field must be labeled using a
 * depth-first ordering of the nodes of the tree, starting with 0 as the root
 * node.  Each row of the table that represents a member of the tree must have
 * a common value in the <code>SeriesId</code> field.
 * 
 * <p>TODO: insert jpg image of 2 small trees and associated rows from table
 * that contains those trees.
 * 
 * @author Joshua
 *
 */
public class DbStoreTreeModel implements TreeModel
{	
	private List<TreeModelListener> treeModelListeners;
	private Vector<Treeable> nodes;
	private Treeable root;
	private Class treeableClass;
	private TreeDefinitionIface treeDef;
	
	/**
	 * @param treeableClass a Class implementing the Treeable interface
	 * @param treeDef the TreeDefinitionIface object enumerating this tree's levels 
	 */
	public DbStoreTreeModel( final Class treeableClass, final TreeDefinitionIface treeDef)
		throws SQLException
	{
		treeModelListeners = new Vector<TreeModelListener>();
		this.treeableClass = treeableClass;
		this.treeDef = treeDef;
		
		Session session = HibernateUtil.getCurrentSession();
		//Query query = session.createQuery("from "+treeableClass.getCanonicalName()+" as node where node.definition = :treeDef");
		//query.setEntity("treeDef",treeDef);
		
		Query query = session.createQuery("from "+treeableClass.getCanonicalName());
		
		nodes = new Vector<Treeable>(query.list());

		// find the root node
		for( Treeable node: nodes )
		{
			if( node.getNodeNumber() == 1 )
			{
				root = node;
				break;
			}
		}
		
		if( root == null )
		{
			// There is no root node.  What do we do here?
			// The database is corrupted.
			throw new SQLException("Corrupt database.  No root node found.");
		}
	}

	public Object getRoot()
	{
		return this.root;
	}

	private Vector<Treeable> getChildren(Treeable parent)
	{
		Vector<Treeable> children = new Vector<Treeable>();
		for( Treeable node: nodes )
		{
			//Treeable
			if( node.getParentNode() != null && node.getParentNode() == parent )
			{
				children.add(node);
			}
		}
		return children;
	}
	
	public Object getChild(Object parent, int index)
	{
		if( !(parent instanceof Treeable) )
		{
			return null;
		}
		Treeable p = (Treeable)parent;
		
		Vector<Treeable> children = getChildren(p);
		if( children.size() <= index )
		{
			return null;
		}
		
		return children.elementAt(index);
	}

	public int getChildCount(Object parent)
	{
		if( !(parent instanceof Treeable) )
		{
			return -1;
		}
		return getChildren((Treeable)parent).size();
	}

	public boolean isLeaf(Object node)
	{
		return (getChildCount(node) == 0) ? true : false;
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
		System.out.println("DbStoreTreeModel.valueForPathChanged() called");
	}

	public int getIndexOfChild(Object parent, Object child)
	{
		if( !(parent instanceof Treeable) )
		{
			return -1;
		}

		Vector<Treeable> children = getChildren((Treeable)parent);
		return children.indexOf(child);
	}

	public void addTreeModelListener( TreeModelListener listener )
	{
		treeModelListeners.add(listener);
	}

	public void removeTreeModelListener( TreeModelListener listener )
	{
		treeModelListeners.remove(listener);
	}

	private TreePath getPathToNode( Treeable node )
	{
		Vector<Treeable> pathNodes = new Vector<Treeable>();
		pathNodes.add(node);

		Treeable parent = (Treeable)node.getParentNode();
		while( parent != null )
		{
			pathNodes.insertElementAt(parent,0);
			parent = (Treeable)parent.getParentNode();
		}
		return new TreePath(pathNodes.toArray());
	}
	
	public void attachChildToParent( Treeable child, Treeable oldParent, Treeable newParent )
	{
		System.out.println("attachChildToParent() Entering");
		child.setParentNode(newParent);
		throwStructureChangeEvent(newParent);
		throwStructureChangeEvent(oldParent);
	}
	
	public void removeChildFromParent( Treeable child, Treeable oldParent )
	{
		System.out.println("removeChildFromParent entering");
	}
	
	public void throwRootLevelModelEvent()
	{
		TreePath pathToRoot = new TreePath(root);
		throwStructureChangeEvent(pathToRoot);
	}
	
	public void throwStructureChangeEvent(TreePath path)
	{
		TreeModelEvent e = new TreeModelEvent(this,path);
		for( TreeModelListener l: treeModelListeners )
		{
			l.treeStructureChanged(e);
		}
	}
	
	public void throwStructureChangeEvent(Treeable node)
	{
		TreePath path = getPathToNode(node);
		throwStructureChangeEvent(path);
	}
	
	public void throwStructureChangeEventForPathIntersection(TreePath path1, TreePath path2)
	{
		Object[] pathArray1 = path1.getPath();
		Object[] pathArray2 = path2.getPath();
		int smallerSize = Math.min(pathArray1.length,pathArray2.length);
		Object[] commonPath = new Object[smallerSize];
		for( int i = 0; i < smallerSize; ++i )
		{
			if( pathArray1[i] == pathArray2[i] )
			{
				commonPath[i] = pathArray1[i];
			}
			else
			{
				break;
			}
		}
		
		TreePath path = new TreePath(commonPath);
		throwStructureChangeEvent(path);
	}
	
	public void throwInsertEvent( Treeable node )
	{
		TreePath path = getPathToNode(node);
		for( TreeModelListener l: treeModelListeners )
		{
			l.treeNodesInserted(new TreeModelEvent(this,path));
		}
	}
	
	public void throwDeleteEvent( Treeable oldParent )
	{
		TreePath path = getPathToNode(oldParent);
		for( TreeModelListener l: treeModelListeners )
		{
			l.treeNodesRemoved(new TreeModelEvent(this,path));
		}
	}
		
	public void commitChangesToDb()
	{
//		fixNodeNumberAndHighChild();
//		
//		writeCurrentTreeStructureToDb();
//
//		throwRootLevelModelEvent();
		
		System.out.println("commitChangesToDb() finished");
	}
	
	private void fixNodeNumberAndHighChild()
	{
		int nextNodeNumber = 1;
		
		// start the recursive numbering process
		numberNode(root,nextNodeNumber);
		
		// now fix all the parent node number pointers
		for( Treeable node: nodes )
		{
			// this should only happen for element 0 of nodes
			// it happens because we start numbering nodes with 1
			if( node == null || node == root )
			{
				continue;
			}
			
			node.setParentNode(node.getParentNode());
		}
	}
	
	private int getDescendantCount( Treeable node )
	{
		int descCount = 0;
		for( Treeable child: getChildren(node) )
		{
			descCount += getDescendantCount(child);
		}
		return descCount;
	}
	
	private int numberNode( Treeable node, int nextNodeNumber )
	{
		node.setNodeNumber(nextNodeNumber++);
		node.setHighestChildNodeNumber(node.getNodeNumber()+getDescendantCount(node));
		
		Iterator<Treeable> children = getChildren(node).iterator();
		while( children.hasNext() )
		{
			nextNodeNumber = numberNode(children.next(),nextNodeNumber);
		}
		
		return nextNodeNumber;
	}
}
