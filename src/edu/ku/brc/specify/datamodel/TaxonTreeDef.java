package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *  @hibernate.class table="taxontreedef" 
 */
public class TaxonTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeNodeId;
     protected Integer treeDefId;
     public String name;
     public Integer rankId;
     public TaxonTreeDef parent;


    // Constructors

    /** default constructor */
    public TaxonTreeDef() {
    }
    
    /** constructor with id */
    public TaxonTreeDef(Integer treeNodeId) {
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
     *      *  @hibernate.property column="Name" length="50"
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
    public TaxonTreeDef getParent() {
        return this.parent;
    }
    
    public void setParent(TaxonTreeDef parent) {
        this.parent = parent;
    }




  // The following is extra code specified in the hbm.xml files

            
            public TreeDefinitionIface getParentDef()
            {
                return this.parent;
            }
            
            /**
        	 * @param parent the new parent TaxonTreeDef object
        	 *
        	 * @throws IllegalArgumentException if treeDef is not instance of TaxonTreeDef
        	 */
        	public void setParentDef( TreeDefinitionIface parent )
            {
                if( !(parent instanceof TaxonTreeDef) )
                {
                    throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDef");
                }
                setParent((TaxonTreeDef)parent);
            }
            
        
  // end of extra code specified in the hbm.xml files
}