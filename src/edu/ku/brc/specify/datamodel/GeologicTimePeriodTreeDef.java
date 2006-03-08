package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *  @hibernate.class
 *             table="geologictimeperiodtreedef" 
 */
public class GeologicTimePeriodTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeNodeId;
     protected Integer treeDefId;
     public String name;
     public Integer rankId;
     public GeologicTimePeriodTreeDef parent;


    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDef() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDef(Integer treeNodeId) {
        this.treeNodeId = treeNodeId;
    }
   
    
    

    // Property accessors

    /**
     *      *  @hibernate.property column="TreeNodeID" length="10" 
     */
    public Integer getTreeNodeId() {
        return this.treeNodeId;
    }
    
    public void setTreeNodeId(Integer treeNodeId) {
        this.treeNodeId = treeNodeId;
    }

    /**
     *      *  @hibernate.id generator-class="assigned"
     *                 type="java.lang.Integer" column="TreeDefID" 
     */
    public Integer getTreeDefId() {
        return this.treeDefId;
    }
    
    public void setTreeDefId(Integer treeDefId) {
        this.treeDefId = treeDefId;
    }

    /**
     *      *  @hibernate.property column="Name" length="64"
     *             
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *  @hibernate.property column="RankID" 
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDef getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriodTreeDef parent) {
        this.parent = parent;
    }




  // The following is extra code specified in the hbm.xml files

            
            public TreeDefinitionIface getParentDef()
            {
                return this.parent;
            }
            
            /**
        	 * @param parent the new parent GeologicTimePeriodTreeDef object
        	 *
        	 * @throws IllegalArgumentException if treeDef is not instance of GeologicTimePeriodTreeDef
        	 */
        	public void setParentDef( TreeDefinitionIface parent )
            {
                if( !(parent instanceof GeologicTimePeriodTreeDef) )
                {
                    throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDef");
                }
                setParent((GeologicTimePeriodTreeDef)parent);
            }
            
        
  // end of extra code specified in the hbm.xml files
}