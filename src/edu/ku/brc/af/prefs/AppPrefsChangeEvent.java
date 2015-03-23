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

import java.util.EventObject;

/**
 * Change event patterned after the PreferenceChangeEvent
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class AppPrefsChangeEvent extends EventObject
{
    protected Object source;
    protected String key;
    protected String newValue;

    /**
     * Constructor.
     * @param source the source object generating the change
     * @param key the key of the change
     * @param newValue the new value
     */
    public AppPrefsChangeEvent(final Object source, final String key, final String newValue)
    {
        super(source);

        this.key = key;
        this.newValue = newValue;
    }

    /**
     * Returns the key.
     * @return the key.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the new value.
     * @return the new value.
     */
    public String getNewValue()
    {
        return newValue;
    }

}
