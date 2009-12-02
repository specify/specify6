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
package edu.ku.brc.dbsupport;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;

import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 27, 2009
 *
 */
public abstract class DBMSUserMgr
{
    public static String factoryName = "edu.ku.brc.dbsupport.DBMSUserMgr"; //$NON-NLS-1$
    
    public enum DBSTATUS { ok, 
                           hasTables, 
                           missingDB,
                           emptyDB, 
                           error,
                           cancelled }
    
    public static final int PERM_NONE        = 0;
    public static final int PERM_SELECT      = 1;
    public static final int PERM_UPDATE      = 2;
    public static final int PERM_DELETE      = 4;
    public static final int PERM_INSERT      = 8;
    public static final int PERM_LOCK_TABLES = 16;
    public static final int PERM_BASIC       = 31;
    
    public static final int PERM_ALL         = 32; // Literally 'all' the permissions
    
    private static DBMSUserMgr instance = null;
   
    protected String hostName = null;
    protected String errMsg   = null;
    
    /**
     * Creates a database with a given name.
     * @param dbName the name of the new database
     * @return true on success
     */
    public abstract boolean createDatabase(String dbName);
    
    /**
     * Drops a database with a given name.
     * @param dbName the name of the database
     * @return true on success
     */
    public abstract boolean dropDatabase(String dbName);
    
    /**
     * Drops a user with a given name.
     * @param username the name of the user's login
     * @return true on success
     */
    public abstract boolean dropUser(String username);
    
    /**
     * Checks to see if the DB exists
     * @param dbName the database name
     * @return true if exists
     */
    public abstract boolean doesDBExists(String dbName);
    
    /**
     * Checks to see if a User exists
     * @param dbName the User name
     * @return true if exists
     */
    public abstract boolean doesUserExists(String userName);
    
	/**
	 * Changes the password for a user
	 * @param username the user name ot be changed
	 * @param oldPwd the old or current password
	 * @param newPwd the new password
	 * @return true on success
	 */
	public abstract boolean changePassword(String username, String oldPwd, String newPwd);

	/**
	 * Closes the connection to the DBMS.
	 * @return true if closed
	 */
	public abstract boolean close();

    /**
     * Connects JUST to the DBMS.
     * @param username the IT Username
     * @param password the IT password
     * @param databaseHost the name or IP address of the machine
     * @return true if the IT user got logged in
     */
    public abstract boolean connectToDBMS(String username, String password, String databaseHost);

    /**
     * Connects to the DBMS and a database.
     * @param username the IT Username
     * @param password the IT password
     * @param databaseHost the name or IP address of the machine
     * @param dbName the name of the database.
     * @return true if the IT user got logged in
     */
    public abstract boolean connect(String username, String password, String databaseHost, String dbName);
    
    /**
     * Enables the Mgr to use an existing connection. Assumes it has permissions to whatever
     * the other calls that are made.
     * @param connection and existing JDBC connection
     */
    public abstract void setConnection(Connection connection);
    
    /**
     * Checks to see if a field exists for a table.
     * @param tableName the name of the table to be checked
     * @param fieldName the name of the field to be checked
     * @return false if table or field doesn't exist
     */
    public abstract boolean doesFieldExistInTable(String tableName, String fieldName);

	/**
	 * Creates a user and assigns permissions.
	 * @param username the user's username
	 * @param password the user's password
	 * @param dbName the data name the permissions are assigned to
	 * @param permissions the mask for the permissions to be set.
	 * @return true if created
	 */
	public abstract boolean createUser(String username, String password, String dbName, int permissions);
	
    /**
     * The database exists, but does it have any tables?
     * @param dbName the database name
     * @return true if is at least one table, false if not.
     */
    public abstract boolean doesDBHaveTables();
    
    /**
     * Check to see if the table is in the schema
     * @param dbName the database name
     * @return true if the table exists, false if not.
     */
    public abstract boolean doesDBHaveTable(final String tableName);
    
    /**
     * Some databases require a specific engine and also the charset needs to be checked (UTF-8).
     * @param dbName the database to check.
     * @return true if ok
     */
    public abstract boolean verifyEngineAndCharSet(final String dbName);

	/**
	 * @return a localized test message describing the error when a method fails
	 */
	public String getErrorMsg() 
    {
        return errMsg;
    }

