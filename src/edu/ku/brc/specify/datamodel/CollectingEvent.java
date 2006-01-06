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
     protected Integer startDate;
     protected Integer endDate;
     protected Short startTime;
     protected Short endTime;
     protected String verbatimLocality;
     protected Integer groupPermittedToView;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Integer methodId;
     private CollectionObjectType collectionObjectType;
     protected Habitat habitat;
     private Stratigraphy stratigraphy;
     protected Set collectionObjects;
     private Set collectors;
     protected Locality locality;
     protected Accession accession;


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
     *             generator-class="assigned"
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
     *             length="10"
     *         
     */
    public Integer getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDate"
     *             length="10"
     *         
     */
    public Integer getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    /**
     *      *            @hibernate.property
     *             column="StartTime"
     *             length="5"
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
     *             length="5"
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
     *             length="1073741823"
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
     *             length="1073741823"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectTypeCollectedID"         
     *         
     */
    public CollectionObjectType getCollectionObjectType() {
        return this.collectionObjectType;
    }
    
    public void setCollectionObjectType(CollectionObjectType collectionObjectType) {
        this.collectionObjectType = collectionObjectType;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     * 			cascade="delete"
     *         
     */
    public Habitat getHabitat() {
        return this.habitat;
    }
    
    public void setHabitat(Habitat habitat) {
        this.habitat = habitat;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     * 			cascade="delete"
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
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectingEventID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObj"
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
     * 
     */
    public Accession getAccession() {
        return this.accession;
    }
    
    public void setAccession(Accession accession) {
        this.accession = accession;
    }




}