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
package edu.ku.brc.specify.config.init;

import java.sql.Timestamp;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class BldrPickList
{
    // Fields    

    protected String            name;
    protected Byte              type;  // see PickListDBAdapterIFace.Type
    protected String            tableName;
    protected String            fieldName;
    protected String            formatter; // dataobj_formatter or uiformatter
    protected Boolean           readOnly;
    protected Integer           sizeLimit;
    protected Boolean           isSystem;
    protected Byte              sortType = 1;
    protected Vector<BldrPickListItem> items;
    
    // Only Used for Import/Export
    protected Timestamp         timestampCreated;
    protected Timestamp         timestampModified;
    protected int               version;
    protected String            filterFieldName;
    protected String            filterValue;

    
    // Constructors

    /** default constructor */
    public BldrPickList()
    {
        // do nothing
    }
    
    public BldrPickList(final PickList pl)
    {
        name      = pl.getName();
        type      = pl.getType();
        tableName = pl.getTableName();
        fieldName = pl.getFieldName();
        formatter = pl.getFormatter();
        readOnly  = pl.getReadOnly();
        sizeLimit = pl.getSizeLimit();
        isSystem  = pl.isSystem();
        sortType  = pl.getSortType();
        
        timestampCreated  = pl.getTimestampCreated();
        timestampModified = pl.getTimestampModified();
        version           = pl.getVersion();
        
        items = new Vector<BldrPickListItem>();
        for (PickListItem pli : pl.getPickListItems())
        {
            items.add(new BldrPickListItem(pli));
        }
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

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getFormatter()
    {
        return formatter;
    }

    public void setFormatter(String formatter)
    {
        this.formatter = formatter;
    }

    public Byte getType()
    {
        return type;
    }

    public void setType(Byte type)
    {
        this.type = type;
    }

    /**
     * @return the isSystem
     */
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
     * @return the sortType
     */
    public Byte getSortType()
    {
        return sortType;
    }

    /**
     * @param sortType the sortType to set
     */
    public void setSortType(Byte sortType)
    {
        this.sortType = sortType;
    }

    public Vector<BldrPickListItem> getItems()
    {
        return this.items;
    }

    public void setItems(Vector<BldrPickListItem> items)
    {
        this.items = items;
    }
    
    public BldrPickListItem getItem(final int index)
    {
        return items.get(index);
    }

    /**
     * @return the timestampCreated
     */
    public Timestamp getTimestampCreated()
    {
        return timestampCreated;
    }

    /**
     * @param timestampCreated the timestampCreated to set
     */
    public void setTimestampCreated(Timestamp timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /**
     * @return the timestampModified
     */
    public Timestamp getTimestampModified()
    {
        return timestampModified;
    }

    /**
     * @param timestampModified the timestampModified to set
     */
    public void setTimestampModified(Timestamp timestampModified)
    {
        this.timestampModified = timestampModified;
    }

    /**
     * @return the version
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version)
    {
        this.version = version;
    }
}
