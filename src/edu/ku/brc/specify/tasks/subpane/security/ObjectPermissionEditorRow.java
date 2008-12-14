/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
public class ObjectPermissionEditorRow implements PermissionEditorRowIFace
{
    private String       type;
    private String       title;
	private String       description;
	private SpPermission ownerPermission;
	private SpPermission groupPermission;
	private SpPermission otherPermission;
	private List<SpPermission> customPermissions;
	private ImageIcon    icon;
    protected PermissionEditorIFace editorPanel;
    protected boolean adminPrincipal;


	/**
	 * @param ownerPermission
	 * @param groupPermission
	 * @param otherPermission
	 * @param type
	 * @param title
	 * @param description
	 * @param icon
	 * @param editorPanel
	 */
	public ObjectPermissionEditorRow(final SpPermission ownerPermission,
	                                 final SpPermission groupPermission, 
	                                 final SpPermission otherPermission,
                                     final String type, 
                                     final String title, 
	                                 final String description, 
	                                 final ImageIcon icon,
                                     final PermissionEditorIFace editorPanel) 
	{
		this.ownerPermission = ownerPermission;
		this.groupPermission = groupPermission;
		this.otherPermission = otherPermission;
		this.customPermissions = null;
		
        this.type       = type;
        this.title       = title;
        this.description = description;
        this.icon        = icon;
        this.editorPanel = editorPanel;
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
    
    /**
     * @param permission
     * @return
     */
    private PermissionIFace createPermissionSettings(final SpPermission permission)
    {
        int options = PermissionSettings.NO_PERM;
        options |= permission.canModify() ? PermissionSettings.CAN_MODIFY : 0;
        options |= permission.canView()   ?   PermissionSettings.CAN_VIEW : 0;
        options |= permission.canAdd()    ?    PermissionSettings.CAN_ADD : 0;
        options |= permission.canDelete() ? PermissionSettings.CAN_DELETE : 0;
        return new PermissionSettings(options);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissions()
     */
    @Override
    public List<PermissionIFace> getPermissions()
    {
        ArrayList<PermissionIFace> list = new ArrayList<PermissionIFace>(1);
        
        list.add(createPermissionSettings(ownerPermission));
        list.add(createPermissionSettings(groupPermission));
        list.add(createPermissionSettings(otherPermission));
        
        return list;
    }
    
    private void setPermSettings(final PermissionIFace permSettings, 
                                 final SpPermission permission)
    {
        permission.setActions(permSettings.canView(), permSettings.canAdd(), permSettings.canModify(), permSettings.canDelete());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#setPermissions(edu.ku.brc.af.auth.PermissionSettings)
     */
    @Override
    public void setPermissions(final List<PermissionIFace> permSettings)
    {
        setPermSettings(permSettings.get(0), ownerPermission);
        setPermSettings(permSettings.get(1), groupPermission);
        setPermSettings(permSettings.get(2), otherPermission);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissionList()
     */
    @Override
    public List<SpPermission> getPermissionList() 
    {
        ArrayList<SpPermission> list = new ArrayList<SpPermission>();
        list.add(ownerPermission);
        list.add(groupPermission);
        list.add(otherPermission);
        return list;
    }
    
    /**
     * @return
     */
    public SpPermission getOwnerPermission() 
	{
		return ownerPermission;
	}

	/**
	 * @param ownerPermission
	 */
	public void setOwnerPermission(SpPermission ownerPermission) 
	{
		this.ownerPermission = ownerPermission;
	}

	/**
	 * @return
	 */
	public SpPermission getGroupPermission() 
	{
		return groupPermission;
	}

	/**
	 * @param groupPermission
	 */
	public void setGroupPermission(SpPermission groupPermission) 
	{
		this.groupPermission = groupPermission;
	}

	public SpPermission getOtherPermission() 
	{
		return otherPermission;
	}

	public void setOtherPermission(SpPermission otherPermission) 
	{
		this.otherPermission = otherPermission;
	}

	public List<SpPermission> getCustomPermissions() 
	{
		return customPermissions;
	}

	public void addCustomPermission(SpPermission permission)
	{
		customPermissions.add(permission);
	}
	
	public void removeCustomPermission(SpPermission permission)
	{
		customPermissions.remove(permission);
	}
	
	public void clearCustomPermission()
	{
		customPermissions.clear();
	}
	
	public String toString()
	{
		return getTitle();
	}
	
	public void addTableRow(DefaultTableModel model, ImageIcon defaultIcon)
	{
		model.addRow(new Object[] 
		        				{ 
		        					icon != null ? icon : defaultIcon, this,
		        					// owner permissions
		        					new Boolean(ownerPermission.canView()), 
		        					new Boolean(ownerPermission.canAdd()), 
		        					new Boolean(ownerPermission.canModify()), 
		        					new Boolean(ownerPermission.canDelete()),
		        					
		        					// group permissions
		        					new Boolean(groupPermission.canView()), 
		        					new Boolean(groupPermission.canAdd()), 
		        					new Boolean(groupPermission.canModify()), 
		        					new Boolean(groupPermission.canDelete()),
		        					
		        					// other permissions
		        					new Boolean(otherPermission.canView()), 
		        					new Boolean(otherPermission.canAdd()), 
		        					new Boolean(otherPermission.canModify()), 
		        					new Boolean(otherPermission.canDelete())
		        				}
		        			);
	}
	
	public void savePermissions(final DataProviderSessionIFace session, 
	                            final DefaultTableModel model,
	                            final SpPrincipal principal,
	                            final int row) throws Exception
	{
		// get value of boolean objects
		Boolean[] values = new Boolean[12]; 
		for (int col = 0; col < 12; col++)
		{
			values[col] = (Boolean) model.getValueAt(row, col + 2);
		}
		
		savePermission(session, ownerPermission, values, principal, 0);
		savePermission(session, groupPermission, values, principal, 4);
		savePermission(session, otherPermission, values, principal, 8);
		
		// save custom permissions
		//for (SpPermission perm : customPermissions)
		//{
		//	// TODO: implement method
		//}
	}
	
	private void savePermission(final DataProviderSessionIFace session,
	                            final SpPermission perm,
	                            final Boolean[] values,
	                            final SpPrincipal principal,
	                            final int col) throws Exception
	{
		Boolean canView = values[col + 0];
		Boolean canAdd  = values[col + 1];
		Boolean canMod  = values[col + 2];
		Boolean canDel  = values[col + 3];
		
		if ( !(canView || canAdd || canMod || canDel) )
		{
			// no flag is set, so delete the permission
			if (perm.getId() != null)
			{
				// if id is not null, it means the permission is from DB
				// so we must delete permission
    			Set<SpPermission> perms = principal.getPermissions();
    			for (SpPermission currPerm : perms)
    			{
    				if (currPerm.getId().equals(perm.getId()))
    				{
    					session.evict(perm);
    					perms.remove(currPerm);
        				//session.saveOrUpdate(principal);
    					session.delete(currPerm);
    					break;
    				}
    			}
			}
		}
		else if (!perm.hasSameFlags(canView, canAdd, canMod, canDel))
		{
			// set new flags
			perm.setActions(canView, canAdd, canMod, canDel);

			// permission has changed: save it
			if (perm.getId() == null)
			{
				// permission doesn't yet exist in database: attach it to its principal
				principal.getPermissions().add(perm);
				perm.getPrincipals().add(principal);
			}
			
			//session.saveOrUpdate(perm);
			//session.saveOrUpdate(principal);
		}
	}
	
	/* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#updatePerm(edu.ku.brc.specify.datamodel.SpPermission, edu.ku.brc.specify.datamodel.SpPermission)
     */
    @Override
    public void updatePerm(SpPermission oldPerm, SpPermission newPerm)
    {
        if (oldPerm == ownerPermission)
        {
            ownerPermission = newPerm;
            
        } else if (oldPerm == groupPermission)
        {
            groupPermission = newPerm;
            
        } else if (oldPerm == otherPermission)
        {
            otherPermission = newPerm;
        }
    }
    
    @Override
    public int compareTo(PermissionEditorRowIFace o)
    {
        return getTitle().compareTo(o.getTitle());
    }

    public boolean isAdminPrincipal()
    {
        return adminPrincipal;
    }

    public void setAdminPrincipal(boolean adminPrincipal)
    {
        this.adminPrincipal = adminPrincipal;
    }
	
	
}
