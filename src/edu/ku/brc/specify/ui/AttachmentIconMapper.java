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
    private static final Logger log = Logger.getLogger(AttachmentIconMapper.class);
    protected Hashtable<Attachment,ImageIcon> thumbnailCache;
    protected List<Attachment> thumbGenStarted;
    
    public AttachmentIconMapper()
    {
        thumbnailCache = new Hashtable<Attachment, ImageIcon>();
        thumbGenStarted = new Vector<Attachment>();
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
        final IconSize size = IconSize.Std32;
        final Attachment a = (Attachment)o;

        ImageIcon cachedIcon = thumbnailCache.get(a);
        if (cachedIcon!=null)
        {
            return cachedIcon;
        }
        
        // try to get the thumbnail from the attachment storage location
        File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(a);
        if (thumb != null)
        {
            ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
            icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
            thumbnailCache.put(a, icon);
            return icon;
        }
        
        // next, try to make a new thumbnail in a tmp directory
        
        // make sure to only start one thumb generating thread per attachment
        boolean doGen = true;
        synchronized (thumbGenStarted)
        {
            if (thumbGenStarted.contains(a))
            {
                doGen = false;
            }
        }

        final String origFilename = a.getOrigFilename();
        if (origFilename!=null && doGen)
        {
            // track the fact that we're starting a thumbnail gen thread
            synchronized (thumbGenStarted)
            {
                thumbGenStarted.add(a);
            }
            
            // start a thumbnail generator thread
            Runnable r = new Runnable()
            {
                @SuppressWarnings("synthetic-access")
                public void run()
                {
                    log.debug("Starting thumb gen thread for " + a.getOrigFilename());
                    Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
                    File thumbFile = null;
                    
                    try
                    {
                        thumbFile = File.createTempFile("sp6_thumb_", null);
                        thumbFile.deleteOnExit();
                        log.debug("Generating thumb for " + a.getOrigFilename());
                        thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath());
                        log.debug("Done generating thumb for " + a.getOrigFilename());
                    }
                    catch (IOException e)
                    {
                        // unable to create thumbnail
                        thumbFile = null;
                    }

                    if (thumbFile!=null)
                    {
                        ImageIcon icon = new ImageIcon(thumbFile.getAbsolutePath());
                        icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
                        log.debug("Caching thumb for " + a.getOrigFilename());
                        thumbnailCache.put(a, icon);
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
        
        String mimeType = a.getMimeType();
        
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
            return IconManager.getIcon("spreadsheet", size);
        }

        return IconManager.getIcon("unknown", size);
    }
}
