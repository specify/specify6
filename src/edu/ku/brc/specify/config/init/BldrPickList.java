/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import java.util.Vector;

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
    
    // Constructors

    /** default constructor */
    public BldrPickList()
    {
        // do nothing
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
}
