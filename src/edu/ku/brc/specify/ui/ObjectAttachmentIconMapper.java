/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class ObjectAttachmentIconMapper implements ObjectIconMapper, ObjectTextMapper
{
    protected AttachmentIconMapper attachmentIconMapper;
    
    public ObjectAttachmentIconMapper()
    {
        attachmentIconMapper = new AttachmentIconMapper();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
    {
        if (o instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> oa = (ObjectAttachmentIFace<?>)o;
            return attachmentIconMapper.getIcon(oa.getAttachment());
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        if (o instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> oa = (ObjectAttachmentIFace<?>)o;
            
            String title = oa.getAttachment().getTitle();
            String filename = oa.getAttachment().getOrigFilename();
            if (filename != null)
            {
                int lastWinSepIndex = filename.lastIndexOf('\\');
                int lastUnixSepIndex = filename.lastIndexOf('/');
                int lastIndex = Math.max(lastWinSepIndex, lastUnixSepIndex);
                if (lastIndex != -1)
                {
                   filename = filename.substring(lastIndex+1);
                }
            }
            
            return (title != null && title.trim().length() > 0) ? title : filename;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[1];
        mappedClasses[0] = ObjectAttachmentIFace.class;
        return mappedClasses;
    }
}
