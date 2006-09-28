/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import javax.swing.ListModel;

/**
 * Describe the basic capabilities required of {@link ListModel}s that allow
 * elements to be added and removed.
 *
 * @author jstewart
 * @code_status Complete
 */
public interface ModifiableListModel<T> extends ListModel
{
    /**
     * Add the element to the end of the model.
     * 
     * @param t the element to add
     */
    public void add(T t);
    
    /**
     * Remove the index-th element from the model.
     * 
     * @param index the index of the element to remove
     * @return the element removed
     */
    public T remove(int index);
    
    /**
     * Remove the first occurance of the given element from the model.
     * 
     * @param element the element to remove
     * @return <code>true</code> if an element was removed
     */
    public boolean remove(T element);

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public T getElementAt(int index);
    
    /**
     * Removes all elements from the model.
     */
    public void clear();
}
