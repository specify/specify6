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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.AttributeIFace;


/**
 * This knows how to get a field's value from a POJO.<br><br>
 * Implementation idea: We may need to cache the method objects,
 * and then the factory will want to create a different object per class that will be using this)
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class DataGetterForObj implements DataObjectGettable
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(DataGetterForObj.class);

    private boolean showErrors = true;
    
    /**
     * Default constructor (needed for factory)
     */
    public DataGetterForObj()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.DataObjectGettable#getFieldValue(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        //System.out.println("["+fieldName+"]["+(dataObj != null ? dataObj.getClass().toString() : "N/A")+"]");
        Object value = null;
        if (dataObj != null)
        {
            try
            {
                Iterator<?> iter = null;
                if (dataObj instanceof Set<?>)
                {
                    iter = ((Set<?>)dataObj).iterator();

                } else if (dataObj instanceof org.hibernate.collection.PersistentSet)
                {
                    iter = ((org.hibernate.collection.PersistentSet)dataObj).iterator();
                }

                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        Object obj = iter.next();
                        if (obj instanceof AttributeIFace) // Not scalable (needs interface)
                        {
                            AttributeIFace asg = (AttributeIFace)obj;
                            //log.debug("["+asg.getDefinition().getFieldName()+"]["+fieldName+"]");
                            if (asg.getDefinition().getFieldName().equals(fieldName))
                            {
                                if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.StringType.getType())
                                {
                                   return asg.getStrValue();

                                //} else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.MemoType.getType())
                                //{
                                //    return asg.getStrValue();

                                } else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.IntegerType.getType())
                                {
                                    return asg.getDblValue().intValue();

                                } else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.FloatType.getType())
                                {
                                    return asg.getDblValue().floatValue();

                                } else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.DoubleType.getType())
                                {
                                    return asg.getDblValue();

                                } else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.BooleanType.getType())
                                {
                                    return new Boolean(asg.getDblValue() != 0.0);
                                }
                            }
                        } else
                        {
                            return null;
                        }
                    }
                }
                //log.debug(fieldName);

                if (fieldName.startsWith("@get"))
                {
                    try
                    {
                        String methodName = fieldName.substring(1, fieldName.length()).trim();
                        Method method     = dataObj.getClass().getMethod(methodName, new Class<?>[] {});
                        if (method != null)
                        {
                            value = method.invoke(dataObj, new Object[] {});
                        }

                    } catch (NoSuchMethodException ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataGetterForObj.class, ex);

                    } catch (IllegalAccessException ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataGetterForObj.class, ex);

                    } catch (InvocationTargetException ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataGetterForObj.class, ex);
                    }

                } else
                {
                    PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
                    if (descr != null)
                    {
                        Method getter = PropertyUtils.getReadMethod(descr);

                        if (getter != null)
                        {
                            value = getter.invoke(dataObj, (Object[])null);
                        }
                    } else if (showErrors)
                    {
                        log.error("We could not find a field named["+fieldName.trim()+"] in data object ["+dataObj.getClass().toString()+"]");
                    }
                }
            } catch (Exception ex)
            {
                log.error(ex);
                if (!(ex instanceof org.hibernate.LazyInitializationException))
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataGetterForObj.class, ex);
                }
            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.DataObjectGettable#usesDotNotation()
     */
    @Override
    public boolean usesDotNotation()
    {
        return true;
    }
    
    
    /**
     * @param obj
     * @return
     */
    public String makeToString(final Object obj)
    {
        showErrors = false;
        
        StringBuilder sb    = new StringBuilder();
        Class<?>      clazz = obj.getClass();
        
        for (Field field : clazz.getDeclaredFields())
        {
            Object val = getFieldValue(obj, field.getName());
            sb.append(field.getName());
            sb.append("=");
            sb.append(val);
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
