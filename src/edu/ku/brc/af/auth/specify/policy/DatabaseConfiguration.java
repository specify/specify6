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
    static private String url = "";
    static private String driver = "";
//    /**
//     * 
//     */
    public DatabaseConfiguration(String url, String driver)
    {
        log.debug("url: " + url);
        log.debug("driver: " + driver);
        DatabaseConfiguration.url = url;
        DatabaseConfiguration.driver = driver;
    }
    
    /**
     * 
     */
    static void init(String url, String driver)
    {
        log.debug("init");
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
        log.debug("deleteAppConfigurationEntry: appName[" + appName + "] loginModuleName[" +loginModuleName + "] url[" + url +"] driver[" + driver + "]");
        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "DELETE FROM sp_app_configuration "
                    + "WHERE appName=\""+appName+"\" AND loginModuleClass=\""+loginModuleName+"\"";
            log.debug("executing SQL: " + sql);
            
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
        log.debug("addAppConfigurationEntry: appName[" + appName + "] entry.getLoginModuleName()[" +entry.getLoginModuleName() + "] url[" + url +"] driver[" + driver + "]");//" + appName + " " +entry.getLoginModuleName() + " " + url +" " + driver);
        // insert an entry into the database for the LoginModule
        // indicated by the passed in AppConfigurationEntry
        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "INSERT INTO sp_app_configuration VALUES (\""+appName+"\", \""+entry.getLoginModuleName()+"\", \""+controlFlagString(entry.getControlFlag())+"\")";
            log.debug("executing SQL: " + sql);
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
            return "required";
        } 
        else if (LoginModuleControlFlag.REQUISITE.equals(flag))
        {
            return "requisite";
        } 
        else if (LoginModuleControlFlag.SUFFICIENT.equals(flag))
        {
            return "sufficient";
        } 
        else if (LoginModuleControlFlag.OPTIONAL.equals(flag))
        {
            return "optional";
        } 
        else
        {
            log.warn("resolveControlFlag is an unknown LoginModuleControlFlag. Returning LoginModuleControlFlag.OPTIONAL; flagpassed[" + flag+"]");
            return "OPTIONAL";
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
        log.debug("getAppConfigurationEntry: appName[" +applicationName + "]");
        log.debug("getAppConfigurationEntry: url[" +url + "]");
        log.debug("getAppConfigurationEntry: driver[" +driver + "]");
        if (applicationName == null) 
        { 
            log.error("getAppConfigurationEntry: Throwing Null pointer exception: applicationName passed in was null." );
            throw new NullPointerException("applicationName passed in was null."); 
        }

        Connection conn = null;
        try
        {
            conn = DatabaseService.getAdminLevelConnection(url, driver);//.getInstance().getConnection();;
            String sql = "SELECT loginModuleClass, controlFlag "
                    + "FROM app_configuration WHERE appName=\""+applicationName+"\"";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, applicationName);
            ResultSet rs = pstmt.executeQuery();
            List<AppConfigurationEntry> entries = new ArrayList<AppConfigurationEntry>();
            while (rs.next())
            {
                String loginModuleClass = rs.getString("loginModuleClass");
                String controlFlagValue = rs.getString("controlFlag");
                AppConfigurationEntry.LoginModuleControlFlag controlFlag = resolveControlFlag(controlFlagValue);
                AppConfigurationEntry entry = new AppConfigurationEntry(loginModuleClass,
                        controlFlag, new HashMap<String, Object>());
                entries.add(entry);
            }

            if (entries.isEmpty())
            {
                log.debug("getAppConfigurationEntry()  No AppConfigurationEntrys found for applicationName: " + applicationName);
            }
            return (AppConfigurationEntry[])entries.toArray(new AppConfigurationEntry[entries.size()]);
        } 
        catch (SQLException e)
        {
            log.error("getAppConfigurationEntry: SQLException retrieving for applicationName="+ applicationName, e);
            throw new RuntimeException("SQLException retrieving for applicationName="+ applicationName, e);
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
                    log.error("getAppConfigurationEntry() Couldn't close connection. SQLException: " + e.getSQLState());
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
        if (name == null) { throw new NullPointerException("control flag name passed in is null."); }

        String uppedName = name.toUpperCase(Locale.US);
        if ("REQUIRED".equals(uppedName))
        {
            return LoginModuleControlFlag.REQUIRED;
        } 
        else if ("REQUISITE".equals(uppedName))
        {
            return LoginModuleControlFlag.REQUISITE;
        } 
        else if ("SUFFICIENT".equals(uppedName))
        {
            return LoginModuleControlFlag.SUFFICIENT;
        } 
        else if ("OPTIONAL".equals(uppedName))
        {
            return LoginModuleControlFlag.OPTIONAL;
        } 
        else
        {
            log.error("resolveControlFlag (up-cased) is an unknown String controlFlag. Returning OPTIONAL flag["+  uppedName +"]");
            return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
        }
    }
}
