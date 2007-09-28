/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

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
    public void addItem(LocalizableItemIFace item);
    
    public void removeItem(LocalizableItemIFace item);
    
    public Collection<LocalizableItemIFace> getContainerItems();
    
    public abstract LocalizableItemIFace getItemByName(String name);
}
