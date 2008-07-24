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

import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * This is a PermissionEnumerator that wraps one or more actual heterogeneous enumerators into a 
 * single enumerator.
 * 
 * @author Ricardo
 *
 */
public class CompositePermissionEnumerator extends PermissionEnumerator {

	protected List<PermissionEnumerator> enumerators = new ArrayList<PermissionEnumerator>();
	
	//@Override
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
											 final Hashtable<String, SpPermission> existingUserPerms,
											 final Hashtable<String, SpPermission> existingGroupPerms) 
	{
		List<PermissionEditorRowIFace> allPerms = new ArrayList<PermissionEditorRowIFace>();
		for (PermissionEnumerator currEnumerator : enumerators)
		{
			allPerms.addAll(currEnumerator.getPermissions(principal, existingUserPerms, existingGroupPerms));
		}
		return allPerms;
	}
	
	public void clear()
	{
		enumerators.clear();
	}
	
	public void addEnumerator(PermissionEnumerator enumerator)
	{
		enumerators.add(enumerator);
	}
}
