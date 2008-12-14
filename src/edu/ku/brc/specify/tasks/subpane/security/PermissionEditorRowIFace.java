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
import edu.ku.brc.af.core.PermissionIFace;
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
	public abstract List<PermissionIFace> getPermissions();
	
	/**
	 * @param permSettings
	 */
	public abstract void setPermissions(List<PermissionIFace> permSettings);
	
	/**
	 * @param session
	 */
	public abstract List<SpPermission> getPermissionList();
	
	/**
	 * @param oldPerm
	 * @param newPerm
	 */
	public abstract void updatePerm(SpPermission oldPerm, SpPermission newPerm);
	
	/**
	 * Indicates whether the information being displayed is from an administrator.
	 * That will affect the way permissions can be set or displayed, i.e., administrators 
	 * have the right to do anything in the system. 
	 */
	public abstract boolean isAdminPrincipal();
	public abstract void setAdminPrincipal(boolean isAdminPrincipal);
	
}
