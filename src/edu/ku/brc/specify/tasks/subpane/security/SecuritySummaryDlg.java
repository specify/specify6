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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.util.Hashtable;

import javax.swing.JTabbedPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalHibernateService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 * Created Date: Sep 10, 2008
 *
 */
@SuppressWarnings("serial")
public class SecuritySummaryDlg extends CustomDialog
{
    /**
     * @param parentDlg
     */
    public SecuritySummaryDlg(final CustomDialog parentDlg) 
    {
        super(parentDlg, getResourceString("SecuritySummaryDlg.DLG_TITLE"), true, OKHELP, null);
        helpContext = "SECURITY_SUMMARY";
        
        okLabel = getResourceString("CLOSE");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        final EditorPanel infoPanel = new EditorPanel(null);
        final CellConstraints cc = new CellConstraints();

        PermissionEditor prefsEdt = new PermissionEditor("Preferences",  new PrefsPermissionEnumerator(), infoPanel,
                true, "SEC_NAME_TITLE", "SEC_ENABLE_PREF", null, null, null);

        PermissionPanelEditor generalEditor = new PermissionPanelEditor(null, null);
        generalEditor.addPanel(new IndvPanelPermEditor("SEC_TOOLS", "SEC_TOOLS_DSC", new TaskPermissionEnumerator(), infoPanel, true));
        generalEditor.addPanel(new PermissionEditor("SEC_TABLES", new TablePermissionEnumerator(), infoPanel, true));
        generalEditor.addPanel(prefsEdt);

        PermissionPanelEditor objEditor = new PermissionPanelEditor(null, null);
        objEditor.addPanel(new IndvPanelPermEditor("SEC_DOS", "SEC_DOS_DSC", new ObjectPermissionEnumerator(), infoPanel));

        // create tabbed panel for different kinds of permission editing tables
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(getResourceString("SEC_GENERAL"), generalEditor);
        //tabbedPane.addTab("SEC_OBJS", objEditor);

        final PanelBuilder mainPB = new PanelBuilder(
        		new FormLayout("f:p:g", "f:p:g,5px,min(325px;p),2dlu,p"), infoPanel);
        
        // lay out controls on panel
        mainPB.add(tabbedPane, cc.xy(1, 1)); 

        // adds panel to custom dialog
        contentPanel = infoPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // updates panels with permission data from the user who's currently logged on
		SpecifyUser user      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
		SpPrincipal principal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
		if (principal != null)
		{
            Hashtable<String, SpPermission> existingPerms = PermissionService.getExistingPermissions(principal.getId());
            
            // get the groups this user belongs to and stuff the list of overriding permissions with their permissions
            Hashtable<String, SpPermission> overridingPerms = PermissionService.getOverridingPermissions(user);

            generalEditor.updateData(principal, null, existingPerms, overridingPerms, null);
            
		} else
		{
		    UIRegistry.showError(String.format("The user '%s' doesn't have a User Principal object, which should not happen.\nPlease contact Specify Support.", user.getName()));
		}
        
        pack();
        
        setSize(600, 500);
    }
}

