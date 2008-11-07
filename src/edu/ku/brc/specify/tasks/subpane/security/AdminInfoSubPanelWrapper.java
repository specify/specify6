/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalHibernateService;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UIRegistry;

/**
 * Wraps a JPanel with a permission editor (if panel for group or user) 
 * for use with card panel layout in SecurityAdminPane  
 * 
 * @author Ricardo
 * @author rods
 *
 */
public class AdminInfoSubPanelWrapper
{
	private JPanel                      displayPanel;
	private List<PermissionPanelEditor> permissionEditors; 
	
	private SpPrincipal                 principal           = null;
	private SpPrincipal                 overrulingPrincipal = null;

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
	 * Set form data based on a given persistent object
	 * If first object is a SpecifyUser, secondObject is the group (GroupPrincipal) a user belongs to
	 * @param dataObj
	 * @param secondObject
	 * @return whether new data was set (usually from setting defaults)
	 */
	public boolean setData(final Object dataObj, 
	                       final Object secondObject)
	{
	    boolean hasChanged = false;
		if (displayPanel instanceof ViewBasedDisplayPanel)
		{
			ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel) displayPanel;
            panel.setData(null);
            panel.setData(dataObj);
			
            SpecifyUser user = null;
            
			// set permissions table if appropriate according to principal (user or usergroup)
			SpPrincipal firstPrincipal = null;
			if (dataObj instanceof SpecifyUser)
			{
			    user      = (SpecifyUser) dataObj;
				firstPrincipal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
				
			} else if (dataObj instanceof SpPrincipal)
			{
				firstPrincipal = (SpPrincipal) dataObj;
			}

			// get user principal
			SpPrincipal secondPrincipal = null;
			if (secondObject instanceof SpecifyUser)
			{
				user            = (SpecifyUser) secondObject;
				secondPrincipal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
			}

			if (firstPrincipal != null && permissionEditors.size() > 0)
			{
			    String userType = null;
			    DataProviderSessionIFace session = null;
		        try
		        {
		            session  = DataProviderFactory.getInstance().createSession();
		            session.attach(firstPrincipal);
		            userType = firstPrincipal.getPermissions().size() == 0 ? (user != null ? user.getUserType() : null) : null;
		            
		        } catch (Exception ex)
		        {
		            ex.printStackTrace();
		            session.rollback();
		            
		        } finally
		        {
		            if (session != null)
		            {
		                session.close();
		            }
		        }
                
                if (userType != null)
                {
                    Object[] options = { 
                            getResourceString("ADMININFO_SET_DEF"), 
                            getResourceString("NO")
                          };
                    int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                 getResourceString("ADMININFO_SUBPNL"), 
                                                                 getResourceString("ADMININFO_SUBPNL_TITLE"), 
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (userChoice != JOptionPane.YES_OPTION)
                    {
                        userType = null;
                    } else
                    {
                        hasChanged = true;
                    }
                }
                
                Hashtable<String, SpPermission> existingPerms = PermissionService.getExistingPermissions(firstPrincipal.getId());
                Hashtable<String, SpPermission> overrulingPerms = null;
                if (secondPrincipal != null)
                {
                    overrulingPerms = PermissionService.getExistingPermissions(secondPrincipal.getId());
                }
                
                principal           = firstPrincipal;
                overrulingPrincipal = secondPrincipal;

				for (PermissionPanelEditor editor : permissionEditors)
				{
					editor.updateData(firstPrincipal, secondPrincipal, existingPerms, overrulingPerms, userType);
				}
			}
		}
		return hasChanged;
	}

    /**
     * @param session the current session
     */
    public void savePermissionData(final DataProviderSessionIFace session) throws Exception
    {
        MultiView   mv  = getMultiView();
        mv.getDataFromUI();
        
        Object obj = mv.getData();
        
        obj = session.merge(obj);
        obj = session.saveOrUpdate(obj);
        
        principal = session.merge(principal);
        
        if (overrulingPrincipal != null)
        {
            overrulingPrincipal = session.merge(overrulingPrincipal);
        }

        for (PermissionPanelEditor editor : permissionEditors)
        {
            editor.savePermissions(session);            
        }
        
        for (SpPermission perm : new ArrayList<SpPermission>(principal.getPermissions()))
        {
            if (StringUtils.isEmpty(perm.getActions()))
            {
                principal.getPermissions().remove(perm);
                perm.getPrincipals().remove(principal);
                session.delete(perm);
            }
        }
        session.saveOrUpdate(principal);
    }
	
	/**
	 * Returns the MultiView associated with a ViewBasedDisplayPanel, or just return null if
	 * wrapped panel is just a regular JPanel
	 * @return the forms MultiView
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
