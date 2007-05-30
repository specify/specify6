package edu.ku.brc.af.core;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.Pair;

/**
 * The UsageTracker class is simply a facade.  Usage stats are stored using the {@link AppPreferences}
 * class.  The UsageTracker is really just a set of helper methods to easily increment and query usage counts
 * of named features.   
 * 
 * @author jstewart
 * @code_status Complete
 */
public class UsageTracker
{
    /**
     * Incremements the usage count of the given feature.
     * 
     * @param featureName the name of the feature for which to increment the usage count
     */
    public synchronized static void incrUsageCount(String featureName)
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        String usagePrefName = "Usage." + featureName;
        int currentUsageCount = appPrefs.getInt(usagePrefName, 0);
        appPrefs.putInt(usagePrefName, ++currentUsageCount);
    }
    
    /**
     * Returns a {@link List} of usage stats as name/value pairs.  If a stat has a count of 0,
     * it may or may not be present in the list.
     * 
     * @return a collection of all usage stats
     */
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
    
    /**
     * Clears all usage stats.
     */
    protected synchronized static void clearUsageStats()
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        Set<Object> prefNames = appPrefs.getProperties().keySet();
        Set<Object> prefNamesCopy = new HashSet<Object>();
        prefNamesCopy.addAll(prefNames);
        for (Object o: prefNamesCopy)
        {
            String prefName = (String)o;
            if (prefName.startsWith("Usage."))
            {
                appPrefs.remove(prefName);
            }
        }
    }
    
    /**
     * Gets the usage count of the given feature.  If the given feature name is not
     * present in the usage stats, 0 is returned.
     * 
     * @param featureName the feature to retrieve the count for
     * @return the usage count
     */
    public synchronized static int getUsageCount(String featureName)
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        return appPrefs.getInt(featureName, 0);
    }
    
    /**
     * Gets the installation ID that 'uniquely' identifies the running instance
     * from other installations.
     * 
     * @return the installation ID string
     */
    public synchronized static String getInstallId()
    {
        AppPreferences appPrefs = AppPreferences.getLocalPrefs();
        
        // get the first part of the install ID
        String installIdStart = appPrefs.get("InstallIdStart", null);
        if (installIdStart == null)
        {
            // create a new ID start (this is the first time the app has run)
            Random r = new Random(System.currentTimeMillis());
            UUID idStart = new UUID(r.nextLong(),r.nextLong());
            installIdStart = idStart.toString();
            appPrefs.put("InstallIdStart", installIdStart);
        }
        
        // get the last part of the install ID
        String installIdEnd = appPrefs.get("InstallIdEnd", null);
        File pluginRegFile = XMLHelper.getConfigDir("plugin_registry.xml");
        long  lastMod = pluginRegFile.lastModified();
        String lastModString = Long.toHexString(lastMod);
        
        if (installIdEnd == null || !installIdEnd.equals(lastModString))
        {
            // somebody must have copied this install to a new location
            // reset the InstallIdEnd preference
            clearUsageStats();
            appPrefs.put("InstallIdEnd", lastModString);
            installIdEnd = lastModString;
        }
        String installId = installIdStart + "--" + installIdEnd; 
        return installId;
    }
}
