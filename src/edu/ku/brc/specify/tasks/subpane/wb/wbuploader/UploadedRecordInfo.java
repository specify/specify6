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
 * @code_status Alpha
 *
 */
public class UploadedRecordInfo extends Pair<Integer, Integer> implements Comparable<UploadedRecordInfo>
{
    protected final int seq;
    
    public UploadedRecordInfo(final Integer key, final Integer wbRow, final int seq)
    {
        super(key, wbRow);
        this.seq = seq;
    }
    
    public Integer getKey()
    {
        return getFirst();
    }
    
    public Integer getWbRow()
    {
        return getSecond();
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
    @Override
    public int compareTo(UploadedRecordInfo o)
    {
        return getKey().compareTo(o.getKey());
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
