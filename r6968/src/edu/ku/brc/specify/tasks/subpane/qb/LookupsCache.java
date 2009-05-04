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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Implements a cache for previously formatted related records and previously determined tree ranks.
 * If there is much repetition of related data, the cache can really speed up results display. And even when there are
 * far more adds to the cache than finds, performance does not seem to adversely affected.
 * 
 */
public class LookupsCache
{
//    private static final Logger      log               = Logger.getLogger(LookupsCache.class);
    
    protected static final int         defaultLookupSize = 128;

    protected LinkedList<Integer>      lookupList        = null;
    protected TreeMap<Integer, Object> lookupTbl         = null;
    protected final int                lookupSize;

    // for debugging...
//    private int                        finds             = 0;
//    private int                        adds              = 0;
//    private static boolean             debugging         = false;

    /**
     * 
     */
    public LookupsCache()
    {
        this(defaultLookupSize);
    }
    
    /**
     * @param lookupSize
     */
    public LookupsCache(final int lookupSize)
    {
        this.lookupSize = lookupSize;
        lookupList = new LinkedList<Integer>();
        lookupTbl = new TreeMap<Integer, Object>();
    }
    
    /**
     * @param key
     * @return format for the key or null if key is not mapped.
     */
    public Object lookupKey(final Integer key)
    {
        if (key != null)
        {
            return lookupTbl.get(key);            
        }
        return null;
    }
    
    /**
     * @param key
     * @param value
     */
    protected void addKey(final Integer key, final Object value)
    {
        if (lookupList.size() == lookupSize)
        {
            //remove the 'oldest' lookup.
            lookupTbl.remove(lookupList.remove());
        }
//        logDaBug(++adds + ": added: " + key);
        lookupList.add(key);
        lookupTbl.put(key, value);
    }
    
//    private void logDaBug(Object bug)
//    {
//        if (debugging)
//        {
//            log.debug(bug);
//        }
//    }

}
