/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.util.AttachmentUtils;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class AttachmentIconMapper implements ObjectIconMapper
{
    public AttachmentIconMapper()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
        Class[] mappedClasses = new Class[1];
        mappedClasses[0] = Attachment.class;
        return mappedClasses;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
    {
        Attachment a = (Attachment)o;
        File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(a);
        if (thumb != null)
        {
            ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
            return IconManager.getScaledIcon(icon, IconSize.NonStd, IconSize.Std24);
        }
        
        if (a.getMimeType() == null)
        {
            return IconManager.getIcon("unknown", IconSize.Std24);
        }
        
        if(a.getMimeType().startsWith("image"))
        {
            return IconManager.getIcon("image", IconSize.Std24);
        }
        
        if(a.getMimeType().startsWith("video"))
        {
            return IconManager.getIcon("video", IconSize.Std24);
        }

        if(a.getMimeType().startsWith("audio"))
        {
            return IconManager.getIcon("audio", IconSize.Std24);
        }

        return IconManager.getIcon("unknown", IconSize.Std24);
    }
}
