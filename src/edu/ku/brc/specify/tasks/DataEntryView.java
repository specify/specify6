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
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.dbsupport.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 27, 2008
 *
 */
public class DataEntryView implements Comparable<DataEntryView>
{
    protected String  name;
    protected String  viewSet;
    protected String  view;
    protected String  iconName;
    protected String  toolTip;
    protected boolean isSideBar;
    
    protected DBTableInfo tableInfo = null;
    
    /**
     * @param name
     * @param viewSet
     * @param view
     * @param iconName
     * @param toolTip
     * @param isSideBar
     */
    public DataEntryView(String name, 
                         String viewSet, 
                         String view, 
                         String iconName, 
                         String toolTip,
                         boolean isSideBar)
    {
        super();
        this.name = name;
        this.viewSet = viewSet;
        this.view = view;
        this.iconName = iconName;
        this.toolTip = toolTip;
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
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
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
        this.tableInfo = tableInfo;
    }

    /**
     * @return the viewSet
     */
    public String getViewSet()
    {
        return viewSet;
    }
    /**
     * @param viewSet the viewSet to set
     */
    public void setViewSet(String viewSet)
    {
        this.viewSet = viewSet;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return tableInfo != null ? tableInfo.getTitle() : name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DataEntryView o)
    {
        return toString().compareTo(o.toString());
    }
    
    /**
     * Configures inner classes for XStream.
     * @param xstream the xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("view",  DataEntryView.class);
        
        xstream.useAttributeFor(DataEntryView.class, "name");
        xstream.useAttributeFor(DataEntryView.class, "viewSet");
        xstream.useAttributeFor(DataEntryView.class, "view");
        xstream.useAttributeFor(DataEntryView.class, "iconName");
        xstream.useAttributeFor(DataEntryView.class, "toolTip");
        xstream.useAttributeFor(DataEntryView.class, "isSideBar");
        
        xstream.omitField(DataEntryView.class, "tableInfo");
        
        xstream.aliasAttribute(DataEntryView.class, "viewSet", "viewset");
        xstream.aliasAttribute(DataEntryView.class, "iconName", "iconname");
        xstream.aliasAttribute(DataEntryView.class, "toolTip", "tooltip");
        xstream.aliasAttribute(DataEntryView.class, "isSideBar", "sidebar");
    }
}