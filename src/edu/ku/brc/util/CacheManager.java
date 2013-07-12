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
package edu.ku.brc.util;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 * @author jstewart
 * 
 * @code_status Alpha
 */
public class CacheManager
{
    private static final Logger log = Logger.getLogger(CacheManager.class);
    
    protected Vector<DataCacheIFace> registeredCaches;
    
    /**
     * 
     */
    public CacheManager()
    {
        this.registeredCaches = new Vector<DataCacheIFace>();
    }
    
    /**
     * @param cache
     */
    public void registerCache(final DataCacheIFace cache)
    {
        registeredCaches.add(cache);
    }
    
    /**
     * @param cache
     */
    public void unregisterCache(final DataCacheIFace cache)
    {
        registeredCaches.remove(cache);
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        synchronized (registeredCaches)
        {
            for (DataCacheIFace cache: registeredCaches)
            {
                try
                {
                    cache.shutdown();
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CacheManager.class, e);
                    log.error("Error shutting down registered cache", e);
                }
            }
        }
        
        registeredCaches.removeAllElements();
    }
    
    /**
     * 
     */
    public void clearAll()
    {
        synchronized (registeredCaches)
        {
            for (DataCacheIFace cache: registeredCaches)
            {
                cache.clear();
            }
        }
    }
}
