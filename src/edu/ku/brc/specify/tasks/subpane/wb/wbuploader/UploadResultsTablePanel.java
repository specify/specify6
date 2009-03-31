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

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
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
    //private static final Logger log = Logger.getLogger(UploadResultsTablePanel.class);

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
//    @Override
//    protected List<ServiceInfo> getServices()
//    {
//        List<ServiceInfo> result =  new Vector<ServiceInfo>();
//        return Uploader.getCurrentUpload.filterServices(super.getServices();
//        for (int s = services.size()-1; s >= 0; s--)
//        {
//            ServiceInfo service = services.get(s);
//            if (includeService(service))
//            {
//                if (service.getTask() instanceof DataEntryTask)
//                {
//                    try
//                    {
//                        ServiceInfo newService = (ServiceInfo )service.clone();
//                        newService.getCommandAction().setProperty("readonly", true);
//                        service = newService;
//                    }
//                    catch (CloneNotSupportedException ex)
//                    {
//                        log.error(ex);
//                        continue;
//                    }
//                }
//                result.add(service);
//            }
//        }
//        return result;
//    }
//    
//    protected boolean includeService(final ServiceInfo service)
//    {
//        return !(service.getTask() instanceof RecordSetTask);
//    }
    
}
