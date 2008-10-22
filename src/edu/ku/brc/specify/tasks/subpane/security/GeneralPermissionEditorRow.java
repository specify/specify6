/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
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
    protected String               type;
	protected SpPermission         permission;
	protected SpPermission         overrulingPermission;
	protected String               title;
	protected String               description;
	protected ImageIcon            icon;
	protected PermissionEditorIFace editorPanel;
	
	/**
	 * @param permission
	 * @param overrulingPermission
	 * @param type
	 * @param title
	 * @param description
	 * @param icon
	 * @param editorPanel
	 */
	public GeneralPermissionEditorRow(final SpPermission permission, 
			                          final SpPermission overrulingPermission,
                                      final String       type,
                                      final String       title,
                                      final String       description,
                                      final ImageIcon    icon,
                                      final PermissionEditorIFace editorPanel) 
	{
		this.permission 			= permission;
		this.overrulingPermission 	= overrulingPermission;
        this.type                   = type;
        this.title                  = title;
        this.description            = description;
        this.icon                   = icon;
        this.editorPanel            = editorPanel;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissionList()
	 */
	public List<SpPermission> getPermissionList() 
	{
	    ArrayList<SpPermission> list = new ArrayList<SpPermission>(1);
	    list.add(permission);
		return list;
	}
	
	/**
     * @return the type
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getType()
     */
    @Override
    public String getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getTitle()
     */
    @Override
    public String getTitle()
	{
		return title;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getIcon()
     */
    @Override
    public ImageIcon getIcon()
    {
        return icon;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getDescription()
	 */
    @Override
	public String getDescription()
	{
		return description;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getEditorPanel()
     */
    @Override
    public PermissionEditorIFace getEditorPanel()
    {
        return editorPanel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissions()
     */
    @Override
    public List<PermissionSettings> getPermissions()
    {
        ArrayList<PermissionSettings> list = new ArrayList<PermissionSettings>(1);
        
        int options = PermissionSettings.NO_PERM;
        options |= permission.canModify() ? PermissionSettings.CAN_MODIFY : 0;
        options |= permission.canView()   ?   PermissionSettings.CAN_VIEW : 0;
        options |= permission.canAdd()    ?    PermissionSettings.CAN_ADD : 0;
        options |= permission.canDelete() ? PermissionSettings.CAN_DELETE : 0;
        list.add(new PermissionSettings(options));
        
        return list;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#setPermissions(edu.ku.brc.af.auth.PermissionSettings)
     */
    @Override
    public void setPermissions(final List<PermissionSettings> permissionSettings)
    {
        PermissionSettings permSettings = permissionSettings.get(0);
        permission.setActions(permSettings.canView(), permSettings.canAdd(), permSettings.canModify(), permSettings.canDelete());
    }
    


    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#addTableRow(javax.swing.table.DefaultTableModel, javax.swing.ImageIcon)
	 */
    @Override
	public void addTableRow(DefaultTableModel model, ImageIcon defaultIcon)
	{
		// FIXME: adjust this to work with 3-state checkbox
		model.addRow(new Object[] 
		        				{ 
		                            icon != null ? icon : defaultIcon, this, 
		        					new Boolean(permission.canView()), 
		        					new Boolean(permission.canAdd()), 
		        					new Boolean(permission.canModify()), 
		        					new Boolean(permission.canDelete())
		        				}
		        			);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString()
	{
		return getTitle();
	}

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(PermissionEditorRowIFace o)
    {
        return getTitle().compareTo(o.getTitle());
    }
	
	
}
