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

package edu.ku.brc.af.plugins;

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
    public enum Position {Insert, Append, AppendNextToLast}
    
    protected Component comp;
    protected Position  pos;
    protected int       index;
    
    /**
     * Constructs a toolbar item desc with the component that will be placed in the toolbar.
     * @param comp the component
     */
    public ToolBarItemDesc(Component comp)
    {
        this.comp = comp;
        this.pos = Position.Append;
        this.index = -1;
    }

    /**
     * Constructs a toolbar item desc with the component that will be placed in the toolbar and gives its position.
     * @param comp the component.
     * @param pos the position in the toolbar or related to the other items
     * @param index the index of where to place the item
     */
    public ToolBarItemDesc(Component comp, Position pos, int index)
    {
        this.comp = comp;
        this.pos = pos;
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
