package edu.ku.brc.af.auth.specify.policy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.specify.SpecifySecurityMgr;
import edu.ku.brc.ui.UIRegistry;

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
    public static Connection getAdminLevelConnection(String url, String driverClass)
    {
        Connection con = null;
        //Statement stmt;
        try
        {
            if(debug)log.debug("getAdminLevelConnection - trying to connect with driver: " + driverClass); //$NON-NLS-1$
            Class.forName(driverClass);
        } catch (java.lang.ClassNotFoundException e)
        {
            log.error("getAdminLevelConnection - Could not connect to database, driverclass - ClassNotFoundException: "); //$NON-NLS-1$
            log.error("getAdminLevelConnection - " + e.getMessage()); //$NON-NLS-1$
        }

        try
        {
            if(debug)log.debug("getAdminLevelConnection - Trying to connect to: " + url); //$NON-NLS-1$
            if(debug)log.error("getAdminLevelConnection - Trying to connect with BUILT IN ADMIN LEVER USER ACCOUNT - need to address"); //$NON-NLS-1$
            con = DriverManager.getConnection(url, SpecifySecurityMgr.embeddedSpecifyAppRootUser, SpecifySecurityMgr.embeddedSpecifyAppRootPwd);
            if(debug)log.debug("getAdminLevelConnection - connected!"); //$NON-NLS-1$
            return con;
        } catch (Exception ex)
        {
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
            log.error("getConnection - Could not connect to database, driverclass - ClassNotFoundException: "); //$NON-NLS-1$
            log.error("getConnection - "+ e.getMessage()); //$NON-NLS-1$
            // throw new LoginException("Database driver class not found: " + driverClass);
        }
        
        if(debug)log.debug("getConnection -  url:" + JaasContext.url); //$NON-NLS-1$
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootUser:" + SpecifySecurityMgr.embeddedSpecifyAppRootUser); //$NON-NLS-1$
        if(debug)log.debug("getConnection -  embeddedSpecifyAppRootPwd:" + SpecifySecurityMgr.embeddedSpecifyAppRootPwd); //$NON-NLS-1$
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