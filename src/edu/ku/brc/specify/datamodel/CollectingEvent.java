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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.AttributeProviderIFace;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectingevent")
@org.hibernate.annotations.Table(appliesTo="collectingevent", indexes =
    {   @Index (name="StationFieldNumberIDX", columnNames={"StationFieldNumber"}),
        @Index (name="StartDateIDX", columnNames={"StartDate"}),
        @Index (name="EndDateIDX", columnNames={"EndDate"})
    })
public class CollectingEvent extends DataModelObjBase implements AttachmentOwnerIFace<CollectingEventAttachment>, AttributeProviderIFace, java.io.Serializable, Comparable<CollectingEvent> {

    // Fields    

    protected Integer               collectingEventId;
    protected String                stationFieldNumber;
    protected String                method;
    protected String                verbatimDate;
    protected Calendar              startDate;
    protected Short                 startDatePrecision; // Accurate to Year, Month, Day
    protected String                startDateVerbatim;
    protected Calendar              endDate;
    protected Short                 endDatePrecision;   // Accurate to Year, Month, Day
    protected String                endDateVerbatim;
    protected Short                 startTime;
    protected Short                 endTime;
    protected String                verbatimLocality;
    protected Integer               groupPermittedToView;
    protected String                remarks;
    protected Integer               visibility;
    protected String                visibilitySetBy;
    protected Set<CollectionObject> collectionObjects;
    protected Set<Collector>        collectors;
    protected Locality              locality;
    protected CollectingTrip        collectingTrip;
    
    protected HabitatAttributes         habitatAttributes;      // Specify 5 Attributes table
    protected Set<CollectingEventAttr>  collectingEventAttrs; // Generic Expandable Attributes
    protected Set<CollectingEventAttachment> collectingEventAttachments;



    // Constructors

    /** default constructor */
    public CollectingEvent()
    {
    	// do nothing
    }
    
    /** constructor with id */
    public CollectingEvent(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectingEventId = null;
        stationFieldNumber = null;
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
        groupPermittedToView = null;
        remarks = null;
        visibility = null;
        collectionObjects = new HashSet<CollectionObject>();
        collectors = new HashSet<Collector>();
        locality = null;
        
        habitatAttributes    = null;
        collectingEventAttrs = new HashSet<CollectingEventAttr>();
        collectingEventAttachments = new HashSet<CollectingEventAttachment>();
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
     *      * Station number or field number of the site where collecting event took place, A number or code recorded in field notes and/or written on field tags that identifies ALL material collected in a CollectingEvent.
     */
    @Column(name = "StationFieldNumber", length = 50)
    public String getStationFieldNumber() {
        return this.stationFieldNumber;
    }
    
    public void setStationFieldNumber(String stationFieldNumber) {
        this.stationFieldNumber = stationFieldNumber;
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
    public Short getStartDatePrecision() {
        return this.startDatePrecision;
    }
    
    public void setStartDatePrecision(Short startDatePrecision) {
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
    public Short getEndDatePrecision() {
        return this.endDatePrecision;
    }
    
    public void setEndDatePrecision(Short endDatePrecision) {
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
     *      * Original statement (literal quotation) of the location of the CollectingEvent as given by the Collector.
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
     *      * The name of the group that this record is visible to.
     */
    @Column(name = "GroupPermittedToView")
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      * Free text to record information that does not conform to structured fields, or to explain data recorded in those fields, particularly problematic interpretations of data given by collector(s).
     */
    @Lob
    @Column(name="Remarks")
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    
    
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility")
    public Integer getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
     
    /**
     * 
     */
    @Column(name = "VisibilitySetBy", length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
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
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Collector> getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set<Collector> collectors) {
        this.collectors = collectors;
    }

    /**
     *      * Locality where collection took place
     */
    @ManyToOne
    @JoinColumn(name = "LocalityID")
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
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


    @ManyToOne
    @JoinColumn(name = "CollectingTripID")
    public CollectingTrip getCollectingTrip()
    {
        return collectingTrip;
    }

    public void setCollectingTrip(CollectingTrip collectingTrip)
    {
        this.collectingTrip = collectingTrip;
    }

    @ManyToOne
    @JoinColumn(name = "HabitatAttributesID")
    public HabitatAttributes getHabitatAttributes()
    {
        return habitatAttributes;
    }

    public void setHabitatAttributes(HabitatAttributes habitatAttributes)
    {
        this.habitatAttributes = habitatAttributes;
    }
    
    @OneToMany(mappedBy = "collectingEvent")
    @Cascade( {CascadeType.ALL} )
    public Set<CollectingEventAttachment> getCollectingEventAttachments()
    {
        return collectingEventAttachments;
    }

    public void setCollectingEventAttachments(Set<CollectingEventAttachment> collectingEventAttachments)
    {
        this.collectingEventAttachments = collectingEventAttachments;
    }

    // Comparable
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
        return 10;
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return DataObjFieldFormatMgr.format(this, getClass());
    }

    @Transient
    public Set<CollectingEventAttachment> getAttachmentReferences()
    {
        return collectingEventAttachments;
    }
}
