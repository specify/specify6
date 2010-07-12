/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.auth.specify.policy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.util.Pair;

/**
 * @author michaelcote
 */
public class DatabaseService
{
    protected static final Logger     log           = Logger.getLogger(DatabaseService.class);
    protected static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //$NON-NLS-1$
    public static Timestamp           now           = new Timestamp(System.currentTimeMillis());
    public static String              nowStr        = dateFormatter.format(now);
    private static boolean            debug         = false;
    static private DatabaseService    INSTANCE      = new DatabaseService();
  
    private DatabaseService()
    {

    }

    /**
     * @param url
     * @param driverClass
     * @return
     */
    public static Connection getAdminLevelConnection(final String url, 
                                                     final String driverClass)
    {
        Connection con = null;
        //Statement stmt;
        try
        {
            if(debug)log.debug("getAdminLevelConnection - trying to connect with driver: " + driverClass); //$NON-NLS-1$
            Class.forName(driverClass);
            
        } catch (java.lang.ClassNotFoundException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatabaseService.class, e);
            log.error("getAdminLevelConnection - Could not connect to database, driverclass - ClassNotFoundException: "); //$NON-NLS-1$
            log.error("getAdminLevelConnection - " + e.getMessage()); //$NON-NLS-1$
        }

        try
        {
            if(debug)log.debug("getAdminLevelConnection - Trying to connect to: " + url); //$NON-NLS-1$
            if(debug)log.error("getAdminLevelConnection - Trying to connect with BUILT IN ADMIN LEVER USER ACCOUNT - need to address"); //$NON-NLS-1$
            Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
            con = DriverManager.getConnection(url, usernamePassword.first, usernamePassword.second);
            if(debug)log.debug("getAdminLevelConnection - connected!"); //$NON-NLS-1$
            return con;
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatabaseService.class, ex);
            log.error("getAdminLevelConnection - Exception: "); //$NON-NLS-1$
            log.error("getAdminLevelConnection - " + ex.getMessage()); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException
    {
        if(debug)log.debug("getConnection"); //$NON-NLS-1$
        try
        {
            Class.forName(JaasContext.driver);
            
        } catch (java.lang.ClassNotFoundException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatabaseService.class, e);
            log.error("getConnection - Could not connect to database, driverclass - ClassNotFoundException: "); //$NON-NLS-1$
            log.error("getConnection - "+ e.getMessage()); //$NON-NLS-1$
            // throw new LoginException("Database driver class not found: " + driverClass);
        }
        
        Pair<String, String> usernamePassword = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();

        if(debug)log.debug("getConnection -  url:" + JaasContext.url); //$NON-NLS-1$
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootUser:" + usernamePassword.first); //$NON-NLS-1$
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootPwd:" + usernamePassword.second); //$NON-NLS-1$
        try
        {
            Connection connection = DriverManager.getConnection(JaasContext.url, usernamePassword.first, usernamePassword.second);
            return connection;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }

    /**
     * @return
     */
    static public DatabaseService getInstance()
    {
        return INSTANCE;
    }
}
