/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import javax.swing.ImageIcon;

/**
 * An interface describing the required capabilities for an object that can
 * be put into an IconTray widget.
 * 
 * @author jstewart
 * @code_status Complete
 */
public interface Trayable
{
    /**
     * Gets a representative icon for the called object.
     * 
     * @return an ImageIcon representative of the called object
     */
    public ImageIcon getIcon();
    
    /**
     * Gets a human-readable name for the called object.
     * 
     * @return a human-readable display name for the called object
     */
    public String getName();
}
