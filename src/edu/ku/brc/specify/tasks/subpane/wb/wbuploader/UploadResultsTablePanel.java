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
