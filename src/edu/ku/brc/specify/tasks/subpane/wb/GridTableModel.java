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

import java.awt.Image;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.IconManager;
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
            
    protected Workbench          workbench;
    protected boolean            isInImageMode    = false;
    protected ImageIcon          blankIcon = IconManager.getIcon("Blank", IconManager.IconSize.Std16);
    protected ImageIcon          imageIcon = IconManager.getIcon("CardImage", IconManager.IconSize.Std16);
    
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected WorkbenchTemplateMappingItem imageMappingItem = null;

    public GridTableModel(final Workbench    workbench, 
                          final Vector<WorkbenchTemplateMappingItem> headers)
    {
        super();
        this.workbench   = workbench;
        this.headers     = headers;
    }
    
    /**
     * Fires off a change notiication for the spreasheet. 
     */
    public void fireDataChanged()
    {
        fireTableDataChanged();
    }
    
    public boolean isInImageMode()
    {
        return isInImageMode;
    }

    public void setInImageMode(boolean isInImageMode)
    {
        if (!this.isInImageMode && isInImageMode)
        {
            if (imageMappingItem == null)
            {
                imageMappingItem = new WorkbenchTemplateMappingItem()
                {
                    @Override
                    public Class<?> getDataType()
                    {
                        return Image.class;
                    }
                };
                imageMappingItem.initialize();
                imageMappingItem.setCaption("Card Image"); // XXX I18N"
                imageMappingItem.setViewOrder((short)headers.size());
            }
            headers.add(imageMappingItem);
            
        } else if (this.isInImageMode && !isInImageMode)
        {
            headers.remove(imageMappingItem);
        }
        this.isInImageMode = isInImageMode;
        fireTableStructureChanged();
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
    @Override
    public String getColumnName(final int column)
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
        if (isInImageMode && column == headers.size() - 1)
        {
            WorkbenchRow rowObj = workbench.getRow(row);
            return rowObj.getCardImage() != null ? imageIcon : blankIcon;
        }
        
        if (getRowCount() > 0)
        {
            return workbench.getWorkbenchRowsAsList().get(row).getData(column);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        if (isInImageMode)
        {
            return column != headers.size() - 1;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (isInImageMode && columnIndex == headers.size() - 1)
        {
            return ImageIcon.class;
        }

        Object obj = getValueAt(0, columnIndex);
        if (obj != null)
        {
            return obj.getClass();
            
        }
        //else
        return String.class;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int row, int column)
    {
        if (isInImageMode && column == headers.size() - 1)
        {
            return;
        }
        
        if (getRowCount() >= 0)
        {
            
            workbench.getWorkbenchRowsAsList().get(row).setData(value.toString(), (short)column);
            fireDataChanged();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#appendRow()
     */
    @Override
    public void appendRow()
    {
        if (getRowCount() >= 0)
        {
            WorkbenchRow wbRow  = getRowCount() > 0 ? workbench.getWorkbenchRowsAsList().get(getRowCount()-1) : null;
            WorkbenchRow newRow = workbench.addRow();
            
            if (wbRow != null)
            {
                // Do Carry Forward
                for (WorkbenchTemplateMappingItem wbdmi : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
                {
                    if (wbdmi.getCarryForward())
                    {
                        newRow.setData(wbRow.getData( wbdmi.getViewOrder()), wbdmi.getViewOrder());
                    }
                }
            }
        }
        
        if (spreadSheet != null)
        {
            spreadSheet.addRow();
        }
        
        fireDataChanged();
        
        if (spreadSheet != null)
        {
            int lastRow = spreadSheet.getRowCount()-1;
            spreadSheet.scrollToRow(lastRow);
            spreadSheet.setRowSelectionInterval(lastRow, lastRow);
            spreadSheet.setColumnSelectionInterval(0, getColumnCount()-1);

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
                wbRow.setData("", (short)col);
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
            spreadSheet.setColumnSelectionInterval(0, getColumnCount()-1);
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
        int oldRowIndex = rowInx;
        int rowIndex    = rowInx;
        if (this.getRowCount() == -1)
        {
            if (this.getRowCount() > 0)
            {
                oldRowIndex = this.getRowCount()-1;
                rowIndex    = this.getRowCount();
            }
        }
        
        WorkbenchRow wbRow;
        WorkbenchRow newRow;
        if (rowIndex == -1)
        {
            wbRow  = null;
            newRow = workbench.insertRow((short)0);
            
        } else
        {
            wbRow  = workbench.getWorkbenchRowsAsList().get(oldRowIndex);
            newRow = workbench.insertRow((short)rowIndex);
        }
        
        // Do Carry Forward
        if (wbRow != null)
        {
            for (WorkbenchTemplateMappingItem wbdmi : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
            {
                if (wbdmi.getCarryForward())
                {
                    newRow.setData(wbRow.getData( wbdmi.getViewOrder()), wbdmi.getViewOrder());
                }
            }
        }

        if (spreadSheet != null)
        {
            spreadSheet.addRow();
        }
        
        fireDataChanged();
        
        if (spreadSheet != null)
        {
            spreadSheet.scrollToRow(rowInx);
            spreadSheet.setRowSelectionInterval(oldRowIndex, oldRowIndex);
            spreadSheet.setColumnSelectionInterval(0, getColumnCount()-1);
        }
    }
}