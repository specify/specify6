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
package edu.ku.brc.specify.conversion;

/**
 * This interface is used to notify listeners of a successful login or when thethe dialog is cancelled.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface CustomDBConverterListener
{
    /**
     * The login was successful
     *
     * @param databaseName the name of the database that was logged into
     * @param userName the username
     */
    public void loggedIn(String databaseName, String userName);
    
    /**
     * The login dialog was cancelled
     */
    public void cancelled();
    
}
