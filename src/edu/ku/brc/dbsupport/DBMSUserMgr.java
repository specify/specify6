/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 
 *
 */
public class DBMSUserMgr
{
    public static final String factoryName = "edu.ku.brc.dbsupport.DBMSUserMgr"; //$NON-NLS-1$
    
    private static DataProviderIFace instance = null;
   
    protected String hostName = null;
    protected String errMsg   = null;
    
	/**
	 * Changes the password for a user
	 * @param username the user name ot be changed
	 * @param oldPwd the old or current password
	 * @param newPwd the new password
	 * @return true on success
	 */
	public boolean changePassword(final String username, final String oldPwd, final String newPwd) 
    {
		
		return false;
	}

	/**
	 * Closes the connection to the DBMS.
	 * @return true if closed
	 */
	public boolean close() 
	{
		
		return false;
	}

	/**
	 * Connects to the DBMS.
	 * @param username the IT Username
	 * @param password the IT password
	 * @return true if the IT user got logged in
	 */
	public boolean connect(final String username, final String password) 
	{
		
		return false;
	}


	/**
	 * Creates a user and assigns permissions.
	 * @param username the user's username
	 * @param password the user's password
	 * @param dbName the data name the permissions are assigned to
	 * @param permissions the mask for the permissions to be set.
	 * @return true if created
	 */
	public boolean createUser(final String username, final String password, final String dbName, final int permissions) 
	{
		return false;
	}

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
	public boolean removeUser(String username, final String password) 
	{
		
		return false;
	}


	/**
	 * @param hostName the hosy name to connect to
	 */
	public void setHostName(String hostName) 
	{
		this.hostName = hostName;
	}

	/**
     * Returns the instance of the DataProviderIFace.
     * @return the instance of the DataProviderIFace.
     */
    public static DataProviderIFace getInstance()
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
                instance = Class.forName(factoryNameStr).asSubclass(DataProviderIFace.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataProviderFactory.class, e);
                InternalError error = new InternalError("Can't instantiate DataProviderFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate DataProviderFactory factory becase " + factoryName + " has not been set."); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
