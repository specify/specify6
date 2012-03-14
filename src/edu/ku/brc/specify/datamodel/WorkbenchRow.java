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
package edu.ku.brc.specify.datamodel;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;
import edu.ku.brc.util.LatLonConverter;

/**
 * WorkbenchRow generated rods
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchrow")
@org.hibernate.annotations.Table(appliesTo="workbenchrow", indexes =
    {   
        @Index (name="RowNumberIDX", columnNames={"RowNumber"})
    })
@SuppressWarnings("serial")
public class WorkbenchRow implements java.io.Serializable, Comparable<WorkbenchRow>, GeoCoordDataIFace
{
    private static final Logger log = Logger.getLogger(WorkbenchRow.class);
    
    public enum LoadStatus {None, Successful, Error, OutOfMemory, TooLarge}
    
    public static final byte UPLD_NONE = 0;
    public static final byte UPLD_SUCCESS = 1;
    public static final byte UPLD_SKIPPED = 2;
    public static final byte UPLD_FAILED = 3;
    
    private static long lastTruncErrorMilli = 0;
    
    protected Integer                workbenchRowId;
    protected Short                  rowNumber;
    protected byte[]                 cardImageData;
    protected String                 cardImageFullPath;
    protected String                 bioGeomancerResults;
    protected Set<WorkbenchDataItem> workbenchDataItems;
    protected Set<WorkbenchRowImage> workbenchRowImages;
    protected Workbench              workbench;
    protected Byte                   uploadStatus;
    protected Byte                   sgrStatus;
    protected String                 lat1Text;
    protected String                 lat2Text;
    protected String                 long1Text;
    protected String                 long2Text;
    
    //For updates
    protected Integer				 				recordId; //recordID exported from 'main' db to this row
    protected Set<WorkbenchRowExportedRelationship> workbenchRowExportedRelationships;
    
    // XXX PREF
    protected int                      				maxWidth  = 500;
    protected int                      				maxHeight = 500;
    protected int                      				maxImageSize = 16000000; // ~ 16 MB
    protected SoftReference<ImageIcon> 				fullSizeImageSR = null;
    
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
     * Constructor for the code that knows the row number.
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
        workbenchRowId     					= null;
        workbench          					= null;
        rowNumber          					= null;
        workbenchDataItems 					= new HashSet<WorkbenchDataItem>();
        workbenchRowImages 					= new HashSet<WorkbenchRowImage>();
        uploadStatus       					= UPLD_NONE;
        lat1Text           					= null;
        lat2Text           					= null;
        long1Text          					= null;
        long2Text          					= null;
        recordId           					= null;
        workbenchRowExportedRelationships 	= new HashSet<WorkbenchRowExportedRelationship>();
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
    @Column(name = "WorkbenchRowID")
    public Integer getWorkbenchRowId()
    {
        return workbenchRowId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    public Integer getId()
    {
    	return this.workbenchRowId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getGeoCoordId()
     */
    @Transient
    @Override
    public Integer getGeoCoordId()
    {
        return this.rowNumber.intValue();
    }
    
    public void setWorkbenchRowId(Integer workbenchRowId)
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

    /**
     * @param cardImageData
     */
    public void setCardImageData(byte[] cardImageData)
    {
        imgIcon = null;
        //this.cardImageData = cardImageData;
    }
        
    /**
     * @param index
     * @param imgOrig
     * @throws IOException
     */
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
                if (newImageData != null)
                {
                    //img.setCardImageData(newImageData);
                    img.setCardImageFullPath(imgOrig.getAbsolutePath());
                    return;
                }
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
    public synchronized int addImage(final File orig) throws IOException
    {
    	return addImage(orig, null);
    }

    /**
     * Adds a new image to the row.
     * 
     * @param orig the image file
     * @param attachToTlbName the table to attach the image to
     * @return the index of the new image
     * @throws IOException if an error occurs while loading or scaling the image file
     */
    public synchronized int addImage(final File orig, final String attachToTblName) throws IOException
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
        }
        
        byte[] imgData = readAndScaleCardImage(orig);
        if (imgData != null)
        {
            int order = workbenchRowImages.size();
            WorkbenchRowImage newRowImage = new WorkbenchRowImage();
            newRowImage.initialize();
            newRowImage.setImageOrder(order);
            newRowImage.setCardImageFullPath(orig.getAbsolutePath());
            //newRowImage.setCardImageData(imgData);
            newRowImage.setWorkbenchRow(this);
            newRowImage.setAttachToTableName(attachToTblName);
            workbenchRowImages.add(newRowImage);
            return order;
        }
        return -1;
    }
    
    /**
     * Adds a new image to the row.
     * 
     * @param orig the image file
     * @param attachToTlbName the table to attach the image to
     * @return the index of the new image
     * @throws IOException if an error occurs while loading or scaling the image file
     */
    public synchronized int addImagePath(final String orig, final String attachToTblName)
    {
        if (workbenchRowImages == null)
        {
            workbenchRowImages = new HashSet<WorkbenchRowImage>();
        }
        
        //byte[] imgData = readAndScaleCardImage(orig);
            int order = workbenchRowImages.size();
            WorkbenchRowImage newRowImage = new WorkbenchRowImage();
            newRowImage.initialize();
            newRowImage.setImageOrder(order);
            newRowImage.setCardImageFullPath(orig);
            newRowImage.setCardImageData(null);
            newRowImage.setWorkbenchRow(this);
            newRowImage.setAttachToTableName(attachToTblName);
            workbenchRowImages.add(newRowImage);
            return order;
    }

    /**
     * @param index
     */
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
            //commented out line below because it messes up delete-orphan process
            //toDelete.setWorkbenchRow(null);
            workbenchRowImages.remove(toDelete);
        }
    }
    
    /**
     * @param index
     * @return
     */
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
    
    /**
     * @return
     */
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
            //imgIcon = new ImageIcon(cardImageData);
            imgIcon = new ImageIcon(cardImageFullPath);
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
    
    /**
     * @param imageFile
     * @return
     * @throws IOException
     */
    public synchronized byte[] readAndScaleCardImage(final File imageFile) throws IOException
    {
        if (imageFile == null)
        {
            throw new NullPointerException("Provided File must be non-null");
        }

        if (!imageFile.exists())
        {
            loadStatus = LoadStatus.Error;
            loadException = new IOException();

            throw (IOException )loadException;
        }
        
        if (imageFile.length() < this.maxImageSize)
        {
            byte[] imgBytes = null;
            try
            {
                // read the original
                byte[] bytes = GraphicsUtils.readImage(imageFile);
                
                ImageIcon img = new ImageIcon(bytes);
    
                // determine if we need to scale
                int     origWidth  = img.getIconWidth();
                int     origHeight = img.getIconHeight();
                boolean scale      = false;
    
                if (origWidth > this.maxWidth || origHeight > maxHeight)
                {
                    scale = true;
                }
    
                if (scale)
                {
                    imgBytes = GraphicsUtils.scaleImage(bytes, this.maxHeight, this.maxWidth, true, false);
                }
                else
                {
                    // since we don't need to scale the image, just grab its bytes
                    imgBytes = bytes;
                }
                
            } catch (javax.imageio.IIOException ex)
            {
                UIRegistry.showLocalizedError("WB_IMG_BAD_FMT");
                loadStatus = LoadStatus.Error;
                loadException = ex;

                return null;
            }
            
            return imgBytes;
        }
        // else, image is too large
        String msg = String.format(UIRegistry.getResourceString("WB_IMG_TOO_BIG"), this.maxImageSize);
        UIRegistry.showError(msg);
        loadStatus = LoadStatus.Error;
        return null;
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
            fullSizeImageSR = null;
            return;
        }
        
        byte[] imgData = null;
        try
        {
            imgData = readAndScaleCardImage(imageFile);
        }
        catch (IOException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchRow.class, e);
            loadStatus = LoadStatus.Error;
            loadException = e;
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
            if (fullSizeImageSR != null)
            {
                fullSizeImageSR = null;
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
    @Column(name="BioGeomancerResults", length=8192)
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
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "WorkbenchID", nullable = false)
    public Workbench getWorkbench()
    {
        return workbench;
    }

    public void setWorkbench(Workbench workbench)
    {
        this.workbench = workbench;
    }

    @OneToMany(mappedBy = "workbenchRow")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<WorkbenchDataItem> getWorkbenchDataItems()
    {
        return workbenchDataItems;
    }

    public void setWorkbenchDataItems(Set<WorkbenchDataItem> workbenchDataItems)
    {
        this.workbenchDataItems = workbenchDataItems;
    }

    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "workbenchRow")
    @OneToMany( mappedBy = "workbenchRow")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<WorkbenchRowImage> getWorkbenchRowImages()
    {
        return workbenchRowImages;
    }

    public void setWorkbenchRowImages(Set<WorkbenchRowImage> workbenchRowImages)
    {
        this.workbenchRowImages = workbenchRowImages;
    }

    /**
     * @return the workbenchRowExportedRelationships
     */
    @OneToMany(mappedBy = "workbenchRow")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<WorkbenchRowExportedRelationship> getWorkbenchRowExportedRelationships()
    {
        return workbenchRowExportedRelationships;
    }

    /**
     * @param workbenchRowExportedRelationships
     */
    public void setWorkbenchRowExportedRelationships(Set<WorkbenchRowExportedRelationship> workbenchRowExportedRelationships)
    {
        this.workbenchRowExportedRelationships = workbenchRowExportedRelationships;
    }

    /**
     * Returns a hashtable of items where the key is the column index of the item.
     * @return a hashtable of items where the key is the column index of the item.
     */
    @Transient
    public Hashtable<Short, WorkbenchDataItem> getItems()
    {
        if (items.size() != workbenchDataItems.size())
        {
            items.clear();
            for (WorkbenchDataItem wbdi : workbenchDataItems)
            {
                items.put(wbdi.getColumnNumber(), wbdi);
            }
        }
        return items;
    }

    /**
     * Returns the data string for a column.
     * @param col the column index
     * @return the string value of the column
     */
    public String getData(final int col)
    {
        WorkbenchDataItem wbdi = getItems().get((short)col);
        if (wbdi != null)
        {
            return wbdi.getCellData();
        }
        // else
//        WorkbenchTemplateMappingItem wbtmi = workbench.getMappingFromColumn((short )col);
//        if (wbtmi != null && wbtmi.getIsRequired())
//        {
//        	//include empty items for required columns to make validation easier, and besides they will be needed eventually (unless wb is never uploaded)
//        	wbdi = new WorkbenchDataItem(this, workbench.getMappingFromColumn((short )col), null, rowNumber); // adds it to the row also
//        	items.put((short )col, wbdi);
//        	workbenchDataItems.add(wbdi);
//        }
        return "";
    }
    
