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

import java.util.Collections;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import edu.ku.brc.specify.core.ExpressResultsTableInfo;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where 
 * supplied as an "in" clause.
 * 
 * @author rods
 *
 */
class ExpressTableResultsHitsCache extends ExpressTableResultsBase
{
    protected Hits hits;
    
    protected String[] rowCache      = null;
    protected int      rowCacheIndex = -1;
    protected int      numCols       = 0;
    protected int[]    indexes       = null;
    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent 
     * @param title the title of the resulys
     * @param sqlStr the SQL string used to populate the results
     * @param colNameMappings the mappings for the column names
     */
    public ExpressTableResultsHitsCache(final ExpressSearchResultsPane esrPane, 
                                        final ExpressResultsTableInfo tableInfo,
                                        final Hits hits)
    {
        super(esrPane, tableInfo);
        
        this.hits = hits;
        indexes = tableInfo.getIndexes();
        fillTable();
    }
    
    /**
     * Display the 'n' number of rows up to topNumEntries
     * 
     * @param numRows the desired number of rows
     */
    protected void setDisplayRows(final int numRows, final int maxNum)
    {
        int rows = Math.min(numRows, maxNum);
        HitsTableModel hitsModel = (HitsTableModel)table.getModel();
        hitsModel.initializeDisplayIndexes();
        hitsModel.addDisplayIndexes(createIndexesArray(rows));      
    }
    
    /**
     * 
     */
    public void fillTable()
    {
        HitsTableModel hitsModel = new HitsTableModel();
        
        // Must be done before setting it into the table
        int[] visCols = tableInfo.getDisplayColIndexes();
        if (visCols != null)
        {
             hitsModel.setDisplayColIndexes(visCols);
        }
        
        colNames = tableInfo.getColNames();

        table.setModel(hitsModel);
        
        configColumnNames();
        
        numCols  = table.getModel().getColumnCount();
        
        rowCount = indexes.length;
        if (rowCount > topNumEntries)
        {
            buildMorePanel();
        }
        setDisplayRows(rowCount, topNumEntries);
        

        invalidate();
        doLayout();
        UICacheManager.forceTopFrameRepaint();    
        
    }
    
    //---------------------------------------------------
    //-- Table Model for Hit Results
    //---------------------------------------------------
    class HitsTableModel extends AbstractTableModel
    {
        private int[] displayIndexes = null;
        private int[] cols           = null;
        
        public HitsTableModel()
        {
            
        }
        
        /**
         * Returns the Class object for a column
         * @param column the column in question
         * @return the Class of the column
         */
        public Class getColumnClass(int column)
        {
            return String.class;
        }

        /**
         * Get the column name
         * @param column the column of the cell to be gotten
         */
        public String getColumnName(int column)
        {
            return colNames[column];
        }
        
        public int getColumnCount() 
        { 
            return cols != null ? cols.length : colNames.length; 
        }
        
        public int getRowCount()
        { 
            return displayIndexes != null ? displayIndexes.length : indexes.length;
        }
        
        public Object getValueAt(int row, int col) 
        { 
            String str = "";
            try
            {
                int inx = displayIndexes != null ? displayIndexes[row] : row;
                int mappedInx = indexes[inx];
                
                Document doc = hits.doc(mappedInx);
                String data = doc.get("data");
                if (col == 0 || row != rowCacheIndex)
                {
                    StringTokenizer st = new StringTokenizer(data, "\t");
                    int numTokens = st.countTokens();
                    if (rowCache == null || numTokens > rowCache.length)
                    {
                        rowCache = new String[numTokens];
                    }
                    for (int i=0;i<numTokens;i++)
                    {
                        rowCache[i] = st.nextToken();
                    }
                    rowCacheIndex = row;
                }
                return cols != null ? rowCache[cols[col]] : rowCache[col];
                
            } catch (Exception ex)
            {
                // XXX ???
                ex.printStackTrace();
            }
            return str;
        }
        
        /**
         * Initializes the display index data structure
         *
         */
        public void initializeDisplayIndexes()
        {
            displayIndexes = null;
        }
 
        /**
         * Sets the display indexes to display only a portion of the recordset
         * @param indexes the array of indexes
         */
        public void addDisplayIndexes(int[] indexes)
        {
            displayIndexes  = indexes;
            this.fireTableDataChanged();
        }
        
        /**
         * Sets the display columns
         * @param cols xxx
         */
        public void setDisplayColIndexes(int[] cols)
        {
            this.cols = cols;
            numCols = Math.min(numCols, cols.length);
        }
    }
    
    /**
     * Returns a RecordSet object from the table
      * @param rows
     * @param column
     * @return
     */
    public RecordSet getRecordSet(final int[] rows, final int column)
    {
        return null;
    }

}
