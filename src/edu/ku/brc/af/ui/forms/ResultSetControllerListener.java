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
package edu.ku.brc.af.ui.forms;

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
