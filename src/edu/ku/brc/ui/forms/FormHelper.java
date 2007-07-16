/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public final class FormHelper
{
    private static final Logger log = Logger.getLogger(FormHelper.class);
    
    private static String currentUserEditStr = "";

    /**
     * Sets the "timestampModified" and the "lastEditedBy" by fields if the exist, if they don't then 
     * then it just ignores the request (no error is thrown). The lastEditedBy use the value of the string
     * set by the method currentUserEditStr.
     * @param dataObj the data object to have the fields set
     * @return true if it was able to set the at least one of the fields
     */
    public static boolean updateLastEdittedInfo(final Object dataObj)
    {
        log.debug("updateLastEdittedInfo for [" + (dataObj != null ? dataObj.getClass() : "dataObj was null") + "]");
        if (dataObj != null)
        {
            try
            {
                DataObjectSettable setter  = DataObjectSettableFactory.get(dataObj.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
                if (setter != null)
                {
                    boolean foundOne = false;
                    //PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, "timestampModified");
                    //if (descr != null)
                    //{
                    //    setter.setFieldValue(dataObj, "timestampModified", new Date());
                    //    foundOne = true;
                    //}
                    
                    PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, "lastEditedBy");
                    if (descr != null)
                    {
                        setter.setFieldValue(dataObj, "lastEditedBy", currentUserEditStr);
                        foundOne = true;
                    }
                    return foundOne;
                }
    
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
        }
        log.debug("updateLastEdittedInfo object is NULL");
        return false;
    }

    /**
      * Creates a new data object and initializes it
      * @param newDataClass class of new Object to be created and initialized
     */
    public static FormDataObjIFace createAndNewDataObj(final String newDataClassName)
    {
        try
        {
            FormDataObjIFace formDataObj = Class.forName(newDataClassName).asSubclass(FormDataObjIFace.class).newInstance();
            formDataObj.initialize();
            
            CommandDispatcher.dispatch(new CommandAction("Data", "NewObjDataCreated", formDataObj));
            
            return formDataObj;
            
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
   
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
   
        } catch (InstantiationException ex)
        {
            ex.printStackTrace();
        }
    
        return null;

    }

    /**
      * Creates a new data object and initializes it
      * @param newDataClass class of new Object to be created and initialized
     */
    public static FormDataObjIFace createAndNewDataObj(final Class newDataClass)
    {
        return createAndNewDataObj(newDataClass.getName());
    }

    /**
      * intializes the data object for searching
     * @param dataObj
     * @return true is successful, false if error
     */
    public static boolean initForSearch(final Object dataObj)
    {
        try
        {
            Method method = dataObj.getClass().getMethod("initForSearch", new Class[] {});
            method.invoke(dataObj, new Object[] {});
    
            return true;
    
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
    
        return false;
    }

    /**
     * Adds new child object to its parent's set and set the parent point in the new obj
     * @param parentDataObj the parent object
     * @param newDataObj the new object to be added to a Set
     */
    public static boolean initAndAddToParent(final Object parentDataObj, final FormDataObjIFace newDataObj)
    {
        newDataObj.initialize();
        if (parentDataObj != null)
        {
            return FormHelper.addToParent(parentDataObj, newDataObj);
        }
        return true;
    }

    /**
     * Adds new child object to its parent's set and set the parent point in the new obj
     * @param parentDataObj the parent object
     * @param newDataObj the new object to be added to a Set
     */
    public static boolean addToParent(final Object parentDataObj, final Object newDataObj)
    {
        if (parentDataObj != null)
        {
            try
            {
                String methodName = "add" + newDataObj.getClass().getSimpleName();
                
                log.debug("Invoking method["+methodName+"] on Object "+(parentDataObj != null ? parentDataObj.getClass().getSimpleName() : " parent is null"));
        
                Method method = parentDataObj.getClass().getMethod(methodName, new Class[] {newDataObj.getClass()});
                method.invoke(parentDataObj, new Object[] {newDataObj});
                log.debug("Adding ["+newDataObj+"] to parent Set["+parentDataObj+"]");
        
                return true;
        
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
        }
        return false;
    }

    /**
     * Returns the Id of the Database Object.
     * @param dbDataObj the object that MUST have a "getId" method to get its Id
     * @returns the Id 
     */
    public static Long getId(final Object dbDataObj)
    {
        try
        {
            Method method = dbDataObj.getClass().getMethod("getId", new Class[] {});
            return (Long)method.invoke(dbDataObj, new Object[] {});
    
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
    
        return null;
    }

    /**
     * Returns the string that represents the current user's username.
     * @return the string that represents the current user's username
     */
    public static String getCurrentUserEditStr()
    {
        return FormHelper.currentUserEditStr;
    }

    /**
     * Sets the string that represents the current user's username.
     * @param currentUserEditStr the username
     */
    public static void setCurrentUserEditStr(String currentUserEditStr)
    {
        FormHelper.currentUserEditStr = currentUserEditStr;
    }

    /**
     * Helper for setting a value into a data object using reflection
     * @param fieldNames the field name(s)
     * @param dataObj the data object that will get the new value
     * @param newData the new data object
     * @param getter the getter to use
     * @param setter the setter to use
     */
    public static void setFieldValue(final String fieldNames,
                                     final Object dataObj,
                                     final Object newData,
                                     final DataObjectGettable getter,
                                     final DataObjectSettable setter)
    {
        if( StringUtils.isNotEmpty(fieldNames) )
        {
            if (setter.usesDotNotation())
            {
                int inx = fieldNames.indexOf(".");
                if (inx > -1)
                {
                    String[] fileNameArray = StringUtils.split(fieldNames, '.');
                    Object data = dataObj;
                    for (int i=0;i<fileNameArray.length;i++)
                    {
                        String fieldName = fileNameArray[i];
                        if (i < fileNameArray.length-1)
                        {
                             data = getter.getFieldValue(data, fieldName);
                            if (data == null)
                            {
                                try
                                {
                                    PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
                                    Class  classObj = descr.getPropertyType();
                                    Object newObj = classObj.newInstance();
                                    log.debug("New Obj ["+newObj+"] of type ["+ classObj +"]being added to ["+dataObj+"]");
                                    if (newObj != null)
                                    {
    
                                        Method method = newObj.getClass().getMethod("initialize", new Class[] {});
                                        method.invoke(newObj, new Object[] {});
                                        setter.setFieldValue(dataObj, fieldName, newObj);
                                        data = newObj;
    
                                        log.debug("Inserting New Obj ["+newObj+"] at top of new DB ObjCache");
    
                                    }
                                } catch (NoSuchMethodException ex)
                                {
                                    ex.printStackTrace();
    
                                } catch (IllegalAccessException ex)
                                {
                                    ex.printStackTrace();
    
                                } catch (InvocationTargetException ex)
                                {
                                    ex.printStackTrace();
    
                                } catch (InstantiationException ex)
                                {
                                    ex.printStackTrace();
                                }
                            }
                        } else
                        {
                            log.info("Data Obj ["+newData+"] being added to ["+data+"]");
                            setter.setFieldValue(data, fieldName, newData);
                        }
                    }
                } else
                {
                    log.info("setFieldValue -  newData ["+newData+"] fieldNames["+fieldNames+"] set into ["+dataObj+"]");
                    setter.setFieldValue(dataObj, fieldNames, newData);
                }
            } else
            {
                log.info("setFieldValue -  newData ["+newData+"] fieldNames["+fieldNames+"] set into ["+dataObj+"]");
                setter.setFieldValue(dataObj, fieldNames, newData);
            }
        }
    }
}
