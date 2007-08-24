/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.af.core.web;


public class FieldDisplayInfo
{
    protected Integer  order;
    protected String   name;
    protected String   pickList       = null;
    protected boolean  isSkipped      = false;
    protected boolean  availForSearch = true;
    
    public FieldDisplayInfo(final Integer order, 
                            final String  name, 
                            final String  pickList)
    {
        super();
        this.order = order;
        this.name = name;
        this.pickList = pickList;
    }

    public FieldDisplayInfo(final String name, 
                            final boolean availForSearch)
    {
        this(null, name, null);
        this.availForSearch = availForSearch;
    }
    
    /**
     * @return the order
     */
    public Integer getOrder()
    {
        return order;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the pickList
     */
    public String getPickList()
    {
        return pickList;
    }

    /**
     * @return the isSkipped
     */
    public boolean isSkipped()
    {
        return isSkipped;
    }

    /**
     * @param isSkipped the isSkipped to set
     */
    public void setSkipped(boolean isSkipped)
    {
        this.isSkipped = isSkipped;
    }

    /**
     * @return the availForSearch
     */
    public boolean isAvailForSearch()
    {
        return availForSearch;
    }

    /**
     * @param availForSearch the availForSearch to set
     */
    public void setAvailForSearch(boolean availForSearch)
    {
        this.availForSearch = availForSearch;
    }
}