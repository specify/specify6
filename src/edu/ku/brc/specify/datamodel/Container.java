package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

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
     protected Set<ContainerItem> items;
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




    // Initializer
    public void initialize()
    {
        containerId = null;
        collectionObjectId = null;
        type = null;
        name = null;
        description = null;
        number = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        items = new HashSet<ContainerItem>();
        container = null;
        location = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Integer getContainerId() {
        return this.containerId;
    }

    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    /**
     *
     */
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }

    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *
     */
    public Short getType() {
        return this.type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     */
    public Integer getNumber() {
        return this.number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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
    public Set<ContainerItem> getItems() {
        return this.items;
    }

    public void setItems(Set<ContainerItem> items) {
        this.items = items;
    }

    /**
     *
     */
    public CollectionObject getContainer() {
        return this.container;
    }

    public void setContainer(CollectionObject container) {
        this.container = container;
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

    // Add Methods

    public void addItem(final ContainerItem item)
    {
        this.items.add(item);
    }

    // Done Add Methods
}
