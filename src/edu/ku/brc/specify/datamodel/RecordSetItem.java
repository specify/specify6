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

import edu.ku.brc.dbsupport.RecordSetItemIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class RecordSetItem implements java.io.Serializable, RecordSetItemIFace {

    // Fields    

     protected Long recordId;


    // Constructors

    /** default constructor */
     public RecordSetItem() 
     {
         
     }
     
     public RecordSetItem(final Long recordId) 
     {
         this.recordId = recordId;
     }
     
     public RecordSetItem(final String recordId) 
     {
         this.recordId = Long.parseLong(recordId);
     }

    // Initializer
    public void initialize()
    {
        recordId = null;
    }
    // End Initializer

    // Property accessors


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetItemIFace#getRecordId()
     */
    public Long getRecordId() {
        return this.recordId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetItemIFace#setRecordId(java.lang.Long)
     */
    public void setRecordId(final Long recordId) {
        this.recordId = recordId;
    }
    
    public int compareTo(RecordSetItemIFace obj)
    {
        return recordId.compareTo(obj.getRecordId());
    }
    
    // Add Methods

    // Done Add Methods
}
