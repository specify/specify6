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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.BackingStoreException;

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
public class AppPrefsMgr implements AppPrefsIFace
{
    private   static final String NOT_INIT = "SpecifyAppPrefs have not been initialized!";
    protected static AppPrefsMgr instance = new AppPrefsMgr();

    protected Properties  properties = null;
    protected String      dirPath;
    protected Hashtable<String, List<AppPrefsChangeListener>> listeners = new Hashtable<String, List<AppPrefsChangeListener>>();
    protected boolean     isChanged = false;

    /**
     * Constructor (that does nothing).
     */
    protected AppPrefsMgr()
    {

    }

    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static AppPrefsMgr getInstance()
    {
        return instance;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#get(java.lang.String, java.lang.String)
     */
    public String get(final String name, final String defaultValue)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        String val = properties.getProperty(name);
        return val != null ? val : defaultValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#put(java.lang.String, java.lang.String)
     */
    public void put(String name, String value)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        properties.setProperty(name, value);
        isChanged = true;
        notifyListeners(new AppPrefsChangeEvent(this, name, value.toString()));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#getInt(java.lang.String, java.lang.Integer)
     */
    public Integer getInt(final String name, final Integer defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Integer.toString(defaultValue)));
        return val == null ? null : Integer.valueOf(val);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#putInt(java.lang.String, java.lang.Integer)
     */
    public void putInt(final String name, final Integer value)
    {
        put(name, value.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#getLong(java.lang.String, java.lang.Long)
     */
    public Long getLong(final String name, final Long defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Long.toString(defaultValue)));
        return val == null ? null : Long.valueOf(val);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#putLong(java.lang.String, java.lang.Long)
     */
    public void putLong(final String name, final Long value)
    {
        put(name, value.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#getBoolean(java.lang.String, java.lang.Boolean)
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Boolean.toString(defaultValue)));
        return val == null ? null : Boolean.valueOf(val);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#putBoolean(java.lang.String, java.lang.Boolean)
     */
    public void putBoolean(final String name, final Boolean value)
    {
        put(name, value.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#getDouble(java.lang.String, java.lang.Double)
     */
    public Double getDouble(final String name, final Double defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Double.toString(defaultValue)));
        return val == null ? null : Double.valueOf(val);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#putDouble(java.lang.String, java.lang.Double)
     */
    public void putDouble(final String name, final Double value)
    {
        put(name, value.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#getFloat(java.lang.String, java.lang.Float)
     */
    public Float getFloat(final String name, final Float defaultValue)
    {
        String val = get(name, (defaultValue == null ? null : Float.toString(defaultValue)));
        return val == null ? null : Float.valueOf(val);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#putFloat(java.lang.String, java.lang.Float)
     */
    public void putFloat(final String name, final Float value)
    {
        put(name, value.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#remove(java.lang.String)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#exists(java.lang.String)
     */
    public boolean exists(final String name)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        return properties.get(name) != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#keys(java.lang.String)
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
            for (Enumeration e=properties.propertyNames();e.hasMoreElements();)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#childrenNames(java.lang.String)
     */
    public String[] childrenNames(final String nodeName)
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        int len = nodeName.length();
        List<String> names = new ArrayList<String>();
        for (Enumeration e=properties.propertyNames();e.hasMoreElements();)
        {
            String name = (String)e.nextElement();
            System.out.println("["+name+"]");
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#load(java.lang.String)
     */
    public AppPrefsIFace load(final String dirPath)// throws IOException
    {
        if (dirPath == null || dirPath.length() == 0)
        {
            throw new RuntimeException("The directory path for the prefs canot be empty!");
        }

        if (properties != null)
        {
            if (!this.dirPath.equals(dirPath))
            {
                throw new RuntimeException("The SpecifyAppPrefs have already been loaded!");
            }
        }
        this.dirPath = dirPath;

        String fullName = dirPath + File.separator + "user.properties";

        properties = new Properties();
        if ((new File(fullName)).exists())
        {
            try
            {
                properties.load(new FileInputStream(fullName));

            } catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#flush()
     */
    public synchronized void flush() throws BackingStoreException
    {
        if (properties == null)
        {
            throw new RuntimeException(NOT_INIT);
        }
        if (isChanged)
        {
            try
            {
                String fullName = dirPath + File.separator + "user.properties";
                properties.store(new FileOutputStream(fullName), "User Prefs");
                isChanged = false;

                //UICacheManager.getAppPrefs().properties.store(new FileOutputStream(fullName), "User Prefs");
            } catch (IOException ex)
            {
                throw new BackingStoreException(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#addChangeListener(java.lang.String, edu.ku.brc.af.prefs.AppPrefsChangeListener)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsIFace#removeChangeListener(java.lang.String, edu.ku.brc.af.prefs.AppPrefsChangeListener)
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

    private static Timer syncTimer = new Timer(true); // Daemon Thread

    static {
        // Add periodic timer task to periodically sync cached prefs
        syncTimer.schedule(new TimerTask() {
            public void run() {
                syncPrefs();
            }
        }, SYNC_INTERVAL*1000, SYNC_INTERVAL*1000);

        // Add shutdown hook to flush cached prefs on normal termination
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
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
    private static void syncPrefs() {
        /*
         * Synchronization necessary because userRoot and systemRoot are
         * lazily initialized.
         */
        AppPrefsMgr prefs;

        synchronized(AppPrefsMgr.class)
        {
            prefs   = instance;
        }

        try {
            if (prefs != null)
            {
                prefs.flush();
            }
        } catch(BackingStoreException e) {
            //getLogger().warning("Couldn't flush user prefs: " + e);
            e.printStackTrace();
        }
    }


}
