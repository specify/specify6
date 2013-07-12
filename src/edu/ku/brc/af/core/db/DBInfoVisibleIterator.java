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
package edu.ku.brc.af.core.db;

import java.util.Iterator;
import java.util.List;

/**
 * Walks a list of items of base class DBInfoBase and returns only the ones with hidden set to false.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 4, 2007
 *
 */
class DBInfoVisibleIterator<T extends DBInfoBase> implements Iterator<DBInfoBase>
{
    protected List<T> list;
    
    protected int pos = 0;
    protected int nxt = -1;
    
    public DBInfoVisibleIterator(final List<T> list)
    {
        this.list = list;
    }
    
    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext()
    {
        nxt = pos;
        while (nxt < list.size())
        {
            if (!list.get(nxt).isHidden)
            {
                return true;
            }
            nxt++;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public T next()
    {
        return list.get(nxt);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new RuntimeException("Not implemented."); //$NON-NLS-1$
    }
    
}
