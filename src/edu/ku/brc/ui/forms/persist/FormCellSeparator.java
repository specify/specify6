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

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormCellSeparator extends FormCell
{
    protected String label;
    protected String collapseCompName;

    public FormCellSeparator(final String id, 
                             final String name, 
                             final String label, 
                             final int    colspan)
    {
        this(id, name, label, null, colspan);
    }    
    
    public FormCellSeparator(final String id, 
                             final String name, 
                             final String label, 
                             final String collapseCompName,
                             final int    colspan)
     {
         super(FormCell.CellType.separator, id, name);
         
         this.label   = label;
         this.collapseCompName   = collapseCompName;
         this.colspan = colspan;
         this.ignoreSetGet = true;
     }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getCollapseCompName()
    {
        return collapseCompName;
    }
    
    
}
