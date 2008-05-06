/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.af.auth.specify.policy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;



/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class DatabaseConfiguration extends Configuration
{
    static private DatabaseConfiguration spDbConfig;
    protected static final Logger log = Logger.getLogger(DatabaseConfiguration.class);
    static private String url = ""; //$NON-NLS-1$
    static private String driver = ""; //$NON-NLS-1$
//    /**
//     * 
//     */
    public DatabaseConfiguration(String url, String driver)
    {
        log.debug("url: " + url); //$NON-NLS-1$
        log.debug("driver: " + driver); //$NON-NLS-1$
        DatabaseConfiguration.url = url;
        DatabaseConfiguration.driver = driver;
    }
    
    /**
     * 
     */
    static void init(String url, String driver)
    {
        log.debug("init"); //$NON-NLS-1$
        spDbConfig = new DatabaseConfiguration(url, driver);
        Configuration.setConfiguration(spDbConfig);
    }
    
    /**
     * @param appName
     * @param loginModuleName
     * @param url
     * @param driver
     * @return
     * @throws SQLException
     */
    public boolean deleteAppConfigurationEntry(String appName, String loginModuleName, String url, String driver)
            throws SQLException
    {
        log.debug("deleteAppConfigurationEntry: appName[" + appName + "] loginModuleName[" +loginModuleName + "] url[" + url +"] driver[" + driver + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "DELETE FROM sp_app_configuration " //$NON-NLS-1$
                    + "WHERE appName=\""+appName+"\" AND loginModuleClass=\""+loginModuleName+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            log.debug("executing SQL: " + sql); //$NON-NLS-1$
            
            //            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            //String sql = "DELETE FROM app_configuration "
            //        + "WHERE appName=? AND loginModuleClass=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, appName);
            //pstmt.setString(2, loginModuleName);
            return pstmt.executeUpdate() > 0;
        } finally
        {
            if (conn != null)
            {
                conn.close();
            }
        }
    }
    
    /**
     * @param appName
     * @param entry
     * @param url
     * @param driver
     * @throws SQLException
     */
    public void addAppConfigurationEntry(String appName, AppConfigurationEntry entry, String url, String driver)
    //{
            throws SQLException
    {
        log.debug("addAppConfigurationEntry: appName[" + appName + "] entry.getLoginModuleName()[" +entry.getLoginModuleName() + "] url[" + url +"] driver[" + driver + "]");//" + appName + " " +entry.getLoginModuleName() + " " + url +" " + driver); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        // insert an entry into the database for the LoginModule
        // indicated by the passed in AppConfigurationEntry
        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "INSERT INTO sp_app_configuration VALUES (\""+appName+"\", \""+entry.getLoginModuleName()+"\", \""+controlFlagString(entry.getControlFlag())+"\")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            log.debug("executing SQL: " + sql); //$NON-NLS-1$
            //String sql = "INSERT INTO app_configuration VALUES (?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, appName);
            //pstmt.setString(2, entry.getLoginModuleName());
            //pstmt.setString(3, controlFlagString(entry.getControlFlag()));
            pstmt.executeUpdate();
        } finally
        {
            if (conn != null)
            {
                conn.close();
            }
        }
    }
    
    /**
     * @param flag
     * @return
     */
    public String controlFlagString(LoginModuleControlFlag flag)
    {
        if (LoginModuleControlFlag.REQUIRED.equals(flag))
        {
            return "required"; //$NON-NLS-1$
        } 
        else if (LoginModuleControlFlag.REQUISITE.equals(flag))
        {
            return "requisite"; //$NON-NLS-1$
        } 
        else if (LoginModuleControlFlag.SUFFICIENT.equals(flag))
        {
            return "sufficient"; //$NON-NLS-1$
        } 
        else if (LoginModuleControlFlag.OPTIONAL.equals(flag))
        {
            return "optional"; //$NON-NLS-1$
        } 
        else
        {
            log.warn("resolveControlFlag is an unknown LoginModuleControlFlag. Returning LoginModuleControlFlag.OPTIONAL; flagpassed[" + flag+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            return "OPTIONAL"; //$NON-NLS-1$
        }

    }
    /**
     * @return
     */
    static public DatabaseConfiguration getDbConfiguration()
    {
        return spDbConfig;
    }
    
    /* (non-Javadoc)
     * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
     */
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName)
    {
        log.debug("getAppConfigurationEntry: appName[" +applicationName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        log.debug("getAppConfigurationEntry: url[" +url + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        log.debug("getAppConfigurationEntry: driver[" +driver + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        if (applicationName == null) 
        { 
            log.error("getAppConfigurationEntry: Throwing Null pointer exception: applicationName passed in was null." ); //$NON-NLS-1$
            throw new NullPointerException("applicationName passed in was null.");  //$NON-NLS-1$
        }

        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "SELECT loginModuleClass, controlFlag " //$NON-NLS-1$
                    + "FROM app_configuration WHERE appName=\""+applicationName+"\""; //$NON-NLS-1$ //$NON-NLS-2$
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, applicationName);
            ResultSet rs = pstmt.executeQuery();
            List<AppConfigurationEntry> entries = new ArrayList<AppConfigurationEntry>();
            while (rs.next())
            {
                String loginModuleClass = rs.getString("loginModuleClass"); //$NON-NLS-1$
                String controlFlagValue = rs.getString("controlFlag"); //$NON-NLS-1$
                AppConfigurationEntry.LoginModuleControlFlag controlFlag = resolveControlFlag(controlFlagValue);
                AppConfigurationEntry entry = new AppConfigurationEntry(loginModuleClass,
                        controlFlag, new HashMap<String, Object>());
                entries.add(entry);
            }

            if (entries.isEmpty())
            {
                log.debug("getAppConfigurationEntry()  No AppConfigurationEntrys found for applicationName: " + applicationName); //$NON-NLS-1$
            }
            return (AppConfigurationEntry[])entries.toArray(new AppConfigurationEntry[entries.size()]);
        } 
        catch (SQLException e)
        {
            log.error("getAppConfigurationEntry: SQLException retrieving for applicationName="+ applicationName, e); //$NON-NLS-1$
            throw new RuntimeException("SQLException retrieving for applicationName="+ applicationName, e); //$NON-NLS-1$
        } 
        finally
        {

            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    log.error("getAppConfigurationEntry() Couldn't close connection. SQLException: " + e.getSQLState()); //$NON-NLS-1$
                }
            }
        }
    }

