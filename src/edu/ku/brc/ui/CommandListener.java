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
package edu.ku.brc.ui;

/**
 * Listeners are notified by the dispatcher through this interface that a command has been generated. 
 * Each listener is then notified in no particular order. Any consumer can "consuke" the command at 
 * any time
 * @code_status Beta
 *.
 * @author rods
 *
 */
public interface CommandListener
{

    /**
     * Listeners are notified by the dispatcher through this method
     * @param cmdAction the command to be executed
     */
    public void doCommand(CommandAction cmdAction);
    
}
