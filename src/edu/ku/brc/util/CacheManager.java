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
    
    protected Vector<DataCache> registeredCaches;
    
    public CacheManager()
    {
        this.registeredCaches = new Vector<DataCache>();
    }
    
    public void registerCache(DataCache cache)
    {
        registeredCaches.add(cache);
    }
    
    public void unregisterCache(DataCache cache)
    {
        registeredCaches.remove(cache);
    }
    
    public void shutdown()
    {
        synchronized (registeredCaches)
        {
            for (DataCache cache: registeredCaches)
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
            for (DataCache cache: registeredCaches)
            {
                cache.clear();
            }
        }
    }
}
