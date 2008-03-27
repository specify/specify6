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


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class QBResultReportServiceCmdData
{
    protected QBQueryForIdResultsHQL info;
    protected Object data;
    /**
     * @param info
     * @param data
     */
    public QBResultReportServiceCmdData(QBQueryForIdResultsHQL info, Object data)
    {
        super();
        this.info = info;
        this.data = data;
    }
    /**
     * @return the info
     */
    public QBQueryForIdResultsHQL getInfo()
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
