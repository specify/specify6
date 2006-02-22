package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="taxonomytreedef"
 *     
 */
public class TaxonomyTreeDef  implements java.io.Serializable {

    // Fields    

     protected Integer taxonomyTreeDefid;
     protected String name;
     protected Integer treeNodeId;
     protected Integer parentNodeId;
     private Set Taxons;
     protected CollectionObjDef collectionObjDef;


    // Constructors

    /** default constructor */
    public TaxonomyTreeDef() {
    }
    
    /** constructor with id */
    public TaxonomyTreeDef(Integer taxonomyTreeDefid) {
        this.taxonomyTreeDefid = taxonomyTreeDefid;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonomyTreeDefID"
     *         
     */
    public Integer getTaxonomyTreeDefid() {
        return this.taxonomyTreeDefid;
    }
    
    public void setTaxonomyTreeDefid(Integer taxonomyTreeDefid) {
        this.taxonomyTreeDefid = taxonomyTreeDefid;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="50"
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
     *             column="TreeNodeId"
     *             length="10"
     *         
     */
    public Integer getTreeNodeId() {
        return this.treeNodeId;
    }
    
    public void setTreeNodeId(Integer treeNodeId) {
        this.treeNodeId = treeNodeId;
    }

    /**
     *      *            @hibernate.property
     *             column="ParentNodeId"
     *             length="10"
     *         
     */
    public Integer getParentNodeId() {
        return this.parentNodeId;
    }
    
    public void setParentNodeId(Integer parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonomyTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Taxon"
     *         
     */
    public Set getTaxons() {
        return this.Taxons;
    }
    
    public void setTaxons(Set Taxons) {
        this.Taxons = Taxons;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     * 			cascade="delete"
     *         
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }




}