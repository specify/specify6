/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.security;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalSQLService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
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
                                prefPrefix, 
                                tblInfo, 
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
    private <T> void addPermissions(final Class<T> clazz,
                                    final DataProviderSessionIFace session,
                                    final SpecifyUser user,
                                    final Hashtable<String, SpPermission> existingPerms,
                                    final List<PermissionEditorRowIFace> perms,
                                    final String objBaseType,
                                    final DBTableInfo tblInfo,
                                    final PermissionEditorIFace editorPanel)
    {
        String      desc = UIRegistry.getLocalizedMessage("ADMININFO_DESC", tblInfo.getTitle());
        String      sql  = String.format("SELECT DISTINCT %s, Name FROM %s WHERE SpecifyUserID = %d", tblInfo.getIdColumnName(), tblInfo.getName(), user.getId());
        Statement   stmt = null;
        ResultSet   rs   = null; 
        
        try
        {
            stmt = DBConnection.getInstance().getConnection().createStatement();
            if (stmt != null)
            {
                rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    int    targetId = rs.getInt(1);
                    String name     = rs.getString(2); 
                    
                    String taskName = "Object." + tblInfo.getTitle() + "." + targetId;
                    
                    SpPermission owner = createPermission(taskName, "Owner", targetId, existingPerms);
                    SpPermission group = createPermission(taskName, "Group", targetId, existingPerms);
                    SpPermission other = createPermission(taskName, "Other", targetId, existingPerms);
                    
                    String title = tblInfo.getTitle() + ": " + name;
                    String description = desc + name;
                    
                    ObjectPermissionEditorRow wrapper = new ObjectPermissionEditorRow(owner, group, other, objBaseType, 
                                                                                      title, description, null, editorPanel);
                    addCustomPermissions(wrapper, taskName, targetId, existingPerms);
                    
                    // add newly created permission to the bag that will be returned
                    perms.add(wrapper);
                }
                rs.close();
                rs = null;
            }
            
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ObjectPermissionEnumerator.class, ex);
            ex.printStackTrace();

        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (SQLException ex2) {}
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
