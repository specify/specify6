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
     * @return
     */
    public abstract Integer getRecordId();

    /**
     * @param recordId
     */
    public abstract void setRecordId(final Integer recordId);
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(RecordSetItemIFace obj);
    
    /**
     * 
     */
    public abstract void clearParentReference();

}