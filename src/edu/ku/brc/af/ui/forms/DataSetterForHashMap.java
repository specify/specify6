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
package edu.ku.brc.af.ui.forms;

import java.util.Map;


/**
 * This knows how to set a field's value into a Hashtable or any object implementing the java.util.Map interface.<br><br>
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class DataSetterForHashMap implements DataObjectSettable
{
    protected Object[] args = new Object[1];

    /**
     * Default constructor (needed for factory)
     */
    public DataSetterForHashMap()
    {
        // do nothing
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setFieldValue(Object dataObj, String fieldName, Object data)
    {
        if (dataObj != null && data != null)
        {
            if (!(dataObj instanceof Map))
            {
                throw new RuntimeException("In DataGetterForHashMap - Object["+dataObj.getClass().getSimpleName()+
                                            "] does not implement java.util.Map");
            }
            ((Map)dataObj).put(fieldName, data);
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
