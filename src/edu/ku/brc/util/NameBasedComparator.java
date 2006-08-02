package edu.ku.brc.util;

import java.util.Comparator;


/**
 * A class used to compare Nameable objects for use in sorting.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public class NameBasedComparator implements Comparator<Nameable>
{
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
		return o1.getName().compareTo(o2.getName());
	}
}
