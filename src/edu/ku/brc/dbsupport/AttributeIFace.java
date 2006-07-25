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
 * 
 * @code_status Unknown (auto-generated)
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
        StringType(4),
        MemoType(5);
        
        FieldType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
        public void set(final short  ord) { this.ord = ord; }
    }


    /**
     * 
     */
    public Integer getAttrId();
    
    public void setAttrId(Integer attrId);
    

    public String getStrValue();
    
    public void setStrValue(String strValue);
    

    public Double getDblValue();
    
    public void setDblValue(Double dblValue);
    

    public Date getTimestampCreated();
    
    public void setTimestampCreated(Date timestampCreated);
    

    public Date getTimestampModified();
    
    public void setTimestampModified(Date timestampModified);


    public AttributeDef getDefinition();
}
