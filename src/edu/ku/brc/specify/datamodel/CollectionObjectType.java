package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="CollectionObjectType"
 *     
 */
public class CollectionObjectType  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectTypeId;
     protected String collectionObjectTypeName;
     protected String category;
     private Set habitats;
     private Set preparations;
     private Set collectionTaxonomyTypes;
     protected Set collectionObjects;
     private Set determinations;
     protected BioAttrs bioattrss;
     protected CollectingEvent collectingevents;
     private Set catalogseriesdefinitions;


    // Constructors

    /** default constructor */
    public CollectionObjectType() {
    }
    
    /** constructor with id */
    public CollectionObjectType(Integer collectionObjectTypeId) {
        this.collectionObjectTypeId = collectionObjectTypeId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectionObjectTypeID"
     *         
     */
    public Integer getCollectionObjectTypeId() {
        return this.collectionObjectTypeId;
    }
    
    public void setCollectionObjectTypeId(Integer collectionObjectTypeId) {
        this.collectionObjectTypeId = collectionObjectTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectionObjectTypeName"
     *             length="50"
     *         
     */
    public String getCollectionObjectTypeName() {
        return this.collectionObjectTypeName;
    }
    
    public void setCollectionObjectTypeName(String collectionObjectTypeName) {
        this.collectionObjectTypeName = collectionObjectTypeName;
    }

    /**
     *      *            @hibernate.property
     *             column="Category"
     *             length="50"
     *         
     */
    public String getCategory() {
        return this.category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="BiologicalObjectTypeCollectedID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Habitat"
     *         
     */
    public Set getHabitats() {
        return this.habitats;
    }
    
    public void setHabitats(Set habitats) {
        this.habitats = habitats;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="PhysicalObjectTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.PrepsObj"
     *         
     */
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="BiologicalObjectTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionTaxonomyType"
     *         
     */
    public Set getCollectionTaxonomyTypes() {
        return this.collectionTaxonomyTypes;
    }
    
    public void setCollectionTaxonomyTypes(Set collectionTaxonomyTypes) {
        this.collectionTaxonomyTypes = collectionTaxonomyTypes;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionObjectTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObj"
     *         
     */
    public Set getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="BiologicalObjectTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Determination"
     *         
     */
    public Set getDeterminations() {
        return this.determinations;
    }
    
    public void setDeterminations(Set determinations) {
        this.determinations = determinations;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public BioAttrs getBioattrss() {
        return this.bioattrss;
    }
    
    public void setBioattrss(BioAttrs bioattrss) {
        this.bioattrss = bioattrss;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public CollectingEvent getCollectingevents() {
        return this.collectingevents;
    }
    
    public void setCollectingevents(CollectingEvent collectingevents) {
        this.collectingevents = collectingevents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ObjectTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CatalogSeriesDefinition"
     *         
     */
    public Set getCatalogseriesdefinitions() {
        return this.catalogseriesdefinitions;
    }
    
    public void setCatalogseriesdefinitions(Set catalogseriesdefinitions) {
        this.catalogseriesdefinitions = catalogseriesdefinitions;
    }




}