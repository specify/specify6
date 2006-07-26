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

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import org.apache.commons.lang.ArrayUtils;

import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.UICacheManager;

/**
 * Creates and maintains a cache of preference entries that listen for changes to the preferences so they can always be up-to-date
 * <br><br>
 * ColorWrapper and SimpleDateFormat object can be registered into the cache and their mutal value will always be up to date, this saves
 * the consumer from having to go get them each time.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class PrefsCache
{
    protected static final PrefsCache instance = new PrefsCache();
    
    protected Preferences                        appPrefs = UICacheManager.getAppPrefs();
    protected Hashtable<String, PrefsCacheEntry> hash     = new Hashtable<String, PrefsCacheEntry>();
    
    
    /**
     * Protected Default Constructor
     */
    protected PrefsCache()
    {
        super();
    }
    
    
    public static PrefsCache getInstance()
    {
         return instance;
    }
    
    /**
     * @param section
     * @param pref
     * @param attrName
     * @return
     */
    protected static String makeKey(final String section, final String pref, final String attrName)
    {
        return section + "." + pref + "." + attrName;
    }
    

    /**
     * The internal non-static version for registering a string
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     * @param defValue the default value
     * @return the entry for this pref
     */
    protected PrefsCacheEntry registerInternal(final String section, final String pref, final String attrName, final String defValue)
    {
        Preferences sectionNode = instance.appPrefs.node(section);
        if (sectionNode != null)
        {
            Preferences prefNode = sectionNode.node(pref);
            if (prefNode != null)
            {
                PrefsCacheEntry prefsCacheEntry = new PrefsCacheEntry(attrName, prefNode.get(attrName, defValue), defValue);
                prefNode.addPreferenceChangeListener(prefsCacheEntry);
                hash.put(makeKey(section, pref, attrName), prefsCacheEntry);
                return prefsCacheEntry;
            }
        }
        return null;
    }
    
    /**
     * Register a simple string value
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     * @param defValue the default value
     */
    public static void register(final String section, final String pref, final String attrName, final String defValue)
    {
        getInstance().registerInternal(section, pref, attrName, defValue);
    }
    
    /**
     * Register a simple string value
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     */
    public static boolean remove(final String section, final String pref, final String attrName)
    {
        PrefsCache cache = getInstance();
        Preferences prefNode = cache.getPrefNode(section, pref);
        if (prefNode != null)
        {
            prefNode.remove(attrName);
            getInstance().hash.remove(makeKey(section, pref, attrName));
            return true;
            
        } else
        {
            throw new RuntimeException("["+section+"/"+pref+"] node was returned null!");
        }
    }
    
    /**
     * Helper method - This "should" always return a node
     * @param section the category or section of the prefs
     * @param pref the pref's name
     * @return the Preferences node to be return
     */
    protected Preferences getPrefNode(final String section, final String pref)
    {
        Preferences sectionNode = appPrefs.node(section);
        if (sectionNode != null)
        {
            return sectionNode.node(pref);
        }        
        return null;
    }
    
    /**
     * Returns a string object from the pref. NOTE: The value retrieved using this method would most
     * likely be set via the Prefs Dialog or someone else directly manipulating the 
     * preferences nodes and calling flush to set off the pref listeners.
     * 
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     * @return the string value or default value
     */
    public static String getValue(final String section, final String pref, final String attrName)
    {
        PrefsCacheEntry prefsCacheEntry = getInstance().hash.get(makeKey(section, pref, attrName));
        return prefsCacheEntry != null ? prefsCacheEntry.getValue() : "";
    }
    
    /**
     * Get the prefValue, if it is new than it sets the default into the prefs, if not it gets the value and returns it
     * @param prefNode the pref's name
     * @param attrName the actual attribute
     * @param defValue the default value
     * @return return the pref's value
     */
    protected String checkForPref(final Preferences prefNode, final String attrName, final String defValue)
    {
        String prefVal;
        try
        {
            //System.out.println("--------");
            //for (String s : prefNode.keys())
            //{
            //    System.out.println("["+s+"]");
            //}
            if (!ArrayUtils.contains(prefNode.keys(), attrName))
            {
                    prefNode.put(attrName, defValue);
                    //prefNode.flush();
                    
                prefVal = defValue;
            }  else
            {
                prefVal = prefNode.get(attrName, defValue);
            }
        } catch (BackingStoreException ex) 
        {
            prefVal = prefNode.get(attrName, defValue);
        }
        return prefVal;
    }
    
    //--------------------------------------------------------------------
    // Color Support
    //--------------------------------------------------------------------

    /**
     * Creates or gets the pref node, creates an entry and then hooks it up as a listener .
     * The current value of the color because the default.
     * @param colorWrapper the color object wrapper
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     */
    public void registerInternal(final ColorWrapper colorWrapper, 
                                 final String       section, 
                                 final String       pref, 
                                 final String       attrName)
    {
        Preferences prefNode = instance.getPrefNode(section, pref);
        if (prefNode != null)
        {
            String defValue = colorWrapper.toString();
            String prefVal  = checkForPref(prefNode, attrName, defValue);
            colorWrapper.setRGB(prefVal);
            
            ColorCacheEntry colorEntry = new ColorCacheEntry(colorWrapper, attrName, prefVal, defValue);
            
            prefNode.addPreferenceChangeListener(colorEntry);
            hash.put(makeKey(section, pref, attrName), colorEntry);
        }
    }
    
    /**
     * Creates or gets the pref node, creates an entry and then hooks it up as a listener.
     * The current value of the color because the default.
     * @param color the color object wrapper
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     */
    public static void register(final ColorWrapper color, 
                                final String       section, 
                                final String       pref, 
                                final String       attrName)
    {
        getInstance().registerInternal(color, section, pref, attrName);
    }

    /**
     * Gets a ColorWrapper Object
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     * @return the object
     */
    public static ColorWrapper getColorWrapper(final String section, 
                                               final String pref, 
                                               final String attrName)
    {
        PrefsCache cache = getInstance();
        ColorCacheEntry colorEntry = (ColorCacheEntry)cache.hash.get(makeKey(section, pref, attrName));
        if (colorEntry != null)
        {
            return colorEntry.getColorWrapper();
        } else
        {
            throw new RuntimeException("Couldn't find Date Entry ["+makeKey(section, pref, attrName)+"]");
        }
    }


    //--------------------------------------------------------------------
    // Date Format Support
    //--------------------------------------------------------------------

    /**
     * Creates or gets the pref node, creates an entry and then hooks it up as a listener.
     * The current value of the SimpleDateFormat because the default.
     * @param simpleFormat the SimpleDateFormat object 
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     */
    public void registerInternal(final SimpleDateFormat simpleFormat, 
                                 final String           section, 
                                 final String           pref, 
                                 final String           attrName)
    {
        Preferences prefNode = instance.getPrefNode(section, pref);
        if (prefNode != null)
        {
            //System.out.println("["+appPrefs+"]");
            //System.out.println("["+prefNode+"]");
            
            String defValue = simpleFormat.toPattern();
            String prefVal  = checkForPref(prefNode, attrName, defValue);
            simpleFormat.applyPattern(prefVal);
            DateFormatCacheEntry dateEntry = new DateFormatCacheEntry(simpleFormat, attrName, prefVal, defValue);
            prefNode.addPreferenceChangeListener(dateEntry);
            hash.put(makeKey(section, pref, attrName), dateEntry);
        }
    }
    
    /**
     * Creates or gets the pref node, creates an entry and then hooks it up as a listener.
     * The current value of the SimpleDateFormat because the default. 
     * @param simpleFormat the SimpleDateFormat object 
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     */
    public static void register(final SimpleDateFormat simpleFormat, 
                                final String       section, 
                                final String       pref, 
                                final String       attrName)
    {
        getInstance().registerInternal(simpleFormat, section, pref, attrName);
    }

    /**
     * Gets a SimpleDateFormat Object
     * @param section the section or category of the pref
     * @param pref the pref's name
     * @param attrName the actual attribute
     * @return the object
     */
    public static SimpleDateFormat getSimpleDateFormat(final String section, 
                                                       final String pref, 
                                                       final String attrName)
    {
        PrefsCache cache = getInstance();
        DateFormatCacheEntry dateEntry = (DateFormatCacheEntry)cache.hash.get(makeKey(section, pref, attrName));
        if (dateEntry != null)
        {
            return dateEntry.getSimpleDateFormat();
        } else
        {
            throw new RuntimeException("Couldn't find Date Entry ["+makeKey(section, pref, attrName)+"]");
        }
    }

    /**
     * @author rods
     *
     */
    public class ColorCacheEntry extends PrefsCacheEntry
    {
        protected ColorWrapper colorWrapper;
        
        public ColorCacheEntry(final ColorWrapper colorWrapper, String attrName, String value, String defValue)
        {
            super(attrName, value, defValue);
            this.colorWrapper = colorWrapper;
        }
        
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            super.preferenceChange(evt);
            if (evt.getKey().equals(this.attrName))
            {
                colorWrapper.setRGB(getValue());
            }
        }

        public ColorWrapper getColorWrapper()
        {
            return colorWrapper;
        }
    }

    /**
     * @author rods
     *
     */
    public class DateFormatCacheEntry extends PrefsCacheEntry
    {
        protected SimpleDateFormat simpleDateFormat;
        
        public DateFormatCacheEntry(final SimpleDateFormat simpleDateFormat, String attrName, String value, String defValue)
        {
            super(attrName, value, defValue);
            this.simpleDateFormat = simpleDateFormat;
        }
        
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            super.preferenceChange(evt);
            if (evt.getKey().equals(this.attrName))
            {
                simpleDateFormat.applyPattern(getValue());
            }
        }

        public SimpleDateFormat getSimpleDateFormat()
        {
            return simpleDateFormat;
        }
        
    }
    

}
