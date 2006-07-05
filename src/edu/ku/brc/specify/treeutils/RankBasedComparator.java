package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Treeable;

public class RankBasedComparator implements Comparator<Treeable>
{
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Treeable o1, Treeable o2)
	{
		return o1.getRankId().compareTo(o2.getRankId());
	}
}
