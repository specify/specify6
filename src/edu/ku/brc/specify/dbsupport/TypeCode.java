/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport;

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * A list of valid values for predefined system coded fields like Agent.agentType, DeterminationStatus.type
 *
 */
public class TypeCode implements PickListDBAdapterIFace, ComboBoxModel
{
    protected final Vector<PickListItemIFace> items;
    protected final String                   fldName;
    
    protected Object selectedItem = null;
    
    /**
     * Default constructor for a dead RecordTypeCode.
     */
    public TypeCode()
    {
        items = null;
        fldName = null;
    }
    
    /**
     * @param items
     * @param fldInfo
     * @param tblInfo
     */
    public TypeCode(final Vector<PickListItemIFace> items, final String fldName)
    {
        this.items = items;
        this.fldName = fldName;
    }

    
    /**
     * @param title
     * @return item with matching title.
     */
    public PickListItemIFace getItemByTitle(final String title)
    {
        for (PickListItemIFace item : items)
        {
            if (item.getTitle().equals(title))
            {
                return item;
            }
        }
        return null;
    }
    
    /**
     * @param value
     * @return
     */
    public PickListItemIFace getItemByValue(final Object value)
    {
        for (PickListItemIFace item : items)
        {
            Object itemValue = item.getValueObject();
            if (itemValue == null && value == null)
            {
                return item;
            }
            if (itemValue != null && itemValue.equals(value))
            {
                return item;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#setAutoSaveOnAdd(boolean)
     */
    @Override
    public void setAutoSaveOnAdd(boolean doAutoSave)
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#addItem(java.lang.String, java.lang.String)
     */
    @Override
    public PickListItemIFace addItem(String title, String value)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getItem(int)
     */
    @Override
    public PickListItemIFace getItem(int index)
    {
        return items.get(index);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getList()
     */
    @Override
    public Vector<PickListItemIFace> getList()
    {
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getPickList()
     */
    @Override
    public PickListIFace getPickList()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#getType()
     */
    @Override
    public Type getType()
    {
        return PickListDBAdapterIFace.Type.Item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isReadOnly()
     */
    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#isTabledBased()
     */
    @Override
    public boolean isTabledBased()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterIFace#save()
     */
    @Override
    public void save()
    {
        //nothing
    }

    /**
     * @return the fldName
     */
    public String getFldName()
    {
        return fldName;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    @Override
    public Object getSelectedItem()
    {
        return selectedItem;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    @Override
    public void setSelectedItem(Object anItem)
    {
        selectedItem = anItem;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
     */
    @Override
    public void addListDataListener(ListDataListener l)
    {
        //rather not
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    @Override
    public Object getElementAt(int index)
    {
        return getItem(index);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    @Override
    public int getSize()
    {
        return items.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
     */
    @Override
    public void removeListDataListener(ListDataListener l)
    {
        // certainly not
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener l)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListDBAdapterIFace#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener(ChangeListener l)
    {
    }
}
