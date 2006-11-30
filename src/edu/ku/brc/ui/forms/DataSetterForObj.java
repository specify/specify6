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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIHelper;


/**
 * This knows how to set a field's value into a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects, 
 * and then the factory will want to create a different object per class that will be using this)
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class DataSetterForObj implements DataObjectSettable
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(DataSetterForObj.class);
    
    protected Object[] args = new Object[1];
    
    /**
     * Default constructor (needed for factory) 
     */
    public DataSetterForObj()
    {
        // do nothing
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#setFieldValue(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void setFieldValue(Object dataObj, String fieldName, Object data)
    {
        log.debug("fieldName["+fieldName+"] dataObj["+dataObj+"] data ["+data+"]");
        try
        {
            PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
            if (descr != null)
            {
                // Check to see if the class of the data we have is different than the one we are trying to set into
                // This typically happens when we have a TextField with a number and it needs to be converted from a 
                // String representation of the number to the actuall numeric type like from String to Integer or Short
                Object dataVal = data;
                if (data != null)
                {
                    Class<?> fieldClass = descr.getPropertyType();
                    if (dataVal.getClass() != fieldClass && !fieldClass.isAssignableFrom(dataVal.getClass()))
                    {
                        dataVal = UIHelper.convertDataFromString(dataVal.toString(), fieldClass);
                    }
                }
                Method setter = PropertyUtils.getWriteMethod(descr);
                if (setter != null)
                {
                    args[0] = dataVal;
                    log.debug("fieldname["+fieldName+"] dataObj["+dataObj+"] data ["+dataVal+"] ("+(dataVal != null ? dataVal.getClass().getSimpleName() : "")+")");
                    setter.invoke(dataObj, new Object[] {dataVal});
                }
            } else
            {
                log.error("We could not find a field named[" + fieldName.trim()+ "] in data object [" + dataObj.getClass().toString() + "]");
            }
        } catch (Exception ex)
        {
            log.error("Trouble setting value field named[" + (fieldName != null ? fieldName.trim() : "null")+ 
                    "] in data object [" + (dataObj != null ? dataObj.getClass().toString() : "null")+ "]");
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectSettable#usesDotNotation()
     */
    public boolean usesDotNotation()
    {
        return true;
    }
    
}
