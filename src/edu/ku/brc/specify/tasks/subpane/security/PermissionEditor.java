/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
@SuppressWarnings("serial")
public class PermissionEditor 
{
    private static final Logger log = Logger.getLogger(PermissionEditor.class);

	protected JTable				permissionTable;
	protected PermissionEnumerator 	enumerator;
	protected SpPrincipal 			principal;
	protected ChangeListener        listener;
	protected boolean               readOnly;
	
	protected DefaultTableModel     model;
	protected ImageIcon             icon;
	protected JComboBox             typeSwitcherCBX;
	
    protected String                nameColTitle = UIRegistry.getResourceString("SEC_NAME_TITLE");
    protected String                viewColTitle = UIRegistry.getResourceString("SEC_VIEW_TITLE");
    protected String                addColTitle  = UIRegistry.getResourceString("SEC_ADD_TITLE");
    protected String                modColTitle  = UIRegistry.getResourceString("SEC_MOD_TITLE");
    protected String                delColTitle  = UIRegistry.getResourceString("SEC_DEL_TITLE");
    
    protected Hashtable<String, Vector<PermissionEditorRowIFace>> typeRowHash = new Hashtable<String, Vector<PermissionEditorRowIFace>>();
	
	/**
	 * @param permissionTable
	 * @param enumerator
	 */
	public PermissionEditor(final JTable               permissionTable,
                            final JComboBox            typeSwitcherCBX,
                            final ChangeListener       listener, 
	                        final PermissionEnumerator enumerator)
	{
        this(permissionTable, typeSwitcherCBX, listener, enumerator, false);
	}

	/**
	 * @param permissionTable
	 * @param enumerator
	 */
	public PermissionEditor(final JTable               permissionTable,
                            final JComboBox            typeSwitcherCBX,
                            final ChangeListener       listener, 
	                        final PermissionEnumerator enumerator,
	                        final boolean              readOnly)
	{
        this.permissionTable    = permissionTable;
        this.typeSwitcherCBX    = typeSwitcherCBX;
		this.enumerator 		= enumerator;
		this.principal 			= null;
		this.listener           = listener;
		this.readOnly           = readOnly;
	}
	
	// TODO: overruling principal (nor the three-state checkbox) feature have been implemented yet
	
	public void fillWithType()
	{
	    log.error("fillWithType");
	    
		if (model == null) 
		{
			return;
		}
		
        while (model.getRowCount() > 0)
        {
            model.removeRow(0);
        }
        
        String type = (String)typeSwitcherCBX.getSelectedItem();
        log.error(type);
        if (type != null)
        {
    	    Vector<PermissionEditorRowIFace> list = typeRowHash.get(type);
    	    if (list != null)
    	    {
        	    for (PermissionEditorRowIFace permWrapper : list) 
                {
        	        permWrapper.addTableRow(model, icon);
                }
    	    }
        }
	}
	
	/**
	 * Updates the table that will be used to display and edit the permissions 
	 */
	public void updateTable(final SpPrincipal principalArg, final SpPrincipal overrulingPrincipal)
	{
		// save principal used when saving permissions later
		this.principal = principalArg;
		
		model = new DefaultTableModel()
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
				return !readOnly && (column >= 2);
			}
		};
		
		addColumnHeaders(model);
		
		IconManager.IconSize iconSize = IconManager.IconSize.Std20;
		icon = IconManager.getIcon("SystemSetup", iconSize);
		
		JLabel label = UIHelper.createLabel("XXXX");
		label.setIcon(icon);
		
		int oldSelIndex = typeSwitcherCBX.getSelectedIndex();
		
		typeRowHash.clear();
		typeSwitcherCBX.removeAllItems();
		
		List<PermissionEditorRowIFace> perms = enumerator.getPermissions(principalArg, overrulingPrincipal);
		Collections.sort(perms, new ComparatorByStringRepresentation<PermissionEditorRowIFace>(true));
        for (PermissionEditorRowIFace permWrapper : perms) 
        {
            Vector<PermissionEditorRowIFace> list = typeRowHash.get(permWrapper.getType());
            if (list == null)
            {
                list = new Vector<PermissionEditorRowIFace>();
                typeRowHash.put(permWrapper.getType(), list);
                typeSwitcherCBX.addItem(permWrapper.getType());
            }
            list.add(permWrapper);
        }
        
        if (typeSwitcherCBX.getModel().getSize() > 0)
        {
            typeSwitcherCBX.setSelectedIndex(oldSelIndex > -1 && oldSelIndex < typeSwitcherCBX.getModel().getSize() ? oldSelIndex : 0);
            fillWithType();
        }

		permissionTable.setModel(model);
		permissionTable.setRowHeight(label.getPreferredSize().height+3);
		
		TableColumn column = permissionTable.getColumnModel().getColumn(0);
		int cellWidth = iconSize.size()+4;
		column.setMinWidth(cellWidth);
		column.setMaxWidth(cellWidth);
		column.setPreferredWidth(cellWidth);
        
        // For Strings with no changes made to the table, the render is a DefaultTableCellRender.
        //DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) column.getCellRenderer();
        // set the alignment to center
        //dtcr.setHorizontalAlignment(SwingConstants.CENTER);

		column = permissionTable.getColumnModel().getColumn(1);
		column.setMinWidth(100);
		column.setMaxWidth(400);
		column.setPreferredWidth(200);
		
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e)
            {
                if (listener != null)
                {
                    listener.stateChanged(new ChangeEvent(this));
                }
            }
        });
        
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
	
	protected void addColumnHeaders(final DefaultTableModel modelArg)
	{
		modelArg.addColumn("");
		modelArg.addColumn(nameColTitle);
		modelArg.addColumn(viewColTitle);
		modelArg.addColumn(addColTitle);
		modelArg.addColumn(modColTitle);
		modelArg.addColumn(delColTitle);
	}
	
	/**
	 * 
	 */
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
            principal = session.merge(principal);

    		int numRows = model.getRowCount();
    		int taskCol = permissionTable.getColumn(nameColTitle).getModelIndex();
    		int viewCol = permissionTable.getColumn(viewColTitle).getModelIndex();
    		int addCol  = permissionTable.getColumn(addColTitle).getModelIndex();
    		int modCol  = permissionTable.getColumn(modColTitle).getModelIndex();
    		int delCol  = permissionTable.getColumn(delColTitle).getModelIndex();

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
