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
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.ku.brc.ui.dnd.DataActionEvent;

/**
 * Utility class for encapsulating an CommandAction into a ActionListener
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class CommandActionWrapper implements ActionListener 
{
    private CommandAction commandAction;
    
    /**
     * Constructor with CommandAction.
     * @param commandAction the command action to send
     */
    public CommandActionWrapper(final CommandAction commandAction)
    {
        this.commandAction = commandAction;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) 
    {
        if (e instanceof DataActionEvent)
        {
            commandAction.setConsumed(false);
            DataActionEvent dataActionEv = (DataActionEvent)e;
            commandAction.setData(dataActionEv.getSourceObj() != null ? dataActionEv.getSourceObj().getData() : null); // Source shouldn't ever be null
        }
        CommandDispatcher.dispatch(commandAction);
    }
}
