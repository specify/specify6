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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
     * Constructor with Commandaction
     * @param commandAction the command action to send
     */
    public CommandActionWrapper(final CommandAction commandAction)
    {
        this.commandAction = commandAction;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        CommandDispatcher.dispatch(commandAction);
    }
}
