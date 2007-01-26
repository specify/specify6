/* This library is free software; you can redistribute it and/or
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

import java.sql.SQLException;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ExpressSearchResults;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.db.ResultSetTableModelDM;
import edu.ku.brc.ui.UICacheManager;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where
 * supplied as an "in" clause.
 *
 * 
 * @code_status Code Freeze
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
    protected java.sql.ResultSet    resultSet;

    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent
     * @param tableInfo the info describing the results
     * @param installServices indicates whether services should be installed
     */
    public ExpressTableResults(final ExpressSearchResultsPaneIFace esrPane,
                               final ExpressSearchResults          results,
                               final boolean                       installServices)
    {
        super(esrPane, results, installServices);
        
        Vector<Long> recIds = results.getRecIds();
        StringBuffer idsStr = new StringBuffer(recIds.size()*8);
        for (int i=0;i<recIds.size();i++)
        {
            if (i > 0) idsStr.append(',');
            idsStr.append(recIds.elementAt(i).toString());
        }
        
        String sqlStr;
        if (results.getJoinColTableId() != null)
        {
            sqlStr = results.getTableInfo().getUpdateSql(results.getJoinColTableId());
            sqlStr = String.format(sqlStr, new Object[] {idsStr.toString()});
            
        } else
        {
            String vsql = tableInfo.getViewSql();
            sqlStr = idsStr.length() > 0 ? vsql.replace("%s", idsStr.toString()) : vsql;
        }

        log.debug("["+sqlStr+"]");
        if (StringUtils.isNotEmpty(sqlStr))
        {
            sqlExecutor = new SQLExecutionProcessor(this, sqlStr);
            sqlExecutor.setAutoCloseConnection(false);
            sqlExecutor.start();
        }

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
    
    
    /**
     * Cleans up references to other objects.
     */
    public void cleanUp()
    {
        super.cleanUp();
        
        resultSet   = null;
        sqlExecutor = null;
    }

    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSetArg)
    {
        this.resultSet = resultSetArg;

        try
        {
            hasResults = resultSetArg.first();
            if (hasResults)
            {
                esrPane.addTable(this);
                
                ResultSetTableModelDM rsm = new ResultSetTableModelDM(resultSet);
                table.setRowSelectionAllowed(true);
                
                int[] visCols = tableInfo.getDisplayColIndexes();
                if (visCols != null)
                {
                     rsm.addDisplayColIndexes(visCols);
                }
                rsm.setColumnNames(tableInfo.getColLabels());
        
                table.setModel(rsm);
        
                configColumns();
        
                rowCount = rsm.getRowCount();
                if (rowCount > topNumEntries+2)
                {
                    buildMorePanel();
                    setDisplayRows(rowCount, topNumEntries);
                }
                else
                {
                	setDisplayRows(rowCount,Integer.MAX_VALUE);
                }
        
                sqlExecutor = null;
                invalidate();
                doLayout();
                UICacheManager.forceTopFrameRepaint();
            }
        } catch (SQLException ex)
        {
            log.error(ex);
            hasResults = false;
        }
           
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
    public RecordSetIFace getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        ResultSetTableModelDM rsm = (ResultSetTableModelDM)table.getModel();
        log.debug("Row Selection Count["+table.getSelectedRowCount()+"]");
        return rsm.getRecordSet(table.getSelectedRows(), column, returnAll);
    }


}
