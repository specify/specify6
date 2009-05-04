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

import java.util.Collections;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Provides data from a already executed and displayed QB query.
 */
public class QBLiveJRDataSource extends QBJRDataSourceBase
{
    protected static final Logger log = Logger.getLogger(QBLiveJRDataSource.class);
    /**
     * pre-computed 'live' data.
     */
    protected final ResultSetTableModel data;
    /**
     * index of current row in data.
     */
    protected int row = -1;
    
    /**
     * Array of selected row numbers.
     */
    protected int[] selectedRows = null;
    /**
     * index of current selected row.
     */
    protected int selectedIdx = -1;
    
    /**
     * @param data
     * @param columnInfo
     */
    public QBLiveJRDataSource(final ResultSetTableModel data, final List<ERTICaptionInfoQB> columnInfo)
    {
        //XXX setting rowIds to true doesn't guarantee that rowIds will be available if Select Distinct was used
        super(columnInfo, true, null);
        this.data = data;
        if (data != null)
        {
            selectedRows = data.getParentERTP().getSelectedRows();
            if (selectedRows.length == 0)
            {
                selectedRows = null;
            }
        }
    }

    public QBLiveJRDataSource(final ResultSetTableModel data, final List<ERTICaptionInfoQB> columnInfo, final Object repeats)
    {
        //XXX setting rowIds to true doesn't guarantee that rowIds will be available if Select Distinct was used
        super(columnInfo, true, repeats);
        this.data = data;
        if (data != null)
        {
            selectedRows = data.getParentERTP().getSelectedRows();
            if (selectedRows.length == 0)
            {
                selectedRows = null;
            }
        }
    }
 
    /**
     * @return the number of records to be printed.
     */
    protected int getRowCount()
    {
        return selectedRows == null ? data.getRowCount() : selectedRows.length;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    @Override
    public Object getFieldValue(JRField arg0) throws JRException
    {
        //XXX - what if user-defined 'resultsetsize' field exists???
        if (arg0.getName().equalsIgnoreCase("resultsetsize"))
        {
        	return String.valueOf(getRowCount()); //currently returned as a string for convenience.
        }
        int fldIdx = getFldIdx(arg0.getName());
        if (fldIdx < 0)
        {
            if (arg0.getClass().equals(String.class))
            {
         	   return String.format(UIRegistry.getResourceString("QBJRDS_UNKNOWN_FIELD"), arg0.getName());
            }
            log.error("field not found: " + arg0.getName());
            return null;
        }
        int colInfoIdx = Collections.binarySearch(colNames, new SourceColumnInfo(arg0.getName(), null, null), srcColNameComparator);
        return processValue(colNames.get(colInfoIdx).getColInfoIdx(), data.getCacheValueAt(row, fldIdx));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#setUpCollNames()
     */
    @Override
    protected void setUpColNames()
    {
    	setUpColNamesPostProcess();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getNext()
     */
    @Override
    protected boolean getNext()
    {
        if (selectedRows == null)
        {
            return ++row < data.getRowCount();
        }
        if (++selectedIdx < selectedRows.length)
        {
            row = selectedRows[selectedIdx];
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getRepeaterRowVals()
     */
    @Override
    protected Object[] getRepeaterRowVals()
    {
        Object[] rowData = new Object[colNames.size()];
        for (int c = 0; c < colNames.size(); c++)
        {
            rowData[c] = data.getCacheValueAt(row, c);
        }
        return rowData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getRecordId()
     */
    @Override
    public Object getRecordId()
    {
        if (!recordIdsIncluded)
        {
            return super.getRecordId();
        }
        return data.getRowId(row);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#hasResultSize()
     */
    @Override
    public boolean hasResultSize()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#size()
     */
    @Override
    public int size()
    {
        //this does not take repeats into account.
        return getRowCount();
    }    
    
    
}
