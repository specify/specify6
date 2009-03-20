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
public class ReportServiceInfo extends ServiceInfo 
{
    protected static final int		SRCHQIdRHQLTblId	= -123;

	protected final ReportsBaseTask	reportsTask;
    
    public ReportServiceInfo(final ReportsBaseTask reportsTask, int tableId)
	{
		super(40, "QB_RESULT_REPORT_SERVICE", 
                tableId,//SRCHQIdRHQLTblId, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_REPORT, 
                        //new SearchResultReportServiceCmdData(this, data)),
                		null),
                ContextMgr.getTaskByClass(QueryTask.class),
                "Reports",
                UIRegistry.getResourceString("QB_RESULTS_REPORT_TT"));
		this.reportsTask = reportsTask;
	}
	
}
