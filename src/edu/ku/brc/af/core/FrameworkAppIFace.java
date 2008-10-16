/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.core;

/**
 * Interface that applications must implement so it can hook into the Mac Application Hooks.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2008
 *
 */
public interface FrameworkAppIFace
{

    /**
     * @param doAppExit whether to exit it not
     * @return whether it exited.
     */
    public abstract boolean doExit(boolean doAppExit);
    
    /**
     * Shows the preferences dialog.
     */
    public abstract void doPreferences();
    
    /**
     * Shows the About dialog.
     */
    public abstract void doAbout();
}
