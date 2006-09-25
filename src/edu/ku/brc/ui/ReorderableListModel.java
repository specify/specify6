/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.util.Vector;

import javax.swing.AbstractListModel;

/**
 * An extension of {@link AbstractListModel} that implements {@link ModifiableListModel}
 * and provides the ability to 'shift' elements up or down in the order.
 *
 * @author jstewart
 * @code_status Complete
 */
public class ReorderableListModel<T> extends AbstractListModel implements ModifiableListModel<T>
{
    /** The collection of elements. */
    protected Vector<T> data;
    
    /**
     * Creates a new instance containing zero elements.
     */
    public ReorderableListModel()
    {
        data = new Vector<T>();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public T getElementAt(int index)
    {
        return data.get(index);
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
    public synchronized void add(T t)
    {
        data.add(t);
        this.fireIntervalAdded(this, data.indexOf(t), data.indexOf(t));
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
    public void shiftLeft(int index)
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
    public void shiftRight(int index)
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
    public void moveToStart(int index)
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
    public void moveToEnd(int index)
    {
        T moving = data.get(index);
        data.remove(index);
        data.add(moving);
        this.fireContentsChanged(this, index, data.size()-1);
    }
}
