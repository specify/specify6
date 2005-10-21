/*
 * Filename:    $RCSfile: ExternalFileRepositoryIFace.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:28 $
 *
 * This library is free software; you can redistribute it and/or
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

public interface ExternalFileRepositoryIFace
{
    /**
     * Copies file into the repository
     * @param aName Logical name of file (must be unique
     * @param aDesc a description of the file
     * @param aMimeType the mime type of the file, it can be null then it will assume it is a octet-stream
     *                   (see http://www.iana.org/assignments/media-types/application/)
     * @param aLocation the external file location
     */
    public boolean put(String aName, String aDesc, String aMimeType, String aLocation);
    
    /**
     * Removes a ExternalFileEntry
     * @param aId
     * @return
     */
    public boolean remove(Long aId);
    
    /**
     * Returns an image by logical name
     * @param aLogicalName
     * @return the image or null if it wasn't found
     */
    public File get(String aLogicalName);
    
    /**
     * Returns an image by records ID
     * @param aImageID
     * @return the image or null if it wasn't found
     */
    public File get(long aImageID);
    
}
