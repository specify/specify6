/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import javax.swing.DefaultListModel;

/**
 * An extension of {@link DefaultListModel} that implements {@link ModifiableListModel}.
 *
 * @author jstewart
 * @code_status Complete
 */
public class DefaultModifiableListModel<T> extends DefaultListModel implements ModifiableListModel<T>
{

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#add(java.lang.Object)
     */
    public void add(T t)
    {
        super.addElement(t);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ModifiableListModel#remove(java.lang.Object)
     */
    public boolean remove(T element)
    {
        return super.removeElement(element);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.DefaultListModel#remove(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T remove(int index)
    {
        return (T)super.remove(index);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.DefaultListModel#getElementAt(int)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getElementAt(int index)
    {
        return (T)super.getElementAt(index);
    }
}
