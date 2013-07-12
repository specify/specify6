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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.QueryExecutor;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

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
        super(parentERTP, results, false, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.ResultSetTableModel#startDataAquisition(boolean)
     */
    @Override
    protected void startDataAquisition(boolean doSequentiallyArg)
    {
        setPropertyListener(this.parentERTP);
        UploadResults uploadResults = (UploadResults )results;
        UploadResultsQuery q = new UploadResultsQuery(this, uploadResults.getUploadTable(), uploadResults.getUploadData());
        results.setQueryTask(QueryExecutor.getInstance().executeQuery(q));
    }

}
