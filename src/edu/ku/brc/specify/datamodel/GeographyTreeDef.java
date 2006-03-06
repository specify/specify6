package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="geographytreedef"
 *     
 */
public class GeographyTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeDefId;
     protected String name;
     protected Integer treeNodeId;
     protected Integer parentNodeId;
     private Set nodes;


    // Constructors

    /** default constructor */
    public GeographyTreeDef() {
    }
    
    /** constructor with id */
    public GeographyTreeDef(Integer treeDefId) {
        this.treeDefId = treeDefId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TreeDefID"
     *         
     */
    public Integer getTreeDefId() {
        return this.treeDefId;
    }
    
    public void setTreeDefId(Integer treeDefId) {
        this.treeDefId = treeDefId;
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
     *             index="IX_GeoTDTreeNodeID"
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
     *             index="IX_GeoTDParentNodeID"
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
     *             column="GeographyTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Geography"
     *         
     */
    public Set getNodes() {
        return this.nodes;
    }
    
    public void setNodes(Set nodes) {
        this.nodes = nodes;
    }




}