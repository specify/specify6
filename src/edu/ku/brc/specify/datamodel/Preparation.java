package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Preparation  implements java.io.Serializable {

    // Fields    

     protected Integer preparationId;
     protected String text1;
     protected String text2;
     protected Integer count;
     protected String storageLocation;
     protected String remarks;
     protected Calendar preparedDate;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Set<LoanPhysicalObject> loanPhysicalObjects;
     protected Set<AttributeIFace> attrs;
     private PrepType prepType;
     private CollectionObject collectionObject;
     private Agent preparedByAgent;
     private Location location;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public Preparation() {
    }
    
    /** constructor with id */
    public Preparation(Integer preparationId) {
        this.preparationId = preparationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        preparationId = null;
        text1 = null;
        text2 = null;
        count = null;
        storageLocation = null;
        remarks = null;
        preparedDate = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
        loanPhysicalObjects = new HashSet<LoanPhysicalObject>();
        attrs = new HashSet<AttributeIFace>();
        prepType = null;
        collectionObject = null;
        preparedByAgent = null;
        location = null;
        externalResources = new HashSet<ExternalResource>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getPreparationId() {
        return this.preparationId;
    }
    
    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * The number of objects (specimens, slides, pieces) prepared
     */
    public Integer getCount() {
        return this.count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 
     */
    public String getStorageLocation() {
        return this.storageLocation;
    }
    
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * 
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
    public Calendar getPreparedDate() {
        return this.preparedDate;
    }
    
    public void setPreparedDate(Calendar preparedDate) {
        this.preparedDate = preparedDate;
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
    public Set<LoanPhysicalObject> getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set<LoanPhysicalObject> loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
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
    public PrepType getPrepType() {
        return this.prepType;
    }
    
    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     * 
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    public Agent getPreparedByAgent() {
        return this.preparedByAgent;
    }
    
    public void setPreparedByAgent(Agent preparedByAgent) {
        this.preparedByAgent = preparedByAgent;
    }

    /**
     * 
     */
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
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

    public void addLoanPhysicalObject(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.add(loanPhysicalObject);
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
