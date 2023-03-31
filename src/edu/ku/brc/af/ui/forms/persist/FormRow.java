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
package edu.ku.brc.af.ui.forms.persist;

import java.util.Vector;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormRow implements Cloneable, FormRowIFace
{
    protected Vector<FormCellIFace> cells = new Vector<FormCellIFace>();
    protected byte rowNumber = 0;
    
    /**
     * Default Constructor
     *
     */
    public FormRow()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#addCell(edu.ku.brc.ui.forms.persist.FormCell)
     */
    public FormCellIFace addCell(FormCellIFace cell)
    {
        cells.add(cell);
        return cell;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#cleanUp()
     */
    public void cleanUp()
    {
        cells.clear();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#getCells()
     */
    public Vector<FormCellIFace> getCells()
    {
        return cells;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#setCells(java.util.Vector)
     */
    public void setCells(Vector<FormCellIFace> cells)
    {
        this.cells = cells;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormRow formRow = (FormRow)super.clone();
        formRow.cells   = new Vector<FormCellIFace>();
        for (FormCellIFace cell : cells)
        {
            formRow.cells.add((FormCell)cell.clone());
        }
        return formRow;      
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuilder sb)
    {
        sb.append("      <rows>\n");
        for (FormCellIFace cell : cells)
        {
            sb.append("        <row>\n");
            cell.toXML(sb);
            sb.append("        </row>\n");
        }
        sb.append("      </rows>\n");
    }    
    /**
     * @return the rowNumber
     */
    public byte getRowNumber()
    {
        return rowNumber;
    }

    /**
     * @param rowNumber the rowNumber to set
     */
    public void setRowNumber(byte rowNumber)
    {
        this.rowNumber = rowNumber;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return Byte.toString((byte)(rowNumber+1));
    }
}
