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
package edu.ku.brc.exceptions;


/**
 * This exception is used to mark (fatal) failures in the initialization and configuration
 * 
 * @code_status Complete
 * 
 * @author rods
 */
@SuppressWarnings("serial") //$NON-NLS-1$
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
