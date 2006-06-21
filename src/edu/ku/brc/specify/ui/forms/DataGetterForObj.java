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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.AttributeIFace;


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
    private static final Logger log = Logger.getLogger(DataGetterForObj.class);


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
    public Object getFieldValue(Object dataObj, String fieldName)
    {
        //boolean debug = true;
        /*if (dataObj instanceof Locality)
        {
            System.out.println("getLocalityName ["+((Locality)dataObj).getLocalityName()+"]");
            debug = true;
        }*/


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
                        if (obj instanceof AttributeIFace) // Not scalable (needs interface)
                        {
                            AttributeIFace asg = (AttributeIFace)obj;
                            //log.debug("["+asg.getDefinition().getFieldName()+"]["+fieldName+"]");
                            if (asg.getDefinition().getFieldName().equals(fieldName))
                            {
                                if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.StringType.getType())
                                {
                                   return asg.getStrValue();

                                } else if (asg.getDefinition().getDataType() == AttributeIFace.FieldType.MemoType.getType())
                                {
                                    return asg.getStrValue();

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
                        Method method = dataObj.getClass().getMethod(methodName, new Class[] {});
                        value = method.invoke(dataObj, new Object[] {});

                    } catch (NoSuchMethodException ex)
                    {
                        ex.printStackTrace();

                    } catch (IllegalAccessException ex)
                    {
                        ex.printStackTrace();

                    } catch (InvocationTargetException ex)
                    {
                        ex.printStackTrace();
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
                    } else
                    {
                        log.error("We could not find a field named["+fieldName.trim()+"] in data object ["+dataObj.getClass().toString()+"]");
                    }
                }
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
        return value;
    }

}
