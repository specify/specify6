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

    public abstract Long getRecordId();

    public abstract void setRecordId(final Long recordId);
    
    public int compareTo(RecordSetItemIFace obj);

}