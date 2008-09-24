/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

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


	public ObjectPermissionEditorRow(final SpPermission ownerPermission,
	                                 final SpPermission groupPermission, 
	                                 final SpPermission otherPermission,
                                     final String type, 
                                     final String title, 
	                                 final String description, 
	                                 final ImageIcon icon) 
	{
		this.ownerPermission = ownerPermission;
		this.groupPermission = groupPermission;
		this.otherPermission = otherPermission;
		this.customPermissions = null;
		
        this.type       = type;
        this.title       = title;
        this.description = description;
        this.icon        = icon;
	}

	public String getTitle() {
		return title;
	}


	public String getDescription() {
		return description;
	}
	
	/**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    public SpPermission getOwnerPermission() 
	{
		return ownerPermission;
	}

	public void setOwnerPermission(SpPermission ownerPermission) 
	{
		this.ownerPermission = ownerPermission;
	}

	public SpPermission getGroupPermission() 
	{
		return groupPermission;
	}

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
        				session.saveOrUpdate(principal);
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
			}
			
			session.saveOrUpdate(perm);
			session.saveOrUpdate(principal);
		}
	}

    @Override
    public int compareTo(PermissionEditorRowIFace o)
    {
        return getTitle().compareTo(o.getTitle());
    }
	
	
}
