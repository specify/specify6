/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * This class enumerates form (view) permissions associated with a principal in a given scope
 * 
 * @author Ricardo
 *
 */
public class FormPermissionEnumerator extends PermissionEnumerator 
{
	protected final String permissionBaseName = "Form";

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getPermissions(edu.ku.brc.specify.datamodel.SpPrincipal, java.util.Hashtable, java.util.Hashtable)
	 */
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
			 final Hashtable<String, SpPermission> existingPerms,
			 final Hashtable<String, SpPermission> overrulingPerms) 
	{
		List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>();

		String type = "Form";
		// create a special permission that allows user to see all forms
		perms.add(getStarPermission(permissionBaseName, 
		                            type,
		                            "Forms: permissions to all forms", 
				                    "Permissions to view, add, modify and delete data using all forms", 
				                    existingPerms, overrulingPerms));

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
			
			String title = view.getTitle();
			String desc = "Permissions to view, add, modify and delete data using form " + view.getTitle();
			
			// add newly created permission to the bag that will be returned
			perms.add(new GeneralPermissionEditorRow(perm, oPerm, type, title, desc, null, null));
		}

		return perms;
	}
}
