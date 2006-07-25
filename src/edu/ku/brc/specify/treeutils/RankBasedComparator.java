package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A compartor to be used in sorting <code>Treeable</code> objects
 * based on their ranks.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public class RankBasedComparator implements Comparator<Treeable>
{
	/**
	 * Compares two <code>Treeable</code> objects based on the values
	 * returned by {@link Treeable#getRankId()}.  Lower rank objects
	 * are considered less than higher rank objects.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @see ReverseRankBasedComparator
	 * {@inheritDoc}
	 * @param o1 a {@link Treeable} object
	 * @param o2 a {@link Treeable} object
	 * @return -1, 0, or 1 if <code>o1</code> is less than, equal to, or greater than <code>o2</code>, respectively
	 */
	public int compare(Treeable o1, Treeable o2)
	{
		return o1.getRankId().compareTo(o2.getRankId());
	}
}
