/* Copyright (C) 2023, Specify Collections Consortium
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.persistence.*;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.AttributeProviderIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectingevent", uniqueConstraints = {
        @UniqueConstraint(columnNames={"DisciplineID", "UniqueIdentifier"} )
    }
)
@org.hibernate.annotations.Table(appliesTo="collectingevent", indexes =
    {   @Index (name="CEStationFieldNumberIDX", columnNames={"StationFieldNumber"}),
        @Index (name="CEStartDateIDX", columnNames={"StartDate"}),
        @Index (name="CEEndDateIDX", columnNames={"EndDate"}),
            @Index (name="CEUniqueIdentifierIDX", columnNames={"UniqueIdentifier"}),
            @Index (name="CEGuidIDX", columnNames={"GUID"})
    })
@SuppressWarnings("serial")
public class CollectingEvent extends DisciplineMember implements AttachmentOwnerIFace<CollectingEventAttachment>, 
                                                                 AttributeProviderIFace, 
                                                                 java.io.Serializable,
                                                                 Comparable<CollectingEvent>,
                                                                 Cloneable
{
    protected static final Logger log = Logger.getLogger(CollectingEvent.class);

    // Fields    

    protected Integer               collectingEventId;
    protected String                stationFieldNumber;
    protected String                stationFieldNumberModifier1;
    protected String                stationFieldNumberModifier2;
    protected String                stationFieldNumberModifier3;
    protected String                method;
    protected String                verbatimDate;
    protected Calendar              startDate;
    protected Byte                  startDatePrecision; // Accurate to Year, Month, Day
    protected String                startDateVerbatim;
    protected Calendar              endDate;
    protected Byte                  endDatePrecision;   // Accurate to Year, Month, Day
    protected String                endDateVerbatim;
    protected Short                 startTime;          // Minutes in 24 hours
    protected Short                 endTime;            // Minutes in 24 hours
    protected String                verbatimLocality;
    protected String                remarks;
    protected Byte                  visibility;
    protected SpecifyUser            visibilitySetBy;
    protected Set<CollectionObject> collectionObjects;
    protected Set<Collector>        collectors;
    protected Locality              locality;
    protected CollectingTrip        collectingTrip;
    protected Byte					sgrStatus;
    protected String                guid;
    protected Integer				integer1;
    protected Integer				integer2;
    protected Integer				reservedInteger3;
    protected Integer				reservedInteger4;
    protected String				text1;
    protected String				text2;
    protected String				text3;	
    protected String				text4;	
    protected String				text5;
    protected String				text6;
    protected String				text7;
    protected String				text8;
    protected String				reservedText1;
    protected String                reservedText2;
    protected PaleoContext			paleoContext;
    protected String uniqueIdentifier;

    protected CollectingEventAttribute          collectingEventAttribute;      // Specify 5 Attributes table
    protected Set<CollectingEventAttr>          collectingEventAttrs;          // Generic Expandable Attributes
    protected Set<CollectingEventAttachment>    collectingEventAttachments;
    protected Set<CollectingEventAuthorization> collectingEventAuthorizations;

    

    private static String ceCOSQL = " FROM collectingevent ce INNER JOIN collectionobject c ON ce.CollectingEventID = c.CollectingEventID WHERE c.CollectingEventID = ";

    // Constructors

    /** default constructor */
    public CollectingEvent()
    {
        // do nothing
    }
    
    /** constructor with id */
    public CollectingEvent(Integer collectingEventId) 
    {
        this.collectingEventId = collectingEventId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        collectingEventId = null;
        stationFieldNumber = null;
        stationFieldNumberModifier1 = null;
        stationFieldNumberModifier2 = null;
        stationFieldNumberModifier3 = null;
        method = null;
        verbatimDate = null;
        startDate = null;
        startDatePrecision = null;
        startDateVerbatim = null;
        endDate = null;
        endDatePrecision = null;
        endDateVerbatim = null;
        startTime = null;
        endTime = null;
        verbatimLocality = null;
        remarks    = null;
        visibility = null;
        sgrStatus  = null;
        guid       = null;
        integer1			  = null;
        integer2			  = null;
        reservedInteger3	  = null;
        reservedInteger4	  = null;
        text1				  = null;
        text2                 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        reservedText1         = null;
        reservedText2         = null;
        collectionObjects            = new HashSet<CollectionObject>();
        collectors                   = new HashSet<Collector>();
        locality                     = null;
        paleoContext          = null;
        uniqueIdentifier = null;
        collectingEventAttribute     = null;
        collectingEventAttrs         = new HashSet<CollectingEventAttr>();
        collectingEventAttachments   = new HashSet<CollectingEventAttachment>();
        collectingEventAuthorizations = new HashSet<CollectingEventAuthorization>();
        hasGUIDField = true;
        setGUID();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectingEventID", nullable = false)
    public Integer getCollectingEventId() {
        return this.collectingEventId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectingEventId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectingEvent.class;
    }
    
    public void setCollectingEventId(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }

    /**
     * Station number or field number of the site where collecting event took place, 
     * A number or code recorded in field notes and/or written on field tags that identifies ALL material collected in a CollectingEvent.
     */
    @Column(name = "StationFieldNumber", length = 50)
    public String getStationFieldNumber() {
        return this.stationFieldNumber;
    }
    
    public void setStationFieldNumber(String stationFieldNumber) {
        this.stationFieldNumber = stationFieldNumber;
    }

    
    /**
	 * @return the stationFieldNumberModifier1
	 */
    @Column(name = "StationFieldNumberModifier1", length = 50)
	public String getStationFieldNumberModifier1() {
		return stationFieldNumberModifier1;
	}

	/**
	 * @param stationFieldNumberModifier1 the stationFieldNumberModifier1 to set
	 */
	public void setStationFieldNumberModifier1(String stationFieldNumberModifier1) {
		this.stationFieldNumberModifier1 = stationFieldNumberModifier1;
	}

	/**
	 * @return the stationFieldNumberModifier2
	 */
	@Column(name = "StationFieldNumberModifier2", length = 50)
	public String getStationFieldNumberModifier2() {
		return stationFieldNumberModifier2;
	}

	/**
	 * @param stationFieldNumberModifier2 the stationFieldNumberModifier2 to set
	 */
	public void setStationFieldNumberModifier2(String stationFieldNumberModifier2) {
		this.stationFieldNumberModifier2 = stationFieldNumberModifier2;
	}

	/**
	 * @return the stationFieldNumberModifier3
	 */
	@Column(name = "StationFieldNumberModifier3", length = 50)
	public String getStationFieldNumberModifier3() {
		return stationFieldNumberModifier3;
	}

	/**
	 * @param stationFieldNumberModifier3 the stationFieldNumberModifier3 to set
	 */
	public void setStationFieldNumberModifier3(String stationFieldNumberModifier3) {
		this.stationFieldNumberModifier3 = stationFieldNumberModifier3;
	}
    /**
     *
     */
    @Column(name = "UniqueIdentifier", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

	/**
	 * @return the     /**
     *
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    
	/**
	 * @return the integer1
	 */
    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger1() {
		return integer1;
	}

	/**
	 * @param integer1 the integer1 to set
	 */
	public void setInteger1(Integer integer1) {
		this.integer1 = integer1;
	}

	/**
	 * @return the integer2
	 */
    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger2() {
		return integer2;
	}

	/**
	 * @param integer2 the integer2 to set
	 */
	public void setInteger2(Integer integer2) {
		this.integer2 = integer2;
	}

	/**
	 * @return the reservedInteger3
	 */
    @Column(name = "ReservedInteger3", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger3() {
		return reservedInteger3;
	}

	/**
	 * @param reservedInteger3 the reservedInteger3 to set
	 */
	public void setReservedInteger3(Integer reservedInteger3) {
		this.reservedInteger3 = reservedInteger3;
	}

	/**
	 * @return the reservedInteger4
	 */
    @Column(name = "ReservedInteger4", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger4() {
		return reservedInteger4;
	}

	/**
	 * @param reservedInteger4 the reservedInteger4 to set
	 */
	public void setReservedInteger4(Integer reservedInteger4) {
		this.reservedInteger4 = reservedInteger4;
	}


    /**
	 * @return the text1
	 */
    @Lob
    @Column(name = "Text1", length = 65535)
	public String getText1() {
		return text1;
	}

	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) {
		this.text1 = text1;
	}

	/**
	 * @return the text2
	 */
    @Lob
    @Column(name = "Text2", length = 65535)
	public String getText2() {
		return text2;
	}

	/**
	 * @param text2 the Text2 to set
	 */
	public void setText2(String text2) {
		this.text2 = text2;
	}

    /**
	 * @return the text3
	 */
    @Lob
    @Column(name = "Text3", length = 65535)
	public String getText3() {
		return text3;
	}

	/**
	 * @param text3 the text3 to set
	 */
	public void setText3(String text3) {
		this.text3 = text3;
	}

	/**
	 * @return the text4
	 */
    @Lob
    @Column(name = "Text4", length = 65535)
	public String getText4() {
		return text4;
	}

	/**
	 * @param text4 the text4 to set
	 */
	public void setText4(String text4) {
		this.text4 = text4;
	}

	/**
	 * @return the text5
	 */
    @Lob
    @Column(name = "Text5", length = 65535)
	public String getText5() {
		return text5;
	}

	/**
	 * @param text5 the text5 to set
	 */
	public void setText5(String text5) {
		this.text5 = text5;
	}

    /**
     * @return the text6
     */
    @Lob
    @Column(name = "Text6", length = 65535)
    public String getText6() {
        return text6;
    }

    /**
     * @param text6 the text6 to set
     */
    public void setText6(String text6) {
        this.text6 = text6;
    }

    /**
     * @return the text7
     */
    @Lob
    @Column(name = "Text7", length = 65535)
    public String getText7() {
        return text7;
    }

    /**
     * @param text7 the text7 to set
     */
    public void setText7(String text7) {
        this.text7 = text7;
    }

    /**
     * @return the text8
     */
    @Lob
    @Column(name = "Text8", length = 65535)
    public String getText8() {
        return text8;
    }

    /**
     * @param text8 the text8 to set
     */
    public void setText8(String text8) {
        this.text8 = text8;
    }

    /**
    *
    */
   //@ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
   //@org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.LOCK })

   @JoinColumn(name = "PaleoContextID", unique = false, nullable = true, insertable = true, updatable = true)
   public PaleoContext getPaleoContext()
   {
       return this.paleoContext;
   }

   public void setPaleoContext(PaleoContext paleoContext)
   {
       this.paleoContext = paleoContext;
   }

    /**
	 * @return the reservedText1
	 */
	   @Column(name = "ReservedText1", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getReservedText1() {
		return reservedText1;
	}

	/**
	 * @param reservedText1 the reservedText1 to set
	 */
	public void setReservedText1(String reservedText1) {
		this.reservedText1 = reservedText1;
	}

	/**
	 * @return the reservedText2
	 */
	   @Column(name = "ReservedText2", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getReservedText2() {
		return reservedText2;
	}

	/**
	 * @param reservedText2 the reservedText2 to set
	 */
	public void setReservedText2(String reservedText2) {
		this.reservedText2 = reservedText2;
	}

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
     *      * The method used to obtain the biological object
     */
    @Column(name = "Method", length = 50)
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      * Date which collector recorded in field book, exactly as reported by the collector.  Should indicate whether reported as range, season, month, etc.
     */
    @Column(name = "VerbatimDate", length = 50)
    public String getVerbatimDate() {
        return this.verbatimDate;
    }
    
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * The date collecting event began
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate")
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * 
     */
    @Column(name = "StartDatePrecision")
    public Byte getStartDatePrecision() {
        return this.startDatePrecision != null ? this.startDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }
    
    public void setStartDatePrecision(Byte startDatePrecision) {
        this.startDatePrecision = startDatePrecision;
    }

    /**
     * 
     */
    @Column(name = "StartDateVerbatim", length = 50)
    public String getStartDateVerbatim() {
        return this.startDateVerbatim;
    }
    
    public void setStartDateVerbatim(String startDateVerbatim) {
        this.startDateVerbatim = startDateVerbatim;
    }

    /**
     *      * The date collecting event ended
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate")
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * 
     */
    @Column(name = "EndDatePrecision")
    public Byte getEndDatePrecision() {
        return this.endDatePrecision != null ? this.endDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }
    
    public void setEndDatePrecision(Byte endDatePrecision) {
        this.endDatePrecision = endDatePrecision;
    }

    /**
     * 
     */
    @Column(name = "EndDateVerbatim", length = 50)
    public String getEndDateVerbatim() {
        return this.endDateVerbatim;
    }
    
    public void setEndDateVerbatim(String endDateVerbatim) {
        this.endDateVerbatim = endDateVerbatim;
    }

    /**
     *      * Start time in military format
     */
    @Column(name = "StartTime")
    public Short getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Short startTime) {
        this.startTime = startTime;
    }

    /**
     *      * End time in military format
     */
    @Column(name = "EndTime")
    public Short getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Short endTime) {
        this.endTime = endTime;
    }

    /**
     *      * Original statement (literal quotation) of the storage of the CollectingEvent as given by the Collector.
     */
    @Lob
    @Column(name = "VerbatimLocality", length=2048)
    public String getVerbatimLocality() {
        return this.verbatimLocality;
    }
    
    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    /**
     *      * Free text to record information that does not conform to structured fields, or to explain data recorded in those fields, particularly problematic interpretations of data given by collector(s).
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) 
    {
        this.remarks = remarks;
    }
    
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility")
    public Byte getVisibility() 
    {
        return this.visibility;
    }
    
    public void setVisibility(Byte visibility) 
    {
        this.visibility = visibility;
    }
     
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "VisibilitySetByID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getVisibilitySetBy() 
    {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(SpecifyUser visibilitySetBy) 
    {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }
    
    /**
     * 
     */
    @OneToMany(mappedBy = "collectingEvent")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     * 
     */
    @OneToMany(mappedBy = "collectingEvent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    @OrderBy("orderNumber ASC")
    public Set<Collector> getCollectors() 
    {
        return this.collectors;
    }
    
    public void setCollectors(Set<Collector> collectors) 
    {
        this.collectors = collectors;
    }

    /**
     *      * Locality where collection took place
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID")
    public Locality getLocality() 
    {
        return this.locality;
    }
    
    public void setLocality(Locality locality) 
    {
        this.locality = locality;
    }

    /**
     * @return the collectingEventAttrs
     */
    @OneToMany(mappedBy="collectingEvent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<CollectingEventAttr> getCollectingEventAttrs()
    {
        return collectingEventAttrs;
    }

    /**
     * @param collectingEventAttrs the collectingEventAttrs to set
     */
    public void setCollectingEventAttrs(Set<CollectingEventAttr> collectingEventAttrs)
    {
        this.collectingEventAttrs = collectingEventAttrs;
    }

   /**
    *
    */
   @Transient
   public Set<AttributeIFace> getAttrs() 
   {
       return new HashSet<AttributeIFace>(this.collectingEventAttrs);
   }

   public void setAttrs(Set<AttributeIFace> collectingEventAttrs) 
   {
       this.collectingEventAttrs.clear();
       for (AttributeIFace a : collectingEventAttrs)
       {
           if (a instanceof CollectingEventAttr)
           {
               this.collectingEventAttrs.add((CollectingEventAttr)a);
           }
       }
   }


    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectingTripID")
    public CollectingTrip getCollectingTrip()
    {
        return collectingTrip;
    }

    public void setCollectingTrip(CollectingTrip collectingTrip)
    {
        this.collectingTrip = collectingTrip;
    }

    @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "CollectingEventAttributeID")
    public CollectingEventAttribute getCollectingEventAttribute()
    {
        return collectingEventAttribute;
    }

    public void setCollectingEventAttribute(CollectingEventAttribute collectingEventAttribute)
    {
        this.collectingEventAttribute = collectingEventAttribute;
    }
    
    //@OneToMany(mappedBy = "collectingEvent")
    //@Cascade( {CascadeType.ALL} )
    @OneToMany(mappedBy = "collectingEvent")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<CollectingEventAttachment> getCollectingEventAttachments()
    {
        return collectingEventAttachments;
    }

    public void setCollectingEventAttachments(Set<CollectingEventAttachment> collectingEventAttachments)
    {
        this.collectingEventAttachments = collectingEventAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        if (collectingTrip != null)
        {
            return CollectingTrip.getClassTableId();
        }
        
        int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(c.CollectionObjectID)" + ceCOSQL + collectingEventId);
        if (cnt > 1)
        {
            return CollectionObject.getClassTableId();
        }
        return null;
    }


    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad() {
    	forceLoad(false);
    }
    
    public void forceLoad(boolean loadCOs)
    {
        if (collectingEventAttribute != null)
        {
            collectingEventAttribute.getId();
        }
        collectingEventAttachments.size();
        if (loadCOs) {
        	collectionObjects.size();
        }
        collectors.size();
        collectingEventAttrs.size();
        collectingEventAuthorizations.size();
    }

    public void fullForceLoad()
    {
        if (collectingEventAttribute != null)
        {
            collectingEventAttribute.getId();
        }
        collectingEventAttachments.size();
        for (CollectionObject co : collectionObjects) {
        	co.forceLoad();
        }
        collectors.size();
        collectingEventAuthorizations.size();
        collectingEventAttrs.size();
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingEvent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<CollectingEventAuthorization> getCollectingEventAuthorizations() {
        return this.collectingEventAuthorizations;
    }

    public void setCollectingEventAuthorizations(Set<CollectingEventAuthorization> collectingEventAuthorizations) {
        this.collectingEventAuthorizations = collectingEventAuthorizations;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (collectingTrip != null)
        {
            return collectingTrip.getId();
        }
        
        // Here is a non-Hibernate fix
        String postSQL = ceCOSQL + collectingEventId;
        
        int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(c.CollectionObjectID)" + postSQL);
        if (cnt == 1)
        {
            Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT c.CollectionObjectID" + postSQL);
            return (Integer)ids.get(0);
        }
        return null;
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
        return 10;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<CollectingEventAttachment> getAttachmentReferences()
    {
        return collectingEventAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CollectingEvent obj = (CollectingEvent)super.clone();
        
        obj.collectingEventId = null;
        obj.collectionObjects            = new HashSet<CollectionObject>();
        obj.collectors                   = new HashSet<Collector>();
        obj.collectingEventAttribute     = null;
        obj.collectingEventAttrs         = new HashSet<CollectingEventAttr>();
        obj.collectingEventAttachments   = new HashSet<CollectingEventAttachment>();
        obj.collectingEventAuthorizations = new HashSet<CollectingEventAuthorization>();
        obj.setGUID();

        
        for (Collector collector : collectors)
        {
            Collector newCollector = (Collector)collector.clone();
            newCollector.setCollectingEvent(obj);
            obj.collectors.add(newCollector);
        }

        for (CollectingEventAuthorization auth : collectingEventAuthorizations) {
            CollectingEventAuthorization newAuth = (CollectingEventAuthorization)auth.clone();
            newAuth.setCollectingEvent(obj);
            obj.collectingEventAuthorizations.add(newAuth);
        }

        // Clone Attributes
        obj.collectingEventAttribute    = collectingEventAttribute != null ? (CollectingEventAttribute)collectingEventAttribute.clone() : null;
        obj.collectingEventAttrs        = new HashSet<CollectingEventAttr>();
        for (CollectingEventAttr cea : collectingEventAttrs)
        {
            CollectingEventAttr newCEA = (CollectingEventAttr)cea.clone();
            obj.collectingEventAttrs.add(newCEA);
            newCEA.setCollectingEvent(obj);
        }
        
        if (paleoContext != null) {
        	Discipline d = AppContextMgr.getInstance().getClassObject(Discipline.class);
        	if (d.getIsPaleoContextEmbedded()) {
        		PaleoContext newPc = (PaleoContext)paleoContext.clone();
        		newPc.setCollectingEvents(new HashSet<CollectingEvent>());
        		newPc.getCollectingEvents().add(obj);
        		obj.paleoContext = newPc;
        	}
        }

        return obj;
    }

    @Override
    public boolean initializeClone(DataModelObjBase originalObj, boolean deep, DataProviderSessionIFace session) {
        if (deep) {
            log.error(getClass().getName() + ": initializeClone is not implemented for deep = true.");
            return false;
        }
        return true;
    }


    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CollectingEvent obj)
    {
        if (obj == null)
        {
            return 0;
        }
        
        Calendar startDateObj = obj.getStartDate();
        Date     date1        = startDate != null ? startDate.getTime() : null;
        Date     date2        = startDateObj != null ? startDateObj.getTime() : null;
        if (startDate == null || startDateObj == null)
        {
            return 0;
        }
        
        return date1.compareTo(date2);
    }
    

}
