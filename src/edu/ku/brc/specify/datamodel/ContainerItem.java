package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class ContainerItem  implements java.io.Serializable {

    // Fields    

     protected Integer containerItemId;
     private Date timestampModified;
     private Date timestampCreated;
     protected Container container;
     protected Set<CollectionObject> collectionObjects;


    // Constructors

    /** default constructor */
    public ContainerItem() {
    }
    
    /** constructor with id */
    public ContainerItem(Integer containerItemId) {
        this.containerItemId = containerItemId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        containerItemId = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        container = null;
        collectionObjects = new HashSet<CollectionObject>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getContainerItemId() {
        return this.containerItemId;
    }
    
    public void setContainerItemId(Integer containerItemId) {
        this.containerItemId = containerItemId;
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
    public Container getContainer() {
        return this.container;
    }
    
    public void setContainer(Container container) {
        this.container = container;
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




    // Add Methods

    public void addCollectionObject(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
    }

    // Done Add Methods
}
