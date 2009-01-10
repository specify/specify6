/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.af.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * The UsageTracker class is simply a facade.  Usage statistics are stored using the {@link AppPreferences}
 * class.  The UsageTracker is really just a set of helper methods to easily increment and query usage counts
 * of named features.
 * 
 * Reworked to store statistics in a username/database named local file.
 * 
 * @author jstewart
 * @author rods
 * @code_status Complete
 */
public class UsageTracker
{
    public static final String NT_CON_EX = "NT_CON_EX"; // Network Connection Exception
    public static final String SQL_EX    = "SQ_EX";     // SQL Exception
    public static final String HQL_EX    = "HQ_EX";     // Hibernate Exception
    public static final String HNDLE_EX  = "HNDLE";     // Handled Exception
    
    private static final String USAGE_PREFIX = "Usage.";
    
    private static File       usageFile  = null;
    private static Properties usageProps = null;
    
    private static Hashtable<String, String> categoriesHash = new Hashtable<String, String>();
    
    /**
     * Sets up the new statistics file.
     * @param databaseName the current database name
     * @param userName the current username
     */
    public static void setUserInfo(final String databaseName, final String userName)
    {
        if (usageFile != null)
        {
            done();
        }
        
        usageFile  = new File(UIRegistry.getAppDataDir() + File.separator + databaseName + "_" + userName +".usage");
        usageProps = new Properties();
        if (!usageFile.exists())
        {
           transferOldStats(usageProps);
           
        } else
        {
            try
            {
                usageProps.load(new FileInputStream(usageFile.getAbsoluteFile()));
                
                // RELEASE TEMP Code
                for (Object keyObj : new Vector<Object>(usageProps.keySet()))
                {
                    String key = keyObj.toString();
                    if (!key.startsWith(USAGE_PREFIX))
                    {
                        usageProps.put(USAGE_PREFIX + key, usageProps.get(key));
                        usageProps.remove(key);
                    }
                }

            } catch (IOException ex)
            {
                // ok to die silently
                ex.printStackTrace();
            }
        }
        
        if (usageProps != null)
        {
            incrUsageCount("AP.LoginedIn");
        }
    }
    
    /**
     * Transfers and removes any old statistics from the user prefs to the new location.
     * @param newProps the new statistics
     */
    private static void transferOldStats(final Properties newProps)
    {
        Properties lpProps = AppPreferences.getLocalPrefs().getProperties(); // not a copy
        for (Object keyObj : new Vector<Object>(lpProps.keySet()))
        {
            String pName = keyObj.toString();
            if (pName.startsWith(USAGE_PREFIX))
            {
                newProps.put(pName.substring(6), lpProps.get(keyObj));
                lpProps.remove(keyObj);
            }
        }
        save();
    }
    
    /**
     * 
     */
    public static void save()
    {
        try
        {
            usageProps.store(new FileOutputStream(usageFile.getAbsoluteFile()), "User Stats"); //$NON-NLS-1$
            
        } catch (IOException ex)
        {
            // ok to die silently
            ex.printStackTrace();
        }
    }
    
    /**
     * Saves the statistics to disk in the AppDataDir.
     */
    public static void done()
    {
        if (usageFile != null && usageProps != null)
        {
            save(); 
        }
        usageProps = null;
        usageFile  = null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        done();
    }

    /**
     * Increments the usage count for a network exception.
     */
    public synchronized static void incrNetworkUsageCount()
    {
        incrUsageCount(NT_CON_EX);
    }

    /**
     * Increments the usage count for a SQL exception.
     */
    public synchronized static void incrSQLUsageCount()
    {
        incrUsageCount(SQL_EX);
    }

    /**
     * Increments the usage count for a SQL exception.
     */
    public synchronized static void incrHQLUsageCount()
    {
        incrUsageCount(HQL_EX);
    }

    /**
     * Increments the usage count for a SQL exception.
     */
    public synchronized static void incrHandledUsageCount()
    {
        incrUsageCount(HNDLE_EX);
    }

    /**
     * Incremements the usage count of the given feature.
     * 
     * @param featureName the name of the feature for which to increment the usage count
     */
    public synchronized static void incrUsageCount(final String featureName)
    {
        if (usageFile == null || usageProps == null)
        {
            AppPreferences appPrefs          = AppPreferences.getLocalPrefs();
            String         usagePrefName     = USAGE_PREFIX + featureName; //$NON-NLS-1$
            int            currentUsageCount = appPrefs.getInt(usagePrefName, 0);
            appPrefs.putInt(usagePrefName, ++currentUsageCount);
            
        } else
        {
            String currentUsageCountStr = usageProps.getProperty(featureName, null);
            int    currentUsageCount    = 0;
            if (currentUsageCountStr != null)
            {
                currentUsageCount = Integer.parseInt(currentUsageCountStr);
            }
            currentUsageCount++;
            usageProps.put(USAGE_PREFIX + featureName, Integer.toString(currentUsageCount));
        }
    }
    
