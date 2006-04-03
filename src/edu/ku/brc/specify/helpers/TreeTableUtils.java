package edu.ku.brc.specify.helpers;

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

public class TreeTableUtils
{
	public static TreeDefinitionItemIface getRelatedDefItemForRank( Treeable item )
	{
		Integer rank = item.getRankId();
		if( rank == null )
		{
			return null;
		}
		TreeDefinitionIface def = item.getTreeDef();
		Session session = HibernateUtil.getCurrentSession();
		session.lock(def, LockMode.NONE);
		Set defItems = def.getTreeDefItems();
		Hibernate.initialize(defItems);
		HibernateUtil.closeSession();

		for( Object defItemObj: defItems )
		{
			TreeDefinitionItemIface defItem = (TreeDefinitionItemIface)defItemObj;
			if( defItem.getRankId().intValue() == rank )
			{
				return defItem;
			}
		}
		return null;
	}
	
	public static Integer getRankOfChildren( Treeable item )
	{
		TreeDefinitionItemIface defItem = getRelatedDefItemForRank(item);
		if( defItem == null )
		{
			return null;
		}
		
		Session session = HibernateUtil.getCurrentSession();
		session.lock(defItem, LockMode.NONE);
		TreeDefinitionItemIface childDefItem = defItem.getChildItem();
		Hibernate.initialize(childDefItem);
		HibernateUtil.closeSession();

		if( childDefItem == null )
		{
			return null;
		}
		
		return childDefItem.getRankId();
	}
	
	public static boolean childrenAllowed( Treeable item )
	{
		return getRankOfChildren(item) != null ? true : false;
	}
	
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
}
