package edu.ku.brc.specify.datamodel;

import java.util.*;


import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import edu.ku.brc.specify.dbsupport.HibernateUtil;


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
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     protected Set items;


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




  // The following is extra code specified in the hbm.xml files

        
    protected CollectionObject collectionObject = null;
    
    /**
     * 
     */
    public CollectionObject getCollectionObject() 
    {
        if (collectionObjectId != null)
        {
	        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class);
	        criteria.add(Expression.eq("collectionObjectId", collectionObjectId));
	        java.util.List list = criteria.list();
	        this.collectionObject = list != null && list.size() > 0 ? (CollectionObject)list.get(0) : null;
	        return this.collectionObject;
	    } 
	    return null;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) 
    {
        this.collectionObject = collectionObject;
        this.collectionObjectId = collectionObject != null ? collectionObject.getCollectionObjectId() : null;
    }
    
    
  // end of extra code specified in the hbm.xml files
}