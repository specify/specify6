package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.principal.UserPrincipalHibernateService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 * Created Date: Sep 10, 2008
 *
 */
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

        JComboBox genTypeSwitcher = UIHelper.createComboBox(new DefaultComboBoxModel());
        JTable generalPermissionsTable = new JTable();
        JPanel generalPermissionsPanel = GeneralPermissionEditor.createGeneralPermissionsPanel(
        		                             generalPermissionsTable, genTypeSwitcher, infoPanel);
        final PermissionEditor generalPermissionsEditor = GeneralPermissionEditor.createGeneralPermissionsEditor(generalPermissionsTable, 
                                                                                                                 genTypeSwitcher, 
                                                                                                                 infoPanel,
                                                                                                                 true);
        
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
        
        final PermissionEditor objectPermissionsEditor = ObjectPermissionEditor.createObjectPermissionsEditor(objectPermissionsTable, 
                                                                                                              objTypeSwitcher, 
                                                                                                              infoPanel, 
                                                                                                              true);
    
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
        
        YesNoCellRenderer yesNoRenderer = new YesNoCellRenderer();
        TableColumnModel tblModel = generalPermissionsTable.getColumnModel();
        for (int i=2;i<tblModel.getColumnCount();i++)
        {
            tblModel.getColumn(i).setCellRenderer(yesNoRenderer);
        }
        
        pack();
    }
    
    class YesNoCellRenderer extends DefaultTableCellRenderer
    {
        public final String YES = getResourceString("YES");
        public final String NO  = getResourceString("NO");
        
        public Font boldFont  = null;
        public Font normalFont = null;
        
        /**
         * 
         */
        public YesNoCellRenderer()
        {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
            
            normalFont = getFont();
            boldFont   = normalFont.deriveFont(Font.BOLD);
        }



        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Boolean)
            {
                label.setForeground(((Boolean)value) ? Color.BLACK : Color.LIGHT_GRAY);
                label.setFont(((Boolean)value) ? boldFont : normalFont);
                label.setText(((Boolean)value) ? YES : NO);
            }
            return label;
        }
        
    }
}

