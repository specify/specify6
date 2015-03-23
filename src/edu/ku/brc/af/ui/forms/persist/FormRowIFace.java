/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

public interface FormRowIFace
{

    /**
     * Adds a FormCell and return the same FormCell
     * @param cell the cell to be added
     * @return the same FormCell
     */
    public abstract FormCellIFace addCell(FormCellIFace cell);

    /**
     * Clean up internal data
     */
    public abstract void cleanUp();

    /**
     * @return Return the collection of cells
     */
    public abstract Vector<FormCellIFace> getCells();

    /**
     * @param cells all the cells
     */
    public abstract void setCells(Vector<FormCellIFace> cells);
    
    /**
     * @param num
     */
    public abstract void setRowNumber(byte num);
    
    /**
     * Appends its XML.
     * @param sb the buffer
     */
    public abstract void toXML(final StringBuilder sb);


    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

}
