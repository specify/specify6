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
import java.util.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;

import edu.ku.brc.specify.dbsupport.*;
import edu.ku.brc.specify.datamodel.*;

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
        //System.out.println("["+fieldName+"]["+(dataObj != null ? dataObj.getClass().toString() : "N/A")+"]");
        Object value = null;
        if (dataObj != null)
        {
            try 
            {
                Iterator iter = null;
                if (dataObj instanceof Set)
                {
                    iter = ((Set)dataObj).iterator();
                } else if (dataObj instanceof org.hibernate.collection.PersistentSet)
                {
                    iter = ((org.hibernate.collection.PersistentSet)dataObj).iterator();
                }
                
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        Object obj = iter.next();
                        if (obj instanceof AttrsSettableGettable) // Not scalable (needs interface)
                        {
                            AttrsSettableGettable asg = (AttrsSettableGettable)obj;
                            if (asg.getName().equals(fieldName))
                            {
                                if (asg.getFieldType() == AttrsMgr.VARC_TYPE)
                                {
                                   return asg.getStrValue();
                                   
                                } else if (asg.getFieldType() == AttrsMgr.INT_TYPE)
                                {
                                    return asg.getIntValue();
                                }
                            }
                        } else
                        {
                            return null;
                        }
                    }                   
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
        String[] fieldsNames = StringUtils.split(fieldName, " ,");
        Object[] values = new Object[fieldsNames.length];
        int cnt = 0;
        for (String fldName : fieldsNames)
        {
            int inx = fldName.indexOf(".");
            if (inx > -1)
            {
                StringTokenizer st = new StringTokenizer(fldName, ".");
                Object data = dataObj;
                while (data != null && st.hasMoreTokens()) 
                {
                    data = getFieldValueInternal(data, st.nextToken());
                }
                values[cnt++] = data == null ? "" : data;
            } else
            {
                values[cnt++] = getFieldValueInternal(dataObj, fieldName);
            }
        }
        
        if (values.length == 1)
        {
            return values[0] == null ? "" : values[0];
        }
        return values;
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getFieldValue(final Object dataObj, final String fieldName, final String format) 
    {
        /*
        if (format != null && format.length() > 0 && fieldName.indexOf(",") > -1)
        {
            String[] fields = StringUtils.split(fieldName, ",");
            String   valStr = format;
            for (int i=0;i<fields.length;i++)
            {
                String token = Integer.toString(i);
                valStr = valStr.replace(token, fields != null ? fields.toString() : "");
            }
            return valStr;
        }
        
        Object value = getFieldValue(dataObj, fieldName);
        return value == null ? "" : value;
        */
        return null;
    }    
    

}
