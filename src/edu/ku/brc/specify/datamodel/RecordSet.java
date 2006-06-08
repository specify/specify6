package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**
 * RecordSet generated by hbm2java
 */
@SuppressWarnings("serial")
public class RecordSet  implements java.io.Serializable {

    // Fields

     protected Long recordSetID;
     protected String name;
     protected Integer tableId;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Set<RecordSetItem> items;
     protected SpecifyUser owner;


    // Constructors

    /** default constructor */
    public RecordSet() 
    {
    }

    /** constructor with id */
    public RecordSet(Long recordSetID) 
    {
        this.recordSetID = recordSetID;
    }

    /** constructor with id */
    public RecordSet(final String name, final int tableId) 
    {
        this.name = name;
        this.tableId = tableId;
    }

    // Initializer
    public void initialize()
    {
        recordSetID = null;
        //name = null;
        //tableId = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        items = new HashSet<RecordSetItem>();
        owner = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Long getRecordSetID() {
        return this.recordSetID;
    }

    public void setRecordSetID(Long recordSetID) {
        this.recordSetID = recordSetID;
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
    public Integer getTableId() {
        return this.tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
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
    public Set<RecordSetItem> getItems() {
        return this.items;
    }

    public void setItems(Set<RecordSetItem> items) {
        this.items = items;
    }

    /**
     *
     */
    public SpecifyUser getOwner() {
        return this.owner;
    }

    public void setOwner(SpecifyUser owner) {
        this.owner = owner;
    }





    // Add Methods

    public void addItem(final int recordId)
    {
        this.items.add(new RecordSetItem(recordId));
    }

    public void addItem(final String recordId)
    {
    	this.items.add(new RecordSetItem(recordId));
    }

    public void addItems(final RecordSetItem item)
    {
        this.items.add(item);
    }

    // Done Add Methods

    // Delete Methods

    public void removeItems(final RecordSetItem item)
    {
        this.items.remove(item);
        //item.setRecordSet(null);
    }

    // Delete Add Methods
}
