/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mysql.jdbc.CommunicationsException;

import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;


/**
 * Constructs a an object to execute an SQL statement and then notify the listener and it is done. Any exception in the
 * SQL processing are passed back to the listener instead of being thrown. <br>The start method asks a thread pool service
 * to execute the query. (It used to execute on its own thread).
 *
 * @code_status Complete
 *
 * @author rods
 */
public class SQLExecutionProcessor
{
    private static final Logger log = Logger.getLogger(SQLExecutionProcessor.class);
    
    private static long            lastErrorTime = 0;

    protected SQLExecutionListener listener;
    protected String               sqlStr;

    protected Connection           dbConnection;
    protected Statement            dbStatement           = null;
    protected boolean              isAutoCloseConnection = true;
    protected QueryExecutor        queryExecutor;
    
    protected Object               data                  = null;
    protected boolean              inError               = false;

    /**
     * Constructs a an object to execute an SQL statement and then notify the listener. Sets isAutoCloseConnection to true, override with "setAutoCloseConnection".
     * @param listener the listener
     * @param sqlStr the SQL statement to be executed.
     */
    public SQLExecutionProcessor(final SQLExecutionListener listener, 
                                 final String sqlStr)
    {
        this(null, null, listener, sqlStr);
    }

    /**
     * Constructs a an object to execute an SQL statement and then notify the listener. Sets isAutoCloseConnection to true, override with "setAutoCloseConnection".
     * @param listener the listener
     * @param sqlStr the SQL statement to be executed.
     */
    public SQLExecutionProcessor(final QueryExecutor queryExecutor,
                                 final SQLExecutionListener listener, 
                                 final String sqlStr)
    {
        this(null, queryExecutor, listener, sqlStr);
    }

    /**
     * Constructs a an object to execute an SQL statement and then notify the listener. 
     * Sets isAutoCloseConnection to true if there is a connection, false is connection is null which means it will create one; override with "setAutoCloseConnection".
     * @param listener the listener
     * @param sqlStr the SQL statement to be executed.
     */
    public SQLExecutionProcessor(final Connection dbConnection, 
                                 final SQLExecutionListener listener, 
                                 final String sqlStr)
    {
        this(dbConnection, null, listener, sqlStr);
    }
    
    /**
     * @param dbConnection
     * @param queryExecutor
     * @param listener
     * @param sqlStr
     */
    public SQLExecutionProcessor(final Connection           dbConnection,
                                 final QueryExecutor        queryExecutor,
                                 final SQLExecutionListener listener, 
                                 final String               sqlStr)
    {
        this.dbConnection  = dbConnection;
        this.queryExecutor = queryExecutor == null ? QueryExecutor.getInstance() : queryExecutor;
        this.listener      = listener;
        this.sqlStr        = trimStr(sqlStr);
        
        this.isAutoCloseConnection = dbConnection == null;
    }
    
    /**
     * @return the listener
     */
    public SQLExecutionListener getListener()
    {
        return listener;
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
    public void setAutoCloseConnection(final boolean isAutoCloseConnection)
    {
        this.isAutoCloseConnection = isAutoCloseConnection;
    }

    /**
     * trim the string of all whitespace and tabs.
     * @param str the string to be trimmed
     * @return returns trimmed string
     */
    protected String trimStr(final String str)
    {
        return str.replace("\n", "").replace("\t", "").replace("\r", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }

    /**
     * Sets a new SQL string.
     * @param sqlStr the SQL string
     */
    public void setSqlStr(String sqlStr)
    {
        this.sqlStr = trimStr(sqlStr);
    }

    /**
     * Returns the sql string.
     * @return the sqlStr
     */
    public String getSqlStr()
    {
        return sqlStr;
    }

    /**
     * Close the DB Connection for this SQL statement.
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

            if (dbConnection != null && isAutoCloseConnection)
            {
                dbConnection.close();
                dbConnection = null;
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SQLExecutionProcessor.class, ex);
            log.error(ex);
        }

     }

    /**
     * Close the DB Connection for this SQL statement.
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
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SQLExecutionProcessor.class, ex);
            log.error(ex);
        }

     }

    /**
     * Starts the thread to make the SQL call.
     *
     */
    public void start()
    {
        QueryExecutor.getInstance().executeQuery(this);
    }

    /**
     * Creates a connection, makes the call and returns the results.
     */
    public void execute()
    {
        try
        {
            if (dbConnection == null)
            {
                dbConnection = DBConnection.getInstance().createConnection();
            }

            if (dbConnection != null)
            {
                if (dbStatement != null)
                {
                    dbStatement.close();
                }
                
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

                //log.debug("SQL ["+sqlStr+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                if (sqlStr.trim().toLowerCase().startsWith("select")) //$NON-NLS-1$
                {
                    ResultSet rs = dbStatement.executeQuery(sqlStr);
                    //log.debug("SQL*%["+sqlStr+"]");
                    listener.exectionDone(this, rs);
                    inError = true;
                    
                } else
                {
                    dbStatement.executeUpdate(sqlStr); // int result return is ignored (probably shouldn't be)
                    //log.debug("SQL**["+sqlStr+"]");
                    listener.exectionDone(this, null);
                }
                
                if (isAutoCloseConnection)
                {
                    close();
                }
            }

        } catch (java.sql.SQLException ex)
        {
            
            long    now       = System.currentTimeMillis(); 
            boolean showError = now - lastErrorTime > 2000;
            lastErrorTime = now;
            
            if (showError)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                if (ex instanceof CommunicationsException)
                {
                    CommandDispatcher.dispatch(new CommandAction("ERRMSG", "DISPLAY", this, null, "BAD_CONNECTION"));
                    return;
                    
                }
                
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SQLExecutionProcessor.class, ex);
                log.error("Error in run["+sqlStr+"]", ex); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            
            if (listener != null)
            {
                listener.executionError(this, ex);
            }

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            
            long    now       = System.currentTimeMillis(); 
            boolean showError = now - lastErrorTime > 2000;
            lastErrorTime = now;
            if (showError)
            {
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SQLExecutionProcessor.class, ex);
                log.error("Error in run["+sqlStr+"]", ex); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (listener != null)
            {
                listener.executionError(this, ex);
            }
        }
    }

    /**
     * Returns the java.sql.Connection object.
     * @return the java.sql.Connection object.
     */
    public Connection getDbConnection()
    {
        return dbConnection;
    }

    /**
     * @return the data
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data)
    {
        this.data = data;
    }
}
