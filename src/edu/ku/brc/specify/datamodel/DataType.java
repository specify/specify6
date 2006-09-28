/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class DataType extends DataModelObjBase implements java.io.Serializable {

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

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
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

    public void addCollectionObjDef(final CollectionObjDef collectionObjDefArg)
    {
        this.collectionObjDef.add(collectionObjDefArg);
        collectionObjDefArg.setDataType(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDef(final CollectionObjDef collectionObjDefArg)
    {
        this.collectionObjDef.remove(collectionObjDefArg);
        collectionObjDefArg.setDataType(null);
    }

    // Delete Add Methods
}
