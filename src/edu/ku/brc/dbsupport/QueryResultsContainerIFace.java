/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.sql.Connection;
import java.util.List;


/**
 * Interface for a containe of results that were gotten (most likely) asynchronously.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Created: Mar 2, 2007
 *
 */
public interface QueryResultsContainerIFace
{

    /**
     * Starts the execution asynchronously.
     * @param listener the calback listener who is notified when it is done
     * @param connect optional JDBC connect for some case where it can be reused
     */
    public void start(QRCProcessorListener listener, Connection connect);
    
    /**
     * Returns whether the execution failed.
     * @return whether the execution failed.
     */
    public boolean hasFailed();
    
    /**
     * Adds a QueryResultsDataObj to the container.
     * @param qrdo the item to be added
     */
    public void add(QueryResultsDataObj qrdo);
    
    /**
     * Returns the list of QueryResultsDataObj objects.
     * @return the list of QueryResultsDataObj objects.
     */
    public List<QueryResultsDataObj> getQueryResultsDataObjs();
    
    /**
     * Clears and extra data objects.
     */
    public void clear();
    
    
}