//    /**
//     * @param wbdi
//     * @return true if the mapping item for wbdi is required.
//     */
//    protected boolean colIsRequired(WorkbenchTemplateMappingItem wbtmi)
//    {
//    	boolean required = false;
//    	if (wbtmi != null)
//    	{
//    		required = wbtmi.getIsRequired() || (wbtmi.getFieldInfo() != null && wbtmi.getFieldInfo().isRequired());
//    	}
//    	return required;
//    }
    
    /**
     * Sets the string data into the column items.
     * @param dataStr the string data
     * @param col the column index to be set
     */
   public WorkbenchDataItem setData(final String dataStr, final short col, final boolean updateGeoRefInfo)
    {
    	return setData(dataStr, col, updateGeoRefInfo, false);
    }
    /**
     * Sets the string data into the column items.
     * @param dataStr the string data
     * @param col the column index to be set
     */
    public WorkbenchDataItem setData(final String dataStr, final short col, final boolean updateGeoRefInfo, final boolean isRequired)
    {
        String data;
        if (StringUtils.isNotEmpty(dataStr) && dataStr.length() > WorkbenchDataItem.getMaxWBCellLength())
        {
            data = dataStr.substring(0, WorkbenchDataItem.getMaxWBCellLength());
            
            // I hate having to do this here, but otherwise it would involve changing too much code.
            // I dispatch the error so there is no UI code here in the data model class.
            if (CommandDispatcher.getInstance() != null)
            {
                long now = System.currentTimeMillis();
                if (now - lastTruncErrorMilli > 4000) // this stops there from being a bunch of errors being displayed. (4 seconds is arbitrary)
                {
                    CommandDispatcher.dispatch(new CommandAction("ERRMSG", "DISPLAY", "WB_ERR_DATA_TRUNC"));
                    lastTruncErrorMilli = now;
                }
            }
        } else
        {
            data = dataStr;
        }
        
        WorkbenchDataItem wbdi = getItems().get(col);
        if (wbdi != null)
        {
            // remove the item if it is set to empty and is not required
            if (StringUtils.isEmpty(data) && (wbdi.isRequired()))
            {
                items.remove(col);
                workbenchDataItems.remove(wbdi);
            }
            //if nothing has changed return null. (Mostly to prevent unnecessary updates of Georef texts).
            else if (data.equals(wbdi.getCellData()))
            {
                return null;
            }
            
            wbdi.setCellData(data);
        } else // the cell doesn't exist so create one
        {
        	if (StringUtils.isNotEmpty(data) || isRequired)
            {
        		Short inx = (short)col;
                wbdi = new WorkbenchDataItem(this, workbench.getMappingFromColumn(col), data, rowNumber); // adds it to the row also
                if (isRequired)
                {
                	wbdi.setRequired(true);
                }
                items.put(inx, wbdi);
                workbenchDataItems.add(wbdi);
            }
        }
        
        //XXX - currently not controlling edits of uploaded recs. 
        //Just clear status when edited, for now.
        this.uploadStatus = WorkbenchRow.UPLD_NONE;

        if (wbdi != null)
        {
            if (updateGeoRefInfo)
            {
                updateGeoRefTextFldsIfNecessary(wbdi.getWorkbenchTemplateMappingItem());
            }
            if (wbdi.getValidationStatus() == WorkbenchDataItem.VAL_ERROR)
            {
                wbdi.setValidationStatus(WorkbenchDataItem.VAL_ERROR_EDIT);
            }
            else if (wbdi.getValidationStatus() == WorkbenchDataItem.VAL_OK)
            {
                wbdi.setValidationStatus(WorkbenchDataItem.VAL_NONE);
            }
        }
        return wbdi;
    }

    /**
     * @param wbdi
     */
    public void updateGeoRefTextFldsIfNecessary(final WorkbenchTemplateMappingItem map)
    {
        if (map.getTableName().equals("locality"))
        {
            
            if (map.getFieldName().equalsIgnoreCase("latitude1") || map.getFieldName().equalsIgnoreCase("latitude2")
                    || map.getFieldName().equalsIgnoreCase("longitude1") || map.getFieldName().equalsIgnoreCase("longitude2"))
            {
                for (WorkbenchDataItem geoFld : getGeoCoordFlds())
                {
                    WorkbenchTemplateMappingItem geoMap = geoFld.getWorkbenchTemplateMappingItem();
                    if (geoMap.getFieldName().equalsIgnoreCase("latitude1"))
                    {
                        setLat1Text(getLatString(geoFld.getCellData()));
                    }
                    else if (geoMap.getFieldName().equalsIgnoreCase("latitude2"))
                    {
                        setLat2Text(getLatString(geoFld.getCellData()));
                    }
                    else if (geoMap.getFieldName().equalsIgnoreCase("longitude1"))
                    {
                        setLong1Text(getLongString(geoFld.getCellData()));
                    }
                    else if (geoMap.getFieldName().equalsIgnoreCase("longitude2"))
                    {
                        setLong2Text(getLongString(geoFld.getCellData()));
                    }
                }
                
            }
            
        }
    }
    
    
    /**
     * @return data items that map to latitude1/2 or longitude1/2. 
     */
    @Transient
    protected List<WorkbenchDataItem> getGeoCoordFlds()
    {
        LinkedList<WorkbenchDataItem> result = new LinkedList<WorkbenchDataItem>();
        for (WorkbenchDataItem wbdi : workbenchDataItems)
        {
            WorkbenchTemplateMappingItem map = wbdi.getWorkbenchTemplateMappingItem();
            if (map.getTableName().equals("locality")) 
            {
                if (map.getFieldName().equalsIgnoreCase("latitude1") || map.getFieldName().equalsIgnoreCase("latitude2")
                        || map.getFieldName().equalsIgnoreCase("longitude1") || map.getFieldName().equalsIgnoreCase("longitude2"))
                result.add(wbdi);
            }
        }
        return result;
    }
    
    /**
     * @param latEntry
     * @return a formatted string for use by specify.plugins.latlon  plugin
     */
    @Transient
    protected String getLatString(final String latEntry)
    {
        String ddString = null;
        try
        {
            GeoRefConverter geoConverter = new GeoRefConverter();
            LatLonConverter.FORMAT fmt = geoConverter.getLatLonFormat(StringUtils.stripToNull(latEntry));
            int decimalSize = geoConverter.getDecimalSize(StringUtils.stripToNull(latEntry));
            if (fmt.equals(LatLonConverter.FORMAT.None))
            {
                return null;
            }
            ddString = geoConverter.convert(StringUtils.stripToNull(latEntry), GeoRefFormat.D_PLUS_MINUS.name());
            BigDecimal bigD = UIHelper.parseDoubleToBigDecimal(ddString);
            return LatLonConverter.ensureFormattedString(bigD, null, fmt, LatLonConverter.LATLON.Latitude, decimalSize);
        }
        catch (NumberFormatException ex)
        {
            //ignore
        }
        catch (Exception ex)
        {
            //ignore;
        }
        return null;
    }
    
    /**
     * @param longEntry
     * @return a formatted string for use by specify.plugins.latlon  plugin
     */
    @Transient
    protected String getLongString(final String longEntry)
    {
        String ddString = null;
        try
        {
            GeoRefConverter geoConverter = new GeoRefConverter();
            LatLonConverter.FORMAT fmt = geoConverter.getLatLonFormat(StringUtils.stripToNull(longEntry));
            int decimalSize = geoConverter.getDecimalSize(StringUtils.stripToNull(longEntry));
            if (fmt.equals(LatLonConverter.FORMAT.None))
            {
                return null;
            }
            ddString = geoConverter.convert(StringUtils.stripToNull(longEntry), GeoRefFormat.D_PLUS_MINUS.name());
            BigDecimal bigD = UIHelper.parseDoubleToBigDecimal(ddString);
            return LatLonConverter.ensureFormattedString(bigD, null, fmt, LatLonConverter.LATLON.Longitude, decimalSize);
        }
        catch (NumberFormatException ex)
        {
            //ignore
        }
        catch (Exception ex)
        {
            //ignore;
        }
        return null;
    }
    
    /**
     * Removes an item from the Row.
     * 
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
        return rowNumber != null && obj != null && obj.rowNumber != null ? rowNumber.compareTo(obj.rowNumber) : 0;
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
            
            if (fullSizeImageSR != null)
            {
                fullSizeImage = fullSizeImageSR.get();
            }
            
            if (fullSizeImage == null)
            {
                try
                {
                    ImageIcon iconImage = new ImageIcon(cardImageFullPath);
                    fullSizeImageSR = new SoftReference<ImageIcon>(iconImage);
                    
                } catch (java.lang.OutOfMemoryError memEx)
                {
                    loadStatus = LoadStatus.OutOfMemory;
                    loadException = new Exception("Out of Memory");
                    log.error(memEx);
                    return null;
                }
                catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchRow.class, ex);
                    log.error(ex);
                    loadStatus    = LoadStatus.Error;
                    loadException = ex;
                    return null;
                }
            }
            
            return fullSizeImageSR.get();
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

    @Transient
    public int getLatitudeIndex()
    {
        return workbench.getColumnIndex(Locality.class, "latitude1");
    }

    @Transient
    public int getLongitudeIndex()
    {
        return workbench.getColumnIndex(Locality.class, "longitude1");
    }

    ////////////////////////////////////////////////////
    // GeoCoordDataIFace methods
    ////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCountry()
     */
    @Transient
    public String getCountry()
    {
        return getData(getCountryIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCounty()
     */
    @Transient
    public String getCounty()
    {
        return getData(getCountyIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLatitude()
     */
    @Transient
    public String getLatitude()
    {
        return getData(getLatitudeIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLocalityString()
     */
    @Transient
    public String getLocalityString()
    {
        return getData(getLocalityStringIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLongitude()
     */
    @Transient
    public String getLongitude()
    {
        return getData(getLongitudeIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getState()
     */
    @Transient
    public String getState()
    {
        return getData(getStateIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getTitle()
     */
    @Transient
    public String getTitle()
    {
        return getData(getLocalityStringIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getXML()
     */
    @Transient
    public String getXML()
    {
        return getBioGeomancerResults();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#set(java.lang.Double, java.lang.Double)
     */
    @Transient
    public void set(String latitude, String longitude)
    {
        setData(latitude, (short)getLatitudeIndex(), true);
        setData(longitude, (short)getLongitudeIndex(), true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#setXML(java.lang.String)
     */
    public void setXML(String xml)
    {
        setBioGeomancerResults(xml);
    }

    /**
     * @return
     */
    @Column(name = "UploadStatus", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getUploadStatus()
    {
        return uploadStatus;
    }

    /**
     * @param uploadStatus
     */
    public void setUploadStatus(Byte uploadStatus)
    {
        this.uploadStatus = uploadStatus;
    }

    
    /**
	 * @return the sgrStatus
	 */
    @Column(name = "SGRStatus", unique = false, nullable = true, insertable = true, updatable = true)
	public Byte getSgrStatus() 
	{
		return sgrStatus;
	}

	/**
	 * @param sgrStatus the sgrStatus to set
	 */
	public void setSgrStatus(Byte sgrStatus) 
	{
		this.sgrStatus = sgrStatus;
	}

	/**
	 * @return the recordId
	 */
    @Column(name = "RecordID", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getRecordId()
	{
		return recordId;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(Integer recordId)
	{
		this.recordId = recordId;
	}

	/**
     * @return the lat1Text
     */
    @Column(name = "Lat1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat1Text()
    {
        return lat1Text;
    }

    /**
     * @param lat1Text the lat1Text to set
     */
    public void setLat1Text(String lat1Text)
    {
        this.lat1Text = lat1Text;
    }

    /**
     * @return the lat2Text
     */
    @Column(name = "Lat2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLat2Text()
    {
        return lat2Text;
    }

    /**
     * @param lat2Text the lat2Text to set
     */
    public void setLat2Text(String lat2Text)
    {
        this.lat2Text = lat2Text;
    }

    /**
     * @return the long1Text
     */
    @Column(name = "Long1Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong1Text()
    {
        return long1Text;
    }

    /**
     * @param long1Text the long1Text to set
     */
    public void setLong1Text(String long1Text)
    {
        this.long1Text = long1Text;
    }

    /**
     * @return the long2Text
     */
    @Column(name = "Long2Text", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLong2Text()
    {
        return long2Text;
    }

    /**
     * @param long2Text the long2Text to set
     */
    public void setLong2Text(String long2Text)
    {
        this.long2Text = long2Text;
    }

}
