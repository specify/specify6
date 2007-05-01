package edu.ku.brc.af.core;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.XMLHelper;
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
            appPrefs.put("InstallIdEnd", lastModString);
            installIdEnd = lastModString;
        }
        String installId = installIdStart + "--" + installIdEnd; 
        return installId;
    }
}
