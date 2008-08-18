/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import edu.ku.brc.af.core.db.DBTableIdMgr;

/**
 * Creates a DBTableIDMgr for the WorkBench.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jul 19, 2008
 *
 */
public class SpecifyWBDBTableIdMgr extends DBTableIdMgr
{
    /**
     * Creates a DBTableIDMgr for the WorkBench.
     */
    public SpecifyWBDBTableIdMgr()
    {
        super(false);
    }

}
