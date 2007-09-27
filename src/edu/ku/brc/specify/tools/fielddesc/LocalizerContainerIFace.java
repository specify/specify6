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

import java.util.List;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public interface LocalizerContainerIFace extends LocalizableNameDescIFace, Comparable<LocalizerContainerIFace>
{
    public abstract List<LocalizableNameDescIFace> getItems();
    
    public abstract LocalizableNameDescIFace getItemByName(String name);
}
