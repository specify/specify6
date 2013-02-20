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
package edu.ku.brc.specify.ui;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * This class is an implementation of ObjectIconMapper that handles Attachment objects.
 *
 * @code_status Alpha
 * @author jstewart
 */
public class AttachmentIconMapper implements ObjectIconMapper
{
    /** A Logger for handling all output coming from this class. */
    private static final Logger log = Logger.getLogger(AttachmentIconMapper.class);
    
    /** A cache of the thumbnails for the Attachments that are handled. */
    protected Hashtable<Attachment,ImageIcon> thumbnailCache;
    
    /** A list of all Attachments for which a thumbnail generating thread has started. */
    protected List<Attachment> thumbGenStarted;
    
    /**
     * Create an instance.
     */
    public AttachmentIconMapper()
    {
        thumbnailCache = new Hashtable<Attachment, ImageIcon>();
        thumbGenStarted = new Vector<Attachment>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[1];
        mappedClasses[0] = Attachment.class;
        return mappedClasses;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(final Object obj)
    {
        final IconSize   size        = IconSize.Std32;
        final Attachment attatchment = (Attachment)obj;

        ImageIcon cachedIcon = thumbnailCache.get(attatchment);
        if (cachedIcon != null)
        {
            return cachedIcon;
        }
        
        // try to get the thumbnail from the attachment storage location
        File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(attatchment);
        if (thumb != null)
        {
            if (thumb.exists())
            {
                ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
                icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
                thumbnailCache.put(attatchment, icon);
                return icon;
            }
            return IconManager.getIcon("BrokenImage");
        }
        
        // next, try to make a new thumbnail in a tmp directory
        
        // make sure to only start one thumb generating thread per attachment
        boolean doGen = true;
        synchronized (thumbGenStarted)
        {
            if (thumb != null || thumbGenStarted.contains(attatchment))
            {
                doGen = false;
            }
        }

        final String origFilename = attatchment.getOrigFilename();
        if (origFilename != null && doGen)
        {
            // track the fact that we're starting a thumbnail gen thread
            synchronized (thumbGenStarted)
            {
                thumbGenStarted.add(attatchment);
            }
            
            // start a thumbnail generator thread
            Runnable r = new Runnable()
            {
                @SuppressWarnings("synthetic-access")
                public void run()
                {
                    log.debug("Starting thumb gen thread for " + attatchment.getOrigFilename());
                    Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
                    File thumbFile = null;
                    
                    try
                    {
                        thumbFile = File.createTempFile("sp6_thumb_", null);
                        thumbFile.deleteOnExit();
                        log.debug("Generating thumb for " + attatchment.getOrigFilename());
                        thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath(), false);
                        log.debug("Done generating thumb for " + attatchment.getOrigFilename());
                    }
                    catch (IOException e)
                    {
                        // unable to create thumbnail
                        thumbFile = null;
                    }

                    if (thumbFile != null)
                    {
                        ImageIcon icon = new ImageIcon(thumbFile.getAbsolutePath());
                        //icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
                        log.debug("Caching thumb for " + attatchment.getOrigFilename());
                        thumbnailCache.put(attatchment, icon);
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
        
        // based on the MIME type of the attachment, return the appropriate icon
        // TODO: this can easily be configured via an XML file instead of hard coding
        String mimeType = attatchment.getMimeType();
        
        if (mimeType == null)
        {
            return IconManager.getIcon("unknown", size);
        }
        
        int inx = mimeType.indexOf('/');
        if (inx > -1)
        {
            String mimePrefix = mimeType.substring(0, inx);
            String mimeStr    = Thumbnailer.getIconNameFromExtension(mimePrefix);
            if (mimeStr != null)
            {
                ImageIcon icon = IconManager.getIcon(mimeStr, size);
                if (icon != null)
                {
                    return icon;
                }
            }
            
            if (inx == 11 && mimePrefix.equalsIgnoreCase("application"))
            {
                mimeStr = mimeType.substring(inx+1, mimeType.length());
                if (!mimeStr.equals("octet-stream"))
                {
                    ImageIcon icon = IconManager.getIcon(mimeStr, size);
                    if (icon != null)
                    {
                        return icon;
                    }
                }
            }
        }
        
        if (StringUtils.isNotEmpty(origFilename))
        {
            String ext = FilenameUtils.getExtension(origFilename);
            if (ext != null)
            {
                String imgIconName = Thumbnailer.getIconNameFromExtension(ext);
                if (imgIconName != null)
                {
                    return IconManager.getIcon(imgIconName, size);
                }
            }
        }

        return IconManager.getIcon("unknown", size);
    }
}
