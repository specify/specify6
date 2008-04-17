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

    public abstract String getTitle();
    
    public abstract boolean isSideBar();
    
    public abstract int getOrder();
    
    public abstract void setOrder(int order);
    
}
