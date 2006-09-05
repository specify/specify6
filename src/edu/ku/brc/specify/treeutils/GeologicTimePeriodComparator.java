package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.GeologicTimePeriod;

/**
 * A class used to compare GeologicTimePeriod objects for use in sorting sibling nodes.
 *
 * @code_status Complete
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
		Float start1 = o1.getStart();
		Float start2 = o2.getStart();
		if(start1 == null)
		{
			return -1;
		}
		else if(start2 == null)
		{
			return 1;
		}
		
		int start =  start1.compareTo(start2);
		if( start != 0 )
		{
			return start;
		}
		
		Float end1 = o1.getEnd();
		Float end2 = o2.getEnd();
		if(end1 == null)
		{
			return 1;
		}
		else if(end2 == null)
		{
			return -1;
		}
		return end1.compareTo(end2);
	}
}
