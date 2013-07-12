/* Copyright (C) 2013, University of Kansas Center for Research
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

import edu.ku.brc.af.ui.forms.DataObjectGettable;

/**
 * This knows how to get a field's value from a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects,
 * and then the factory will want to create a different object per class that will be using this)
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class PrefsGettableImpl implements DataObjectGettable
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(PrefsGettableImpl.class);

    /**
     * Default constructor (needed for factory)
     */
    public PrefsGettableImpl()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        return dataObj instanceof AppPreferences ? ((AppPreferences)dataObj).get(fieldName, "") : null; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#usesDotNotation()
     */
    public boolean usesDotNotation()
    {
        return false;
    }

}
