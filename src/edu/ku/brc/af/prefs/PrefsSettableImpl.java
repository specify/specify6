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

import java.awt.Color;

import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.forms.DataObjectSettable;

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
        System.out.println("setFieldValue["+dataObj+"]  ["+fieldName+"]  ["+ newData+"]");
        AppPreferences.getRemote().put(fieldName, newData == null ? "" : newData.toString());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#usesDotNotation()
     */
    public boolean usesDotNotation()
    {
        return false;
    }
}
