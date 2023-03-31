/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.auth.specify.permission;

import java.io.FilePermission;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.UnresolvedPermission;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.policy.DatabaseService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Aug 16, 2007
 *
 */
public class PermissionService
{
    protected static final Logger      log             = Logger.getLogger(PermissionService.class);
    private static boolean             debug           = false;
    static private final Certificate[] EMPTY_CERTS     = new Certificate[0];
    static private final Class<?>[]    ZERO_ARGS       = {};
    static private final Object[]      ZERO_OBJS       = {};
    static private final Class<?>[]    ONE_STRING_ARG  = { String.class };
    static private final Class<?>[]    TWO_STRING_ARGS = { String.class, String.class };

    /**
     * @param id
     * @throws SQLException
     */
    public static void removePermission(Integer id)
    {
        removePrincipalPermissions(Collections.singleton(id));
    }

    /**
     * @param ids
     * @throws SQLException
     */
    public static void removePrincipalPermissions(final Set<?> ids)
    {
        Connection conn = null;
        PreparedStatement tiePstmt = null;
        PreparedStatement permPstmt = null;
        try
        {
            //XXX convert to hibernate (it's ok if it isn't converted to Hibernate - rods)
            conn = DatabaseService.getInstance().getConnection();
            String sql = "DELETE FROM spprincipal_sppermission WHERE SpPermissionID = ?"; //$NON-NLS-1$
            tiePstmt = conn.prepareStatement(sql);
            permPstmt = conn.prepareStatement("DELETE FROM sppermission WHERE SpPermissionID= ?"); //$NON-NLS-1$
            for (Iterator<?> itr = ids.iterator(); itr.hasNext();)
            {
                Integer id = (Integer)itr.next();
                tiePstmt.setString(1, ""+ id +""); //$NON-NLS-1$ //$NON-NLS-2$
                permPstmt.setString(1, ""+ id +""); //$NON-NLS-1$ //$NON-NLS-2$
                tiePstmt.executeUpdate();
                permPstmt.executeUpdate();
            }
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (tiePstmt != null)  tiePstmt.close(); 
                if (permPstmt != null)  permPstmt.close(); 
                
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

    /**
     * @param principalIds
     * @return
     * @throws SQLException
     */
    public static List<Permission> findPrincipalPermissions(final Set<Integer> principalIds) 
    {
        if(debug)log.debug("findPrincipalPermissions"); //$NON-NLS-1$
        List<Permission> permissions = new ArrayList<Permission>();
        for (Iterator<Integer> itr = principalIds.iterator(); itr.hasNext();)
        {
            Integer principalId = itr.next();
            if(debug)log.debug("findPrincipalPermissions - principalID" + principalId); //$NON-NLS-1$
            permissions.addAll(findPrincipalBasedPermissions(principalId));
        }
        return permissions;
    }
    
    /**
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    private static List<SpPrincipal> getGroupPrincipals(final SpecifyUser user) 
    {

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List<?> principals = session.getDataList("SELECT pc FROM SpPrincipal as pc " +
            		"INNER JOIN pc.specifyUsers as user WHERE " +
            		    "groupSubClass = 'edu.ku.brc.af.auth.specify.principal.GroupPrincipal' " +
            		    "AND user.id = " + user.getId());
            return (List<SpPrincipal>) principals;
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            e.printStackTrace();
        }
        finally
        {
            session.close();
        }
        
        return null;
    }
    
    /**
     * Get all permissions that may override the user's own permissions. It's for display purposes,
     * so we don't need to include more than one permission per kind of permission.
     *  
     * @param user
     * @return
     */
    public static Hashtable<String, SpPermission> getOverridingPermissions(final SpecifyUser user) 
    {
        Hashtable<String, SpPermission> hash        = new Hashtable<String, SpPermission>();
        DataProviderSessionIFace        session     = DataProviderFactory.getInstance().createSession();
        List<SpPrincipal>               principals  = getGroupPrincipals(user);
        String                          strSet      = getPrincipalSet(principals); 
        try
        {
            List<?> perms = session.getDataList("SELECT pm FROM SpPermission as pm INNER JOIN FETCH pm.principals as pc WHERE pc.id in " + strSet);
            for (Object permObj : perms)
            {
                SpPermission perm = (SpPermission)permObj;
                hash.put(perm.getName(), perm);
                log.debug(perm.getName()+"  "+perm.getActions());
            }
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            e.printStackTrace();
        }
        finally
        {
            session.close();
        }
        
        return hash;
    }
    
    /**
     * @param principals
     * @return
     */
    private static String getPrincipalSet(final List<SpPrincipal> principals) 
    {
        StringBuffer inClause = new StringBuffer();
        inClause.append("(");
        boolean first = true;
        for (SpPrincipal spPrincipal : principals)
        {
            if (!first) 
            {
                inClause.append(", ");
            }
            else 
            {
                first = false;
            } 
            inClause.append(spPrincipal.getId());
        }
        inClause.append(")");
        return inClause.toString();
    }
    
    /**
     * @param principalId
     * @return
     */
    public static Hashtable<String, SpPermission> getExistingPermissions(final Integer principalId)
    {
    	Hashtable<String, SpPermission> hash    = new Hashtable<String, SpPermission>();
        DataProviderSessionIFace        session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
        	List<?> perms = session.getDataList("SELECT pm FROM SpPermission as pm INNER JOIN FETCH pm.principals as pc WHERE pc.id = " + principalId);
        	for (Object permObj : perms)
        	{
        		SpPermission perm = (SpPermission)permObj;
        		hash.put(perm.getName(), perm);
        		//log.debug(principalId+"  "+perm.getName()+"  "+perm.getActions());
        	}
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
        	e.printStackTrace();
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return hash;
    }
    
    
    /**
     * @param principalId
     * @return
     * @throws SQLException
     */
    public static List<Permission> findPrincipalBasedPermissions(Integer principalId)
    {
        if(debug)log.debug("findPrincipalBasedPermissions - principalId: "+ principalId); //$NON-NLS-1$
        List<Permission> perms = new ArrayList<Permission>();
        Connection        conn = null;
        PreparedStatement pstmt = null;
        try
        {
            Collection collection = (Collection)AppContextMgr.getInstance().getClassObject(Collection.class); 
            conn = DatabaseService.getInstance().getConnection();
            String sql = String.format("SELECT pm.SpPermissionID, pm.PermissionClass, pm.Name, pm.Actions, p.userGroupScopeID FROM sppermission AS pm " +
                                        "Inner Join spprincipal_sppermission AS sp ON pm.SpPermissionID = sp.SpPermissionID " +
                                        "Inner Join spprincipal AS p ON sp.SpPrincipalID = p.SpPrincipalID " +
                                        "WHERE p.SpPrincipalID = %d AND p.userGroupScopeID = %d", principalId, collection.getId());
            if(debug)log.debug("sql: " + sql); //$NON-NLS-1$
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Integer id      = rs.getInt("SpPermissionID");     //$NON-NLS-1$
                String clazzStr = rs.getString("PermissionClass"); //$NON-NLS-1$
                String name     = rs.getString("Name");            //$NON-NLS-1$
                String actions  = rs.getString("Actions");         //$NON-NLS-1$

                if(debug)log.debug("findPermissions()Permission found:  id={" //$NON-NLS-1$
                        +id+"}, class={" //$NON-NLS-1$
                        +clazzStr+"}, name={" //$NON-NLS-1$
                        +name+"}, actions={" //$NON-NLS-1$
                        +actions+"}"); //$NON-NLS-1$
                Permission perm = createPrincipalBasedPermission(id, clazzStr, name, actions);
                if (perm != null)
                {
                    perms.add(perm);
                } else
                {
                    continue;
                }

            }
        }
        catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (pstmt != null)  pstmt.close(); 
                
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return perms;
    }

    /**
     * @param id
     * @param clazzStr
     * @param name
     * @param actions
     * @return
     */
    private static Permission createPrincipalBasedPermission(final Integer id, final String clazzStr, final String name, final String actions)
    {
        if (debug) log.debug("createPrincipalBasedPermission - [" + clazzStr + "] [" + name + "] [" + actions + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Permission perm = null;
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(clazzStr);
        } catch (ClassNotFoundException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("ClassNotFoundException: " + e.getMessage()); //$NON-NLS-1$
        }

        if (clazz == null)
        {
            perm = new UnresolvedPermission(clazzStr, name, actions, EMPTY_CERTS);

        } 
        else if (clazz.equals(java.io.FilePermission.class))
        {
            perm = new FilePermission(name, actions);
        }
        else if (Permission.class.isAssignableFrom(clazz))
        {
            try
            {
                if (name == null && actions == null)
                {
                    Constructor<?> con = clazz.getConstructor(ZERO_ARGS);
                    perm = (Permission)con.newInstance(ZERO_OBJS);
                } else if (actions == null)
                {
                    Constructor<?> con = clazz.getConstructor(ONE_STRING_ARG);
                    String[] args = new String[] { name };
                    perm = (Permission)con.newInstance((Object[]) args);
                }
                // BasicPermission types
                else if (name != null && actions != null)
                {
                    Constructor<?> con = clazz.getConstructor(TWO_STRING_ARGS);
                    String[] args = new String[] { name, actions };
                    perm = (Permission)con.newInstance((Object[]) args);
                } else
                {
                    log.error("findPermissions() No suitable constructor (default, one String, or two String args) found to create Permission of type ["+clazz+"]. Skipping"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } catch (NoSuchMethodException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazzStr+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (InstantiationException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazz+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (IllegalAccessException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazz+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (InvocationTargetException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazz+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else
        {
            log.error("findPermissions() Permission with Id {"+id+"}has unsupported type of {"+clazz+"}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return perm;
    }

//    /**
//     * @param principalId
//     * @param dbPermission
//     * @throws SQLException
//     */
//    public static void addPermission(Integer principalId, DatabasePermission dbPermission) throws SQLException
//    {
//        addPermission( principalId, /*dbPermission.getId(),*/ dbPermission);
//    }
    
    static private boolean joinSpPrincipalPermission(SpPrincipal sp, Permission permission)
    {
        if(debug)log.debug("joinSpPrincipalPermission"); //$NON-NLS-1$
        
        Connection        conn         = null;
        PreparedStatement pstmt        = null;
        Integer           principalId  = sp.getId();
        Integer           permissionId = getPermissionsId(permission);
        
        if (principalId == null || permissionId == null)
        {
            return false;
        }
        
        if (!doesSpPrincipalHavePermission(sp, permission))
        {
            try
            {
                //XXX convert to hibernate
                conn = DatabaseService.getInstance().getConnection();
                String query = "INSERT INTO spprincipal_sppermission VALUES (" + principalId + ", " //$NON-NLS-1$ //$NON-NLS-2$
                        + permissionId + ")"; //$NON-NLS-1$
                pstmt = conn.prepareStatement(query);
                if(debug)log.debug("joinSpPrincipalPermission - executing: " + query); //$NON-NLS-1$
                pstmt.executeUpdate();
                conn.close();
                return true;
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e); //$NON-NLS-1$
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (conn != null)  conn.close();
                    if (pstmt != null)  pstmt.close(); 
                } catch (SQLException e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                    log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }   
        return false;
    }
    
    /**
     * @param sp
     * @param permission
     * @return
     */
    private static boolean doesSpPrincipalHavePermission(SpPrincipal sp, Permission permission)
    {
        if(debug)log.debug("doesSpPrincipalHavePermission"); //$NON-NLS-1$
        
        boolean           isPermissionGranted = false;
        Connection        conn                = null;
        PreparedStatement pstmt               = null;
        Integer           principalId         = sp.getId();
        Integer           permissionId        = getPermissionsId(permission);
        
        if (principalId == null || permissionId == null)
        {
            return false;
        }
        
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection(); 
            String query = "SELECT count(*) from spprincipal_sppermission " //$NON-NLS-1$
                + "WHERE spprincipal_sppermission.SpPrincipalID="+principalId+" " //$NON-NLS-1$ //$NON-NLS-2$
                + "AND spprincipal_sppermission.SpPermissionID="+permissionId+" "; //$NON-NLS-1$ //$NON-NLS-2$
            pstmt = conn.prepareStatement(query);
            if(debug)log.debug("doesSpPrincipalHavePermission - executing: " + query); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                int i = rs.getInt(1);
                if (i > 0)
                {
                    isPermissionGranted = true;
                    if(debug)log.debug("doesSpPrincipalHavePermission -   permission is already granted"); //$NON-NLS-1$
                }
            }        
        }
        catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (pstmt != null)  pstmt.close(); 
                
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return isPermissionGranted;  
    }
    
    /**
     * @param permission
     * @return
     */
    private static Integer getPermissionsId(Permission permission)
    {
        if(debug)log.debug("getPermissionsId"); //$NON-NLS-1$
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection();   
            String query = "SELECT sppermission.SpPermissionID FROM sppermission WHERE sppermission.Actions='"+permission.getActions()+"' " //$NON-NLS-1$ //$NON-NLS-2$
            + "AND sppermission.Name='" + permission.getName() + "' " //$NON-NLS-1$ //$NON-NLS-2$
            + "AND sppermission.PermissionClass='"+permission.getClass().getCanonicalName()+ "' "; //$NON-NLS-1$ //$NON-NLS-2$
             pstmt = conn.prepareStatement(BasicSQLUtils.escapeStringLiterals(query));
            if(debug)log.debug("executing: " + query); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Integer id = rs.getInt("SpPermissionID"); //$NON-NLS-1$
                if(debug)log.debug("getPermissionsId() found: sppermission.SpPermissionID="+id); //$NON-NLS-1$
                return id;
            }
            return null;
            
        }
        catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (pstmt != null)  pstmt.close(); 
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * @param sp
     * @param permission
     */
    public static void giveSpPrincipalPermission(final SpPrincipal sp, final Permission permission)
    {
        createPermission(permission);
        joinSpPrincipalPermission(sp, permission);
    }
    
    /**
     * @param permission
     */
    private static void createPermission(final Permission permission) 
    {
        Connection conn = null;
        PreparedStatement pstmt = null; 
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection();            
            pstmt = conn.prepareStatement("INSERT INTO sppermission (Actions, Name, PermissionClass) VALUES (?, ?, ?)");         //$NON-NLS-1$
            pstmt.setString(1, permission.getActions());
            pstmt.setString(2, permission.getName());
            pstmt.setString(3, permission.getClass().getName());
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (pstmt != null)  pstmt.close(); 
                if (conn != null)  conn.close();
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        } 
    }
    
    /**
     * @param name
     * @return
     */
    public static SpPrincipal getSpPrincipalByName(final String name)
    {
        // log.debug("getSpPrincipalByName: " + name);
        final DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        SpPrincipal principal = null;
        try
        {
            final List<?> lister = session.getDataList(SpPrincipal.class, "name", name); //$NON-NLS-1$
            if (lister.size() == 0)  return null;
            principal = (SpPrincipal)lister.get(0);
            
        } catch (final Exception e1)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e1);
            log.error(e1);
            e1.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return principal;
    }  
    
    /**
     * @param s
     * @param perm
     * @return
     */
    public static boolean runCheckPermssion(final Subject s, final Permission perm)
    {
        if(debug)log.debug(String.format("runCheckPermssion - calling doAsPrivileged to check if subject has permission [%s] [%s]", perm.getName(), perm.getActions())); //$NON-NLS-1$
        try
        {
            //log.debug("runCheckPermssion: calling doAsPrivileged"); //$NON-NLS-1$
            Subject.doAsPrivileged(s, new PrivilegedAction<Object>() {
                public Object run()
                {
                    //log.debug("runCheckPermssion: checking permission"); //$NON-NLS-1$
                    AccessController.checkPermission(perm);
                    if(debug)log.debug("runCheckPermssion - permission found, returning true"); //$NON-NLS-1$
                    return true;
                }

            }, null);
            return true;
        } catch (SecurityException e)
        {
            //log.warn("runCheckPermssion - Does not have permission" + perm.toString()); //$NON-NLS-1$
            
        } catch (Exception ee)
        {
            log.error("runCheckPermssion - exception caught"); //$NON-NLS-1$
            ee.printStackTrace();
            log.error(ee.getCause());
            log.error(ee.getMessage());
        }
        if(debug)log.debug("runCheckPermssion - permission NOT granted"); //$NON-NLS-1$
        return false;
    }
}
