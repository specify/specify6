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

/**
 * This represents a layout panel (JPanel) that supports JGoodies layout
 * 
 * @author rods
 *
 */
public class FormCellPanel extends FormCell
{
    protected String colDef;
    protected String rowDef;
    protected String panelType;
    protected List<FormRow> rows = new Vector<FormRow>(); 

    /**
     * Constructor
     * @param name the name
     * @param id the id
     * @param colDef JGoodies column definition
     * @param rowDef JGoodies row definition
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    public FormCellPanel(final String            id, 
                         final String            name,
                         final String            panelType, 
                         final String            colDef, 
                         final String            rowDef,
                         final int               colspan, 
                         final int               rowspan)
    {
        super(FormCell.CellType.panel, id, name, colspan, rowspan);
        this.panelType    = panelType;
        this.ignoreSetGet = true;
        this.colDef       = colDef;
        this.rowDef       = rowDef;
    }
    
    /**
     * Add a row to the form
     * @param row the row to add
     * @return the row that was added
     */
    public FormRow addRow(FormRow row)
    {
        rows.add(row);
        return row;
    }    
    
    public String getColDef()
    {
        return colDef;
    }

    public String getRowDef()
    {
        return rowDef;
    }

    public String getPanelType()
    {
        return panelType;
    }

    public List<FormRow> getRows()
    {
        return rows;
    }

}
