/* Copyright (C) 2009, University of Kansas Center for Research
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
 * Interface to notify folks when an SubPaneIFace is added or removed from the manager.
 * The most important notification is when a SubPane is closed and the task can be notified.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface SubPaneMgrListener
{
    /**
     * Notication that a SubPane was added to the manager.
     * @param subPane the subpane that was added
     */
    public void subPaneAdded(SubPaneIFace subPane);
    
    /**
     * Notication that a SubPane was removed from the manager.
     * @param subPane the subpane that was removed
     */
    public void subPaneRemoved(SubPaneIFace subPane);
    
    
    /**
     * Notication that a SubPane was removed from the manager.
     * @param subPane the subpane that was removed
     */
    public void subPaneShown(SubPaneIFace subPane);
    
    
}
