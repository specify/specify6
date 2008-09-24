/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

/**
 * Interface for wrapping each row for permission editors
 *  
 * @author Ricardo
 *
 */
public interface PermissionEditorRowIFace extends Comparable<PermissionEditorRowIFace>
{
    /**
     * @return
     */
    public abstract String getType();
    
	/**
	 * @param model
	 * @param icon
	 */
	public abstract void addTableRow(DefaultTableModel model, ImageIcon icon);
	
	/**
	 * @return
	 */
	public abstract String getTitle();
	
	/**
	 * @return
	 */
	public abstract String getDescription();
}
