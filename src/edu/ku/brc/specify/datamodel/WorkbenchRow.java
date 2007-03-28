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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

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
import org.hibernate.annotations.Index;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace;
import edu.ku.brc.util.Pair;

/**
 * WorkbenchRow generated rods
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "workbenchrow")
@org.hibernate.annotations.Table(appliesTo="taxon", indexes =
    {   
        @Index (name="RowNumberIDX", columnNames={"RowNumber"})
    })
@org.hibernate.annotations.Proxy(lazy = false)
public class WorkbenchRow implements java.io.Serializable, GoogleEarthPlacemarkIFace, Comparable<WorkbenchRow>
{
    protected Long                   workbenchRowId;
    protected Short                  rowNumber;
    protected byte[]                 cardImageData;
    protected String                 cardImageFullPath;
    protected String                 bioGeomancerResults;
    protected Set<WorkbenchDataItem> workbenchDataItems;
    protected Workbench              workbench;
    
    // XXX PREF
    protected int                      maxWidth  = 500;
    protected int                      maxHeight = 500;
    protected WeakReference<ImageIcon> fullSizeImageWR = null;
    
    // Transient Data Members
    protected Hashtable<Short, WorkbenchDataItem>            items    = new Hashtable<Short, WorkbenchDataItem>();
    protected Hashtable<Short, WorkbenchTemplateMappingItem> mappings = null;
    protected Vector<WorkbenchDataItem>                      dataList = null;

    
    /**
     * Constrcutor (for JPA).
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
        this.cardImageData = cardImageData;
    }
    
    @Transient
    public ImageIcon getCardImage()
    {
        if (cardImageData==null || cardImageData.length==0)
        {
            return null;
        }
        // otherwise
        
        ImageIcon imageIcon = new ImageIcon(cardImageData);
        return imageIcon;
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
    public void setCardImage(final String imgFilePath)
    {
        setCardImage(new File(imgFilePath));
    }
    
    /**
     * Stores the image found at imgFilePath into the row as the card image data, scaling
     * the image if necessary.  The path to the original image is also set via {@link #setCardImageFullPath(String)}.
     * 
     * This code is taken almost completely from ImageThumbnailGenerator.
     * 
     * @param imageFile the full path to the image file
     * @throws IOException 
     */
    public void setCardImage(final File imageFile)
    {
        try
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
                // calculate the new height and width while maintaining the aspect ratio
                int thumbWidth;
                int thumbHeight;
                if( origWidth >= origHeight )
                {
                    thumbWidth = maxWidth;
                    thumbHeight = (int)(origHeight * ((float)thumbWidth/(float)origWidth));
                }
                else
                {
                    thumbHeight = maxHeight;
                    thumbWidth = (int)(origWidth * ((float)thumbHeight/(float)origHeight));
                }
                
                // scale the image
                BufferedImage thumbImage = new BufferedImage(thumbWidth,thumbHeight,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics2D = thumbImage.createGraphics();
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics2D.drawImage(img, 0, 0, thumbWidth, thumbHeight, null);
    
                // save thumbnail image to the byte[] as a JPEG
                ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream(4096);
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outputByteStream);
                JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
                param.setQuality(1, false);
                encoder.setJPEGEncodeParam(param);
                encoder.encode(thumbImage);
                imgBytes = outputByteStream.toByteArray();
            }
            else
            {
                // since we don't need to scale the image, just grab its bytes
                imgBytes = FileUtils.readFileToByteArray(imageFile);
            }
            
            this.setCardImageData(imgBytes);
            this.setCardImageFullPath(imageFile.getAbsolutePath());
            
        } catch (Exception ex)
        {
            System.err.println(ex);
        }
    }
    
    @Column(name="CardImageFullPath", length=255)
    public String getCardImageFullPath()
    {
        return cardImageFullPath;
    }

    public void setCardImageFullPath(String cardImageFullPath)
    {
        this.cardImageFullPath = cardImageFullPath;
    }

    /**
     * Gets the XML text of the BioGeomancer response from a lookup
     * using the data in this row.
     * 
     * @return the XML string
     */
    @Lob
    @Column(name="BioGeomancerResults")
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
     * Sest the string data into the column items.
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
            
        } else
        {
            if (StringUtils.isNotEmpty(dataStr))
            {
                wbdi = new WorkbenchDataItem(this, dataStr, rowNumber, col); // adds it to the row also
                items.put(wbdi.getColumnNumber(), wbdi);
            }
        }
        return wbdi;
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
        ImageIcon fullSizeImage = null;
        
        if (fullSizeImageWR != null)
        {
            fullSizeImage = fullSizeImageWR.get();
        }
        
        if (fullSizeImage == null)
        {
            fullSizeImageWR = new WeakReference<ImageIcon>(new ImageIcon(cardImageFullPath));
        }
        
        return fullSizeImageWR.get();
    }
    
    //------------------------------------------------------------------------
    // GoogleEarthPlacemarkIFace Interface
    //------------------------------------------------------------------------
    
    protected void initExportData()
    {
        if (mappings == null)
        {
            mappings = new Hashtable<Short, WorkbenchTemplateMappingItem>();
            for (WorkbenchTemplateMappingItem wbtmi : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
            {
                mappings.put(wbtmi.getViewOrder(), wbtmi);
            }
        }
        
        if (dataList == null)
        {
            dataList = new Vector<WorkbenchDataItem>(getWorkbenchDataItems());
            Collections.sort(dataList);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getTitle()
     */
    @Transient
    public String getTitle()
    {
        initExportData();
       
        // XXX : Fix Me
//        StringBuilder sb = new StringBuilder();
//        for (WorkbenchDataItem wbdi : dataList)
//        {
//            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
//            
//            // XXX temporary fix  DEMO
//            if (wbtmi.getIsIncludedInTitle() || wbtmi.getCaption().equals("LocalityName") )
//            {
//                if (sb.length() > 0)
//                {
//                    sb.append(' ');
//                }
//                sb.append(wbdi.getCellData());
//            }
//        }
//        
//        return sb.toString();
        
        return Integer.toString(getRowNumber()+1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getHtmlContent()
     */
    @Transient
    public String getHtmlContent()
    {
        // XXX In the Future this should call a delegate
        // so it isn't hard code in the class
        
        initExportData();
        
        StringBuilder sb = new StringBuilder("<table>");
        for (WorkbenchDataItem wbdi : dataList)
        {
            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
            if (wbtmi.getIsExportableToContent())
            {
                sb.append("<tr><td align=\"right\">");
                sb.append(wbtmi.getCaption());
                sb.append("</td><td align=\"left\">");
                sb.append(wbdi.getCellData());
                sb.append("</td></tr>\n");
            }
        } 
        sb.append("</table>\n");
        
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
     */
    @Transient
    public Pair<Double, Double> getLatLon()
    {
        initExportData();
        
        Double latitude  = null;
        Double longitude = null;
        
        for (WorkbenchDataItem wbdi : dataList)
        {
            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
            if (wbtmi.getFieldName().equals("latitude1"))
            {
                String valStr = wbdi.getCellData();
                if (StringUtils.isNotEmpty(valStr))
                {
                    latitude = Double.parseDouble(valStr);
                }
            } else if (wbtmi.getFieldName().equals("longitude1"))
            {
                String valStr = wbdi.getCellData();
                if (StringUtils.isNotEmpty(valStr))
                {
                    longitude = Double.parseDouble(valStr);
                }
            }
        } 
        
        if (latitude != null && longitude != null)
        {
            return new Pair<Double, Double>(latitude, longitude);
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#cleanup()
     */
    public void cleanup()
    {
        mappings.clear();
        mappings = null;
    }
    
}
