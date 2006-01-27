package edu.ku.brc.specify.ui.db;

import java.util.*;




/**
 *       Represents a PickList
 *       @author Rod Spears (with help from Hibernate)
 *     
 */
public class PickList  implements java.io.Serializable {

    // Fields    

     private Integer picklist_id;
     private Date created;
     private Set items;


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