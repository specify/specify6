/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.sql.Timestamp;

import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Class representing elements in lists for predefined system coded fields like Agent.agentType, DeterminationStatus.type
 */
public class RecordTypeCodeItem extends Pair<String, Number> implements PickListItemIFace
{
    /**
     * Default constructor.
     */
    public RecordTypeCodeItem()
    {
        super(null, null);
    }
    
    /**
     * @param title
     * @param value
     */
    public RecordTypeCodeItem(final String title, final Number value)
    {
        super(title, value);
    }
    
    /**
     * @return the title.
     */
    public String getTitle()
    {
        return getFirst();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#getId()
     */
    @Override
    public Integer getId()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#getPickList()
     */
    @Override
    public PickListIFace getPickList()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#getTimestampCreated()
     */
    @Override
    public Timestamp getTimestampCreated()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#getValue()
     */
    @Override
    public String getValue()
    {
        return getValueObject().toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#getValueObject()
     */
    @Override
    public Object getValueObject()
    {
        return getSecond();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#setPickList(edu.ku.brc.ui.db.PickListIFace)
     */
    @Override
    public void setPickList(PickListIFace pickList)
    {
        //nuthin
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#setTimestampCreated(java.sql.Timestamp)
     */
    @Override
    public void setTimestampCreated(Timestamp createdDate)
    {
        //nuthin
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title)
    {
        // no
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value)
    {
        // sorry
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListItemIFace#setValueObject(java.lang.Object)
     */
    @Override
    public void setValueObject(Object valueObject)
    {
        // nope
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListItemIFace#getOrdinal()
     */
    @Override
    public Integer getOrdinal()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListItemIFace#setOrdinal(java.lang.Integer)
     */
    @Override
    public void setOrdinal(Integer ordinal)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.PickListItemIFace#SetPickList(edu.ku.brc.af.ui.db.PickListIFace)
     */
    @Override
    public void SetPickList(PickListIFace pickList)
    {
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(PickListItemIFace o)
    {
        return getTitle().compareTo(o.getTitle());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Pair#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }
        
    
}
