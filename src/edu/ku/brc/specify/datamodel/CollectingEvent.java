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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "collectingevent")
public class CollectingEvent extends DataModelObjBase implements java.io.Serializable, Comparable<CollectingEvent> {

    // Fields    

     protected Long collectingEventId;
     protected String stationFieldNumber;
     protected String method;
     protected String verbatimDate;
     protected Calendar startDate;
     protected Short startDatePrecision;
     protected String startDateVerbatim;
     protected Calendar endDate;
     protected Short endDatePrecision;
     protected String endDateVerbatim;
     protected Short startTime;
     protected Short endTime;
     protected String verbatimLocality;
     protected Integer groupPermittedToView;
     protected String remarks;
     protected Integer visibility;
     protected String visibilitySetBy;
     protected Set<CollectionObject> collectionObjects;
     protected Set<Collector> collectors;
     protected Locality locality;
     protected Stratigraphy stratigraphy;
     protected CollectingTrip collectingTrip;
     protected Set<AttributeIFace> attrs;
     protected Set<Attachment> attachments;


    // Constructors

    /** default constructor */
    public CollectingEvent()
    {
    	// do nothing
    }
    
    /** constructor with id */
    public CollectingEvent(Long collectingEventId) {
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
        stratigraphy = null;
        attrs = new HashSet<AttributeIFace>();
        attachments = new HashSet<Attachment>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectingEventID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getCollectingEventId() {
        return this.collectingEventId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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
    
    public void setCollectingEventId(Long collectingEventId) {
        this.collectingEventId = collectingEventId;
    }

    /**
     *      * Station number or field number of the site where collecting event took place, A number or code recorded in field notes and/or written on field tags that identifies ALL material collected in a CollectingEvent.
     */
    @Column(name = "StationFieldNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getStationFieldNumber() {
        return this.stationFieldNumber;
    }
    
    public void setStationFieldNumber(String stationFieldNumber) {
        this.stationFieldNumber = stationFieldNumber;
    }

    /**
     *      * The method used to obtain the biological object
     */
    @Column(name = "Method", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      * Date which collector recorded in field book, exactly as reported by the collector.  Should indicate whether reported as range, season, month, etc.
     */
    @Column(name = "VerbatimDate", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
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
    @Column(name = "StartDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getStartDatePrecision() {
        return this.startDatePrecision;
    }
    
    public void setStartDatePrecision(Short startDatePrecision) {
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
     *      * The date collecting event ended
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
    @Column(name = "EndDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getEndDatePrecision() {
        return this.endDatePrecision;
    }
    
    public void setEndDatePrecision(Short endDatePrecision) {
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
     *      * Start time in military format
     */
    @Column(name = "StartTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Short startTime) {
        this.startTime = startTime;
    }

    /**
     *      * End time in military format
     */
    @Column(name = "EndTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Short endTime) {
        this.endTime = endTime;
    }

    /**
     *      * Original statement (literal quotation) of the location of the CollectingEvent as given by the Collector.
     */
    @Column(name = "VerbatimLocality", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getVerbatimLocality() {
        return this.verbatimLocality;
    }
    
    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    /**
     *      * The name of the group that this record is visible to.
     */
    @Column(name = "GroupPermittedToView", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      * Free text to record information that does not conform to structured fields, or to explain data recorded in those fields, particularly problematic interpretations of data given by collector(s).
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
     
    /**
     * 
     */
    @Column(name = "VisibilitySetBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingEvent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingEvent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Collector> getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set<Collector> collectors) {
        this.collectors = collectors;
    }

    /**
     *      * Locality where collection took place
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "LocalityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    /**
     * 
     */
    @ManyToOne(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "StratigraphyID", unique = false, nullable = true, insertable = true, updatable = true)
    public Stratigraphy getStratigraphy() {
        return this.stratigraphy;
    }
    
    public void setStratigraphy(Stratigraphy stratigraphy) {
        this.stratigraphy = stratigraphy;
    }

    /**
     * 
     */
    @OneToMany(targetEntity=edu.ku.brc.specify.datamodel.CollectingEventAttr.class,
            cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "CollectingTripID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectingTrip getCollectingTrip()
    {
        return collectingTrip;
    }

    public void setCollectingTrip(CollectingTrip collectingTrip)
    {
        this.collectingTrip = collectingTrip;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectingEvent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments)
	{
		this.attachments = attachments;
	}

    // Add Methods

	public void addCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
        collectionObject.setCollectingEvent(this);
    }

    public void addCollector(final Collector collector)
    {
        this.collectors.add(collector);
        collector.setCollectingEvent(this);
    }

    public void addAttrs(final CollectingEventAttr attr)
    {
        this.attrs.add(attr);
        attr.setCollectingEvent(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.remove(collectionObject);
        collectionObject.setCollectingEvent(null);
    }

    public void removeCollector(final Collector collector)
    {
        this.collectors.remove(collector);
        collector.setCollectingEvent(null);
    }

    public void removeAttrs(final CollectingEventAttr attr)
    {
        this.attrs.remove(attr);
        attr.setCollectingEvent(null);
    }

    // Delete Add Methods
    
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
    public Integer getTableId()
    {
        return 10;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if( ref instanceof Attachment )
        {
            Attachment a = (Attachment)ref;
            attachments.add(a);
            a.setCollectingEvent(this);
            return;
        }
        super.addReference(ref, refType);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace refObj, String refType)
    {
        if( refObj instanceof Attachment )
        {
            attachments.remove(refObj);
            return;
        }
        super.removeReference(refObj, refType);
    }
}
