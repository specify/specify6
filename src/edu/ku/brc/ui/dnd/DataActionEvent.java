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
package edu.ku.brc.ui.dnd;

import java.awt.event.ActionEvent;
/**
 * 
 * An DataAction Event that knows the source and destination and can carry a data object with it.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DataActionEvent extends ActionEvent
{
    protected GhostActionable sourceObj = null;
    protected GhostActionable destObj   = null;
    protected Object          data      = null;

    /**
     * Constructor.
     * @param source the source object, the object that initiated the drag-n-drop.
     * @param destination the destination object, the object it was dropped on.
     * @param data the data of the drag-n-drop
     */
    public DataActionEvent(final GhostActionable sourceObj, final GhostActionable destObj, final Object data)
    {
        super(sourceObj, 1, null);
        
        this.sourceObj = sourceObj;
        this.destObj   = destObj;
        this.data      = data;
    }
    
    /**
     * Returns the data object.
     * @return the data object.
     */
    public Object getData()
    {
        return data;
    }

    /**
     * Returns the destination object (this is typically the object that the object was dropped on).
     * @return the destination object
     */
    public GhostActionable getDestObj()
    {
        return destObj;
    }

    /**
     * Sets the destination object (this is usually called after the drop).
     * @param destObj the destination object.
     */
    public void setDestObj(final GhostActionable destObj)
    {
        this.destObj = destObj;
    }

    /**
     * Returns the source object (this is typically the object that the object was dropped on).
     * @return the source object
     */
    public GhostActionable getSourceObj()
    {
        return sourceObj;
    }

}
