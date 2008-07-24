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

import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * Abstract class that defines the basic methods used to enumerate potential and existing permissions
 * of a principal. It's used to build the permission table used by instances of PermissionEditor class.  
 *  
 * @author Ricardo
 *
 */
public abstract class PermissionEnumerator 
{
	/**
	 * Returns the permissions of a given principal
	 * @param principal
	 * @param overrulingPrincipal
	 * @return
	 */
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, final SpPrincipal overrulingPrincipal)
	{
		Hashtable<String, SpPermission> existingPerms   = PermissionService.getExistingPermissions(principal.getId());
		
		Hashtable<String, SpPermission> overrulingPerms = null;
		if (overrulingPrincipal != null)
		{
			overrulingPerms = PermissionService.getExistingPermissions(overrulingPrincipal.getId());
		}
		return getPermissions(principal, existingPerms, overrulingPerms);
	}

	/**
	 * Returns a special permission that allows user to see all tasks
	 * @return
	 */
	protected PermissionEditorRowIFace getStarPermission(
			final String permissionBaseName,
			final String title,
			final String description,
			final Hashtable<String, SpPermission> existingPerms,
		 	final Hashtable<String, SpPermission> overrulingPerms)
	{
		String permName = permissionBaseName + ".*";
		SpPermission perm  = existingPerms.get(permName);
		SpPermission oPerm = (overrulingPerms != null)? overrulingPerms.get(permName) : null;

		if (perm == null)
		{
			// no permission with this name, create new one
			perm = new SpPermission();
			perm.setName(permName);
			perm.setActions("");
			perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
		}
		return new GeneralPermissionEditorRow(perm, oPerm, title, description);
	}

	/**
	 * Abstract method that enumerates permissions for a given principal
	 * @param principal
	 * @param existingPerms
	 * @param overrulingPerms
	 * @return
	 */
	public abstract List<PermissionEditorRowIFace> getPermissions(
			final SpPrincipal principal, 
			final Hashtable<String, SpPermission> existingPerms,
			final Hashtable<String, SpPermission> overrulingPerms);
}
