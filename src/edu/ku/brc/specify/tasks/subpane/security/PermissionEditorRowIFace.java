/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    public abstract ImageIcon getIcon();
    
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
     * @return the override text or null
     */
    public abstract String getOverrideText(int option);
    
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
	 * @return the list of permission objects
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
