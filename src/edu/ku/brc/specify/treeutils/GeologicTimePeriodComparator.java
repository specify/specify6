package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.GeologicTimePeriod;

/**
 * A class used to compare GeologicTimePeriod objects for use in sorting sibling nodes.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public class GeologicTimePeriodComparator implements Comparator<GeologicTimePeriod>
{
	/**
	 * Compare two GeologicTimePeriod objects based on the values they return for getEnd().
	 * 
	 * @param o1 a {@link GeologicTimePeriod} object
	 * @param o2 a {@link GeologicTimePeriod} object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
	 */
	public int compare(GeologicTimePeriod o1, GeologicTimePeriod o2)
	{
		int start =  o1.getStart().compareTo(o2.getStart());
		if( start != 0 )
		{
			return start;
		}
		
		return o1.getEnd().compareTo(o2.getEnd());
	}
}
