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

package edu.ku.brc.af.tasks.subpane;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import edu.ku.brc.af.tasks.ExpressResultsTableInfo;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.ui.UICacheManager;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where
 * supplied as an "in" clause.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressTableResultsHitsCache extends ExpressTableResultsBase
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(ExpressTableResultsHitsCache.class);

    // Data Members
    protected Hits hits;

    protected String[] rowCache      = null;
    protected int      rowCacheIndex = -1;
    protected int      numCols       = 0;
    protected int[]    indexes       = null;

    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent
     * @param tableInfo the info describing the results
     * @param installServices indicates whether services should be installed
     * @param hits the hits results
    */
    public ExpressTableResultsHitsCache(final ExpressSearchResultsPaneIFace esrPane,
                                        final ExpressResultsTableInfo tableInfo,
                                        final boolean installServices,
                                        final Hits hits)
    {
        super(esrPane, tableInfo, installServices);

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

        colLabels = tableInfo.getColLabels();

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

    /**
     * Returns a RecordSet object from the table
     * @param rows The array of indexes into the rows
     * @param column the column to get the indexes from
     * @param returnAll indicates whether all the records should be returned if nothing was selected
     * @return  a RecordSet object from the table
     */
    public RecordSet getRecordSet(final int[] rows, final int column, final boolean returnAll)
    {
        HitsTableModel hitsModel = (HitsTableModel)table.getModel();
        return hitsModel.getRecordSet(table.getSelectedRows(), column, returnAll);
    }


    //---------------------------------------------------
    //-- Table Model for Hit Results
    //---------------------------------------------------
    @SuppressWarnings("serial")
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
        public Class<?> getColumnClass(int column)
        {
            return String.class;
        }

        /**
         * Get the column name
         * @param column the column of the cell to be gotten
         */
        public String getColumnLabel(int column)
        {
            return colLabels[column];
        }

        public int getColumnCount()
        {
            return cols != null ? cols.length : colLabels.length;
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

        /**
         * Returns a RecordSet object from the table
         * @param rows the selected rows
         * @param column the col that contains the ID
         * @param returnAll indicates whether all the records should be returned if nothing was selected
         * @return Returns a RecordSet object from the table
         */
        public RecordSet getRecordSet(final int[] rows, final int column, final boolean returnAll)
        {
            RecordSet rs = new RecordSet();

            Set<RecordSetItem> items = new HashSet<RecordSetItem>();
            rs.setItems(items);

            try
            {
                if (rows == null || rows.length == 0)
                {
                    if (!returnAll)
                    {
                        return rs;
                    }

                    for (int i=0;i<hits.length();i++)
                    {
                        Document doc  = hits.doc(i);
                        RecordSetItem rsi = new RecordSetItem();
                        rsi.setRecordId(doc.get("id"));
                        items.add(rsi);
                    }
                } else
                {
                    for (int i=0;i<rows.length;i++)
                    {
                        Document doc  = hits.doc(indexes[rows[i]]);
                        //log.debug("["+doc.get("id")+"]["+doc.get("table")+"]["+doc.get("data")+"]");
                        RecordSetItem rsi = new RecordSetItem();
                        rsi.setRecordId(doc.get("id"));
                        items.add(rsi);
                    }
                }
            } catch (Exception ex)
            {
                // XXX ???
                ex.printStackTrace();
            }
            return rs;
        }

    }
}
