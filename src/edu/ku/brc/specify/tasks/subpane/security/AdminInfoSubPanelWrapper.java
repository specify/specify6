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

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.MultiView;
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
    public boolean setData(final DataModelObjBaseWrapper firstWrp, 
                           final DataModelObjBaseWrapper secondWrp)
    {
        boolean hasChanged = false;
        if (!(displayPanel instanceof ViewBasedDisplayPanel))
        {
            // let's quit as soon as possible
            return false;
        }
        
        Object firstObj = firstWrp.getDataObj();
        Object secondObj = (secondWrp != null)? secondWrp.getDataObj() : null;
        
        ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel) displayPanel;
        panel.setData(null);
        panel.setData(firstWrp.getDataObj());
        
        SpecifyUser user = null;
        
        // set permissions table if appropriate according to principal (user or usergroup)
        SpPrincipal firstPrincipal = null;
        SpPrincipal secondPrincipal = null;
        if (firstObj instanceof SpecifyUser)
        {
            user            = (SpecifyUser) firstObj;
            firstPrincipal  = user.getUserPrincipal();
            secondPrincipal = (SpPrincipal) secondObj; // must be the user group
            
        } else if (firstObj instanceof SpPrincipal)
        {
            // first object is just a user group 
            firstPrincipal = (SpPrincipal) firstObj;
        }

        if (firstPrincipal == null || permissionEditors.size() == 0)
        {
            return false;
        }

        String userType = (user != null)? user.getUserType() : null;

        Hashtable<String, SpPermission> existingPerms = PermissionService.getExistingPermissions(firstPrincipal.getId());
        Hashtable<String, SpPermission> overrulingPerms = null;
        if (secondPrincipal != null)
        {
            overrulingPerms = PermissionService.getExistingPermissions(secondPrincipal.getId());
        }
        
        principal           = firstPrincipal;
        //overrulingPrincipal = secondPrincipal;

        for (PermissionPanelEditor editor : permissionEditors)
        {
            editor.updateData(firstPrincipal, secondPrincipal, existingPerms, overrulingPerms, userType);
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
        
        session.update(obj);
        session.update(principal);
        
        for (PermissionPanelEditor editor : permissionEditors)
        {
            editor.savePermissions(session);            
        }
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
