/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;

import javax.persistence.*;

import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;

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
    protected BigDecimal              width;
    protected BigDecimal              height;
    protected BigDecimal              objLength;
    protected String             units;
    protected String             composition;
    protected String             remarks;
    protected String             source;
    protected Calendar           determinedDate;
    protected Byte               determinedDatePrecision;

    protected String             lightRecommendations;
    protected String             displayRecommendations;
    protected String             otherRecommendations;
    
    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;

    protected CollectionObject   collectionObject;
    protected Preparation        preparation;
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
        determinedDate       = null;
        lightRecommendations   = null;
        displayRecommendations = null;
        otherRecommendations   = null;
        collectionObject     = null;
        preparation = null;
        division             = AppContextMgr.getInstance().getClassObject(Division.class);
        events               = new HashSet<ConservEvent>();
        conservDescriptionAttachments = new HashSet<ConservDescriptionAttachment>();

        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
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
    @Column(name = "Width", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getWidth()
    {
        return this.width;
    }

    public void setWidth(final BigDecimal width)
    {
        this.width = width;
    }

    /**
     *
     */
    @Column(name = "Height", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getHeight()
    {
        return this.height;
    }

    public void setHeight(final BigDecimal height)
    {
        this.height = height;
    }

    /**
     *
     */
    @Column(name = "ObjLength", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getObjLength()
    {
        return this.objLength;
    }

    public void setObjLength(final BigDecimal length)
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
     *      * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return this.text4;
    }
    
    public void setText4(String text4) {
        this.text4 = text4;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return this.text5;
    }
    
    public void setText5(String text5) {
        this.text5 = text5;
    }
    /**
     * @return the yesNo1
     */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    /**
     * @param yesNo1 the yesNo1 to set
     */
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     * @return the yesNo2
     */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    /**
     * @param yesNo2 the yesNo2 to set
     */
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the yesNo3
     */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    /**
     * @param yesNo3 the yesNo3 to set
     */
    public void setYesNo3(Boolean yesNo3) {
        this.yesNo3 = yesNo3;
    }
    
    /**
     * @return the yesNo4
     */
    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4() {
        return yesNo4;
    }

    /**
     * @param yesNo4 the yesNo4 to set
     */
    public void setYesNo4(Boolean yesNo4) {
        this.yesNo4 = yesNo4;
    }

    /**
     * @return the yesNo5
     */
    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5() {
        return yesNo5;
    }

    /**
     * @param yesNo5 the yesNo5 to set
     */
    public void setYesNo5(Boolean yesNo5) {
        this.yesNo5 = yesNo5;
    }
    
    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation()
    {
        return this.preparation;
    }

    public void setPreparation(final Preparation preparation)
    {
        this.preparation = preparation;
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

    @Temporal(TemporalType.DATE)
    @Column(name = "CatalogedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDeterminedDate() {
        return determinedDate;
    }

    public void setDeterminedDate(Calendar determinedDate) {
        this.determinedDate = determinedDate;
    }

    public Byte getDeterminedDatePrecision() {
        return determinedDatePrecision;
    }

    public void setDeterminedDatePrecision(Byte determinedDatePrecision) {
        this.determinedDatePrecision = determinedDatePrecision;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
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

	@Override
	public void forceLoad() {
		// TODO Auto-generated method stub
		super.forceLoad();
		events.size();
		for (ConservEvent ce : events) {
		    ce.forceLoad();
        }
        conservDescriptionAttachments.size();
	}
    
    
}
