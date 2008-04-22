/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 16, 2008
 *
 */
public interface TaskConfigItemIFace extends Comparable<TaskConfigItemIFace>
{

    /**
     * @return
     */
    public abstract String getTitle();
    
    /**
     * @return
     */
    public abstract int getOrder();
    
    /**
     * @param order
     */
    public abstract void setOrder(int order);
    
    /**
     * @return whether the item should be displayed
     */
    public abstract boolean isVisible();
    
}
