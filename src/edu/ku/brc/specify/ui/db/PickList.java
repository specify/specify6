package edu.ku.brc.specify.ui.db;

import java.util.Date;
import java.util.Set;




/**
 *       Represents a PickList
 *       @author Rod Spears (with help from Hibernate)
 *     
 */
@SuppressWarnings("serial")
public class PickList  implements java.io.Serializable {

    // Fields    

     protected Integer pickListId;
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
    public PickList(Integer pickListId) {
        this.pickListId = pickListId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getPickListId() {
        return this.pickListId;
    }
    
    public void setPickListId(Integer pickListId) {
        this.pickListId = pickListId;
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