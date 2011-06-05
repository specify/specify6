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

import java.util.HashSet;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * 
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 29, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "conservdescription")
@org.hibernate.annotations.Table(appliesTo="conservdescription", indexes =
    {   @Index (name="ConservDescShortDescIDX", columnNames={"ShortDesc"})
    })
public class ConservDescription extends DataModelObjBase implements AttachmentOwnerIFace<ConservDescriptionAttachment>, java.io.Serializable, Cloneable
{
    // Fields    
    protected Integer            conservDescriptionId;
    protected String             shortDesc;
    protected String             description;
    protected String             backgroundInfo;
    protected Float              width;
    protected Float              height;
    protected Float              objLength;
    protected String             units;
    protected String             composition;
    protected String             remarks;
    protected String             source;
    
    protected String             lightRecommendations;
    protected String             displayRecommendations;
    protected String             otherRecommendations;
    
    protected CollectionObject   collectionObject;
    protected Division           division;
    
    protected Set<ConservEvent>  events;
    protected Set<ConservDescriptionAttachment> conservDescriptionAttachments;

    // Constructors

    /** default constructor */
    public ConservDescription()
    {
        //
    }

    /** constructor with id */
    public ConservDescription(Integer conservDescriptionId)
    {
        this.conservDescriptionId = conservDescriptionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        conservDescriptionId = null;
        shortDesc            = null;
        description          = null;
        backgroundInfo       = null;
        width                = null;
        height               = null;
        objLength            = null;
        units                = null;
        composition          = null;
        remarks              = null;
        source               = null;
        lightRecommendations   = null;
        displayRecommendations = null;
        otherRecommendations   = null;
        collectionObject     = null;
        division             = null;
        events               = new HashSet<ConservEvent>();
        conservDescriptionAttachments = new HashSet<ConservDescriptionAttachment>();

    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ConservDescriptionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getConservDescriptionId()
    {
        return this.conservDescriptionId;
    }

    public void setConservDescriptionId(Integer conservDescriptionId)
    {
        this.conservDescriptionId = conservDescriptionId;
    }

    /**
     *
     */
    @Column(name = "ShortDesc", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getShortDesc()
    {
        return this.shortDesc;
    }

    public void setShortDesc(final String shortDesc)
    {
        this.shortDesc = shortDesc;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     *
     */
    @Lob
    @Column(name = "BackgroundInfo", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getBackgroundInfo()
    {
        return this.backgroundInfo;
    }

    public void setBackgroundInfo(final String backgroundInfo)
    {
        this.backgroundInfo = backgroundInfo;
    }

    /**
     *
     */
    @Column(name = "Width", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getWidth()
    {
        return this.width;
    }

    public void setWidth(final Float width)
    {
        this.width = width;
    }

    /**
     *
     */
    @Column(name = "Height", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getHeight()
    {
        return this.height;
    }

    public void setHeight(final Float height)
    {
        this.height = height;
    }

    /**
     *
     */
    @Column(name = "ObjLength", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getObjLength()
    {
        return this.objLength;
    }

    public void setObjLength(final Float length)
    {
        this.objLength = length;
    }

    /**
     *
     */
    @Column(name = "Units", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getUnits()
    {
        return this.units;
    }

    public void setUnits(final String units)
    {
        this.units = units;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Composition", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getComposition()
    {
        return this.composition;
    }

    public void setComposition(final String composition)
    {
        this.composition = composition;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(final String remarks)
    {
        this.remarks = remarks;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Source", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getSource()
    {
        return this.source;
    }

    public void setSource(final String source)
    {
        this.source = source;
    }
    
    /**
     * @return the lightRecommendations
     */
    @Lob
    @Column(name = "LightRecommendations", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getLightRecommendations()
    {
        return lightRecommendations;
    }

    /**
     * @param lightRecommendations the lightRecommendations to set
     */
    public void setLightRecommendations(String lightRecommendations)
    {
        this.lightRecommendations = lightRecommendations;
    }

    /**
     * @return the displayRecommendations
     */
    @Lob
    @Column(name = "DisplayRecommendations", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getDisplayRecommendations()
    {
        return displayRecommendations;
    }

    /**
     * @param displayRecommendations the displayRecommendations to set
     */
    public void setDisplayRecommendations(String displayRecommendations)
    {
        this.displayRecommendations = displayRecommendations;
    }

    /**
     * @return the otherRecommendations
     */
    @Lob
    @Column(name = "OtherRecommendations", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getOtherRecommendations()
    {
        return otherRecommendations;
    }

    /**
     * @param otherRecommendations the otherRecommendations to set
     */
    public void setOtherRecommendations(String otherRecommendations)
    {
        this.otherRecommendations = otherRecommendations;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObject getCollectionObject()
    {
        return this.collectionObject;
    }

    public void setCollectionObject(final CollectionObject collectionObject)
    {
        this.collectionObject = collectionObject;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Division getDivision()
    {
        return this.division;
    }

    public void setDivision(final Division division)
    {
        this.division = division;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "conservDescription")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ConservEvent> getEvents()
    {
       return this.events;
    }

    public void setEvents(final Set<ConservEvent> events)
    {
        this.events = events;
    }

    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "conservDescription")
    @OneToMany(mappedBy = "conservDescription")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<ConservDescriptionAttachment> getConservDescriptionAttachments()
    {
        return conservDescriptionAttachments;
    }

    public void setConservDescriptionAttachments(Set<ConservDescriptionAttachment> conservDescriptionAttachments)
    {
        this.conservDescriptionAttachments = conservDescriptionAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        if (division != null)
        {
            return Division.getClassTableId();
        }
        if (collectionObject != null)
        {
            return CollectionObject.getClassTableId();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (division != null)
        {
            return division.getId();
        }
        if (collectionObject != null)
        {
            return collectionObject.getId();
        }
        return null;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.conservDescriptionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ConservDescription.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
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
        return 103;
    }

    @Transient
    public Set<ConservDescriptionAttachment> getAttachmentReferences()
    {
        return conservDescriptionAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        ConservDescription obj = (ConservDescription)super.clone();
        
        obj.conservDescriptionId = null;
        obj.collectionObject     = null;
        obj.division             = null;
        
        obj.events = new HashSet<ConservEvent>();
        obj.conservDescriptionAttachments = new HashSet<ConservDescriptionAttachment>();
        
        return obj;
    }
}
