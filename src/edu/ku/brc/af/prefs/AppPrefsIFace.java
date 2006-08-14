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

import java.util.prefs.BackingStoreException;

/**
 * This interface describes the application level preference system. We decided to have our own preference
 * system different than java.util.Preferences because we wanted to store our prefs in a single file. We also decided to
 * NOT override the current preference system by installing out own factory and implementing the absrtact class because
 * we just didn't want anything that robust.<br><br>
 * This Pref system enables user to specify a hierachy through a dot notation but it isn't enforced.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface AppPrefsIFace
{

    /**
     * Gets a string value.
     * @param name the name of the pref
     * @param defaultValue the default value
     * @return the value as a String.
     */
    public String get(final String name, final String defaultValue);

    /**
     * Sets a String value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void put(String name, String value);

    /**
     * Returns the value as a Integer.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Integer.
     */
    public Integer getInt(final String name, final Integer defaultValue);

    /**
     * Sets a Integer value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putInt(final String name, final Integer value);

    /**
     * Returns the value as a Long.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Long.
     */
    public Long getLong(final String name, final Long defaultValue);

    /**
     * Sets a Long value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putLong(final String name, final Long value);

    /**
     * Returns the value as a Boolean.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Boolean.
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue);

    /**
     * Sets a Boolean value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putBoolean(final String name, final Boolean value);

    /**
     * Returns the value as a Double.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Double.
     */
    public Double getDouble(final String name, final Double defaultValue);

    /**
     * Sets a Double value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putDouble(final String name, final Double value);

    /**
     * Returns the value as a Float.
     * @param name the name
     * @param defaultValue the default value
     * @return the value as a Float
     */
    public Float getFloat(final String name, final Float defaultValue);

    /**
     * Sets a Float value into a pref.
     * @param name the name
     * @param value the new value
     */
    public void putFloat(final String name, final Float value);

    /**
     * Removes a pref by name.
     * @param name the name
     */
    public void remove(final String name);

    /**
     * Returns true if the pref already exists.
     * @param name the name
     * @return true if the pref already exists.
     */
    public boolean exists(final String name);

    /**
     * Returns a list of all the attrs for a given node.
     * @param nodeName the name of the node
     * @return array of strings a list of all the attrs for a given node.
     */
    public String[] keys(final String nodeName);

    /**
     * Returns a list of all the children names for a given node.
     * @param nodeName the node's name
     * @return  a list of all the children names for a given node.
     */
    public String[] childrenNames(final String nodeName);

    /**
     * Return the singleton after the preferences are loaded.
     * @param dirPath the directory path to where the prefs file will be created.
     */
    public AppPrefsIFace load(final String dirPath);

    /**
     * Saves the contents to disk.
     * @throws BackingStoreException
     */
    public void flush() throws BackingStoreException;

    /**
     * Adds a change listener for a pref.
     * @param name the name
     * @param l the listener
     */
    public void addChangeListener(final String name, final AppPrefsChangeListener l);


    /**
     * Removes a change listener for a pref
     * @param name the name
     * @param l the listener
     */
    public void removeChangeListener(final String name, final AppPrefsChangeListener l);

}