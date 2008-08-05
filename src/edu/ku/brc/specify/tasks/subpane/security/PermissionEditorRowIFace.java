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
public interface PermissionEditorRowIFace 
{
	public void addTableRow(DefaultTableModel model, ImageIcon icon);
	
	public String getTitle();
	
	public String getDescription();
}
