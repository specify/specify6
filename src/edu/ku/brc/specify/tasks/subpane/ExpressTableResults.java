/* Filename:    $RCSfile: ExpressTableResults.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.tasks.subpane;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.tasks.ExpressResultsTableInfo;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.ResultSetTableModelDM;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where
 * supplied as an "in" clause.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressTableResults extends ExpressTableResultsBase implements SQLExecutionListener
{
    // Static Data Members
    private static final Logger log  = Logger.getLogger(ExpressTableResults.class);

    // Data Members
    protected SQLExecutionProcessor sqlExecutor;
    protected java.sql.ResultSet resultSet;

    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent
     * @param tableInfo the info describing the results
     * @param installServices indicates whether services should be installed
     */
    public ExpressTableResults(final ExpressSearchResultsPaneIFace esrPane,
                               final ExpressResultsTableInfo tableInfo,
                               final boolean installServices)
    {
        super(esrPane, tableInfo, installServices);

        sqlExecutor = new SQLExecutionProcessor(this, tableInfo.getViewSql());
        sqlExecutor.setAutoCloseConnection(false);
        sqlExecutor.start();

    }

    /**
     * Display the 'n' number of rows up to topNumEntries
     *
     * @param numRows the desired number of rows
     */
    protected void setDisplayRows(final int numRows, final int maxNum)
    {
        int rows = Math.min(numRows, maxNum);
        ResultSetTableModelDM rsm = (ResultSetTableModelDM)table.getModel();
        rsm.initializeDisplayIndexes();
        rsm.addDisplayIndexes(createIndexesArray(rows));

    }

    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
    {
        this.resultSet = resultSet;

        ResultSetTableModelDM rsm = new ResultSetTableModelDM(resultSet);
        table.setRowSelectionAllowed(true);
        int[] visCols = tableInfo.getDisplayColIndexes();
        if (visCols != null)
        {
             rsm.addDisplayColIndexes(visCols);
        }

        table.setModel(rsm);
        //colNames = tableInfo.getColNames();

        configColumnNames();

        rowCount = rsm.getRowCount();
        if (rowCount > topNumEntries)
        {
            buildMorePanel();
        }

        setDisplayRows(rowCount, topNumEntries);

        sqlExecutor = null;
        invalidate();
        doLayout();
        UICacheManager.forceTopFrameRepaint();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(final SQLExecutionProcessor process, final Exception ex)
    {
        sqlExecutor = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressTableResultsBase#getRecordSet(int[], int, boolean)
     */
    public RecordSet getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        ResultSetTableModelDM rsm = (ResultSetTableModelDM)table.getModel();
        log.debug("Row Selection Count["+table.getSelectedRowCount()+"]");
        return rsm.getRecordSet(table.getSelectedRows(), column, returnAll);
    }


}
