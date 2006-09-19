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

import edu.ku.brc.dbsupport.AttributeIFace;




/**

 */
public class CollectingEvent  implements java.io.Serializable, Comparable<CollectingEvent> {

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
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Set<CollectionObject> collectionObjects;
     protected Set<Collectors> collectors;
     protected Locality locality;
     protected Stratigraphy stratigraphy;
     protected Set<AttributeIFace> attrs;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public CollectingEvent() {
    }
    
    /** constructor with id */
    public CollectingEvent(Long collectingEventId) {
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
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObjects = new HashSet<CollectionObject>();
        collectors = new HashSet<Collectors>();
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
    public Long getCollectingEventId() {
        return this.collectingEventId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.collectingEventId;
    }
    
    public void setCollectingEventId(Long collectingEventId) {
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
     *      * Original statement (literal quotation) of the location of the CollectingEvent as given by the Collectors.
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
    public Set<Collectors> getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set<Collectors> collectors) {
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

    public void addCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
        collectionObject.setCollectingEvent(this);
    }

    public void addCollectors(final Collectors collector)
    {
        this.collectors.add(collector);
        collector.setCollectingEvent(this);
    }

    public void addAttrs(final CollectingEventAttr attr)
    {
        this.attrs.add(attr);
        attr.setCollectingEvent(this);
    }

    public void addExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
        externalResource.getCollectinEvents().add(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.remove(collectionObject);
        collectionObject.setCollectingEvent(null);
    }

    public void removeCollectors(final Collectors collector)
    {
        this.collectors.remove(collector);
        collector.setCollectingEvent(null);
    }

    public void removeAttrs(final CollectingEventAttr attr)
    {
        this.attrs.remove(attr);
        attr.setCollectingEvent(null);
    }

    public void removeExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.remove(externalResource);
        externalResource.getCollectinEvents().remove(null);
    }

    // Delete Add Methods
    
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
