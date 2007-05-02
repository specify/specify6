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
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Apr 26, 2007
 *
 */
public class ASearchableCell
{
    int row = -1;
    int col = -1;
    boolean found = false;
    /**
     * 
     */
    public ASearchableCell(int row, int col, boolean found)
        {
        this.row =row;
        this.col = col;
        this.found = found;

    }
    /**
     * @return the col
     */
    public int getCol()
    {
        return this.col;
    }
    /**
     * @return the found
     */
    public boolean isFound()
    {
        return this.found;
    }
    /**
     * @return the row
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
