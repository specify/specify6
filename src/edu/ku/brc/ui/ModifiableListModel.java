/* Copyright (C) 2015, University of Kansas Center for Research
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
