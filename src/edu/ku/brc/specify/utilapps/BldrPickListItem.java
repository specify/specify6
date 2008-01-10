/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

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

    // Constructors

    /** default constructor */
    public BldrPickListItem()
    {
        // do nothing
    }

    public BldrPickListItem(final String title, final String value, final Timestamp timestampCreated)
    {
        super();
        this.title = title;
        this.value = value;
    }

    public BldrPickListItem(final String title, final Object valueObject, final Timestamp timestampCreated)
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return title;
    }
}
