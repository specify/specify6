/**
 * 
 */
package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public interface AttachmentManagerIface
{
    public File getOriginal(Attachment attachment);
    public File getThumbnail(Attachment attachment);
    public void storeAttachmentFile(Attachment attachment, File attachmentFile, File thumbnail) throws IOException;
}
