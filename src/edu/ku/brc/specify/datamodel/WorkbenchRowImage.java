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
import java.lang.ref.SoftReference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.ImageIcon;

/**
 * A data class to hold images corresponding to WorkbenchRows.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals (as described be {@link Comparable#compareTo(Object)}.
 *
 * @author jstewart
 * @code_status Alpha
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchrowimage")
@SuppressWarnings("serial")
public class WorkbenchRowImage implements java.io.Serializable, Comparable<WorkbenchRowImage>
{
    protected Integer         workbenchRowImageId;
    protected Integer      imageOrder;
    protected byte[]       cardImageData;
    protected String       cardImageFullPath;
    protected String       attachToTableName; //which table to attach the image when uploaded
    protected WorkbenchRow workbenchRow;
    protected SoftReference<ImageIcon> fullSizeImageSR = null;
    protected ImageIcon thumbnail = null;
    
    /**
     * Constructor (for JPA compliance).
     */
    public WorkbenchRowImage()
    {
        //
    }
    
    public void initialize()
    {
        workbenchRowImageId = null;
        imageOrder          = null;
        cardImageData       = null;
        cardImageFullPath   = null;
        attachToTableName   = null;
        workbenchRow        = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "WorkbenchRowImageID", nullable = false)
    public Integer getWorkbenchRowImageId()
    {
        return workbenchRowImageId;
    }

    public void setWorkbenchRowImageId(Integer workbenchRowImageId)
    {
        this.workbenchRowImageId = workbenchRowImageId;
    }
    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    public Integer getId()
    {
        return this.workbenchRowImageId;
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
	 * @return the attachToTableName
	 */
    @Column(name="AttachToTableName", length=64)
	public String getAttachToTableName()
	{
		return attachToTableName;
	}

	/**
	 * @param attachToTableName the attachToTableName to set
	 */
	public void setAttachToTableName(String attachToTableName)
	{
		this.attachToTableName = attachToTableName;
	}

	@Column(name = "ImageOrder")
    public Integer getImageOrder()
    {
        return imageOrder;
    }

    public void setImageOrder(Integer imageOrder)
    {
        this.imageOrder = imageOrder;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "WorkbenchRowID", nullable = false)
    public WorkbenchRow getWorkbenchRow()
    {
        return workbenchRow;
    }

    public void setWorkbenchRow(WorkbenchRow workbenchRow)
    {
        this.workbenchRow = workbenchRow;
    }
    
    @Override
    public String toString()
    {
        return imageOrder + ": " + cardImageFullPath;
    }
    
    ////////////////////////////////////
    // Helper methods
    ////////////////////////////////////
    
    @Transient
    public ImageIcon getImage()
    {
        if (cardImageData != null)
        {
            return new ImageIcon(cardImageData);
        }
        
        return getFullSizeImage();
    }
    
    @Transient
    public ImageIcon getFullSizeImage()
    {
            ImageIcon fullSizeImage = null;
            
            // try to get the image from the SoftReference
            if (fullSizeImageSR != null)
            {
                fullSizeImage = fullSizeImageSR.get();
            }
            
            // if the image is still null, reload the SoftReference
            if (fullSizeImage == null || fullSizeImage.getIconWidth() == -1)
            {
                ImageIcon iconImage = null;
                
                File file = new File(cardImageFullPath);
                if (file.exists())
                {
                    iconImage = new ImageIcon(cardImageFullPath);
                    if (iconImage == null || iconImage.getIconHeight() == -1)
                    {
                        try
                        {
                            iconImage = new ImageIcon(cardImageData);
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                
                if (iconImage == null || iconImage.getIconHeight() == -1)
                {
                    fullSizeImageSR = null;
                    return null;
                }
                
                fullSizeImageSR = new SoftReference<ImageIcon>(iconImage);
            }
            
            return fullSizeImageSR.get();
    }
    
    @Transient
    public ImageIcon getThumbnail()
    {
        return thumbnail;
    }
    
    public void setThumbnail(ImageIcon thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WorkbenchRowImage o)
    {
        if (imageOrder != null && o != null && o.imageOrder != null)
        {
            return imageOrder.compareTo(o.imageOrder);
        }
        return 0;
    }
}
