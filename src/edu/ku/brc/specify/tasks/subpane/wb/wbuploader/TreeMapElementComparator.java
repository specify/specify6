/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Comparator;
import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Compares vectors of TreeMapElement
 *
 */
public class TreeMapElementComparator implements Comparator<Vector<TreeMapElement>>
{

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Vector<TreeMapElement> tm1, Vector<TreeMapElement> tm2)
    {
        if (tm1 == tm2)
        {
            return 0;
        }
        if (tm1 == null)
        {
            return -1;
        }
        if (tm2 == null)
        {
            return 1;
        }
        if (tm1.size() < tm2.size())
        {
            return -1;
        }
        if (tm1.size() > tm2.size())
        {
            return 1;
        }
        //ranks better be the same for all members so just compare first
        if (tm1.get(0).getRank() < tm2.get(0).getRank())
        {
            return -1;
        }
        if (tm1.get(0).getRank() > tm2.get(0).getRank())
        {
            return 1;
        }
        return 0;
    }

}
