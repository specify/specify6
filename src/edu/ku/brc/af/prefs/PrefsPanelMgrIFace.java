/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
