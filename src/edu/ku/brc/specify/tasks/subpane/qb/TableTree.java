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

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;

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
    protected TableTree         parent = null;
    protected Vector<TableTree> kids = new Vector<TableTree>();
    protected String            abbrev = null;
    protected DBTableInfo       tableInfo = null;
    protected boolean           isAlias   = false;
    protected TableQRI           tableQRI   = null;          

    /**
     * @param name
     * @param isAlias
     */
    public TableTree(final String name, final String field, boolean isAlias)
    {
        this.name   = name;
        this.field = field;
        this.isAlias = isAlias;
        processTblInfo();
    }
    
    /**
     * @param name
     * @param field
     * @param abbrev
     * @param tableInfo
     */
    public TableTree(final String name,
                     final String field,
                     final String abbrev,
                     final DBTableInfo tableInfo)
    {
        this.tableInfo  = tableInfo;
        processTblInfo();
        this.name   = name;
        this.field  = field;
        this.abbrev = abbrev;
    }

    /**
     * Builds tableQRI from tableInfo.
     */
    protected void processTblInfo()
    {
        if (tableInfo != null)
        {
            tableQRI = new TableQRI(this);
            for (DBFieldInfo fi : tableInfo.getFields())
            {
                tableQRI.addField(fi);
            }
        }
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

    /**
     * @return number of kids.
     */
    public int getKids()
    {
        return kids.size();
    }
    
    /**
     * @param k
     * @return kid with index k.
     */
    public TableTree getKid(int k)
    {
        return kids.get(k);
    }
    
    /**
     * @param kid
     * @return kid if added else null.
     */
    public TableTree addKid(final TableTree kid)
    {
        if (kids.add(kid))
        {
            kid.setParent(this);
            return kid;
        }
        return null;
    }
    
    /**
     * Clears kids.
     */
    public void clearKids()
    {
        kids.clear();
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
            //obj.tableQRI = (TableQRI)tableQRI.clone();
            obj.setTableQRIClone(tableQRI);
        }
        return obj;
    }

    /**
     * @return tableQRI.
     */
    public TableQRI getTableQRI()
    {
        return tableQRI;
    }

    /**
     * @param tableQRI the tableQRI to clone and set
     */
    public void setTableQRIClone(TableQRI tableQRI) throws CloneNotSupportedException
    {
        this.tableQRI = (TableQRI)tableQRI.clone();
        this.tableQRI.setTableTree(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        //strictly for the purposes of QueryBldrPane.doSearch()
        return this.getClass().equals(obj.getClass()) && 
            tableInfo.getTableId() == ((TableTree)obj).getTableInfo().getTableId();
    }
    
    /**
     * @return a comma-separated list of the TableIds in path from the root to this. 
     * 
     */
    public String getPathFromRoot()
    {
        String treeStr = String.valueOf(getTableInfo().getTableId());
        TableTree p = getParent();
        while (p != null && p.getTableInfo() != null)
        {
            treeStr = String.valueOf(p.getTableInfo().getTableId()) + "," + treeStr;
            p = p.getParent();
        }
        return treeStr;
    }

}