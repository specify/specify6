package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

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
     protected Set<CollectionObject> collectionObjects;
     protected Set<Collector> collectors;
     protected Locality locality;
     protected Stratigraphy stratigraphy;
     protected Set<AttributeIFace> attrs;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public CollectingEvent() {
    }
    
    /** constructor with id */
    public CollectingEvent(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
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
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        collectionObjects = new HashSet<CollectionObject>();
        collectors = new HashSet<Collector>();
        locality = null;
        stratigraphy = null;
        attrs = new HashSet<AttributeIFace>();
        externalResources = new HashSet<ExternalResource>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getCollectingEventId() {
        return this.collectingEventId;
    }
    
    public void setCollectingEventId(Integer collectingEventId) {
        this.collectingEventId = collectingEventId;
    }

    /**
     *      * Station number or field number of the site where collecting event took place, A number or code recorded in field notes and/or written on field tags that identifies ALL material collected in a CollectingEvent.
     */
    public String getStationFieldNumber() {
        return this.stationFieldNumber;
    }
    
    public void setStationFieldNumber(String stationFieldNumber) {
        this.stationFieldNumber = stationFieldNumber;
    }

    /**
     *      * The method used to obtain the biological object
     */
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      * Date which collector recorded in field book, exactly as reported by the collector.  Should indicate whether reported as range, season, month, etc.
     */
    public String getVerbatimDate() {
        return this.verbatimDate;
    }
    
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * The date collecting event began
     */
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * 
     */
    public Short getStartDatePrecision() {
        return this.startDatePrecision;
    }
    
    public void setStartDatePrecision(Short startDatePrecision) {
        this.startDatePrecision = startDatePrecision;
    }

    /**
     * 
     */
    public String getStartDateVerbatim() {
        return this.startDateVerbatim;
    }
    
    public void setStartDateVerbatim(String startDateVerbatim) {
        this.startDateVerbatim = startDateVerbatim;
    }

    /**
     *      * The date collecting event ended
     */
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * 
     */
    public Short getEndDatePrecision() {
        return this.endDatePrecision;
    }
    
    public void setEndDatePrecision(Short endDatePrecision) {
        this.endDatePrecision = endDatePrecision;
    }

    /**
     * 
     */
    public String getEndDateVerbatim() {
        return this.endDateVerbatim;
    }
    
    public void setEndDateVerbatim(String endDateVerbatim) {
        this.endDateVerbatim = endDateVerbatim;
    }

    /**
     *      * Start time in military format
     */
    public Short getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(Short startTime) {
        this.startTime = startTime;
    }

    /**
     *      * End time in military format
     */
    public Short getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Short endTime) {
        this.endTime = endTime;
    }

    /**
     *      * Original statement (literal quotation) of the location of the CollectingEvent as given by the Collector.
     */
    public String getVerbatimLocality() {
        return this.verbatimLocality;
    }
    
    public void setVerbatimLocality(String verbatimLocality) {
        this.verbatimLocality = verbatimLocality;
    }

    /**
     *      * The name of the group that this record is visible to.
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      * Free text to record information that does not conform to structured fields, or to explain data recorded in those fields, particularly problematic interpretations of data given by collector(s).
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     * 
     */
    public Set<Collector> getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set<Collector> collectors) {
        this.collectors = collectors;
    }

    /**
     *      * Locality where collection took place
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
    public Stratigraphy getStratigraphy() {
        return this.stratigraphy;
    }
    
    public void setStratigraphy(Stratigraphy stratigraphy) {
        this.stratigraphy = stratigraphy;
    }

    /**
     * 
     */
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    /**
     * 
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }




    // Add Methods

    public void addCollectionObject(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
    }

    public void addCollector(final Collector collector)
    {
        this.collectors.add(collector);
    }

    public void addAttr(final AttributeIFace attr)
    {
        this.attrs.add(attr);
    }

    public void addExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
    }

    // Done Add Methods
}
