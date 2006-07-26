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
package edu.ku.brc.dbsupport;
import java.util.Date;

import edu.ku.brc.specify.datamodel.AttributeDef;

/**
 * Datamodel classes that represent Attributes for must implement this interface. 
 * An attribute object can hold a single value of a certain type. The type of the value is defined
 * by the AttributeDef object. The database class "should" enfore setting the correct type of value.
 * For example, if the AttributeDef indicates the value is a string then it should only allow a string to be set.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface AttributeIFace
{
    // Field Type Enumerations
    public enum FieldType 
    {
        IntegerType(0),
        FloatType(1),
        DoubleType(2),
        BooleanType(3),
        StringType(4);
        //MemoType(5); // XXX Not sure if we can really support memo types (currently most of the attr tables do not)
        //             // we should probably remove this or figure out how to enforce it.
        
        FieldType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
        public void set(final short  ord) { this.ord = ord; }
    }


    /**
     * Returns the record id.
     * @return the record id
     */
    public Integer getAttrId();
    
    /**
     * Sets the record id.
     * @param attrId
     */
    public void setAttrId(Integer attrId);  

    /**
     * Returns that if it is a string, otherwise null
     * @return a value if it is a string, otherwise null
     */
    public String getStrValue();
    
    /**
     * Sest the value as a string value.
     * @param strValue the string value to be set
     */
    public void setStrValue(String strValue);
    
    /**
     * Returns the value if it is a Int value.
     * @return the Bool value
     */
    public Integer getIntValue();
    
    /**
     * Sets the value to a Int value.
     * @param value the Int value
     */
    public void setIntValue(Integer value);
    
    /**
     * Returns the value if it is a Float value.
     * @return the int value
     */
    public Float getFloatValue();
    
    /**
     * Sets the value to a Float value.
     * @param value the Float value
     */
    public void setFloatValue(Float value);
    
   /**
     * Returns the value if it is a Bool value.
     * @return the Bool value
     */
    public Boolean getBoolValue();
    
    /**
     * Sets the value to a Bool value.
     * @param value the Bool value
     */
    public void setBoolValue(Boolean value);
    

    /**
     * Returns the value if it is a double value.
     * @return the double value
     */
    public Double getDblValue();
    
     /**
     * Sets the value to a double value.
     * @param value the double value
     */
    public void setDblValue(Double value);
    

    /**
     * @return
     */
    public Date getTimestampCreated();
    
    /**
     * @param timestampCreated
     */
    public void setTimestampCreated(Date timestampCreated);
    

    /**
     * Returns the timestamp of the record (could be null for a new object).
     * @return the timestamp of the record (could be null for a new object)
     */
    public Date getTimestampModified();
    
    /**
     * Sets the timestamp.
     * @param timestampModified the timestamp value
     */
    public void setTimestampModified(Date timestampModified);


    /**
     * Returns the definition of the attribute
     * @return the definition of the attribute
     */
    public AttributeDef getDefinition();
}
