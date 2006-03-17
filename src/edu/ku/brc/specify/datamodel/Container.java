package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="container"
 *     
 */
public class Container  implements java.io.Serializable {

    // Fields    

     protected Integer containerId;
     protected Short type;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     protected Set items;
     protected Location location;


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
     *             generator-class="assigned"
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
     *      *            @hibernate.one-to-one
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