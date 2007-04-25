package edu.ku.brc.af.core;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.util.Pair;

public class UsageTracker
{
    public synchronized static void incrUsageCount(String featureName)
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        String usagePrefName = "Usage." + featureName;
        int currentUsageCount = appPrefs.getInt(usagePrefName, 0);
        appPrefs.putInt(usagePrefName, ++currentUsageCount);
    }
    
    public synchronized static List<Pair<String,Integer>> getUsageStats()
    {
        List<Pair<String,Integer>> usageStats = new Vector<Pair<String,Integer>>();
        
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        Set<Object> prefNames = appPrefs.getProperties().keySet();
        for (Object o: prefNames)
        {
            String prefName = (String)o;
            if (prefName.startsWith("Usage."))
            {
                int count = appPrefs.getInt(prefName, 0);
                Pair<String,Integer> stat = new Pair<String, Integer>(prefName.substring(6),count);
                usageStats.add(stat);
            }
        }
        
        return usageStats;
    }
    
    public synchronized static int getUsageCount(String featureName)
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        return appPrefs.getInt(featureName, 0);
    }
}
