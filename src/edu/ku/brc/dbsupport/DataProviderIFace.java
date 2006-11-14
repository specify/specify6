/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.dbsupport;

/**
 * A wrapper interface for a provider class. 
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public interface DataProviderIFace
{
    /**
     * Eveicts a class of objects from the provider.
     * @param clsObject the class
     */
    public void evict(Class clsObject);
    
    /**
     * Request the provider to be shutdown.
     */
    public void shutdown();
    
    /**
     * Gets the current session, if the current session doesn't exist, it creates a new one.
     * @return the current session
     */
    public DataProviderSessionIFace getCurrentSession();
    
    /**
     * Creates a new session.
     * @return the seesion
     */
    public DataProviderSessionIFace createSession();
    
    /**
     * Adds a listener for changes to any database object.
     * @param listener the listener that will be notified
     */
    //public void addChangeListener(DataProviderChangeListener listener);
    
}
