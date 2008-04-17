/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
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
    protected boolean isOn;
    protected int     order;
    
    protected Vector<Integer> tableIds = new Vector<Integer>();
    
    // Transient
    protected String title;
    
    /**
     * 
     */
    public InteractionEntry()
    {
        super();
    }

    /**
     * @param name
     * @param labelKey
     * @param viewName
     * @param action
     * @param iconName
     * @param isOn
     */
    public InteractionEntry(String name, 
                            String tableName, 
                            String labelKey, 
                            String viewName, 
                            String cmdType,
                            String action,
                            String iconName,
                            int[] dropTableIds)
    {
        super();
        this.name = name;
        this.tableName = tableName;
        this.labelKey = labelKey;
        this.viewName = viewName;
        this.cmdType = cmdType;
        this.action = action;
        this.iconName = iconName;
        this.isOn = true;
        
        if (dropTableIds != null)
        {
            for (int id : dropTableIds)
            {
                tableIds.add(id);
            }
        }
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
     * @return the isOn
     */
    public boolean isOn()
    {
        return isOn;
    }


    /**
     * @param isOn the isOn to set
     */
    public void setOn(boolean isOn)
    {
        this.isOn = isOn;
    }


    /**
     * @return the tableIds
     */
    public Vector<Integer> getTableIds()
    {
        return tableIds;
    }
    
    public int[] getTableIdsAsArray()
    {
        int[] ids = new int[tableIds.size()];
        int i = 0;
        for (int id : tableIds)
        {
            ids[i++] = id;
        }
        return ids;
    }

    /**
     * @param tableIds the tableIds to set
     */
    public void setTableIds(Vector<Integer> tableIds)
    {
        this.tableIds = tableIds;
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
        xstream.useAttributeFor(InteractionEntry.class, "isOn");
        xstream.useAttributeFor(InteractionEntry.class, "viewName");
        xstream.useAttributeFor(InteractionEntry.class, "cmdType");
        xstream.useAttributeFor(InteractionEntry.class, "order");
        
        xstream.aliasAttribute(InteractionEntry.class, "name",      "name");
        xstream.aliasAttribute(InteractionEntry.class, "tableName", "table");
        xstream.aliasAttribute(InteractionEntry.class, "labelKey",  "labelKey");
        xstream.aliasAttribute(InteractionEntry.class, "action",    "action");
        xstream.aliasAttribute(InteractionEntry.class, "iconName",  "table");
        xstream.aliasAttribute(InteractionEntry.class, "isOn",      "isturnedon");
        xstream.aliasAttribute(InteractionEntry.class, "viewName",  "view");
        xstream.aliasAttribute(InteractionEntry.class, "cmdType",   "type");
        xstream.aliasAttribute(InteractionEntry.class, "order",   "order");
        
        xstream.omitField(InteractionEntry.class, "title");
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigItemIFace#isSideBar()
     */
    public boolean isSideBar()
    {
        return isOn;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
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
        entry.name     = name;
        entry.tableName = tableName;
        entry.labelKey = labelKey;  // Key needed for localization
        entry.viewName = viewName;
        entry.cmdType  = cmdType;
        entry.action   = action;
        entry.iconName = action;
        entry.isOn     = isOn;
        entry.order    = order;
        entry.tableIds = new Vector<Integer>(tableIds); // doing this instead of clone
        return entry;
    }
    
}
