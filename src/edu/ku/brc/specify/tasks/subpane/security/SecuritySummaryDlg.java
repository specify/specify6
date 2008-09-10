package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.principal.UserPrincipalHibernateService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

public class SecuritySummaryDlg extends CustomDialog
{
    public SecuritySummaryDlg(final CustomDialog parentDlg) {
        super(parentDlg, getResourceString("SecuritySummaryDlg.DLG_TITLE"), true, OKHELP, null);
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

        JComboBox genTypeSwitcher = UIHelper.createComboBox(new DefaultComboBoxModel());
        JTable generalPermissionsTable = new JTable();
        JPanel generalPermissionsPanel = GeneralPermissionEditor.createGeneralPermissionsPanel(
        		generalPermissionsTable, genTypeSwitcher, infoPanel);
        final PermissionEditor generalPermissionsEditor = GeneralPermissionEditor.
        	createGeneralPermissionEditor(generalPermissionsTable, genTypeSwitcher, infoPanel, true);
        
        genTypeSwitcher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                generalPermissionsEditor.fillWithType();
            }
        });
        
    	JComboBox    objTypeSwitcher = UIHelper.createComboBox(new DefaultComboBoxModel());
    	JTable objectPermissionsTable = new JTable();
        JPanel objectPermissionsPanel  = ObjectPermissionEditor.createObjectPermissionsPanel(
        		objectPermissionsTable, objTypeSwitcher, infoPanel);
        
        final PermissionEditor objectPermissionsEditor = ObjectPermissionEditor.
        	createObjectPermissionsEditor(objectPermissionsTable, objTypeSwitcher, infoPanel, true);
    
        objTypeSwitcher.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e)
        	{
        		objectPermissionsEditor.fillWithType();
        	}
        });
        // create tabbed panel for different kinds of permission editing tables
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", generalPermissionsPanel); // I18N
        tabbedPane.addTab("Objects", objectPermissionsPanel);  // I18N
        
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
        
        generalPermissionsEditor.updateTable(principal, null);
        objectPermissionsEditor.updateTable(principal, null);
        
        pack();
    }
}

