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
    
    ExecutorService jpaQueryExecServ = Executors.newFixedThreadPool(5);
    
    /**
     * Private Constructor.
     */
    private QueryExecutor()
    {
        // no op
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
    public static void shutdown()
    {
        instance.jpaQueryExecServ.shutdownNow();
    }
    
    /**
     * Requests a Custom Query be executed using the Queue.
     * @param customQuery
     */
    public static void executeQuery(final CustomQueryIFace customQuery)
    {
        // create a background thread to do the web service work
        @SuppressWarnings("unused")
        Callable<CustomQueryIFace> customQueryWorker = new Callable<CustomQueryIFace>()
        {
            @SuppressWarnings("synthetic-access")
            public CustomQueryIFace call() throws Exception
            {
                customQuery.execute();
                return customQuery;
            }
        };
        instance.jpaQueryExecServ.submit(customQueryWorker);
    }
    
    /**
     * Requests a Query be executed using the Queue.
     * @param sqlExeProc the query
     */
    public static void executeQuery(final SQLExecutionProcessor sqlExeProc)
    {
        // create a background thread to do the web service work
        @SuppressWarnings("unused")
        Callable<SQLExecutionProcessor> sqlExeProcWorker = new Callable<SQLExecutionProcessor>()
        {
            @SuppressWarnings("synthetic-access")
            public SQLExecutionProcessor call() throws Exception
            {
                sqlExeProc.execute();
                return sqlExeProc;
            }
        };
        instance.jpaQueryExecServ.submit(sqlExeProcWorker);
    }

}
