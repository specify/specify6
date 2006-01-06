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
    
    public FormCell createSubView(String name, 
                                  String viewSetName, 
                                  int    id, 
                                  String classObj,
                                  int    colspan, 
                                  int    rowspan)
    {
        return addCell(new FormCellSubView(name, viewSetName, id, classObj, colspan, rowspan));
    }
    
    public FormCell createField(String name, String label)
    {
        return addCell(new FormCell(FormCell.CellType.field, name, label));
    }
    
    public FormCell createLabel(String label)
    {
        return addCell(new FormCell(FormCell.CellType.label, null, label));
    }
    
    public FormCell createSeparator(String aLabel)
    {
        return addCell(new FormCell(FormCell.CellType.separator, null, aLabel));
    }
    
    public FormCell createCell(FormCell.CellType type, 
                               String name, 
                               String label, 
                               String uitype, 
                               String format, 
                               int    cols, 
                               int    rows, 
                               int    colspan, 
                               int    rowspan)
    {
        return addCell(new FormCell(type, name, label, uitype, format, cols, rows, colspan, rowspan));
    }
    
}
