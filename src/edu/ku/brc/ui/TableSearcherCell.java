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
/**
 * 
 */
package edu.ku.brc.ui;

/**
 * Object allows us to incapsulate the row, col and boolean of whether a search value was found in a table cell.
 * 
 * @author megkumin
 *
 * @code_status Complete
 *
 * Created Date: Apr 26, 2007
 *
 */
public class TableSearcherCell
{
    private int row = -1;
    private int col = -1;
    private boolean found = false;
    
    /**
     * Returns the information associated with a found value in the TableSearcher class.
     * allows user to acces the row, col and whether teh value was found
     */
    public TableSearcherCell(int row, int col, boolean found)
    {
        this.row = row;
        this.col = col;
        this.found = found;

    }
    
    /**
     * @return the col in which the value was found
     */
    public int getCol()
    {
        return this.col;
    }
    
    /**
     * @return true if a value is found
     */
    public boolean isFound()
    {
        return this.found;
    }
    
    /**
     * @return the row in which the value was found
     */
    public int getRow()
    {
        return this.row;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
    }
}
