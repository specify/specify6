/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

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
            if (thumbGenStarted.contains(attatchment))
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
                        icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
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
        
        if(mimeType.startsWith("image"))
        {
            return IconManager.getIcon("image", size);
        }
        
        if(mimeType.startsWith("video"))
        {
            return IconManager.getIcon("video", size);
        }
        
        if(mimeType.startsWith("audio"))
        {
            return IconManager.getIcon("audio", size);
        }
        
        if(mimeType.equals("application/pdf"))
        {
            return IconManager.getIcon("pdf", size);
        }
        
        if(mimeType.equals("text/html"))
        {
            return IconManager.getIcon("html", size);
        }
        
        if(mimeType.equals("text/plain"))
        {
            return IconManager.getIcon("text", size);
        }
        
        if(mimeType.equals("application/excel") ||
                mimeType.equals("text/csv") ||
                mimeType.equals("application/csv") ||
                mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.msexcel") )
                
        {
            return IconManager.getIcon("Spreadsheet", size);
        }

        return IconManager.getIcon("unknown", size);
    }
}
