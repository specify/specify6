/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

/**
 * Describes capabilities required of all orderable objects.
 *
 * @author jstewart
 * @code_status Complete
 */
public interface Orderable
{
    /**
     * Retrieves the order index of the object.
     * 
     * @return the order index
     */
    public int getOrderIndex();
    
    /**
     * Sets the order index of the object.
     * 
     * @param order the new order index
     */
    public void setOrderIndex(int order);
}
