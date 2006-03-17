package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *  @hibernate.class
 *             table="geologictimeperiodtreedef" 
 */
public class GeologicTimePeriodTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeDefId;
     protected String name;
     protected String remarks;
     private Set treeEntries;
     private Set treeDefItems;


    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDef() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDef(Integer treeDefId) {
        this.treeDefId = treeDefId;
    }
   
    
    

    // Property accessors

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
     *      *                 @hibernate.property column="Name" length="64"
     *             
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *                 @hibernate.property column="Remarks" length="255"
     *             
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * Entries referencing this defintion
     */
    public Set getTreeEntries() {
        return this.treeEntries;
    }
    
    public void setTreeEntries(Set treeEntries) {
        this.treeEntries = treeEntries;
    }

    /**
     *      * The individual elements of this definition
     */
    public Set getTreeDefItems() {
        return this.treeDefItems;
    }
    
    public void setTreeDefItems(Set treeDefItems) {
        this.treeDefItems = treeDefItems;
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