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
import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.policy.DatabaseService;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * @author megkumin
 * 
 * @code_status Alpha
 * 
 */
public class UserPrincipalHibernateService
{
    protected static final Logger log = Logger.getLogger(UserPrincipalHibernateService.class);

    /**
     * 
     */
    public UserPrincipalHibernateService()
    {
        // TODO Auto-generated constructor stub
    }

    static public SpecifyUser getUserByName(String name)
    {
        DataProviderSessionIFace session = null;
        @SuppressWarnings("unused")
        SpecifyUser user = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            final List<?> lister = session.getDataList(SpecifyUser.class, "name", name);
            user = (SpecifyUser)lister.get(0);
        } catch (final Exception e1)
        {
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return user;
    }

    // XXX TODO convert to hibernate
    static public boolean addToUserGroup(SpecifyUser user, SpPrincipal group)
    {
        Connection conn = null;
        try
        {
            log.debug("executing sql to add user to group");
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("INSERT INTO specifyuser_spprincipal VALUES (?, ?)");
            pstmt.setString(1, user.getId() + "");
            pstmt.setString(2, group.getId() + "");
            log.debug("executing: " + pstmt.toString());
            int res = pstmt.executeUpdate();
            return 0 < res;
        } catch (SQLException e)
        {
            log.error("addToUserGroup - " + e);
            e.printStackTrace();
        } finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    log.error("Exception caught: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // XXX TODO convert to hibernate
    static public boolean isUserInGroup(SpecifyUser user, SpPrincipal group)
    {
        boolean empty = true;
        Connection conn = null;
        try
        {
            log.debug("checking to see if user is in group before attempting to delete");
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?");
            pstmt.setString(1, user.getId() + "");
            pstmt.setString(2, group.getId() + "");
            log.debug("executing: " + pstmt.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                empty = false;
            }
            if (empty)
            {
                log.debug("User [" + user.getName() + "] does not belong to group ["
                        + group.getName() + "]");
            } else
            {
                log.debug("User [" + user.getName() + "]  belongs to group [" + group.getName()
                        + "]");
            }
            return !empty;
        } catch (SQLException e)
        {
            log.error("Exception caught: " + e);
            e.printStackTrace();
        } finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    log.error("Exception caught: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
        return empty;

        // DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        // String sql = "select count(SpecifyUserID) from specifyuser_spprincipal "
        // + "where SpecifyUserID = " + user.getId() + " AND SpPrincipalID=" + group.getId();
        // List<?> entries = session.createQuery(sql).list();
        // // Object result = session.getData("select count(SpecifyUserID) from
        // // edu.ku.brc.specify.datamodel.specifyuser_spprincipal "
        // // + "where SpecifyUserID = " + user.getId() + " AND SpPrincipalID="+ group.getId());
        // // session.close();
        // Object result = entries.get(0);
        // int count = result != null ? (Integer)result : 0;
        // if (count > 0)
        // return true;
        // return false;
    }

    // XXX TODO convert to hibernate
    static public boolean removeUserFromGroups(SpecifyUser user, SpPrincipal group)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        String qStr = "DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID = " + user.getId()
                + " AND SpPrincipalID=" + group.getId();
        session.createQuery(qStr).executeUpdate();
        session.close();
        return true;
    }

    // XXX TODO convert to hibernate
    static public boolean addUserGroup(SpPrincipal group)// throws SQLException
    {
        Connection conn = null;
        String sql = "INSERT INTO spprincipal (Name, ClassName) VALUES(?,?)";
        PreparedStatement pstmt = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getClass().getName());
            return 0 < pstmt.executeUpdate();
        } catch (Exception e)
        {
            log.error("Executing sql" + pstmt.toString());
            log.error("Exception caught: " + e);
            return false;
        } finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    // XXX TODO convert to hibernate
    static public void removeUserGroup(SpPrincipal group) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();

            PreparedStatement pstmtGrp = conn
                    .prepareStatement("DELETE FROM specifyuser_spprincipal WHERE SpPrincipalID=?");
            pstmtGrp.setString(1, group.getId() + "");
            pstmtGrp.executeUpdate();

            PreparedStatement pstmt = conn
                    .prepareStatement("DELETE FROM spprincipal WHERE SpPrincipalID=?");
            pstmt.setString(1, group.getId() + "");
            pstmt.executeUpdate();

        } catch (Exception e)

        {
            log.error("Exception caught: " + e);
        } finally
        {
            if (conn != null)
            {
                conn.close();
            }
        }

    }

    // XXX TODO convert to hibernate
    static public boolean removeUserFromGroup(SpecifyUser user, SpPrincipal group)
    {
        Connection conn = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?");
            pstmt.setString(1, user.getId() + "");
            pstmt.setString(2, group.getId() + "");
            int res = pstmt.executeUpdate();
            return 0 < res;
        } catch (SQLException e)
        {
            log.error("addToUserGroup - " + e);
            e.printStackTrace();
        } finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    log.error("Exception caught: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
