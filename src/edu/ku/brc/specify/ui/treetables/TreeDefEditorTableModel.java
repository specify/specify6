/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.util.RankBasedComparator;

/**
 * An implementation of {@link TableModel} for use in showing {@link JTable}s that present
 * a related set of {@link TreeDefItemIface} objects.
 *
 * @code_status Beta
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeDefEditorTableModel <I extends TreeDefItemIface<?,?,I>>
										extends AbstractTableModel
{
	public static final int NAME_COL        = 0;
	public static final int REMARKS_COL     = 1;
	public static final int FULLNAME_COL    = 2;
	public static final int ENFORCED_COL    = 3;
    public static final int TEXT_BEFORE_COL = 4;
    public static final int TEXT_AFTER_COL  = 5;
	
	/** A Vector of TreeDefItemIface objects holding the table data. */
	protected Vector<I> tableData;
	
	/**
     * Creates a new TreeDefEditorTableModel from the given set of def items.
     * 
	 * @param defItems the def items for the corresponding tree definition
	 */
	public TreeDefEditorTableModel(Set<? extends I> defItems)
	{
		tableData = new Vector<I>(defItems);
		Collections.sort(tableData,new RankBasedComparator());
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 6;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return tableData.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		I row = tableData.get(rowIndex);
		switch(columnIndex)
		{
			case NAME_COL:
			{
				return row.getName();
			}
			case REMARKS_COL:
			{
				return row.getRemarks();
			}
			case FULLNAME_COL:
			{
				return row.getIsInFullName();
			}
			case ENFORCED_COL:
			{
				return row.getIsEnforced();
			}
            case TEXT_BEFORE_COL:
            {
                return row.getTextBefore();
            }
            case TEXT_AFTER_COL:
            {
                return row.getTextAfter();
            }
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		I row = tableData.get(rowIndex);
		switch(columnIndex)
		{
			case NAME_COL:
			{
				row.setName((String)aValue);
				break;
			}
			case REMARKS_COL:
			{
				row.setRemarks((String)aValue);
				break;
			}
			case FULLNAME_COL:
			{
				row.setIsInFullName((Boolean)aValue);
				break;
			}
			case ENFORCED_COL:
			{
				row.setIsEnforced((Boolean)aValue);
				break;
			}
            case TEXT_BEFORE_COL:
            {
                row.setTextBefore((String)aValue);
                break;
            }
            case TEXT_AFTER_COL:
            {
                row.setTextAfter((String)aValue);
                break;
            }
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch(columnIndex)
		{
			case NAME_COL:
			case REMARKS_COL:
            case TEXT_BEFORE_COL:
            case TEXT_AFTER_COL:
			{
				return String.class;
			}
			case FULLNAME_COL:
			case ENFORCED_COL:
			{
				return Boolean.class;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column)
	{
		switch(column)
		{
			case NAME_COL:
			{
				return "Name";
			}
			case REMARKS_COL:
			{
				return "Remarks";
			}
			case FULLNAME_COL:
			{
				return "In Full Name";
			}
			case ENFORCED_COL:
			{
				return "Enforced";
			}
            case TEXT_BEFORE_COL:
            {
                return "Text Before";
            }
            case TEXT_AFTER_COL:
            {
                return "Text After";
            }
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		if( tableData.get(rowIndex).getParent() == null )
		{
			if( columnIndex == ENFORCED_COL )
			{
                // don't allow editing of the "isEnforced" field in the root tree def item 
				return false;
			}
			return true;
		}
		return true;
	}

	/**
     * Adds a new row to the table model.
     * 
	 * @see java.util.Vector#add(int, Object)
     * @param index the location of the new row
	 * @param element the new row data
	 */
	public void add(int index, I element)
	{
		tableData.add(index,element);
		fireTableRowsInserted(index,index);
	}

	/**
     * Adds a new row to the end of the table model.
     * 
     * @see java.util.Vector#add(Object)
	 * @param o the new row data
	 */
	public void add(I o)
	{
		tableData.add(o);
		int index = tableData.indexOf(o);
		fireTableRowsInserted(index,index);
	}

	/**
     * Returns the index-th row of the table model as a TreeDefItemIface instance.
     * 
     * @see java.util.Vector#get(int)
	 * @param index the row index
	 * @return the TreeDefItemIface representing the row's data
	 */
	public I get(int index)
	{
		return tableData.get(index);
	}

	/**
     * Returns the index of the given Object as a row in the table model.
     * 
     * @see java.util.Vector#indexOf(Object)
	 * @param elem the row data object
	 * @return the index of the object in the table model
	 */
	public int indexOf(Object elem)
	{
		return tableData.indexOf(elem);
	}

	/**
     * Removes the given row from the table model.
     * 
     * @throws ArrayIndexOutOfBoundsException if the index is out of range ( index < 0 || index >= size())
     * @see java.util.Vector#remove(int)
	 * @param index the row index
	 * @return the removed row
	 */
	public I remove(int index)
	{
		I deleted = tableData.remove(index);
		fireTableRowsDeleted(index,index);
		return deleted;
	}

	/**
     * Removes the given row from the table model.
     * 
     * @see java.util.Vector#remove(Object)
	 * @param o the row data object
	 * @return true if the data object was in the table model, false otherwise
	 */
	public boolean remove(Object o)
	{
		int index = tableData.indexOf(o);
		boolean deleted = tableData.remove(o);
		
		if(deleted)
		{
			fireTableRowsDeleted(index,index);
		}
		return deleted;
	}
}
