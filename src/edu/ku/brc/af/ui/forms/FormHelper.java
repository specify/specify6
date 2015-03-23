/* Copyright (C) 2015, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace.FRAME_TYPE;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * XXX This needs to be moved into a Specify package or change the reference to Agent.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 */
public final class FormHelper
{
    private static final Logger log = Logger.getLogger(FormHelper.class);
    
    private static String currentUserEditStr = "";
    
    public static final String DATA_OBJ_SETTER = "edu.ku.brc.af.ui.forms.DataSetterForObj";
    public static final String DATA_OBJ_GETTER = "edu.ku.brc.af.ui.forms.DataGetterForObj";

    /**
     * XXX This needs to be moved! This references the specify packge
     * 
     * Sets the "timestampModified" and the "lastEditedBy" by fields if the exist, if they don't then 
     * then it just ignores the request (no error is thrown). The lastEditedBy use the value of the string
     * set by the method currentUserEditStr.
     * @param dataObj the data object to have the fields set
     * @return true if it was able to set the at least one of the fields
     */
    public static boolean updateLastEdittedInfo(final Object dataObj)
    {
        return updateLastEdittedInfo(dataObj, false);
    }
    
    /**
     * XXX This needs to be moved! This references the specify packge
     * 
     * Sets the "timestampModified" and the "lastEditedBy" by fields if the exist, if they don't then 
     * then it just ignores the request (no error is thrown). The lastEditedBy use the value of the string
     * set by the method currentUserEditStr.
     * @param dataObj the data object to have the fields set
     * @param doCreatedTime indicates it should set the created time also
     * @return true if it was able to set the at least one of the fields
     */
    public static boolean updateLastEdittedInfo(final Object dataObj, final boolean doCreatedTime)
    {
        log.debug("updateLastEdittedInfo for [" + (dataObj != null ? dataObj.getClass() : "dataObj was null") + "]");
        if (dataObj != null)
        {
            if (dataObj instanceof Collection<?>)
            {
                boolean retVal = false;
                for (Object o: (Collection<?>)dataObj)
                {
                    if (updateLastEdittedInfo(o))
                    {
                        retVal = true;
                    }
                }
                return retVal;
            }
            
            try
            {
                DataObjectSettable setter  = DataObjectSettableFactory.get(dataObj.getClass().getName(), DATA_OBJ_SETTER);
                if (setter != null)
                {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    boolean foundOne = false;
                    PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, "timestampModified");
                    if (descr != null)
                    {
                        setter.setFieldValue(dataObj, "timestampModified", timestamp);
                        foundOne = true;
                    }
                    
                    if (doCreatedTime)
                    {
                        descr = PropertyUtils.getPropertyDescriptor(dataObj, "timestampCreated");
                        if (descr != null)
                        {
                            setter.setFieldValue(dataObj, "timestampCreated", timestamp);
                            foundOne = true;
                        }
                    }
                    
                    descr = PropertyUtils.getPropertyDescriptor(dataObj, "modifiedByAgent");
                    if (descr != null)
                    {
                        setter.setFieldValue(dataObj, "modifiedByAgent", Agent.getUserAgent());
                        foundOne = true;
                    }
                    return foundOne;
                }
    
            } catch (NoSuchMethodException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
                ex.printStackTrace();
    
            } catch (IllegalAccessException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
                ex.printStackTrace();
    
            } catch (InvocationTargetException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
                ex.printStackTrace();
            } 
        }
        log.debug("updateLastEdittedInfo object is NULL");
        return false;
    }

    /**
      * Creates a new data object and initializes it.
      * @param newDataClass class of new Object to be created and initialized
      */
    public static FormDataObjIFace createAndNewDataObj(final Class<?> newDataClass)
    {
        return createAndNewDataObj(newDataClass, null);
    }
    
    /**
     * Creates a new data object and initializes it.
     * @param newDataClass class of new Object to be created and initialized
     * @param busRules the business rules from the view
     */
    public static FormDataObjIFace createAndNewDataObj(final Class<?> newDataClass,
                                                       final BusinessRulesIFace busRules)
    {
        return createAndNewDataObj(newDataClass, null, busRules);
    }


    /**
      * Creates a new data object and initializes it.
      * @param newDataClass class of new Object to be created and initialized
      * @param overrideAddKids whether to override the business rules as to whether to add children
      * @param busRules the business rules from the view
     */
    public static FormDataObjIFace createAndNewDataObj(final Class<?> newDataClass, 
                                                       final Boolean overrideAddKids,
                                                       final BusinessRulesIFace busRules)
    {
        try
        {
            FormDataObjIFace formDataObj = (FormDataObjIFace)newDataClass.newInstance();
            formDataObj.initialize();
            BusinessRulesIFace br = busRules != null ? busRules : DBTableIdMgr.getInstance().getBusinessRule(newDataClass);
            if (((overrideAddKids != null && overrideAddKids) || overrideAddKids == null) && br != null)
            {
                br.addChildrenToNewDataObjects(formDataObj);
            }
            CommandDispatcher.dispatch(new CommandAction("Data", "NewObjDataCreated", formDataObj));
            
            return formDataObj;
            
        } catch (IllegalAccessException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
            ex.printStackTrace();
   
        } catch (InstantiationException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new data object and initializes it.
     * @param newDataClass class of new Object to be created and initialized
    */
   public static FormDataObjIFace createAndNewDataObj(final String newDataClassName)
   {
       return createAndNewDataObj(newDataClassName, null);
   }
   
    /**
     * Creates a new data object and initializes it.
     * @param newDataClass class of new Object to be created and initialized
     * @param busRules the business rules from the view
     */
    public static FormDataObjIFace createAndNewDataObj(final String newDataClassName, final BusinessRulesIFace busRules)
    {
        return createAndNewDataObj(newDataClassName, null, busRules);
    }
  
   /**
     * Creates a new data object and initializes it.
     * @param newDataClass class of new Object to be created and initialized
     * @param overrideAddKids whether to override the business rules as to whether to add children
     * @param busRules the business rules from the view
    */
   public static FormDataObjIFace createAndNewDataObj(final String newDataClassName, 
                                                      final Boolean overrideAddKids,
                                                      final BusinessRulesIFace busRules)
   {
        try
        {
            return createAndNewDataObj(Class.forName(newDataClassName).asSubclass(FormDataObjIFace.class), overrideAddKids, busRules);
            
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
        }
    
        return null;

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
            Method method = dataObj.getClass().getMethod("initForSearch", new Class<?>[] {});
            method.invoke(dataObj, new Object[] {});
    
            return true;
    
        } catch (NoSuchMethodException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
        } catch (InvocationTargetException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
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
                
                log.debug("Invoking method["+methodName+"] on Object "+(parentDataObj.getClass().getSimpleName()));
        
                Method method = parentDataObj.getClass().getMethod(methodName, new Class<?>[] {newDataObj.getClass()});
                method.invoke(parentDataObj, new Object[] {newDataObj});
                
                log.debug("Adding ["+newDataObj+"] to parent Set["+parentDataObj+"]");
                
                return true;
        
            } catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
        
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
        
            } catch (InvocationTargetException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
            }
        } else
        {
            log.error("parentDataObj was null");
        }
        return false;
    }

    /**
     * Returns the Id of the Database Object.
     * @param dbDataObj the object that MUST have a "getId" method to get its Id
     * @returns the Id 
     */
    public static Integer getId(final Object dbDataObj)
    {
        try
        {
            Method method = dbDataObj.getClass().getMethod("getId", new Class<?>[] {});
            return (Integer)method.invoke(dbDataObj, new Object[] {});
    
        } catch (NoSuchMethodException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
        } catch (InvocationTargetException ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
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
                                    //log.debug("fieldName ["+fieldName+"]");
                                    PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, fieldName.trim());
                                    Class<?>  classObj = descr.getPropertyType();
                                    Object newObj = classObj.newInstance();
                                    //log.debug("New Obj ["+newObj+"] of type ["+ classObj +"]being added to ["+dataObj+"]");
                                    if (newObj != null)
                                    {
    
                                        Method method = newObj.getClass().getMethod("initialize", new Class<?>[] {});
                                        method.invoke(newObj, new Object[] {});
                                        setter.setFieldValue(dataObj, fieldName, newObj);
                                        data = newObj;
    
                                        //log.debug("Inserting New Obj ["+newObj+"] at top of new DB ObjCache");
    
                                    }
                                } catch (NoSuchMethodException ex)
                                {
                                    ex.printStackTrace();
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
                                } catch (IllegalAccessException ex)
                                {
                                    ex.printStackTrace();
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
                                } catch (InvocationTargetException ex)
                                {
                                    ex.printStackTrace();
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
    
                                } catch (InstantiationException ex)
                                {
                                    ex.printStackTrace();
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormHelper.class, ex);
                                }
                            }
                        } else
                        {
                            //log.info("Data Obj ["+newData+"] being added to ["+data+"]");
                            setter.setFieldValue(data, fieldName, newData);
                        }
                    }
                } else
                {
                    //log.info("setFieldValue -  newData ["+newData+"] fieldNames["+fieldNames+"] set into ["+dataObj+"]");
                    setter.setFieldValue(dataObj, fieldNames, newData);
                }
            } else
            {
                //log.info("setFieldValue -  newData ["+newData+"] fieldNames["+fieldNames+"] set into ["+dataObj+"]");
                setter.setFieldValue(dataObj, fieldNames, newData);
            }
        }
    }
    
    /**
     * Helper for setting a value on an object.
     * @param dataObj the parent data object
     * @param fieldName the name of the field to get a value of
     * @return the value of the field
     */
    public static Object getValue(final FormDataObjIFace dataObj, final String fieldName)
    {
        if (dataObj != null)
        {
            DataObjectGettable getter = DataObjectGettableFactory.get(dataObj.getDataClass().getName(), FormHelper.DATA_OBJ_GETTER);
            if (getter != null)
            {
                return getter.getFieldValue(dataObj, fieldName);
            }
            throw new RuntimeException("Could get a getter for FormDataObjIFace ["+dataObj.getDataClass().getName()+"]");
        }
        return null;
    }
    
    /**
     * Helper for setting a value.
     * @param dataObj the data object to have a field set
     * @param fieldName the field to be set
     * @param value the value to be set
     */
    public static void setValue(final FormDataObjIFace dataObj, final String fieldName, final Object value)
    {
        if (dataObj != null)
        {
            DataObjectSettable setter = DataObjectSettableFactory.get(dataObj.getDataClass().getName(), FormHelper.DATA_OBJ_SETTER);
            if (setter != null)
            {
                setter.setFieldValue(dataObj, fieldName, value);
                
            } else
            {
                throw new RuntimeException("Could get a setter for FormDataObjIFace ["+dataObj.getDataClass().getName()+"]");
            }
        }
    }
    
    /**
     * This helper method is used for when a field name is using 'dot' notation i.e. agent.lastName. It will 
     * walk the DBTableInfo schema and find the DBTableChildIFace object for the field or relationship
     * at the end of the path.
     * @param fieldName the full path field name
     * @param tblInfo the current table (the table of the first item in the path)
     * @return the DBTableChildIFace object or null if there was an error in the path.
     */
    public static DBTableChildIFace getChildInfoFromPath(final String fieldName, final DBTableInfo tblInfo)
    {
        String[]    names      = StringUtils.split(fieldName, '.');
        DBTableInfo curTblInfo = tblInfo;
        int         cnt        = 0;
        for (String name : names)
        {
            if (cnt == 0 && name.equals(tblInfo.getName()))
            {
                cnt++;
                continue;
            }
            DBTableChildIFace item = curTblInfo.getItemByName(name);
            if (item == null)
            {
                return null;
            }
            
            if (cnt == names.length-1)
            {
                return item;
                
            } else if (item instanceof DBRelationshipInfo)
            {
                DBRelationshipInfo rel = (DBRelationshipInfo)item;
                DBTableInfo nxtTbl = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
                if (nxtTbl != null)
                {
                    curTblInfo = nxtTbl;
                } else
                {
                    return null;
                }
            } else
            {
                return null;
            }
            cnt++;
        }
        return null;
    }

    /**
     * Creates a dialog for editing or viewing a data object.
     * @param altView the current AaltView
     * @param mainComp the mainComp that this is being launched from
     * @param dataObj the data object for the dialog (cannot be NULL)
     * @param isEditMode whether it is in edit mode
     * @param isNewObject whether it is a new object
     * @return the dialog
     */
    public static  ViewBasedDisplayIFace createDataObjectDialog(final JComponent       mainComp, 
                                                                final FormDataObjIFace dataObj, 
                                                                final boolean          isEditMode,
                                                                final boolean          isNewObject)
    {
        DBTableInfo setTI = DBTableIdMgr.getInstance().getByClassName(dataObj.getClass().getName());
        String defFormName = setTI.getEditObjDialog();
    
        if (StringUtils.isNotEmpty(defFormName))
        {
            int     opts = (isNewObject ? MultiView.IS_NEW_OBJECT : MultiView.NO_OPTIONS) | MultiView.HIDE_SAVE_BTN;
            opts &= ~MultiView.RESULTSET_CONTROLLER; // Clear Bit first
            String  title   = (isNewObject && isEditMode) ? getResourceString("EDIT") : dataObj.getIdentityTitle();
            ViewBasedDisplayIFace dialog = getViewbasedFactory().createDisplay(UIHelper.getWindow(mainComp),
                                                                        defFormName,
                                                                        title,
                                                                        getResourceString(isEditMode ? "ACCEPT" : "CLOSE"),
                                                                        isEditMode,
                                                                        opts,
                                                                        null,
                                                                        FRAME_TYPE.DIALOG);
            return dialog;
        }
        // else
        log.error("The Default Form Name is empty for Object type ["+dataObj.getClass().getName()+"] (Check the 'newobjdlg' value in the 'display' node in the file specify_tableid_listing.xml (and specify_datamodel.xml))");
        
        return null;
    }
    
    /**
     * Retrieves a string for restricted values.
     * @param dataObj a data object that implements FormDataObjIFace
     * @return a localized string "(Resticted)" or null
     */
    public static String checkForRestrictedValue(final Object dataObj)
    {
        if (AppContextMgr.isSecurityOn() && dataObj != null && dataObj instanceof FormDataObjIFace)
        {
            return checkForRestrictedValue(DBTableIdMgr.getInstance().getByShortClassName(dataObj.getClass().getSimpleName()));
        }
        return null;
    }
    
    /**
     * Retrieves a string for restricted values.
     * @param tableId the ID of the table info
     * @return a localized string "(Restricted)" or null
     */
    public static String checkForRestrictedValue(final int tableId)
    {    
        if (AppContextMgr.isSecurityOn())
        {
            return checkForRestrictedValue(DBTableIdMgr.getInstance().getInfoById(tableId));
        }
        return null;
    }
    
    /**
     * Checks to see if a DBTableInfo is restricted or not and returns the "Restricted" sring or null.
     * @param tblInfo the table info
     * @return a localized string "(Resticted)" or null
     */
    public static String checkForRestrictedValue(final DBTableInfo tblInfo)
    {    
        if (tblInfo != null)
        {
            PermissionSettings perm = tblInfo.getPermissions();
            if (perm != null)
            {
                if (!perm.canView())
                {
                    return UIRegistry.getResourceString("RESTRICTED");
                }
            }
        }
        return null;
    }
    
    
    public static void dumpDataObj(final DataProviderSessionIFace session, final Object dataObj, final Hashtable<Class<?>, Boolean> clsHash, final int level)
    {
        if (dataObj == null || clsHash.get(dataObj.getClass()) != null) return;
        
        String clsName = dataObj.getClass().toString();
        if (clsName.indexOf("CGLIB") > -1) return;
        
        
        StringBuilder indent = new StringBuilder();
        for (int i=0;i<level;i++) indent.append("  ");
        
        for (Method method : dataObj.getClass().getMethods())
        {
            String methodName = method.getName();
            if (!methodName.startsWith("get") || method.getModifiers() == 9)
            {
                continue;
            }
            
            String fieldName = methodName.substring(3,4).toLowerCase() + methodName.substring(4, methodName.length());
            Object kidData   = null;
            try
            {
                kidData = getValue((FormDataObjIFace)dataObj, fieldName);
                
            } catch (Exception ex)
            {
                System.out.println(indent+fieldName+" = <no data>");
            }
            if (kidData != null)
            {
                if (kidData instanceof Set<?>)
                {
                    for (Object obj : ((Set<?>)kidData))
                    {
                        System.out.println(indent+fieldName+" = "+obj);
                        if (obj instanceof FormDataObjIFace)
                        {
                            dumpDataObj(session, obj, clsHash, level+1);
                        }
                    }
                } else
                {
                    System.out.println(indent+fieldName+" = "+kidData);
                    if (kidData instanceof FormDataObjIFace)
                    {
                        session.attach(kidData);
                        dumpDataObj(session, kidData, clsHash, level+1);
                    }
                }
                    
            }
            
        }
    }
    
    public static void dumpFieldNotebook(final String msg, final FieldNotebook fn)
    {
        System.out.println("-----------"+msg+"---------------\n"+fn.getName());
        for (FieldNotebookPageSet ps : fn.getPageSets())
        {
            System.out.println("    "+ps.getStartDate());
            for (FieldNotebookPage p : ps.getPages())
            {
                System.out.println("        "+p.getPageNumber());
            }
        }
    }
    
}
