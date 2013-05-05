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
import java.util.Calendar;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 * An interface defining the required methods for a class capable of managing attachment
 * files and their thumbnails.
 *
 * @code_status Beta
 * @author jstewart
 * @author rods
 */
public interface AttachmentManagerIface
{
    /**
     * Whether it was initialized OK.
     * @param urlStr null for File-based stores and not null for web-based stores
     * @return true if initialized and can be used.
     */
    public abstract boolean isInitialized(String urlStr);
    
    /**
     * Sets the attachmentLocation field in the passed in Attachment
     * object.  This allows the AttachmentManagerIface implementation
     * to provide a storage location that isn't already in use.
     * 
     * @param attachment the Attachment for which a storage location is needed
     * @param doDisplayErrors false for silent mode, true for popu dialog errors
     * @return true if successfully set
     */
    public abstract boolean setStorageLocationIntoAttachment(Attachment attachment, boolean doDisplayErrors);
    
    /**
     * Get a file handle to the attachment original.
     * 
     * @param attachment the attachment record
     * @return a java.io.File handle to the attachment document
     */
    public abstract File getOriginal(Attachment attachment);
    
    /**
     * @param attachLoc
     * @param originalLoc
     * @param mimeType
     * @return
     */
    public abstract File getOriginal(String attachLoc,
                                     String originalLoc,
                                     String mimeType);

    /**
     * @param attachLoc
     * @param originalLoc
     * @param mimeType
     * @param maxSideInPixels
     * @return
     */
    public abstract File getOriginalScaled(String attachLoc,
                                           String originalLoc,
                                           String mimeType,
                                           int maxSideInPixels);

    /**
     * @param attachmentID the record id of the attachment.
     * @return the embedded image meta data for the image in the repository.
     */
    public abstract String getMetaDataAsJSON(int attachmentID);
    
    /**
     * @param attachmentID the record id of the attachment.
     * @return the embedded image meta data for the image in the repository.
     */
    public abstract Calendar getFileEmbddedDate(int attachmentID);
    
    /**
     * Get a file handle to the attachment thumbnail.
     * 
     * @param attachment the attachment record
     * @param maxSize the max size of width and/or height
     * @return a java.io.File handle to the attachment thumbnail
     */
    public abstract File getThumbnail(Attachment attachment, int maxSize);
    
    /**
     * Store a new attachment file (and thumbnail) in the manager's storage area.  A call
     * to attachment.setAttachmentLocation will occur.
     * 
     * @param attachment the attachment record
     * @param attachmentFile the original attachment document
     * @param thumbnail the thumbnail of the original
     * @throws IOException if an error occurs when storing the files
     */
    public abstract void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException;
    
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
    public abstract void replaceOriginal(Attachment attachment, File newOriginal, File newThumbnail) throws IOException;
    
    /**
     * Delete the files (original and thumbnail) associated with this attachment record.
     * 
     * @param attachment the DB record holding the file info
     * @throws IOException if an error occurs when deleting the files
     */
    public abstract void deleteAttachmentFiles(Attachment attachment) throws IOException;
    
    
    /**
     * Regenerates the thumbnail from the original file.
     * @param attachment the attachment with thumbnail
     * @return the File for the thumbnail.
     * @throws IOException
     */
    public abstract File regenerateThumbnail(final Attachment attachment) throws IOException;
    
    /**
     * Resets the baseDirectory.
     * @param baseDir the new base directory
     * @throws IOException
     */
    public abstract void setDirectory(File baseDir) throws IOException;
    
    /**
     * @return the directory of the Attachment Manager
     */
    public abstract File getDirectory();
    
    /**
     * @return true if a networked mapped drive is being used for the attachment manager. false if a Web Service is being used.
     */
    public abstract boolean isDiskBased();

    /**
     * @return string URI 'template' that includes symbols for substituting various parameters that can be substituted with the 
     * appropriate values for locating the proper image. Any service or app can ask for this URL template, the Attachment Manager does not
     * have to support all of the symbols listed below, but they may be passed in.<br>
     * FILENAME - name of the file.<br>
     * COLL - Collection Name  (same as in Specify)<br>
     * DIV - Division Name (same as in Specify)<br>
     * INST - Institution Name (same as in Specify)<br>
     * SCALE - an integer number in pixels indicating the size of the largest side of the image. Blank for no scaling.<br>
     */
    public abstract String getImageAttachmentURL();
    
    /**
     * @param sizeInPixels
     */
    public void setThumbSize(int sizeInPixels);
    
    /**
     * Perform any internal cleanup needed before shutdown.
     */
    public abstract void cleanup();
}
