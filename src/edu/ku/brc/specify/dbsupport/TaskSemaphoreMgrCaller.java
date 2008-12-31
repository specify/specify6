/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import edu.ku.brc.specify.datamodel.SpTaskSemaphore;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public interface TaskSemaphoreMgrCaller
{
    public TaskSemaphoreMgr.USER_ACTION resolveConflict(SpTaskSemaphore semaphore, boolean previouslyLocked,
                                                 String prevLockBy);
}
