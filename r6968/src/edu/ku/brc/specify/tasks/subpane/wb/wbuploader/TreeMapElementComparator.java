/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Comparator;
import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * Compares vectors of TreeMapElement
 *
 */
public class TreeMapElementComparator implements Comparator<Vector<TreeMapElement>>
{

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Vector<TreeMapElement> tm1, Vector<TreeMapElement> tm2)
    {
        if (tm1 == tm2)
        {
            return 0;
        }
        if (tm1 == null)
        {
            return -1;
        }
        if (tm2 == null)
        {
            return 1;
        }
        if (tm1.size() < tm2.size())
        {
            return -1;
        }
        if (tm1.size() > tm2.size())
        {
            return 1;
        }
        //ranks better be the same for all members so just compare first
        if (tm1.get(0).getRank() < tm2.get(0).getRank())
        {
            return -1;
        }
        if (tm1.get(0).getRank() > tm2.get(0).getRank())
        {
            return 1;
        }
        return 0;
    }

}
