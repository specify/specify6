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

import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Creates an UploadResultsQuery and executes it.
 *
 */
public class UploadResultSetTableModel extends ResultSetTableModel
{

    /**
     * @param parentERTP
     * @param results
     */
    public UploadResultSetTableModel(final ESResultsTablePanelIFace parentERTP,
                                     final QueryForIdResultsIFace results)
    {
        super(parentERTP, results);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.ResultSetTableModel#startDataAquisition(boolean)
     */
    @Override
    protected void startDataAquisition(boolean doSequentially)
    {
        setPropertyListener(this.parentERTP);
        UploadResults uploadResults = (UploadResults )results;
        UploadResultsQuery q = new UploadResultsQuery(uploadResults.getUploadTable(), uploadResults.getUploadData());
        q.execute(this);
    }

}
