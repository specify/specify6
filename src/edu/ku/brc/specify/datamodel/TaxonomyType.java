package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="TaxonomyType"
 *     
 */
public class TaxonomyType  implements java.io.Serializable {

    // Fields    

     protected Integer taxonomyTypeId;
     protected Integer kingdomId;
     protected String taxonomyTypeName;
     protected Boolean treeInfoUpToDate;
     private Set taxonNames;
     private Set taxonomicUnitTypes;
     private Set collectionTaxonomyTypes;


    // Constructors

    /** default constructor */
    public TaxonomyType() {
    }
    
    /** constructor with id */
    public TaxonomyType(Integer taxonomyTypeId) {
        this.taxonomyTypeId = taxonomyTypeId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonomyTypeID"
     *         
     */
    public Integer getTaxonomyTypeId() {
        return this.taxonomyTypeId;
    }
    
    public void setTaxonomyTypeId(Integer taxonomyTypeId) {
        this.taxonomyTypeId = taxonomyTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="KingdomID"
     *             length="10"
     *         
     */
    public Integer getKingdomId() {
        return this.kingdomId;
    }
    
    public void setKingdomId(Integer kingdomId) {
        this.kingdomId = kingdomId;
    }

    /**
     *      *            @hibernate.property
     *             column="TaxonomyTypeName"
     *             length="50"
     *         
     */
    public String getTaxonomyTypeName() {
        return this.taxonomyTypeName;
    }
    
    public void setTaxonomyTypeName(String taxonomyTypeName) {
        this.taxonomyTypeName = taxonomyTypeName;
    }

    /**
     *      *            @hibernate.property
     *             column="TreeInfoUpToDate"
     *             length="1"
     *             not-null="true"
     *         
     */
    public Boolean getTreeInfoUpToDate() {
        return this.treeInfoUpToDate;
    }
    
    public void setTreeInfoUpToDate(Boolean treeInfoUpToDate) {
        this.treeInfoUpToDate = treeInfoUpToDate;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonomyTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.TaxonName"
     *         
     */
    public Set getTaxonNames() {
        return this.taxonNames;
    }
    
    public void setTaxonNames(Set taxonNames) {
        this.taxonNames = taxonNames;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonomyTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.TaxonomicUnitType"
     *         
     */
    public Set getTaxonomicUnitTypes() {
        return this.taxonomicUnitTypes;
    }
    
    public void setTaxonomicUnitTypes(Set taxonomicUnitTypes) {
        this.taxonomicUnitTypes = taxonomicUnitTypes;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonomyTypeID"
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




}