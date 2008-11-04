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
     * Prevent editing on components in editor if parameter is true.
     * This method should really be called setEnabled() but isn't to avoid collision with JPanel's 
     * method with the same name.
     *  
     * @param readOnly boolean value indicating whether to make the component read only or not.
     */
    public abstract void setReadOnly(boolean readOnly);

    /**
     * @param l
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /**
     * @param l
     */
    public abstract void removeChangeListener(ChangeListener l);
    
}
