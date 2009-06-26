/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

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
@SuppressWarnings("serial")
public class QBResultsTablePanel extends ESResultsTablePanel
{
    public QBResultsTablePanel(final ExpressSearchResultsPaneIFace esrPane,
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
        return new QBResultSetTableModel(this, results);
    }
    
    /**
     * @return the tableModel
     */
    public QBResultSetTableModel getTableModel()
    {
    	return (QBResultSetTableModel )resultSetTableModel;
    }
}
