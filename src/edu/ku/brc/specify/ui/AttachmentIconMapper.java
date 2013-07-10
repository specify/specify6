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
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        thumbnailCache  = new Hashtable<Attachment, ImageIcon>();
        thumbGenStarted = new Vector<Attachment>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    @Override
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[1];
        mappedClasses[0] = Attachment.class;
        return mappedClasses;
    }
    
    /**
     * Sends notification on GUI thread
     * @param listener
     * @param imgIcon
     */
    private ImageIcon notifyListener(final ChangeListener listener, final ImageIcon imgIcon)
    {
        if (listener != null)
        {
            SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
            {
                @Override
                protected Boolean doInBackground() throws Exception
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (Exception ex) {}
                    return null;
                }

                @Override
                protected void done()
                {
                    listener.stateChanged(new ChangeEvent(imgIcon));
                }
        
            };
            worker.execute();
        }
        return imgIcon;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object, javax.swing.event.ChangeListener)
     */
    @Override
    public ImageIcon getIcon(final Object obj, final ChangeListener listener)
    {
        final IconSize   iconSize   = IconSize.Std32;
        final int        size       = edu.ku.brc.util.thumbnails.Thumbnailer.getInstance().getMaxSize().width;
        final Attachment attachment = (Attachment)obj;

        ImageIcon cachedIcon = thumbnailCache.get(attachment);
        if (cachedIcon != null)
        {
            return notifyListener(listener, cachedIcon);
        }
        
        // try to get the thumbnail from the attachment storage location
        File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(attachment, size);
        if (thumb != null)
        {
            if (thumb.exists())
            {
                ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
                //icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
                thumbnailCache.put(attachment, icon);
                return notifyListener(listener, icon);
            }
            return notifyListener(listener, IconManager.getIcon("BrokenImage"));
        }
        
        if (StringUtils.isNotEmpty(attachment.getAttachmentLocation()))
        {
            // the attachment location is set, so it should be there.
            // don't fall back to the original file because that
            // will just confuse matters.
            return notifyListener(listener, IconManager.getIcon("BrokenImage"));
        }
        
        // next, try to make a new thumbnail in a tmp directory
        
        // make sure to only start one thumb generating thread per attachment
        boolean doGen = true;
        synchronized (thumbGenStarted)
        {
            if (thumb != null || thumbGenStarted.contains(attachment))
            {
                doGen = false;
            }
        }

        final String origFilename = attachment.getOrigFilename();
        if (origFilename != null && doGen)
        {
            // track the fact that we're starting a thumbnail gen thread
            synchronized (thumbGenStarted)
            {
                thumbGenStarted.add(attachment);
            }
            
            // start a thumbnail generator thread
            ImageLoader imgLoader = new ImageLoader(attachment, origFilename, listener);
            imgLoader.execute();
        }
        
        // based on the MIME type of the attachment, return the appropriate icon
        // TODO: this can easily be configured via an XML file instead of hard coding
        String mimeType = attachment.getMimeType();
        
        if (mimeType == null)
        {
            return notifyListener(listener, IconManager.getIcon("unknown", iconSize));
        }
        
        int inx = mimeType.indexOf('/');
        if (inx > -1)
        {
            String mimePrefix = mimeType.substring(0, inx);
            String mimeStr    = Thumbnailer.getIconNameFromExtension(mimePrefix);
            if (mimeStr != null)
            {
                ImageIcon icon = IconManager.getIcon(mimeStr, iconSize);
                if (icon != null)
                {
                    return notifyListener(listener, icon);
                }
            }
            
            if (inx == 11 && mimePrefix.equalsIgnoreCase("application"))
            {
                mimeStr = mimeType.substring(inx+1, mimeType.length());
                if (!mimeStr.equals("octet-stream"))
                {
                    ImageIcon icon = IconManager.getIcon(mimeStr, iconSize);
                    if (icon != null)
                    {
                        return notifyListener(listener, icon);
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
                    return notifyListener(listener, IconManager.getIcon(imgIconName, iconSize));

                }
            }
        }

        return notifyListener(listener, IconManager.getIcon("unknown", iconSize));
    }
    
    //-------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------
    class ImageLoader extends SwingWorker<ImageIcon, ImageIcon>
    {
        private Attachment     attachment;
        private String         origFilename;
        private ChangeListener listener;
        private ImageIcon      imgIcon   = null;
        
        /**
         * @param listener
         */
        /**
         * @param attachment
         * @param origFilename
         * @param listener
         */
        public ImageLoader(final Attachment     attachment, 
                           final String         origFilename, 
                           final ChangeListener listener)
        {
            super();
            this.attachment   = attachment;
            this.origFilename = origFilename;
            this.listener     = listener;
        }

        @Override
        protected ImageIcon doInBackground() throws Exception
        {
            log.debug("Starting thumb gen thread for " + attachment.getOrigFilename());
            Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
            File thumbFile = null;
            
            try
            {
                thumbFile = File.createTempFile("sp6_thumb_", null);
                thumbFile.deleteOnExit();
                log.debug("Generating thumb for " + attachment.getOrigFilename());
                thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath(), false);
                log.debug("Done generating thumb for " + attachment.getOrigFilename());
                
            } catch (IOException e)
            {
                // unable to create thumbnail
                thumbFile = null;
            }

            if (thumbFile != null)
            {
                imgIcon = new ImageIcon(thumbFile.getAbsolutePath());
                //icon = IconManager.getScaledIcon(icon, IconSize.NonStd, size);
                log.debug("Caching thumb for " + attachment.getOrigFilename());
                thumbnailCache.put(attachment, imgIcon);
            }
            return null;
        }

        @Override
        protected void done()
        {
            super.done();
            if (imgIcon != null && listener != null)
            {
                listener.stateChanged(new ChangeEvent(imgIcon));
            }
        }
    }
}
