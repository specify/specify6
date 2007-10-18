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
public class BaseQRI implements QryListRendererIFace, Comparable<QryListRendererIFace>
{
    protected BaseQRI   parent;
    protected TableTree tableTree;
    protected String    iconName;
    protected String    title;
    protected Boolean   isInUse = null;
    
    public BaseQRI(final BaseQRI parent, final TableTree tableTree)
    {
        this.parent    = parent;
        this.tableTree = tableTree;
    }

    public boolean isInUse()
    {
        return isInUse != null && isInUse;
    }
    
    /**
     * @return the parent
     */
    public BaseQRI getParent()
    {
        return parent;
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
}
