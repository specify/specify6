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

import java.security.AccessController;
import java.security.PrivilegedAction;

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
    
    public static final int PERM_NONE   = 0;
    public static final int PERM_SELECT = 1;
    public static final int PERM_UPDATE = 2;
    public static final int PERM_DELETE = 4;
    public static final int PERM_INSERT = 8;
    public static final int PERM_ALL    = 15;
    
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
