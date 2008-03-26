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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.ServiceProviderIFace;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ERTICaptionInfo;

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
    protected String title;
    protected int    tableId;
    protected String iconName;
    protected SortedSet<SpReport> reports = new TreeSet<SpReport>(
            new Comparator<SpReport>()
            {
                public int compare(SpReport o1,
                                   SpReport o2)
                {
                    return o1.getName().compareTo(o2.getName());
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
                                  final List<?>   listOfIds)
    {
        super(null, bannerColor, searchTerm, listOfIds);
        this.title    = title;
        this.tableId  = tableId;
        this.iconName = iconName;
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
        if (reports.size() == 0)
        {
            //XXX if a query that has reports has been previously run  (and this method returns a ServiceInfo)
            //then even though this returns null, the results pane will have a Report Runner service button
            //because apparently services are being cached at some point??
            //If don't get rid of old services then definitely need to stop toolTip setup below.
            return null;
        }
        
        List<ServiceInfo> result = new LinkedList<ServiceInfo>();
        String toolTip;
        if (reports.size() == 1)
        {
            toolTip = String.format(UIRegistry.getResourceString("QB_RESULTS_ONE_REPORT_TT"), reports.first().getName());
        }
        else
        {
            toolTip = UIRegistry.getResourceString("QB_RESULTS_REPORT_TT");
        }
            
        result.add(new ServiceInfo("QB_RESULT_REPORT_SERVICE", 
                -1, 
                new CommandAction(QueryTask.QUERY, QueryTask.QUERY_RESULTS_REPORT, 
                        new QBResultReportServiceCmdData(this, data)),
                ContextMgr.getTaskByClass(QueryTask.class),
                "Reports",
                toolTip));
        return result;
    }
    
    /**
     * @param reports 
     * 
     * adds (and sorts) reports to this.reports.
     */
    public void setReports(Set<SpReport> reports)
    {
        this.reports.addAll(reports);
    }

    /**
     * @return the reports
     */
    public SortedSet<SpReport> getReports()
    {
        return reports;
    }
}