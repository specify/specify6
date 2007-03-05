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

/**
 * Interface for listeners for a QueryResulsContainer.
 * @author rod
 *
 * @code_status Alpha
 *
 * Created: Mar 2, 2007
 *
 */
public interface QRCProcessorListener
{
    /**
     * Notification that the process is done and has finished without exception.
     * @param qrc the calling processor
     */
    public void exectionDone(QueryResultsContainerIFace qrc);
    
    /**
     * Notification that the process was done and completed with an exception.
     * @param qrc the calling processor
     */
    public void executionError(QueryResultsContainerIFace qrc);
}
