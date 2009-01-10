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

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalSQLService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
public class ObjectPermissionEnumerator extends PermissionEnumerator 
{
    protected static final String prefPrefix = "DO";
    
    /**
     * @param permBaseName
     * @param descKey
     */
    public ObjectPermissionEnumerator()
    {
        super(prefPrefix, "");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getPermissions(edu.ku.brc.specify.datamodel.SpPrincipal, java.util.Hashtable, java.util.Hashtable, java.lang.String)
     */
    @Override
    public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal,
                                                         final Hashtable<String, SpPermission> existingPerms,
                                                         final Hashtable<String, SpPermission> overrulingPerms,
                                                         final String      userType) 
    {
        int userId = UserPrincipalSQLService.getSpecifyUserId(principal);
        List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>();

        // get all workbenches of a user
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            SpecifyUser user = new SpecifyUser(userId);
            session.attach(user);
            
            ObjectPermissionPanel panel = new ObjectPermissionPanel();
            
            Class<?>[] classes = new Class<?>[] {Workbench.class, RecordSet.class, SpQuery.class, SpReport.class};
            
            for (Class<?> cls : classes)
            {
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(cls.getName());
                
                addPermissions(cls, session, user, existingPerms, perms, 
                                cls.getMethod("getId"), 
                                cls.getMethod("getName"),
                                prefPrefix, 
                                tblInfo.getTitle(), 
                                UIRegistry.getLocalizedMessage("ADMININFO_DESC", tblInfo.getTitle()),
                                panel);
            }
        } 
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ObjectPermissionEnumerator.class, e);
            e.printStackTrace();
        }
        finally
        {
            session.close();
        }
        
        return perms;
    }

    /**
     * @param taskName
     * @param subType
     * @param targetId
     * @param existingPerms
     * @return
     */
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
            perm.initialize();
            perm.setName(taskName + "." + subType);
            perm.setActions("");
            perm.setTargetId(targetId);
            perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
        }
        
        return perm;
    }
    
    /**
     * @param wrapper
     * @param taskName
     * @param targetId
     * @param existingPerms
     */
    private void addCustomPermissions(@SuppressWarnings("unused")final ObjectPermissionEditorRow wrapper, 
                                      @SuppressWarnings("unused")final String taskName, 
                                      @SuppressWarnings("unused")final Integer targetId, 
                                      @SuppressWarnings("unused")final Hashtable<String, SpPermission> existingPerms)
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
    private <T> void addPermissions(final Class<T> clazz,
                                    final DataProviderSessionIFace session,
                                    final SpecifyUser user,
                                    final Hashtable<String, SpPermission> existingPerms,
                                    final List<PermissionEditorRowIFace> perms,
                                    final Method getId,
                                    final Method getName,
                                    final String objBaseType,
                                    final String objBaseTitle,
                                    final String objBaseDesc,
                                    final PermissionEditorIFace editorPanel)
    {
        List<T> list = session.getDataList(clazz, "specifyUser", user);
        for (T item : list)
        {
            String  name     = null; 
            Integer targetId = null;
            
            try 
            {
                targetId = (Integer) getId.invoke(item);
                name = (String) getName.invoke(item);
            }
            catch (Exception e) { } // ignore exception
            
            String taskName = "Object." + objBaseTitle + "." + targetId;
            
            SpPermission owner = createPermission(taskName, "Owner", targetId, existingPerms);
            SpPermission group = createPermission(taskName, "Group", targetId, existingPerms);
            SpPermission other = createPermission(taskName, "Other", targetId, existingPerms);
            
            String title = objBaseTitle + ": " + name;
            String description = objBaseDesc + name;
            
            ObjectPermissionEditorRow wrapper = new ObjectPermissionEditorRow(owner, group, other, objBaseType, 
                                                                              title, description, null, editorPanel);
            addCustomPermissions(wrapper, taskName, targetId, existingPerms);
            
            // add newly created permission to the bag that will be returned
            perms.add(wrapper);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getSecurityOptions()
     */
    @Override
    protected List<SecurityOptionIFace> getSecurityOptions()
    {
        // This is called from within PermissionEnumerator's getPermissions. We are overriding that method
        // in this class so it doesn't need to return anything.
        return null;
    }
    
    
}
