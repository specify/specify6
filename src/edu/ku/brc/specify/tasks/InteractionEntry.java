/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
 * A class for reading and writing the configuration of the sidebar for Interactions.
 * @author rod
 *
 * @code_status Beta
 *
 * Apr 16, 2008
 *
 */
public class InteractionEntry implements TaskConfigItemIFace, Comparable<TaskConfigItemIFace>, Cloneable
{
    protected String  name;
    protected String  tableName;
    protected String  labelKey;  // Key needed for localization
    protected String  viewName;
    protected String  cmdType;
    protected String  action;
    protected String  iconName;
    protected String  tooltip;
    protected boolean isOnLeft;
    protected boolean isSearchService;
    protected boolean isEnabled;
    protected boolean isVisible;
    protected int     order;
    
    protected Vector<EntryFlavor> draggableFlavors = new Vector<EntryFlavor>();
    protected Vector<EntryFlavor> droppableFlavors = new Vector<EntryFlavor>();
    
    // Transient
    protected String title;
    protected String i18NTooltip;
    
    /**
     * 
     */
    public InteractionEntry()
    {
        super();
        this.isEnabled = true;
    }

    public void addDraggable(final Class<?> cls, final String humanReadable, final int[] dndTableIds)
    {
        draggableFlavors.add(new EntryFlavor(cls.getName(), humanReadable, true, dndTableIds));
    }
    
    public void addDroppable(final Class<?> cls, final String humanReadable, final int[] dndTableIds)
    {
        droppableFlavors.add(new EntryFlavor(cls.getName(), humanReadable, false, dndTableIds));
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
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * @return the labelKey
     */
    public String getLabelKey()
    {
        return labelKey;
    }

    /**
     * @param labelKey the labelKey to set
     */
    public void setLabelKey(String labelKey)
    {
        this.labelKey = labelKey;
    }

    /**
     * @return the viewName
     */
    public String getViewName()
    {
        return viewName;
    }

    /**
     * @param viewName the viewName to set
     */
    public void setViewName(String viewName)
    {
        this.viewName = viewName;
    }

    /**
     * @return the cmdType
     */
    public String getCmdType()
    {
        return cmdType;
    }

    /**
     * @param cmdType the cmdType to set
     */
    public void setCmdType(String cmdType)
    {
        this.cmdType = cmdType;
    }

    /**
     * @return the action
     */
    public String getAction()
    {
        return action;
    }


    /**
     * @param action the action to set
     */
    public void setAction(String action)
    {
        this.action = action;
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
     * @return the order
     */
    public int getOrder()
    {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    /**
     * @return the tooltip
     */
    public String getTooltip()
    {
        return tooltip;
    }

    /**
     * @return the draggableFlavors
     */
    public Vector<EntryFlavor> getDraggableFlavors()
    {
        return draggableFlavors;
    }

    /**
     * @param draggableFlavors the draggableFlavors to set
     */
    public void setDraggableFlavors(Vector<EntryFlavor> draggableFlavors)
    {
        this.draggableFlavors = draggableFlavors;
    }

    /**
     * @return the droppableFlavors
     */
    public Vector<EntryFlavor> getDroppableFlavors()
    {
        return droppableFlavors;
    }

    /**
     * @param droppableFlavors the droppableFlavors to set
     */
    public void setDroppableFlavors(Vector<EntryFlavor> droppableFlavors)
    {
        this.droppableFlavors = droppableFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#isOnLeft()
     */
    public boolean isOnLeft()
    {
        return isOnLeft;
    }

    /**
     * @param isOnLeft the isOnLeft to set
     */
    public void setOnLeft(boolean isOnLeft)
    {
        this.isOnLeft = isOnLeft;
    }

    /**
     * @return the isSearchService
     */
    public boolean isSearchService()
    {
        return isSearchService;
    }

    /**
     * @param isSearchService the isSearchService to set
     */
    public void setSearchService(boolean isSearchService)
    {
        this.isSearchService = isSearchService;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title != null ? title : labelKey;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the i18NTooltip
     */
    public String getI18NTooltip()
    {
        return i18NTooltip;
    }

    /**
     * @param tooltip the i18NTooltip to set
     */
    public void setI18NTooltip(String tooltip)
    {
        i18NTooltip = tooltip;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#isVisible()
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /**
     * @param isVisible the isVisible to set
     */
    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    /**
     * @return the isEnabled
     */
    public boolean isEnabled()
    {
        return isEnabled;
    }

    /**
     * @param isEnabled the isEnabled to set
     */
    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
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
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return title == null ? name : title;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        InteractionEntry entry = (InteractionEntry)super.clone();
        
        entry.draggableFlavors = new Vector<EntryFlavor>();
        entry.droppableFlavors = new Vector<EntryFlavor>();

        for (EntryFlavor ef : draggableFlavors)
        {
            entry.draggableFlavors.add((EntryFlavor)ef.clone());
        }
        for (EntryFlavor ef : droppableFlavors)
        {
            entry.droppableFlavors.add((EntryFlavor)ef.clone());
        }
        return entry;
    }
    
    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("entry", InteractionEntry.class);
        
        xstream.useAttributeFor(InteractionEntry.class, "name");
        xstream.useAttributeFor(InteractionEntry.class, "tableName");
        xstream.useAttributeFor(InteractionEntry.class, "labelKey");
        xstream.useAttributeFor(InteractionEntry.class, "action");
        xstream.useAttributeFor(InteractionEntry.class, "iconName");
        xstream.useAttributeFor(InteractionEntry.class, "isOnLeft");
        xstream.useAttributeFor(InteractionEntry.class, "viewName");
        xstream.useAttributeFor(InteractionEntry.class, "cmdType");
        xstream.useAttributeFor(InteractionEntry.class, "order");
        xstream.useAttributeFor(InteractionEntry.class, "isSearchService");
        xstream.useAttributeFor(InteractionEntry.class, "tooltip");
        xstream.useAttributeFor(InteractionEntry.class, "isEnabled");
        xstream.useAttributeFor(InteractionEntry.class, "isVisible");
        
        xstream.aliasAttribute(InteractionEntry.class, "tableName", "table");
        xstream.aliasAttribute(InteractionEntry.class, "labelKey",  "label");
        xstream.aliasAttribute(InteractionEntry.class, "iconName",  "icon");
        xstream.aliasAttribute(InteractionEntry.class, "isOnLeft",  "isonleft");
        xstream.aliasAttribute(InteractionEntry.class, "viewName",  "view");
        xstream.aliasAttribute(InteractionEntry.class, "cmdType",   "type");
        xstream.aliasAttribute(InteractionEntry.class, "isSearchService", "issrchsrv");
        xstream.aliasAttribute(InteractionEntry.class, "isEnabled", "enabled");
        xstream.aliasAttribute(InteractionEntry.class, "isVisible", "visible");
        
        xstream.omitField(InteractionEntry.class, "title");
        xstream.omitField(InteractionEntry.class, "i18NTooltip");
    }

}
