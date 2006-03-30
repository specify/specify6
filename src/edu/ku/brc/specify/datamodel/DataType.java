package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

 */
public class DataType  implements java.io.Serializable {

    // Fields    

     protected Integer dataTypeId;
     protected String name;
     private Set collectionObjDef;


    // Constructors

    /** default constructor */
    public DataType() {
    }
    
    /** constructor with id */
    public DataType(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getDataTypeId() {
        return this.dataTypeId;
    }
    
    public void setDataTypeId(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
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
    public Set getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(Set collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("name").append("='").append(getName()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
	}



}