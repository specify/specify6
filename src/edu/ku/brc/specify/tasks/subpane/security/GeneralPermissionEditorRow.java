/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.specify.datamodel.SpPermission;


/**
 * Wraps an SpPermission object and adds functionality needed to display and handle it when edited 
 * in a permission table.
 *  
 * @author Ricardo
 *
 */
public class GeneralPermissionEditorRow implements PermissionEditorRowIFace
{
	protected SpPermission permission;
	protected SpPermission overrulingPermission;
	protected String title;
	protected String description;
	
	public GeneralPermissionEditorRow(
			SpPermission permission, 
			SpPermission overrulingPermission,
			String title,
			String description) 
	{
		this.permission 			= permission;
		this.overrulingPermission 	= overrulingPermission;
		this.title					= title;
		this.description			= description;
	}
	
	public SpPermission getPermission() 
	{
		return permission;
	}
	
	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public void addTableRow(DefaultTableModel model, ImageIcon icon)
	{
		// FIXME: adjust this to work with 3-state checkbox
		model.addRow(new Object[] 
		        				{ 
		        					icon, this, 
		        					new Boolean(permission.canView()), 
		        					new Boolean(permission.canAdd()), 
		        					new Boolean(permission.canModify()), 
		        					new Boolean(permission.canDelete())
		        				}
		        			);
	}
	
	public String toString()
	{
		return getTitle();
	}
}
