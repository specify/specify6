/* Filename:    $RCSfile: SQLExecutionProcessor.java,v $
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author rods
 * Constructs a an object to execute an SQL staement and then notify the listener and it is done. Any exception in the
 * SQL processing are passed back to the listener instead of being thrown. This class is running in its own thread.
 */
public class SQLExecutionProcessor implements Runnable
{
    private static Log log = LogFactory.getLog(SQLExecutionProcessor.class);

    protected Thread               thread;
    protected SQLExecutionListener listener;
    protected String               sqlStr;
    
    protected Connection           dbConnection          = null;    
    protected Statement            dbStatement           = null;
    protected boolean              isAutoCloseConnection = true;  

    /**
     * Constructs a an object to execute an SQL staement and then notify the listener
     * @param listener the listener
     * @param sqlStr the SQL statement to be executed.
     */
    public SQLExecutionProcessor(final SQLExecutionListener listener, final String sqlStr)
    {
        this.listener = listener;
        this.sqlStr   = trimStr(sqlStr);
    }
    
    /**
     * Constructs a an object to execute an SQL staement and then notify the listener
     * @param listener the listener
     * @param sqlStr the SQL statement to be executed.
     */
    public SQLExecutionProcessor(final Connection dbConnection, final SQLExecutionListener listener, final String sqlStr)
    {
        this.dbConnection = dbConnection;
        this.listener     = listener;
        this.sqlStr       = trimStr(sqlStr);
    }
    
    /**
     * Returns whether the connection and statement should be automatically close.
     * @return Returns whether the connection and statement should be automatically close.
     */
    public boolean isAutoCloseConnection()
    {
        return isAutoCloseConnection;
    }

    /**
     * Sets whether the connection and statement should be automatically close.
     * @param isAutoCloseConnection true - auto close, false do not auto close
     */
    public void setAutoCloseConnection(boolean isAutoCloseConnection)
    {
        this.isAutoCloseConnection = isAutoCloseConnection;
    }
    
    /**
     * trim the string of all whitespace and tabs
     * @param str the string to be trimmed
     * @return returns trimmed string
     */
    protected String trimStr(final String str)
    {
        return str.replace("\n", "").replace("\t", "").replace("\r", "").trim();
    }
    
    /**
     * Sets a new SQL string
     * @param sqlStr the SQL string
     */
    public void setSqlStr(String sqlStr)
    {
        this.sqlStr = trimStr(sqlStr);
    }

    /**
     * Close the DB Connection for this SQL statement
     *
     */
    public void close() 
    {            
        try
        {
            if (dbStatement != null)
            {
                dbStatement.close();
                dbStatement = null;
            }
            
            if (dbConnection != null)
            {
                dbConnection.close();
                dbConnection = null;   
            }
        } catch (SQLException ex)
        {
            log.error(ex);
        }

     }

    /**
     * Close the DB Connection for this SQL statement
     *
     */
    public void closeStatement() 
    {            
        try
        {
            if (dbStatement != null)
            {
                dbStatement.close();
                dbStatement = null;
            }            
        } catch (SQLException ex)
        {
            log.error(ex);
        }

     }

    /**
     * Starts the thread to make the SQL call
     *
     */
    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Stops the thread making the call
     *
     */
    public synchronized void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }
    
    /**
     * Creates a connection, makes the call and returns the results
     */
    public void run()
    {
        try
        {
            if (dbConnection == null)
            {
                dbConnection = DBConnection.getInstance().getConnection();
            }
            
            if (dbConnection != null)
            {
                if (dbStatement != null)
                {
                    dbStatement.close();
                }
                dbStatement = dbConnection.createStatement();
                
                log.info("SQL ["+sqlStr+"]");
                if (sqlStr.toLowerCase().indexOf("select") == 0)
                {
                    ResultSet rs = dbStatement.executeQuery(sqlStr);
                    log.info("SQL*["+sqlStr+"]");
                    listener.exectionDone(this, rs);
                } else
                {
                    int result = dbStatement.executeUpdate(sqlStr);
                    log.info("SQL*["+sqlStr+"]");
                    listener.exectionDone(this, null);
                }
                
                if (isAutoCloseConnection)
                {
                    close();
                }
            }

        } catch (java.sql.SQLException ex)
        {
            ex.printStackTrace();
            log.error("Error in run["+sqlStr+"]", ex);
            listener.executionError(this, ex);
                  
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error("Error in run["+sqlStr+"]", ex);
            listener.executionError(this, ex);
            
        }
    }

    /**
     * 
     * @return
     */
    public Connection getDbConnection()
    {
        return dbConnection;
    }
    
    
    
}
