/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.PermissionIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 29, 2008
 *
 */
public class TaskPermPersist
{
    protected String  taskName;
    protected String  userType;
    protected boolean canView;
    protected boolean canModify;
    protected boolean canDel;
    protected boolean canAdd;
    
    public TaskPermPersist()
    {
        super();
    }

    /**
     * @param taskName
     * @param canView
     * @param canModify
     * @param canDel
     * @param canAdd
     */
    public TaskPermPersist(String taskName, 
                            String userType,
                            boolean canView, 
                            boolean canModify, 
                            boolean canDel,
                            boolean canAdd)
    {
        super();
        this.taskName = taskName;
        this.userType = userType;
        this.canView = canView;
        this.canModify = canModify;
        this.canDel = canDel;
        this.canAdd = canAdd;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public boolean isCanView()
    {
        return canView;
    }

    public void setCanView(boolean canView)
    {
        this.canView = canView;
    }

    public boolean isCanModify()
    {
        return canModify;
    }

    public void setCanModify(boolean canModify)
    {
        this.canModify = canModify;
    }

    public boolean isCanDel()
    {
        return canDel;
    }

    public void setCanDel(boolean canDel)
    {
        this.canDel = canDel;
    }

    public boolean isCanAdd()
    {
        return canAdd;
    }

    public void setCanAdd(boolean canAdd)
    {
        this.canAdd = canAdd;
    }
    
    public PermissionIFace getDefaultPerms()
    {
        return new PermissionSettings(canView, canModify, canDel, canAdd);
    }
    
    
    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
     // Aliases
        xstream.alias("tp", TaskPermPersist.class); //$NON-NLS-1$
        xstream.aliasAttribute(TaskPermPersist.class, "taskName",  "name"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(TaskPermPersist.class, "userType",  "type"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(TaskPermPersist.class, "canView",   "view"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(TaskPermPersist.class, "canModify", "modify"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(TaskPermPersist.class, "canDel",    "del"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(TaskPermPersist.class, "canAdd",    "add"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}