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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.busrules.SpecifyUserBusRules;

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
    private TextFieldWithInfo           agentTFWI   = null;
    
    private SpPrincipal                 principal   = null;  // first  Principal
    private SpPrincipal                 principal2  = null;  // second Principal
    
    private SpecifyUser                 user        = null;
    private DataModelObjBaseWrapper     firstWrp    = null;
    private DataModelObjBaseWrapper     secondWrp   = null;
    
    /**
     * Constructor taking only a JPanel as parameter
     * 
     * @param displayPanel
     */
    public AdminInfoSubPanelWrapper(final JPanel displayPanel)
    {
        this.displayPanel = displayPanel;
        permissionEditors = new ArrayList<PermissionPanelEditor>();
        
        MultiView mv = getMultiView();
        if (mv != null)
        {
            FormViewObj          fvo      = mv.getCurrentViewAsFormViewObj();
            Component            cbx      = fvo.getControlByName("agent");
            if (cbx != null && cbx instanceof TextFieldWithInfo)
            {
                agentTFWI = (TextFieldWithInfo)cbx;
            }
        }
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
    public void addPermissionEditor(final PermissionPanelEditor permissionEditor)
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
     * @param firstWrpArg
     * @param secondWrpArg
     * @return whether new data was set (usually from setting defaults)
     */
    public boolean setData(final DataModelObjBaseWrapper firstWrpArg, 
                           final DataModelObjBaseWrapper secondWrpArg,
                           final Division                division)
    {
        firstWrp  = firstWrpArg;
        secondWrp = secondWrpArg;
        
        boolean hasChanged = false;
        if (!(displayPanel instanceof ViewBasedDisplayPanel))
        {
            // let's quit as soon as possible
            return false;
        }
        
        Object firstObj  = firstWrp.getDataObj();
        Object secondObj = (secondWrp != null) ? secondWrp.getDataObj() : null;

        ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel)displayPanel;
        panel.setData(null);

        user = null;
        
        String userType = null;
        
        // set permissions table if appropriate according to principal (user or usergroup)
        SpPrincipal firstPrincipal  = null;
        SpPrincipal secondPrincipal = null;
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
                    
            if (firstObj instanceof SpecifyUser)
            {
                user            = session.get(SpecifyUser.class, ((SpecifyUser)firstObj).getId());
                userType        = user.getUserType();
                firstPrincipal  = user.getUserPrincipal();
                secondPrincipal = (SpPrincipal)secondObj; // must be the user group
                
                panel.setData(user);
                for (Agent agent : user.getAgents())
                {
                    if (agent.getDivision().getId().equals(division.getId()))
                    {
                        agentTFWI.setValue(agent, null);
                        break;
                    }
                }
                
            } else if (firstObj instanceof SpPrincipal)
            {
                // first object is just a user group 
                user            = null;
                firstPrincipal  = session.get(SpPrincipal.class, ((SpPrincipal)firstObj).getId());
                secondPrincipal = null;
                panel.setData(firstPrincipal);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        if (firstPrincipal == null || permissionEditors.size() == 0)
        {
            return false;
        }
        
        principal  = firstPrincipal;
        principal2 = secondPrincipal;

        Hashtable<String, SpPermission> existingPerms   = PermissionService.getExistingPermissions(principal.getId());
        Hashtable<String, SpPermission> overrulingPerms = null;
        if (principal2 != null)
        {
            overrulingPerms = PermissionService.getExistingPermissions(principal2.getId());
        }

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
        MultiView mv = getMultiView();
        mv.getDataFromUI();
        
        Object obj = mv.getData();
        
        SpecifyUserBusRules busRules = new SpecifyUserBusRules();
        busRules.initialize(mv.getCurrentView());
        
        // Couldn't call BuinessRules because of a double session
        // need to look into it later
        //BusinessRulesIFace br = mv.getCurrentViewAsFormViewObj().getBusinessRules();
        
        Agent toBeReleased = null;
        
        // We need to do this because we can't call the BusniessRules
        if (obj instanceof SpecifyUser)
        {
            user = (SpecifyUser)obj;
            
            busRules.beforeMerge(user, session);
            
            user = session.merge(user);
            busRules.beforeSave(user, session);
            
        } else
        {
            obj = session.merge(obj);
            session.saveOrUpdate(obj);
        }
        
        principal = session.merge(principal);
        session.saveOrUpdate(principal);
        
        if (toBeReleased != null)
        {
            toBeReleased = session.merge(toBeReleased);
            toBeReleased.setSpecifyUser(null);
            session.saveOrUpdate(toBeReleased);
        }
        
        for (PermissionPanelEditor editor : permissionEditors)
        {
            editor.savePermissions(session);            
        }
        
        if (user != null)
        {
            user = session.merge(user);
            firstWrp.setDataObj(user);
            secondWrp.setDataObj(principal2);
            
        } else
        {
            firstWrp.setDataObj(principal);
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
            ViewBasedDisplayPanel panel = (ViewBasedDisplayPanel)displayPanel;
            return panel.getMultiView();
        }
        // else
        return null;
    }
}
