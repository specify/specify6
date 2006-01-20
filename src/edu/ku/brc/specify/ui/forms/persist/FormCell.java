/* Filename:    $RCSfile: FormCell.java,v $
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

/**
 * This represents all the information about a cell in the form.
 * @author rods
 *
 */
public class FormCell
{
    public enum CellType {separator, field, label, subview, command};
    
    // Required fields
    protected CellType type;
    protected String   name;

    protected int      colspan = 1;
    protected int      rowspan = 1;
   
   
    /**
     * 
     */
    public FormCell()
    {
        
    }

    /**
     * Constructor
     * @param type type of cell
     * @param name the name
     */
    public FormCell(final CellType type, final String name)
    {
        this.type = type;
        this.name = name;
    }
    
    /**
     * Constructor
     * @param type type of cell
     * @param name the name
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    public FormCell(final CellType type, 
                    final String   name, 
                    final int      colspan, 
                    final int      rowspan)
    {
        this(type, name);

        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    public int getColspan()
    {
        return colspan;
    }

    public void setColspan(int colspan)
    {
        this.colspan = colspan;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRowspan()
    {
        return rowspan;
    }

    public void setRowspan(int rowspan)
    {
        this.rowspan = rowspan;
    }

    public CellType getType()
    {
        return type;
    }

    public void setType(CellType type)
    {
        this.type = type;
    }

 }
