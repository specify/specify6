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
public interface AttachmentManagerIface
{
    public File getOriginal(Attachment attachment);
    public File getThumbnail(Attachment attachment);
    public File[] getGroupOriginals(AttachmentGroup group);
    public File[] getGroupThumbnails(AttachmentGroup group);
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException;
}
