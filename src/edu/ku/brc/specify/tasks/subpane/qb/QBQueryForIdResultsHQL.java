/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.awt.Color;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.ServiceProviderIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.ReportsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class QBQueryForIdResultsHQL extends QueryForIdResultsHQL implements ServiceProviderIFace
{
    protected static final int                                QBQIdRHQLTblId = -123;

    protected final QueryBldrPane                             queryBuilder;
    protected final AtomicReference<Future<CustomQueryIFace>> queryTask      = new AtomicReference<Future<CustomQueryIFace>>();
    protected final AtomicReference<CustomQueryIFace>         query          = new AtomicReference<CustomQueryIFace>();
    protected final AtomicBoolean                             cancelled      = new AtomicBoolean(
                                                                                     false);
    protected List<Pair<String, Object>>                      params;
    protected String                                          title;
    protected int                                             tableId;
    protected String                                          iconName;

    protected final SortedSet<QBResultReportServiceInfo> reports = new TreeSet<QBResultReportServiceInfo>(
                new Comparator<QBResultReportServiceInfo>()
                {
                    public int compare(QBResultReportServiceInfo o1,
                                       QBResultReportServiceInfo o2)
                    {
                        return o1.getReportName().compareTo(o2.getReportName());
                    }

                });
    
    /**
     * @param bannerColor
     * @param title
     * @param iconName
     * @param tableId
     * @param searchTerm
     * @param listOfIds
     */
    public QBQueryForIdResultsHQL(final Color     bannerColor,
                                  final String    title,
                                  final String    iconName,
                                  final int       tableId,
                                  final String    searchTerm,
                                  final List<?>   listOfIds,
                                  final QueryBldrPane queryBuilder)
    {
        super(null, bannerColor, searchTerm, listOfIds);
        this.title    = title;
        this.tableId  = tableId;
        this.iconName = iconName;
        this.queryBuilder = queryBuilder;
        buildReports();
    }
    
    /**
     * @param list
     */
    public void setCaptions(List<ERTICaptionInfo> list)
    {
        this.captions = list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getDisplayOrder()
     */
    @Override
    public Integer getDisplayOrder()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getIconName()
     */
    @Override
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#buildCaptions()
     */
    @Override
    protected void buildCaptions()
    {
        //no.
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTableId()
     */
    @Override
    public int getTableId()
    {
        return tableId;
    }

    //--------------------------------------------------------------------
    // ServiceProviderIFace Interface
    //--------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ServiceProviderIFace#getServices()
     */
    public List<ServiceInfo> getServices(final Object data)
    {
        //Since only query results pane is shown at a time, can remove services from previous runs without messing anything else up.
        ContextMgr.removeServicesByTaskAndTable(ContextMgr.getTaskByClass(QueryTask.class), QBQIdRHQLTblId);    

        if (reports.size() == 0)
        {
            return null;
        }
        
        List<ServiceInfo> result = new LinkedList<ServiceInfo>();
        
        
        result.add(new ServiceInfo(40, "QB_RESULT_REPORT_SERVICE", 
                QBQIdRHQLTblId, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_REPORT, 
                        new QBResultReportServiceCmdData(this, data)),
                ContextMgr.getTaskByClass(QueryTask.class),
                "Reports",
                UIRegistry.getResourceString("QB_RESULTS_REPORT_TT")));
        return result;
    }
    
    public synchronized void reportDeleted(final Integer resourceId)
    {
        for (QBResultReportServiceInfo repInfo : reports)
        {
            if (repInfo.getResourceId() != null && repInfo.getResourceId().equals(resourceId))
            {
                reports.remove(repInfo);
                break;
            }
        }
    }
    
    public void buildReports()
    {
        reports.clear();
        boolean buildEm = true;
        if (UIHelper.isSecurityOn())
        {
            Taskable reportsTask = ContextMgr.getTaskByClass(ReportsTask.class);
            if (reportsTask != null)
            {
                buildEm = reportsTask.getPermissions().canView();
            }
        }
        if (buildEm)
        {
            //first add reports associated directly with the results
            if (queryBuilder != null)
            {
                for (SpReport rep : queryBuilder.getReportsForQuery())
                {
                    if (repContextIsActive(rep.getAppResource()))
                    {
                        reports.add(new QBResultReportServiceInfo(rep.getName(), rep.getName(), true, null, rep.getAppResource().getId(), rep.getRepeats()));
                    }
                }
            }
        
            if (tableId != -1)
            {
                //second add reports associated with the same table as the current results
                List<AppResourceIFace> reps = AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME);
                reps.addAll(AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME));
                for (AppResourceIFace rep : reps)
                {
                    String tblIdStr = rep.getMetaData("tableid");
                    if (tblIdStr != null)
                    {
                        if (tableId == Integer.valueOf(tblIdStr))
                        {
                            reports.add(new QBResultReportServiceInfo(rep.getDescription() /*'title' seems to be currently stored in description */,
                                    rep.getName() /* and filename in name */, false, null, ((SpAppResource)rep).getId(), null));
                        }
                        else
                        {
                            //third add reports based on other queries...
                            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                            try
                            {
                                SpReport spRep = (SpReport)session.getData("from SpReport where appResourceId = " + ((SpAppResource)rep).getId());
                                if (spRep != null && spRep.getQuery() != null && spRep.getQuery().getContextTableId().intValue() == tableId)
                                {
                                    reports
                                            .add(new QBResultReportServiceInfo(
                                                rep.getDescription() /*
                                                                         * 'title' seems to be
                                                                         * currently stored in
                                                                         * description
                                                                         */,
                                                rep.getName() /* and filename in name */, false, spRep.getId(),
                                                ((SpAppResource)rep).getId(), null));
                                }
                            }
                            finally
                            {
                                session.close();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param repResource
     * @returns true if repResource is currently available from the AppContextMgr.
     */
    protected boolean repContextIsActive(final AppResourceIFace repResource)
    {
        AppResourceIFace match = AppContextMgr.getInstance().getResource(repResource.getName());
        //XXX - Is it really safe to assume there there won't be more than one resource with the same name and mimeType?
        return match != null && match.getMimeType().equalsIgnoreCase(repResource.getMimeType());
    }
    /**
     * @return the reports
     */
    public SortedSet<QBResultReportServiceInfo> getReports()
    {
        return reports;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getParams()
     */
    @Override
    public List<Pair<String, Object>> getParams()
    {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(List<Pair<String, Object>> params)
    {
        this.params = params;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#cleanUp()
     */
    @Override
    public void complete()
    {
        if (queryBuilder != null)
        {
            queryBuilder.resultsComplete();
        }
        super.complete();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getQueryTask()
     */
    @Override
    public Future<?> getQueryTask()
    {
        return queryTask.get();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#setQueryTask(java.util.concurrent.Future)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setQueryTask(Future<?> queryTask)
    {
        this.queryTask.set((Future<CustomQueryIFace>)queryTask);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#queryTaskDone()
     */
    @Override
    public void queryTaskDone(final Object results)
    {
        try
        {
            query.set((CustomQueryIFace)results);
            if (cancelled.get())
            {
                query.get().cancel();
            }
            queryTask.set(null);
            queryBuilder.queryTaskDone();
            if (queryBuilder.isCountOnly())
            {
                query.get().cancel();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public CustomQueryIFace getQuery()
    {
        return query.get();
    }
    
    protected boolean isRetrieving()
    {
        return queryTask.get() != null && query.get() == null;
    }
    
    protected boolean isDisplaying()
    {
        return !isRetrieving() && query.get() != null;
    }
    
    public void cancel()
    {
        if (isRetrieving())
        {
            cancelled.set(true);
            queryTask.get().cancel(true);
            //and what else needs to be done???....
        }
        else if (isDisplaying())
        {
            cancelled.set(true);
            query.get().cancel(); 
        }
        //else do nuthin.
    }

    /**
     * @return the cancelled
     */
    public boolean getCancelled()
    {
        return cancelled.get();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#setRecIds(java.util.Vector)
     */
    @Override
    public void setRecIds(final Vector<Integer> ids)
    {
        //Could also fill recIds in queryTaskDone method, but would entail an extra pass through the CustomQueryIFace results,
        //and an extra list of ids. 
        
        //don't think it is necessary to copy the ids?
        this.recIds = ids;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#showProgress()
     */
    @Override
    public boolean showProgress()
    {
        //QueryBuilder sets up progress bar and status messages for itself.
        return false;
    }
}
