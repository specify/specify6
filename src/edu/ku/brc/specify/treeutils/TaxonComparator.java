/* Copyright (C) 2015, University of Kansas Center for Research
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

import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.util.NameBasedComparator;
import edu.ku.brc.util.RankBasedComparator;

/**
 * An implementation of @see {@link Comparator} for use in sorting
 * instances of Taxon, based first on rank, then on name.
 *
 * @code_status Complete
 * @author jstewart
 */
public class TaxonComparator implements Comparator<Taxon>
{
	/** The primary comparator used to sort Taxons. */
	protected RankBasedComparator rankComparator;
	/** The secondary comparator used to sort Taxons when the primary comparator returns 0. */
	protected NameBasedComparator nameComparator;
	
	/**
	 * Constructs a new TaxonComparator object.
	 */
	public TaxonComparator()
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator();
	}
	
	/**
	 * Compares to Taxon objects.
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @param o1 a {@link Taxon} object
	 * @param o2 a {@link Taxon} object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
	 */
	public int compare(Taxon o1, Taxon o2)
	{
		int rankResult = rankComparator.compare(o1,o2);
		if(rankResult != 0)
		{
			return rankResult;
		}
		return nameComparator.compare(o1,o2);
	}
}
