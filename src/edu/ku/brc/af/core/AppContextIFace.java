/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.core;

import java.io.File;

/**
 * Manages the Application Context per a user and database. This "context" may have different meanings to
 * different applications.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface AppContextIFace
{

    /**
     * Sets the current context.
     * @param databaseName the name of the database 
     * @param userName the user name
     * @return  true if the context was set correctly
     */
    public boolean setContext(final String databaseName, 
                              final String userName);
    
    /**
     * Returns the File object that represents the directory for the current user and database.
     * @return the File object that represents the directory for the current user and database.
     */
    public File getCurrentContext();
    
    /**
     * Returns the File object that represents the directory for the current user and database plus the name passed in.
     * @param fileName a file name to be appended to the Current Context Directory
     * @return the File object that represents the directory for the current user and database plus the name passed in.
     */
    public File getCurrentContext(final String fileName);
    
}