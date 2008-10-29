/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.PermissionIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 29, 2008
 *
 */
public class BackupTask extends edu.ku.brc.af.tasks.BackupTask
{

    public BackupTask()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(final String userType)
    {
        if (StringUtils.isNotEmpty(userType))
        {
            if (userType.equals("CollectionManager"))
            {
                
            } else if (userType.equals("Guest"))
            {
                
            } else if (userType.equals("DataEntry"))
            {
                
            }
        }
        return null;
    }
    
}
