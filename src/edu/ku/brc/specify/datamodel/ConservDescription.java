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

import java.util.Calendar;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

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
public class ConservDescription extends DataModelObjBase implements java.io.Serializable
{
    // Fields    
    protected Integer          conservDescriptionId;
    protected String           shortDesc;
    protected String           description;
    protected String           backgroundInfo;
    protected Float            width;
    protected Float            height;
    protected Float            objLength;
    protected String           units;
    protected String           composition;
    protected String           remarks;
    protected String           source;
    protected Calendar         curatorApprovalDate;
    
    protected CollectionObject   collectionObject;
    protected Accession          accession;
    protected Division           division;
    protected Agent              curator;

    protected Set<Attachment>    attachments;
    protected Set<ConservEvent>  events;

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
        objLength               = null;
        units                = null;
        composition          = null;
        remarks              = null;
        source               = null;
        curatorApprovalDate  = null;
        attachments          = new HashSet<Attachment>();
        collectionObject     = null;
        accession            = null;
        division             = null;
        curator              = null;
        events               = new HashSet<ConservEvent>();
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
    @Column(name = "hortDesc", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
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
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
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
    @Column(name = "BackgroundInfo", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
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
    @Column(name = "omposition", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
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
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
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
    @Column(name = "Source", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getSource()
    {
        return this.source;
    }

    public void setSource(final String source)
    {
        this.source = source;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CuratorApprovalDate", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public Calendar getCuratorApprovalDate()
    {
        return this.curatorApprovalDate;
    }

    public void setCuratorApprovalDate(final Calendar curatorApprovalDate)
    {
        this.curatorApprovalDate = curatorApprovalDate;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "conservDescription")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Attachment> getAttachments()
    {
        return this.attachments;
    }

    public void setAttachments(final Set<Attachment> attachments)
    {
        this.attachments = attachments;
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
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession()
    {
        return this.accession;
    }

    public void setAccession(final Accession accession)
    {
        this.accession = accession;
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
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CuratorID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getCurator()
    {
        return this.curator;
    }

    public void setCurator(final Agent curator)
    {
        this.curator = curator;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "conservDescription")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservEvent> getEvents()
    {
       return this.events;
    }

   public void setEvents(final Set<ConservEvent> events)
   {
       this.events = events;
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

}
