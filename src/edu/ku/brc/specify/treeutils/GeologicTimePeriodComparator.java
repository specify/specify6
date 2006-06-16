package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Treeable;

public class GeologicTimePeriodComparator implements Comparator<Treeable>
{
	public int compareGTPs(GeologicTimePeriod o1, GeologicTimePeriod o2)
	{
		int start =  o1.getStart().compareTo(o2.getStart());
		if( start != 0 )
		{
			return start;
		}
		
		return o1.getEnd().compareTo(o2.getEnd());
	}

	public int compare(Treeable o1, Treeable o2)
	{
		if( o1 instanceof GeologicTimePeriod && o2 instanceof GeologicTimePeriod )
		{
			return compareGTPs((GeologicTimePeriod)o1, (GeologicTimePeriod)o2);
		}
		
		return o1.getName().compareTo(o2.getName());
	}
}
