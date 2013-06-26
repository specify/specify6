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
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.ImageLoaderExector;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 24, 2012
 *
 */
public class ImageDataItem implements ImageLoaderListener
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
    private List<Pair<String, Object>> dataList = null;
    private boolean   isSelected        = false;
    
    private double    lat;
    private double    lon;
    private Boolean   hasLatLon = null; 
    

    
    private String shortName = null;
    
    private AtomicBoolean stopLoading = new AtomicBoolean(false);
    
    private ImageLoaderListener externalImgLoadListener;
    
    //private AtomicBoolean isLoading = new AtomicBoolean(false);
    //private AtomicBoolean isError   = new AtomicBoolean(false);
    
    private ImageLoader     imageLoader = null;

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
     * @param externalLoadListener
     */
    public void loadScaledImage(final int scale, final ImageLoaderListener externalLoadListener)
    {
        loadImage(scale < 0, scale, externalLoadListener);
    }
    
    /**
     * @param doLoadFullImage
     * @param scale
     * @param externalLoadListener
     */
    private void loadImage(final boolean             doLoadFullImage, 
                           final int                 scale,
                           final ImageLoaderListener externalLoadListener)
    {
        this.externalImgLoadListener = externalLoadListener;
        
        if (!stopLoading.get())
        {
            //System.out.println("loadImage - doLoadFullImage "+doLoadFullImage+"   scale "+scale);
            if (imageLoader == null)
            {
                imageLoader = new ImageLoader(imgName, mimeType, doLoadFullImage, scale, this);
            }
            
            imageLoader.setScale(scale);
            imageLoader.setDoLoadFullImage(doLoadFullImage);
            
            ImageLoaderExector.getInstance().loadImage(imageLoader);
        }
    }

    /**
     * @return the fullImgIcon
     */
    public Integer getOwnerRecId()
    {
        return dataList != null && dataList.size() > 0 ? (Integer)dataList.get(dataList.size()-1).second : null;
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
    public List<Pair<String, Object>> getDataMap()
    {
        return dataList;
    }


    /**
     * @param dataMap the dataMap to set
     */
    public void setDataMap(List<Pair<String, Object>> dataMap)
    {
        this.dataList = dataMap;
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
     * @param bd
     * @return
     */
    private String convert(final BigDecimal bd)
    {
        if (bd != null)
        {
            String str = StringUtils.stripEnd(bd.toString(), "0");
            return str.endsWith(".") ? str + "0" : str;
        }
        return null;
    }
    
    /**
     * 
     */
    private void ensureLatLon()
    {
        if (hasLatLon == null)
        {
            hasLatLon = false;
            BigDecimal latBD = getValue(dataList, 2, "Latitude1");
            BigDecimal lonBD = getValue(dataList, 2, "Longitude1");
            if (latBD != null && lonBD != null)
            {
                String latStr = convert(latBD);
                String lonStr = convert(lonBD);
                hasLatLon = lonStr != null && latStr != null;
                if (hasLatLon)
                {
                    lat = latBD.doubleValue();
                    lon = lonBD.doubleValue();
                    setValue(dataList, 2, "Latitude1",  latStr);
                    setValue(dataList, 2, "Longitude1", lonStr);
                }
            }
        }
    }
    
    /**
     * @return the lat
     */
    public double getLat()
    {
        ensureLatLon();
        return lat;
    }

    /**
     * @return the lon
     */
    public double getLon()
    {
        ensureLatLon();
        return lon;
    }

    /**
     * @return the hasLatLon
     */
    public Boolean hasLatLon()
    {
        ensureLatLon();
        return hasLatLon;
    }

    /**
     * 
     */
    public void cleanup()
    {
        stopLoading.set(true);
        
        if (imageLoader != null) 
        {
            imageLoader.stopLoading();
        }
    }
    
    
    /**
     * @param dataList
     * @param tableId
     * @param columnName
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static <T> T getValue(List<Pair<String, Object>> dataList, final int tableId, final String columnName)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (ti != null)
        {
            DBFieldInfo fi = ti.getFieldByColumnName(columnName);
            if (fi != null)
            {
                for (Pair<String, Object> p : dataList)
                {
                    if (p.first.equals(fi.getTitle()))
                    {
                        return (T)p.second;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param dataList
     * @param tableId
     * @param columnName
     * @param data
     */
    @SuppressWarnings("unchecked")
    protected static void setValue(List<Pair<String, Object>> dataList, final int tableId, final String columnName, final Object data)
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (ti != null)
        {
            DBFieldInfo fi = ti.getFieldByColumnName(columnName);
            if (fi != null)
            {
                for (Pair<String, Object> p : dataList)
                {
                    if (p.first.equals(fi.getTitle()))
                    {
                        p.second = data;
                    }
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------------------
    //-- ImageLoaderListener
    //--------------------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imagedLoaded(java.lang.String, java.lang.String, boolean, int, boolean, javax.swing.ImageIcon, java.io.File)
     */
    @Override
    public void imagedLoaded(final String    imageName, 
                             final String    mimeType, 
                             final boolean   doLoadFullImage, 
                             final int       scale, 
                             final boolean   isError,
                             final ImageIcon imageIcon,
                             final File      localFile)
    {
        ImageDataItem.this.localFile = localFile;

        if (!imageLoader.isError())
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
                if (externalImgLoadListener != null)
                {
                    if (!stopLoading.get())
                    {
                        externalImgLoadListener.imagedLoaded(imageName, mimeType, doLoadFullImage, scale, isError, imageIcon, localFile);
                    } else
                    {
                        externalImgLoadListener.imageStopped(imageName, doLoadFullImage);
                    }
                }
            }
        });
        imageLoader.cleanup();
        imageLoader = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imageStopped(java.lang.String)
     */
    @Override
    public void imageStopped(final String imageName, final boolean doLoadFullImage)
    {
        if (externalImgLoadListener != null)// && !stopLoading.get())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    externalImgLoadListener.imageStopped(imageName, doLoadFullImage);
                }
            });
        }
        imageLoader.cleanup();
        imageLoader = null;
    }
}
