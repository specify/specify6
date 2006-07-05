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

import org.apache.log4j.Logger;

/**
 * A singleton that remembers all the information needed for creating a Database connection. 
 * After setting the necessary parameters you can ask it for a connection at anytime.<br><br>
 * Also, has a factory method for creating instances so users can connect to more than one database ata time.
 * 
 * @author rods
 *
 */
public class DBConnection
{
    private static final Logger log = Logger.getLogger(DBConnection.class);
    
    protected String dbUsername;
    protected String dbPassword;
    protected String dbDriver;
    protected String dbServer;
    protected String dbName;
    
    protected String errMsg = "";
    
    // Static Data Members
    protected static final DBConnection instance = new DBConnection();
    
    /**
     * Protected Default constructor
     *
     */
    protected DBConnection()
    {
        
    }
    
    /**
     * @return the error message if it was caused by an exception
     */
    public String getErrorMsg()
    {
        return this.errMsg;
    }
    
    /**
     * Returns a new connection to the database from an instance of DBConnection.
     * It uses the database name, driver, username and password to connect.
     * @return the JDBC connection to the database
     */
    public Connection createConnection()
    {
        Connection con = null;
        try
        {
            Class.forName(dbDriver); // load driver
            
            log.debug("["+dbServer+dbName+"]["+dbUsername+"]["+dbPassword+"]");
            con = DriverManager.getConnection(dbServer+dbName, dbUsername, dbPassword);
            
        } catch (Exception ex)
        {
            //log.error("Error in getConnection", ex);
            errMsg = ex.getMessage();
        }
        return con;
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
     * @param dbUsername the username
     * @param dbPassword the password
     */
    public void setUsernamePassword(final String dbUsername, final String dbPassword)
    {
        this.dbUsername   = dbUsername;
        this.dbPassword = dbPassword;
    }
    
    /**
     * Sets the database name 
     * @param dbName the database name
     */
    public void setDatabaseName(final String dbName)
    {
        this.dbName = dbName;
    }
    
    /**
     * Sets the driver name 
     * @param dbDriver the driver name
     */
    public void setDriver(final String dbDriver)
    {
        this.dbDriver = dbDriver;
    }
    
    /**
     * Sets the dbServer name 
     * @param dbDriver the dbServer name
     */
    public void setServer(final String dbServer)
    {
        this.dbServer = dbServer;
        
        if (!instance.dbServer.endsWith("/"))
        {
            instance.dbServer += "/";
        }
    }
    
    public String getDriver()
    {
        return dbDriver;
    }

    public String getServer()
    {
        return dbServer;
    }

    public String getDatabaseName()
    {
        return dbName;
    }

    public String getPassword()
    {
        return dbPassword;
    }

    public String getUserName()
    {
        return dbUsername;
    }
    
    /**
     * Returns a new connection to the database/. It uses the database name, driver, username and password to connect
     * @return the JDBC connection to the database
     */
    public static Connection getConnection()
    {
        return instance.createConnection();
    }
    
    /**
     * @param dbDriver the driver name
     * @param dbServer the dbServer url
     * @param dbName the database name (just the name)
     * @param dbUsername the username
     * @param dbPassword the password
     * @return a new instance of a DBConnection
     */
    public static DBConnection createInstance(final String dbDriver, 
                                              final String dbServer, 
                                              final String dbName, 
                                              final String dbUsername, 
                                              final String dbPassword)
    {
        DBConnection dbConnection = new DBConnection();
        
        dbConnection.setDriver(dbDriver);
        dbConnection.setServer(dbServer);
        dbConnection.setDatabaseName(dbName);
        dbConnection.setUsernamePassword(dbUsername, dbPassword);
        
        return dbConnection;
    }

}
