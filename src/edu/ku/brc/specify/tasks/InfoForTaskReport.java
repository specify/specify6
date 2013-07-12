/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Stores info about reports.
 */
public class InfoForTaskReport extends Pair<SpAppResource, SpReport> implements Comparable<InfoForTaskReport>
{
    /**
     * @param appResource
     * @param report
     */
    public InfoForTaskReport(final SpAppResource spAppResource, final SpReport spReport)
    {
        super(spAppResource, spReport);
    }
    
    /**
     * @return the appResource
     */
    public SpAppResource getSpAppResource()
    {
        return getFirst();
    }
    
    /**
     * @return the spReport
     */
    public SpReport getSpReport()
    {
        return getSecond();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Pair#toString()
     */
    @Override
    public String toString()
    {
        return getSpAppResource().getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(InfoForTaskReport o)
    {
        return getSpAppResource().getName().compareTo(o.getSpAppResource().getName());
    }
}
