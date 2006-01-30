package edu.ku.brc.specify.ui.db;

import java.util.*;




/**
 *       Represents a PickList
 *       @author Rod Spears (with help from Hibernate)
 *     
 */
public class PickList  implements java.io.Serializable {

    // Fields    

     protected Integer picklist_id;
     protected String name;
     protected Boolean readOnly;
     protected Integer sizeLimit;
     protected Date created;
     protected Set items;


    // Constructors

    /** default constructor */
    public PickList() {
    }
    
    /** constructor with id */
    public PickList(Integer picklist_id) {
        this.picklist_id = picklist_id;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getPicklist_id() {
        return this.picklist_id;
    }
    
    public void setPicklist_id(Integer picklist_id) {
        this.picklist_id = picklist_id;
    }

    /**
     *      *            @hibernate.property
     *             column="name"
     *             length="64"
     *             not-null="true"
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
     *             column="readonly"
     *             not-null="true"
     *             length="1"
     *         
     */
    public Boolean getReadOnly() {
        return this.readOnly;
    }
    
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     *      *            @hibernate.property
     *             column="sizeimit"
     *             length="10"
     *         
     */
    public Integer getSizeLimit() {
        return this.sizeLimit;
    }
    
    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    /**
     * 
     */
    public Date getCreated() {
        return this.created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * 
     */
    public Set getItems() {
        return this.items;
    }
    
    public void setItems(Set items) {
        this.items = items;
    }




}