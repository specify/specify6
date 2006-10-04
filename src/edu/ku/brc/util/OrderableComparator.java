/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

import java.util.Comparator;

/**
 * A comparator for sorting objects that implement the Orderable interface.
 *
 * @code_status Complete
 * @author jstewart
 */
public class OrderableComparator implements Comparator<Orderable>
{
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Orderable o1, Orderable o2)
    {
        int oi1 = o1.getOrderIndex();
        int oi2 = o2.getOrderIndex();
        if (oi1==oi2)
        {
            return 0;
        }
        return (oi1<oi2) ? -1 : 1;
    }
}
