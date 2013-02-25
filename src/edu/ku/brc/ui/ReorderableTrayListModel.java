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
package edu.ku.brc.ui;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

/**
 * An extension of {@link AbstractListModel} that implements {@link ModifiableListModel}
 * and provides the ability to 'shift' elements up or down in the order.
 *
 * @author jstewart
 * @code_status Complete
 */
public class ReorderableTrayListModel<T> extends AbstractListModel implements ModifiableListModel<T>
{
    /** The collection of elements. */
    protected Vector<T> data;
    
    /**
     * Creates a new instance containing zero elements.
     */
    public ReorderableTrayListModel()
    {
        data = new Vector<T>();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public T getElementAt(int index)
    {
        if (data != null && index > -1 && index < data.size())
        {
            return data.get(index);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize()
    {
        return data.size();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#add(java.lang.Object)
     */
    public synchronized void add(final T t)
    {
        data.add(t);
        //this.fireIntervalAdded(this, data.indexOf(t), data.indexOf(t));
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                ReorderableTrayListModel.this.fireIntervalAdded(this, data.indexOf(t), data.indexOf(t));
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#remove(int)
     */
    public synchronized T remove(int index)
    {
        T t = data.remove(index);
        this.fireIntervalRemoved(this, index, index);
        return t;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#remove(java.lang.Object)
     */
    public synchronized boolean remove(T element)
    {
        int index = data.indexOf(element);
        if(index == -1)
        {
            return false;
        }
        data.remove(element);
        return true;
    }
    
    /**
     * Shift the indicated element 'left' in the order.
     * 
     * @param index the index of the element to shift.
     */
    public synchronized void shiftLeft(int index)
    {
        T moving = data.get(index);
        int newIndex = (index-1<0) ? 0 : index-1;
        data.remove(index);
        data.insertElementAt(moving, newIndex);
        this.fireContentsChanged(this, newIndex, index);
    }
    
    /**
     * Shift the indicated element 'right' in the order.
     * 
     * @param index the index of the element to shift.
     */
    public synchronized void shiftRight(int index)
    {
        T moving = data.get(index);
        int newIndex = (index+1>data.size()-1) ? data.size()-1 : index+1;
        data.remove(index);
        data.insertElementAt(moving, newIndex);
        this.fireContentsChanged(this, newIndex, index);
    }
    
    /**
     * Shift the indicated element to the start of the order.
     * 
     * @param index the index of the element to shift.
     */
    public synchronized void moveToStart(int index)
    {
        T moving = data.get(index);
        data.remove(index);
        data.insertElementAt(moving, 0);
        this.fireContentsChanged(this, 0, index);
    }
    
    /**
     * Shift the indicated element to the end of the order.
     * 
     * @param index the index of the element to shift.
     */
    public synchronized void moveToEnd(int index)
    {
        T moving = data.get(index);
        data.remove(index);
        data.add(moving);
        this.fireContentsChanged(this, index, data.size()-1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#clear()
     */
    public synchronized void clear()
    {
        if(data.isEmpty())
        {
            return;
        }
        
        int index1 = data.size()-1;
        data.clear();
        this.fireIntervalRemoved(this, 0, index1);
    }
}
