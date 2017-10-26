/* Copyright (C) 2017, University of Kansas Center for Research
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
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
public class QueryBatchEditServiceInfo extends ServiceInfo {

	
    public QueryBatchEditServiceInfo() {
		super(40, "QB_RESULT_BATCH_EDIT_SERVICE", 
                -1, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_BATCH_EDIT, 
                		null),
                ContextMgr.getTaskByClass(QueryTask.class),
                "CleanUp",
                UIRegistry.getResourceString("QB_RESULTS_BATCH_EDIT_TT"));
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.core.ServiceInfo#isAvailable(int)
	 */
	@Override
	public boolean isAvailable(int tableIdArg) {
		if (tableIdArg == -1) {
			return false;
		}
		//XXX security anyone?
		WorkbenchTask wbTask = (WorkbenchTask)ContextMgr.getTaskByClass(WorkbenchTask.class);
		return wbTask != null && wbTask.getUpdateSchemaForTable(tableIdArg) != null;
	}

	/**
	 * @param priority
	 * @param serviceName
	 * @param tableId
	 * @param command
	 * @param task
	 * @param iconName
	 * @param tooltip
	 */
	public QueryBatchEditServiceInfo(Integer priority, String serviceName, int tableId, CommandAction command,
			Taskable task, String iconName, String tooltip) {
		super(priority, serviceName, tableId, command, task, iconName, tooltip);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param priority
	 * @param serviceName
	 * @param tableId
	 * @param command
	 * @param task
	 * @param iconName
	 * @param tooltip
	 * @param isDefault
	 */
	public QueryBatchEditServiceInfo(Integer priority, String serviceName, int tableId, CommandAction command,
			Taskable task, String iconName, String tooltip, boolean isDefault) {
		super(priority, serviceName, tableId, command, task, iconName, tooltip, isDefault);
		// TODO Auto-generated constructor stub
	}

}
