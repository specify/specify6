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

/**
 * Provides callbacks for drop events on JLists.
 *
 * @code_status Complete
 * @author jstewart
 */
public interface DragDropCallback
{
    /**
     * Performs all tasks required by the event of <code>dragged</code>
     * being dropped on <code>droppedOn</code>.
     *
     * @param draggedObj the dragged object
     * @param dropLocObj the object that <code>dragged</code> was dropped on
     * @param dropAction the type of drop action
     * @return true if the drop was successfully handled
     */
    public boolean dropOccurred(Object dragged, Object droppedOn, int dropAction);
    
    /**
     * Determines if a drop of <code>dragged</code> being dropped on <code>droppedOn</code>
     * is acceptable.
     * 
     * @param dragged the dragged object
     * @param droppedOn the object that <code>dragged</code> is being dropped on
     * @param dropAction the type of drop action
     * @return true if the drop is acceptable
     */
    public boolean dropAcceptable(Object dragged, Object droppedOn, int dropAction);
    
    /**
     * Signals that a drag-and-drop operation ended.
     * 
     * @param success the end status of the drag-and-drop operation
     */
    public void dragDropEnded(boolean success);
}
