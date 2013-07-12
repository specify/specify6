/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name="recordsetitem")
public class RecordSetItem implements java.io.Serializable, RecordSetItemIFace {

    // Fields

    protected Integer   recordSetItemId;
    protected RecordSet recordSet;
    protected Integer   recordId;


    // Constructors

    /** default constructor */
     public RecordSetItem() 
     {
         //
     }
     
     public RecordSetItem(final Integer recordId) 
     {
         this.recordId = recordId;
     }
     
     public RecordSetItem(final String recordId) 
     {
         this.recordId = Integer.parseInt(recordId);
     }

    // Initializer
    public void initialize()
    {
        recordSetItemId = null;
        recordId = null;
        recordSet = null;
    }
    // End Initializer

    // Property accessors

    @Id
    @GeneratedValue
    @Column(name = "RecordSetItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getRecordSetItemId()
    {
        return recordSetItemId;
    }

    public void setRecordSetItemId(Integer recordSetItemId)
    {
        this.recordSetItemId = recordSetItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetItemIFace#getRecordId()
     */
    @Column(name = "RecordId", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getRecordId() 
    {
        return this.recordId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.RecordSetItemIFace#setRecordId(java.lang.Integer)
     */
    public void setRecordId(final Integer recordId) 
    {
        this.recordId = recordId;
    }
    
    public int compareTo(RecordSetItemIFace obj)
    {
        return recordId != null && obj != null && obj.getRecordId() != null ? recordId.compareTo(obj.getRecordId()) : 0;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "RecordSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public RecordSet getRecordSet()
    {
        return recordSet;
    }

    /**
     * @param recordSet
     */
    public void setRecordSet(RecordSet recordSet)
    {
        this.recordSet = recordSet;
    }
    
    /**
     * @param recordSet
     */
    public void setRecordSet(RecordSetIFace recordSet)
    {
        setRecordSet((RecordSet)recordSet);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.RecordSetItemIFace#clearParentReference()
     */
    public void clearParentReference()
    {
        recordSet = null;
    }
}
