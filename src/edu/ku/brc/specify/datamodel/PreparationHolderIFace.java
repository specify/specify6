/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.datamodel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 9, 2008
 *
 */
public interface PreparationHolderIFace
{

    /**
     * @return provides a single preparation
     */
    public abstract Preparation getPreparation();
    
    public abstract Integer getId();
    
    public abstract Integer getQuantity();
    
    public abstract Integer getQuantityReturned();
    
}
