/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class SearchResultReportServiceInfo extends Pair<String, String>
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
     * repeat setting for the repeat.
     */
    protected final Object repeats;
    
    /**
     * @param reportName 
     * @param fileName - the file for the report
     * @param liveData - true if the report uses the fields provided by the current QB results
     * @param spReportId - id of SpReport record if another query is required to generate report
     */
    public SearchResultReportServiceInfo(final String reportName, final String fileName, final boolean liveData, final Integer spReportId,
                                     final Integer resourceId, final Object repeats)
    {
        super(reportName, fileName);
        this.liveData = liveData;
        this.spReportId = spReportId;
        this.resourceId = resourceId;
        this.repeats = repeats;
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

    /**
     * @return the repeats
     */
    public Object getRepeats()
    {
        return repeats;
    }
    
}
