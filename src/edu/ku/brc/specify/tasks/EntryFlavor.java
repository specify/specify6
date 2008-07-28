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
 * Class that describes a DataFlavorTableExt for a NavButton as defined in XML.
 *  
 * @author rod
 *
 * @code_status Beta
 *
 * Jul 25, 2008
 *
 */
public class EntryFlavor implements Cloneable
{
    protected boolean         isDraggableFlavor;
    protected String          className;
    protected String          humanReadable;
    protected Vector<Integer> tableIds = new Vector<Integer>();
    
    public EntryFlavor(String  className, 
                       String humanReadable,
                       boolean isDraggableFlavor, 
                       int[]   dndTableIds)
    {
        this.className         = className;
        this.humanReadable     = humanReadable;
        this.isDraggableFlavor = isDraggableFlavor;
        
        if (dndTableIds != null)
        {
            for (int id : dndTableIds)
            {
                tableIds.add(id);
            }
        }
    }
    
    /**
     * @return the isDraggableFlavor
     */
    public boolean isDraggableFlavor()
    {
        return isDraggableFlavor;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return the humanReadable
     */
    public String getHumanReadable()
    {
        return humanReadable;
    }

    /**
     * @param humanReadable the humanReadable to set
     */
    public void setHumanReadable(String humanReadable)
    {
        this.humanReadable = humanReadable;
    }

    /**
     * @return the tableIds
     */
    public Vector<Integer> getTableIds()
    {
        return tableIds;
    }

    /**
     * @param isDraggableFlavor the isDraggableFlavor to set
     */
    public void setDraggableFlavor(boolean isDraggableFlavor)
    {
        this.isDraggableFlavor = isDraggableFlavor;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * @param tableIds the tableIds to set
     */
    public void setTableIds(Vector<Integer> tableIds)
    {
        this.tableIds = tableIds;
    }
    
    /**
     * @return
     */
    public Class<?> getDataFlavorClass()
    {
        try
        {
            return Class.forName(className);
            
        } catch (Exception ex)
        {
            System.err.println(className);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @return
     */
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
    
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        EntryFlavor ef = (EntryFlavor)super.clone();
        ef.className         = className;
        ef.humanReadable     = humanReadable;
        ef.isDraggableFlavor = isDraggableFlavor;
        ef.tableIds          = new Vector<Integer>(tableIds); // doing this instead of clone
        return ef;
    }


    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("flavor", EntryFlavor.class);
        
        xstream.useAttributeFor(EntryFlavor.class, "isDraggableFlavor");
        xstream.useAttributeFor(EntryFlavor.class, "className");
        xstream.useAttributeFor(EntryFlavor.class, "humanReadable");
        
        xstream.aliasAttribute(EntryFlavor.class, "className",  "classname");
        xstream.aliasAttribute(EntryFlavor.class, "humanReadable",  "humanreadable");
        xstream.aliasAttribute(EntryFlavor.class, "isDraggableFlavor",  "isdrag");
    }

}
