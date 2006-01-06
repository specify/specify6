/* Filename:    $RCSfile: DataGetterForObj.java,v $
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This knows how to get a field's value from a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects, 
 * and then the factory will want to create a different object per class that will be using this)
 * 
 * @author rods
 *
 */
public class DataGetterForObj implements DataObjectGettable
{
    // Static Data Members
    private static Log log = LogFactory.getLog(DataGetterForObj.class);
    
    /**
     * Default constructor (needed for factory) 
     */
    public DataGetterForObj()
    {
        
    }
    
    /**
     * Generic helper method needed for getting data
     * @param dataObj the source of the value
     * @param fieldName the fieldname of the desired value
     * @return the value of the field in the data object
     */
    public Object getFieldValueInternal(Object dataObj, String fieldName) 
    {
        Object value = null;
        if (dataObj != null)
        {
            try {
                if (dataObj instanceof Set)
                {
                    /* XXX Uncomment me when class is checked in
                    for (Iterator iter=((Set)dataObj).iterator();iter.hasNext();)
                    {
                        Object obj = iter.next();
                        if (obj instanceof PrepAttrs) // Not scalable (needs interface)
                        {
                            PrepAttrs ad = (PrepAttrs)obj;
                            if (ad.getName().equals(fieldName))
                            {
                                return ad.getValue();
                            }
                        } else
                        {
                            return null;
                        }
                    }*/
                    
                }
                PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
                if (descr != null)
                {
                    Method getter = PropertyUtils.getReadMethod(descr);
                    /*if (fieldName.indexOf(".") != -1)
                    {
                        log.error("DataGetterForObj.getFieldValueInternal - How in thew world did we get here!");
                    }*/
                    if (getter != null)
                    {
                        value = getter.invoke(dataObj, (Object[])null);
                    }
                } else 
                {
                    log.error("We could not find a field named["+fieldName.trim()+"] in data object ["+dataObj.getClass().toString()+"]");
                }
            } catch (Exception ex) 
            {
                log.error(ex);
            }
        }
        return value;    
    }   
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName) 
    {
        // XXX need to replace this code with the library that does this from the Cookbook
        
        int inx = fieldName.indexOf(".");
        if (inx > -1)
        {
            StringTokenizer st = new StringTokenizer(fieldName, ".");
            
            Object data = dataObj;
            while (data != null && st.hasMoreTokens()) {
                data = getFieldValueInternal(data, st.nextToken());
            }
            return data == null ? "" : data;
        }
        
        Object value = getFieldValueInternal(dataObj, fieldName);
        return value == null ? "" : value; 
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getFieldValue(Object dataObj, String fieldName, String format) 
    {
        
        if (format != null && format.length() > 0 && fieldName.indexOf(",") > -1)
        {
            StringTokenizer st = new StringTokenizer(fieldName, ",");
            
            Vector<Object> vals = new Vector<Object>();
            while (dataObj != null && st.hasMoreTokens()) {
                Object retVal = getFieldValue(dataObj, st.nextToken());
                if (retVal != null)
                {
                    vals.addElement(retVal);
                }
            }
            
            String valStr = format;
            for (int i=0;i<vals.size();i++)
            {
                String token = Integer.toString(i);
                Object data = vals.elementAt(i);
                valStr = valStr.replace(token, data != null ? data.toString() : "");
            }
            return valStr;
        }
        
        Object value = getFieldValue(dataObj, fieldName);
        return value == null ? "" : value; 
    }    
    

}
