package edu.ku.brc.af.auth.specify.policy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.specify.SpecifySecurityMgr;

/**
 * @author michaelcote
 */
public class DatabaseService
{
    protected static final Logger     log           = Logger.getLogger(DatabaseService.class);
    protected static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
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
    public static Connection getAdminLevelConnection(String url, String driverClass)
    {
        Connection con = null;
        //Statement stmt;
        try
        {
            if(debug)log.debug("getAdminLevelConnection - trying to connect with driver: " + driverClass);
            Class.forName(driverClass);
        } catch (java.lang.ClassNotFoundException e)
        {
            log.error("getAdminLevelConnection - Could not connect to database, driverclass - ClassNotFoundException: ");
            log.error("getAdminLevelConnection - " + e.getMessage());
        }

        try
        {
            if(debug)log.debug("getAdminLevelConnection - Trying to connect to: " + url);
            if(debug)log.error("getAdminLevelConnection - Trying to connect with BUILT IN ADMIN LEVER USER ACCOUNT - need to address");
            con = DriverManager.getConnection(url, SpecifySecurityMgr.embeddedSpecifyAppRootUser, SpecifySecurityMgr.embeddedSpecifyAppRootPwd);
            if(debug)log.debug("getAdminLevelConnection - connected!");
            return con;
        } catch (Exception ex)
        {
            log.error("getAdminLevelConnection - Exception: ");
            log.error("getAdminLevelConnection - " + ex.getMessage());
        }
        return null;
    }

    /**
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException
    {
        if(debug)log.debug("getConnection");
        try
        {
            Class.forName(JaasContext.driver);
        } catch (java.lang.ClassNotFoundException e)
        {
            log.error("getConnection - Could not connect to database, driverclass - ClassNotFoundException: ");
            log.error("getConnection - "+ e.getMessage());
            // throw new LoginException("Database driver class not found: " + driverClass);
        }
        
        if(debug)log.debug("getConnection -  url:" + JaasContext.url);
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootUser:" + SpecifySecurityMgr.embeddedSpecifyAppRootUser);
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootPwd:" + SpecifySecurityMgr.embeddedSpecifyAppRootPwd);
        Connection connection = DriverManager.getConnection(JaasContext.url, SpecifySecurityMgr.embeddedSpecifyAppRootUser, SpecifySecurityMgr.embeddedSpecifyAppRootPwd);
        return connection;
    }

    /**
     * @return
     */
    static public DatabaseService getInstance()
    {
        return INSTANCE;
    }
}