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
package edu.ku.brc.af.core;

import java.util.Properties;

/**
 * Class that defines a command that is issued by UI components in a Task's SubPane or from the side bar.
 * These commands are typically serviced by the task itself.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class TaskCommandDef
{
    protected String iconName;
    protected String name;
    protected Properties params;

    /**
     * Constructs a TaskCommand Definition.
     * @param name the name of the command
     * @param iconName the icon for the command (to be looked up in the IconManager by name)
     * @param params a Hash Map of parameters
     */
    public TaskCommandDef(final String name, final String iconName, final Properties params)
    {
        this.iconName = iconName;
        this.name     = name;
        this.params   = params;
    }

    public String getIconName()
    {
        return iconName;
    }

    public String getName()
    {
        return name;
    }

    public Properties getParams()
    {
        return params;
    }

}
