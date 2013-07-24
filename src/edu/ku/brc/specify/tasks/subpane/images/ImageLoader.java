/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.io.File;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.ImageLoaderIFace;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 4, 2012
 *
 */
public class ImageLoader implements ImageLoaderIFace
{
    private String              imageName;
    private String              mimeType;
    private boolean             doLoadFullImage;
    private int                 scale;
    private File                localFile;
    
    private ImageIcon           imageIcon;
    private boolean             isError;
    private ImageLoaderListener listener;
    //private AtomicBoolean       contLoading = new AtomicBoolean(true);
    private boolean             contLoading = true;
    private byte[]              bytes       = null;

    /**
     * @param imageName
     * @param mimeType
     * @param doLoadFullImage
     * @param scale
     * @param listener
     */
    public ImageLoader(final String  imageName, 
                       final String  mimeType, 
                       final boolean doLoadFullImage, 
                       final int     scale,
                       final ImageLoaderListener listener)
    {
        super();
        this.imageName       = imageName;
        this.mimeType        = mimeType;
        this.doLoadFullImage = doLoadFullImage;
        this.scale           = scale;
        this.listener        = listener;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#load()
     */
    @Override
    public void load()
    {
        if (!contLoading)
        {
            //System.err.println(">>>>>>>>>>>>>>>>>>>>>>> skipping loading: "+this.imageName);
            return;
        }
        
        //System.out.println(id+" Starting Load: "+imageName);
        try
        {
            if (bytes == null) 
            {
                bytes = new byte[10240*2];
            }
            AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
            localFile = null;
            if (doLoadFullImage)
            {
                if (scale == -1)
                {
                    localFile = attachmentMgr.getOriginal(imageName, null, mimeType, bytes);
                } else
                {
                    localFile = attachmentMgr.getOriginalScaled(imageName, null, mimeType, scale, bytes);
                }
            } else
            {
                localFile = attachmentMgr.getOriginalScaled(imageName, null, mimeType, scale, bytes);
            }
            
            if (localFile != null)
            {
                if (!doLoadFullImage || AttachmentUtils.isMimeTypeDisplayable(mimeType))
                {
                    imageIcon = new ImageIcon(localFile.getAbsolutePath());
                    isError   = imageIcon == null;
                }
            } else
            {
                isError = true;
            }
            //System.out.println(" Done Load: "+imageName);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        // contLoading is for when something or someone wants to 
        // prematurely stop the loading.external 
        if (listener != null)
        {
            if  (contLoading) 
            {
                listener.imageLoaded(imageName, mimeType, doLoadFullImage, scale, isError, imageIcon, localFile);
            } else {
                listener.imageStopped(imageName, doLoadFullImage);
            }
        }
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#done()
     */
    @Override
    public void done()
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#getStatus()
     */
    @Override
    public int getStatus()
    {
        return isError ? ImageLoaderIFace.kError : ImageLoaderIFace.kImageOK;
    }

    /**
     * @return the doLoadFullImage
     */
    public boolean isDoLoadFullImage()
    {
        return doLoadFullImage;
    }

    /**
     * @param doLoadFullImage the doLoadFullImage to set
     */
    public void setDoLoadFullImage(boolean doLoadFullImage)
    {
        this.doLoadFullImage = doLoadFullImage;
    }

    /**
     * @return the scale
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(int scale)
    {
        this.scale = scale;
    }

    /**
     * @return the imageName
     */
    public String getImageName()
    {
        return imageName;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * @return the imageIcon
     */
    public ImageIcon getImageIcon()
    {
        return imageIcon;
    }

    /**
     * @return the isError
     */
    public boolean isError()
    {
        return isError;
    }
    
    /**
     * Enables the Loader to be able to start again.
     */
    public void enableLoading()
    {
        this.contLoading = true;
    }

    /**
     * @param imageName the imageName to set
     */
    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#stopLoading()
     */
    @Override
    public synchronized void stopLoading()
    {
        this.contLoading = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#cleanup()
     */
    @Override
    public void cleanup()
    {
        imageName = null;
        mimeType  = null;
        imageIcon = null;
        listener  = null;
        bytes     = null;
        localFile = null;
    }
}
