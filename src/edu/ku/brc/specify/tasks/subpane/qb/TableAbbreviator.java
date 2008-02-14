/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.HashMap;
import java.util.Map;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Assigns unique abbreviations/aliases for tables during the construction of
 *hql/sql for a query.
 */
class TableAbbreviator
{
    
    /**
     * Maps tableIds with the number of occurrences in the query.
     */
    protected Map<Integer, Integer>  idCountMap;
    /**
     * Maps table trees with their assigned abbreviations.
     */
    protected Map<Integer, String> ttAbbrevMap;
    
    public TableAbbreviator()
    {
        idCountMap = new HashMap<Integer, Integer>();
        ttAbbrevMap = new HashMap<Integer, String>();
    }
    
    /**
     * @param tt
     * @return a unique (within the current query) sql/hql abbreviation or alias
     * for the table represented by tt.
     */
    public String getAbbreviation(final TableTree tt)
    {
        if (ttAbbrevMap.containsKey(tt.getTableInfo().getTableId()))
        {
            return ttAbbrevMap.get(tt.getTableInfo().getTableId());
        }
        //else
        if (idCountMap.containsKey(tt.getTableInfo().getTableId()))
        {
            return newAbbrev(tt, idCountMap.get(tt.getTableInfo().getTableId()) + 1);
        }
        //else
        return newAbbrev(tt, 0);
    }
    
    protected String newAbbrev(final TableTree tt, final Integer count)
    {
        idCountMap.put(tt.getTableInfo().getTableId(), count);
        String result = tt.getAbbrev() + count;
        ttAbbrevMap.put(tt.getTableInfo().getTableId(), result);
        return result;
    }
    
    public void clear()
    {
        idCountMap.clear();
        ttAbbrevMap.clear();
    }
}
