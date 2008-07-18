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
public class RowRepeaterConst implements RowRepeater
{

    protected final int repeats;
    
    public RowRepeaterConst(final int repeats)
    {
        this.repeats = repeats;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.RowRepeater#repeats(java.lang.Object[])
     */
    //@Override
    public int repeats(Object[] row)
    {
        return repeats;
    }

}
