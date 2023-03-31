/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
 * This exception is used to mark (fatal) failures in infrastructure and system code.
 * 
 * @code_status Complete
 * 
 * @author rods
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class UIException extends RuntimeException
{
    // private static final long serialVersionUID = 0;

    /**
     * Default Constructor.
     */
    public UIException()
    {
    }

    /**
     * Constructor.
     * @param message the text message
     */
    public UIException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * @param message the text message
     * @param cause throwable
     */
    public UIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with throwable.
     * @param cause throwable
     */
    public UIException(Throwable cause)
    {
        super(cause);
    }
}
