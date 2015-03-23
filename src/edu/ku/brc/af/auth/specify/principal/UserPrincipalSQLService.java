/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.auth.specify.principal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.policy.DatabaseService;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * @author megkumin
 * @author rods
 * 
 * @code_status Alpha
 * 
 */
public class UserPrincipalSQLService
{
    protected static final Logger log = Logger.getLogger(UserPrincipalSQLService.class);
    protected static final boolean isDebug = false;
    
    /**
     * 
     */
    public UserPrincipalSQLService()
    {
        
    }

    /**
     * Retrieves the SpecifyUser ID of a given user principal 
     * @param principal
     * @return
     */
    public static int getSpecifyUserId(final SpPrincipal principal)
    {
    	int result = -1; 
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                pstmt = conn.prepareStatement("SELECT specifyuser_spprincipal.SpecifyUserID "
                                + " FROM specifyuser_spprincipal WHERE SpPrincipalID=?");
                pstmt.setInt(1, principal.getId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next())
                {
                	result = rs.getInt("SpecifyUserID");
                }
            } else
            {
                log.error("getSpecifyUserId - database connection was null");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error("Exception caught: " + e);
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
            
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if(pstmt != null)  pstmt.close(); 
                
            } catch (SQLException e)
            {
                log.error("Exception caught: " + e.toString());
                e.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
            }
        }
        return result;
    }
    
    /**
     * @param userId
     * @return
     */
    /*public static ResultSet getUsersPrincipalsByUserId(final String userId)
    {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try
        {
            if (isDebug) log.debug("getUsersPrincipalsByUserId: " + userId); //$NON-NLS-1$
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                pstmt = conn.prepareStatement("SELECT specifyuser_spprincipal.SpPrincipalID " //$NON-NLS-1$
                                + " FROM specifyuser_spprincipal WHERE SpecifyUserID=?"); //$NON-NLS-1$
                pstmt.setString(1, userId);
                if (isDebug) log.debug("executing: " + pstmt.toString()); //$NON-NLS-1$
                rs = pstmt.executeQuery();
            } else
            {
                log.error("getUsersPrincipalsByUserId - database connection was null"); //$NON-NLS-1$
            }
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if(pstmt != null)  pstmt.close(); 
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return rs;
    }*/

    /**
     * @param principalId
     * @return
     */
    public static boolean isPrincipalAdmin(final Integer principalId)
    {
        String sql = "SELECT count(up.SpPrincipalID) as ct" //$NON-NLS-1$
                        + " FROM specifyuser_spprincipal as up" //$NON-NLS-1$
                        + " INNER JOIN spprincipal as p on (up.SpPrincipalID=p.SpPrincipalID)" //$NON-NLS-1$
                        + " WHERE p.GroupSubClass='" + AdminPrincipal.class.getCanonicalName() + "'"
                        + " AND up.SpecifyUserID=" + principalId;
        Integer count = BasicSQLUtils.getCount(sql);
        return count != null && count > 0;
    }
    
    /**
     * @param msg
     * @param sql
     * @param args
     * @return
     */
    protected static boolean performUpdate(final String msg, final String sql, final Object...args)
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                pstmt = conn.prepareStatement(sql); //$NON-NLS-1$
                for (int i=0;i<args.length;i++)
                {
                    pstmt.setString(i+1, args[i].toString());
                }
                return 0 < pstmt.executeUpdate();
                
            } else
            {
                log.error(msg + " - database connection was null"); //$NON-NLS-1$
            }
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
        } finally
        {
            try
            {
                if (conn != null)  conn.close();
                if(pstmt != null)  pstmt.close(); 
                
            } catch (SQLException e) { }
        }
        return false;
    }

    
    /**
     * @param userId
     * @param userGroupId
     * @return
     */
    public static boolean deleteFromUserGroup(final Integer userId, final Integer userGroupId)
    {
        return performUpdate("deleteFromUserGroup", 
                             "DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?",
                             userId, userGroupId);
    }


    /**
     * @param userId
     * @param userGroupId
     * @return
     */
    public static boolean addToUserGroup(final Integer userId, final Integer userGroupId)
    {
        return performUpdate("addToUserGroup",
                             "INSERT INTO specifyuser_spprincipal VALUES (?, ?)",
                             userId, userGroupId);
    }

    /**
     * @param username
     * @param password
     * @throws SQLException
     */
    public static void addUser(final String username, final String password) throws SQLException
    {
        performUpdate("addUser",
                      "INSERT INTO specifyuser (Name, Password) VALUES (?, ?)",
                      username, password);
    }

    /**
     * @param userName
     * @return
     */
    public static String getUsersIdByName(final String userName)
    {
        Connection conn = null;
        String id = null;
        PreparedStatement pstmt = null;
        try
        {
            if (isDebug) log.debug("getUsersIdByName: " + userName); //$NON-NLS-1$
            conn  = DatabaseService.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT specifyuser.SpecifyUserID FROM specifyuser WHERE name=?"); //$NON-NLS-1$
            pstmt.setString(1, userName);
            if (isDebug) log.debug("executing: " + pstmt.toString()); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                id = rs.getString("SpecifyUserID"); //$NON-NLS-1$
            }
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
            } catch (SQLException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
                log.error("addUser Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return id;
    }

    /**
     * @param user
     * @param pass
     * @param driverClass
     * @param url
     * @return
     * @throws Exception
     */
    public static Set<SpPrincipal> getUsersGroupsByUsername(final String user) throws Exception
    {
        if (isDebug) log.debug("findGroups() called"); //$NON-NLS-1$
        Set<SpPrincipal>  principals = new HashSet<SpPrincipal>();
        Connection        conn       = null;
        PreparedStatement pstmt      = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            if (isDebug) log.debug("findGroups() called.  user:" + user); //$NON-NLS-1$
            conn = DatabaseService.getInstance().getConnection();
            String myUserId = UserPrincipalSQLService.getUsersIdByName(user);
            String sql = "SELECT specifyuser_spprincipal.SpPrincipalID FROM specifyuser_spprincipal WHERE SpecifyUserID=?"; //$NON-NLS-1$
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, myUserId);
            if (isDebug) log.debug("executing: " + pstmt.toString()); //$NON-NLS-1$
            
            ResultSet spPrincipalIDSet = pstmt.executeQuery();
            while (spPrincipalIDSet.next())
            {
                String princId = spPrincipalIDSet.getString("SpPrincipalID"); //$NON-NLS-1$
                Integer princIdInt = spPrincipalIDSet.getInt("SpPrincipalID"); //$NON-NLS-1$
                sql = "SELECT distinct(spprincipal.name), spprincipal.groupsubclass " //$NON-NLS-1$
                        + "FROM specifyuser_spprincipal, spprincipal " //$NON-NLS-1$
                        + "WHERE (specifyuser_spprincipal.specifyuserid= ? " //$NON-NLS-1$
                        + "AND specifyuser_spprincipal.spprincipalid= ? " //$NON-NLS-1$
                        + "AND spprincipal.spprincipalid= ?)"; //$NON-NLS-1$
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, myUserId);
                pstmt.setString(2, princId);
                pstmt.setString(3, princId);
                //if (isDebug) log.debug("findGroups() executing query:" + pstmt.toString()); //$NON-NLS-1$
                ResultSet rs = pstmt.executeQuery();
                while (rs.next())
                {
                    String      groupName = rs.getString("name"); //$NON-NLS-1$
                    String      className = rs.getString("groupsubclass"); //$NON-NLS-1$
                    SpPrincipal grp       = new SpPrincipal(princIdInt);
                    grp.setName(groupName);
                    grp.setGroupSubClass(className);
                    principals.add(grp);
                }
            }

        } catch (SQLException e)
        {
            log.error("Exception caught: " + e); //$NON-NLS-1$
            e.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalSQLService.class, e);
            
        } finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
                
            } catch (SQLException e) {}
        }
        return principals;
    }
}
