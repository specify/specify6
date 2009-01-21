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
package edu.ku.brc.af.ui.forms;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.ui.forms.FormViewObj.FVOFieldInfo;
import edu.ku.brc.af.ui.forms.persist.FormCellField;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class holds all the carry forward information for a single form. It also knows how to "copy" or carry forward 
 * data from the field in one object to another.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class CarryForwardInfo
{
    
    protected List<FVOFieldInfo>  fieldList = new ArrayList<FVOFieldInfo>();
    protected DataObjectGettable  getter;
    protected DataObjectSettable  setter;
    protected FormViewDefIFace    formViewDef;
    protected FormViewObj         formViewObj;
    
    /**
     * @param classObj
     * @param formViewObj
     * @param formViewDef
     */
    public CarryForwardInfo(final Class<?> classObj, 
                            final FormViewObj formViewObj, 
                            final FormViewDefIFace formViewDef)
    {
        this.formViewObj = formViewObj;
        this.formViewDef = formViewDef;
        
        getter  = DataObjectGettableFactory.get(classObj.getName(), FormHelper.DATA_OBJ_GETTER);
        setter  = DataObjectSettableFactory.get(classObj.getName(), FormHelper.DATA_OBJ_SETTER);
    }
    
    /**
     * Clears the internal list and adds a list of fields.
     * @param id the ID to be added
     */
    public void add(final List<FVOFieldInfo> items)
    {
        fieldList.clear();
        
        fieldList.addAll(items);
        formViewObj.setDoCarryForward(true);
    }
    
    /**
     * Adds an ID.
     * @param id the ID to be added
     */
    public void add(final String id)
    {
        FVOFieldInfo fvoFieldInfo = formViewObj.getFieldInfoForId(id);
        if (fvoFieldInfo != null && fvoFieldInfo.getFormCell() instanceof FormCellField)
        {
            fieldList.add(fvoFieldInfo);
            formViewObj.setDoCarryForward(true);
        }
    }
    
    /**
     * Remove item.
     * @param id id of item to be removed
     */
    public void remove(final String id)
    {
        FVOFieldInfo fvoFieldInfo = formViewObj.getFieldInfoForId(id);
        if (fvoFieldInfo != null && fvoFieldInfo.getFormCell() instanceof FormCellField)
        {
            fieldList.remove(fvoFieldInfo);
            formViewObj.setDoCarryForward(fieldList.size() > 0);
        }
    }

    /**
     * @return the fieldList
     */
    public List<FVOFieldInfo> getFieldList()
    {
        return fieldList;
    }

    /**
     * Checks to see if the field list contains this id
     * @param id the id to be checked
     * @return true it is in the list, false it is not
     */
    public boolean contains(final String id)
    {
        for (FVOFieldInfo fvoFieldInfo : fieldList)
        {
            if (fvoFieldInfo.getFormCell().getIdent().equals(id))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if there is one or more fields configured.
     */
    public boolean hasConfiguredFields()
    {
        return fieldList.size() > 0;
    }
    
    /**
     *  Clears the field list.
     */
    public void clear()
    {
        fieldList.clear();
    }
    
    private boolean isClonable(final Object obj)
    {
        try
        {
            Method method = obj.getClass().getMethod("clone", new Class<?>[] {});
            // Pretty much every Object has a "clone" method but we need 
            // to check to make sure it is implemented by the same class of 
            // Object that is in the Set.
            return method.getDeclaringClass() == obj.getClass();
            
        } catch (Exception ex) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CarryForwardInfo.class, ex);
        }
        return false;
    }
    
    /**
     * Clones the field/object.
     * @param fieldName the name of the field
     * @param fieldDataValue the old field value
     * @param newData the new parent object the field is being set into.
     */
    private void cloneCFField(final String fieldName,
                              final Object fieldDataValue,
                              final Object newData)
    {
        try
        {
            Object data = ((FormDataObjIFace)fieldDataValue).clone();
            setter.setFieldValue(newData, fieldName, data);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CarryForwardInfo.class, ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * checks to see if the data can be set or cloned.
     * @param businessRules the business rules
     * @param fieldName the field name
     * @param fieldDataValue the old data
     * @return whether the data can be or should be cloned
     */
    private boolean isFieldClonable(final BusinessRulesIFace businessRules,
                                    final String fieldName,
                                    final Object fieldDataValue)
    {
        if (fieldDataValue != null && 
            businessRules != null && 
            businessRules.shouldCloneField(fieldName))
        {
            if (isClonable(fieldDataValue))
            {
                return true;
            }
            UIRegistry.showError("A request has been made to Clone an object ["+fieldDataValue.getClass().getSimpleName()+"] and it doesn't implement Clone\nPlease ask engineering to do that.");
        }
        return false;
    }
    
    /**
     * Sets the Value (ManyToOne) or Clones it like a OneToOne.
     * @param businessRules the rules
     * @param carryFwdData the carry forward object
     * @param fvoFieldInfo field info
     * @param newData the new data
     */
    protected void setOrCloneDataValue(final BusinessRulesIFace businessRules, 
                                       final Object             carryFwdData,
                                       final FVOFieldInfo       fvoFieldInfo,
                                       final Object             newData)
    {
        String fieldName = fvoFieldInfo.getFormCell().getName();
        
        if (fieldName.equals("this") && 
            fvoFieldInfo.getComp() instanceof UIPluginable &&
            ((UIPluginable)fvoFieldInfo.getComp()).canCarryForward())
        {
            String[] fieldNames = ((UIPluginable)fvoFieldInfo.getComp()).getCarryForwardFields();
            if (fieldNames != null)
            {
                for (String fldName : fieldNames)
                {
                    Object fieldDataValue = getter.getFieldValue(carryFwdData, fldName);
                    
                    if (isFieldClonable(businessRules, fldName, fieldDataValue))
                    {
                        cloneCFField(fldName, fieldDataValue, newData);
                    } else
                    {
                        setter.setFieldValue(newData, fldName, fieldDataValue);
                    }
                }
            }
            return;
            
        } else 
        {
            Object fieldDataValue = getter.getFieldValue(carryFwdData, fieldName);
            if (isFieldClonable(businessRules, fieldName, fieldDataValue))
            {
                cloneCFField(fieldName, fieldDataValue, newData);
            } else
            {
                setter.setFieldValue(newData, fieldName, fieldDataValue);
            }
        }
    }
    
    /**
     * Copies a field's data from the old object to the new.
     * @param carryFwdData the old data object
     * @param newData the new data object
     */
    @SuppressWarnings("unchecked")
    public void carryForward(final BusinessRulesIFace businessRules, 
                             final Object carryFwdData, 
                             final Object newData)
    {
        for (FVOFieldInfo fvoFieldInfo : fieldList)
        {
            
            if (fvoFieldInfo.getFormCell() instanceof FormCellFieldIFace)
            {
                setOrCloneDataValue(businessRules, carryFwdData, fvoFieldInfo, newData);
                
            } else if (fvoFieldInfo.getFormCell() instanceof FormCellSubViewIFace)
            {
                FormCellSubViewIFace subViewFormCell = (FormCellSubViewIFace)fvoFieldInfo.getFormCell();
                Object fromData = getter.getFieldValue(carryFwdData, subViewFormCell.getName());
                Object toData   = getter.getFieldValue(newData, subViewFormCell.getName());
                
                if (fromData instanceof Set<?> && toData instanceof Set<?>)
                {
                    Set<?> fromSet = (Set<?>)fromData;
                    for (Object dObj : fromSet)
                    {
                        if (dObj instanceof FormDataObjIFace)
                        {
                            try
                            {
                                Object newObj = ((FormDataObjIFace)dObj).clone();
                                ((FormDataObjIFace)newData).addReference((FormDataObjIFace)newObj, subViewFormCell.getName());
                                
                            } catch (CloneNotSupportedException ex)
                            {
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CarryForwardInfo.class, ex);
                                //log.error(ex);
                                ex.printStackTrace();
                            }
                        }
                    }
                } else if (fvoFieldInfo.getFieldInfo() instanceof DBRelationshipInfo)
                {
                    DBRelationshipInfo ri = (DBRelationshipInfo)fvoFieldInfo.getFieldInfo();
                    if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                    {
                        setOrCloneDataValue(businessRules, carryFwdData, fvoFieldInfo, newData);
                    }
                }
                    
            }
        }
    }
    
    /**
     * Cleans up data.
     */
    public void cleanUp()
    {
        fieldList.clear();
        getter      = null;
        setter      = null;
        formViewDef = null;
        formViewObj = null;
    }

}
