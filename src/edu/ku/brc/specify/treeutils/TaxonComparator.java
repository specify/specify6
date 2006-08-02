/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.util.NameBasedComparator;
import edu.ku.brc.util.RankBasedComparator;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TaxonComparator implements Comparator<Taxon>
{
	protected RankBasedComparator rankComparator;
	protected NameBasedComparator nameComparator;
	
	public TaxonComparator()
	{
		rankComparator = new RankBasedComparator();
		nameComparator = new NameBasedComparator();
	}
	
	/**
	 *
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
		else
		{
			return nameComparator.compare(o1,o2);
		}
	}
}
