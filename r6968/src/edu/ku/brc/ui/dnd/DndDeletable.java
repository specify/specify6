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
package edu.ku.brc.ui.dnd;

import javax.swing.Icon;

import edu.ku.brc.ui.CommandAction;

/**
 * Interface for object that can be dropped into the trash, it makes the object responsible 
 * for knowing how to get itself deleted, instead of someone else.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public interface DndDeletable
{
    /**
     * Asks the object to send a request for itself to be deleted
     * @return return true if deleted, false if it couldn't be deleted
     */
    public boolean deleteRequest();
    
    /**
     * Returns an XML string for cahing the entire contents of the object to be serialized
     * @return Returns an XML string for cahing the entire contents of the object to be serialized
     */
    public String toXML();
    
    /**
     * The command that will be dispatched when "deleteRequest" is called
     * @param cmdAction the command that will delete the object from itself container
     */
    public void setDeleteCommandAction(final CommandAction cmdAction);
    
    /**
     * The name of the object being deleted
     * @return The name of the object being deleted
     */
    public String getName();
    
    /**
     * An icon that is representative of the item
     * @return An icon that is representative of the item
     */
    public Icon getIcon();
}
