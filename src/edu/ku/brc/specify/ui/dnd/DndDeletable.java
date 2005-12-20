/* Filename:    $RCSfile: BaseTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.dnd;

import edu.ku.brc.specify.ui.CommandAction;

/**
 * Interface for object that can be dropped into the trash, it makes the object responsible 
 * for knowing how to get itself deleted, instead of someone else.
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
    public void setCommandAction(final CommandAction cmdAction);
}
