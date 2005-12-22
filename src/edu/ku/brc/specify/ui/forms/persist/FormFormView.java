/* Filename:    $RCSfile: FormFormView.java,v $
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

import java.util.List;
import java.util.Vector;


public class FormFormView extends FormView
{
    protected Vector<String>  columnDef = new Vector<String>();
    protected Vector<String>  rowDef    = new Vector<String>();
    protected Vector<FormRow> rows      = new Vector<FormRow>(); 

    /**
     * 
     *
     */
    public FormFormView()
    {
        super(ViewType.form, -1, "", "");
        
    }

    /**
     * Creates a Form View
     * @param type the type (could be form or field)
     * @param id the id
     * @param name the name
     * @param desc description
     */
    public FormFormView(final FormView.ViewType type, final int id, final String name, final String desc)
    {
        super(type, id, name, desc);
        
    }
    
    public void addColDef(String aColDef)
    {
        columnDef.add(aColDef);
    }

    public void addRowDef(String aRowDef)
    {
        rowDef.add(aRowDef);
    }
    
    public FormRow addRow(FormRow aRow)
    {
        rows.add(aRow);
        return aRow;
    }

    public List<String> getColumnDef()
    {
        return columnDef;
    }

    public List<String> getRowDef()
    {
        return rowDef;
    }

    public List<FormRow> getRows()
    {
        return rows;
    }

    public void setColumnDef(Vector<String> columnDef)
    {
        this.columnDef = columnDef;
    }

    public void setRowDef(Vector<String> rowDef)
    {
        this.rowDef = rowDef;
    }

    public void setRows(Vector<FormRow> rows)
    {
        this.rows = rows;
    }
    
}
