/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public interface QryListRendererIFace
{
    /**
     * @return
     */
    public String getIconName();
    /**
     * @return
     */
    public String getTitle();
    /**
     * @return
     */
    public boolean hasChildren();
    
    /**
     * @return
     */
    public Boolean getIsInUse();
    /**
     * @param isInUse
     */
    public void setIsInUse(Boolean isInUse);
    
}