package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A class used to compare Treeable objects for use in sorting sibling nodes.
 * 
 * @author jstewart
 */
public class GeologicTimePeriodComparator implements Comparator<Treeable>
{
	/**
	 * Compare two GeologicTimePeriod objects based on the values they return for getEnd().
	 * 
	 * @param o1 a GeologicTimePeriod object
	 * @param o2 a GeologicTimePeriod object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
	 */
	public int compareGTPs(GeologicTimePeriod o1, GeologicTimePeriod o2)
	{
		int start =  o1.getStart().compareTo(o2.getStart());
		if( start != 0 )
		{
			return start;
		}
		
		return o1.getEnd().compareTo(o2.getEnd());
	}

	/**
	 * Compare two Treeable objects.  If both objects are instances of GeologicTimePeriod,
	 * <code>compareGTPs(o1,o2)</code> is called.  Otherwise, the objects are compared
	 * based on the values returned by calls to <code>getName()</code>.
	 * 
	 * @param o1 a Treeable object
	 * @param o2 a Treeable object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Treeable o1, Treeable o2)
	{
		if( o1 instanceof GeologicTimePeriod && o2 instanceof GeologicTimePeriod )
		{
			return compareGTPs((GeologicTimePeriod)o1, (GeologicTimePeriod)o2);
		}
		
		return o1.getName().compareTo(o2.getName());
	}
}
