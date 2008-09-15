package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;

/**
 * Just a factory to build the special kind of composite enumerator for general permissions
 * 
 * @author Ricardo
 *
 */
public class GeneralPermissionEditor
{
    public static PermissionEditor createGeneralPermissionEditor(final JTable table,
                                                                 final JComboBox typeSwitcherCBX,
                                                                 final ChangeListener listener)
    {
    	return createGeneralPermissionEditor(table, typeSwitcherCBX, listener, false);
    }

    /**
     * @param table
     * @param typeSwitcherCBX
     * @param listener
     * @param readOnly
     * @return
     */
    public static PermissionEditor createGeneralPermissionEditor(final JTable table,
                                                                 final JComboBox typeSwitcherCBX,
                                                                 final ChangeListener listener,
                                                                 final boolean readOnly) 
    {
        //PermissionEnumerator e1 = new FormPermissionEnumerator();
        PermissionEnumerator e1 = new DataObjPermissionEnumerator();
        PermissionEnumerator e2 = new TaskPermissionEnumerator();
        CompositePermissionEnumerator enumerator = new CompositePermissionEnumerator();
        enumerator.addEnumerator(e1);
        enumerator.addEnumerator(e2);
        return new PermissionEditor(table, typeSwitcherCBX, listener, enumerator, readOnly);
    }
    
    public static JPanel createGeneralPermissionsPanel(final JTable generalPermissionsTable, 
                                                       final JComboBox genTypeSwitcher, 
                                                       final EditorPanel infoPanel) 
    {
    	// create general permission table
    	final CellConstraints cc = new CellConstraints();

    	JPanel generalPermissionsPanel = new JPanel(new BorderLayout());
        UIHelper.makeTableHeadersCentered(generalPermissionsTable, false);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        pb.add(genTypeSwitcher, cc.xy(2, 1));
        
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(pb.getPanel(), BorderLayout.NORTH);
        innerPanel.add(new JScrollPane(generalPermissionsTable), BorderLayout.CENTER);
        generalPermissionsPanel.add(innerPanel, BorderLayout.CENTER);
        
        return generalPermissionsPanel;
    }
}
