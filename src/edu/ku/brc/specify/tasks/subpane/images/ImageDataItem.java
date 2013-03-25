/* Copyright (C) 2012, University of Kansas Center for Research
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.ui.ImageLoaderExector;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 24, 2012
 *
 */
public class ImageDataItem
{
    public static int STD_ICON_SIZE = 135;
    static ImageIcon noImage = null;
    
    private int       tableId; // i.e. ColObj, CE, Tax, etc
    private Integer   attachmentId;
    private String    imgName;
    private String    title;
    private String    mimeType;
    private File      localFile;
    private ImageIcon imgIcon;
    private ImageIcon fullImgIcon       = null;
    private Map<String, Object> dataMap = null;
    private boolean   isSelected        = false;
    
    private String shortName = null;
    
    private AtomicBoolean stopLoading = new AtomicBoolean(false);

    
    private ItemImageLoaderListener itemImgLoadListener;
    
    //private AtomicBoolean isLoading = new AtomicBoolean(false);
    //private AtomicBoolean isError   = new AtomicBoolean(false);
    
    private ImageLoader     loadImage = null;

    /**
     * @param attachmentId
     * @param tableId
     * @param title
     * @param imgName
     * @param mimeType
     */
    public ImageDataItem(final Integer attachmentId, 
                         final int     tableId,
                         final String  title, 
                         final String  imgName, 
                         final String  mimeType)
    {
        super();
        this.attachmentId   = attachmentId;
        this.tableId        = tableId;
        this.imgName        = imgName;
        this.title          = title;
        this.mimeType       = mimeType;
        this.imgIcon        = null;
        this.localFile      = null;
        
        /*if (noImage == null)
        {
            noImage = IconManager.getImage("Loading");
        }*/
        //loadImage(false, STD_ICON_SIZE, this.changeListener);
    }
    
    
    /**
     * @return
     */
    public ImageIcon getImageIcon()
    {
        /*if (imgIcon == null || isLoading.get() || isError.get())
        {
            return noImage;
        }*/
        return imgIcon;
    }
    
    /**
     * @param scale
     * @param chgListener
     */
    public void loadScaledImage(final int scale, final ImageLoaderListener imgLoadListener)
    {
        loadImage(scale < 0, scale, imgLoadListener);
    }
    
    /**
     * @param doLoadFullImage
     */
    private void loadImage(final boolean             doLoadFullImage, 
                           final int                 scale,
                           final ImageLoaderListener imgLoadListener)
    {
        if (!stopLoading.get())
        {
            //System.out.println("loadImage - doLoadFullImage "+doLoadFullImage+"   scale "+scale);
            if (loadImage == null)
            {
                itemImgLoadListener = new ItemImageLoaderListener();
                loadImage           = new ImageLoader(imgName, mimeType, doLoadFullImage, scale, itemImgLoadListener);
            }
            
            itemImgLoadListener.setImgLoadListener(imgLoadListener);
            loadImage.setScale(scale);
            loadImage.setDoLoadFullImage(doLoadFullImage);
            
            ImageLoaderExector.getInstance().loadImage(loadImage);
        }
    }

    /**
     * @return the fullImgIcon
     */
    public ImageIcon getFullImgIcon()
    {
        return fullImgIcon;
    }

    /**
     * @param fullImgIcon the fullImgIcon to set
     */
    public void setFullImgIcon(ImageIcon fullImgIcon)
    {
        this.fullImgIcon = fullImgIcon;
    }

    /**
     * @return the attachmentId
     */
    public Integer getAttachmentId()
    {
        return attachmentId;
    }

    /**
     * @return the imgName
     */
    public String getImgName()
    {
        return imgName;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * @return the imgIcon
     */
    public ImageIcon getImgIcon()
    {
        return imgIcon;
    }

    /**
     * @param imgIcon the imgIcon to set
     */
    public void setImgIcon(ImageIcon imgIcon)
    {
        this.imgIcon = imgIcon;
    }

    /**
     * @return the tableId
     */
    public int getTableId()
    {
        return tableId;
    }


    /**
     * @return the localFile
     */
    public File getLocalFile()
    {
        return localFile;
    }


    /**
     * @param localFile the localFile to set
     */
    public void setLocalFile(File localFile)
    {
        this.localFile = localFile;
    }


    /**
     * @return the dataMap
     */
    public Map<String, Object> getDataMap()
    {
        return dataMap;
    }


    /**
     * @param dataMap the dataMap to set
     */
    public void setDataMap(Map<String, Object> dataMap)
    {
        this.dataMap = dataMap;
    }
    
    /**
     * @return the isSelected
     */
    public boolean isSelected()
    {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }

    /**
     * @return the shortName
     */
    public String getShortName()
    {
        if (shortName == null)
        {
            shortName = FilenameUtils.getBaseName(title);
        }
        return shortName;
    }

    /**
     * 
     */
    public void shutdown()
    {
        stopLoading.set(true);
    }
    
    //--------------------------------------------------------------
    class ItemImageLoaderListener implements ImageLoaderListener
    {
        private ImageLoaderListener imgLoadListener;
        
        /**
         * @param imgLoadListener the imgLoadListener to set
         */
        public void setImgLoadListener(ImageLoaderListener imgLoadListener)
        {
            this.imgLoadListener = imgLoadListener;
        }

        @Override
        public void imagedLoaded(final String imageName, 
                                 final String mimeType, 
                                 final boolean doLoadFullImage, 
                                 final int scale, 
                                 final boolean isError,
                                 final ImageIcon imageIcon,
                                 final File localFile)
        {
            ImageDataItem.this.localFile = localFile;

            if (!loadImage.isError())
            {
                if (doLoadFullImage)
                {
                    fullImgIcon = imageIcon;
                } else
                {
                    imgIcon = imageIcon;
                }
            }
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!stopLoading.get() && imgLoadListener != null)
                    {
                        imgLoadListener.imagedLoaded(imageName, mimeType, doLoadFullImage, scale, isError, imageIcon, localFile);
                    }
                }
            });
        }
    }
}
