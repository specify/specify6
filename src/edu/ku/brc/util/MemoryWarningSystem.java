/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.util;

/**
 * http://www.javaspecialists.co.za/archive/Issue092.html
 * 
 * @author by Dr. Heinz M. Kabutz
 *
 * @code_status Alpha
 *
 * Mar 30, 2007
 *
 */
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * This memory warning system will call the listener when we
 * exceed the percentage of available memory specified.  There
 * should only be one instance of this object created, since the
 * usage threshold can only be set to one number.
 */
public class MemoryWarningSystem
{
    private final Collection<Listener> listeners = new ArrayList<Listener>();
    
    private static double thresholdPercentage = 0.6;
    private static double thresholdStep       = 0.1;
    
    public interface Listener
    {
        public void memoryUsageLow(long usedMemory, long maxMemory);
        public void memoryUsage(long usedMemory, long maxMemory);
    }

    public MemoryWarningSystem()
    {
        MemoryMXBean        mbean   = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        emitter.addNotificationListener(new NotificationListener()
        {
            public void handleNotification(Notification n, Object hb)
            {
                long   maxMemory  = tenuredGenPool.getUsage().getMax();
                long   usedMemory = tenuredGenPool.getUsage().getUsed();
                double percentageUsed = ((double) usedMemory) / maxMemory;
                if (thresholdStep < 0.9 && percentageUsed > thresholdStep)
                {
                    thresholdStep = Math.max(1.0, MemoryWarningSystem.getThresholdPercentage() + 0.2);
                    for (Listener listener : listeners)
                    {
                        listener.memoryUsageLow(usedMemory, maxMemory);
                    }
                }
                
                if (n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED))
                {
                    for (Listener listener : listeners)
                    {
                        listener.memoryUsageLow(usedMemory, maxMemory);
                    }
                }
            }
        }, null, null);
    }

    public boolean addListener(Listener listener)
    {
        return listeners.add(listener);
    }

    public boolean removeListener(Listener listener)
    {
        return listeners.remove(listener);
    }

    private static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();

    public static void setPercentageUsageThreshold(final double percentage)
    {
        if (percentage <= 0.0 || percentage > 1.0) 
        { 
            throw new IllegalArgumentException("Percentage not in range"); 
        }
        thresholdPercentage = percentage;
        long maxMemory = tenuredGenPool.getUsage().getMax();
        long warningThreshold = (long) (maxMemory * percentage);
        tenuredGenPool.setUsageThreshold(warningThreshold);
    }

    /**
     * @return the thresholdPercentage
     */
    public static double getThresholdPercentage()
    {
        return thresholdPercentage;
    }

    /**
     * @param thresholdPercentage the thresholdPercentage to set
     */
    public static void setThresholdPercentage(double thresholdPercentage)
    {
        MemoryWarningSystem.thresholdPercentage = thresholdPercentage;
    }

    /**
     * Tenured Space Pool can be determined by it being of type HEAP and by it being possible to set
     * the usage threshold.
     */
    private static MemoryPoolMXBean findTenuredGenPool()
    {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
        {
            // I don't know whether this approach is better, or whether
            // we should rather check for the pool name "Tenured Gen"?
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) { return pool; }
        }
        throw new AssertionError("Could not find tenured space");
    }
}