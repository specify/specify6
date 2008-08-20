/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.List;

import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadResultsTablePanel extends ESResultsTablePanel
{

    /**
     * @param esrPane
     * @param results
     * @param installServices
     * @param isExpandedAtStartUp
     */
    public UploadResultsTablePanel(final ExpressSearchResultsPaneIFace esrPane,
                               final QueryForIdResultsIFace    results,
                               final boolean                   installServices,
                               final boolean                   isExpandedAtStartUp)
    {
        super(esrPane, results, installServices, isExpandedAtStartUp);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#createModel()
     */
    @Override
    protected ResultSetTableModel createModel()
    {
        return new UploadResultSetTableModel(this, results);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#setTitleBar()
     */
    @Override
    protected void setTitleBar()
    {
        super.setTitleBar();
        UploadResults ur = (UploadResults )results;
        if (ur.getUploadedRecCount() > rowCount)
        {
            topTitleBar.setText(String.format("%s - %d/%d", results.getTitle(), rowCount, ur.getUploadedRecCount()));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#getServices()
     */
    @Override
    protected List<ServiceInfo> getServices()
    {
        // TODO Auto-generated method stub
        List<ServiceInfo> result =  super.getServices();
        for (int s = result.size()-1; s >= 0; s--)
        {
            ServiceInfo service = result.get(s);
            System.out.println(service.getName());
            if (!includeService(result.get(s)))
            {
                result.remove(s);
            }
            else if (result.get(s).getTask() instanceof DataEntryTask)
            {
                result.get(s).getCommandAction().setProperty("readonly", true);
            }
        }
        return result;
    }
    
    protected boolean includeService(final ServiceInfo service)
    {
        return !(service.getTask() instanceof RecordSetTask);
    }
    
}
