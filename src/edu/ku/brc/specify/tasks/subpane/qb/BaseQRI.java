/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.qb;

/**
 * A base class for items being rendered in a list.
 * 
 * @author rod
 *
 * @code_status alpha
 *
 * Oct 18, 2007
 *
 */
public class BaseQRI implements QryListRendererIFace, Comparable<QryListRendererIFace>, Cloneable
{
    protected TableTree tableTree;
    protected String    iconName;
    protected String    title;
    protected Boolean   isInUse = false;
    
    /**
     * @param tableTree
     */
    public BaseQRI(final TableTree tableTree)
    {
        this.tableTree = tableTree;
    }

    /**
     * @return whether it is in use
     */
    public boolean isInUse()
    {
        return isInUse != null && isInUse;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#getIsInUse()
     */
    public Boolean getIsInUse()
    {
        return isInUse;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#setIsInUse(java.lang.Boolean)
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
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#getIconName()
     */
    public String getIconName()
    {
        return iconName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#hasChildren()
     */
    public boolean hasChildren()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QryListRendererIFace#hasMultiChildren()
     */
    public boolean hasMultiChildren()
    {
        return false;
    }

    /**
     * @param tableTree the tableTree to set
     */
    public void setTableTree(TableTree tableTree)
    {
        this.tableTree = tableTree;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        //strictly for the purposes of QueryBldrPane.doSearch()
    	if (obj == null)
    	{
    		return false;
    		
    	}
        return this.getClass().equals(obj.getClass()) && this.tableTree != null
            && ((BaseQRI)obj).tableTree != null && this.tableTree.equals(((BaseQRI)obj).tableTree);
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
    

}
