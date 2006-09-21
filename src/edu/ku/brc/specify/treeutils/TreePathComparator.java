/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;
import java.util.List;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreePathComparator<T extends Treeable<T,D,I>,
                                D extends TreeDefIface<T,D,I>,
                                I extends TreeDefItemIface<T,D,I>>
                                implements Comparator<T>
{
	protected TreeOrderSiblingComparator siblingComp;
	protected boolean ignoreCase;
	
	public TreePathComparator(boolean ignoreCase)
	{
		siblingComp = new TreeOrderSiblingComparator(ignoreCase);
		this.ignoreCase = ignoreCase;
	}
	
	/**
	 *
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @param o1
	 * @param o2
	 * @return
	 */
	public int compare(T o1, T o2)
	{
		if(o1.isDescendantOf(o2))
		{
			return 1;
		}
		if(o2.isDescendantOf(o1))
		{
			return -1;
		}
		
		List<T> path1 = o1.getAllAncestors();
		path1.add(o1);
		List<T> path2 = o2.getAllAncestors();
		path2.add(o2);
		
		for( int i = 0; i < Math.min(path1.size(),path2.size()); ++i )
		{
			T t1 = path1.get(i);
			T t2 = path2.get(i);
			
			// if the nodes are the same, skip to the next level down
			if(t1==t2)
			{
				continue;
			}
			
			// at this point we are dealing with nodes from a common parent o1An.get(i-1)
			
			return siblingComp.compare(t1,t2);
		}
		
		return 0;
	}
}
