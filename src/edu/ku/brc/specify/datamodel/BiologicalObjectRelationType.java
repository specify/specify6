package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="BiologicalObjectRelationType"
 *     
 */
public class BiologicalObjectRelationType  implements java.io.Serializable {

    // Fields    

     protected Integer biologicalObjectRelationTypeId;
     protected String relationshipName;
     protected String leftSideRoleName;
     protected String rightSideRoleName;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Short isReflexive;
     private Set biologicalObjectRelations;


    // Constructors

    /** default constructor */
    public BiologicalObjectRelationType() {
    }
    
    /** constructor with id */
    public BiologicalObjectRelationType(Integer biologicalObjectRelationTypeId) {
        this.biologicalObjectRelationTypeId = biologicalObjectRelationTypeId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BiologicalObjectRelationTypeID"
     *         
     */
    public Integer getBiologicalObjectRelationTypeId() {
        return this.biologicalObjectRelationTypeId;
    }
    
    public void setBiologicalObjectRelationTypeId(Integer biologicalObjectRelationTypeId) {
        this.biologicalObjectRelationTypeId = biologicalObjectRelationTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="RelationshipName"
     *             length="50"
     *         
     */
    public String getRelationshipName() {
        return this.relationshipName;
    }
    
    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    /**
     *      *            @hibernate.property
     *             column="LeftSideRoleName"
     *             length="50"
     *         
     */
    public String getLeftSideRoleName() {
        return this.leftSideRoleName;
    }
    
    public void setLeftSideRoleName(String leftSideRoleName) {
        this.leftSideRoleName = leftSideRoleName;
    }

    /**
     *      *            @hibernate.property
     *             column="RightSideRoleName"
     *             length="50"
     *         
     */
    public String getRightSideRoleName() {
        return this.rightSideRoleName;
    }
    
    public void setRightSideRoleName(String rightSideRoleName) {
        this.rightSideRoleName = rightSideRoleName;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
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
     *      *            @hibernate.property
     *             column="IsReflexive"
     *             length="5"
     *         
     */
    public Short getIsReflexive() {
        return this.isReflexive;
    }
    
    public void setIsReflexive(Short isReflexive) {
        this.isReflexive = isReflexive;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="BiologicalObjectRelationTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BiologicalObjectRelation"
     *         
     */
    public Set getBiologicalObjectRelations() {
        return this.biologicalObjectRelations;
    }
    
    public void setBiologicalObjectRelations(Set biologicalObjectRelations) {
        this.biologicalObjectRelations = biologicalObjectRelations;
    }




}