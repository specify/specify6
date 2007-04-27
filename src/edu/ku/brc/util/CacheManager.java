package edu.ku.brc.util;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 * @author jstewart
 * @code_status Alpha
 */
public class CacheManager
{
    private static final Logger log = Logger.getLogger(CacheManager.class);
    
    protected Vector<DataCacheIFace> registeredCaches;
    
    public CacheManager()
    {
        this.registeredCaches = new Vector<DataCacheIFace>();
    }
    
    public void registerCache(DataCacheIFace cache)
    {
        registeredCaches.add(cache);
    }
    
    public void unregisterCache(DataCacheIFace cache)
    {
        registeredCaches.remove(cache);
    }
    
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
                    log.error("Error shutting down registered cache", e);
                }
            }
        }
        
        registeredCaches.removeAllElements();
    }
    
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
