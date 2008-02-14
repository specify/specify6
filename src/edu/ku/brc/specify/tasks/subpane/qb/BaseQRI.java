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

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class BaseQRI implements QryListRendererIFace, Comparable<QryListRendererIFace>, Cloneable
{
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected TableTree tableTree;
    protected String    iconName;
    protected String    title;
    protected Boolean   isInUse = null;
    
    public BaseQRI(final TableTree tableTree)
    {
        this.tableTree = tableTree;
    }

    public boolean isInUse()
    {
        return isInUse != null && isInUse;
    }
    
    /**
     * @return the isInUse
     */
    public Boolean getIsInUse()
    {
        return isInUse;
    }

    /**
     * @param isInUse the isInUse to set
     */
    public void setIsInUse(Boolean isInUse)
    {
        this.isInUse = isInUse;
    }

    /**
     * @return the tableTree
     */
    public TableTree getTableTree()
    {
        return tableTree;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(QryListRendererIFace qri)
    {
        //System.out.println(qri);
        //System.out.println(title+"]["+qri.getTitle());
        return title.compareTo(qri.getTitle());
    }
    
    public String getIconName()
    {
        return iconName;
    }
    
    public String getTitle()
    {
        return title;
    }
    public boolean hasChildren()
    {
        return true;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * @param tableTree the tableTree to set
     */
    public void setTableTree(TableTree tableTree)
    {
        this.tableTree = tableTree;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        //strictly for the purposes of QueryBldrPane.doSearch()
        return this.getClass().equals(obj.getClass()) && this.tableTree.equals(((BaseQRI)obj).tableTree);
    }
    
}
