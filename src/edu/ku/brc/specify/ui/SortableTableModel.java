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

package edu.ku.brc.specify.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * A generic tableModel that does the sorting and is aggregated with the actual model. The Sorted model keeps an array 
 * for mapping the sorted indexes into the actual model.
 *  
 * @author rods
 *
 */
public class SortableTableModel implements TableModel, TableModelListener
{

    protected EventListenerList listenerList = new EventListenerList();
    protected TableModel        delegatedModel;
    protected int[]             sortedIndicies;
    protected int               sortColumn;
    protected Comparator        comparator;
    protected Comparator[]      comparators;
    protected boolean[]         sortDirection;
    protected boolean           isSorted         = false;

    /**
     * @param tm
     */
    public SortableTableModel(TableModel tm)
    {
        delegatedModel = tm;
        //delegatedModel.addTableModelListener(this);
        comparators    = new Comparator[tm.getColumnCount()];
        sortDirection  = new boolean[tm.getColumnCount()];
        sortedIndicies = new int[0];
        
        for (int i=0;i<sortDirection.length;i++)
        {
            // true means sorting up, so start out as false so 
            // the first time they click it sorts up
            sortDirection[i] = false; 
        }
    }

    // listener stuff
    public void addTableModelListener(TableModelListener l)
    {
        listenerList.add(TableModelListener.class, l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        listenerList.remove(TableModelListener.class, l);
    }

    public void fireTableModelEvent(TableModelEvent e)
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == TableModelListener.class)
            {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }

    // contents stuff

    public Class<?> getColumnClass(int column)
    {
        if (delegatedModel.getRowCount() > 0)
        {
            return delegatedModel.getValueAt(0, column).getClass();
            
        } else 
        {
            return Object.class;
        }
    }
    
    public TableModel getDelegateModel()
    {
        return this.delegatedModel;
    }

    public int getColumnCount()
    {
        return delegatedModel.getColumnCount();
    }

    public String getColumnName(int index)
    {
        return delegatedModel.getColumnName(index);
    }

    public int getRowCount()
    {
        return delegatedModel.getRowCount();
    }

