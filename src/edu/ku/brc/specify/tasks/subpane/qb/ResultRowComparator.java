/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ResultRowComparator implements Comparator<Vector<Object>>
{
    protected final List<SortElement> sortDef;
    
    public ResultRowComparator(final List<SortElement> sortDef)
    {
        this.sortDef = sortDef;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Vector<Object> o1, Vector<Object> o2)
    {
        for (SortElement s : sortDef)
        {
            int result = doCompare(s, o1, o2);
            if (result != 0)
            {
                return result;
            }
        }
        return 0;
    }
    
    protected int doCompare(SortElement s, Vector<Object> o1, Vector<Object> o2)
    {
      Object obj1 = s.getDirection() == SortElement.ASCENDING ? o1.get(s.getColumn()) : o2.get(s.getColumn());
      Object obj2 = s.getDirection() == SortElement.ASCENDING ? o2.get(s.getColumn()) : o1.get(s.getColumn());
      if (obj1 == null && obj2 == null)
      {
          return 0;
      }
      if (obj1 == null)
      {
          return -1;
      }
      if (obj2 == null)
      {
          return 1;
      }
      return obj1.toString().compareTo(obj2.toString());
    }
    
}
