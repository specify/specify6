/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalSQLService;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
public class ObjectPermissionEnumerator extends PermissionEnumerator {

	//@Override
	public List<PermissionEditorRowIFace> getPermissions(SpPrincipal principal,
			Hashtable<String, SpPermission> existingPerms,
			Hashtable<String, SpPermission> overrulingPerms) 
	{
		int userId = UserPrincipalSQLService.getSpecifyUserId(principal);
		List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>();

		// get all workbenches of a user
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	SpecifyUser user = new SpecifyUser(userId);
        	session.attach(user);
        	
        	addPermissions(Workbench.class, session, user, existingPerms, perms, 
        			Workbench.class.getMethod("getId"), Workbench.class.getMethod("getName"),
        			"Workbench", "Permission to view, add, modify, or delete data in the workbench ");
        	
        	addPermissions(RecordSet.class, session, user, existingPerms, perms, 
        			RecordSet.class.getMethod("getId"), RecordSet.class.getMethod("getName"),
        			"RecordSet", "Permission to view, add, modify, or delete record set ");
        	
/*			XXX problem: loans are not linked to specify user directly
         	addPermissions(Loan.class, session, user, existingPerms, perms, 
        			Loan.class.getMethod("getId"), Loan.class.getMethod("getId"),
        			"Loan", "Permission to view, add, modify, or delete data in loan ");
*/
        } 
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	session.close();
        }
		
		return perms;
	}

	private SpPermission createPermission(final String taskName, 
	                                      final String subType, 
	                                      final Integer targetId, 
	                                      final Hashtable<String, SpPermission> existingPerms)
	{
		// first check if there is a permission with this name
		SpPermission perm  = existingPerms.get(taskName + "." + subType);
		
		if (perm == null)
		{
			// no permission with this name, create new one
			perm = new SpPermission();
			perm.setName(taskName + "." + subType);
			perm.setActions("");
			perm.setTargetId(targetId);
			perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
		}
		
		return perm;
	}
	
	private void addCustomPermissions(final ObjectPermissionEditorRow wrapper, 
	                                  final String taskName, 
	                                  final Integer targetId, 
	                                  final Hashtable<String, SpPermission> existingPerms)
	{
		// TODO: add custom permissions to wrapper
	}

	/**
	 * Enumerates the permissions for user on a a generic type of object
	 * @param <T>
	 * @param clazz
	 * @param session
	 * @param user
	 * @param existingPerms
	 * @param perms
	 * @param getId
	 * @param getName
	 * @param objBaseTitle
	 * @param objBaseDesc
	 */
	private <T> void addPermissions(
			Class<T> clazz,
			DataProviderSessionIFace session,
			final SpecifyUser user,
			final Hashtable<String, SpPermission> existingPerms,
			List<PermissionEditorRowIFace> perms,
			Method getId,
			Method getName,
			String objBaseTitle,
			String objBaseDesc)
	{
    	List<T> list = session.getDataList(clazz, "specifyUser", user);
    	for (T item : list)
    	{
        	String name = null; 
    		Integer targetId = null;
    		
    		try 
    		{
    			targetId = (Integer) getId.invoke(item);
    			name = (String) getName.invoke(item);
    		}
    		catch (Exception e) { } // ignore exception
        	
        	String taskName 	= "Object." + objBaseTitle + "." + targetId;
        	
        	SpPermission owner = createPermission(taskName, "Owner", targetId, existingPerms);
        	SpPermission group = createPermission(taskName, "Group", targetId, existingPerms);
        	SpPermission other = createPermission(taskName, "Other", targetId, existingPerms);
        	
        	String title = objBaseTitle + ": " + name;
        	String description = objBaseDesc + name;
        	
        	ObjectPermissionEditorRow wrapper = new ObjectPermissionEditorRow(owner, group, other, title, description, null);
        	addCustomPermissions(wrapper, taskName, targetId, existingPerms);
        	
			// add newly created permission to the bag that will be returned
			perms.add(wrapper);
    	}
	}
}
