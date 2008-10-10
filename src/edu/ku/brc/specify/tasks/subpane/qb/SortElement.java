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
 * Describes the sort settings for a single column in a result set.
 *
 */
public class SortElement 
{
    public static final int ASCENDING  = 0;
    public static final int DESCENDING = 1;

    protected final int     column;        // the column's index
    protected final int     direction; 
    
    /**
     * @param column
     * @param direction
     */
    public SortElement(final int column, final int direction)
    {
        this.column = column;
        this.direction = direction;
    }

    /**
     * @return the column
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * @return the direction
     */
    public int getDirection()
    {
        return direction;
    }
    
}
