/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.ku.brc.af.auth.specify.principal.UserPrincipalHibernateService;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.ui.forms.MultiView;

/**
 * Wraps a JPanel with a permission editor (if panel for group or user) 
 * for use with card panel layout in SecurityAdminPane  
 * 
 * @author Ricardo
 *
 */
public class AdminInfoSubPanelWrapper
{
	private JPanel                 displayPanel;
	private List<PermissionEditor> permissionEditors; 
	
	/**
	 * Constructor taking only a JPanel as parameter
	 * 
	 * @param displayPanel
	 */
	public AdminInfoSubPanelWrapper(JPanel displayPanel)
	{
		this.displayPanel = displayPanel;
		permissionEditors = new ArrayList<PermissionEditor>();
	}

	public void clearPermissionEditors()
	{
		permissionEditors.clear();
	}
	
	public void addPermissionEditor(PermissionEditor permissionEditor)
	{
		permissionEditors.add(permissionEditor);
	}
	
	public void removePermissionEditor(PermissionEditor permissionEditor)
	{
		permissionEditors.remove(permissionEditor);
	}
	
	public JPanel getDisplayPanel()
	{
		return displayPanel;
	}
	
	public void savePermissionData()
	{
		for (PermissionEditor editor : permissionEditors)
		{
			editor.savePermissions();			
		}
	}
	
	/**
	 * Set form data based on a given persistent object
	 * If first object is a SpecifyUser, secondObject is the group (GroupPrincipal) a user belongs to
	 * @param dataObj
	 * @param secondObject
	 */
	public void setData(Object dataObj, Object secondObject)
	{
		if (displayPanel instanceof ViewBasedDisplayPanel)
		{
			ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel) displayPanel;
			panel.setData(dataObj);
			
			// set permissions table if appropriate according to principal (user or usergroup)
			SpPrincipal principal = null;
			if (dataObj instanceof SpecifyUser)
			{
				// get user principal
				SpecifyUser user = (SpecifyUser) dataObj;
				principal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
			}
			else if (dataObj instanceof SpPrincipal)
			{
				principal = (SpPrincipal) dataObj;
			}

			// get user principal
			SpPrincipal secondPrincipal = null;
			if (secondObject instanceof SpecifyUser)
			{
				SpecifyUser user = (SpecifyUser) secondObject;
				secondPrincipal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
			}

			if (principal != null && permissionEditors.size() > 0)
			{
				for (PermissionEditor editor : permissionEditors)
				{
					editor.updateTable(principal, secondPrincipal);
				}
			}
		}
	}
	
	/**
	 * Returns the MultiView associated with a ViewBasedDisplayPanel, or just return null if
	 * wrapped panel is just a regular JPanel
	 * @return
	 */
	public MultiView getMultiView()
	{
		if (displayPanel instanceof ViewBasedDisplayPanel)
		{
			ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel) displayPanel;
    		return panel.getMultiView();
		}
		// else
		return null;
	}
}
