/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class BldrPickListItem
{
    // Fields
    private String title;
    private String value;
    private Integer ordinal;

    // Constructors

    /** default constructor */
    public BldrPickListItem()
    {
        // do nothing
    }

    public BldrPickListItem(final String title, final String value)
    {
        super();
        this.title = title;
        this.value = value;
    }

    public BldrPickListItem(final String title)
    {
        super();
        this.title       = title;
        this.value       = null;
    }

    /**
     * 
     */
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * 
     */
    public String getValue()
    {
        return this.value == null ? title : value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
    
    /**
     * @return the ordinal
     */
    public Integer getOrdinal()
    {
        return ordinal;
    }

    /**
     * @param ordinal the ordinal to set
     */
    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return title;
    }
}
