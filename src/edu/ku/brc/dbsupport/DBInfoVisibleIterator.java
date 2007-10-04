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
package edu.ku.brc.dbsupport;

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
        throw new RuntimeException("Not implemented.");
    }
    
}
