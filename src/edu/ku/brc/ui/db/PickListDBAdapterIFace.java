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
package edu.ku.brc.ui.db;

import java.util.Vector;

/**
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 10, 2006
 *
 */
public interface PickListDBAdapterIFace
{
    
    /**
     * Returns the pciklist object.
     * @return Returns the pciklist object
     */
    public PickListIFace getPickList();

    /**
     * Returns the list of PickList items.
     * @return Returns the list of PickList items
     */
    public Vector<PickListItemIFace> getList();
    
    /**
     * Gets a pick list item by index.
     * @param index the index in question
     * @return pick list item by index
     */
    public PickListItemIFace getItem(final int index);
    
    /**
     * Adds a new item to a picklist.
     * @param title the title (or text) of the picklist
     * @param value although currently no supported we may want to display one text string but save a different one
     * @return returns the new PickListItem
     */
    public PickListItemIFace addItem(final String title, final String value);
    
    /**
     * Persists the picklist and it's items.
     */
    public void save();
}
