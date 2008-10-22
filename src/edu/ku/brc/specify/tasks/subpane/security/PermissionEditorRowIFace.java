/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.specify.datamodel.SpPermission;

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
    public ImageIcon getIcon();
    
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
	
	/**
	 * @return
	 */
	public abstract PermissionEditorIFace getEditorPanel();
	
	/**
	 * @return
	 */
	public abstract List<PermissionSettings> getPermissions();
	
	/**
	 * @param permSettings
	 */
	public abstract void setPermissions(List<PermissionSettings> permSettings);
	
	/**
	 * @param session
	 */
	public abstract List<SpPermission> getPermissionList();
}
