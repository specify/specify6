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
package edu.ku.brc.specify.tests;

import junit.framework.TestCase;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
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
    protected String           itUsername = "Specify";
    protected String           itPassword = "Specify";
    protected String           dbName     = "mytestdb";
    protected String           hostName   = "localhost";

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        
        BasicSQLUtils.setSkipTrackExceptions(true);
        
        /*DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");

        
        String connStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Opensys, hostName, dbName);

        dbConn = DBConnection.getInstance();

        dbConn.setDriver(driverInfo.getDriverClassName());
        dbConn.setDialect(driverInfo.getDialectClassName());
        dbConn.setConnectionStr(connStr);
        dbConn.setUsernamePassword(itUsername, itPassword);
        dbConn.setSkipDBNameCheck(true);
        
        mgr = new MySQLDMBSUserMgr();
        mgr.setHostName(hostName);
        */
        
        mgr = new MySQLDMBSUserMgr();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        mgr.close();
        //dbConn.close();
    }

    /**
     * Test method for 
     */
    public void testCreateDB()
    {
        BasicSQLUtils.setSkipTrackExceptions(false);
        
        if (mgr.connectToDBMS(itUsername, itPassword, hostName))
        {
            if (mgr.doesDBExists(dbName))
            {
                assertTrue(mgr.dropDatabase(dbName));
            }
            
            System.out.println("Created database"+dbName);
            
            assertTrue(mgr.createDatabase(dbName));
            
            System.out.println("Created database"+dbName);
            
            assertTrue(mgr.doesDBExists(dbName));
            
            System.out.println("Database "+dbName+" exists");
            
            assertTrue(mgr.dropDatabase(dbName));
            
            System.out.println("Dropped database"+dbName);
        } else
        {
            assertTrue(false);
        }
    }
    
    /**
     * Test method for 
     */
    public void testCreateUser()
    {
        BasicSQLUtils.setSkipTrackExceptions(false);
        
        if (mgr.connectToDBMS(itUsername, itPassword, hostName))
        {
            if (mgr.doesDBExists(dbName))
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
