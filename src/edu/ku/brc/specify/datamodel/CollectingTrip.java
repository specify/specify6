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

import javax.persistence.*;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.AttributeProviderIFace;

/**
 * CollectingTrip generated by hbm2java
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectingtrip")
@org.hibernate.annotations.Table(appliesTo="collectingtrip", indexes =
    {   
        @Index (name="COLTRPNameIDX", columnNames={"CollectingTripName"}),
        @Index (name="COLTRPStartDateIDX", columnNames={"StartDate"})
    })
@SuppressWarnings("serial")
public class CollectingTrip extends DisciplineMember implements java.io.Serializable, AttachmentOwnerIFace<CollectingTripAttachment>,
       Comparable<CollectingTrip>,
        Cloneable
{
    protected static final Logger log = Logger.getLogger(CollectingTrip.class);

     // Fields
    protected Integer               collectingTripId;
    protected String                remarks;
    protected Calendar              startDate;
    protected Byte                  startDatePrecision; // Accurate to Year, Month, Day
    protected String                startDateVerbatim;
    protected Calendar              endDate;
    protected Byte                  endDatePrecision;   // Accurate to Year, Month, Day
    protected String                endDateVerbatim;
    protected Short                 startTime;          // Minutes in 24 hours
    protected Short                 endTime;            // Minutes in 24 hours
    protected String                collectingTripName;
    protected Calendar                    date1;
    protected Byte                        date1Precision;
    protected Calendar                    date2;
    protected Byte                        date2Precision;
    protected String                sponsor;
    protected String                vessel;
    protected String                cruise;
    protected String                expedition;
    protected Agent agent1;
    protected Agent agent2;

    protected String                text1;
    protected String                text2;
    protected String                text3;
    protected String                text4;
    protected String text5;
    protected String text6;
    protected String text7;
    protected String text8;
    protected String text9;
    protected Integer               number1;
    protected Integer               number2;
    protected Boolean               yesNo1;
    protected Boolean               yesNo2;

    protected CollectingTripAttribute collectingTripAttribute;
    protected Set<CollectingEvent>  collectingEvents;
    protected Set<CollectingTripAuthorization> collectingTripAuthorizations;
    protected Set<CollectingTripAttachment> collectingTripAttachments;
    protected Set<FundingAgent>     fundingAgents;

    // Constructors

    /** default constructor */
    public CollectingTrip()
    {
        // do nothing
    }
    
    /** constructor with id */
    public CollectingTrip(Integer collectingTripId) {
        this.collectingTripId = collectingTripId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectingTripId   = null;
        remarks            = null;
        startDate          = null;
        startDatePrecision = null;
        startDateVerbatim  = null;
        endDate            = null;
        endDatePrecision   = null;
        endDateVerbatim    = null;
        startTime          = null;
        endTime            = null;
        collectingTripName = null;
        sponsor            = null;
        vessel             = null;
        cruise             = null;
        expedition         = null;
        date1 = null;
        date1Precision = 1;
        date2 = null;
        date2Precision = 1;
        agent1 = null;
        agent2 = null;
        
        text1   = null;
        text2   = null;
        text3   = null;
        text4   = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        text9 = null;
        number1 = null;
        number2 = null;
        yesNo1  = null;
        yesNo2  = null;

        collectingTripAttribute = null;

        fundingAgents = new HashSet<FundingAgent>();
        collectingEvents = new HashSet<CollectingEvent>();
        collectingTripAttachments = new HashSet<CollectingTripAttachment>();
        collectingTripAuthorizations = new HashSet<CollectingTripAuthorization>();
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectingTripID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectingTripId() {
        return this.collectingTripId;
    }
    
    public void setCollectingTripId(Integer collectingTripId) {
        this.collectingTripId = collectingTripId;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return collectingTripId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectingTrip.class;
    }

    @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "CollectingTripAttributeID")
    public CollectingTripAttribute getCollectingTripAttribute()
    {
        return collectingTripAttribute;
    }

    public void setCollectingTripAttribute(CollectingTripAttribute collectingTripAttribute)
    {
        this.collectingTripAttribute = collectingTripAttribute;
    }

    //@OneToMany(mappedBy = "collectingTrip")
    //@Cascade( {CascadeType.ALL} )
    @OneToMany(mappedBy = "collectingTrip")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<CollectingTripAttachment> getCollectingTripAttachments()
    {
        return collectingTripAttachments;
    }

    public void setCollectingTripAttachments(Set<CollectingTripAttachment> collectingTripAttachments)
    {
        this.collectingTripAttachments = collectingTripAttachments;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }

    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent2ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent2() {
        return agent2;
    }

    public void setAgent2(Agent agent2) {
        this.agent2 = agent2;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    /**
     *
     * @param date1
     */
    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date1Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate1Precision() {
        return date1Precision;
    }

    /**
     *
     * @param date1Precision
     */
    public void setDate1Precision(Byte date1Precision) {
        this.date1Precision = date1Precision;
    }
    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    /**
     *
     * @param date2
     */
    public void setDate2(Calendar date2) {
        date2 = date2;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date2Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate2Precision() {
        return date2Precision;
    }

    /**
     *
     * @param date2Precision
     */
    public void setDate2Precision(Byte date2Precision) {
        date2Precision = date2Precision;
    }

    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }

    @Lob
    @Column(name = "Text6", length = 65535)
    public String getText6() {
        return text6;
    }

    public void setText6(String text6) {
        this.text6 = text6;
    }

    @Lob
    @Column(name = "Text7", length = 65535)
    public String getText7() {
        return text7;
    }

    public void setText7(String text7) {
        this.text7 = text7;
    }

    @Lob
    @Column(name = "Text8", length = 65535)
    public String getText8() {
        return text8;
    }

    public void setText8(String text8) {
        this.text8 = text8;
    }

    @Lob
    @Column(name = "Text9", length = 65535)
    public String getText9() {
        return text9;
    }

    public void setText9(String text9) {
        this.text9 = text9;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingTrip")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<CollectingTripAuthorization> getCollectingTripAuthorizations() {
        return this.collectingTripAuthorizations;
    }

    public void setCollectingTripAuthorizations(Set<CollectingTripAuthorization> collectingTripAuthorizations) {
        this.collectingTripAuthorizations = collectingTripAuthorizations;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "StartDateVerbatim", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getStartDateVerbatim() {
        return this.startDateVerbatim;
    }
    
    public void setStartDateVerbatim(String startDateVerbatim) {
        this.startDateVerbatim = startDateVerbatim;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "EndDateVerbatim", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getEndDateVerbatim() {
        return this.endDateVerbatim;
    }
    
    public void setEndDateVerbatim(String endDateVerbatim) {
        this.endDateVerbatim = endDateVerbatim;
    }

    /**
     * 
     */
    @Column(name = "StartTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Short startTime) {
        this.startTime = startTime;
    }

    /**
     * 
     */
    @Column(name = "EndTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Short endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the collectingTripName
     */
    @Column(name = "CollectingTripName", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
    public String getCollectingTripName()
    {
        return collectingTripName;
    }

    /**
     * @param name the collectingTripName to set
     */
    public void setCollectingTripName(String collectingTripName)
    {
        this.collectingTripName = collectingTripName;
    }

    /**
     * @return the sponsor
     */
    @Column(name = "Sponsor", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSponsor()
    {
        return sponsor;
    }

    /**
     * @param sponsor the sponsor to set
     */
    public void setSponsor(String sponsor)
    {
        this.sponsor = sponsor;
    }

    
    /**
	 * @return the vessel
	 */
    @Column(name = "Vessel", unique = false, nullable = true, insertable = true, updatable = true, length = 250)
	public String getVessel() {
		return vessel;
	}

	/**
	 * @param vessel the vessel to set
	 */
	public void setVessel(String vessel) {
		this.vessel = vessel;
	}

	/**
	 * @return the cruise
	 */
    @Column(name = "Cruise", unique = false, nullable = true, insertable = true, updatable = true, length = 250)
	public String getCruise() {
		return cruise;
	}

	/**
	 * @param cruise the cruise to set
	 */
	public void setCruise(String cruise) {
		this.cruise = cruise;
	}

	/**
	 * @return the expedition
	 */
    @Column(name = "Expedition", unique = false, nullable = true, insertable = true, updatable = true, length = 250)
	public String getExpedition() {
		return expedition;
	}

	/**
	 * @param expedition the expedition to set
	 */
	public void setExpedition(String expedition) {
		this.expedition = expedition;
	}

	/**
     * @return the text1
     */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @return the text2
     */
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     * @return the text3
     */
    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getText3()
    {
        return text3;
    }

    /**
     * @param text3 the text3 to set
     */
    public void setText3(String text3)
    {
        this.text3 = text3;
    }

    /**
     * @return the text4
     */
    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getText4()
    {
        return text4;
    }

    /**
     * @param text4 the text4 to set
     */
    public void setText4(String text4)
    {
        this.text4 = text4;
    }

    /**
     * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber1() {
        return this.number1;
    }

    public void setNumber1(Integer number1) {
        this.number1 = number1;
    }

    /**
     * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber2() {
        return this.number2;
    }

    public void setNumber2(Integer number2) {
        this.number2 = number2;
    }

    /**
     * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }
    
    /**
     * 
     */
    @OneToMany(mappedBy = "collectingTrip")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    @OrderBy("orderNumber ASC")
    public Set<FundingAgent> getFundingAgents() 
    {
        return this.fundingAgents;
    }
    
    public void setFundingAgents(Set<FundingAgent> fundingAgents) 
    {
        this.fundingAgents = fundingAgents;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingTrip")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectingEvent> getCollectingEvents() 
    {
        return this.collectingEvents;
    }
    
    public void setCollectingEvents(Set<CollectingEvent> collectingEvents) 
    {
        this.collectingEvents = collectingEvents;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        CollectingTrip obj = (CollectingTrip)super.clone();
        initialize();
        obj.collectingTripId = null;

        for (FundingAgent fa : fundingAgents) {
            FundingAgent newFa = (FundingAgent)fa.clone();
            newFa.setCollectingTrip(obj);
            obj.fundingAgents.add(newFa);
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<CollectingTripAttachment> getAttachmentReferences()
    {
        return collectingTripAttachments;
    }

    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CollectingTrip obj)
    {
        if (obj == null)
        {
            return 0;
        }

        Calendar startDateObj = obj.getStartDate();
        Date date1        = startDate != null ? startDate.getTime() : null;
        Date     date2        = startDateObj != null ? startDateObj.getTime() : null;
        if (startDate == null || startDateObj == null)
        {
            return 0;
        }

        return date1.compareTo(date2);
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 87;
    }

}
