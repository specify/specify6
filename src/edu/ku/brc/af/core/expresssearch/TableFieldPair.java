/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class TableFieldPair implements Comparable<TableFieldPair>
{
    protected DBTableInfo tableinfo;
    protected DBFieldInfo fieldInfo;
    protected boolean     isInUse    = false;
    
    public TableFieldPair(final DBTableInfo tableinfo, final DBFieldInfo fieldInfo)
    {
        super();
        this.tableinfo = tableinfo;
        this.fieldInfo = fieldInfo;
    }

    public boolean isInUse()
    {
        return isInUse;
    }

    public void setInUse(boolean isMapped)
    {
        this.isInUse = isMapped;
    }

    public DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    public DBTableInfo getTableinfo()
    {
        return tableinfo;
    }
    
    /**
     * Return the title for the Field.
     * @return the title for the Field.
     */
    public String getTitle()
    {
        return fieldInfo.getColumn();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fieldInfo.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableFieldPair obj)
    {
        return fieldInfo.toString().compareTo(obj.fieldInfo.toString());
    }

}
