/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package edu.ku.brc.specify.tasks.subpane.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.util.ComparatorByStringRepresentation;


/**
 * This class manages the editing of a set of homogeneous permissions for a SpPrincipal.
 * The component is a table with columns for the permission and checkboxes for its actions
 * The set of permissions must be homogeneous, ie., accept the same set of actions so that
 * each action can be displayed as a table column. 
 * 
 * @author Ricardo
 *
 */
public class PermissionEditor 
{
    private static final Logger log = Logger.getLogger(PermissionEditor.class);

	protected JTable				permissionTable;
	protected PermissionEnumerator 	enumerator;
	protected SpPrincipal 			principal;
	
	/**
	 * @param permissionTable
	 * @param enumerator
	 */
	public PermissionEditor(final JTable permissionTable, final PermissionEnumerator enumerator)
	{
		this.permissionTable 	= permissionTable;
		this.enumerator 		= enumerator;
		this.principal 			= null;
	}
	
	// TODO: overruling principal (nor the three-state checkbox) feature have been implemented yet
	
	/**
	 * Updates the table that will be used to display and edit the permissions 
	 */
	public void updateTable(final SpPrincipal principalArg, final SpPrincipal overrulingPrincipal)
	{
		// save principal used when saving permissions later
		this.principal = principalArg;
		
		@SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel()
		{
			public Class<?> getColumnClass(int columnIndex)
			{
				switch (columnIndex)
				{
					case 0: return ImageIcon.class;
					case 1: return String.class;
					default: return Boolean.class;
				}
			}
			
			public boolean isCellEditable(int row, int column) 
			{
				return (column >= 2);
			}
			
		};
		
		addColumnHeaders(model);
		
		ImageIcon sysIcon = IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16);

		List<PermissionEditorRowIFace> perms = enumerator.getPermissions(principalArg, overrulingPrincipal);
		Collections.sort(perms, new ComparatorByStringRepresentation<PermissionEditorRowIFace>(true));
		for (PermissionEditorRowIFace permWrapper : perms) 
		{
			permWrapper.addTableRow(model, sysIcon);
		}

		permissionTable.setModel(model);

		TableColumn column = permissionTable.getColumnModel().getColumn(0);
		column.setMinWidth(16);
		column.setMaxWidth(16);
		column.setPreferredWidth(16);

		column = permissionTable.getColumnModel().getColumn(1);
		column.setMinWidth(100);
		column.setMaxWidth(400);
		column.setPreferredWidth(200);
		

		/*		
		TristateRenderer renderer = new TristateRenderer();
		TristateEditor editor = new TristateEditor();
		for (int i = 2; i <= 5; ++i)
		{
			column = permissionTable.getColumnModel().getColumn(i);
			column.setCellRenderer(renderer);
			column.setCellEditor(editor);
		}
		*/
	}
	
	protected void addColumnHeaders(final DefaultTableModel model)
	{
		model.addColumn("");
		model.addColumn("Task");
		model.addColumn("View");
		model.addColumn("Add");
		model.addColumn("Modify");
		model.addColumn("Delete");
	}
	
	public void savePermissions()
	{
		// nothing to save if we didn't specify a principal yet
		if (principal == null)
		{
			return;
		}
		
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
			session.attach(principal);

            DefaultTableModel model = (DefaultTableModel) permissionTable.getModel();
    		int numRows = model.getRowCount();
    		int taskCol = permissionTable.getColumn("Task").getModelIndex();
    		int viewCol = permissionTable.getColumn("View").getModelIndex();
    		int addCol  = permissionTable.getColumn("Add").getModelIndex();
    		int modCol  = permissionTable.getColumn("Modify").getModelIndex();
    		int delCol  = permissionTable.getColumn("Delete").getModelIndex();

    		for (int row = 0; row < numRows; ++row)
    		{
    			GeneralPermissionEditorRow wrapper = (GeneralPermissionEditorRow) model.getValueAt(row, taskCol);
    			SpPermission perm = wrapper.getPermission();
    			Boolean canView = (Boolean) model.getValueAt(row, viewCol);
    			Boolean canAdd  = (Boolean) model.getValueAt(row, addCol);
    			Boolean canMod  = (Boolean) model.getValueAt(row, modCol);
    			Boolean canDel  = (Boolean) model.getValueAt(row, delCol);
    			
    			if ( !(canView || canAdd || canMod || canDel) )
    			{
    				// no flag is set, so delete the permission
    				if (perm.getId() != null)
    				{
    					// if id is not null, it means the permission is from DB
    					// so we must delete permission
    	    			Set<SpPermission> perms = principal.getPermissions();
    	    			for (SpPermission currPerm : perms)
    	    			{
    	    				if (currPerm.getId().equals(perm.getId()))
    	    				{
    	    					session.evict(perm);
    	    					perms.remove(currPerm);
    	        				session.saveOrUpdate(principal);
    	    					session.delete(currPerm);
    	    					break;
    	    				}
    	    			}
    				}
    			}
    			else if (!perm.hasSameFlags(canView, canAdd, canMod, canDel))
    			{
    				// set new flags
					perm.setActions(canView, canAdd, canMod, canDel);

					// permission has changed: save it
    				if (perm.getId() == null)
    				{
    					// permission doesn't yet exist in database: attach it to its principal
    					principal.getPermissions().add(perm);
    				}
    				
    				session.saveOrUpdate(perm);
    				session.saveOrUpdate(principal);
    			}
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
