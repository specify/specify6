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

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

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
    protected TableQRI    table = null;
    protected DBFieldInfo fi;
    
    /**
     * @param table
     * @param fi
     */
    public FieldQRI(final TableQRI table, final DBFieldInfo fi)
    {
        super(null);
        
        this.table = table;
        this.fi    = fi;
        
        if (fi != null)
        {
            title    = fi.getTitle();
            //buildValues();
        }
        iconName = "BlankIcon";
    }
        
    /**
     * @return the fieldInfo.
     */
    public DBFieldInfo getFieldInfo()
    {
        return fi;
    }
    
    /**
     * @return the name of the field.
     */
    public String getFieldName()
    {
        return fi.getName();
    }
    
    /**
     * @return the formatter for the field.
     */
    public UIFieldFormatterIFace getFormatter()
    {
        if (fi != null)
            return fi.getFormatter();
        return null;
    }
    
    /**
     * @return the tableInfo.
     */
    public DBTableInfo getTableInfo()
    {
        if (fi != null)
            return fi.getTableInfo();
        return null;
    }
    
    /**
     * @param ta
     * @param forWhereClause
     * @return sql/hql specification for this field.
     */
    public String getSQLFldSpec(final TableAbbreviator ta, final boolean forWhereClause)
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
    
    
    /**
     * @return true if the schema field represented by this object has been hidden.
     */
    public boolean isFieldHidden()
    {
        return getFieldInfo().isHidden();
    }
    
    /**
     * @return a string identifier unique to this field within the query that is independent of the field's title.
     */
    public String getStringId()
    {
        return getTableTree().getPathFromRoot() + "." + getTableTree().getField() + "." + getTableInfo().getName() + "." + getFieldName();
    }
}