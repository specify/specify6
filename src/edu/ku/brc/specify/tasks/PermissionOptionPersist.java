/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 29, 2008
 *
 */
public class PermissionOptionPersist
{
    protected String  taskName;
    protected String  userType;
    protected boolean canView;
    protected boolean canModify;
    protected boolean canDel;
    protected boolean canAdd;
    
    /**
     * 
     */
    public PermissionOptionPersist()
    {
        super();
    }

    /**
     * @param taskName
     * @param userType
     * @param canView
     * @param canModify
     * @param canDel
     * @param canAdd
     */
    public PermissionOptionPersist(String taskName, 
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
    
    /**
     * @param taskName
     * @param userType
     * @param perm
     */
    public PermissionOptionPersist(String taskName, 
                                   String userType,
                                   PermissionIFace perm)
    {
        this(taskName, userType, perm.canView(), perm.canModify(), perm.canDelete(), perm.canAdd());
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
    
    public SpPermission getSpPermission()
    {
        SpPermission perm = new SpPermission();
        perm.setActions(canView, canAdd, canModify, canDel);
        perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
        // XXX set name: perm.setName(taskName)
        return perm;
    }
    
    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
     // Aliases
        xstream.alias("tp", PermissionOptionPersist.class); //$NON-NLS-1$
        xstream.aliasAttribute(PermissionOptionPersist.class, "taskName",  "name"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(PermissionOptionPersist.class, "userType",  "type"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(PermissionOptionPersist.class, "canView",   "view"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(PermissionOptionPersist.class, "canModify", "modify"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(PermissionOptionPersist.class, "canDel",    "del"); //$NON-NLS-1$ //$NON-NLS-2$
        xstream.aliasAttribute(PermissionOptionPersist.class, "canAdd",    "add"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
