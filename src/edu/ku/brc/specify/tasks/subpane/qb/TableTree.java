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

import edu.ku.brc.dbsupport.DBFieldInfo;
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
    protected String            abbrev = null;
    protected DBTableInfo       tableInfo = null;
    protected boolean           isAlias   = false;
    protected TableQRI           tableQRI   = null;          

    public TableTree(final TableTree parent, 
                     final String name, boolean isAlias)
    {
        this.parent = parent;
        this.name   = name;
        this.isAlias = isAlias;
        if (this.parent != null)
        {
            this.parent.addKid(this);
            if (this.tableInfo != null)
            {
                this.tableQRI = new TableQRI(this);
                for (DBFieldInfo fi : this.tableInfo.getFields())
                {
                    tableQRI.addField(fi);
                }
            }
        }
    }
    
    public TableTree(final TableTree parent, 
                     final String name,
                     final String field,
                     final String abbrev,
                     final DBTableInfo tableInfo)
    {
        this.parent = parent;
        this.tableInfo  = tableInfo;
        if (this.parent != null)
        {
            this.parent.addKid(this);
            if (this.tableInfo != null)
            {
                this.tableQRI = new TableQRI(this);
                for (DBFieldInfo fi : this.tableInfo.getFields())
                {
                    tableQRI.addField(fi);
                }
            }
        }
        this.name   = name;
        this.field  = field;
        this.abbrev = abbrev;
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
        if (abbrev != null)
        {
            return abbrev;
        }
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

    public int getKids()
    {
        return kids.size();
    }
    
    public TableTree getKid(int k)
    {
        return kids.get(k);
    }
    
    public boolean addKid(final TableTree kid)
    {
        kid.setParent(this);
        return kids.add(kid);
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
        
        obj.kids      = new Vector<TableTree>();
        for (TableTree tt : kids)
        {
            TableTree newKid = (TableTree)tt.clone();
            obj.addKid(newKid);
        }
        
        if (tableQRI != null)
        {
            obj.tableQRI = (TableQRI)tableQRI.clone();
        }
        return obj;
    }

    public TableQRI getTableQRI()
    {
        return tableQRI;
    }

    /**
     * @param tableQRI the tableQRI to set
     */
    public void setTableQRIClone(TableQRI tableQRI) throws CloneNotSupportedException
    {
        this.tableQRI = (TableQRI)tableQRI.clone();
        this.tableQRI.setTableTree(this);
    }
}