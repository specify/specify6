package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class DataType  implements java.io.Serializable {

    // Fields

     protected Long dataTypeId;
     protected String name;
     protected Set<CollectionObjDef> collectionObjDef;


    // Constructors

    /** default constructor */
    public DataType() {
    }

    /** constructor with id */
    public DataType(Long dataTypeId) {
        this.dataTypeId = dataTypeId;
    }




    // Initializer
    public void initialize()
    {
        dataTypeId = null;
        name = null;
        collectionObjDef = new HashSet<CollectionObjDef>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Long getDataTypeId() {
        return this.dataTypeId;
    }

    public void setDataTypeId(Long dataTypeId) {
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
    public Set<CollectionObjDef> getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(Set<CollectionObjDef> collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer(128);

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("name").append("='").append(getName()).append("' ");
      buffer.append("]");

      return buffer.toString();
	}




    // Add Methods

    public void addCollectionObjDef(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.add(collectionObjDef);
        collectionObjDef.setDataType(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDef(final CollectionObjDef collectionObjDef)
    {
        this.collectionObjDef.remove(collectionObjDef);
        collectionObjDef.setDataType(null);
    }

    // Delete Add Methods
}
