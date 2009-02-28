/*
     * Copyright (C) 2009  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tests;

import junit.framework.TestCase;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 27, 2009
 *
 */
public class MySQLDBMSMgrTest extends TestCase
{
    protected MySQLDMBSUserMgr mgr        = null;
    protected DBConnection     dbConn     = null;
    protected String           itUsername = "root";
    protected String           itPassword = "Nessie1601";
    protected String           dbName     = "mytestdb";
    protected String           hostName   = "localhost";

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        
        BasicSQLUtils.setSkipTrackExceptions(true);
        
        DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");

        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Opensys, hostName, dbName);

        dbConn = DBConnection.getInstance();

        dbConn.setDriver(driverInfo.getDriverClassName());
        dbConn.setDialect(driverInfo.getDialectClassName());
        dbConn.setConnectionStr(connStr);
        dbConn.setUsernamePassword(itUsername, itPassword);
        dbConn.setSkipDBNameCheck(true);
        
        mgr = new MySQLDMBSUserMgr();
        mgr.setHostName(hostName);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        mgr.close();
        dbConn.close();
    }

    /**
     * Test method for 
     */
    public void testCreateDB()
    {
        if (mgr.connect(itUsername, itPassword))
        {
            if (mgr.dbExists(dbName))
            {
                assertTrue(mgr.dropDatabase(dbName));
            }
            assertTrue(mgr.createDatabase(dbName));
            
            System.out.println("Created database"+dbName);
            
            assertTrue(mgr.dbExists(dbName));
            
            System.out.println("Database "+dbName+" exists");
            
            assertTrue(mgr.dropDatabase(dbName));
            
            System.out.println("Dropped database"+dbName);
        }
    }
    
    /**
     * Test method for 
     */
    public void testCreateUser()
    {
        
        if (mgr.connect(itUsername, itPassword))
        {
            if (mgr.dbExists(dbName))
            {
                assertTrue(mgr.dropDatabase(dbName));
            }
            assertTrue(mgr.createDatabase(dbName));
            
            assertTrue(mgr.createUser("myuser", "myuser", dbName, DBMSUserMgr.PERM_SELECT | DBMSUserMgr.PERM_DELETE));
            assertTrue(mgr.getPermissions("myuser", dbName) == (DBMSUserMgr.PERM_SELECT | DBMSUserMgr.PERM_DELETE));
            assertTrue(mgr.setPermissions("myuser", dbName, (DBMSUserMgr.PERM_SELECT | DBMSUserMgr.PERM_DELETE | DBMSUserMgr.PERM_UPDATE)));
            assertTrue(mgr.getPermissions("myuser", dbName) == (DBMSUserMgr.PERM_SELECT | DBMSUserMgr.PERM_DELETE | DBMSUserMgr.PERM_UPDATE));
            assertTrue(mgr.removeUser("myuser", "myuser"));
            
            assertTrue(mgr.dropDatabase(dbName));
        }
        
    }
    
}
