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

    public ImageIcon getIcon();
    
    public String getText();
    
    // For Tables
    public boolean isExpandable();
    
    public boolean isExpanded();
    
    public void setExpanded(boolean expand);
    
    // For Fields
    public boolean isChecked();
    
    public void setChecked(boolean checked);
    
    
}
