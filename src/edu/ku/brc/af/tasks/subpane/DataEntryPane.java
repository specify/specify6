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

package edu.ku.brc.af.tasks.subpane;

import edu.ku.brc.af.core.Taskable;

/**
 * This pane will be responsible for creating forms, but at the moment I am not sure what it really does.
 *
 * @code_status Code Freeze
 * 
 * @author rods
 * 
 */
@SuppressWarnings("serial")
public class DataEntryPane extends BaseSubPane
{
    /**
     * The data entry pane.
     * @param name the name of the subpane
     * @param task the owning task
     */
    public DataEntryPane(final String name, 
                         final Taskable task)
    {
        super(name, task);
        
    }
    
}
