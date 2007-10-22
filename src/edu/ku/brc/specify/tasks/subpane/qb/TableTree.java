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

import java.util.Vector;

import edu.ku.brc.dbsupport.DBTableInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class TableTree implements Cloneable, Comparable<TableTree>
{
    protected String            name;
    protected String            field;
    protected TableTree         parent;
    protected Vector<TableTree> kids      = new Vector<TableTree>();
    protected DBTableInfo       tableInfo = null;
    protected boolean           isAlias   = false;
    protected BaseQRI           baseQRI   = null;          

    public TableTree(final TableTree parent, 
                     final String name)
    {
        this.parent = parent;
        this.name   = name;
    }
    
    public TableTree(final TableTree parent, 
                     final String name,
                     final String field,
                     final DBTableInfo tableInfo)
    {
        this.parent = parent;
        this.name   = name;
        this.field  = field;
        this.tableInfo  = tableInfo;
    }

    /**
     * @return the baseQRI
     */
    public BaseQRI getBaseQRI()
    {
        return baseQRI;
    }

    /**
     * @param baseQRI the baseQRI to set
     */
    public void setBaseQRI(BaseQRI baseQRI)
    {
        this.baseQRI = baseQRI;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(TableTree parent)
    {
        this.parent = parent;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the abbrev
     */
    public String getAbbrev()
    {
        if (tableInfo != null)
        {
            return tableInfo.getAbbrev();
        }
        throw new RuntimeException("TableInfo is null for ["+name+"]");
    }

    /**
     * @return the field
     */
    public String getField()
    {
        return field;
    }

    /**
     * @return the parent
     */
    public TableTree getParent()
    {
        return parent;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * @return the kids
     */
    public Vector<TableTree> getKids()
    {
        return kids;
    }

    /**
     * @return the isAlias
     */
    public boolean isAlias()
    {
        return isAlias;
    }

    /**
     * @param isAlias the isAlias to set
     */
    public void setAlias(boolean isAlias)
    {
        this.isAlias = isAlias;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableTree o)
    {
        return name.compareTo(o.name);
    }
    
    /**
     * @param field the field to set
     */
    public void setField(String field)
    {
        this.field = field;
    }

    /**
     * @param tableInfo the tableInfo to set
     */
    public void setTableInfo(DBTableInfo tableInfo)
    {
        this.tableInfo = tableInfo;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        TableTree obj = (TableTree)super.clone();
        
        obj.name      = name;
        obj.field     = field;
        obj.parent    = parent;
        obj.kids      = new Vector<TableTree>();
        for (TableTree tt : kids)
        {
            obj.kids.add(tt);
        }
        obj.tableInfo = tableInfo;
        
        return obj;
    }

}