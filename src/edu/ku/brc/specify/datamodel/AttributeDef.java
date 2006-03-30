package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

 */
public class AttributeDef  implements java.io.Serializable {

    // Fields    

     protected Integer attributeDefId;
     protected Short tableType;
     protected String fieldName;
     protected Short dataType;
     protected CollectionObjDef collectionObjDef;
     protected PrepType prepType;
     protected Set collectingEventAttrs;
     protected Set preparationAttr;
     protected Set collectionObjectAttrs;
     protected Set externalResources;


    // Constructors

    /** default constructor */
    public AttributeDef() {
    }
    
    /** constructor with id */
    public AttributeDef(Integer attributeDefId) {
        this.attributeDefId = attributeDefId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getAttributeDefId() {
        return this.attributeDefId;
    }
    
    public void setAttributeDefId(Integer attributeDefId) {
        this.attributeDefId = attributeDefId;
    }

    /**
     * 
     */
    public Short getTableType() {
        return this.tableType;
    }
    
    public void setTableType(Short tableType) {
        this.tableType = tableType;
    }

    /**
     * 
     */
    public String getFieldName() {
        return this.fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 
     */
    public Short getDataType() {
        return this.dataType;
    }
    
    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }

    /**
     * 
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     * 
     */
    public PrepType getPrepType() {
        return this.prepType;
    }
    
    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     * 
     */
    public Set getCollectingEventAttrs() {
        return this.collectingEventAttrs;
    }
    
    public void setCollectingEventAttrs(Set collectingEventAttrs) {
        this.collectingEventAttrs = collectingEventAttrs;
    }

    /**
     * 
     */
    public Set getPreparationAttr() {
        return this.preparationAttr;
    }
    
    public void setPreparationAttr(Set preparationAttr) {
        this.preparationAttr = preparationAttr;
    }

    /**
     * 
     */
    public Set getCollectionObjectAttrs() {
        return this.collectionObjectAttrs;
    }
    
    public void setCollectionObjectAttrs(Set collectionObjectAttrs) {
        this.collectionObjectAttrs = collectionObjectAttrs;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




}