    public int getDelegatedRow(int rowIndex)
    {
        return isSorted ? sortedIndicies[rowIndex]  : rowIndex;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return delegatedModel.getValueAt(getDelegatedRow(rowIndex), columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return delegatedModel.isCellEditable(rowIndex, columnIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        delegatedModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    // internal helpers
    /**
     * @param c
     * @param i
     */
    protected void setComparatorForColumn(Comparator c, int i)
    {
        // range check
        if (i > comparators.length)
        {
            Comparator[] newComparators = new Comparator[i + 1];
            System.arraycopy(comparators, 0, newComparators, 0, comparators.length);
            comparators = newComparators;
        }
        // add the comparator
        comparators[i] = c;
    }

    /**
     * @param i
     */
    public void setSortColumn(int i)
    {
        sortColumn = i;

        // reset current comparator, possibly to null, which 
        // will make us use "natural ordering" for those values
        comparator = null;
        if ((comparators != null) && (comparators.length > 0))
        {
            // is there one in the list of comparators?
            comparator = comparators[sortColumn];
        }

        //comparator.
        sortDirection[sortColumn] = !sortDirection[sortColumn];
        // now do the sort
        resort();
    }

    /**
     * the sort column.
     * @return the sort column 
     */
    public int getSortColumn()
    {
        return sortColumn;
    }

    /** called to rebuild the delegate-to-sortable mapping
     */
    @SuppressWarnings("unchecked")
    protected void resort()
    {
        
        // does sortedIndicies need to grow or shrink?
        if (sortedIndicies.length != delegatedModel.getRowCount())
        {
            sortedIndicies = new int[delegatedModel.getRowCount()];
        }
        // build up a list of SortingDelegates
        ArrayList sortMe = new ArrayList();
        for (int i = 0; i < delegatedModel.getRowCount(); i++)
        {
            SortingDelegate sd = new SortingDelegate(delegatedModel.getValueAt(i, getSortColumn()), i);
            sortMe.add(sd);
        }
        // now sort him with the SortingDelegateComparator
        SortingDelegateComparator sdc = new SortingDelegateComparator(comparator, getSortColumn());
        Collections.sort(sortMe, sdc);

        // fill sortedIndicies array
        // index -> value represents mapping from original
        // row to sorted row
        for (int i = 0; i < sortMe.size(); i++)
        {
            sortedIndicies[i] = ((SortingDelegate) sortMe.get(i)).row;
        }
        
        isSorted = true;
        
        // fire change event
        fireAllChanged();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e)
    {
        switch (e.getType())
        {
            case TableModelEvent.DELETE:
            {
                resort();
                fireAllChanged();
                break;
            }
            case TableModelEvent.INSERT:
            {
                resort();
                fireAllChanged();
                break;
            }
            case TableModelEvent.UPDATE:
            {
                resort();
                fireAllChanged();
                break;
            }

        }
    }

    /**
     * 
     */
    protected void fireAllChanged()
    {
        TableModelEvent e = new TableModelEvent(this);
        fireTableModelEvent(e);
    }

    
    //-------------------------------------------------------------------
    //-- Inner Classes
    //-------------------------------------------------------------------
    public class SortingDelegate extends Object
    {
        public Object value;
        public int    row;

        public SortingDelegate(Object v, int r)
        {
            value = v;
            row = r;
        }
    }

    // "pointer object" int 1 is current value and int 2 is
    // current row in table

    // comparator which applies current comparator's compare rule
    // to value 2 in 
    class SortingDelegateComparator extends Object implements Comparator
    {
        protected Comparator comp;
        protected int columnIndex;

        public SortingDelegateComparator(Comparator c, int columnIndex)
        {
            comp = c;
            this.columnIndex = columnIndex;
        }

        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2)
        {
            Object v1 = ((SortingDelegate) o1).value;
            Object v2 = ((SortingDelegate) o2).value;
            
            if (comp != null)
            {
                return sortDirection[columnIndex] ? comp.compare(v1, v2) : comp.compare(v2, v1);
            
            } else if (v1 instanceof Comparable)
            {
                return sortDirection[columnIndex] ? ((Comparable) v1).compareTo(v2) : ((Comparable) v2).compareTo(v1);
                
            } else 
            {
                throw new IllegalArgumentException("Can't compare objects " + "for sorting");
            }

        }
    }
    
    /**
     * This is here bcause it needs state from the model to render the header correctly
     *
     */
    class SortableTableHeaderCellRenderer extends GradiantLabel implements TableCellRenderer 
    {
        protected ImageIcon downIcon;
        protected ImageIcon upIcon;
        protected ImageIcon blankIcon;
        
        public SortableTableHeaderCellRenderer()
        {
            super("");
            
            //downIcon = IconManager.getImage("DownArrow", IconManager.IconSize.Std16);
            downIcon  = IconManager.getScaledIcon(IconManager.getIcon("DownArrow", IconManager.IconSize.Std16), IconManager.IconSize.Std16, IconManager.IconSize.Std8);
            upIcon    = IconManager.getScaledIcon(IconManager.getIcon("UpArrow", IconManager.IconSize.Std16), IconManager.IconSize.Std16, IconManager.IconSize.Std8);
            blankIcon = IconManager.getScaledIcon(IconManager.getIcon("BlankIcon", IconManager.IconSize.Std16), IconManager.IconSize.Std16, IconManager.IconSize.Std8);
            this.setHorizontalTextPosition(DefaultTableCellRenderer.LEFT);
            //setBorder(BorderFactory.createBevelBorder(SortableTableModel.this.hashCode(), getBackground(), getForeground()));
            
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) 
        {
            
            this.setIcon(isSorted && vColIndex == sortColumn ? (sortDirection[vColIndex] ? downIcon : upIcon) : blankIcon);
            setText(value.toString());
            return this;
        }
        
        
        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
    
}
