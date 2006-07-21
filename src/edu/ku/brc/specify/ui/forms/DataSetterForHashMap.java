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
package edu.ku.brc.specify.ui.forms;

import java.util.Map;


/**
 * This knows how to set a field's value into a Hashtable or any object implementing the java.util.Map interface.<br><br>
 *
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

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectSettable#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setFieldValue(Object dataObj, String fieldName, Object data)
    {
        if (dataObj != null)
        {
            if (!(dataObj instanceof Map))
            {
                throw new RuntimeException("In DataGetterForHashMap - Object["+dataObj.getClass().getSimpleName()+
                                            "] does not implement java.util.Map");
            }
            ((Map)dataObj).put(fieldName, data);
        }
    }

}
