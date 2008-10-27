/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
