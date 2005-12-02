/* Filename:    $RCSfile: FormRow.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
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
package edu.ku.brc.specify.ui.forms.persist;

import java.util.*;

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
     * @param aCell the cell to be added
     * @return the same FormCell
     */
    public FormCell addCell(FormCell aCell)
    {
        cells.add(aCell);
        return aCell;
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

    //-------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------
    
    public FormCell createSubView(String aName, String aViewSetName, int aId, String aClass)
    {
        return addCell(new FormCellSubView(aName, aViewSetName, aId, aClass));
    }
    
    public FormCell createField(String aName, String aLabel)
    {
        return addCell(new FormCellWithLabel(FormCell.CellType.field, aName, aLabel));
    }
    
    public FormCell createLabel(String aLabel)
    {
        return addCell(new FormCellWithLabel(FormCell.CellType.label, null, aLabel));
    }
    
    public FormCell createSeparator(String aLabel)
    {
        return addCell(new FormCellWithLabel(FormCell.CellType.separator, null, aLabel));
    }
    
    public FormCell createCell(FormCell.CellType aType, String aName, String aLabel)
    {
        return addCell(new FormCellWithLabel(aType, aName, aLabel));
    }
    
    
}
