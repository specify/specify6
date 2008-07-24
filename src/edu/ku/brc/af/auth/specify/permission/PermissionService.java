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
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

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
    static public void removePermission(Integer id) throws SQLException
    {
        removePrincipalPermissions(Collections.singleton(id));
    }

    /**
     * @param ids
     * @throws SQLException
     */
    static public void removePrincipalPermissions(Set<?> ids) throws SQLException
    {
        Connection conn = null;
        PreparedStatement tiePstmt = null;
        PreparedStatement permPstmt = null;
        try
        {
            //XXX convert to hibernate
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
    static public List<Permission> findPrincipalPermissions(Set<Integer> principalIds) 
    {
        if(debug)log.debug("findPrincipalPermissions"); //$NON-NLS-1$
        List<Permission> permissions = new ArrayList<Permission>();
        for (Iterator<Integer> itr = principalIds.iterator(); itr.hasNext();)
        {
            Integer principalId = (Integer)itr.next();
            if(debug)log.debug("findPrincipalPermissions - principalID" + principalId); //$NON-NLS-1$
            permissions.addAll(findPrincipalBasedPermissions(principalId));
        }
        return permissions;
    }
    
    static public Hashtable<String, SpPermission> getExistingPermissions(Integer principalId)
    {
    	Hashtable<String, SpPermission> hash = new Hashtable<String, SpPermission>();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	List<?> perms = session.getDataList("FROM SpPermission as pm " +
        			"INNER JOIN pm.principals as pc " + 
        			"WHERE pc.id = " + principalId);
        	for (Object permObj : perms)
        	{
        		Object[] permObjArr = (Object[]) permObj;
        		SpPermission perm = (SpPermission) permObjArr[0];
        		hash.put(perm.getName(), perm);
        	}
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	session.close();
        }
        
        return hash;
    }
    
    
    /**
     * @param principalId
     * @return
     * @throws SQLException
     */
    static public List<Permission> findPrincipalBasedPermissions(Integer principalId)
    {
        if(debug)log.debug("findPrincipalBasedPermissions"); //$NON-NLS-1$
        List<Permission> perms = new ArrayList<Permission>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection();
            String sql = "SELECT sppermission.SpPermissionID SpPermissionID, " //$NON-NLS-1$
                    + "sppermission.PermissionClass PermissionClass, SpPermission.Name Name, " //$NON-NLS-1$
                    + "sppermission.Actions Actions " + "FROM spprincipal_sppermission, SpPermission " //$NON-NLS-1$ //$NON-NLS-2$
                    + "WHERE spprincipal_sppermission.SpPrincipalID="+principalId+" " //$NON-NLS-1$ //$NON-NLS-2$
                    + "AND sppermission.SpPermissionID=spprincipal_sppermission.SpPermissionID "; //$NON-NLS-1$
            log.debug("sql: " + sql); //$NON-NLS-1$
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Integer id = rs.getInt("SpPermissionID"); //$NON-NLS-1$
                String clazzStr = rs.getString("PermissionClass"); //$NON-NLS-1$
                String name = rs.getString("Name"); //$NON-NLS-1$
                String actions = rs.getString("Actions"); //$NON-NLS-1$

                log.debug("findPermissions()Permission found:  id={" //$NON-NLS-1$
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
    private static Permission createPrincipalBasedPermission(Integer id, String clazzStr, String name, String actions)
    {
        if (debug) log.debug("createPrincipalBasedPermission - [" + clazzStr + "] [" + name + "] [" + actions + "]");; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Permission perm = null;
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(clazzStr);
        } catch (ClassNotFoundException e)
        {
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
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazzStr+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (InstantiationException e)
            {
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazz+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (IllegalAccessException e)
            {
                log.error("findPermissions() Constructor for Permission with Id {"+id+"}of {"+clazz+"} threw Exception {"+e+"}. Skipping."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (InvocationTargetException e)
            {
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
//    static public void addPermission(Integer principalId, DatabasePermission dbPermission) throws SQLException
//    {
//        addPermission( principalId, /*dbPermission.getId(),*/ dbPermission);
//    }
    
    static private boolean joinSpPrincipalPermission(SpPrincipal sp, Permission permission)
    {
        log.debug("joinSpPrincipalPermission"); //$NON-NLS-1$
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer principalId = sp.getId();
        Integer permissionId = getPermissionsId(permission);
        if (principalId == null || permissionId == null)
            return false;
        if (!doesSpPrincipalHavePermission(sp, permission))
        {
            try
            {
                //XXX convert to hibernate
                conn = DatabaseService.getInstance().getConnection();
                String query = "INSERT INTO spprincipal_sppermission VALUES (" + principalId + ", " //$NON-NLS-1$ //$NON-NLS-2$
                        + permissionId + ")"; //$NON-NLS-1$
                pstmt = conn.prepareStatement(query);
                log.debug("joinSpPrincipalPermission - executing: " + query); //$NON-NLS-1$
                pstmt.executeUpdate();
                conn.close();
                return true;
            } catch (SQLException e)
            {
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
                    log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }   
        return false;
    }
    
    private static boolean doesSpPrincipalHavePermission(SpPrincipal sp, Permission permission)
    {
        log.debug("doesSpPrincipalHavePermission"); //$NON-NLS-1$
        boolean isPermissionGranted = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        Integer principalId = sp.getId();
        Integer permissionId = getPermissionsId(permission);
        if(principalId ==null || permissionId==null)
            return false;
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection(); 
            String query = "SELECT count(*) from spprincipal_sppermission " //$NON-NLS-1$
                + "WHERE spprincipal_sppermission.SpPrincipalID="+principalId+" " //$NON-NLS-1$ //$NON-NLS-2$
                + "AND spprincipal_sppermission.SpPermissionID="+permissionId+" "; //$NON-NLS-1$ //$NON-NLS-2$
            pstmt = conn.prepareStatement(query);
            log.debug("doesSpPrincipalHavePermission - executing: " + query); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                int i = rs.getInt(1);
                if (i > 0)
                {
                    isPermissionGranted = true;
                    log.debug("doesSpPrincipalHavePermission -   permission is already granted"); //$NON-NLS-1$
                }
            }        
        }
        catch (SQLException e)
        {
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
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return isPermissionGranted;  
    }
    
    private static Integer getPermissionsId(Permission permission)
    {
        log.debug("getPermissionsId"); //$NON-NLS-1$
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            //XXX convert to hibernate
            conn = DatabaseService.getInstance().getConnection();   
            String query = "SELECT sppermission.SpPermissionID FROM sppermission WHERE sppermission.Actions=\""+permission.getActions()+"\" " //$NON-NLS-1$ //$NON-NLS-2$
            + "AND sppermission.Name=\""+permission.getName() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
            + "AND sppermission.PermissionClass=\""+permission.getClass().getCanonicalName()+ "\" "; //$NON-NLS-1$ //$NON-NLS-2$
             pstmt = conn.prepareStatement(query);
            log.debug("executing: " + query); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                Integer id = rs.getInt("SpPermissionID"); //$NON-NLS-1$
                log.debug("getPermissionsId() found: sppermission.SpPermissionID="+id); //$NON-NLS-1$
                return id;
            }
            return null;
            
        }
        catch (SQLException e)
        {
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
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * @param principalId
     * @param spPermissionId
     * @param permission
     * @throws SQLException
     */
    static public void giveSpPrincipalPermission(SpPrincipal sp, Permission permission)
    {
        createPermission(permission);
        joinSpPrincipalPermission(sp, permission);
    }
    
    private static void createPermission(Permission permission) 
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
            conn.close();
        }
        catch (SQLException e)
        {
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
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        } 
    }
    
    /**
     * 
     */
    public static SpPrincipal getSpPrincipalByName(String name)
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
            log.error(e1);
            e1.printStackTrace();
        } finally
        {
            if (session != null)
            {
                session.close();
                return principal;
            }
        }
        return null;
    }  
    
    public static boolean runCheckPermssion(Subject s, final Permission perm)
    {
        log.debug("runCheckPermssion - calling doAsPrivileged to check if subject has permission"); //$NON-NLS-1$
        try
        {
            log.debug("runCheckPermssion: calling doAsPrivileged"); //$NON-NLS-1$
            Subject.doAsPrivileged(s, new PrivilegedAction<Object>() {
                public Object run()
                {
                    log.debug("runCheckPermssion: checking permission"); //$NON-NLS-1$
                    AccessController.checkPermission(perm);
                    log.debug("runCheckPermssion - permission found, returning true"); //$NON-NLS-1$
                    return true;
                }

            }, null);
            return true;
        } catch (SecurityException e)
        {
            log.warn("runCheckPermssion - Does not have permission" + perm.toString()); //$NON-NLS-1$
        } catch (Exception ee)
        {
            log.error("runCheckPermssion - exception caught"); //$NON-NLS-1$
            ee.printStackTrace();
            log.error(ee.getCause());
            log.error(ee.getMessage());
        }
        log.debug("runCheckPermssion - permission NOT granted"); //$NON-NLS-1$
        return false;
    }
}