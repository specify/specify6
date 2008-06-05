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

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class QBResultReportServiceInfo extends Pair<String, String>
{
    /**
     * true if the report can be run directly from the current query results. False if record ids must be retrieved for results.
     */
    protected final boolean liveData;
    
    /**
     * the key for the report, if non-null then new connection is required. 
     */
    protected final Integer spReportId;
    
    /**
     * the appResourceId for the report
     */
    protected final Integer resourceId;
    
    /**
     * @param reportName 
     * @param fileName - the file for the report
     * @param liveData - true if the report uses the fields provided by the current QB results
     * @param spReportId - id of SpReport record if another query is required to generate report
     */
    public QBResultReportServiceInfo(final String reportName, final String fileName, final boolean liveData, final Integer spReportId,
                                     final Integer resourceId)
    {
        super(reportName, fileName);
        this.liveData = liveData;
        this.spReportId = spReportId;
        this.resourceId = resourceId;
    }   
    
    /**
     * @return name of report
     */
    public String getReportName()
    {
        return getFirst();
    }
    
    /**
     * @return name of the report file
     */
    public String getFileName()
    {
        return getSecond();
    }
    
    /**
     * @return liveData
     */
    public boolean isLiveData()
    {
        return liveData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Pair#toString()
     */
    @Override
    public String toString()
    {
         return getReportName();
    }

    /**
     * @return true if the report requires a connection provided by a query other than the query generating the current results.
     */
    public boolean isRequiresNewConnection()
    {
        return spReportId != null;
    }

    /**
     * @return the spReportId
     */
    public Integer getSpReportId()
    {
        return spReportId;
    }

    /**
     * @return the resourceId
     */
    public Integer getResourceId()
    {
        return resourceId;
    }
    
}
