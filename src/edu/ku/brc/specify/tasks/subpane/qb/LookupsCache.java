/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Implements a cache for previously formatted many-to-one related records.
 * If there is much repetition of related data, the cache can really speed up results display. And even when there are
 * far more adds to the cache than finds, performance does not seem to adversely affected.
 * 
 */
public class LookupsCache
{
//    private static final Logger      log               = Logger.getLogger(LookupsCache.class);
    
    protected static final int         defaultLookupSize = 64;

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
