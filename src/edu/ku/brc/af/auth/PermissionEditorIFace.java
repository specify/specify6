/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.auth;

import java.awt.Component;
import java.util.List;

import javax.swing.event.ChangeListener;

import edu.ku.brc.af.core.PermissionIFace;

/**
 * A custom editor for a set of permissions. It is used when View/Modify/Delete/Add are used differently
 * to describe permissions for an object. For example, the Backup tool may use View for enabling 
 * Backup and Modify for enabling Restore.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 21, 2008
 *
 */
public interface PermissionEditorIFace
{
    /**
     * @param permissions
     */
    public abstract void setPermissions(List<PermissionIFace> permissions);
    
    /**
     * @return
     */
    public abstract List<PermissionIFace> getPermissions();
    
    /**
     * @return
     */
    public abstract Component getUIComponent();
    
    /**
     * @return
     */
    public abstract boolean hasChanged();
    
    /**
     * 
     */
    public abstract void setChanged(boolean changed);

    /**
     * @param title
     */
    public abstract void setTitle(String title);
    
    /**
     * @return an array of table ids that is associated with the object that the editor is representing.
     */
    public abstract int[] getAssociatedTableIds();
    
    /**
     * @param option the option (view, add, modify, delete)
     * @param text the order text for it or null
     * @param readOnly whether the panel is readonly
     */
    public abstract void setOverrideText(int option, String text, boolean readOnly);

    /**
     * @param l
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /**
     * @param l
     */
    public abstract void removeChangeListener(ChangeListener l);
    
}
