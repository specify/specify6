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

        PermissionPanelEditor generalEditor = new PermissionPanelEditor();
        generalEditor.addPanel(new PermissionEditor("Data Objects", new DataObjPermissionEnumerator(), infoPanel, true));
        generalEditor.addPanel(new IndvPanelPermEditor("Tasks",     new TaskPermissionEnumerator(),    infoPanel, true));
        generalEditor.addPanel(prefsEdt);

        PermissionPanelEditor objEditor = new PermissionPanelEditor();
        objEditor.addPanel(new IndvPanelPermEditor("Data Objects", new ObjectPermissionEnumerator(), infoPanel));


        //      create tabbed panel for different kinds of permission editing tables
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", generalEditor); // I18N
        tabbedPane.addTab("Objects", objEditor);  // I18N

        final PanelBuilder mainPB = new PanelBuilder(
        		new FormLayout("f:p:g", "p,5px,min(325px;p),2dlu,p"), infoPanel);
        
        // lay out controls on panel
        mainPB.add(tabbedPane,             cc.xy(1, 1)); 

        // adds panel to custom dialog
        contentPanel = infoPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // updates panels with permission data from the user who's currently logged on
		SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
		SpPrincipal principal = UserPrincipalHibernateService.getUserPrincipalBySpecifyUser(user);
        Hashtable<String, SpPermission> existingPerms = PermissionService.getExistingPermissions(principal.getId());
        
        // get the groups this user belongs to and stuff the list of overriding permissions with their permissions
        Hashtable<String, SpPermission> overridingPerms = PermissionService.getOverridingPermissions(user);

		generalEditor.updateData(principal, null, existingPerms, overridingPerms, null);
        
        pack();
    }
}

