/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.dbsupport;

/**
 * Simple wrapper for Hibernate's StaleObjectStateException.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 */
public class StaleObjectException extends Exception
{

    /**
     * Default Constructor.
     */
    public StaleObjectException()
    {
        // node code
    }

    /**
     * Constructs with a message.
     * @param message the message
     */
    public StaleObjectException(String message)
    {
        super(message);
    }

    /**
     * Constructs with a Throwable.
     * @param cause the Throwable
     */
    public StaleObjectException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs with a message and Throwable.
     * @param message the message
     * @param cause Throwable
     */
    public StaleObjectException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
