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
