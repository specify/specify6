/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
