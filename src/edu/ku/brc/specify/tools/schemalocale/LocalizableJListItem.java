/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import org.apache.commons.lang.StringUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public class LocalizableJListItem implements Comparable<LocalizableJListItem>
{
    
    protected String name;
    protected Integer id;
    protected String displayName;

    public LocalizableJListItem(String name, Integer id, String displayName)
    {
        super();
        this.name = name;
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return displayName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return StringUtils.isNotEmpty(displayName) ? displayName : name;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(LocalizableJListItem o)
    {
        return toString().compareTo(o.toString());
    }
}
