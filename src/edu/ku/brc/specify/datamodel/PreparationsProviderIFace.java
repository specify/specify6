/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Set;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 9, 2008
 *
 */
public interface PreparationsProviderIFace
{

    /**
     * @return a set of PreparationHolderIFace objects =
     */
    public abstract Set<PreparationHolderIFace> getPreparations();
    
    
    public abstract int getTableId();

}
