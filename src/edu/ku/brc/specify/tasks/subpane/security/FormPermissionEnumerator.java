/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.forms.persist.ViewIFace;

/**
 * This class enumerates form (view) permissions associated with a principal in a given scope
 * 
 * @author Ricardo
 *
 */
public class FormPermissionEnumerator extends PermissionEnumerator 
{
	protected final String permissionBaseName = "Form";

	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
			 final Hashtable<String, SpPermission> existingPerms,
			 final Hashtable<String, SpPermission> overrulingPerms) 
	{
		List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>();

		// create a special permission that allows user to see all forms
		perms.add(getStarPermission(permissionBaseName, "Forms: permissions to all forms", 
				"Permissions to view, add, modify and delete data using all forms", existingPerms, overrulingPerms));

		// get all views
        List<ViewIFace> list = SpecifyAppContextMgr.getInstance().getAllViews();
        for (ViewIFace view : list)
        {
        	String formName 	= view.getName();
        	String taskName 	= permissionBaseName + "." + formName;
        	
			// first check if there is a permission with this name
			SpPermission perm  = existingPerms.get(taskName);
			SpPermission oPerm = (overrulingPerms != null)? overrulingPerms.get(taskName) : null;
			
			if (perm == null)
			{
				// no permission with this name, create new one
				perm = new SpPermission();
				perm.setName(taskName);
				perm.setActions("");
				perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
			}
			
			String title = "Form: " + view.getTitle();
			String desc = "Permissions to view, add, modify and delete data using form " + view.getTitle();
			
			// add newly created permission to the bag that will be returned
			perms.add(new GeneralPermissionEditorRow(perm, oPerm, title, desc));
		}

		return perms;
	}
}
