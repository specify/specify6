/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.prefs;

import java.awt.Component;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 10, 2008
 *
 */
public interface PrefsPanelMgrIFace
{

    /**
     * Adds a prefs panel.
     * @param name the name of the panel
     * @param comp the UI component (usually a JPanel)
     * @return whether it was added
     */
    public abstract boolean addPanel(final String name, final Component comp);
    
    /**
     * Shows a prefs panel by name
     * @param name
     */
    public abstract void showPanel(final String name);
    
    /**
     * Request to the Prefs Manager to close, it will return false if it wasn't closed.
     * @return false if not close and the caller shouldn't continue
     */
    public abstract boolean closePrefs();
    
    
}
