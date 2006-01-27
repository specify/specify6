package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="attrsdef"
 *     
 */
public class AttrsDef  implements java.io.Serializable {

    // Fields    

     protected Integer attrsDefId;
     protected Short disciplineType;
     protected Short tableType;
     protected Short subType;
     protected String fieldName;
     protected Short dataType;


    // Constructors

    /** default constructor */
    public AttrsDef() {
    }
    
    /** constructor with id */
    public AttrsDef(Integer attrsDefId) {
        this.attrsDefId = attrsDefId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AttrsDefID"
     *         
     */
    public Integer getAttrsDefId() {
        return this.attrsDefId;
    }
    
    public void setAttrsDefId(Integer attrsDefId) {
        this.attrsDefId = attrsDefId;
    }

    /**
     *      *            @hibernate.property
     *             column="disciplineType"
     *             length="5"
     *         
     */
    public Short getDisciplineType() {
        return this.disciplineType;
    }
    
    public void setDisciplineType(Short disciplineType) {
        this.disciplineType = disciplineType;
    }

    /**
     *      *            @hibernate.property
     *             column="tableType"
     *             length="5"
     *         
     */
    public Short getTableType() {
        return this.tableType;
    }
    
    public void setTableType(Short tableType) {
        this.tableType = tableType;
    }

    /**
     *      *            @hibernate.property
     *             column="subType"
     *             length="5"
     *         
     */
    public Short getSubType() {
        return this.subType;
    }
    
    public void setSubType(Short subType) {
        this.subType = subType;
    }

    /**
     *      *            @hibernate.property
     *             column="fieldName"
     *             length="32"
     *         
     */
    public String getFieldName() {
        return this.fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     *      *            @hibernate.property
     *             column="dataType"
     *             length="5"
     *         
     */
    public Short getDataType() {
        return this.dataType;
    }
    
    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }




}