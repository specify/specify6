/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 *Stores informtion about records created during a workbench upload.
 *
 * @code_status Alpha
 *
 */
public class UploadedRecordInfo extends Pair<Integer, Integer> implements Comparable<UploadedRecordInfo>
{
    protected final int seq;
    protected final Object autoAssignedVal; //value of auto-assigned field for the record. (Assuming there will never be more than one)
    
    /**
     * @param key
     * @param wbRow
     * @param seq
     */
    public UploadedRecordInfo(final Integer key, final Integer wbRow, final int seq, final Object autoAssignedVal)
    {
        super(key, wbRow);
        this.seq = seq;
        this.autoAssignedVal = autoAssignedVal;
    }
    
    /**
     * @return the record key.
     */
    public Integer getKey()
    {
        return getFirst();
    }
    
    /**
     * @return the workbench row that produced the record.
     */
    public Integer getWbRow()
    {
        return getSecond();
    }

    
    /**
     * @return the autoAssignedVal
     */
    public Object getAutoAssignedVal()
    {
        return autoAssignedVal;
    }

    /**
     * @return the seq
     */
    public int getSeq()
    {
        return seq;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(UploadedRecordInfo o)
    {
        //return getKey().compareTo(o.getKey());
        int result = getWbRow().compareTo(o.getWbRow());
        if (result == 0)
        {
            result = getKey().compareTo(o.getKey());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return getKey().intValue() == ((UploadedRecordInfo )obj).getKey().intValue();
    }
    
}
