/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


/**
 * @author megkumin
 *
 */
public class JDBCCalls
{
    protected static final Logger log = Logger.getLogger(JDBCCalls.class);
    
	/**
	 * 
	 */
	public JDBCCalls()
	{
		// TODO Auto-generated constructor stub
	}
	
    public static void executeQuery(String queryString)
    {
         Connection           dbConnection    = null;
        log.debug(queryString);

        try
        {
            dbConnection = DBConnection.getInstance().createConnection();
            Statement  dbStatement = dbConnection.createStatement();
            ResultSet rs           = dbStatement.executeQuery(queryString);
            
            rs.close();
            dbStatement.close();
            dbStatement = null;
            dbConnection.close();
            dbConnection = null;
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
	
	public static void createDatabaseMySQL(String databaseName)
	{
		String queryString = "CREATE DATABASE IF NOT EXISTS " + databaseName; //$NON-NLS-1$
        executeQuery(queryString);
	}
    
    public static void deleteDatabaseMySQL(String databaseName)
    {
        String queryString = "DROP DATABASE IF EXISTS " + databaseName; //$NON-NLS-1$
        executeQuery(queryString);
    }

	public static void addUserToDatabase(String username)
	{
		
	}
    
    /**
     * Clean Up internal state
     */
    public void cleanUp()
    {

    }
	/**
	 * @param args
	 * void
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}

}
