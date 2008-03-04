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

import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.ui.forms.FormViewObj.FieldInfo;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.FormViewDefIFace;

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
    
    protected List<FieldInfo>     fieldList = new ArrayList<FieldInfo>();
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
        
        getter  = DataObjectGettableFactory.get(classObj.getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
        setter  = DataObjectSettableFactory.get(classObj.getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
    }
    
    /**
     * Clears the internal list and adds a list of fields.
     * @param id the ID to be added
     */
    public void add(final List<FieldInfo> items)
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
        FieldInfo fieldInfo = formViewObj.getFieldInfoForId(id);
        if (fieldInfo != null && fieldInfo.getFormCell() instanceof FormCellField)
        {
            fieldList.add(fieldInfo);
            formViewObj.setDoCarryForward(true);
        }
    }
    
    /**
     * Remove item.
     * @param id id of item to be removed
     */
    public void remove(final String id)
    {
        FieldInfo fieldInfo = formViewObj.getFieldInfoForId(id);
        if (fieldInfo != null && fieldInfo.getFormCell() instanceof FormCellField)
        {
            fieldList.remove(fieldInfo);
            formViewObj.setDoCarryForward(fieldList.size() > 0);
        }
    }

    /**
     * @return the fieldList
     */
    public List<FieldInfo> getFieldList()
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
        for (FieldInfo fieldInfo : fieldList)
        {
            if (fieldInfo.getFormCell().getIdent().equals(id))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     *  Clears the field list.
     */
    public void clear()
    {
        fieldList.clear();
    }
    
    /**
     * Copies a field's data from the old object to the new.
     * @param carryFwdData the old data object
     * @param newData the new data object
     */
    public void carryForward(final Object carryFwdData, final Object newData)
    {
        for (FieldInfo fieldInfo : fieldList)
        {
            FormCellFieldIFace cellField = (FormCellFieldIFace)fieldInfo.getFormCell();
            
            //Object data = getter.getFieldValue(carryFwdData, cellField.getName());
            //System.out.println(data);
            setter.setFieldValue(newData, cellField.getName(), getter.getFieldValue(carryFwdData, cellField.getName()));
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
