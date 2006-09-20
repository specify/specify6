/**
 * 
 */
package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentGroup;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class FileStoreAttachmentManager implements AttachmentManagerIface
{
    protected String baseDirectory;
    
    public FileStoreAttachmentManager()
    {
        baseDirectory = null;
    }
    
    /**
     *
     *
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     * @param attachment
     * @return
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

    /**
     * 
     * 
     * @see edu.ku.brc.util.AttachmentManagerIface#getGroupOriginals(edu.ku.brc.specify.datamodel.AttachmentGroup)
     * @param group
     * @return
     */
    public File[] getGroupOriginals(AttachmentGroup group)
    {
        int groupSize = group.getAttachments().size();
        File[] attachments = new File[groupSize];
        int index = 0;
        for( Attachment a: group.getAttachments() )
        {
            attachments[index] = getOriginal(a);
            ++index;
        }
        return attachments;
    }

    /**
     *
     *
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     * @param attachment
     * @return
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

    /**
     *
     *
     * @see edu.ku.brc.util.AttachmentManagerIface#getGroupThumbnails(edu.ku.brc.specify.datamodel.AttachmentGroup)
     * @param group
     * @return
     */
    public File[] getGroupThumbnails(AttachmentGroup group)
    {
        int groupSize = group.getAttachments().size();
        File[] thumbnails = new File[groupSize];
        int index = 0;
        for( Attachment a: group.getAttachments() )
        {
            thumbnails[index] = getThumbnail(a);
            ++index;
        }
        return thumbnails;
    }

    /**
     *
     *
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     * @param attachment
     * @param attachmentFile
     * @param thumbnail
     * @throws IOException 
     */
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentID() + "_" + attachment.getOrigFilename();
        attachment.setAttachmentLocation(attachLoc);
        File origDest = new File(baseDirectory + File.separator + attachLoc);
        FileUtils.copyFile(attachmentFile, origDest);
        
        File thumbDest = new File(baseDirectory + File.separator + "thumbnails" + File.separator + attachLoc);
        FileUtils.copyFile(attachmentFile, thumbDest);
    }
}
