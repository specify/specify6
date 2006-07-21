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
package edu.ku.brc.ui.forms;

import java.util.Map;


/**
 * This knows how to get a field's value from a Hashtable or any object implementing the java.util.Map interface.<br><br>
 * Implementation idea: Doesn't support any field names with a "." in them, but will format Dates.
 *
 * @author rods
 *
 */
/**
 * @author rods
 *
 */
public class DataGetterForHashMap implements DataObjectGettable
{
    protected Object[] values = new Object[2];


    /**
     * Default constructor (needed for factory)
     */
    public DataGetterForHashMap()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        if (dataObj != null)
        {
            if (!(dataObj instanceof Map))
            {
                throw new RuntimeException("In DataGetterForHashMap - Object["+dataObj.getClass().getSimpleName()+
                                            "] does not implement java.util.Map");
            }
            return ((Map)dataObj).get(fieldName);
        }
        return null;
    }

}
