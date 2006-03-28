package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="collectingevent"
 *     
 */
public class CollectingEvent  implements java.io.Serializable {

    // Fields    

     protected Integer collectingEventId;
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
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Integer methodId;
     protected Set collectionObjects;
     private Set collectors;
     protected Locality locality;
     protected Stratigraphy stratigraphy;
     protected Set attrs;
     private Set externalResources;


    // Constructors

    /** default constructor */
    public CollectingEvent() {
    }
    
    /** constructor with id */
    public CollectingEvent(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="CollectingEventID"
     *         
     */
    public Integer getCollectingEventId() {
        return this.collectingEventId;
    }
    
    public void setCollectingEventId(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }

    /**
     *      *            @hibernate.property
     *             column="StationFieldNumber"
     *             length="50"
     *         
     */
    public String getStationFieldNumber() {
        return this.stationFieldNumber;
    }
    
    public void setStationFieldNumber(String stationFieldNumber) {
        this.stationFieldNumber = stationFieldNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Method"
     *             length="50"
     *         
     */
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      *            @hibernate.property
     *             column="VerbatimDate"
     *             length="50"
     *         
     */
    public String getVerbatimDate() {
        return this.verbatimDate;
    }
    
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      *            @hibernate.property
     *             column="StartDate"
     *         
     */
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *      *            @hibernate.property
     *             column="StartDatePrecision"
     *         
     */
    public Short getStartDatePrecision() {
        return this.startDatePrecision;
    }
    
    public void setStartDatePrecision(Short startDatePrecision) {
        this.startDatePrecision = startDatePrecision;
    }

    /**
     *      *            @hibernate.property
     *             column="StartDateVerbatim"
     *             length="50"
     *         
     */
    public String getStartDateVerbatim() {
        return this.startDateVerbatim;
    }
    
    public void setStartDateVerbatim(String startDateVerbatim) {
        this.startDateVerbatim = startDateVerbatim;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDate"
     *         
     */
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDatePrecision"
     *         
     */
    public Short getEndDatePrecision() {
        return this.endDatePrecision;
    }
    
    public void setEndDatePrecision(Short endDatePrecision) {
        this.endDatePrecision = endDatePrecision;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDateVerbatim"
     *             length="50"
     *         
     */
    public String getEndDateVerbatim() {
        return this.endDateVerbatim;
    }
    
    public void setEndDateVerbatim(String endDateVerbatim) {
        this.endDateVerbatim = endDateVerbatim;
    }

    /**
     *      *            @hibernate.property
     *             column="StartTime"
     *         
     */
    public Short getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Short startTime) {
        this.startTime = startTime;
    }

    /**
     *      *            @hibernate.property
     *             column="EndTime"
     *         
     */
    public Short getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Short endTime) {
        this.endTime = endTime;
    }

    /**
     *      *            @hibernate.property
     *             column="VerbatimLocality"
     *         
     */
    public String getVerbatimLocality() {
        return this.verbatimLocality;
    }
    
    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    /**
     *      *            @hibernate.property
     *             column="GroupPermittedToView"
     *             length="10"
     *         
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *             not-null="true"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *             update="false"
     *             not-null="true"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.property
     *             column="MethodID"
     *             length="10"
     *         
     */
    public Integer getMethodId() {
        return this.methodId;
    }
    
    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectingEventID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *         
     */
    public Set getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectingEventID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Collector"
     *         
     */
    public Set getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set collectors) {
        this.collectors = collectors;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="LocalityID"         
     *         
     */
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    /**
     *      *            @hibernate.one-to-one
     *            @hibernate.column name="LocalityID"         
     *         
     */
    public Stratigraphy getStratigraphy() {
        return this.stratigraphy;
    }
    
    public void setStratigraphy(Stratigraphy stratigraphy) {
        this.stratigraphy = stratigraphy;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectingEventAttrID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectingEventAttr"
     *         
     */
    public Set getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set attrs) {
        this.attrs = attrs;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




}