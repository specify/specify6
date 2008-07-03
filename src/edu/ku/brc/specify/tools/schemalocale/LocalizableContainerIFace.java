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

import java.util.Collection;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public interface LocalizableContainerIFace extends LocalizableItemIFace, Comparable<LocalizableContainerIFace>
{
    
    public abstract Integer getId();
    
    /**
     * @param item
     */
    public abstract void addItem(LocalizableItemIFace item);
    
    /**
     * @param item
     */
    public abstract void removeItem(LocalizableItemIFace item);
    
    /**
     * @return
     */
    public abstract Collection<LocalizableItemIFace> getContainerItems();
    
    /**
     * @param name
     * @return
     */
    public abstract LocalizableItemIFace getItemByName(String name);
    
    /**
     * @return the aggregator name
     */
    public String getAggregator();
    
    /**
     * @param aggregator the aggregator to set
     */
    public void setAggregator(String aggregator);
}
