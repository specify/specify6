/* Copyright (C) 2015, University of Kansas Center for Research
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
