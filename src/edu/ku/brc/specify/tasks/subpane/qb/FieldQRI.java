/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class FieldQRI extends BaseQRI
{
    protected TableQRI table = null;
    protected DBFieldInfo fi;
    
    public FieldQRI(final TableQRI table, final DBFieldInfo fi)
    {
        super(null);
        this.table = table;
        this.fi  = fi;
        if (fi != null)
        {
            title    = fi.getTitle();
        }
        iconName = "BlankIcon";
    }
    
    public DBFieldInfo getFieldInfo()
    {
        return fi;
    }
    
    @Override
    public boolean hasChildren()
    {
        return false;
    }

    public String getFieldName()
    {
        return fi.getName();
    }
    
    public UIFieldFormatterIFace getFormatter()
    {
        if (fi != null)
            return fi.getFormatter();
        return null;
    }
    
    public DBTableInfo getTableInfo()
    {
        if (fi != null)
            return fi.getTableInfo();
        return null;
    }
    
    public String getSQLFldSpec(final TableAbbreviator ta)
    {
        return ta.getAbbreviation(table.getTableTree()) + "." + getFieldName();
    }
    
    public Class<?> getDataClass()
    {
        if (fi != null)
            return fi.getDataClass();
        return String.class;
    }

    /**
     * @param table the table to set
     */
    public void setTable(TableQRI table)
    {
        this.table = table;
    }

    /**
     * @return the table
     */
    public TableQRI getTable()
    {
        return table;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#getTableTree()
     */
    @Override
    public TableTree getTableTree()
    {
        if (table != null)
        {
            return table.getTableTree();
        }
        return null;
    }
    
    
}