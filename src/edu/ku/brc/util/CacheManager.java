/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
