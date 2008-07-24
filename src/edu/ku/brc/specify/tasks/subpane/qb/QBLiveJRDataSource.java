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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ERTICaptionInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Provides data from a already executed and displayed QB query.
 */
public class QBLiveJRDataSource extends QBJRDataSourceBase
{
    /**
     * pre-computed 'live' data.
     */
    protected final ResultSetTableModel data;
    /**
     * index of current row in data.
     */
    protected int row = -1;
    
    /**
     * @param data
     * @param columnInfo
     */
    public QBLiveJRDataSource(final ResultSetTableModel data, final List<ERTICaptionInfo> columnInfo)
    {
        //XXX setting rowIds to true doesn't guarantee that rowIds will be available if Select Distinct was used
        super(columnInfo, true, null);
        this.data = data;
    }

    public QBLiveJRDataSource(final ResultSetTableModel data, final List<ERTICaptionInfo> columnInfo, final Object repeats)
    {
        //XXX setting rowIds to true doesn't guarantee that rowIds will be available if Select Distinct was used
        super(columnInfo, true, repeats);
        this.data = data;
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
        	return String.valueOf(data.getRowCount()); //currently returned as a string for convenience.
        }
        int fldIdx = getFldIdx(arg0.getName());
        if (fldIdx < 0)
        {
            return String.format(UIRegistry.getResourceString("QBJRDS_UNKNOWN_FIELD"), arg0.getName());
        }
        return processValue(fldIdx, data.getCacheValueAt(row, fldIdx));
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getNext()
     */
    @Override
    protected boolean getNext()
    {
        return ++row < data.getRowCount();
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
