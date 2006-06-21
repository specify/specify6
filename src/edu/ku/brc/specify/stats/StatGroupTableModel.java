/*
 * Filename:    $RCSfile: StatGroupTableModel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:55 $
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
package edu.ku.brc.specify.stats;


import java.util.Vector;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class StatGroupTableModel extends AbstractTableModel
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(StatGroupTableModel.class);

    // Data Members
    protected Vector<StatDataItem> data        = new Vector<StatDataItem>();
    protected Vector<Class>        classNames  = new Vector<Class>();
    protected int                  currentRow  = 0;
    protected int                  numRows     = 0;
    protected String[]             columnNames;
    
    protected StatGroupTable       statGroupTable;
    

    /**
     * Construct 
     * @param statGroupTable needed so it can be notified when new data has arrived
     * @param columnNames the names to be used a column headers
     */
    public StatGroupTableModel(final StatGroupTable statGroupTable, final String[] columnNames)
    {
        this.statGroupTable = statGroupTable;
        this.columnNames = columnNames;
    }

    
    /**
     * Adds a StatDataItem
     * @param sdi the item
     */
    public void addDataItem(final StatDataItem sdi)
    {
        data.add(sdi);
        this.fireTableDataChanged();
        sdi.setTableModel(this);
    }
    
    /**
     * Returns a StatDataItem
     * @param index the index of the item to be returned
     */
    public StatDataItem getDataItem(final int index)
    {
        return data.get(index);
    }
    
    /**
     * Notifies the TableModel that the underlying data has changed
     */
    public void fireNewData()
    {
        this.fireTableDataChanged();
        if (statGroupTable != null)
        {
            statGroupTable.fireNewData();
        }
    }
    
    /**
     * Clears the model 
     */
    public void clear()
    {
        for (StatDataItem sdi : data)
        {
            sdi.clear();
        }
        data.clear();
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 2; // XXX Hard Coded for now becuse StatDataItem can only handle two columns
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass(int column)
    {
        
        Object val = null;
        if (column == 1 && data.size() > 0)
        {
            StatDataItem sdi = data.get(0);
            val = sdi.getValue();
        }
        return column == 0 ? String.class : (val == null ? String.class : val.getClass());
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int column)
    {
        return columnNames != null ? columnNames[column] : "";
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column)
    {
        if (row < data.size() && column < 2)
        {
            StatDataItem sdi = data.get(row);
            return column == 0 ? sdi.getDescription() : sdi.getValue();

            //return data.get(row);
        }
        return "";
    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int row, int column)
    {

    }


    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
         return data.size();
    }
}
