package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
public class ObjectPermissionEditor// extends PermissionEditor 
{
    //private static final Logger log = Logger.getLogger(ObjectPermissionEditor.class);



    /**
     * A factory for the ObjectPermissionEditor
     * 
     * @param table
     * @param listener
     * @return
     */
    protected static PermissionEditor createObjectPermissionsEditor(final JTable         table,
                                                                    @SuppressWarnings("unused")final JComboBox      typeSwitcherCBX,
                                                                    final ChangeListener listener,
                                                                    @SuppressWarnings("unused")final boolean        readOnly)
    {
        return null;//new ObjectPermissionEditor(table, typeSwitcherCBX, listener, new ObjectPermissionEnumerator(), readOnly);
    }

    /**
     * @param objectPermissionsTable
     * @param objTypeSwitcher
     * @param infoPanel
     * @return
     */
    public static JPanel createObjectPermissionsPanel(JTable objectPermissionsTable, JComboBox objTypeSwitcher, EditorPanel infoPanel) 
    {
    	// create object permission table
    	final CellConstraints cc = new CellConstraints();

    	JPanel objectPermissionsPanel = new JPanel(new BorderLayout());
    	UIHelper.makeTableHeadersCentered(objectPermissionsTable, false);

    	PanelBuilder otPB            = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
    	otPB.add(objTypeSwitcher, cc.xy(2, 1));

    	JPanel innerPanel = new JPanel(new BorderLayout());
    	innerPanel.add(otPB.getPanel(), BorderLayout.NORTH);
    	innerPanel.add(new JScrollPane(objectPermissionsTable), BorderLayout.CENTER);
    	objectPermissionsPanel.add(innerPanel, BorderLayout.CENTER);

    	/*
    	final PermissionEditor objectsPermissionEditor = ObjectPermissionEditor.
    	createObjectPermissionsEditor(objectPermissionsTable, objTypeSwitcher, infoPanel);
    	objTypeSwitcher.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e)
    		{
    			//objectsPermissionEditor.fillWithType();
    		}
    	});
    	*/
    	return objectPermissionsPanel;
    }    
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditor#addColumnHeaders(javax.swing.table.DefaultTableModel)
	 */
	protected void addColumnHeaders(DefaultTableModel modelArg)
	{
		modelArg.addColumn("");
		modelArg.addColumn("Task");
		
		modelArg.addColumn("View");
		modelArg.addColumn("Add");
		modelArg.addColumn("Modify");
		modelArg.addColumn("Delete");
		
		modelArg.addColumn("View");
		modelArg.addColumn("Add");
		modelArg.addColumn("Modify");
		modelArg.addColumn("Delete");
		
		modelArg.addColumn("View");
		modelArg.addColumn("Add");
		modelArg.addColumn("Modify");
		modelArg.addColumn("Delete");
	}
	
	public void savePermissions()
	{
		// nothing to save if we didn't specify a principal yet
		/*if (principal == null)
			return;
		
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
			session.attach(principal);

    		int numRows = model.getRowCount();
    		int taskCol = permissionTable.getColumn("Task").getModelIndex();

    		for (int row = 0; row < numRows; ++row)
    		{
    			// get permission row wrapper
    			ObjectPermissionEditorRow wrapper = (ObjectPermissionEditorRow) model.getValueAt(row, taskCol);
    			
    			wrapper.savePermissions(session, model, principal, row);
    		}
    		session.commit();
        } 
        catch (final Exception e1)
        {
            session.rollback();
            log.error("Exception caught: " + e1.toString());
            e1.printStackTrace();
        } 
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }*/
	}
}
