/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.util.NameBasedComparator;
import edu.ku.brc.util.RankBasedComparator;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeOrderSiblingComparator implements Comparator<Treeable<?,?,?>>
{
	protected RankBasedComparator rankComparator;
	protected NameBasedComparator nameComparator;
	
	public TreeOrderSiblingComparator()
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator();
	}
	
	public TreeOrderSiblingComparator(boolean ignoreCase)
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator(ignoreCase);
	}
	
	/**
	 *
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * @param o1 a {@link Taxon} object
	 * @param o2 a {@link Taxon} object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
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