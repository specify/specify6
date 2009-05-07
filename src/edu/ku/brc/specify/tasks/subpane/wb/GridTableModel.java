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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.tmanfe.SpreadSheetModel;

/**
 * The Model for the Spreadsheet for the Workbench and it can add a new image column to the mode when it is needed.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Mar 8, 2007
 *
 */
@SuppressWarnings("serial")
public class GridTableModel extends SpreadSheetModel
{
    private static final Logger log = Logger.getLogger(GridTableModel.class);
            
    protected Workbench          workbench;
    protected boolean            batchMode        = false;
    protected boolean            isInImageMode    = false;
    protected boolean            isUserEdit       = true;
    protected ImageIcon          blankIcon = IconManager.getIcon("Blank", IconManager.IconSize.Std16);
    protected ImageIcon          imageIcon = IconManager.getIcon("CardImage", IconManager.IconSize.Std16);
    
    protected Vector<WorkbenchTemplateMappingItem> headers          = new Vector<WorkbenchTemplateMappingItem>();
    protected WorkbenchTemplateMappingItem         imageMappingItem = null;

    /**
     * @param workbench
     */
    public GridTableModel(final Workbench workbench)
    {
        super();
        setWorkbench(workbench);
    }
    
    /**
     * Sets in a new Workbench.
     * @param workbench the new wb
     */
    public void setWorkbench(final Workbench workbench)
    {
        this.workbench = workbench;
        
        // Make the new Header List
        headers.clear();
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        if (imageMappingItem != null)
        {
            headers.add(imageMappingItem);
        }
    }
    
    /**
     * Fires off a change notiication for the spreasheet. 
     */
    public void fireDataChanged()
    {
        fireTableDataChanged();
    }
    
    /**
     * @return whether it is in image mode or not.
     */
    public boolean isInImageMode()
    {
        return isInImageMode;
    }

