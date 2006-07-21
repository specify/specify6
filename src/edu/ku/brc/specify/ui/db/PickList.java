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
package edu.ku.brc.specify.ui.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A pickList of items. A pcikList can be readonly or have a set number of items where the oldest items "falls off" the queue
 * and it reaches it's limit.
 *     
 */
@SuppressWarnings("serial")
public class PickList implements java.io.Serializable
{

    // Fields    

    protected Integer           pickListId;
    protected String            name;
    protected Boolean           readOnly;
    protected Integer           sizeLimit;
    protected Date              created;
    protected Set<PickListItem> items;

    // Constructors

    /** default constructor */
    public PickList()
    {
    }

    /** constructor with id */
    public PickList(Integer pickListId)
    {
        this.pickListId = pickListId;
    }

    // Initializer
    public void initialize()
    {
        pickListId = -1;
        readOnly   = false;
        sizeLimit  = 50;
        items      = new HashSet<PickListItem>();
        created    = new Date();
    }

    // Property accessors

    /**
     * 
     */
    public Integer getPickListId()
    {
        return this.pickListId;
    }

    public void setPickListId(Integer pickListId)
    {
        this.pickListId = pickListId;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Boolean getReadOnly()
    {
        return this.readOnly;
    }

    public void setReadOnly(Boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public Integer getSizeLimit()
    {
        return this.sizeLimit;
    }

    public void setSizeLimit(Integer sizeLimit)
    {
        this.sizeLimit = sizeLimit;
    }

    public Date getCreated()
    {
        return this.created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public Set<PickListItem> getItems()
    {
        return this.items;
    }

    public void setItems(Set<PickListItem> items)
    {
        this.items = items;
    }

    public PickListItem addPickListItem(final PickListItem item)
    {
        items.add(item);
        return item;
    }

    public void removePickListItem(final PickListItem item)
    {
        items.remove(item);
    }

}
