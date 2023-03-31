/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;
import java.util.List;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * An implementation of {@link Comparator} that sorts tree nodes according to their
 * visual order in a tree UI widget.
 * 
 * @author jstewart
 * @code_status Beta
 * @param <T> a subclass of {@link Treeable}
 * @param <D> a subclass of {@link TreeDefIface}
 * @param <I> a subclass of {@link TreeDefItemIface}
 */
public class TreePathComparator<T extends Treeable<T,D,I>,
                                D extends TreeDefIface<T,D,I>,
                                I extends TreeDefItemIface<T,D,I>>
                                implements Comparator<T>
{
	/** A {@link Comparator} for sorting sibling nodes. */
	protected TreeOrderSiblingComparator siblingComp;
	/** A toggle for setting the case-sensativity of the name-based sorting. */
	protected boolean ignoreCase;
	
	/**
     * Constructor.  
     * 
	 * @param ignoreCase indicator of whether to perform case-sensative sorting or not.
	 */
	public TreePathComparator(boolean ignoreCase)
	{
		siblingComp = new TreeOrderSiblingComparator(ignoreCase);
		this.ignoreCase = ignoreCase;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
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
