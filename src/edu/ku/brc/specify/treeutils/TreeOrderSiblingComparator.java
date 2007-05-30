/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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