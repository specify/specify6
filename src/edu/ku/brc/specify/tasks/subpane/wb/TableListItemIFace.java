/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb;

import javax.swing.ImageIcon;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 6, 2007
 *
 */
public interface TableListItemIFace
{

    /**
     * @return
     */
    public abstract ImageIcon getIcon();
    
    /**
     * @return
     */
    public abstract String getText();
    
    //public abstract String getTitle();
    
    
    //------------------------------
    // For Tables
    //------------------------------
    /**
     * @return
     */
    public abstract boolean isExpandable();
    
    /**
     * @return
     */
    public abstract boolean isExpanded();
    
    /**
     * @param expand
     */
    public abstract void setExpanded(boolean expand);
    
    //------------------------------
    // For Fields
    //------------------------------
    
    /**
     * @return
     */
    public abstract boolean isChecked();
    
    /**
     * @param checked
     */
    public abstract void setChecked(boolean checked);
    
    
}
