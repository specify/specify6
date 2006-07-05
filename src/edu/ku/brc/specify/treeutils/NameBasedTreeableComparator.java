package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A class used to compare Treeable objects for use in sorting sibling nodes.
 * 
 * @author jstewart
 */
public class NameBasedTreeableComparator implements Comparator<Treeable>
{
	/**
	 * Compare two Treeable objects.  The objects are compared
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
		return o1.getName().compareTo(o2.getName());
	}
}
