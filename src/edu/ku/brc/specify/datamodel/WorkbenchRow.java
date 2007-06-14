/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;

import edu.ku.brc.ui.GraphicsUtils;

/**
 * WorkbenchRow generated rods
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "workbenchrow")
@org.hibernate.annotations.Table(appliesTo="workbenchrow", indexes =
    {   
        @Index (name="RowNumberIDX", columnNames={"RowNumber"})
    })
@org.hibernate.annotations.Proxy(lazy = false)
public class WorkbenchRow implements java.io.Serializable, Comparable<WorkbenchRow>
{
    private static final Logger log = Logger.getLogger(WorkbenchRow.class);
    
    public enum LoadStatus {None, Successful, Error, OutOfMemory, TooLarge}
    
    protected Long                   workbenchRowId;
    protected Short                  rowNumber;
    protected byte[]                 cardImageData;
    protected String                 cardImageFullPath;
    protected String                 bioGeomancerResults;
    protected Set<WorkbenchDataItem> workbenchDataItems;
    protected Set<WorkbenchRowImage> workbenchRowImages;
    protected Workbench              workbench;
    
    // XXX PREF
    protected int                      maxWidth  = 500;
    protected int                      maxHeight = 500;
    protected int                      maxImageSize = 16000000; // ~ 16 MB
    protected WeakReference<ImageIcon> fullSizeImageWR = null;
    
    // Transient Data Members
    protected Hashtable<Short, WorkbenchDataItem>            items         = new Hashtable<Short, WorkbenchDataItem>();
    protected LoadStatus                                     loadStatus    = LoadStatus.None;
    protected Exception                                      loadException = null;
    protected ImageIcon                                      imgIcon       = null;
    
    /**
     * Constructor (for JPA).
     */
    public WorkbenchRow()
    {
        //
    }
    
    /**
     * Constrcutor for the code that knows the row number.
     * @param rowNum the row number or index
     */
    public WorkbenchRow(final Workbench workbench, final short rowNum)
    {
        initialize();
        
        this.workbench = workbench;
        this.rowNumber = rowNum;
    }
    
    // Initializer
    public void initialize()
    {
        workbenchRowId     = null;
        workbench          = null;
        rowNumber          = null;
        workbenchDataItems = new HashSet<WorkbenchDataItem>();
    }
    // End Initializer
    
    /**
     * Assumes it is connected to a Session and forces all the data to be loaded. 
     */
    public void forceLoad()
    {
        for (WorkbenchDataItem item : getWorkbenchDataItems())
        {
            item.getCellData();
        }

        if (getWorkbenchRowImages() != null)
        {
            getWorkbenchRowImages().size();
        }
    }
    
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchRowID", nullable = false)
    public Long getWorkbenchRowId()
    {
        return workbenchRowId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    public Long getId()
    {
        return this.workbenchRowId;
    }
    
    public void setWorkbenchRowId(Long workbenchRowId)
    {
        this.workbenchRowId = workbenchRowId;
    }
    

    /**
     * @return
     */
    @Column(name = "RowNumber")
    public Short getRowNumber()
    {
        return rowNumber;
    }

    public void setRowNumber(Short rowNumber)
    {
        this.rowNumber = rowNumber;
    }

    @Lob
    @Column(name = "CardImageData", length=16000000)
    public byte[] getCardImageData()
    {
        return cardImageData;
    }

    public void setCardImageData(byte[] cardImageData)
    {
        imgIcon = null;
        this.cardImageData = cardImageData;
    }
        
    public synchronized void setImage(int index, File imgOrig) throws IOException
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
        }
        
        if (index > workbenchRowImages.size()-1)
        {
            addImage(imgOrig);
            return;
        }
        
        for (WorkbenchRowImage img: workbenchRowImages)
        {
            if (img.getImageOrder().intValue() == index)
            {
                byte[] newImageData = readAndScaleCardImage(imgOrig);
                img.setCardImageData(newImageData);
                img.setCardImageFullPath(imgOrig.getAbsolutePath());
                return;
            }
        }
    }
    
    /**
     * Adds a new image to the row.
     * 
     * @param orig the image file
     * @return the index of the new image
     * @throws IOException if an error occurs while loading or scaling the image file
     */
    public synchronized int addImage(File orig) throws IOException
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
        }
        
        byte[] imgData = readAndScaleCardImage(orig);
        
        int order = workbenchRowImages.size();
        WorkbenchRowImage newRowImage = new WorkbenchRowImage();
        newRowImage.initialize();
        newRowImage.setImageOrder(order);
        newRowImage.setCardImageFullPath(orig.getAbsolutePath());
        newRowImage.setCardImageData(imgData);
        newRowImage.setWorkbenchRow(this);
        workbenchRowImages.add(newRowImage);
        return order;
    }
    
    public synchronized void deleteImage(int index)
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
            return;
        }
        
        WorkbenchRowImage toDelete = null;
        
        for (WorkbenchRowImage rowImg: workbenchRowImages)
        {
            if (rowImg.getImageOrder().intValue() == index)
            {
                toDelete = rowImg;
                continue;
            }
            
            if (rowImg.getImageOrder() > index)
            {
                int newOrder = rowImg.getImageOrder().intValue() - 1;
                rowImg.setImageOrder(newOrder);
            }
        }
        
        if (toDelete != null)
        {
            toDelete.setWorkbenchRow(null);
            workbenchRowImages.remove(toDelete);
        }
    }
    
    @Transient
    public synchronized WorkbenchRowImage getRowImage(int index)
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
            return null;
        }
        
        for (WorkbenchRowImage img: workbenchRowImages)
        {
            if (img.getImageOrder() != null && img.getImageOrder().intValue() == index)
            {
                return img;
            }
        }
        return null;
    }
    
    @Transient
    public synchronized ImageIcon getCardImage()
    {
        if (cardImageData == null || cardImageData.length == 0)
        {
            return null;
        }
        // otherwise
        
        if (imgIcon == null)
        {
            imgIcon = new ImageIcon(cardImageData);
        }
        return imgIcon;
    }
    
    /**
     * Stores the image found at imgFilePath into the row as the card image data, scaling
     * the image if necessary.  The path to the original image is also set via {@link #setCardImageFullPath(String)}.
     * 
     * This code is taken almost completely from ImageThumbnailGenerator.
     * 
     * @param imgFilePath the full path to the image file
     * @throws IOException 
     */
    public synchronized void setCardImage(final String imgFilePath)
    {
        setCardImage(new File(imgFilePath));
    }
    
    public synchronized byte[] readAndScaleCardImage(final File imageFile) throws IOException
    {
        if (imageFile == null)
        {
            throw new NullPointerException("Provided File must be non-null");
        }

        if (imageFile.length() < this.maxImageSize)
        {
            // read the original
            BufferedImage img = ImageIO.read(imageFile);

            // determine if we need to scale
            int origWidth = img.getWidth();
            int origHeight = img.getHeight();
            boolean scale = false;

            if (origWidth > this.maxWidth || origHeight > maxHeight)
            {
                scale = true;
            }

            byte[] imgBytes = null;

            if (scale)
            {
                imgBytes = GraphicsUtils.scaleImage(img, this.maxHeight, this.maxWidth, true);
            }
            else
            {
                // since we don't need to scale the image, just grab its bytes
                imgBytes = FileUtils.readFileToByteArray(imageFile);
            }
            
            return imgBytes;
        }
        // else, image is too large
        throw new IOException("Provided image is too large.  Maximum image size is " + this.maxImageSize + " bytes.");
    }
    
    /**
     * Stores the image found at imgFilePath into the row as the card image data, scaling the image
     * if necessary. The path to the original image is also set via
     * {@link #setCardImageFullPath(String)}.
     * 
     * This code is taken almost completely from ImageThumbnailGenerator.
     * 
     * @param imageFile the full path to the image file
     * @throws IOException
     */
    public synchronized void setCardImage(final File imageFile)
    {
        imgIcon       = null;
        
        loadStatus    = LoadStatus.None;
        loadException = null;
        
        if (imageFile == null)
        {
            setCardImageData(null);
            setCardImageFullPath(null);
            fullSizeImageWR = null;
            return;
        }
        
        byte[] imgData;
        try
        {
            imgData = readAndScaleCardImage(imageFile);
        }
        catch (IOException e)
        {
            loadStatus = LoadStatus.Error;
            loadException = e;
            return;
        }
        
        if (imgData != null)
        {
            setCardImageData(imgData);
            setCardImageFullPath(imageFile.getAbsolutePath());
        }
    }
    
    /**
     * @return the loadException
     */
    @Transient
    public Exception getLoadException()
    {
        return loadException;
    }

    /**
     * @return the loadStatus
     */
    @Transient
    public LoadStatus getLoadStatus()
    {
        return loadStatus;
    }

    @Column(name="CardImageFullPath", length=255)
    public String getCardImageFullPath()
    {
        return cardImageFullPath;
    }

    public void setCardImageFullPath(String cardImageFullPath)
    {
        this.cardImageFullPath = cardImageFullPath;
        
        // clear out the weak reference to the card image, since it's out of date now
        synchronized(this)
        {
            if (fullSizeImageWR != null)
            {
                fullSizeImageWR = null;
            }
        }
    }

    /**
     * Gets the XML text of the BioGeomancer response from a lookup
     * using the data in this row.
     * 
     * @return the XML string
     */
    @Lob
    @Column(name="BioGeomancerResults", length=65535)
    public String getBioGeomancerResults()
    {
        return bioGeomancerResults;
    }

    /**
     * Stores the given string as the text of the BioGeomancer response
     * from a lookup using the data in this row.
     * 
     * @param bioGeomancerResults the response text to store
     */
    public void setBioGeomancerResults(String bioGeomancerResults)
    {
        this.bioGeomancerResults = bioGeomancerResults;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchID", nullable = false)
    public Workbench getWorkbench()
    {
        return workbench;
    }

    public void setWorkbench(Workbench workbench)
    {
        this.workbench = workbench;
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "workbenchRow")
    // @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })
    public Set<WorkbenchDataItem> getWorkbenchDataItems()
    {
        return workbenchDataItems;
    }

    public void setWorkbenchDataItems(Set<WorkbenchDataItem> workbenchDataItems)
    {
        this.workbenchDataItems = workbenchDataItems;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "workbenchRow")
    @Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<WorkbenchRowImage> getWorkbenchRowImages()
    {
        return workbenchRowImages;
    }

    public void setWorkbenchRowImages(Set<WorkbenchRowImage> workbenchRowImages)
    {
        this.workbenchRowImages = workbenchRowImages;
    }

    /**
     * Returns a hashtable of items where the key is the column index of the item.
     * @return a hashtable of items where the key is the column index of the item.
     */
    @Transient
    public Hashtable<Short, WorkbenchDataItem> getItems()
    {
        return items;
    }

    /**
     * Returns the data string for a column.
     * @param col the column index
     * @return the string value of the column
     */
    public String getData(final int col)
    {
        if (items.size() != workbenchDataItems.size())
        {
            items.clear();
            for (WorkbenchDataItem wbdi : workbenchDataItems)
            {
                items.put(wbdi.getColumnNumber(), wbdi);
            }
        }
        WorkbenchDataItem wbdi = items.get((short)col);
        if (wbdi != null)
        {
            return wbdi.getCellData();
        }
        // else
        return "";
    }
    
    /**
     * Sets the string data into the column items.
     * @param dataStr the string data
     * @param col the column index to be set
     */
    public WorkbenchDataItem setData(final String dataStr, final short col)
    {
        WorkbenchDataItem wbdi = items.get(col);
        if (wbdi != null)
        {
            // XXX we may actually want to remove and 
            // delete the item if it is set to empty
            wbdi.setCellData(dataStr);
            
        } else // the cell doesn't exist so create one
        {
            if (StringUtils.isNotEmpty(dataStr))
            {
                Short inx = (short)col;
                wbdi = new WorkbenchDataItem(this, workbench.getMappingFromColumn(col), dataStr, rowNumber); // adds it to the row also
                items.put(inx, wbdi);
                workbenchDataItems.add(wbdi);
            }
        }
        return wbdi;
    }
    
    /**
     * Removes an item from the Row.
     * @param item the item to be removed
     * @return the same items that was removed
     */
    public WorkbenchDataItem delete(final WorkbenchDataItem item)
    {
        short colInx = item.getColumnNumber();
        items.remove(colInx);
        workbenchDataItems.remove(item);
        item.setWorkbenchRow(null);
        return item;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    public Class<?> getDataClass()
    {
        return WorkbenchRow.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 90;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final WorkbenchRow obj)
    {
        return rowNumber.compareTo(obj.rowNumber);
    }
    
    //------------------------------------------------------------------------
    // Large Image Support
    //------------------------------------------------------------------------
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     * @return Reads in the disciplines file (is loaded when the class is loaded).
     */
    @Transient
    public ImageIcon getFullSizeImage()
    {
        if (cardImageData != null && StringUtils.isNotEmpty(cardImageFullPath))
        {
            ImageIcon fullSizeImage = null;
            
            if (fullSizeImageWR != null)
            {
                fullSizeImage = fullSizeImageWR.get();
            }
            
            if (fullSizeImage == null)
            {
                try
                {
                    ImageIcon iconImage = new ImageIcon(cardImageFullPath);
                    fullSizeImageWR = new WeakReference<ImageIcon>(iconImage);
                    
                } catch (java.lang.OutOfMemoryError memEx)
                {
                    loadStatus = LoadStatus.OutOfMemory;
                    loadException = new Exception("Out of Memory");
                    log.error(memEx);
                    return null;
                }
                catch (Exception ex)
                {
                    log.error(ex);
                    loadStatus    = LoadStatus.Error;
                    loadException = ex;
                    return null;
                }
            }
            
            return fullSizeImageWR.get();
        }
        return null;
    }

    ////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////

    @Transient
    public int getLocalityStringIndex()
    {
        return workbench.getColumnIndex(Locality.class, "localityName");
    }

    @Transient
    public int getCountryIndex()
    {
        return workbench.getColumnIndex(Geography.class, "Country");
    }

    @Transient
    public int getStateIndex()
    {
        return workbench.getColumnIndex(Geography.class, "State");
    }

    @Transient
    public int getCountyIndex()
    {
        return workbench.getColumnIndex(Geography.class, "County");
    }
}
