/* Filename:    $RCSfile: DBConnection.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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

package edu.ku.brc.specify.dbsupport;

public class QueryResultsDataObj 
{
    protected Object  result = null;
    protected int     row    = 0;
    protected int     col    = 0;
    protected boolean isProcessable = true;
    
    /**
     * 
     * @param row
     * @param col
     */
    public QueryResultsDataObj(int row, int col)
    {
        this.row  = row;
        this.col  = col;
    }

    /**
     * 
     * @param result
     */
    public QueryResultsDataObj(Object result)
    {
        this.result   = result;
        isProcessable = false;
    }

    /**
     * 
     * @return
     */
    public Object getResult()
    {
        return result;
    }

    /**
     * 
     * @param result
     */
    public void setResult(Object result)
    {
        this.result = result;
    }

    /**
     * 
     * @return
     */
    public int getCol()
    {
        return col;
    }

    /**
     * 
     * @param col
     */
    public void setCol(int col)
    {
        this.col = col;
    }

    /**
     * 
     * @return
     */
    public int getRow()
    {
        return row;
    }

    /**
     * 
     * @param row
     */
    public void setRow(int row)
    {
        this.row = row;
    }

    /**
     * 
     * @return
     */
    public boolean isProcessable()
    {
        return isProcessable;
    }
    
    /**
     * 
     *
     */
    public void clear()
    {
        result = null;
    }
    
}
