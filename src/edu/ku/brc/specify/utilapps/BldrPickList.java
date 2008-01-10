/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

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
    protected Integer           type;  // see PickListDBAdapterIFace.Type
    protected String            tableName;
    protected String            fieldName;
    protected String            formatter; // dataobj_formatter or uiformatter
    protected Boolean           readOnly;
    protected Integer           sizeLimit;
    protected Vector<BldrPickListItem> items;
    
    // Constructors

    /** default constructor */
    public BldrPickList()
    {
        // do nothing
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
    public Vector<BldrPickListItem> getItems()
    {
        return this.items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListIFace#setItems(java.util.Set)
     */
    public void setItems(Vector<BldrPickListItem> items)
    {
        this.items = items;
    }
    
    public BldrPickListItem getItem(final int index)
    {
        return items.get(index);
    }
}
