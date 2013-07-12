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

    /**
     * @param user
     * @return
     */
    static public SpPrincipal getUserPrincipalBySpecifyUser(final SpecifyUser user)
    {
        DataProviderSessionIFace session = null;
        SpPrincipal principal = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            String sql = "FROM SpPrincipal as pc INNER JOIN FETCH pc.specifyUsers as sp WHERE groupSubClass='" + UserPrincipal.class.getCanonicalName() + "' AND sp.name = '" + user.getName() + "'";
            log.debug(sql);
            final List<?> lister = session.getDataList(sql);
            if (lister != null && lister.size() > 0)
            {
                principal = (SpPrincipal)lister.get(0);
                principal.getPermissions().size();
            }
            
        } catch (final Exception e1)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e1);
            log.error("Exception caught: " + e1.toString());
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
     * @param name
     * @return
     */
    static public SpecifyUser getUserByName(final String name)
    {
        DataProviderSessionIFace session = null;
        @SuppressWarnings("unused") //$NON-NLS-1$
        SpecifyUser user = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            final List<?> lister = session.getDataList(SpecifyUser.class, "name", name); //$NON-NLS-1$
            user = (SpecifyUser)lister.get(0);
            
        } catch (final Exception e1)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e1);
            log.error("Exception caught: " + e1.toString()); //$NON-NLS-1$
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

    /**
     * @param user
     * @param group
     * @return
     */
    static public boolean addToUserGroup(SpecifyUser user, SpPrincipal group)
    {
        Connection conn = null;
        try
        {
            log.debug("executing sql to add user to group"); //$NON-NLS-1$
            
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO specifyuser_spprincipal VALUES (?, ?)"); //$NON-NLS-1$
            pstmt.setString(1, user.getId() + ""); //$NON-NLS-1$
            pstmt.setString(2, group.getId() + ""); //$NON-NLS-1$
            log.debug("executing: " + pstmt.toString()); //$NON-NLS-1$
            int res = pstmt.executeUpdate();
            return 0 < res;
            
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
            log.error("addToUserGroup - " + e); //$NON-NLS-1$
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
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
                    log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * @param user
     * @param group
     * @return
     */
    static public boolean isUserInGroup(SpecifyUser user, SpPrincipal group)
    {
        boolean    empty = true;
        Connection conn  = null;
        try
        {
            log.debug("checking to see if user is in group before attempting to delete"); //$NON-NLS-1$
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?"); //$NON-NLS-1$
            pstmt.setString(1, user.getId() + ""); //$NON-NLS-1$
            pstmt.setString(2, group.getId() + ""); //$NON-NLS-1$
            log.debug("executing: " + pstmt.toString()); //$NON-NLS-1$
            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                empty = false;
            }
            if (empty)
            {
                log.debug("User [" + user.getName() + "] does not belong to group [" //$NON-NLS-1$ //$NON-NLS-2$
                        + group.getName() + "]"); //$NON-NLS-1$
            } else
            {
                log.debug("User [" + user.getName() + "]  belongs to group [" + group.getName() //$NON-NLS-1$ //$NON-NLS-2$
                        + "]"); //$NON-NLS-1$
            }
            return !empty;
            
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
            log.error("Exception caught: " + e); //$NON-NLS-1$
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
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
                    log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
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

    /**
     * @param user
     * @param group
     * @return
     */
    static public boolean removeUserFromGroups(SpecifyUser user, SpPrincipal group)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        String qStr = "DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID = " + user.getId() + //$NON-NLS-1$
                      " AND SpPrincipalID=" + group.getId(); //$NON-NLS-1$
        session.createQuery(qStr, false).executeUpdate();
        session.close();
        return true;
    }

    /**
     * @param group
     * @return
     */
    static public boolean addUserGroup(SpPrincipal group)// throws SQLException
    {
        Connection conn = null;
        String sql = "INSERT INTO spprincipal (Name, ClassName) VALUES(?,?)"; //$NON-NLS-1$
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
            log.error("Executing sql" + pstmt.toString()); //$NON-NLS-1$
            log.error("Exception caught: " + e); //$NON-NLS-1$
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
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param group
     * @throws SQLException
     */
    static public void removeUserGroup(SpPrincipal group) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();

            PreparedStatement pstmtGrp = conn.prepareStatement("DELETE FROM specifyuser_spprincipal WHERE SpPrincipalID=?"); //$NON-NLS-1$
            pstmtGrp.setString(1, group.getId() + ""); //$NON-NLS-1$
            pstmtGrp.executeUpdate();

            PreparedStatement pstmt = conn .prepareStatement("DELETE FROM spprincipal WHERE SpPrincipalID=?"); //$NON-NLS-1$
            pstmt.setString(1, group.getId() + ""); //$NON-NLS-1$
            pstmt.executeUpdate();

        } catch (Exception e)


        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
            
        } finally
        {
            if (conn != null)
            {
                conn.close();
            }
        }

    }

    /**
     * @param user
     * @param group
     * @return
     */
    static public boolean removeUserFromGroup(SpecifyUser user, SpPrincipal group)
    {
        Connection conn = null;
        try
        {
            conn = DatabaseService.getInstance().getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("DELETE FROM specifyuser_spprincipal WHERE SpecifyUserID=? AND SpPrincipalID=?"); //$NON-NLS-1$
            pstmt.setString(1, user.getId() + ""); //$NON-NLS-1$
            pstmt.setString(2, group.getId() + ""); //$NON-NLS-1$
            int res = pstmt.executeUpdate();
            return 0 < res;
            
        } catch (SQLException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
            log.error("addToUserGroup - " + e); //$NON-NLS-1$
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
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UserPrincipalHibernateService.class, e);
                    log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
