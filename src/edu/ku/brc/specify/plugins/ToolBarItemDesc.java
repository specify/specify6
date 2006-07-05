/* Filename:    $RCSfile: ToolBarItemDesc.java,v $
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

package edu.ku.brc.specify.plugins;

import java.awt.Component;

public class ToolBarItemDesc
{

    public enum Position {Insert, Append, AppendNextToLast}
    
    protected Component comp;
    protected Position  pos;
    protected int       index;
    
    public ToolBarItemDesc(Component comp)
    {
        this.comp = comp;
        this.pos = Position.Append;
        this.index = -1;
    }

    public ToolBarItemDesc(Component comp, Position pos, int index)
    {
        this.comp = comp;
        this.pos = pos;
        this.index = index;
    }

    public Component getComp()
    {
        return comp;
    }

    public void setComp(Component comp)
    {
        this.comp = comp;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public Position getPos()
    {
        return pos;
    }

    public void setPos(Position pos)
    {
        this.pos = pos;
    }
    
}
