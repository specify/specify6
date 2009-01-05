/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
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
public interface TaskSemaphoreMgrCallerIFace
{
    /**
     * @param semaphore
     * @param previouslyLocked
     * @param prevLockBy
     * @return
     */
    public TaskSemaphoreMgr.USER_ACTION resolveConflict(SpTaskSemaphore semaphore, 
                                                        boolean previouslyLocked,
                                                        String prevLockBy);
}
