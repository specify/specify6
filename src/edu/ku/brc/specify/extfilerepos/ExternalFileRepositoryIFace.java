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

package edu.ku.brc.specify.extfilerepos;

import java.io.File;

import edu.ku.brc.specify.datamodel.Agent;

/**
 * Interface representing the external file repository
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */

public interface ExternalFileRepositoryIFace
{
    /**
     * Copies file into the repository
     * @param fileName Logical name of file (must be unique
     * @param mimeType the mime type of the file, it can be null then it will assume it is a octet-stream
     *                   (see http://www.iana.org/assignments/media-types/application/)
     * @param externalLocation the external file location
     * @param remarks a description of the file (optional)
     * @param agent agent that added the file
     * @return returns true if file is put in repos, false if not
     */
    public boolean put(final String fileName, 
                       final String mimeType, 
                       final String externalLocation, 
                       final String remarks,
                       final Agent  agent);
    
    /**
     * Removes a ExternalFileEntry
     * @param id the id of the item to be removed
     * @return true if the file with the Id was remove, false it it wasn't
     */
    public boolean remove(Integer id);
    
    /**
     * Returns an image by file name
     * @param fileName the file to retrieve
     * @return the image or null if it wasn't found
     */
    public File get(String fileName);
    
    /**
     * Returns an image by records ID
     * @param imageID
     * @return the image or null if it wasn't found
     */
    public File get(long imageID);
    
}
