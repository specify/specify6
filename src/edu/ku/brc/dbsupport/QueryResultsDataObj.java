/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.dbsupport;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

/**
 * A class that describes where the the desired piece of data is located (row/col) in the results set 
 * and whether it has been processed, meaning: does it have it's value yet?
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class QueryResultsDataObj 
{
    protected Object  result        = null;
    protected int     row           = 0;
    protected int     col           = 0;
    protected boolean isProcessable = true;
    protected String  formatStr     = ""; //$NON-NLS-1$
    
    /**
     * Constructs with row and column.
     * @param row the row it came from
     * @param col the column it came from
     * @param formatStr the string format
     */
    public QueryResultsDataObj(final int row, final int col, final String formatStr)
    {
        this.row       = row;
        this.col       = col;
        this.formatStr = formatStr;
    }

    /**
     * Constructs with row and column.
     * @param row the row it came from
     * @param col the column it came from
     */
    public QueryResultsDataObj(final int row, final int col)
    {
        this(row, col, null);
    }

    /**
     * Constructs with object with a format..
     * @param result the value of the object
     * @param formatStr the string format
     */
    public QueryResultsDataObj(final Object result, final String formatStr)
    {
        this.result        = result;
        this.isProcessable = false;
        this.formatStr     = formatStr;
    }

    /**
     * Constructs with object. Sometimes it is convient for the owner to pre-seed the value.
     */
    public QueryResultsDataObj(final Object result)
    {
        this(result, null);
    }

    /**
     * Returns the processed result.
     * @return Returns the processed result
     */
    public Object getResult()
    {
        if (StringUtils.isNotEmpty(formatStr))
        {
            if (result instanceof BigDecimal && formatStr.startsWith("%")  && formatStr.endsWith("f"))
            {
                result = ((BigDecimal)result).doubleValue();
            }
            return String.format(formatStr, new Object[] {result} );
        }
        return result;
    }

    /**
     * Sets the processed results.
     * @param result the new value
     */
    public void setResult(Object result)
    {
        this.result = result;
    }

    /**
     * Returns the column wher the data is located in the resultset.
     * @return Returns the column where the data is located in the resultset
     */
    public int getCol()
    {
        return col;
    }

    /**
     * Sest the column of where the data is located.
     * @param col the column
     */
    public void setCol(int col)
    {
        this.col = col;
    }

    /**
     * Returns the row where the data is located in the resultset.
     * @return Returns the row where the data is located in the resultset
     */
    public int getRow()
    {
        return row;
    }

    /**
     * Sest the row of where the data is located.
     * @param row the row
     */
    public void setRow(int row)
    {
        this.row = row;
    }

    /**
     * Returns whether it has been processed or not.
     * @return Returns whether it has been processed or not
     */
    public boolean isProcessable()
    {
        return isProcessable;
    }
    
    /**
     * Clear the result pointer so we can prevent memory leaks.
     *
     */
    public void clear()
    {
        result = null;
    }
    
}
