package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="geologictimeperiodtreedef"
 *     
 */
public class GeologicTimePeriodTreeDef  implements java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodTreeDefId;
     protected String name;
     protected Integer treeNodeId;
     protected Integer parentNodeId;
     private Set nodes;


    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDef() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDef(Integer geologicTimePeriodTreeDefId) {
        this.geologicTimePeriodTreeDefId = geologicTimePeriodTreeDefId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GeologicTimePeriodTreeDefID"
     *         
     */
    public Integer getGeologicTimePeriodTreeDefId() {
        return this.geologicTimePeriodTreeDefId;
    }
    
    public void setGeologicTimePeriodTreeDefId(Integer geologicTimePeriodTreeDefId) {
        this.geologicTimePeriodTreeDefId = geologicTimePeriodTreeDefId;
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
     *             column="GeologicTimePeriodTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GeologicTimePeriod"
     *         
     */
    public Set getNodes() {
        return this.nodes;
    }
    
    public void setNodes(Set nodes) {
        this.nodes = nodes;
    }




}