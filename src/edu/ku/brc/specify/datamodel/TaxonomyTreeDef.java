package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="taxonomytreedef"
 *     
 */
public class TaxonomyTreeDef  implements java.io.Serializable {

    // Fields    

     protected Integer taxonomyTreeDefId;
     protected String name;
     protected Integer treeNodeId;
     protected Integer parentNodeId;
     private Set children;
     private CollectionObjDef collectionObjDef;


    // Constructors

    /** default constructor */
    public TaxonomyTreeDef() {
    }
    
    /** constructor with id */
    public TaxonomyTreeDef(Integer taxonomyTreeDefId) {
        this.taxonomyTreeDefId = taxonomyTreeDefId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonomyTreeDefID"
     *         
     */
    public Integer getTaxonomyTreeDefId() {
        return this.taxonomyTreeDefId;
    }
    
    public void setTaxonomyTreeDefId(Integer taxonomyTreeDefId) {
        this.taxonomyTreeDefId = taxonomyTreeDefId;
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
     *             column="TreeNodeID"
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
     *             column="ParentNodeID"
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
    public Set getChildren() {
        return this.children;
    }
    
    public void setChildren(Set children) {
        this.children = children;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionObjDefID"         
     *         
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }




}