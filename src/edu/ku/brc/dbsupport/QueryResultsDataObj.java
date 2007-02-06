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

package edu.ku.brc.dbsupport;

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
    protected String  formatStr     = "";
    
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
     * Constructs with object. Sometimes it is convient for the owner to pre-seed the value.
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
    public QueryResultsDataObj(Object result)
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
