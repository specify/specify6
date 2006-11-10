/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 * An implementation of AttachmentManagerIface that uses the underlying filesystem to
 * provide storage of the attachments and thumbnails.
 *
 * @author jstewart
 * @code_status Beta
 */
public class FileStoreAttachmentManager implements AttachmentManagerIface
{
    /** The base directory path of where the files will be stored. */
    protected String baseDirectory;
    
    /**
     * Creates a new instance, setting baseDirectory to null.
     */
    public FileStoreAttachmentManager(String baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getOriginal(Attachment attachment)
    {
        String attachFileName = baseDirectory + attachment.getAttachmentLocation();
        File attach = new File(attachFileName);
        if (attach.exists())
        {
            return attach;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getThumbnail(Attachment attachment)
    {
        String thumbnailFileName = baseDirectory + File.separator + "thumbnails" + File.separator + attachment.getAttachmentLocation();
        File thumbnail = new File(thumbnailFileName);
        if (thumbnail.exists())
        {
            return thumbnail;
        }
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentID() + "_" + attachment.getOrigFilename();
        attachment.setAttachmentLocation(attachLoc);
        File origDest = new File(baseDirectory + File.separator + attachLoc);
        FileUtils.copyFile(attachmentFile, origDest);
        
        File thumbDest = new File(baseDirectory + File.separator + "thumbnails" + File.separator + attachLoc);
        FileUtils.copyFile(thumbnail, thumbDest);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#replaceOriginal(edu.ku.brc.specify.datamodel.Attachment, java.io.File)
     */
    public void replaceOriginal(Attachment attachment, File newOriginal, File newThumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentLocation();
        File orig = new File(baseDirectory + File.separator + attachLoc);
        replaceFile(orig, newOriginal);

        File thumb = new File(baseDirectory + File.separator + "thumbnails" + File.separator + attachLoc);
        replaceFile(thumb, newThumbnail);
    }
    
    /**
     * Replace origFile with newFile, attempting to recover from an exception if it occurs.
     * 
     * @param origFile the original file
     * @param newFile the replacement version
     * @throws IOException a disk IO error occurred during the process
     */
    protected void replaceFile(File origFile, File newFile) throws IOException
    {
        File tmpOrig = File.createTempFile("sp6-", ".tmp");
        FileUtils.copyFile(origFile, tmpOrig);
        try
        {
            // try to copy the new attachment into place
            // if this fails, we should try to copy the old original back into place
            FileUtils.copyFile(newFile, origFile);
        }
        catch (IOException e)
        {
            // if the attachment file differs from the original that we copied to a tmp location...
            if( !FileUtils.contentEquals(origFile, tmpOrig) )
            {
                // copy the tmp version back into place
                FileUtils.copyFile(tmpOrig, origFile);
            }
        }
    }
}
