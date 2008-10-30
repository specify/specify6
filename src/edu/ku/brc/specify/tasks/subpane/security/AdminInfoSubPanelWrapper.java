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
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * Wraps a JPanel with a permission editor (if panel for group or user) 
 * for use with card panel layout in SecurityAdminPane  
 * 
 * @author Ricardo
 *
 */
public class AdminInfoSubPanelWrapper
{
	private JPanel                      displayPanel;
	private List<PermissionPanelEditor> permissionEditors; 
	
	/**
	 * Constructor taking only a JPanel as parameter
	 * 
	 * @param displayPanel
	 */
	public AdminInfoSubPanelWrapper(final JPanel displayPanel)
	{
		this.displayPanel = displayPanel;
		permissionEditors = new ArrayList<PermissionPanelEditor>();
	}

	/**
	 * 
	 */
	public void clearPermissionEditors()
	{
		permissionEditors.clear();
	}
	
	/**
	 * @param permissionEditor
	 */
	public void addPermissionEditor(PermissionPanelEditor permissionEditor)
	{
		permissionEditors.add(permissionEditor);
	}
	
	/**
	 * @param permissionEditor
	 */
	public void removePermissionEditor(PermissionPanelEditor permissionEditor)
	{
		permissionEditors.remove(permissionEditor);
	}
	
	/**
	 * @return
	 */
	public JPanel getDisplayPanel()
	{
		return displayPanel;
	}
	
	/**
     * @return the permissionEditors
     */
    public List<PermissionPanelEditor> getPermissionEditors()
    {
        return permissionEditors;
    }

    /**
	 * @param session
	 */
	public void savePermissionData(final DataProviderSessionIFace session) throws Exception
	{
        MultiView   mv  = getMultiView();
        mv.getDataFromUI();
        
		for (PermissionPanelEditor editor : permissionEditors)
		{
			editor.savePermissions(session);			
		}
	}
	
	/**
	 * Set form data based on a given persistent object
	 * If first object is a SpecifyUser, secondObject is the group (GroupPrincipal) a user belongs to
	 * @param dataObj
	 * @param secondObject
	 */
	public void setData(final Object dataObj, final Object secondObject)
	{
		if (displayPanel instanceof ViewBasedDisplayPanel)
		{
			ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel) displayPanel;
            panel.setData(null);
            panel.setData(dataObj);
			
            SpecifyUser user = null;
            
			// set permissions table if appropriate according to principal (user or usergroup)
			SpPrincipal principal = null;
			if (dataObj instanceof SpecifyUser)
			{
				// get user principal
			
			    user = (SpecifyUser) dataObj;
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
				user = (SpecifyUser) secondObject;
				secondPrincipal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
			}

			if (principal != null && permissionEditors.size() > 0)
			{
				for (PermissionPanelEditor editor : permissionEditors)
				{
				    //System.err.println("AdminInfoSubPane "+ principal.getPermissions().size());
				    String userType = principal.getPermissions().size() == 0 ? user.getUserType() : null;
				    
				    /*for (PermissionPanelContainerIFace permPanel : editor.getPanels())
				    {
				        List<PermissionEditorRowIFace> perms = permPanel.getPermissionEnumerator().getPermissions(principal, secondPrincipal);
				        for (PermissionEditorRowIFace item : perms)
				        {
				            System.err.println(item);
				        }
				    }*/
				    
					editor.updateData(principal, secondPrincipal, userType);
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