//    /* (non-Javadoc)
//     * @see javax.security.auth.login.Configuration#getParameters()
//     */
//    @Override
//    public Parameters getParameters()
//    {
//        // TODO Auto-generated method stub
//        return super.getParameters();
//    }
//
//    /* (non-Javadoc)
//     * @see javax.security.auth.login.Configuration#getProvider()
//     */
//    @Override
//    public Provider getProvider()
//    {
//        // TODO Auto-generated method stub
//        return super.getProvider();
//    }
//
//    /* (non-Javadoc)
//     * @see javax.security.auth.login.Configuration#getType()
//     */
//    @Override
//    public String getType()
//    {
//        // TODO Auto-generated method stub
//        return super.getType();
//    }

    /* (non-Javadoc)
     * @see javax.security.auth.login.Configuration#refresh()
     */
    @Override
    public void refresh()
    {
        // TODO Auto-generated method stub
        //super.refresh();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }
    public LoginModuleControlFlag resolveControlFlag(String name)
    {
        if (name == null) { throw new NullPointerException("control flag name passed in is null."); } //$NON-NLS-1$

        String uppedName = name.toUpperCase(Locale.US);
        if ("REQUIRED".equals(uppedName)) //$NON-NLS-1$
        {
            return LoginModuleControlFlag.REQUIRED;
        } 
        else if ("REQUISITE".equals(uppedName)) //$NON-NLS-1$
        {
            return LoginModuleControlFlag.REQUISITE;
        } 
        else if ("SUFFICIENT".equals(uppedName)) //$NON-NLS-1$
        {
            return LoginModuleControlFlag.SUFFICIENT;
        } 
        else if ("OPTIONAL".equals(uppedName)) //$NON-NLS-1$
        {
            return LoginModuleControlFlag.OPTIONAL;
        } 
        else
        {
            log.error("resolveControlFlag (up-cased) is an unknown String controlFlag. Returning OPTIONAL flag["+  uppedName +"]"); //$NON-NLS-1$ //$NON-NLS-2$
            return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
        }
    }
}
