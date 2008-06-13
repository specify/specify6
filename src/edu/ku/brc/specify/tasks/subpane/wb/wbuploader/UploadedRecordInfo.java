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
public class UploadedRecordInfo extends Pair<Integer, Integer>
{
    public UploadedRecordInfo(final Integer key, final Integer wbRow)
    {
        super(key, wbRow);
    }
    
    public Integer getKey()
    {
        return getFirst();
    }
    
    public Integer getWbRow()
    {
        return getSecond();
    }
    
}
