/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.util.concurrent.atomic.AtomicReference;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class QBResultsSubPane extends ESResultsSubPane
{
    protected AtomicReference<QBResultsTablePanel> resultsTable = new AtomicReference<QBResultsTablePanel>(null);
    
	public QBResultsSubPane(final String name,
                            final Taskable task,
                            final boolean includeExplainPane)
    {
        super(name, task, includeExplainPane);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsSubPane#createResultsTable(edu.ku.brc.af.ui.db.QueryForIdResultsIFace)
     */
    @Override
    protected ESResultsTablePanel createResultsTable(QueryForIdResultsIFace results)
    {
    	resultsTable.set(new QBResultsTablePanel(this, results, results.shouldInstallServices(), results.isExpanded()));
    	return resultsTable.get();
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getHelpTarget()
	 */
	@Override
	public String getHelpTarget() 
	{
		return "QB_Results";
	}

	public QBResultsTablePanel getResultsTable() 
	{
		return resultsTable.get();
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.ESResultsSubPane#aboutToShutdown()
	 */
	@Override
	public boolean aboutToShutdown() 
	{
		boolean result = super.aboutToShutdown();
		if (result)
		{
			QBResultSetTableModel tblModel = resultsTable.get().getTableModel();
			if (tblModel != null)
			{
				tblModel.cancelBackgroundLoads();
			}

		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.ESResultsSubPane#createLayoutManager()
	 */
	@Override
	protected LayoutManager createLayoutManager() {
		return new BorderLayout();
	}
}
