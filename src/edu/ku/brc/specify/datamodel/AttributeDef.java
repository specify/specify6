package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
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
     protected Set<CollectingEventAttr> collectingEventAttrs;
     protected Set<PreparationAttr> preparationAttr;
     protected Set<CollectionObjectAttr> collectionObjectAttrs;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public AttributeDef() {
    }

    /** constructor with id */
    public AttributeDef(Integer attributeDefId) {
        this.attributeDefId = attributeDefId;
    }




    // Initializer
    public void initialize()
    {
        attributeDefId = null;
        tableType = null;
        fieldName = null;
        dataType = null;
        collectionObjDef = null;
        prepType = null;
        collectingEventAttrs = new HashSet<CollectingEventAttr>();
        preparationAttr = new HashSet<PreparationAttr>();
        collectionObjectAttrs = new HashSet<CollectionObjectAttr>();
        externalResources = new HashSet<ExternalResource>();
    }
    // End Initializer

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
    public Set<CollectingEventAttr> getCollectingEventAttrs() {
        return this.collectingEventAttrs;
    }

    public void setCollectingEventAttrs(Set<CollectingEventAttr> collectingEventAttrs) {
        this.collectingEventAttrs = collectingEventAttrs;
    }

    /**
     *
     */
    public Set getPreparationAttr() {
        return this.preparationAttr;
    }

    public void setPreparationAttr(Set<PreparationAttr> preparationAttr) {
        this.preparationAttr = preparationAttr;
    }

    /**
     *
     */
    public Set<CollectionObjectAttr> getCollectionObjectAttrs() {
        return this.collectionObjectAttrs;
    }

    public void setCollectionObjectAttrs(Set<CollectionObjectAttr> collectionObjectAttrs) {
        this.collectionObjectAttrs = collectionObjectAttrs;
    }

    /**
     *
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }

    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }




    // Add Methods

    public void addCollectingEventAttr(final CollectingEventAttr collectingEventAttr)
    {
        this.collectingEventAttrs.add(collectingEventAttr);
    }

    public void addPreparationAttr(final PreparationAttr preparationAttr)
    {
        this.preparationAttr.add(preparationAttr);
    }

    public void addCollectionObjectAttr(final CollectionObjectAttr collectionObjectAttr)
    {
        this.collectionObjectAttrs.add(collectionObjectAttr);
    }

    public void addExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
    }

    // Done Add Methods
}
