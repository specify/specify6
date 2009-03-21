/**
 * 
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
//XXX Turns out this class is unnecessary. Dump It.
public class ReportServiceInfo extends ServiceInfo 
{
    public ReportServiceInfo(int tableId)
	{
		super(40, "QB_RESULT_REPORT_SERVICE", 
                tableId,//SRCHQIdRHQLTblId, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_REPORT, 
                		null),
                ContextMgr.getTaskByClass(QueryTask.class),
                "Reports",
                UIRegistry.getResourceString("QB_RESULTS_REPORT_TT"));
	}
}
