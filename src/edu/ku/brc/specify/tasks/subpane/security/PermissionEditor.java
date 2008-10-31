/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.PermissionPanelContainerIFace;
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
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PermissionEditor extends JPanel implements PermissionPanelContainerIFace
{
    private static final Logger log = Logger.getLogger(PermissionEditor.class);

    protected String                panelName;
	protected JTable				table;
	protected PermissionEnumerator 	enumerator;
	protected SpPrincipal 			principal;
	protected ChangeListener        listener;
	protected boolean               readOnly;
	
	protected DefaultTableModel     model;
	protected ImageIcon             icon;
	
    protected String                nameColTitle;
    protected String                viewColTitle;
    protected String                addColTitle;
    protected String                modColTitle;
    protected String                delColTitle;
    
    protected Vector<PermissionEditorRowIFace> rowDataList = new Vector<PermissionEditorRowIFace>();
	
	/**
	 * @param panelName
	 * @param enumerator
	 * @param listener
	 */
	public PermissionEditor(final String panelName,
	                        final PermissionEnumerator enumerator,
	                        final ChangeListener       listener)
	{
        this(panelName, enumerator, listener, false, 
             "SEC_NAME_TITLE", "SEC_VIEW_TITLE", "SEC_ADD_TITLE", "SEC_MOD_TITLE", "SEC_DEL_TITLE");
	}

	/**
	 * @param panelName
	 * @param enumerator
	 * @param listener
	 * @param readOnly
	 */
	public PermissionEditor(final String               panelName,
                            final PermissionEnumerator enumerator,
                            final ChangeListener       listener, 
	                        final boolean              readOnly,
                            final String               nameKey,
                            final String               viewKey,
                            final String               addKey,
                            final String               modKey,
                            final String               delKey)
	{
	    super(new BorderLayout());
	    
	    nameColTitle = UIRegistry.getResourceString(nameKey);
	    viewColTitle = viewKey != null ? UIRegistry.getResourceString(viewKey) : null;
	    addColTitle  = addKey  != null ? UIRegistry.getResourceString(addKey) : null;
	    modColTitle  = modKey  != null ? UIRegistry.getResourceString(modKey) : null;
	    delColTitle  = delKey  != null ? UIRegistry.getResourceString(delKey) : null;

	    
        this.panelName  = panelName;
        this.table      = new JTable();
		this.enumerator = enumerator;
		this.principal 	= null;
		this.listener   = listener;
		this.readOnly   = readOnly;
		
		UIHelper.makeTableHeadersCentered(table, false);
		
		JScrollPane sp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelIFace#getPanelName()
     */
    @Override
    public String getPanelName()
    {
        return panelName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#getPermissionEnumerator()
     */
    @Override
    public PermissionEnumerator getPermissionEnumerator()
    {
        return enumerator;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.auth.PermissionPanelContainerIFace#updateData(edu.ku.brc.specify.datamodel.SpPrincipal, edu.ku.brc.specify.datamodel.SpPrincipal, java.util.Hashtable, java.util.Hashtable, java.lang.String)
	 */
	public void updateData(final SpPrincipal principalArg, 
                           final SpPrincipal overrulingPrincipalArg, 
                           final Hashtable<String, SpPermission> existingPerms,
                           final Hashtable<String, SpPermission> overrulingPerms,
                           final String     userType)
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
		
		rowDataList.clear();
		
		List<PermissionEditorRowIFace> perms = enumerator.getPermissions(principalArg, existingPerms, overrulingPerms, userType);
		Collections.sort(perms, new ComparatorByStringRepresentation<PermissionEditorRowIFace>(true));
        for (PermissionEditorRowIFace permWrapper : perms) 
        {
            rowDataList.add(permWrapper);
        }
        
        if (model == null) 
        {
            return;
        }
        
        while (model.getRowCount() > 0)
        {
            model.removeRow(0);
        }
        
        for (PermissionEditorRowIFace permWrapper : rowDataList) 
        {
            permWrapper.addTableRow(model, icon);
        }
        
		table.setModel(model);
		table.setRowHeight(label.getPreferredSize().height+3);
		
		int rows   = 15;
		int height = 0; 
	    for(int row = 0; row < rows; row++) 
	    {
	        height += table.getRowHeight(row);
	    }
	 
	    table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredScrollableViewportSize().width, height)); 

		TableColumn column = table.getColumnModel().getColumn(0);
		int cellWidth = iconSize.size()+4;
		column.setMinWidth(cellWidth);
		column.setMaxWidth(cellWidth);
		column.setPreferredWidth(cellWidth);
        
        // For Strings with no changes made to the table, the render is a DefaultTableCellRender.
        //DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) column.getCellRenderer();
        // set the alignment to center
        //dtcr.setHorizontalAlignment(SwingConstants.CENTER);

		column = table.getColumnModel().getColumn(1);
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
	
	/**
	 * @param modelArg the model
	 */
	protected void addColumnHeaders(final DefaultTableModel modelArg)
	{
		modelArg.addColumn("");
		modelArg.addColumn(nameColTitle);
		
		if (viewColTitle != null) modelArg.addColumn(viewColTitle);
		if (addColTitle != null) modelArg.addColumn(addColTitle);
		if (modColTitle != null) modelArg.addColumn(modColTitle);
		if (delColTitle != null) modelArg.addColumn(delColTitle);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.auth.PermissionPanelMgrIFace#savePermissions(edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	public void savePermissions(final DataProviderSessionIFace session) throws Exception
	{
		// nothing to save if we didn't specify a principal yet
		if (principal == null)
		{
			return;
		}
		
        log.debug("Saving Principal: "+principal.getId()+"  hashCode: "+principal.hashCode());
        
		int numRows = model.getRowCount();
		int taskCol = nameColTitle != null ? table.getColumn(nameColTitle).getModelIndex() : -1;
		int viewCol = viewColTitle != null ? table.getColumn(viewColTitle).getModelIndex() : -1;
		int addCol  = addColTitle != null ? table.getColumn(addColTitle).getModelIndex() : -1;
		int modCol  = modColTitle != null ? table.getColumn(modColTitle).getModelIndex() : -1;
		int delCol  = delColTitle != null ? table.getColumn(delColTitle).getModelIndex() : -1;

		for (int row = 0; row < numRows; ++row)
		{
		    PermissionEditorRowIFace wrapper = (PermissionEditorRowIFace) model.getValueAt(row, taskCol);
			SpPermission perm = wrapper.getPermissionList().get(0); // Only has one
		    
			Boolean canView = viewCol > -1 ? (Boolean) model.getValueAt(row, viewCol) : false;
			Boolean canAdd  = addCol > -1 ? (Boolean) model.getValueAt(row, addCol) : false;
			Boolean canMod  = modCol > -1 ? (Boolean) model.getValueAt(row, modCol) : false;
			Boolean canDel  = delCol > -1 ? (Boolean) model.getValueAt(row, delCol) : false;
			
			if ( !(canView || canAdd || canMod || canDel) )
			{
				// no flag is set, so delete the permission
				if (perm.getId() != null)
				{
				    perm.setActions("");
                    session.saveOrUpdate(session.merge(perm));
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
					perm.getPrincipals().add(principal);
				}
				session.saveOrUpdate(session.merge(perm));
			}
		}
	}
}
