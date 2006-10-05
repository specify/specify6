package edu.ku.brc.util;

import java.util.Comparator;

/**
 * A class used to compare Nameable objects for use in sorting.
 *
 * @code_status Complete
 * @author jstewart
 */
public class NameBasedComparator implements Comparator<Nameable>
{
	protected boolean ignoreCase;
	
	/**
	 * Constructs a new instance that is case sensative.
	 */
	public NameBasedComparator()
	{
		ignoreCase = false;
	}
	
	/**
	 * Constructs a new instance with case-sensativity determined
	 * by the value of the <code>ignoreCase</code> parameter.
	 *
	 * @param ignoreCase
	 */
	public NameBasedComparator(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}
	
	/**
	 * Compare two Nameable objects.  The objects are compared
	 * based on the values returned by calls to <code>getName()</code>.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * {@inheritDoc}
	 * @param o1 a {@link Nameable} object
	 * @param o2 a {@link Nameable} object
	 * @return -1, 0, or 1 if <code>o1</code> is less than, equal to, or greater than <code>o2</code>, respectively
	 */
	public int compare(Nameable o1, Nameable o2)
	{
		if(ignoreCase)
		{
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		}
        
        // to make sure the names are not null
        String name1 = o2.getName();
        String name2 = o2.getName();
        if (name1==null)
        {
            name1="";
        }
        if (name2==null)
        {
            name2="";
        }
        
		return o1.getName().compareTo(o2.getName());
	}
}
