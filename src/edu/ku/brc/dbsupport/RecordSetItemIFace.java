/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.dbsupport;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public interface RecordSetItemIFace extends Comparable<RecordSetItemIFace>
{

    /**
     * @return the record ID of the record it references
     */
    public abstract Integer getRecordId();

    /**
     * @param recordId sets the record ID of the item it references
     */
    public abstract void setRecordId(final Integer recordId);
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(RecordSetItemIFace obj);
    
    /**
     * Clears the reference to the Parent RecordSetIFace
     */
    public abstract void clearParentReference();

}