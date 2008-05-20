/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.ireportspecify;

import it.businesslogic.ireport.Report;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Extends iReport Report with association to a specify spAppResource.
 */
public class ReportSpecify extends Report
{
    protected AppResourceIFace appResource = null;
    protected QBJRDataSourceConnection connection = null;
    protected SpReport spReport = null;
    
    /**
     * 
     */
    public ReportSpecify()
    {
        super();
    }
    /**
     * @param appResource
     */
    public ReportSpecify(final AppResourceIFace appResource)
    {
        super();
        this.appResource = appResource;
    }

    /**
     * @param spReport
     */
    public ReportSpecify(final SpReport spReport)
    {
        super();
        this.spReport = spReport;
        if (this.spReport != null)
        {
            this.appResource = this.spReport.getAppResource();
        }
    }
    
    /**
     * @param appResource the appResouce to set
     */
    public void setAppResource(final AppResourceIFace appResource)
    {
        this.appResource = appResource;
    }

    /**
     * @return the appResource
     */
    public AppResourceIFace getAppResource()
    {
        return appResource;
    }

    /**
     * @return the connection
     */
    public QBJRDataSourceConnection getConnection()
    {
        return connection;
    }
    /**
     * @param connection the connection to set
     */
    public void setConnection(QBJRDataSourceConnection connection)
    {
        this.connection = connection;
    }
    /**
     * @return the spReport
     */
    public SpReport getSpReport()
    {
        return spReport;
    }

    /**
     * @param spReport the spReport to set
     */
    public void setSpReport(SpReport spReport)
    {
        this.spReport = spReport;
    }
    
    /**
     * @param appResource
     * @return true if this.appResource matches appResource.
     * 
     * Merely matching names for now.
     */
    public boolean resourceMatch(final AppResourceIFace appRes)
    {
        if (appResource != null)
        {
            return appResource.getName().equals(appRes.getName());
        }
        return false;
    }
}
