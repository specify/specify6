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

package edu.ku.brc.specify.core.subpane;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.ExpressResultsTableInfo;
import edu.ku.brc.specify.dbsupport.SQLExecutionListener;
import edu.ku.brc.specify.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.ui.CloseButton;
import edu.ku.brc.specify.ui.GradiantButton;
import edu.ku.brc.specify.ui.GradiantLabel;
import edu.ku.brc.specify.ui.TriangleButton;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where 
 * supplied as an "in" clause.
 * 
 * @author rods
 *
 */
class ExpressTableResults extends ExpressTableResultsBase implements SQLExecutionListener
{
    protected SQLExecutionProcessor sqlExecutor;

    
    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent 
     * @param title the title of the resulys
     * @param sqlStr the SQL string used to populate the results
     * @param colNameMappings the mappings for the column names
     */
    public ExpressTableResults(final ExpressSearchResultsPane esrPane, 
                               final ExpressResultsTableInfo tableInfo)
    {
        super(esrPane, tableInfo);
        
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
        ResultSetTableModel rsm = (ResultSetTableModel)table.getModel();
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
        ResultSetTableModel rsm = new ResultSetTableModel(resultSet);
        rsm.addDisplayIndexes(createIndexesArray(7)); // pre-initialize to reduce flash (not sure if this is working)
        
        table.setModel(rsm);
        table.setRowSelectionAllowed(true);
        
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


}
