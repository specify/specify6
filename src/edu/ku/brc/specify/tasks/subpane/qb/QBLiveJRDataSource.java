/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
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
        return processValue(fldIdx, data.getCacheValueAt(row, fldIdx));
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
    
   
}
