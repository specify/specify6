/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

import javax.swing.ImageIcon;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.IconManager;

public class TableInfo implements TableListItemIFace, Comparable<TableInfo>
{
    protected DBTableInfo            tableInfo;
    protected Vector<FieldInfo>      fieldItems;
    protected boolean                isExpanded;
    protected ImageIcon              icon;
    
    public TableInfo(final DBTableInfo tableInfo, IconManager.IconSize size)
    {
        this.tableInfo = tableInfo;
        
        icon = IconManager.getIcon(tableInfo.getShortClassName().toLowerCase(), size);
    }

    /**
     * @return the fieldItems
     */
    public Vector<FieldInfo> getFieldItems()
    {
        return fieldItems;
    }

    /**
     * @param fieldItems the fieldItems to set
     */
    public void setFieldItems(Vector<FieldInfo> fieldItems)
    {
        this.fieldItems = fieldItems;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    public String getIconName()
    {
        return tableInfo.getName();
    }
    
    /**
     * @param icon the icon to set
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableInfo obj)
    {
        return tableInfo.toString().compareTo(obj.tableInfo.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getIcon()
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#getText()
     */
    public String getText()
    {
        return tableInfo.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isChecked()
     */
    public boolean isChecked()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpandable()
     */
    public boolean isExpandable()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#isExpanded()
     */
    public boolean isExpanded()
    {
        return isExpanded;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setChecked(boolean)
     */
    public void setChecked(boolean checked)
    {
        // no-op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.TableListItemIFace#setExpanded(boolean)
     */
    public void setExpanded(boolean expand)
    {
        isExpanded = expand;
    }
    
    
}

