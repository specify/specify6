/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.Component;

/**
 * Describes a Toolbar item for a plugin.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class ToolBarItemDesc
{
    public enum Position {Insert, Append, AppendNextToLast, AdjustRightLastComp}
    
    protected Component comp;
    protected Position  pos;
    protected int       index;
    
    /**
     * Constructs a toolbar item desc with the component that will be placed in the toolbar.
     * @param comp the component
     */
    public ToolBarItemDesc(final Component comp)
    {
        this(comp, Position.Append, -1);
    }

    /**
     * Constructs a toolbar item desc with the component that will be placed in the toolbar.
     * @param comp the component
     */
    public ToolBarItemDesc(final Component comp, final Position pos)
    {
        this(comp, pos, -1);
    }

    /**
     * Constructs a toolbar item desc with the component that will be placed in the toolbar and gives its position.
     * @param comp the component.
     * @param pos the position in the toolbar or related to the other items
     * @param index the index of where to place the item
     */
    public ToolBarItemDesc(final Component comp, final Position pos, final int index)
    {
        this.comp  = comp;
        this.pos   = pos;
        this.index = index;
    }

    /**
     * Returns the component.
     * @return the component
     */
    public Component getComp()
    {
        return comp;
    }

    /**
     * Sets the comp.
     * @param comp the comp
     */
    public void setComp(Component comp)
    {
        this.comp = comp;
    }

    /**
     * Returns the index.
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Sets the index.
     * @param index
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * Returns the poistion.
     * @return the position
     */
    public Position getPos()
    {
        return pos;
    }

    /**
     * Sets the position.
     * @param pos the position
     */
    public void setPos(Position pos)
    {
        this.pos = pos;
    }
    
}
