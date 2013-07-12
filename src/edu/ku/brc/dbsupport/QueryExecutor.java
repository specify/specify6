/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class enables Queries to be executed in a thread pool.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 10, 2007
 *
 */
public class QueryExecutor
{
    private static QueryExecutor instance = new QueryExecutor();
    
    protected ExecutorService jpaQueryExecServ = null;
    
    /**
     * Private Constructor.
     */
    public QueryExecutor()
    {
        this(5);
    }
    
    /**
     * Private Constructor.
     */
    public QueryExecutor(final int queueSize)
    {
        jpaQueryExecServ = Executors.newFixedThreadPool(queueSize);
    }
    
    /**
     * @return the single instance.
     */
    public static QueryExecutor getInstance()
    {
        return instance;
    }
    
    /**
     * Requests an immediate shutdown of the processing queue.
     */
    public void shutdown()
    {
        jpaQueryExecServ.shutdownNow();
    }
    
    /**
     * Requests a Custom Query be executed using the Queue.
     * @param customQuery
     */
    //public static void executeQuery(final CustomQueryIFace customQuery)
    public Future<CustomQueryIFace> executeQuery(final CustomQueryIFace customQuery)
    {
        // create a background thread to do the web service work
        @SuppressWarnings("unused") //$NON-NLS-1$
        Callable<CustomQueryIFace> customQueryWorker = new Callable<CustomQueryIFace>()
        {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public CustomQueryIFace call() throws Exception
            {
                customQuery.execute();
                return customQuery;
            }
        };
        return jpaQueryExecServ.submit(customQueryWorker);
    }
    
    /**
     * Requests a Query be executed using the Queue.
     * @param sqlExeProc the query
     */
    public void executeQuery(final SQLExecutionProcessor sqlExeProc)
    {
        // create a background thread to do the web service work
        @SuppressWarnings("unused") //$NON-NLS-1$
        Callable<SQLExecutionProcessor> sqlExeProcWorker = new Callable<SQLExecutionProcessor>()
        {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public SQLExecutionProcessor call() throws Exception
            {
                sqlExeProc.execute();
                return sqlExeProc;
            }
        };
        jpaQueryExecServ.submit(sqlExeProcWorker);
    }
}
