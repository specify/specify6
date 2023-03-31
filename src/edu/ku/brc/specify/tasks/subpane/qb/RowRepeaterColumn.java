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
package edu.ku.brc.specify.tasks.subpane.qb;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class RowRepeaterColumn implements RowRepeater
{
    final int columnIndex;
    
    public RowRepeaterColumn(final int columnIndex)
    {
        this.columnIndex = columnIndex;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.RowRepeater#repeats(java.lang.Object[])
     */
    //@Override
    public int repeats(Object[] row)
    {
        Object val = row[columnIndex];
        if (val == null)
        {
            return 1; //Or 0??
        }
        if (val instanceof Number)
        {
            int numVal = ((Number )val).intValue();
            //if <=0 then just return 1. No erasing or backing up or whatever-
            return numVal <= 0 ? 1 : numVal;
        } 
        try {
        	int numVal = Integer.valueOf(val.toString()).intValue();
            return numVal <= 0 ? 1 : numVal;
        } catch (NumberFormatException fex) {
        		//just return 1 below
        }
        
        return 1; // Or 0?? Or blow up??
    }

}
