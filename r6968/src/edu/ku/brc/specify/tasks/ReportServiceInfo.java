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
package edu.ku.brc.specify.tasks;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 */
public class ReportServiceInfo extends ServiceInfo 
{
    public ReportServiceInfo()
	{
		super(40, "QB_RESULT_REPORT_SERVICE", 
                -1, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_REPORT, 
                		null),
                ContextMgr.getTaskByClass(QueryTask.class),
                "Reports",
                UIRegistry.getResourceString("QB_RESULTS_REPORT_TT"));
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.ServiceInfo#isAvailable(int)
	 */
	@Override
	public boolean isAvailable(int tableIdArg) 
	{
		ReportsBaseTask repTask = (ReportsBaseTask )ContextMgr.getTaskByClass(ReportsTask.class);
		if (repTask == null) //probably impossible but...
		{
			return false;
		}
		
		return repTask.getReports(tableIdArg, null).size() > 0;
	}
    
    
}
