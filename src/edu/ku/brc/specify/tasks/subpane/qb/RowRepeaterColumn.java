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

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class RowRepeaterColumn implements RowRepeater
{
    final int columnIndex;
    
    public RowRepeaterColumn(final int columnIndex)
    {
        this.columnIndex = columnIndex;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.RowRepeater#repeats(java.lang.Object[])
     */
    @Override
    public int repeats(Object[] row)
    {
        Object val = row[columnIndex];
        if (val == null)
        {
            return 1; //Or 0??
        }
        if (val instanceof Number)
        {
            int numVal = ((Number )val).intValue();
            //if <=0 then just return 1. No erasing or backing up or whatever-
            return numVal <= 0 ? 1 : numVal;
        }
        return 1; // Or 0?? Or blow up??
    }

}
