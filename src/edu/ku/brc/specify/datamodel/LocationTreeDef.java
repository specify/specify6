package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="locationtreedef"
 *     
 */
public class LocationTreeDef  implements java.io.Serializable {

    // Fields    

     protected Integer locationTreeDefId;
     protected String name;
     protected Integer treeNodeId;
     protected Integer parentNodeId;
     private Set nodes;


    // Constructors

    /** default constructor */
    public LocationTreeDef() {
    }
    
    /** constructor with id */
    public LocationTreeDef(Integer locationTreeDefId) {
        this.locationTreeDefId = locationTreeDefId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LocationTreeDefID"
     *         
     */
    public Integer getLocationTreeDefId() {
        return this.locationTreeDefId;
    }
    
    public void setLocationTreeDefId(Integer locationTreeDefId) {
        this.locationTreeDefId = locationTreeDefId;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="64"
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
     *             index="IX_TreeNodeID"
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
     *             index="IX_ParentNodeID"
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
     *             column="LocationTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Location"
     *         
     */
    public Set getNodes() {
        return this.nodes;
    }
    
    public void setNodes(Set nodes) {
        this.nodes = nodes;
    }




}