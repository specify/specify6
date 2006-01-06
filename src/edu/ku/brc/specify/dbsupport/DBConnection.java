/* Filename:    $RCSfile: DBConnection.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.dbsupport;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A singleton that remembers all the information needed for creating a Database connection. 
 * After setting the necessary parameters you can ask it for a connection at anytime.
 * 
 * @author rods
 *
 */
public class DBConnection
{
    private static Log log = LogFactory.getLog(DBConnection.class);
    
    protected String dbUserid;
    protected String dbPassword;
    protected String dbDriver;
    protected String dbName;
    
    // Static Data Members
    protected static DBConnection instance = new DBConnection();
    
    /**
     * Protected Default constructor
     *
     */
    protected DBConnection()
    {
        
    }
    
    /**
     * Returns the instance to the singleton
     * @return the instance to the singleton
     */
    public static DBConnection getInstance()
    {
        return instance;
    }
    
    /**
     * Sets the user name and password
     * @param dbUserid the username
     * @param dbPassword the password
     */
    public static void setUsernamePassword(final String dbUserid, final String dbPassword)
    {
        instance.dbUserid   = dbUserid;
        instance.dbPassword = dbPassword;
    }
    
    /**
     * Sets the database name 
     * @param dbName the database name
     */
    public static void setDBName(final String dbName)
    {
        instance.dbName = dbName;
    }
    
    /**
     * Sets the driver name 
     * @param dbDriver the driver name
     */
    public static void setDriver(final String dbDriver)
    {
        instance.dbDriver = dbDriver;
    }
    
    /**
     * Returns a new connection to the database/. It uses the database name, driver, username and password to connect
     * @return the JDBC connection to the database
     */
    public static Connection getConnection()
    {
        Connection con = null;
        try
        {
            Class.forName(instance.dbDriver); // load driver
            
            con = DriverManager.getConnection(instance.dbName, instance.dbUserid, instance.dbPassword );
            
        } catch (Exception ex)
        {
            log.error("Error in getConnection", ex);
        }
        return con;
    }

}
