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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
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
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "picklist")
@org.hibernate.annotations.Table(appliesTo="picklist", indexes =
    {   @Index (name="PickListNameIDX", columnNames={"Name"})
    })
public class PickList extends DataModelObjBase implements PickListIFace, java.io.Serializable, Comparable<PickList>
{
    // Fields    

    protected Integer           pickListId;
    protected String            name;
    protected Integer           type;  // see PickListDBAdapterIFace.Type
    protected String            tableName;
    protected String            fieldName;
    protected String            formatter; // dataobj_formatter or uiformatter
    protected Boolean           readOnly;
    protected Integer           sizeLimit;
    protected Boolean           isSystem; 
    protected Set<PickListItem> pickListItems;
    protected Collection        collection;
    
    // Transient
    protected Set<PickListItemIFace> items = null;

    // Constructors

    /** default constructor */
    public PickList()
    {
        // do nothing
    }

    /**
     * @param name
     */
    public PickList(final String name)
    {
        this.name = name;
    }

    /** constructor with id */
    public PickList(Integer pickListId)
    {
        this.pickListId = pickListId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        pickListId = null;
        name       = null;
        type       = 0;
        tableName  = null;
        fieldName  = null;
        formatter  = null;
        readOnly   = false;
        sizeLimit  = 50;
        isSystem   = false;
        
        collection = AppContextMgr.getInstance() == null || !AppContextMgr.getInstance().hasContext() ? null : AppContextMgr.getInstance().getClassObject(Collection.class);
        
        pickListItems = new HashSet<PickListItem>();
    }

    // Property accessors

    /**
     * Returns the primary ID.
      * @return the primary ID.
     */
    @Id
    @GeneratedValue
    @Column(name = "PickListID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPickListId()
    {
        return this.pickListId;
    }
    
    /**
     * Sets Primary ID.
     * @param pickListId the id
     */
    public void setPickListId(Integer pickListId)
    {
        this.pickListId = pickListId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.pickListId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PickList.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }
    
   /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getName()
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
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
    @Column(name = "ReadOnly", unique = false, nullable = false, insertable = true, updatable = true)
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
    @Column(name = "SizeLimit", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
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
    @Column(name = "FieldName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
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
    @Column(name = "TableName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
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
    @Column(name = "Formatter", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
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
    @Column(name = "Type", unique = false, nullable = false, insertable = true, updatable = true)
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

    /**
     * @return
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "pickList")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<PickListItem> getPickListItems()
    {
        return this.pickListItems;
    }

    /**
     * @return the isSystem
     */
    @Column(name="IsSystem", unique = false, nullable = false, updatable = true, insertable = true)
    public Boolean getIsSystem()
    {
        return isSystem;
    }

    /**
     * @param isSystem the isSystem to set
     */
    public void setIsSystem(Boolean isSystem)
    {
        this.isSystem = isSystem;
    }
    
    /**
     * @return the collection
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Collection getCollection()
    {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }

    /**
     * @param items
     */
    public void setPickListItems(Set<PickListItem> items)
    {
        this.pickListItems = items;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getNumItems()
     */
    @Transient
    public int getNumItems()
    {
        return pickListItems.size();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#removeItem(edu.ku.brc.ui.db.PickListItemIFace)
     */
    public void removeItem(PickListItemIFace item)
    {
        if (items != null)
        {
            items.remove(item);
        }
        pickListItems.remove(item);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#getItems()
     */
    @Transient
    public Set<PickListItemIFace> getItems()
    {
        if (this.items == null)
        {
            items = new HashSet<PickListItemIFace>();
            for (PickListItem rsi : pickListItems)
            {
                this.items.add(rsi);
            }
        }
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
     * @see edu.ku.brc.ui.db.PickListIFace#addItem(java.lang.String, java.lang.String)
     */
    public PickListItemIFace addItem(final String title, final String value)
    {
        if (items == null)
        {
            items = new HashSet<PickListItemIFace>();
        }
        PickListItem pli = new PickListItem(title, value, new Timestamp(System.currentTimeMillis()));
        items.add(pli);
        pickListItems.add(pli);
        pli.setPickList(this);
        return pli;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#addItem(java.lang.String, java.lang.Object)
     */
    public PickListItemIFace addItem(final String title, final Object value)
    {
        if (items == null)
        {
            items = new HashSet<PickListItemIFace>();
        }
        PickListItem pli = new PickListItem(title, value, new Timestamp(System.currentTimeMillis()));
        items.add(pli);
        pickListItems.add(pli);
        pli.setPickList(this);
        return pli;
       
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#addItem(edu.ku.brc.ui.db.PickListItemIFace)
     */
    public PickListItemIFace addItem(final PickListItemIFace item)
    {
        if (items == null)
        {
            items = new HashSet<PickListItemIFace>();
        }
        items.add(item);
        pickListItems.add((PickListItem)item);
        return item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 500;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(PickList o)
    {
        return name.toLowerCase().compareTo(o.name.toLowerCase());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    public String toString()
    {
        return name;
    }
}
