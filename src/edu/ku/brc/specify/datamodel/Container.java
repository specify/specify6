package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="container"
 *     
 */
public class Container  implements java.io.Serializable {

    // Fields    

     protected Integer containerId;
     protected Integer collectionObjectId;
     protected Short type;
     protected String name;
     protected String description;
     protected Integer number;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     protected Set items;
     protected CollectionObject container;
     private Location location;


    // Constructors

    /** default constructor */
    public Container() {
    }
    
    /** constructor with id */
    public Container(Integer containerId) {
        this.containerId = containerId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="ContainerID"
     *         
     */
    public Integer getContainerId() {
        return this.containerId;
    }
    
    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    /**
     *      *            @hibernate.property
     *             type="int"
     *             column="CollectionObjectID"
     *             not-null="false"
     *         
     */
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }
    
    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *      *            @hibernate.property
     *             column="Type"
     *         
     */
    public Short getType() {
        return this.type;
    }
    
    public void setType(Short type) {
        this.type = type;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="64"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="255"
     *         
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      *            @hibernate.property
     *             column="Number"
     *         
     */
    public Integer getNumber() {
        return this.number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ContainerItemsID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ContainerItem"
     *         
     */
    public Set getItems() {
        return this.items;
    }
    
    public void setItems(Set items) {
        this.items = items;
    }

    /**
     *      *            @hibernate.many-to-one
     * 	        not-null="false"
     * 			unique="true" 
     * 			insert="false" 
     * 			update="false"
     * 			cascade="all"
     *            @hibernate.column name="CollectionObjectID"
     *         
     */
    public CollectionObject getContainer() {
        return this.container;
    }
    
    public void setContainer(CollectionObject container) {
        this.container = container;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="LocationID"         
     *         
     */
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }




}