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
