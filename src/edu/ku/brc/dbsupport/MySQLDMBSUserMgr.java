/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.sql.Connection;


/**
 * @author norine
 *
 */
public class MySQLDMBSUserMgr extends DBMSUserMgr 
{
	private DBConnection dbConnection = null;
	private Connection   connection   = null;
	/**
	 * 
	 */
	public MySQLDMBSUserMgr() 
	{
		super();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
	public boolean changePassword(final String username, final String oldPwd, final String newPwd) 
    {
    	try
		{
			if (connection != null)
			{
				return true;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#close()
	 */
	@Override
	public boolean close() 
	{
		try
		{
			dbConnection.close();
			return true;
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#connect(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean connect(final String username, final String password) 
	{
		try
		{
			dbConnection = new DBConnection();
			dbConnection.setConnectionStr("");
			dbConnection.setDialect("");
			dbConnection.setDriver("");
			dbConnection.setUsernamePassword(username, password);
			
			connection = dbConnection.createConnection();
			return true;
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#createUser(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	@Override
	public boolean createUser(final String username, final String password, final String dbName, final int permissions) 
	{
		try
		{
			if (connection != null)
			{
				//StringBuilder 
				return true;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DBMSUserMgrIFace#removeUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean removeUser(String username, final String password) 
	{
		try
		{
			if (connection != null)
			{
				return true;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}


}
