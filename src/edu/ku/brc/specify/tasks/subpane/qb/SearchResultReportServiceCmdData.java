/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class SearchResultReportServiceCmdData
{
    protected QueryForIdResultsHQL info;
    protected Object data;
    /**
     * @param info
     * @param data
     */
    public SearchResultReportServiceCmdData(QueryForIdResultsHQL info, Object data)
    {
        super();
        this.info = info;
        this.data = data;
    }
    /**
     * @return the info
     */
    public QueryForIdResultsHQL getInfo()
    {
        return info;
    }
    /**
     * @return the data
     */
    public Object getData()
    {
        return data;
    }
    
}