    /**
     * Sets the model into image mode so it can add a the image column to the spreadsheet.
     * @param isInImageMode
     */
    public void setInImageMode(boolean isInImageMode)
    {
        if (!this.isInImageMode && isInImageMode)
        {
            if (imageMappingItem == null)
            {
                imageMappingItem = new WorkbenchTemplateMappingItem()
                {
                    @Override
                    public String getFieldName()
                    {
                        return "Image";
                    }

                };
                imageMappingItem.initialize();
                imageMappingItem.setCaption(UIRegistry.getResourceString("WB_IMAGE"));
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
        return workbench != null ? workbench.getWorkbenchRows().size() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column)
    {
        // if this is the image column...
        if (isInImageMode && column == headers.size() - 1)
        {
            WorkbenchRow rowObj = workbench.getRow(row);
            Set<WorkbenchRowImage> images = rowObj.getWorkbenchRowImages();
            if (images != null && images.size() > 0)
            {
                StringBuilder sb = new StringBuilder();
                for (WorkbenchRowImage rowImg: images)
                {
                    String fullPath = rowImg.getCardImageFullPath();
                    String filename = FilenameUtils.getName(fullPath);
                    sb.append(filename + "; ");
                }
                sb.delete(sb.length()-2,sb.length());
                return sb.toString();
            }
            // else
            return "";
        }
        
        // otherwise...
        if (getRowCount() > row)
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
//        if (isInImageMode && columnIndex == headers.size() - 1)
//        {
//            return ImageIcon.class;
//        }

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
            String currentValue = workbench.getWorkbenchRowsAsList().get(row).getData(column);
            boolean changed = !StringUtils.equals(currentValue, value.toString());
            if (changed)
            {
            	workbench.getWorkbenchRowsAsList().get(row).setData(value.toString(), (short)column, isUserEdit);
            	if (!batchMode)
            	{
            		fireTableCellUpdated(row, column);
            	}
            }
        }
    }

    
	/**
     * @param value
     * @param row
     * @param column
     * @param isUserEdit
     */
    public void setValueAt(Object value, int row, int column, boolean isUserEdit)
    {
        //Right now, isUserEdit is only false if a GeoRefConversion is responsible for the setValueAt() call.
        this.isUserEdit = isUserEdit;
        try
        {
            setValueAt(value, row, column);
        }
        finally
        {
            this.isUserEdit = true;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#appendRow()
     */
    @Override
    public void appendRow()
    {
        int selInx = spreadSheet.getSelectedRow();
        if (selInx == -1)
        {
            selInx = spreadSheet.getEditingRow();
        }
        
        addRowAt(getRowCount(), selInx);
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
                wbRow.setData("", (short)col, true);
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
        Arrays.sort(rows);
        for (int i=rows.length-1;i>-1;i--)
        {
            log.info("Deleting Row Index "+rows[i]+" Row Count["+getRowCount()+"]");
            workbench.deleteRow(rows[i]);
            if (spreadSheet != null)
            {
                spreadSheet.removeRow(rows[i], rows.length == 1);
            }
        }
        fireDataChanged();
        
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
        int rowIndex    = rowInx == -1 ? this.getRowCount() : rowInx;
        if (this.getRowCount() == -1)
        {
            if (this.getRowCount() > 0)
            {
                oldRowIndex = this.getRowCount()-1;
                rowIndex    = this.getRowCount();
            } else
            {
                rowIndex = -1;
            }
        }
        addRowAt(rowIndex, oldRowIndex);
    }
    
    /**
     * INserts a row after this index.
     * @param rowInx the index to be insertsed after
     */
    public void insertAfterRow(int rowInx)
    {
        if (rowInx == -1)
        {
            throw new RuntimeException("Why is rowInx == -1?");
        }
        
        int oldRowIndex = rowInx;
        int rowIndex    = rowInx + 1;
        
        if (rowIndex > this.getRowCount())
        {
            rowIndex = this.getRowCount();
        }
        addRowAt(rowIndex, oldRowIndex);
    }
  
    
    /**
     * Add the row at an index and copy any Carry Forward Values.
     * @param rowIndex the index to add at
     * @param oldRowIndex the index to copy from
     */
    protected void addRowAt(final int rowIndex, final int oldRowIndex)
    {
        WorkbenchRow wbRow  = null;
        if (oldRowIndex > -1 && getRowCount() > 0)
        {
            wbRow  = workbench.getWorkbenchRowsAsList().get(oldRowIndex);
        }
        
        WorkbenchRow newRow;
        if (rowIndex == -1 || rowIndex == getRowCount())
        {
            newRow = workbench.addRow();
            
        } else
        {
            newRow = workbench.insertRow((short)rowIndex);
        }
        
        // Do Carry Forward
        if (wbRow != null)
        {
            for (WorkbenchTemplateMappingItem wbdmi : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
            {
                if (wbdmi.getCarryForward())
                {
                    newRow.setData(wbRow.getData( wbdmi.getViewOrder()), wbdmi.getViewOrder(), true);
                }
            }
        }

        if (spreadSheet != null)
        {
            spreadSheet.addRow();
        }
        
        fireDataChanged();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#getColDataLen(int)
     */
    @Override
    public int getColDataLen(final int column)
    {
        return headers.get(column).getDataFieldLength();
    }
    
    /**
     * Cleans up references.
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        
        if (headers != null)
        {
            headers.clear();
        }
        
        workbench        = null;
        headers          = null;
        imageMappingItem = null;
    }
    
    /**
     * @param column
     * @return mapping for column
     */
    public WorkbenchTemplateMappingItem getColMapping(int column)
    {
    	return headers.get(column);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#isBatchMode()
	 */
	@Override
	public boolean isBatchMode()
	{
		return batchMode;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.tmanfe.SpreadSheetModel#setBatchMode(boolean)
	 */
    /**
     * Caller must take responsibility clearing this flag and
     * calling fireTableChanged or other necessary methods
     * when batch operation is completed.
     */
	@Override
	public void setBatchMode(boolean value)
	{
		batchMode = value;
	}
}
