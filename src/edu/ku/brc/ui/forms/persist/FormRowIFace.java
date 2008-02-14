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