	/**
	 * Removes a user.
	 * @return true if removed
	 */
	public abstract boolean removeUser(String username, String password);
	
	/**
	 * Gets the permissions for a user for a database.
	 * @param username the user
	 * @param dbName the database
	 * @return the mask of bits indicating the permissions setting
	 */
	public abstract int getPermissions(String username, String dbName);

	/**
	 * Sets permissions for a user on a database 
	 * @param username the user
	 * @param dbName the database
	 * @param permissions the permissions mask
	 * @return true on success
	 */
	public abstract boolean setPermissions(String username, String dbName, int permissions);
	
    /**
     * @param hostName the host name to connect to
     */
    public void setHostName(String hostName) 
    {
        this.hostName = hostName;
    }
    
    /**
     * If there is no database, or the database has no tables then it returns true, 
     * or it prompts the user whether to proceed.
     * @param dbName database name
     * @param hostName host or database server name
     * @param itUsername the IT username
     * @param itPassword the IT password
     * @return if the database has table then it prompts the user,
     * DBSTATUS.ok if no database, or the database has no tables; DBSTATUS.cancelled 
     */
    public static DBSTATUS isOkToProceed(final String dbName, 
                                         final String hostName, 
                                         final String itUsername,
                                         final String itPassword)
    {
        DBSTATUS status = checkForDB(dbName, hostName, itUsername,itPassword);
        if (status == status.hasTables || status == status.emptyDB)
        {
            status = UIHelper.promptForAction("PROCEED", "CANCEL", "DEL_CUR_DB_TITLE", UIRegistry.getLocalizedMessage("DEL_CUR_DB", dbName)) ? DBSTATUS.ok : DBSTATUS.cancelled;
        }
        return status;
    }

    /**
     * Check to see if the database exists and has tables. 
     * If the database is embedded then it doesn't check for the existence of tables, just if the data directory is there.
     * @param dbName database name
     * @param hostName host or database server name
     * @param itUsername the IT username
     * @param itPassword the IT password
     * @return returns hasTables when there are table, 
     * or missingOrEmpty when db is missing or there are no tables in it, 
     * or error when the user couldn't get logged in (does not return 'ok' instead it returns 'emptyDB' or 'hasTables')
     */
    public static DBSTATUS checkForDB(final String dbName, 
                                      final String hostName, 
                                      final String itUsername,
                                      final String itPassword)
    {
        
        boolean isEmbedded = DBConnection.getInstance().isEmbedded();
        if (isEmbedded)
        {
            File dbDataDir = DBConnection.getEmbeddedDataDir();
            if (dbDataDir != null)
            {
                return dbDataDir.exists() ? DBSTATUS.hasTables : DBSTATUS.missingDB;
                
            }
            return DBSTATUS.error;
        }
        
        DBMSUserMgr mgr = null;
        try
        {
            mgr = DBMSUserMgr.getInstance();
            
            if (mgr.connectToDBMS(itUsername, itPassword, hostName)) // can we even login?
            {
                if (mgr.doesDBExists(dbName)) // does it exist?
                {
                    mgr.close();
                    
                    if (mgr.connect(itUsername, itPassword, hostName, dbName)) // it exists, but can we open it?
                    {
                        return mgr.doesDBHaveTables() ? DBSTATUS.hasTables : DBSTATUS.emptyDB;
                    }
                    
                    // We are here because the database exists, but we cannot open it
                    return DBSTATUS.error;
                    
                } else
                {
                    return DBSTATUS.missingDB;
                }
            }
            
            // Can't login
            return DBSTATUS.error;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyDBSetupWizard.class, ex);
            
        } finally
        {
            if (mgr != null)
            {
                mgr.close();
            }
        }
        
        return DBSTATUS.error;
    }

	/**
     * Returns the instance of the DataProviderIFace.
     * @return the instance of the DataProviderIFace.
     */
    public static DBMSUserMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                            factoryName);}});
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = Class.forName(factoryNameStr).asSubclass(DBMSUserMgr.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBMSUserMgr.class, e);
                InternalError error = new InternalError("Can't instantiate DBMSUserMgr factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate DBMSUserMgr factory becase " + factoryName + " has not been set."); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
