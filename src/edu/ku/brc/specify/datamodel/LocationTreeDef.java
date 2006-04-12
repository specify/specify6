package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class LocationTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeDefId;
     protected String name;
     protected String remarks;
     protected Set treeEntries;
     protected Set treeDefItems;
     protected Set collObjDefs;


    // Constructors

    /** default constructor */
    public LocationTreeDef() {
    }
    
    /** constructor with id */
    public LocationTreeDef(Integer treeDefId) {
        this.treeDefId = treeDefId;
    }
   
    

    // Initializer
    /*public void initialize()
    {
        treeDefId = null;
        name = null;
        remarks = null;
        treeEntries = new HashSet<TreeEntrie>();
        treeDefItems = new HashSet<TreeDefItem>();
        collObjDefs = new HashSet<CollObjDef>();
    }*/
    // End Initializer 

    // Property accessors

    /**
     * 
     */
    public Integer getTreeDefId() {
        return this.treeDefId;
    }
    
    public void setTreeDefId(Integer treeDefId) {
        this.treeDefId = treeDefId;
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
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Set getTreeEntries() {
        return this.treeEntries;
    }
    
    public void setTreeEntries(Set treeEntries) {
        this.treeEntries = treeEntries;
    }

    /**
     * 
     */
    public Set getTreeDefItems() {
        return this.treeDefItems;
    }
    
    public void setTreeDefItems(Set treeDefItems) {
        this.treeDefItems = treeDefItems;
    }

    /**
     * 
     */
    public Set getCollObjDefs() {
        return this.collObjDefs;
    }
    
    public void setCollObjDefs(Set collObjDefs) {
        this.collObjDefs = collObjDefs;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("treeDefId").append("='").append(getTreeDefId()).append("' ");			
      buffer.append("name").append("='").append(getName()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
	}



}