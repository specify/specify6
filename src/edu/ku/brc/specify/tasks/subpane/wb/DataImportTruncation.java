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
package edu.ku.brc.specify.tasks.subpane.wb;



/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class DataImportTruncation
{
    protected int    row;
    protected short    col;
    protected int len;
    protected String colHeader;
    protected String originalValue;

    /**
     * @return the originalValue
     */
    public String getOriginalValue()
    {
        return originalValue;
    }

    /**
     * @return the row
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return the col
     */
    public short getCol()
    {
        return col;
    }

    /**
     * @return the colHeader
     */
    public String getColHeader()
    {
        return colHeader;
    }

    /**
     * @return portion of original value that was not imported
     */
    public String getExcluded()
    {
        return this.originalValue.substring(len);
    }
    
    public DataImportTruncation(final int row, final short col, final int len, final String colHeader, final String originalValue)
    {
        super();
        this.row = row;
        this.col = col;
        this.len = len;
        this.colHeader = colHeader;
        this.originalValue = originalValue;
     }

}
