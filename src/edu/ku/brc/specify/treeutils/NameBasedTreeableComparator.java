package edu.ku.brc.specify.treeutils;

import java.util.Comparator;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A class used to compare Treeable objects for use in sorting sibling nodes.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public class NameBasedTreeableComparator implements Comparator<Treeable>
{
	/**
	 * Compare two Treeable objects.  The objects are compared
	 * based on the values returned by calls to <code>getName()</code>.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * {@inheritDoc}
	 * @param o1 a {@link Treeable} object
	 * @param o2 a {@link Treeable} object
	 * @return -1, 0, or 1 if <code>o1</code> is less than, equal to, or greater than <code>o2</code>, respectively
	 */
	public int compare(Treeable o1, Treeable o2)
	{
		return o1.getName().compareTo(o2.getName());
	}
}
