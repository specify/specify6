/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr.FieldInfo;

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
    protected DBTableIdMgr.TableInfo tableinfo;
    protected DBTableIdMgr.FieldInfo fieldInfo;
    protected boolean                isInUse    = false;
    
    public TableFieldPair(DBTableIdMgr.TableInfo tableinfo, FieldInfo fieldInfo)
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

    public DBTableIdMgr.FieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    public DBTableIdMgr.TableInfo getTableinfo()
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
