package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

public class ObjectPermissionEditor extends PermissionEditor {

    private static final Logger log = Logger.getLogger(ObjectPermissionEditor.class);

	/**
	 * @param permissionTable
	 * @param enumerator
	 */
	public ObjectPermissionEditor(final JTable permissionTable, final ObjectPermissionEnumerator enumerator)
	{
		// we can only create instances of this class providing the right enumerator class
		super(permissionTable, enumerator);
	}

	protected void addColumnHeaders(DefaultTableModel model)
	{
		model.addColumn("");
		model.addColumn("Task");
		
		model.addColumn("View");
		model.addColumn("Add");
		model.addColumn("Modify");
		model.addColumn("Delete");
		
		model.addColumn("View");
		model.addColumn("Add");
		model.addColumn("Modify");
		model.addColumn("Delete");
		
		model.addColumn("View");
		model.addColumn("Add");
		model.addColumn("Modify");
		model.addColumn("Delete");
	}
	
	public void savePermissions()
	{
		// nothing to save if we didn't specify a principal yet
		if (principal == null)
			return;
		
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
			session.attach(principal);

            DefaultTableModel model = (DefaultTableModel) permissionTable.getModel();
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
        }
	}
}
