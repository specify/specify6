/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
