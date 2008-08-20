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

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;

/**
 * This class enumerates task related permissions associated with a principal
 * 
 * @author Ricardo
 *
 */
public class TaskPermissionEnumerator extends PermissionEnumerator 
{
	public final String permissionBaseName = "Task";

	//@Override
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
			 final Hashtable<String, SpPermission> existingPerms,
			 final Hashtable<String, SpPermission> overrulingPerms) 
	{
		// iterate through all possible tasks
		Collection<Taskable> tasks = TaskMgr.getInstance().getAllTasks();
		List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>(tasks.size());
		
		// create a special permission that allows user to see all forms
		perms.add(getStarPermission(permissionBaseName, "Tasks: permissions to all tasks", // I18N
				"Permissions to view, add, modify and delete data in all tasks", 
				existingPerms, overrulingPerms));

		// sort permissions by their string representations 
		//Arrays.sort(perms, new ComparatorByStringRepresentation<SpPermission>());
		for (Taskable task : tasks)
		{
			// first check if there is a permission with this name
			String taskName = permissionBaseName + "." + task.getName();
			SpPermission perm  = existingPerms.get(taskName);
			SpPermission oPerm = (overrulingPerms != null)? overrulingPerms.get(taskName) : null;

			if (perm == null)
			{
				perm = new SpPermission();
				perm.setName(taskName);
				perm.setActions("");
				perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
			}
			
			String title = "Task: " + task.getTitle(); // I18N ???? (maybe not)
			String desc = "Permissions to view, add, modify and delete data in task " + task.getTitle();

			// add newly created permission to the bag that will be returned
//			/ImageIcon icon = IconManager.getIcon(task.getImageIcon(), id)
			perms.add(new GeneralPermissionEditorRow(perm, oPerm, title, desc, task.getImageIcon()));
		}
		
		return perms;
	}
}
