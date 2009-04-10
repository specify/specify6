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

package edu.ku.brc.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * A singleton that remembers all the information needed for creating a JDBC Database connection. 
 * It uses the DBConnection
 * After setting the necessary parameters you can ask it for a connection at anytime.<br><br>
 * Also, has a factory method for creating instances so users can connect to more than one database ata time.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DBConnection
{
    private static final Logger log = Logger.getLogger(DBConnection.class);
    
    protected String dbUsername;
    protected String dbPassword;
    protected String dbConnectionStr;             // For Create or Open
    protected String dbCloseConnectionStr = null; // for closing
    protected String dbDriver;
    protected String dbDialect;                   // needed for Hibernate
    protected String dbName;
    
    protected boolean argHaveBeenChecked = false;
    protected boolean skipDBNameCheck    = false;
    
    protected Connection connection = null;
     
    protected String     errMsg = ""; //$NON-NLS-1$
    
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
     * @param dbUsername
     * @param dbPassword
     * @param dbConnectionStr
     * @param dbDriver
     * @param dbDialect
     * @param dbName
     */
    public DBConnection(String dbUsername, 
                        String dbPassword, 
                        String dbConnectionStr,
                        String dbDriver, 
                        String dbDialect, 
                        String dbName)
    {
        super();
        this.dbUsername      = dbUsername;
        this.dbPassword      = dbPassword;
        this.dbConnectionStr = dbConnectionStr;
        this.dbDriver        = dbDriver;
        this.dbDialect       = dbDialect;
        this.dbName          = dbName;
        this.skipDBNameCheck = dbName == null;
    }

    /**
     * The error message if it was caused by an exception.
     * @return the error message if it was caused by an exception
     */
    public String getErrorMsg()
    {
        return this.errMsg;
    }
    
    /**
     * @param skipDBNameCheck the skipDBNameCheck to set
     */
    public void setSkipDBNameCheck(boolean skipDBNameCheck)
    {
        this.skipDBNameCheck = skipDBNameCheck;
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
            if (!argHaveBeenChecked)
            {
                if (!skipDBNameCheck && StringUtils.isEmpty(dbName))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_NAME"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbConnectionStr))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_CONN_STR"); //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbUsername))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_USERNAME");//"The Username is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbPassword))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_PASSWORD");//"The Password is empty."; //$NON-NLS-1$
                    return null;
                }
                if (StringUtils.isEmpty(dbDriver))
                {
                    errMsg = getResourceString("DBConnection.NO_DB_DRIVER"); //$NON-NLS-1$
                    return null;
                }
                argHaveBeenChecked = true;
            }
            Class.forName(dbDriver); // load driver
            
            //log.debug("["+dbConnectionStr+"]["+dbUsername+"]["+dbPassword+"] ");
            con = DriverManager.getConnection(dbConnectionStr, dbUsername, dbPassword);
            
        } catch (SQLException sqlEX)
        {
            log.error("Error in getConnection", sqlEX);
            if (sqlEX.getNextException() != null)
            {
                errMsg = sqlEX.getNextException().getMessage();
            } else
            {
                errMsg = sqlEX.getMessage();
            }
                
        } catch (Exception ex)
        {
//            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBConnection.class, ex);
            log.error("Error in getConnection", ex);
            errMsg = ex.getMessage();
        }
        return con;
    }
    
    /**
     * Closes the connection to the database and disposes it.
     */
    public void close()
    {
        try
        {
            // This is primarily for Derby non-networked database. 
            if (dbCloseConnectionStr != null)
            {
                Connection con = DriverManager.getConnection(dbCloseConnectionStr, dbUsername, dbPassword);
                if (con != null)
                {
                    con.close();
                }
            } else if (connection != null)
            {
                connection.close();
                connection = null;
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }
    
    /**
     * Returns the instance to the singleton.
     * @return the instance to the singleton
     */
    public static DBConnection getInstance()
    {
        return instance;
    }
    
    /**
     * Sets the user name and password.
     * @param dbUsername the username
     * @param dbPassword the password
     */
    public void setUsernamePassword(final String dbUsername, final String dbPassword)
    {
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the database name.
     * @param dbName the database name
     */
    public void setDatabaseName(final String dbName)
    {
        this.dbName = dbName;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the driver name.
     * @param dbDriver the driver name
     */
    public void setDriver(final String dbDriver)
    {
        this.dbDriver = dbDriver;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the Hibernate Dialect class name.
     * @param dbDialect the driver name
     */
    public void setDialect(final String dbDialect)
    {
        this.dbDialect = dbDialect;
        argHaveBeenChecked = false;
    }
    
    /**
     * Sets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @param dbConnectionStr the full connection string
     */
    public void setConnectionStr(final String dbConnectionStr)
    {
        this.dbConnectionStr = dbConnectionStr;
        argHaveBeenChecked = false;
    }
    
    /**
     * Returns the driver
     * @return the driver
     */
    public String getDriver()
    {
        return dbDriver;
    }

    /**
     * Gets the fully specified path to connect to the database.
     * i.e. jdbc:mysql://localhost/fish<br>Some databases may need to construct their fully specified path.
     * @return the full connection string
     */
    public String getConnectionStr()
    {
        return dbConnectionStr;
    }

    /**
     * Returns the Close Connection String.
     * @return the Close Connection String.
     */
    public String getDbCloseConnectionStr()
    {
        return dbCloseConnectionStr;
    }

    /**
     * Sets the Close Connection String.
     * @param dbCloseConnectionStr the string (can be null to clear it)
     */
    public void setDbCloseConnectionStr(final String dbCloseConnectionStr)
    {
        this.dbCloseConnectionStr = dbCloseConnectionStr;
    }

    /**
     * Returns the Database Name.
     * @return the Database Name.
     */
    public String getDatabaseName()
    {
        return dbName;
    }

    /**
     * Returns the Password.
     * @return the Password.
     */
    public String getPassword()
    {
        return dbPassword;
    }

    /**
     * Returns the USe Name.
     * @return the USe Name.
     */
    public String getUserName()
    {
        return dbUsername;
    }
    
    /**
     * Returns the Dialect.
     * @return the Dialect.
     */
    public String getDialect()
    {
        return dbDialect;
    }

    /**
     * Returns a new connection to the database. 
     * @return the JDBC connection to the database
     */
    public Connection getConnection()
    {
        if (connection == null)
        {
            connection = createConnection();
        }
        
        return connection;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize()
    {
        //DataProviderFactory.getInstance().shutdown();
        close();
    }
    
    /**
     * Create a new instance.
     * @param dbDriver the driver name
     * @param dbDialect the dialect class name for Hibernate
     * @param dbName the database name (just the name)
     * @param dbConnectionStr the full connection string
     * @param dbUsername the username
     * @param dbPassword the password
     * @return a new instance of a DBConnection
     */
    public static DBConnection createInstance(final String dbDriver, 
                                              final String dbDialect, 
                                              final String dbName, 
                                              final String dbConnectionStr, 
                                              final String dbUsername, 
                                              final String dbPassword)
    {
        DBConnection dbConnection = new DBConnection();
        
        dbConnection.setDriver(dbDriver);
        dbConnection.setDialect(dbDialect);
        dbConnection.setDatabaseName(dbName);
        dbConnection.setConnectionStr(dbConnectionStr);
        dbConnection.setUsernamePassword(dbUsername, dbPassword);
        
        return dbConnection;
    }

}
