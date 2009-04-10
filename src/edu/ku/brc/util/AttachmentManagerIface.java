/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 * An interface defining the required methods for a class capable of managing attachment
 * files and their thumbnails.
 *
 * @code_status Beta
 * @author jstewart
 */
public interface AttachmentManagerIface
{
    /**
     * Sets the attachmentLocation field in the passed in Attachment
     * object.  This allows the AttachmentManagerIface implementation
     * to provide a storage location that isn't already in use.
     * 
     * @param attachment the Attachment for which a storage location is needed
     */
    public void setStorageLocationIntoAttachment(Attachment attachment);
    
    /**
     * Get a file handle to the attachment original.
     * 
     * @param attachment the attachment record
     * @return a java.io.File handle to the attachment document
     */
    public File getOriginal(Attachment attachment);

    /**
     * Get a file handle to the attachment thumbnail.
     * 
     * @param attachment the attachment record
     * @return a java.io.File handle to the attachment thumbnail
     */
    public File getThumbnail(Attachment attachment);
    
    /**
     * Store a new attachment file (and thumbnail) in the manager's storage area.  A call
     * to attachment.setAttachmentLocation will occur.
     * 
     * @param attachment the attachment record
     * @param attachmentFile the original attachment document
     * @param thumbnail the thumbnail of the original
     * @throws IOException if an error occurs when storing the files
     */
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException;
    
    /**
     * Replace the existing attachment file with a new version.  If an exception occurs during the
     * replacement process, there is no guarantee as to the state of the attachment file or its
     * thumbnail.
     * 
     * @param attachment the attachment record
     * @param newOriginal the new version of the attachment document
     * @param newThumbnail the new version of the thumbnail
     * @throws IOException if an error occurs when replacing the files
     */
    public void replaceOriginal(Attachment attachment, File newOriginal, File newThumbnail) throws IOException;
    
    /**
     * Delete the files (original and thumbnail) associated with this attachment record.
     * 
     * @param attachment the DB record holding the file info
     * @throws IOException if an error occurs when deleting the files
     */
    public void deleteAttachmentFiles(Attachment attachment) throws IOException;
    
    /**
     * Resets the baseDirectory.
     * @param baseDir the new base directory
     * @throws IOException
     */
    public void setDirectory(File baseDir) throws IOException;
    
    /**
     * @return the directory of the Attachment Manager
     */
    public File getDirectory();
    
    /**
     * Perform any internal cleanup needed before shutdown.
     */
    public void cleanup();
}
