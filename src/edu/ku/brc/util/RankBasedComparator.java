package edu.ku.brc.util;

import java.util.Comparator;


/**
 * A compartor to be used in sorting <code>Rankable</code> objects
 * based on their ranks.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public class RankBasedComparator implements Comparator<Rankable>
{
	/**
	 * Compares two <code>Rankable</code> objects based on the values
	 * returned by {@link Rankable#getRankId()}.  Lower rank objects
	 * are considered less than higher rank objects.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @see ReverseRankBasedComparator
	 * {@inheritDoc}
	 * @param o1 a {@link Rankable} object
	 * @param o2 a {@link Rankable} object
	 * @return -1, 0, or 1 if <code>o1</code> is less than, equal to, or greater than <code>o2</code>, respectively
	 */
	public int compare(Rankable o1, Rankable o2)
	{
		return o1.getRankId().compareTo(o2.getRankId());
	}
}
