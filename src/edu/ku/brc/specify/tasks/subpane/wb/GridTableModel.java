/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.tmanfe.SpreadSheetModel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2007
 *
 */
public class GridTableModel extends SpreadSheetModel
{
    private static final Logger log = Logger.getLogger(GridTableModel.class);
            

    protected Workbench   workbench;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();

    public GridTableModel(final Workbench  workbench, 
                          final Vector<WorkbenchTemplateMappingItem> headers)
    {
        super();
        
        this.workbench   = workbench;
        this.headers     = headers;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return headers.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int column)
    {
        if (headers != null)
        {
            String label = headers.get(column).getCaption();
            return label != null ? label : "";
        }
        log.error("columnList should not be null!");
        return "N/A";
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return workbench.getWorkbenchRows().size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column)
    {
        if (getRowCount() >= 0)
        {
            return workbench.getWorkbenchRowsAsList().get(row).getData(column);
        }
        return null;
    }

    public boolean isCellEditable(int row, int column)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass(int columnIndex)
    {
        Object obj = getValueAt(0, columnIndex);
        if (obj != null)
        {
            return obj.getClass();
            
        } else
        {
            return String.class;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object value, int row, int column)
    {
        if (getRowCount() >= 0)
        {
            workbench.getWorkbenchRowsAsList().get(row).setData(value.toString(), column);
            fireDataChanged();
        }
    }
    
    public void fireDataChanged()
    {
        fireTableDataChanged();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#appendRow()
     */
    @Override
    public void appendRow()
    {
        workbench.addRow();
        
        if (spreadSheet != null)
        {
            spreadSheet.addRow();
        }
        
        fireDataChanged();
        
        if (spreadSheet != null)
        {
            spreadSheet.scrollToRow(spreadSheet.getRowCount()-1);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#clearCells(int[], int[])
     */
    @Override
    public void clearCells(int[] rows, int[] cols)
    {
        for (int rowInx : rows)
        {
            WorkbenchRow wbRow = workbench.getRow(rowInx);
            for (int col : cols)
            {
                wbRow.setData("", col);
            }
        }
        fireDataChanged();
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#deleteRows(int[])
     */
    @Override
    public void deleteRows(int[] rows)
    {
        int currentRow = rows[0];
        for (int i=0;i<rows.length;i++)
        {
            workbench.deleteRow(currentRow);
            if (spreadSheet != null)
            {
                spreadSheet.removeRow(currentRow);
            }
        }
        fireDataChanged();
        
        int rowCount = workbench.getWorkbenchRowsAsList().size();
        if (spreadSheet != null && rowCount > 0)
        {
            if (currentRow >= rowCount)
            {
                spreadSheet.setRowSelectionInterval(rowCount-1, rowCount-1);
            } else
            {
                spreadSheet.setRowSelectionInterval(currentRow, currentRow);
            }
            spreadSheet.setColumnSelectionInterval(0, 6);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#fill(int, int, int[])
     */
    @Override
    public void fill(int colInx, int valueRowInx, int[] rowInxs)
    {
        Object value = getValueAt(valueRowInx, colInx);
        for (int rowInx : rowInxs)
        {
            setValueAt(value, rowInx, colInx);
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#insertRow(int)
     */
    @Override
    public void insertRow(int rowInx)
    {
        //WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(index);
        workbench.insertRow(rowInx);

        if (spreadSheet != null)
        {
            spreadSheet.addRow();
        }
        
        fireDataChanged();
        
        if (spreadSheet != null)
        {
            spreadSheet.scrollToRow(rowInx);
        }
        //setChanged(true);
    }

    
}