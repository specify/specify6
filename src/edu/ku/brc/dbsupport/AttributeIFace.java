/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.dbsupport;
import java.sql.Timestamp;

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
        public static String getString(final short ord)
        {
            String[] names = {"Integer", "Float", "Double", "Boolean", "String"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            return names[ord];
        }
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
     * Returns the timestamp it was created.
     * @return the timestamp it was created
     */
    public Timestamp getTimestampCreated();
    
    /**
     * Sets the timestamp it was created.
     * @param timestampCreated the timestamp it was created
     */
    public void setTimestampCreated(Timestamp timestampCreated);
    

    /**
     * Returns the timestamp of the record (could be null for a new object).
     * @return the timestamp of the record (could be null for a new object)
     */
    public Timestamp getTimestampModified();
    
    /**
     * Sets the timestamp.
     * @param timestampModified the timestamp value
     */
    public void setTimestampModified(Timestamp timestampModified);


    /**
     * Returns the definition of the attribute
     * @return the definition of the attribute
     */
    public AttributeDef getDefinition();
}
