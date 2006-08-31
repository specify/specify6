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



/**
 * Represents an entry in the cache of a single simple string pref that is always up-to-date because
 * it listens for changes.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class AppPrefsCacheEntry implements AppPrefsChangeListener
{
    protected String attrName;
    protected String value;
    protected String defValue;

    /**
     * Constructor.
     * @param attrName the fully specified attribute name
     * @param value the current value
     * @param defValue the default value
     */
    public AppPrefsCacheEntry(String attrName, String value, String defValue)
    {
        this.attrName = attrName;
        this.value    = value;
        this.defValue = defValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        value = AppPreferences.getRemote().get(evt.getKey(), defValue);
    }

    /**
     * Returns the default value.
     * @return the default value
     */
    public String getDefValue()
    {
        return defValue;
    }

    /**
     * Returns the value.
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

}
