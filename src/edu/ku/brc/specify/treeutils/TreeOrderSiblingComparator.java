/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.util.NameBasedComparator;
import edu.ku.brc.util.RankBasedComparator;

/**
 * This class is an implementation of {@link Comparator} for sorting
 * {@link Treeable} objects.  Sorting is done first by rank, then by name.
 * 
 * @author jstewart
 * @code_status Complete
 */
public class TreeOrderSiblingComparator implements Comparator<Treeable<?,?,?>>
{
	/** A comparator for sorting {@link Treeable}s according to rank. */
	protected RankBasedComparator rankComparator;
    /** A comparator for sorting {@link Treeable}s according to name. */
	protected NameBasedComparator nameComparator;
	
	/**
	 * Constructor.  Name-based sorting is case-sensative.
	 */
	public TreeOrderSiblingComparator()
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator();
	}
	
	/**
     * Constructor.
     * 
	 * @param ignoreCase indicator of whether to perform case-sensative sorting or not.
	 */
	public TreeOrderSiblingComparator(boolean ignoreCase)
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator(ignoreCase);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Treeable<?,?,?>o1, Treeable<?,?,?> o2)
	{
		int rankResult = rankComparator.compare(o1,o2);
		if(rankResult != 0)
		{
			return rankResult;
		}
		return nameComparator.compare(o1,o2);
	}
}
