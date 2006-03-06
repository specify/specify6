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
     protected Short tableType;
     protected Short subType;
     protected String fieldName;
     protected Short dataType;
     protected CollectionObjDef collectionObjDefID;


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
     *             column="TableType"
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
     *             column="SubType"
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
     *             column="FieldName"
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
     *             column="DataType"
     *         
     */
    public Short getDataType() {
        return this.dataType;
    }
    
    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionObjDefID"
     *         
     */
    public CollectionObjDef getCollectionObjDefID() {
        return this.collectionObjDefID;
    }
    
    public void setCollectionObjDefID(CollectionObjDef collectionObjDefID) {
        this.collectionObjDefID = collectionObjDefID;
    }




}