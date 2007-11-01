/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;

public class TableInfoRenderable implements TableNameRendererIFace, Comparable<TableInfoRenderable>
{
    public enum SortBy {Table, Field, TableAndField}
    
    protected DBTableInfo tableInfo;
    protected DBFieldInfo fieldInfo;
    protected String      title;
    protected String      iconName;
    protected SortBy      sortBy = SortBy.Table; 
    protected Object      userData = null;
    
    public TableInfoRenderable(final DBTableInfo tableInfo)
    {
        this.tableInfo = tableInfo;
        this.title     = tableInfo.getShortClassName();
        this.iconName  = tableInfo.getClassObj().getSimpleName();
    }

    public TableInfoRenderable(final DBFieldInfo fieldInfo)
    {
        this.tableInfo = fieldInfo.getTableInfo();
        this.fieldInfo = fieldInfo;
        this.title     = fieldInfo.getColumn();
        this.iconName  = tableInfo.getClassObj().getSimpleName();
    }

    public TableInfoRenderable(final DBTableInfo tableInfo,
                               final String                 title,
                               final String                 iconName)
    {
        this.tableInfo = tableInfo;
        this.title     = title;
        this.iconName  = iconName;
    }

    public TableInfoRenderable(final String title,
                               final String iconName)
    {
        this(null, title, iconName);
        this.fieldInfo = null;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * @return the fieldInfo
     */
    public DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getIconName()
     */
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * @param sortBy the sortBy to set
     */
    public void setSortBy(final SortBy sortBy)
    {
        this.sortBy = sortBy;
    }

    /**
     * @return the userData
     */
    public Object getUserData()
    {
        return userData;
    }

    /**
     * @param userData the userData to set
     */
    public void setUserData(Object userData)
    {
        this.userData = userData;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableInfoRenderable obj)
    {
        switch (sortBy)
        {
            case Table:
                return tableInfo.getShortClassName().compareTo(obj.tableInfo.getShortClassName());
                
            case Field:
                return fieldInfo.getColumn().compareTo(obj.fieldInfo.getColumn());
                
            case TableAndField:
                {
                    String first  = tableInfo.getShortClassName() + fieldInfo.getColumn();
                    String second = obj.tableInfo.getShortClassName() + obj.fieldInfo.getColumn();
                    return first.compareTo(second);
                }
        }
        return title.compareTo(obj.title);
    }
}
