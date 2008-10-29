/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.StartUpTask;
import edu.ku.brc.af.tasks.StatsTrackerTask;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * This class enumerates task related permissions associated with a principal
 * 
 * @author Ricardo
 *
 */
public class TaskPermissionEnumerator extends PermissionEnumerator 
{
	public    final String permissionBaseName = "Task";
	protected final String type               = "Task";

	/**
	 * 
	 */
	public TaskPermissionEnumerator()
	{
	    
	}
	
    /**
     * @param task
     * @return
     */
    protected boolean isTaskOK(final Taskable task)
    {
        if (!(StatsTrackerTask.class.isAssignableFrom(task.getTaskClass()) ||
              StartUpTask.class.isAssignableFrom(task.getTaskClass())))
        {
            return true;
        }
        return false;
    }
    
    /**
     * @param perms
     * @param securityOption
     * @param icon
     * @param principal
     * @param existingPerms
     * @param overrulingPerms
     */
    protected void checkAndAddPermission(final List<PermissionEditorRowIFace> perms,
                                         final SecurityOptionIFace             securityOption,
                                         final ImageIcon                       icon,
                                         final SpPrincipal                     principal, 
                                         final Hashtable<String, SpPermission> existingPerms,
                                         final Hashtable<String, SpPermission> overrulingPerms,
                                         final boolean                         doAddDefaultPerms)
    {
     // first check if there is a permission with this name
        String       taskName = permissionBaseName + "." + securityOption.getPermissionName();
        SpPermission perm     = existingPerms.get(taskName);
        SpPermission oPerm    = (overrulingPerms != null)? overrulingPerms.get(taskName) : null;

        if (perm == null)
        {
            perm = new SpPermission();
            perm.setName(taskName);
            perm.setActions("");
            perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
            
            if (doAddDefaultPerms)
            {
                PermissionIFace defPerms = securityOption.getDefaultPermissions("CollectionManager");
                if (defPerms != null)
                {
                    perm.setActions(defPerms.canView(), defPerms.canAdd(), defPerms.canModify(), defPerms.canDelete());
                }
            }
        }
        
        String desc = "Permissions to view, add, modify and delete data in task " + securityOption.getShortDesc();

        PermissionEditorIFace editPanel = null;
        editPanel = securityOption.getPermEditorPanel();
        
        // add newly created permission to the bag that will be returned
        perms.add(new GeneralPermissionEditorRow(perm, oPerm, type, securityOption.getPermissionTitle(), 
                                                 desc, icon, editPanel));
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getPermissions(edu.ku.brc.specify.datamodel.SpPrincipal, java.util.Hashtable, java.util.Hashtable)
	 */
    @Override
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal                     principal, 
			                                             final Hashtable<String, SpPermission> existingPerms,
			                                             final Hashtable<String, SpPermission> overrulingPerms,
			                                             final boolean                         doAddDefaultPerms) 
	{
		// iterate through all possible tasks
		Collection<Taskable>           tasks = TaskMgr.getInstance().getAllTasks();
		List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>(tasks.size());
		
		
		// create a special permission that allows user to see all forms
		perms.add(getStarPermission(permissionBaseName, 
		                            type,
		                            "Tasks: permissions to all tasks", // I18N
				                    "Permissions to view, add, modify and delete data in all tasks", 
				                    existingPerms, 
				                    overrulingPerms));

		// sort permissions by their string representations 
		for (Taskable task : tasks)
		{
		    if (isTaskOK(task))
		    {
		        ImageIcon taskIcon = task.getIcon(Taskable.StdIcon20);
		        
		        checkAndAddPermission(perms, task, taskIcon, principal, existingPerms, overrulingPerms, doAddDefaultPerms);
		        
		        List<SecurityOptionIFace> additionalOptions = task.getAdditionalSecurityOptions();
		        if (additionalOptions != null)
		        {
		            for (SecurityOptionIFace sOpt : additionalOptions)
		            {
		                ImageIcon icon = sOpt.getIcon(Taskable.StdIcon20);
		                if (icon == null)
		                {
		                    icon = taskIcon;
		                }
		                checkAndAddPermission(perms, sOpt, icon, principal, existingPerms, overrulingPerms, doAddDefaultPerms);
		            }
		        }
		    }
		}
		
		return perms;
	}
}
