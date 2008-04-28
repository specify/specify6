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
/**
 * 
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
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * @author megkumin
 * 
 * @code_status Alpha
 * 
 */
public class UserPrincipalSQLService
{
    protected static final Logger log = Logger.getLogger(UserPrincipalSQLService.class);

    /**
     * 
     */
    public UserPrincipalSQLService()
    {
    }

    static public ResultSet getUsersPrincipalsByUserId(String userId)
    {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try
        {
            log.debug("getUsersPrincipalsByUserId: " + userId);
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                pstmt = conn.prepareStatement("SELECT specifyuser_spprincipal.SpPrincipalID "
                                + " FROM specifyuser_spprincipal WHERE SpecifyUserID=?");
                pstmt.setString(1, userId);
                log.debug("executing: " + pstmt.toString());
                rs = pstmt.executeQuery();
            } else
            {
                log.error("getUsersPrincipalsByUserId - database connection was null");
            }
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
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
            }
        }
        return rs;
    }

    static public boolean deleteFromUserGroup(Integer userId, Integer userGroupId)
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                 pstmt = conn
                        .prepareStatement("DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?");
                pstmt.setString(1, userId + "");
                pstmt.setString(2, userGroupId + "");
                return 0 < pstmt.executeUpdate();
            } else
            {
                log.error("deleteFromUserGroup - database connection was null");
            }
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
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
            }
        }
        return false;
    }

    static public boolean addToUserGroup(Integer userId, Integer userGroupId)
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            if (conn != null)
            {
                pstmt = conn .prepareStatement("INSERT INTO specifyuser_spprincipal VALUES (?, ?)");
                pstmt.setString(1, userId + "");
                pstmt.setString(2, userGroupId + "");
                return 0 < pstmt.executeUpdate();
            }
            else
            {
                log.error("addToUserGroup - database connection was null");
            }
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
        }finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
            } catch (SQLException e)
            {
                log.error("addToUserGroup Exception caught: " + e.toString());
                e.printStackTrace();
            }
        }
        return false;
    }

    static public void addUser(String username, String password) throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            pstmt = conn.prepareStatement(
                    "INSERT INTO specifyuser (Name, Password) VALUES (?, ?)");
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        } 
        catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
        }finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
            } catch (SQLException e)
            {
                log.error("addUser Exception caught: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    static public String getUsersIdByName(String userName)
    {
        Connection conn = null;
        String id = null;
        PreparedStatement pstmt = null;
        try
        {
            log.debug("getUsersIdByName: " + userName);
            conn = DatabaseService.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT specifyuser.SpecifyUserID FROM specifyuser WHERE name=?");
            pstmt.setString(1, userName);
            log.debug("executing: " + pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                id = rs.getString("SpecifyUserID");
            }
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
            } catch (SQLException e)
            {
                log.error("addUser Exception caught: " + e.toString());
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
    public static Set<SpPrincipal> getUsersGroupsByUsername(String user) throws Exception
    {
        log.debug("findGroups() called");
        Set<SpPrincipal> principals = new HashSet<SpPrincipal>();
        Connection conn = null;
        PreparedStatement pstmt =null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            log.debug("findGroups() called.  user:" + user);
            conn = DatabaseService.getInstance().getConnection();
            String myUserId = UserPrincipalSQLService.getUsersIdByName(user);
            String sql = "SELECT specifyuser_spprincipal.SpPrincipalID "
                + "FROM specifyuser_spprincipal WHERE SpecifyUserID=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, myUserId);
            log.debug("executing: " + pstmt.toString());
            ResultSet spPrincipalIDSet = pstmt.executeQuery();
            while (spPrincipalIDSet.next())
            {
                String princId = spPrincipalIDSet.getString("SpPrincipalID");
                Integer princIdInt = spPrincipalIDSet.getInt("SpPrincipalID");
                sql = "SELECT distinct(spprincipal.name)," + "spprincipal.groupsubclass "
                        + "FROM specifyuser_spprincipal, spprincipal "
                        + "WHERE (specifyuser_spprincipal.specifyuserid= ? "
                        + "AND specifyuser_spprincipal.spprincipalid= ? "
                        + "AND spprincipal.spprincipalid= ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, myUserId);
                pstmt.setString(2, princId);
                pstmt.setString(3, princId);
                log.debug("findGroups() executing query:" + pstmt.toString());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next())
                {
                    String groupName = rs.getString("name");
                    String className = rs.getString("groupsubclass");
                    SpPrincipal grp = new SpPrincipal(princIdInt);
                    grp.setName(groupName);
                    grp.setGroupSubClass(className);
                    principals.add(grp);
                }
            }

        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
        } finally
        {
            try
            {
                if (conn != null)   conn.close();
                if (pstmt != null)  pstmt.close();
            } catch (SQLException e)
            {
                log.error("addUser Exception caught: " + e.toString());
                e.printStackTrace();
            }
        }
        return principals;
    }
}
