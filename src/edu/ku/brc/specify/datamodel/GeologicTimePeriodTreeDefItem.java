package edu.ku.brc.specify.datamodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class GeologicTimePeriodTreeDefItem  implements TreeDefinitionItemIface,java.io.Serializable {

    // Fields    

     protected Integer treeDefItemId;
     protected String name;
     protected Integer rankId;
     protected GeologicTimePeriodTreeDef treeDef;
     protected GeologicTimePeriodTreeDefItem parent;
     protected Set children;


    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDefItem() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDefItem(Integer treeDefItemId) {
        this.treeDefItemId = treeDefItemId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getTreeDefItemId() {
        return this.treeDefItemId;
    }
    
    public void setTreeDefItemId(Integer treeDefItemId) {
        this.treeDefItemId = treeDefItemId;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
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
    public GeologicTimePeriodTreeDef getTreeDef() {
        return this.treeDef;
    }
    
    public void setTreeDef(GeologicTimePeriodTreeDef treeDef) {
        this.treeDef = treeDef;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDefItem getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriodTreeDefItem parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set getChildren() {
        return this.children;
    }
    
    public void setChildren(Set children) {
        this.children = children;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("treeDefItemId").append("='").append(getTreeDefItemId()).append("' ");			
      buffer.append("name").append("='").append(getName()).append("' ");			
      buffer.append("treeDef").append("='").append(getTreeDef()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
	}



  // The following is extra code specified in the hbm.xml files

                
                public TreeDefinitionIface getTreeDefinition()
                {
                    return getTreeDef();
                }
                
                public void setTreeDefinition(TreeDefinitionIface treeDef)
                {
                    if( !(treeDef instanceof GeologicTimePeriodTreeDef) )
                    {
                        throw new IllegalArgumentException("Argument must be an instanceof GeologicTimePeriodTreeDef");
                    }
                    setTreeDef((GeologicTimePeriodTreeDef)treeDef);
                }
                
                public TreeDefinitionItemIface getParentItem()
                {
                    return getParent();
                }
                
                public void setParentItem(TreeDefinitionItemIface parent)
                {
                    if( !(parent instanceof GeologicTimePeriodTreeDefItem) )
                    {
                        throw new IllegalArgumentException("Argument must be an instanceof GeologicTimePeriodTreeDefItem");
                    }
                    setParent((GeologicTimePeriodTreeDefItem)parent);
                }
    
                public TreeDefinitionItemIface getChildItem()
                {
                    if( getChildren().isEmpty() )
                    {
                        return null;
                    }
                    
                    return (TreeDefinitionItemIface)getChildren().iterator().next();
                }
                
                public void setChildItem(TreeDefinitionItemIface child)
                {
                    if( !(child instanceof GeologicTimePeriodTreeDefItem) )
                    {
                        throw new IllegalArgumentException("Argument must be an instanceof GeologicTimePeriodTreeDefItem");
                    }
                    Set children = Collections.synchronizedSet(new HashSet());
                    children.add(child);
                    setChildren(children);
                }
                
            
  // end of extra code specified in the hbm.xml files
}