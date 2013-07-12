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
package edu.ku.brc.util;

import java.util.Comparator;


/**
 * A comparator to be used in sorting {@link Rankable} objects
 * based on their ranks.
 *
 * @code_status Complete
 * @author jstewart
 */
public class RankBasedComparator implements Comparator<Rankable>
{
	/**
	 * Compares two {@link Rankable} objects based on the values
	 * returned by {@link Rankable#getRankId()}.  Lower rank objects
	 * are considered less than higher rank objects.  A <code>null</code>
	 * value for rank is considered to be less than any non-<code>null</code>
	 * value.  If both objects have <code>null</code> ranks, -1 is returned.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * {@inheritDoc}
	 * @param o1 a {@link Rankable} object
	 * @param o2 a {@link Rankable} object
	 * @return -1, 0, or 1 if <code>o1</code> is less than, equal to, or greater than <code>o2</code>, respectively
	 */
	public int compare(Rankable o1, Rankable o2)
	{
		Integer rank1 = o1.getRankId();
		Integer rank2 = o2.getRankId();
		if(rank1 == null)
		{
			return -1;
		}
		else if(rank2 == null)
		{
			return 1;
		}
		
		return rank1.compareTo(rank2);
	}
}
