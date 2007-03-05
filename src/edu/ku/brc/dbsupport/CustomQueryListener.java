/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.dbsupport;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Created: Mar 4, 2007
 *
 */
public interface CustomQueryListener
{
    /**
     * Notification that the process is done and has finished without exception.
     * @param customQuery the custom query being executed
     */
    public void exectionDone(CustomQuery customQuery);
    
    /**
     * Notification that the process was done and completed with an exception.
     * @param customQuery the custom query being executed
     */
    public void executionError(CustomQuery customQuery);
}
