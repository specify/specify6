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
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SQLExecutionProcessor implements Runnable
{
    private static Log log = LogFactory.getLog(SQLExecutionProcessor.class);

    protected Thread               thread;
    protected SQLExecutionListener listener;
    protected String               sqlStr;
    protected Connection           dbConnection;    
    protected Statement            dbStatement;

    /**
     * 
     * @param listener
     * @param sqlStr
     */
    public SQLExecutionProcessor(final SQLExecutionListener listener, final String sqlStr)
    {
        this.listener = listener;
        this.sqlStr   = sqlStr;
    }
    
    /**
     * 
     *
     */
    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }
    
    /**
     * 
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
     * 
     */
    public void run()
    {
        try
        {
            dbConnection = DBConnection.getInstance().getConnection();    
            dbStatement = dbConnection.createStatement();
            
            log.info("SQL["+sqlStr+"]");
            listener.exectionDone(this, dbStatement.executeQuery(sqlStr));
            
            dbStatement.close();
            dbStatement = null;
            dbConnection.close();
            dbConnection = null;

        } catch (java.sql.SQLException ex)
        {
            ex.printStackTrace();
            log.error("Error in special", ex);
            listener.executionError(this, ex);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error("Error in special", ex);
            listener.executionError(this, ex);
            
        }

    }
    
}
