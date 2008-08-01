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
 * An interface for rendering Query items in a list.
 * 
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
     * @return the icon name
     */
    public String getIconName();
    
    /**
     * @return the textual title
     */
    public String getTitle();
    
    /**
     * @return whether it has children
     */
    public boolean hasChildren();
    
    /**
     * This should only be called if hasChildren is true.
     * 
     * @return false means it has only a single child (OneToOne or ManyToOne),
     * true means there are many children (ManyToMany, OneToMany)
     */
    public boolean hasMultiChildren();
    
    /**
     * @return whether it is in use
     */
    public Boolean getIsInUse();
    
    /**
     * @param isInUse set whether it is in use
     */
    public void setIsInUse(Boolean isInUse);
    
}