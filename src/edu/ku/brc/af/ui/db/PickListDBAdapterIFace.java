/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.db;

import java.util.Vector;

import javax.swing.event.ChangeListener;

/**
 * There are three types of PickLists:<br>
 * 1) A PickList that uses values from the PickListItems table meaning the PickList object is managing all the items.<br>
 * 2) A PickList that uses the entire table, which really means it is return all the records as items in the PickList and they are formatted using the DataObjectFormatter<br>
 * For example, a list the DeterminationStatus.<br>
 * 3) A PickList that uses the values from an arbitrary table and it should search on a field. It returns a list of objects that are formatted with a DataObjectFormatter.
 * 
 * Note: Any Class implementing this interface so it can be used in the ValComboBox must also implement ComboBoxModel!
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 10, 2006
 *
 */
public interface PickListDBAdapterIFace
{
    public enum Type 
    {
        Item(0), Table(1), TableField(2);
        
        private int type;
        Type(int t) { type = t; }
        public int value() { return type; }
        public static Type valueOf(int t) 
        {
            switch (t)
            {
                case 0 : return Item;
                case 1 : return Table;
                case 2 : return TableField;
            }
            throw new RuntimeException("Unknown type["+t+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Returns the picklist object.
     * @return Returns the picklist object
     */
    public abstract PickListIFace getPickList();

    /**
     * Returns the list of PickList items.
     * @return Returns the list of PickList items
     */
    public abstract Vector<PickListItemIFace> getList();
    
    /**
     * Gets a pick list item by index.
     * @param index the index in question
     * @return pick list item by index
     */
    public abstract PickListItemIFace getItem(final int index);
    
    /**
     * Adds a new item to a picklist.
     * @param title the title (or text) of the picklist
     * @param value although currently no supported we may want to display one text string but save a different one
     * @return returns the new PickListItem
     */
    public abstract PickListItemIFace addItem(final String title, final String value);
    
    /**
     * Persists the picklist and it's items.
     */
    public abstract void save();
    
    
    /**
     * Tells the adapter whether to save after an item has been added, or
     * wait until 'save' has been called.
     * @param doAutoSave true/false
     */
    public abstract void setAutoSaveOnAdd(boolean doAutoSave);
    
    
    /**
     * Returns whether the PickList is mutable.
     * @return whether the PickList is mutable.
     */
    public abstract boolean isReadOnly();
    
    /**
     * When this returns true it means the PickListItems will be populated from other tables
     * in the Database schema.
     * @return true other tables, false not other tables
     */
    public abstract boolean isTabledBased();
    
    /**
     * Returns the type of adapter.
     * @return the type of adapter.
     */
    public abstract Type getType();
    
    public abstract void addChangeListener(ChangeListener l);
    public abstract void removeChangeListener(ChangeListener l);
}
