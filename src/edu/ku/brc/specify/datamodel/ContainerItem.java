package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="containeritem"
 *     
 */
public class ContainerItem  implements java.io.Serializable {

    // Fields    

     protected Integer ContainerItemId;
     private Date timestampModified;
     private Date timestampCreated;
     protected Container container;
     protected Set collectionObjects;


    // Constructors

    /** default constructor */
    public ContainerItem() {
    }
    
    /** constructor with id */
    public ContainerItem(Integer ContainerItemId) {
        this.ContainerItemId = ContainerItemId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ContainerItemID"
     *         
     */
    public Integer getContainerItemId() {
        return this.ContainerItemId;
    }
    
    public void setContainerItemId(Integer ContainerItemId) {
        this.ContainerItemId = ContainerItemId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ContainerID"         
     *         
     */
    public Container getContainer() {
        return this.container;
    }
    
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ContainerID"
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




}