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
package edu.ku.brc.af.prefs;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;

import org.apache.log4j.Logger;

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
    public static final String factoryName = "edu.ku.brc.af.prefs.AppPrefsIOIFace";

    // Static Data Memebers
    protected final static String LOCALFILENAME  = "user.properties";
    
    private   static final String NOT_INIT = "AppPrefs have not been initialized!";
    
    protected static final Logger log                 = Logger.getLogger(AppPreferences.class);
            
    protected static AppPreferences instanceRemote    = null;
    protected static AppPreferences instanceLocal     = null;

    // instanceRemote Data Memeber
    protected Properties         properties           = null;
    protected String             dirPath;
    protected boolean            isChanged            = false;
    protected boolean            isRemote;
    protected String             localFileName        = null;
    
    protected String             remoteSaverClassName = null;
    protected AppPrefsIOIFace    appPrefsIO           = null;

    protected Hashtable<String, List<AppPrefsChangeListener>> listeners = new Hashtable<String, List<AppPrefsChangeListener>>();
    
    /**
     * Constructor for Remote and Local prefs.
     * @param isRemote true means the prefs are stored in the database, false on local disk
     */
    protected AppPreferences(final boolean isRemote)
    {
        
        if (isRemote)
        {
            // Start by checking to see if we have a Remote IO impl
            this.remoteSaverClassName = System.getProperty(factoryName, null);
            if (remoteSaverClassName == null)
            {
                throw new InternalError("System Property '"+factoryName+"' must be set!");
                
            }
            // else
            this.isRemote = true;
            
            try 
            {
                appPrefsIO = (AppPrefsIOIFace)Class.forName(remoteSaverClassName).newInstance();
                appPrefsIO.setAppPrefsMgr(this);
                
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate "+factoryName+" factory " + remoteSaverClassName);
                error.initCause(e);
                throw error;
            }
           
        } else
        {
            this.isRemote      = false;
            this.localFileName = LOCALFILENAME;
            this.appPrefsIO    = new AppPrefsDiskIOIImpl();
            this.appPrefsIO.setAppPrefsMgr(this);
        }
    }

    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPreferences getRemote()
    {
        //log.debug("** Creating Remote Prefs.");
        if (instanceRemote == null)
        {
            instanceRemote = new AppPreferences(true);
        }
        return instanceRemote;
    }
    
    /**
     * Flushes the values and then terminates the Prefs so a new one can be created.
     */
    public static void shutdownRemotePrefs()
    {
        // Flush and shutdown the Remote Store
        try
        {
            if (instanceRemote != null)
            {
                instanceRemote.flush();
                instanceRemote.listeners.clear();
                instanceRemote.appPrefsIO = null;
                instanceRemote = null;
            }
        } catch (BackingStoreException ex)
        {
           log.error(ex); 
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
            if (instanceLocal != null)
            {
                instanceLocal.flush();
                instanceLocal.listeners.clear();
                instanceLocal.appPrefsIO = null;
                instanceLocal = null;
            }
        } catch (BackingStoreException ex)
        {
           log.error(ex); 
        }
    }

    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPreferences getLocalPrefs()
    {
        if (instanceLocal == null)
        {
            instanceLocal = new AppPreferences(false);
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
        return isRemote;
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
     * Sest the properties file.
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
        if (properties == null)
        {
            load();
        }
        String val = properties.getProperty(name);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a String value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void put(String name, String value)
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
        String val = get(name, (defaultValue == null ? null : Integer.toString(defaultValue)));
        return val == null ? null : Integer.valueOf(val);
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
     * Returns the value as a Boolean.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Boolean.
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Boolean.toString(defaultValue)));
        return val == null ? null : Boolean.valueOf(val);
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
            log.info("["+name+"]");
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
     * Loads the preferences fromeither just the local file, or from the remote and local "locations" and then synchronizes them, 
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

    //---------------------------------------------------------------------------------------
    //-- The Code below is re-purposed from Sun's Preferences.java
    //-- Since our prefs are eccentially working like theirs we need them to flush/save automatically
    //-- and to make sure they get saved on exit
    //---------------------------------------------------------------------------------------

    /**
     * Sync interval in seconds.
     */
    @SuppressWarnings("unchecked")
    private static final int SYNC_INTERVAL = Math.max(1,
        Integer.parseInt((String)
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return System.getProperty("java.util.prefs.syncInterval", "30");
                }
        })));

    protected static Timer syncTimer = new Timer(true); // Daemon Thread
    protected static boolean connectedToDB = false;
    static {
        // Add periodic timer task to periodically sync cached prefs
        syncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                syncPrefs();
            }
        }, SYNC_INTERVAL*1000, SYNC_INTERVAL*1000);

        // Add shutdown hook to flush cached prefs on normal termination
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        syncTimer.cancel();
                        syncPrefs();
                    }
                });
                return null;
            }
        });
    }

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
            log.error("Couldn't flush the local prefs: " + e);
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
            log.error("Couldn't flush the remote prefs: " + e);
            e.printStackTrace();
        }
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
        public void setAppPrefsMgr(AppPreferences appPrefsMgr);
        
        /**
         * See if the prefs have been persisted yet (or ever).
         * @return true if they have been saved, false if they have not been
         */
        public boolean exists();
        
        /**
         * Returns the date of the last time they were save or null if they have not.
         * @return the date of the last time they were save or null if they have not
         */
        public Date lastSavedDate();
        
        /**
         * Loads the Prefs.
         */
        public void load();
        
        /**
         * Flushes the Prefs to disk or database.
         */
        public void flush() throws BackingStoreException;
        
    }

 
}
