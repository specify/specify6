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
    private String foundValue = "";
    
    /**
     * Returns the information associated with a found value in the TableSearcher class.
     * allows user to acces the row, col and whether teh value was found
     */
    public TableSearcherCell(int row, int col, boolean found, String foundValue)
    {
        this.row = row;
        this.col = col;
        this.found = found;
        this.foundValue = foundValue;
    }
    
    /**
     * @return the col in which the value was found
     */
    public int getColumn()
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

    /**
     * @return the foundValue
     */
    public String getFoundValue()
    {
        return foundValue;
    }

    /**
     * @param foundValue the foundValue to set
     */
    public void setFoundValue(String foundValue)
    {
        this.foundValue = foundValue;
    }
}
