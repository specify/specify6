/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.prefs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIHelper;

/**
 * A reference implementation for the preferences/properties system. These are persisted every 30 seconds or whatever the
 * value "java.util.prefs.syncInterval" is set to. They are also saved automatically at shutdown. These is no real need
 * to call flush.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AppPreferences
{
    public enum AppPrefType {local, remote, global}
    
    public static final String factoryName = "edu.ku.brc.af.prefs.AppPrefsIOIFace"; //$NON-NLS-1$
    public static final String factoryGlobalName = "edu.ku.brc.af.prefs.AppPrefsIOIFaceGlobal"; //$NON-NLS-1$

    // Static Data Members
    public final static String LOCALFILENAME  = "user.properties"; //$NON-NLS-1$
    
    private   static final String NOT_INIT = "AppPrefs have not been initialized!"; //$NON-NLS-1$
    
    protected static final Logger log                 = Logger.getLogger(AppPreferences.class);
            
    protected static AppPreferences instanceRemote    = null;
    protected static AppPreferences instanceLocal     = null;
    protected static AppPreferences instanceGlobal    = null;

    // instanceRemote Data Member
    protected Properties         properties           = null;
    protected String             dirPath;
    protected boolean            isChanged            = false;
    protected AppPrefType        prefType;
    protected String             localFileName        = null;
    
    protected String             saverClassName       = null;
    protected AppPrefsIOIFace    appPrefsIO           = null;
    protected boolean            isEnabled            = true;

    protected Hashtable<String, List<AppPrefsChangeListener>> listeners = new Hashtable<String, List<AppPrefsChangeListener>>();
    
    // Used for Synchronizing the Preferences to the Backend store
    protected static Timer         syncTimer     = null; // Daemon Thread
    protected static boolean       connectedToDB = false;
    protected static AtomicBoolean blockTimer    = new AtomicBoolean(false);

    
    /**
     * Constructor for Remote, Global and Local prefs.
     * @param prefType remote is per user in the DB, global is in the DB, local on local disk
     */
    protected AppPreferences(final AppPrefType prefType)
    {
        this.prefType = prefType;
        
        if (prefType == AppPrefType.remote)
        {
            this.appPrefsIO = createFactoryIO(factoryName);
           
        } else if (prefType == AppPrefType.global)
        {
            // Start by checking to see if we have a Remote IO impl
            
            this.appPrefsIO = createFactoryIO(factoryGlobalName);
            
        } else
        {
            this.localFileName = LOCALFILENAME;
            this.appPrefsIO    = new AppPrefsDiskIOIImpl();
            this.appPrefsIO.setAppPrefsMgr(this);
        }
    }
    
    /**
     * @param factoryClassName
     * @param factoryNm
     * @return
     */
    private AppPrefsIOIFace createFactoryIO(final String factoryNm)
    {
        this.saverClassName = System.getProperty(factoryNm, null);
        if (this.saverClassName == null)
        {
            throw new InternalError("System Property '"+factoryNm+"' must be set!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        AppPrefsIOIFace prefIO = null;
        try 
        {
            prefIO = (AppPrefsIOIFace)Class.forName(this.saverClassName).newInstance();
            prefIO.setAppPrefsMgr(this);
            
        } catch (Exception e) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppPreferences.class, e);
            InternalError error = new InternalError("Can't instantiate "+this.saverClassName +" factory for " + factoryNm); //$NON-NLS-1$ //$NON-NLS-2$
            error.initCause(e);
            throw error;
        }
        return prefIO;
    }
    
    /**
     * 
     */
    public static void setBlockTimer()
    {
        blockTimer.set(true);
    }

    /**
     * @return
     */
    public static boolean hasRemotePrefs()
    {
        return instanceRemote != null;
    }
    
    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPreferences getRemote()
    {
        if (instanceRemote == null)
        {
            instanceRemote = new AppPreferences(AppPrefType.remote);
        }
        return instanceRemote;
    }
    
    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPreferences getGlobalPrefs()
    {
        if (instanceGlobal == null)
        {
            instanceGlobal = new AppPreferences(AppPrefType.global);
        }
        return instanceGlobal;
    }
    
    /**
     * Flushes the values and then terminates the Prefs so a new one can be created.
     * Also, set 'conenctedToDB' to true.
     */
    public static void startup()
    {
        connectedToDB = true;
    }
    
    /**
     * Flushes the values and then terminates the Prefs so a new one can be created.
     */
    public static void shutdownPrefs()
    {
        if (syncTimer != null)
        {
            connectedToDB = false;
            syncTimer.cancel();
            syncTimer.purge();
            syncTimer     = null;
        }
    }
    
    private static void shutdownPref(final AppPreferences pref) throws BackingStoreException
    {
        if (pref != null)
        {
            if (connectedToDB)
            {
                pref.flush();
            }
            pref.listeners.clear();
            pref.appPrefsIO = null;
        }

    }
    
    /**
     * Flushes the values and then terminates the Prefs so a new one can be created.
     */
    public static void shutdownRemotePrefs()
    {
        // Flush and shutdown the Remote Store
        try
        {
            blockTimer.set(true);
            
            shutdownPref(instanceRemote);
            shutdownPref(instanceGlobal);
            
            instanceRemote = null;
            instanceGlobal = null;
            
            blockTimer.set(false);
            
        } catch (BackingStoreException ex)
        {
            log.error(ex); 
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppPreferences.class, ex);
        }
    }
    
    /**
     * Flushes the values and then terminates the Prefs so a new one can be created.
     */
    public static void shutdownLocalPrefs()
    {
        // Flush and shutdown the Local Store
        try
        {
            if (instanceLocal != null && !blockTimer.get())
            {
                blockTimer.set(true);
                if (instanceLocal != null)
                {
                    AppPreferences local = instanceLocal;
                    instanceLocal = null;
                    local.flush();
                    local.listeners.clear();
                    local.appPrefsIO = null;
                }
                blockTimer.set(false);
            }
            
        } catch (BackingStoreException ex)
        {
            log.error(ex); 
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppPreferences.class, ex);
        }
    }
    
    /**
     * 
     */
    public static void shutdownAllPrefs()
    {
        try
        {
            AppPreferences.getLocalPrefs().flush();
        } catch (BackingStoreException ex) {}
        
        AppPreferences.shutdownRemotePrefs();
        AppPreferences.shutdownPrefs();
    }

    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPreferences getLocalPrefs()
    {
        if (instanceLocal == null)
        {
            instanceLocal = new AppPreferences(AppPrefType.local);
        }
        
        return instanceLocal;
    }
    
    /**
     * The directory path.
     * @return directory path.
     */
    public String getDirPath()
    {
        return dirPath;
    }

    /**
     * Sets the path to where it is suppose to save them.
     * @param dirPath the path.
     */
    public void setDirPath(String dirPath)
    {
        this.dirPath = dirPath;
    }

    /**
     * Whether it has been changed.
     * @return Whether it has been changed.
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /**
     * Sets whether it has changed.
     * @param isChanged true - changed, false not
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /**
     * Whether it is remote.
     * @return Whether it is remote.
     */
    public boolean isRemote()
    {
        return prefType == AppPrefType.remote;
    }

    /**
     * The file name of the disk file.
     * @return The file name of the disk file.
     */
    public String getLocalFileName()
    {
        return localFileName;
    }

    /**
     * The Properties object.
     * @return The Properties object.
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Sets the properties file.
     * @param properties the properties file.
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
    
    /**
     * Gets a string value.
     * @param name the name of the pref
     * @param defaultValue the default value
     * @return the value as a String.
     */
    public String get(final String name, final String defaultValue)
    {
        return get(name, defaultValue, false);
    }

    /**
     * Gets a string value.
     * @param name the name of the pref
     * @param defaultValue the default value
     * @param doDefVal set the default value if it isn't there
     * @return the value as a String.
     */
    public String get(final String name, final String defaultValue, boolean doDefVal)
    {
        if (properties == null)
        {
            load();
        }
        
        String val = properties.getProperty(name);
        if (val == null && doDefVal && defaultValue != null)
        {
            put(name, defaultValue);
        }
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a String value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void put(final String name, final String value)
    {
        if (properties == null)
        {
            load();
        }
        properties.setProperty(name, value);
        isChanged = true;
        notifyListeners(new AppPrefsChangeEvent(this, name, value.toString()));
    }

    /**
     * Returns the value as a Integer.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Integer.
     */
    public Integer getInt(final String name, final Integer defaultValue)
    {
        return getInt(name, defaultValue, false);
    }
    
    /**
     * Returns the value as a Integer.
     * @param name the name
     * @param defaultValue the default value
     * @param doDefVal set the default value if it isn't there
     * @return the value as a Integer.
     */
    public Integer getInt(final String name, final Integer defaultValue, final boolean doDefVal)
    {
        String val = get(name, null);
        if (val == null && doDefVal && defaultValue != null)
        {
            putInt(name, defaultValue);
        }
        return val == null ? (defaultValue == null ? null : defaultValue) : Integer.valueOf(val);
    }
    
    /**
     * Sets a Integer value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putInt(final String name, final Integer value)
    {
        put(name, value.toString());
    }

    /**
     * Returns the value as a Long.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Long.
     */
    public Long getLong(final String name, final Long defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Long.toString(defaultValue)));
        return val == null ? null : Long.valueOf(val);
    }

    /**
     * Sets a Long value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putLong(final String name, final Long value)
    {
        put(name, value.toString());
    }

    /**
     * Gets a value as Boolean.
     * @param name the name of the pref
     * @param defaultValue the default value
     * @param doDefVal set the default value if it isn't there
     * @return the value as a String.
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue, final boolean doDefVal)
    {
        String val = get(name, null);
        if (val == null && doDefVal && defaultValue != null)
        {
            putBoolean(name, defaultValue);
        }
        return val == null ? (defaultValue == null ? null : defaultValue) : Boolean.valueOf(val);
    }


    /**
     * Returns the value as a Boolean.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Boolean.
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue)
    {
        return getBoolean(name, defaultValue, false);
    }

    /**
     * Sets a Boolean value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putBoolean(final String name, final Boolean value)
    {
        put(name, value.toString());
    }

    /**
     * Returns the value as a Double.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Double.
     */
    public Double getDouble(final String name, final Double defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Double.toString(defaultValue)));
        return val == null ? null : Double.valueOf(val);
    }

    /**
     * Sets a Double value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putDouble(final String name, final Double value)
    {
        put(name, value.toString());
    }

    /**
     * Returns the value as a Float.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Float
     */
    public Float getFloat(final String name, final Float defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Float.toString(defaultValue)));
        return val == null ? null : Float.valueOf(val);
    }

    /**
     * Sets a Float value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putFloat(final String name, final Float value)
    {
        put(name, value.toString());
    }
    
    /**
     * Gets a value as Color
     * @param name the name of the pref
     * @param defaultValue the default value
     * @param doDefVal set the default value if it isn't there
     * @return the value as a String.
     */
    public Color getColor(final String name, final Color defaultColor, final boolean doDefVal)
    {
        String colorStr = get(name, null);
        if (StringUtils.isNotEmpty(colorStr))
        {
            try
            {
                return UIHelper.parseRGB(colorStr);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppPreferences.class, ex);
                
            }
        } else
        {
            putColor(name, defaultColor);
        }
        return defaultColor;
    }

    /**
     * Gets a value as Color
     * @param name the name of the pref
     * @param defaultValue the default value
     * @return the value as a String.
     */
    public Color getColor(final String name, final Color defaultColor)
    {
        return getColor(name, defaultColor, false);
    }
    
    /**
     * @param name
     * @param color
     */
    public void putColor(final String name, final Color color)
    {
        put(name, String.format("%d, %d, %d", color.getRed(), color.getGreen(), color.getBlue())); //$NON-NLS-1$
    }

    /**
     * @param name
     * @param color
     */
    public void putColor(final String name, final String color)
    {
        put(name, color);
    }

    /**
     * Removes a pref by name.
     * @param name the name
     */
    public void remove(final String name)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        List<AppPrefsChangeListener> list = listeners.get(name);
        if (list != null)
        {
            list.clear();
            listeners.remove(name);
        }
        properties.remove(name);
        isChanged = true;
    }

    /**
     * Returns true if the pref already exists.
     * @param name the name
     * @return true if the pref already exists.
     */
    public boolean exists(final String name)
    {
        if (properties == null)
        {
            load();
            //throw new RuntimeException(NOT_INIT + appPrefsIO.getClass().getName());
        }
        return properties.get(name) != null;
    }

    /**
     * Returns a list of all the attrs for a given node.
     * @param nodeName the name of the node
     * @return array of strings a list of all the attrs for a given node.
     */
    public String[] keys(final String nodeName)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        String[] keys = null;
        if (nodeName != null && nodeName.length() > 0)
        {
            List<String> names = new ArrayList<String>();
            int len = nodeName.length();
            for (Enumeration<?> e=properties.propertyNames();e.hasMoreElements();)
            {
                String name = (String)e.nextElement();
                if (name.startsWith(nodeName))
                {
                    String nm = name.substring(len+1, name.length());
                    int inx = nm.indexOf('.');
                    if (inx == -1)
                    {
                        names.add(nm);
                    }
                }
            }
            keys = new String[names.size()];
            int inx = 0;
            for (String s : names)
            {
                keys[inx++] = s;
            }
        } else
        {
            keys = new String[0];

        }
        return keys;
    }

    /**
     * Returns a list of all the children names for a given node.
     * @param nodeName the node's name
     * @return  a list of all the children names for a given node.
     */
    public String[] childrenNames(final String nodeName)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        int len = nodeName.length();
        List<String> names = new ArrayList<String>();
        for (Enumeration<?> e=properties.propertyNames();e.hasMoreElements();)
        {
            String name = (String)e.nextElement();
            log.info("["+name+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            if (name.startsWith(nodeName))
            {
                String nm = name.substring(len+1, name.length());
                int inx = nm.indexOf('.');
                int inxLast = nm.lastIndexOf('.');
                if (inx > 0 && inx == inxLast)
                {
                    names.add(nm.substring(0, inx));
                }
            }
        }
        String[] childNames = new String[names.size()];
        int inx = 0;
        for (String s : names)
        {
            childNames[inx++] = s;
        }
        return childNames;
    }

    /**
     * Loads the preferences from either just the local file, or from the remote and local "locations" and then synchronizes them, 
     * meaning both the local and the remote will have the same values where the newer values are copied to the older one.
     * @param dirPath the directory path to where the prefs file will be created.
     * @return return the AppPreferences that was loaded.
     */
    public AppPreferences load()
    {
        if (properties == null)
        {
            if (appPrefsIO.exists())
            {
                appPrefsIO.load();
            } else
            {
                properties = new Properties();
            }
        }

        return this;
    }

    /**
     * Saves the contents to disk.
     * @throws BackingStoreException
     */
    public synchronized void flush() throws BackingStoreException
    {
        /*if (!isRemote)
        {
            System.err.println(instanceLocal+"  "+(instanceLocal == null ? 0 : Calendar.getInstance().getTime().getTime()));
            
            putLong("update_time", instanceLocal == null ? 0 : Calendar.getInstance().getTime().getTime());
        }*/
        
        // Only flush the properties if they are loaded and have changed
        if (isChanged && properties != null && appPrefsIO != null)
        {
            appPrefsIO.flush();
        }

    }

    /**
     * Adds a change listener for a pref.
     * @param name the name
     * @param l the listener
     */
    public void addChangeListener(final String name, final AppPrefsChangeListener l)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        List<AppPrefsChangeListener> list = listeners.get(name);
        if (list == null)
        {
            list = new ArrayList<AppPrefsChangeListener>();
            listeners.put(name, list);
        }
        list.add(l);
    }

    /**
     * Removes a change listener for a pref.
     * @param name the name
     * @param l the listener
     */
    public void removeChangeListener(final String name, final AppPrefsChangeListener l)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        List<AppPrefsChangeListener> list = listeners.get(name);
        if (list != null)
        {
            list.remove(l);
        }
    }

    /**
     * Tells prefs whether it is ok to access the database.
     * @param connectedToDB true - it is ok, false it isn't
     */
    public static void setConnectedToDB(boolean connectedToDB)
    {
        AppPreferences.connectedToDB = connectedToDB;
    }

    /**
     * Notifies listener of a property change.
     * @param e the change event
     */
    protected void notifyListeners(AppPrefsChangeEvent e)
    {
        List<AppPrefsChangeListener> list = listeners.get(e.getKey());
        if (list != null)
        {
            for (AppPrefsChangeListener l : list)
            {
                l.preferenceChange(e);
            }
        }
    }
    
    /**
     * Schedules a timer for flushing and saving the Prefs.
     */
    /*private static void schedulePrefSynching()
    {
        // Add periodic timer task to periodically sync cached prefs
        if (syncTimer == null)
        {
            syncTimer = new Timer(true); // Daemon Thread
            syncTimer.schedule(new TimerTask() {
                @Override
                public void run() 
                {
                    if (!blockTimer.get())
                    {
                        syncPrefs();
                    }
                }
            }, SYNC_INTERVAL*1000, SYNC_INTERVAL*1000);
        }
    }*/

    //---------------------------------------------------------------------------------------
    //-- The Code below is re-purposed from Sun's Preferences.java
    //-- Since our prefs are eccentially working like theirs we need them to flush/save automatically
    //-- and to make sure they get saved on exit
    //---------------------------------------------------------------------------------------

    /**
     * Sync interval in seconds.
     */
    /*@SuppressWarnings("unchecked") //$NON-NLS-1$
    private static final int SYNC_INTERVAL = Math.max(1,
        Integer.parseInt((String)
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return System.getProperty("java.util.prefs.syncInterval", "30"); //$NON-NLS-1$ //$NON-NLS-2$
                }
        })));

    static {
        
        schedulePrefSynching();

        // Add shutdown hook to flush cached prefs on normal termination
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        if (syncTimer != null)
                        {
                            syncTimer.cancel();
                        }
                        if (!blockTimer.get())
                        {
                            syncPrefs();
                        }
                    }
                });
                return null;
            }
        });
    }*/

    /**
     * Saves the prefs
     */
    protected static void syncPrefs() {
        /*
         * Synchronization necessary because userRoot and systemRoot are
         * lazily initialized.
         */
        AppPreferences prefsLocal;
        AppPreferences prefsRemote;

        synchronized(AppPreferences.class)
        {
            prefsLocal   = instanceLocal;
            prefsRemote  = instanceRemote;
        }

        try 
        {
            if (prefsLocal != null)
            {
                prefsLocal.flush();
            }
            
        } catch(BackingStoreException e) 
        {
            log.error("Couldn't flush the local prefs: " + e); //$NON-NLS-1$
            e.printStackTrace();
        }
        
        try 
        {
            if (connectedToDB && prefsRemote != null)
            {
                prefsRemote.flush();
            }
        } catch(BackingStoreException e) 
        {
            log.error("Couldn't flush the remote prefs: " + e); //$NON-NLS-1$
            e.printStackTrace();
        }
    }
    
    /**
     * @param enabled
     */
    public void setEnabled(final boolean enabled)
    {
        this.isEnabled = enabled;
    }
    
    /**
     * @return whether the preferences are available. (Whether they have been setup).
     */
    public boolean isAvailable()
    {
        return isEnabled && appPrefsIO != null && appPrefsIO.isAvailable();
    }

    /**
     * @return the last date the prefs were saved or null.
     */
    public Date getLastSavedDate()
    {
        if (appPrefsIO != null)
        {
            return appPrefsIO.lastSavedDate();
        }
        return null;
    }
    
    //-------------------------------------------------------------------------
    //-- AppPrefsIOIFace Interface for performing Prefs IO
    //-------------------------------------------------------------------------
    public interface AppPrefsIOIFace
    {
        /**
         * Sets the Prefs that this will act on.
         * @param appPrefsMgr the prefs
         */
        public abstract void setAppPrefsMgr(AppPreferences appPrefsMgr);
        
        /**
         * See if the prefs have been persisted yet (or ever).
         * @return true if they have been saved, false if they have not been
         */
        public abstract boolean exists();
        
        /**
         * Returns the date of the last time they were save or null if they have not.
         * @return the date of the last time they were save or null if they have not
         */
        public abstract Date lastSavedDate();
        
        /**
         * Loads the Prefs.
         */
        public abstract void load();
        
        /**
         * Flushes the Prefs to disk or database.
         */
        public abstract void flush() throws BackingStoreException;
        
        /**
         * @return whether the preferences are available. (Whether they have been setup).
         */
        public abstract boolean isAvailable();
        
    }

 
}
