/* Filename:    $RCSfile: DataGetterForHashMap.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Map;

import edu.ku.brc.specify.prefs.PrefsCache;


/**
 * This knows how to get a field's value from a Hashtable or any object implementing the java.util.Map interface.<br><br>
 * Implementation idea: Doesn't support any field names with a "." in them, but will format Dates.
 *
 * @author rods
 *
 */
public class DataGetterForHashMap implements DataObjectGettable
{
    protected static SimpleDateFormat scrDateFormat = null;
    protected Object[] objectArray = new Object[1];

    /**
     * Default constructor (needed for factory)
     */
    public DataGetterForHashMap()
    {
        if (scrDateFormat == null)
        {
            scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getFieldValue(final Object dataObj, final String fieldName, final String formatName, final String format)
    {
        if (isNotEmpty(formatName))
        {
            throw new RuntimeException("DataGetterForHashMap doesn't support the formatName argument!");
        }
        
        Object value = getFieldValue(dataObj, fieldName);
        if (format == null || value == null)
        {
            return value;
        }

        if (value instanceof java.util.Date)
        {
            value = scrDateFormat.format((java.util.Date)value);

        } else if (value instanceof Calendar)
        {
            value = scrDateFormat.format(((Calendar)value).getTime());

        } else
        {
            try
            {
                objectArray[0] = value;
                Formatter formatter = new Formatter();
                formatter.format(format, (Object[])objectArray);
                value = formatter.toString();

            } catch (java.util.IllegalFormatConversionException ex)
            {
                value = value != null ? value.toString() : "";
            }
        }
        return value;
    }
}
