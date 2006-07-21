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
package edu.ku.brc.ui.forms.persist;

import java.util.List;
import java.util.Vector;

public class FormRow
{
    Vector<FormCell> cells = new Vector<FormCell>();
    
    /**
     * Default Constructor
     *
     */
    public FormRow()
    {
        
    }
    
    /**
     * Adds a FormCell and return the same FormCell
     * @param cell the cell to be added
     * @return the same FormCell
     */
    public FormCell addCell(FormCell cell)
    {
        cells.add(cell);
        return cell;
    }
    
    /**
     * Clean up internal data
     */
    public void cleanUp()
    {
        cells.clear();
    }
    
    /**
     * @return Return the collection of cells
     */
    public List<FormCell> getCells()
    {
        return cells;
    }

    /**
     * @param cells all the cells
     */
    public void setCells(Vector<FormCell> cells)
    {
        this.cells = cells;
    }
}
