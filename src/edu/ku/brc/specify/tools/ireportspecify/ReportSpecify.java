/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tools.ireportspecify;

import it.businesslogic.ireport.Report;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.subpane.SpJRIReportConnection;

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
    protected SpJRIReportConnection connection = null;
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
    public SpJRIReportConnection getConnection()
    {
        return connection;
    }
    /**
     * @param connection the connection to set
     */
    public void setConnection(SpJRIReportConnection connection)
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
