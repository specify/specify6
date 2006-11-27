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
package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

/**
 * A pickList of items. A pcikList can be readonly or have a set number of items where the oldest items "falls off" the queue
 * and it reaches it's limit.
 *    
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PickList extends DataModelObjBase implements PickListIFace, java.io.Serializable
{
    // Fields    

    protected Long              pickListId;
    protected String            name;
    protected Integer           type;
    protected String            tableName;
    protected String            fieldName;
    protected String            formatter; // dataobj_formatter or uiformatter
    protected Boolean           readOnly;
    protected Integer           sizeLimit;
    protected Set<PickListItemIFace> items;

    // Constructors

    /** default constructor */
    public PickList()
    {
        // do nothing
    }

    /** constructor with id */
    public PickList(Long pickListId)
    {
        this.pickListId = pickListId;
    }

    // Initializer
    public void initialize()
    {
        pickListId = null;
        name       = null;
        type       = 0;
        tableName  = null;
        fieldName  = null;
        formatter  = null;
        readOnly   = false;
        sizeLimit  = 50;
        items      = new HashSet<PickListItemIFace>();
        
        // base class
        timestampCreated  = new Date();
        timestampModified = null;
        lastEditedBy      = null;
    }

    // Property accessors

    /**
     * Returns the primary ID.
      * @return the primary ID.
     */
    public Long getPickListId()
    {
        return this.pickListId;
    }
    
    /**
     * Sets Primary ID.
     * @param pickListId the id
     */
    public void setPickListId(Long pickListId)
    {
        this.pickListId = pickListId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    public Long getId()
    {
        return this.pickListId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return PickList.class;
    }

   /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getReadOnly()
     */
    public Boolean getReadOnly()
    {
        return this.readOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setReadOnly(java.lang.Boolean)
     */
    public void setReadOnly(Boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getSizeLimit()
     */
    public Integer getSizeLimit()
    {
        return this.sizeLimit;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setSizeLimit(java.lang.Integer)
     */
    public void setSizeLimit(Integer sizeLimit)
    {
        this.sizeLimit = sizeLimit;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getFieldName()
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setFieldName(java.lang.String)
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getTableName()
     */
    public String getTableName()
    {
        return tableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setTableName(java.lang.String)
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getFormatter()
     */
    public String getFormatter()
    {
        return formatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setFormatter(java.lang.String)
     */
    public void setFormatter(String formatter)
    {
        this.formatter = formatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getType()
     */
    public Integer getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setType(java.lang.Short)
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getItems()
     */
    public Set<PickListItemIFace> getItems()
    {
        return this.items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setItems(java.util.Set)
     */
    public void setItems(Set<PickListItemIFace> items)
    {
        this.items = items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#addPickListItem(java.lang.String, java.lang.String)
     */
    public PickListItemIFace addPickListItem(final String title, final String value)
    {
        PickListItem pli = new PickListItem(title, value, new Date());
        items.add(pli);
        return pli;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#addPickListItem(java.lang.String, java.lang.Object)
     */
    public PickListItemIFace addPickListItem(final String title, final Object value)
    {
        PickListItem pli = new PickListItem(title, value, new Date());
        items.add(pli);
        return pli;
       
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#addPickListItem(edu.ku.brc.ui.db.PickListItemIFace)
     */
    public PickListItemIFace addPickListItem(final PickListItemIFace item)
    {
        items.add(item);
        return item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#removePickListItem(edu.ku.brc.ui.db.PickListItemIFace)
     */
    public void removePickListItem(final PickListItemIFace item)
    {
        items.remove(item);
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 500;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    public String getIdentityTitle()
    {
        return name;
    }
}
