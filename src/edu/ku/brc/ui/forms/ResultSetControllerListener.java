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
package edu.ku.brc.ui.forms;

/**
 * Interface that enables objects to listen for changes to the current record of a record set
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface ResultSetControllerListener
{
    /**
     * Notifies listener that the current record has changed
     * @param newIndex the new index
     */
    public void indexChanged(int newIndex);
    
    
    /**
     * Notifies the listener that the index is about to change
     * @param oldIndex the previous index 
     * @param newIndex the new index
     * @return (this is unused at the moment but it could tell the caller whether it should change the index)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex);
    
    /**
     * Notifies the listener that a new record was added.
     */
    public void newRecordAdded();
    
}
