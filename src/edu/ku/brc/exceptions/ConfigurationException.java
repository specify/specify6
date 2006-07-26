/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package edu.ku.brc.exceptions;

/**
 * This exception is used to mark (fatal) failures in the initialization and configuration
 * 
 * @code_status Complete
 * 
 * @author rods
 */
@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException
{
    /**
     * Default Constructor.
     */
    public ConfigurationException()
    {
    }

    /**
     * Constructor.
     * @param message the text message
     */
    public ConfigurationException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param message the text message
     * @param cause throwable
     */
    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with throwable.
     * @param cause throwable
     */
    public ConfigurationException(Throwable cause)
    {
        super(cause);
    }
}
