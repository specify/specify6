/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

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
    
    protected File originalsDir;
    protected File thumbsDir;
    
    /** A collection of all files created by calls to setStorageLocationIntoAttachment
     * that have not yet been filled by calls to storeAttachmentFile.
     */
    protected Vector<String> unfilledFiles;
    
    /**
     * Creates a new instance, setting baseDirectory to null.
     * @throws IOException 
     */
    public FileStoreAttachmentManager(String baseDirectory) throws IOException
    {
        this.baseDirectory = baseDirectory;
        originalsDir = new File(baseDirectory + File.separator + "originals");
        thumbsDir    = new File(baseDirectory + File.separator + "thumbnails");
        
        originalsDir.mkdirs();
        thumbsDir.mkdirs();
        
        if (!originalsDir.canWrite())
        {
            throw new IOException("Storage directory not writable: " + originalsDir.getAbsolutePath());
        }
        if (!thumbsDir.canWrite())
        {
            throw new IOException("Storage directory not writable: " + originalsDir.getAbsolutePath());
        }
        
        unfilledFiles = new Vector<String>();
    }
    
    public void setStorageLocationIntoAttachment(Attachment attachment)
    {
        String attName = attachment.getOrigFilename();
        int lastPeriod = attName.lastIndexOf('.');
        String suffix = ".att";
        if (lastPeriod!=-1)
        {
            suffix = ".att" + attName.substring(lastPeriod);
        }
        try
        {
            File storageFile = File.createTempFile("sp6-", suffix, originalsDir);
            attachment.setAttachmentLocation(storageFile.getName());
            unfilledFiles.add(attachment.getAttachmentLocation());
        }
        catch (IOException e)
        {
            // TODO What to do here?
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getOriginal(Attachment attachment)
    {
        String fileLoc = attachment.getAttachmentLocation();
        File storedFile = new File(baseDirectory + File.separator + "originals" + File.separator + fileLoc);
        if (storedFile.exists())
        {
            return storedFile;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getThumbnail(Attachment attachment)
    {
        String fileLoc = attachment.getAttachmentLocation();
        File storedFile = new File(baseDirectory + File.separator + "thumbnails" + File.separator + fileLoc);
        if (storedFile.exists())
        {
            return storedFile;
        }
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentLocation();
        
        File origFile = new File(baseDirectory + File.separator + "originals" + File.separator + attachLoc);
        
        File thumbFile = new File(baseDirectory + File.separator + "thumbnails" + File.separator + attachLoc);

        FileUtils.copyFile(attachmentFile, origFile);
        FileUtils.copyFile(thumbnail, thumbFile);
        
        unfilledFiles.remove(attachment.getAttachmentLocation());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#replaceOriginal(edu.ku.brc.specify.datamodel.Attachment, java.io.File)
     */
    public void replaceOriginal(Attachment attachment, File newOriginal, File newThumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentLocation();
        File origFile = new File(baseDirectory + File.separator + "originals" + File.separator + attachLoc);
        File thumbFile = new File(baseDirectory + File.separator + "thumbnails" + File.separator + attachLoc);

        replaceFile(origFile, newOriginal);
        replaceFile(thumbFile, newThumbnail);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#cleanup()
     */
    public void cleanup()
    {
        for (String unusedFile: unfilledFiles)
        {
            File f = new File(originalsDir.getAbsolutePath() + File.separator + unusedFile);
            f.delete();
        }
    }
}
