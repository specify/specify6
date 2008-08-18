/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.core.db;


/**
 * Interface for common methods for Fields and Relationships for a DBTableInfo.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 14, 2008
 *
 */
public interface DBTableChildIFace
{
    /**
     * @return the description
     */
    public abstract String getDescription();
    
    /**
     * @param description the description
     */
    public abstract void setDescription(String description);

    /**
     * @return the name
     */
    public abstract String getTitle();
    
    /**
     * @param title the title
     */
    public abstract void setTitle(String title);

    /**
     * @return the name
     */
    public abstract String getName();

    /**
     * @return the isHidden
     */
    public abstract boolean isHidden();
    
    /**
     * @param isHidden whether it is hidden
     */
    public abstract void setHidden(boolean isHidden);
    
    /**
     * @return whether it can be updated or not
     */
    public abstract boolean isUpdatable();
    
    /**
     * @return whether the field is required
     */
    public abstract boolean isRequired();
    
    /**
     * @return the data class of the child.
     */
    public abstract Class<?> getDataClass();
}
