/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;

/**
 *
 * @author jstewart
 * @code_status Alpha
 */
public class ReorderableListModel<T> extends AbstractListModel
{
    protected Vector<T> data;
    
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
    
    public synchronized void add(T t)
    {
        data.add(t);
        this.fireIntervalAdded(this, data.indexOf(t), data.indexOf(t));
    }
    
    public synchronized T remove(int index)
    {
        T t = data.remove(index);
        this.fireIntervalRemoved(this, index, index);
        return t;
    }
    
    public void shiftLeft(int index)
    {
        T moving = data.get(index);
        int newIndex = (index-1<0) ? 0 : index-1;
        data.remove(index);
        data.insertElementAt(moving, newIndex);
        this.fireContentsChanged(this, newIndex, index);
    }
    
    public void shiftRight(int index)
    {
        T moving = data.get(index);
        int newIndex = (index+1>data.size()-1) ? data.size()-1 : index+1;
        data.remove(index);
        data.insertElementAt(moving, newIndex);
        this.fireContentsChanged(this, newIndex, index);
    }
    
    public void moveToStart(int index)
    {
        T moving = data.get(index);
        data.remove(index);
        data.insertElementAt(moving, 0);
        this.fireContentsChanged(this, 0, index);
    }
    
    public void moveToEnd(int index)
    {
        T moving = data.get(index);
        data.remove(index);
        data.add(moving);
        this.fireContentsChanged(this, index, data.size()-1);
    }
}
