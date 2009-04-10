/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.awt.Color;

import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.ColorWrapper;

/**
 * Class that implements the DataObjectSettable for preferences
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class PrefsSettableImpl implements DataObjectSettable
{

    /**
     * Constructor.
     */
    public PrefsSettableImpl()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setFieldValue(final Object dataObj, final String fieldName, final Object data)
    {
        Object newData = data;
        if (newData instanceof Color)
        {
            newData = ColorWrapper.toString((Color)newData);
        }
        //System.out.println("setFieldValue["+dataObj+"]  ["+fieldName+"]  ["+ newData+"]");
        
        if (dataObj instanceof AppPreferences)
        {
            ((AppPreferences)dataObj).put(fieldName, newData == null ? "" : newData.toString()); //$NON-NLS-1$
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#usesDotNotation()
     */
    public boolean usesDotNotation()
    {
        return false;
    }
}