    /**
     * Returns a {@link List} of usage statistics as name/value pairs.  If a stat has a count of 0,
     * it may or may not be present in the list.
     * 
     * @return a collection of all usage statistics
     */
    /*public synchronized static List<Pair<String,Integer>> getUsageStatsFromLocalPrefs()
    {
        List<Pair<String,Integer>> usageStats = new Vector<Pair<String,Integer>>();
        
        AppPreferences appPrefs  = AppPreferences.getLocalPrefs();
        Set<Object>    prefNames = appPrefs.getProperties().keySet();
        for (Object keyObj : prefNames)
        {
            String prefName = keyObj.toString();
            if (prefName.startsWith(USAGE_PREFIX)) //$NON-NLS-1$
            {
                int count = appPrefs.getInt(prefName, 0);
                Pair<String,Integer> stat = new Pair<String, Integer>(prefName, count);
                usageStats.add(stat);
            }
        }
        
        return usageStats;
    }*/
    
    /**
     * Returns a {@link List} of usage statistics as name/value pairs.  If a statistic has a count of 0,
     * it may or may not be present in the list.
     * 
     * @return a collection of all usage statistics
     */
    public synchronized static List<Pair<String,Integer>> getUsageStats()
    {
        List<Pair<String,Integer>> usageStats = new Vector<Pair<String,Integer>>();
        
        for (Object keyObj : usageProps.keySet())
        {
            String prefName = keyObj.toString();
            String valStr;
            
            if (prefName.equals(USAGE_PREFIX+"RunCount"))
            {
                valStr = AppPreferences.getLocalPrefs().get(prefName, "");
            } else
            {
                valStr = usageProps.getProperty(prefName);
            }
            if (!valStr.isEmpty() && StringUtils.isNumeric(valStr))
            {
                int count = Integer.parseInt(valStr);
                Pair<String,Integer> stat = new Pair<String, Integer>(prefName, count);
                usageStats.add(stat);
            }
        }
        
        return usageStats;
    }
    
    /**
     * Clears all usage statistics.
     */
    protected synchronized static void clearUsageStats()
    {
        AppPreferences appPrefs      = AppPreferences.getLocalPrefs();
        Set<Object>    prefNames     = appPrefs.getProperties().keySet();
        Set<Object>    prefNamesCopy = new HashSet<Object>();
        prefNamesCopy.addAll(prefNames);
        for (Object o: prefNamesCopy)
        {
            String prefName = (String)o;
            if (prefName.startsWith(USAGE_PREFIX)) //$NON-NLS-1$
            {
                appPrefs.remove(prefName);
            }
        }
        
        if (usageProps != null)
        {
            usageProps.clear();
        }
    }
    
    /**
     * Gets the usage count of the given feature.  If the given feature name is not
     * present in the usage statistics, 0 is returned.
     * 
     * @param featureName the feature to retrieve the count for
     * @return the usage count
     */
    public synchronized static int getUsageCount(final String featureName)
    {
        if (featureName.startsWith(USAGE_PREFIX))
        {
            AppPreferences appPrefs = AppPreferences.getLocalPrefs();
            return appPrefs.getInt(featureName, 0);
        }
        
        String valStr = usageProps.getProperty(featureName);
        if (StringUtils.isNumeric(valStr))
        {
            return Integer.parseInt(valStr);
        }
        return 0;
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
        String installIdStart = appPrefs.get("InstallIdStart", null); //$NON-NLS-1$
        if (installIdStart == null)
        {
            // create a new ID start (this is the first time the app has run)
            Random r = new Random(System.currentTimeMillis());
            UUID idStart = new UUID(r.nextLong(),r.nextLong());
            installIdStart = idStart.toString();
            appPrefs.put("InstallIdStart", installIdStart); //$NON-NLS-1$
        }
        
        // get the last part of the install ID
        String installIdEnd  = appPrefs.get("InstallIdEnd", null); //$NON-NLS-1$
        File   pluginRegFile = XMLHelper.getConfigDir("plugin_registry.xml"); //$NON-NLS-1$
        long   lastMod       = pluginRegFile.lastModified();
        String lastModString = Long.toHexString(lastMod);
        
        if (installIdEnd == null || !installIdEnd.equals(lastModString))
        {
            // somebody must have copied this install to a new storage
            // reset the InstallIdEnd preference
            clearUsageStats();
            appPrefs.put("InstallIdEnd", lastModString); //$NON-NLS-1$
            installIdEnd = lastModString;
        }
        String installId = installIdStart + "--" + installIdEnd;  //$NON-NLS-1$
        return installId;
    }
    
    /**
     * @param cat
     * @param desc
     */
    public static void addCategory(final String cat, final String desc)
    {
        if (StringUtils.isNotEmpty(cat))
        {
            categoriesHash.put(cat, desc);
        }
    }

    /**
     * @return the categoriesHash
     */
    public static Hashtable<String, String> getCategoriesHash()
    {
        return categoriesHash;
    }
}
