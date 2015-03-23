/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 27, 2008
 *
 */
public class DataEntryView implements TaskConfigItemIFace, Comparable<TaskConfigItemIFace>, Cloneable
{
    protected String  title;
    protected String  view;
    protected String  iconName;
    protected String  toolTip;
    protected int     order;
    protected boolean isSideBar;
    
    // Transient
    protected DBTableInfo tableInfo = null;
    
    /**
     * @param kingdomTaxonName
     * @param viewSet
     * @param view
     * @param iconName
     * @param toolTip
     * @param isSideBar
     */
    public DataEntryView(String title, 
                         String view, 
                         String iconName, 
                         String toolTip,
                         int     order,
                         boolean isSideBar)
    {
        super();
        this.title = title;
        this.view = view;
        this.iconName = iconName;
        this.toolTip = toolTip;
        this.order = order;
        this.isSideBar = isSideBar;
    }

    /**
     * 
     */
    public DataEntryView()
    {
        super();
    }
    
    /**
     * @param name the name to set
     */
    public void setTitle(String name)
    {
        this.title = name;
    }


    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * @param tableInfo the tableInfo to set
     */
    public void setTableInfo(DBTableInfo tableInfo)
    {
        if (tableInfo == null)
        {
            throw new RuntimeException("tableInfo is null!");
        }
        this.tableInfo = tableInfo;
    }

    /**
     * @return the view
     */
    public String getView()
    {
        return view;
    }
    /**
     * @param view the view to set
     */
    public void setView(String view)
    {
        this.view = view;
    }
    /**
     * @return the iconName
     */
    public String getIconName()
    {
        return iconName;
    }
    /**
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }
    /**
     * @return the toolTip
     */
    public String getToolTip()
    {
        return toolTip;
    }
    /**
     * @param toolTip the toolTip to set
     */
    public void setToolTip(String toolTip)
    {
        this.toolTip = toolTip;
    }
    /**
     * @return the isSideBar
     */
    public boolean isSideBar()
    {
        return isSideBar;
    }
    /**
     * @param isSideBar the isSideBar to set
     */
    public void setSideBar(boolean isSideBar)
    {
        this.isSideBar = isSideBar;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#isOnLeft()
     */
    public boolean isOnLeft()
    {
        return false; // isn't used
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#getOrder()
     */
    public int getOrder()
    {
        return order;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#setOrder(int)
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#isVisible()
     */
    public boolean isVisible()
    {
        return isSideBar;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return StringUtils.isNotEmpty(title) ? title : tableInfo != null ? tableInfo.getTitle() : view;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TaskConfigItemIFace o)
    {
        Integer o1 = getOrder();
        Integer o2 = o.getOrder();
        return o1.compareTo(o2);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        DataEntryView dev = (DataEntryView)super.clone();
        dev.title      = title;
        dev.view      = view;
        dev.iconName  = iconName;
        dev.toolTip   = toolTip;
        dev.order     = order;
        dev.isSideBar = isSideBar;
        return dev;
    }

    /**
     * Configures inner classes for XStream.
     * @param xstream the xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("view",  DataEntryView.class);
        
        xstream.useAttributeFor(DataEntryView.class, "title");
        xstream.useAttributeFor(DataEntryView.class, "view");
        xstream.useAttributeFor(DataEntryView.class, "iconName");
        xstream.useAttributeFor(DataEntryView.class, "toolTip");
        xstream.useAttributeFor(DataEntryView.class, "isSideBar");
        xstream.useAttributeFor(DataEntryView.class, "order");
        
        xstream.omitField(DataEntryView.class, "tableInfo");
        
        xstream.aliasAttribute(DataEntryView.class, "iconName", "iconname");
        xstream.aliasAttribute(DataEntryView.class, "toolTip", "tooltip");
        xstream.aliasAttribute(DataEntryView.class, "isSideBar", "sidebar");
    }
}
