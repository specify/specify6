/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.specify.datamodel.SpPermission;

/**
 * Wraps an SpPermission object and adds functionality needed to display and
 * handle it when edited in a permission table.
 * 
 * @author Ricardo
 * 
 */
public class GeneralPermissionEditorRow implements PermissionEditorRowIFace
{
    protected String                type;
    protected SpPermission          permission;
    protected SpPermission          overrulingPermission;
    protected String                title;
    protected String                description;
    protected ImageIcon             icon;
    protected PermissionEditorIFace editorPanel;
    protected boolean               adminPrincipal;
    
    protected GeneralPermissionTableCellValueWrapper viewWrap = null;
    protected GeneralPermissionTableCellValueWrapper modWrap  = null;
    protected GeneralPermissionTableCellValueWrapper delWrap  = null;
    protected GeneralPermissionTableCellValueWrapper addWrap  = null;
    
    

    /**
     * @param permission
     * @param overrulingPermission
     * @param type
     * @param title
     * @param description
     * @param icon
     * @param editorPanel
     */
    public GeneralPermissionEditorRow(final SpPermission permission,
                                      final SpPermission overrulingPermission, 
                                      final String type,
                                      final String title, 
                                      final String description, 
                                      final ImageIcon icon,
                                      final PermissionEditorIFace editorPanel,
                                      final boolean adminPrincipal)
    {
        this.permission     = permission;
        this.overrulingPermission = overrulingPermission;
        this.type           = type;
        this.title          = title;
        this.description    = description;
        this.icon           = icon;
        this.editorPanel    = editorPanel;
        this.adminPrincipal = adminPrincipal;
        
        viewWrap = new GeneralPermissionTableCellValueWrapper(permission, overrulingPermission, "view", adminPrincipal);
        addWrap  = new GeneralPermissionTableCellValueWrapper(permission, overrulingPermission, "add", adminPrincipal);
        modWrap  = new GeneralPermissionTableCellValueWrapper(permission, overrulingPermission, "modify", adminPrincipal);
        delWrap  = new GeneralPermissionTableCellValueWrapper(permission, overrulingPermission, "delete", adminPrincipal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissionList()
     */
    public List<SpPermission> getPermissionList()
    {
        ArrayList<SpPermission> list = new ArrayList<SpPermission>(1);
        list.add(permission);
        return list;
    }

    /**
     * @return the type
     */
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getType()
     */
    @Override
    public String getType()
    {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getIcon()
     */
    @Override
    public ImageIcon getIcon()
    {
        return icon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getDescription()
     */
    @Override
    public String getDescription()
    {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getEditorPanel()
     */
    @Override
    public PermissionEditorIFace getEditorPanel()
    {
        return editorPanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#getPermissions()
     */
    @Override
    public List<PermissionIFace> getPermissions()
    {
        ArrayList<PermissionIFace> list = new ArrayList<PermissionIFace>(1);

        SpPermission perm = overrulingPermission != null ? overrulingPermission : permission;
        int options = PermissionSettings.NO_PERM;
        options |= perm.canModify() ? PermissionSettings.CAN_MODIFY : 0;
        options |= perm.canView() ?   PermissionSettings.CAN_VIEW   : 0;
        options |= perm.canAdd() ?    PermissionSettings.CAN_ADD    : 0;
        options |= perm.canDelete() ? PermissionSettings.CAN_DELETE : 0;
        list.add(new PermissionSettings(options));

        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#setPermissions(edu.ku.brc.af.auth.PermissionIFace)
     */
    @Override
    public void setPermissions(final List<PermissionIFace> permissionSettings)
    {
        PermissionIFace permSettings = permissionSettings.get(0);
        permission.setActions(permSettings.canView(), permSettings.canAdd(), permSettings.canModify(), permSettings.canDelete());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#addTableRow(javax.swing.table.DefaultTableModel,
     *      javax.swing.ImageIcon)
     */
    @Override
    public void addTableRow(final DefaultTableModel model, final ImageIcon defaultIcon)
    {
        model.addRow(new Object[] { icon != null ? icon : defaultIcon, this, viewWrap, addWrap, modWrap, delWrap});
    }
    
    /**
     * @param option
     * @return
     */
    public String getOverrideText(final int option)
    {
        switch (option)
        {
            case  1 : return viewWrap != null && viewWrap.isOverriden() ? viewWrap.getOverrulingPermissionText() : null;
                
            case  2 : return modWrap != null && modWrap.isOverriden() ? modWrap.getOverrulingPermissionText() : null;
                
            case  4 : return delWrap != null && delWrap.isOverriden() ? delWrap.getOverrulingPermissionText() : null;
                
            case  8 : return addWrap != null && addWrap.isOverriden() ? addWrap.getOverrulingPermissionText() : null;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#updatePerm(edu.ku.brc.specify.datamodel.SpPermission,
     *      edu.ku.brc.specify.datamodel.SpPermission)
     */
    @Override
    public void updatePerm(final SpPermission oldPerm, final SpPermission newPerm)
    {
        if (oldPerm == permission)
        {
            permission = newPerm;

        } else if (overrulingPermission == permission)
        {
            overrulingPermission = newPerm;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(PermissionEditorRowIFace o)
    {
        return getTitle().compareTo(o.getTitle());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#isAdminPrincipal()
     */
    public boolean isAdminPrincipal()
    {
        return adminPrincipal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEditorRowIFace#setAdminPrincipal(boolean)
     */
    public void setAdminPrincipal(boolean adminPrincipal)
    {
        this.adminPrincipal = adminPrincipal;
    }

}
