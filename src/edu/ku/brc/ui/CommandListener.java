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
