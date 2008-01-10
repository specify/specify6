/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValSpinner;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class PickListBusRules extends BaseBusRules
{
    //protected static final String noneStr = getResourceString("None");
    
    public PickListBusRules()
    {
        // no op
    }
    
    /**
     * Fixes up the Comboboxes.
     * @param fvo the form
     */
    public static void adjustForm(final FormViewObj fvo)
    {
        final ValComboBox formatterCBX = (ValComboBox)fvo.getControlByName("formatterCBX");
        final ValComboBox tablesCBX     = (ValComboBox)fvo.getControlByName("tablesCBX");
        final ValComboBox fieldsCBX     = (ValComboBox)fvo.getControlByName("fieldsCBX");
        final ValComboBox typesCBX      = (ValComboBox)fvo.getControlByName("typesCBX");

        tablesCBX.getComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                tableSelected(fvo);
            }
        });
        
        typesCBX.getComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                typeSelected(fvo);
            }
        });
        
        fieldsCBX.setEnabled(false);
        formatterCBX.setEnabled(false);
        
        PickListBusRules.fillTableNameCBX(tablesCBX);
        
        DefaultComboBoxModel model = (DefaultComboBoxModel)typesCBX.getComboBox().getModel();
        model.removeAllElements();
        String[] types = {UIRegistry.getResourceString("PL_ITEMS"), UIRegistry.getResourceString("PL_TABLE"), UIRegistry.getResourceString("PLTABLEFIELD")};
        for (String title : types)
        {
            model.addElement(title);
        }
    }
    
    /**
     * @param fvo
     * @param tablesCBX
     * @param fieldsCBX
     * @param formatterCBX
     */
    private static void tableSelected(final FormViewObj fvo)
    {
        ValComboBox formatterCBX = (ValComboBox)fvo.getControlByName("formatterCBX");
        ValComboBox tablesCBX     = (ValComboBox)fvo.getControlByName("tablesCBX");
        ValComboBox fieldsCBX     = (ValComboBox)fvo.getControlByName("fieldsCBX");

        String noneStr = getResourceString("None");
        
        PickList pickList = (PickList)fvo.getDataObj();
        if (pickList == null)
        {
            return;
        }
        
        JComboBox   tableCbx  = tablesCBX.getComboBox();
        DBTableInfo tableInfo = (DBTableInfo)tableCbx.getSelectedItem();
        if (tableInfo != null)
        {
            if (tableInfo.getName().equals(noneStr))
            {
                pickList.setTableName(null);
                pickList.setFieldName(null);
                pickList.setFormatter(null);
                
            } else
            {

                pickList.setTableName(tableInfo.getName());
                
                DefaultComboBoxModel fldModel = (DefaultComboBoxModel)fieldsCBX.getComboBox().getModel();
                fldModel.removeAllElements();
                
                for (DBFieldInfo fi : tableInfo.getFields())
                {
                    if (fi.getDataClass() == String.class)
                    {
                        fldModel.addElement(fi);
                    }
                }
                fieldsCBX.setEnabled(fldModel.getSize() > 0);
                
                Vector<DataObjSwitchFormatter> list = new Vector<DataObjSwitchFormatter>();
                for (DataObjSwitchFormatter fmt : DataObjFieldFormatMgr.getFormatters())
                {
                    if (fmt.getDataClass() == tableInfo.getClassObj())
                    {
                        list.add(fmt);
                    }
                }
                
                DefaultComboBoxModel fmtModel = (DefaultComboBoxModel)formatterCBX.getComboBox().getModel();
                fmtModel.removeAllElements();
                
                if (list.size() > 0)
                {
                    Collections.sort(list);
                    
                    for (DataObjSwitchFormatter fmt : list)
                    {
                        fmtModel.addElement(fmt);
                    }
                    formatterCBX.setEnabled(true);
                } else
                {
                    formatterCBX.setEnabled(false);
                }
            }
            
        } else
        {
            fieldsCBX.setEnabled(false);
        }
    }
    
    /**
     * @param tablesCBX
     */
    public static void fillTableNameCBX(final ValComboBox tablesCBX)
    {
        if (tablesCBX.getComboBox().getModel().getSize() == 0)
        {
            DefaultComboBoxModel tblModel = (DefaultComboBoxModel)tablesCBX.getComboBox().getModel();
            String noneStr = UIRegistry.getResourceString("None");
            DBTableInfo none = new DBTableInfo(-1, PickList.class.getName(), "none", "", "");
            none.setTitle(noneStr);
            
            tblModel.addElement(none);
            
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                if (DataModelObjBase.class.isAssignableFrom(ti.getClassObj()) && 
                        !ti.getName().startsWith("sp") && 
                        !ti.getName().startsWith("workbench") && 
                        !ti.getName().startsWith("user") && 
                        !ti.getName().startsWith("appres"))
                {
                    tblModel.addElement(ti);
                }
            }
        }
    }
    
    /**
     * @param vcbx
     * @param value
     * @return
     */
    private int getIndexInModel(final ValComboBox vcbx, final String value)
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel)vcbx.getComboBox().getModel();
        for (int i=0;i<model.getSize();i++)
        {
            Object obj = model.getElementAt(i);
            if (obj instanceof DBTableInfo)
            {
                DBTableInfo ti = (DBTableInfo)obj;
                System.err.println("["+ti.getName()+"]["+value+"]");
                if (ti.getName().equals(value))
                {
                    return i;
                }
                
            } else if (obj instanceof DBFieldInfo)
            {
                DBFieldInfo fi = (DBFieldInfo)obj;
                System.err.println("["+fi.getName()+"]["+value+"]");
                if (fi.getName().equals(value))
                {
                    return i;
                }
                
            } else if (obj instanceof DataObjSwitchFormatter)
            {
                DataObjSwitchFormatter fmt = (DataObjSwitchFormatter)obj;
                System.err.println("["+fmt.getName()+"]["+value+"]");
                if (fmt.getName().equals(value))
                {
                    return i;
                }
            } else if (obj.toString().equals(value))
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * @param fvo
     */
    private static void typeSelected(final FormViewObj fvo)
    {
        ValComboBox formatterCBX = (ValComboBox)fvo.getControlByName("formatterCBX");
        ValComboBox tablesCBX     = (ValComboBox)fvo.getControlByName("tablesCBX");
        ValComboBox fieldsCBX     = (ValComboBox)fvo.getControlByName("fieldsCBX");
        ValComboBox typesCBX      = (ValComboBox)fvo.getControlByName("typesCBX");
        ValSpinner  sizeLimitSp   = (ValSpinner)fvo.getControlByName("sizeLimit");
        
        MultiView pickListItemsMV = (MultiView)fvo.getControlByName("pickListItems");
        
        int typeIndex = typesCBX.getComboBox().getSelectedIndex();
        switch (typeIndex) 
        {
            case 0:
                tablesCBX.setEnabled(false);
                fieldsCBX.setEnabled(false);
                formatterCBX.setEnabled(false);
                sizeLimitSp.setEnabled(true);
                pickListItemsMV.setVisible(true);
                break;
                
            case 1:
                tablesCBX.setEnabled(true);
                fieldsCBX.setEnabled(false);
                formatterCBX.setEnabled(true);
                sizeLimitSp.setEnabled(false);
                pickListItemsMV.setVisible(false);
                break;
                
            case 2:
                tablesCBX.setEnabled(true);
                fieldsCBX.setEnabled(true);
                formatterCBX.setEnabled(true);
                sizeLimitSp.setEnabled(false);
                pickListItemsMV.setVisible(false);
                break;
                
            default:
                break;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterFillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(final Object dataObj, final Viewable viewable)
    {
        super.afterFillForm(dataObj, viewable);
        
        PickList    pickList = (PickList)dataObj;
        FormViewObj fvo      = (FormViewObj)viewable;
        
        if (pickList != null)
        {
        
            ValComboBox typesCBX       = (ValComboBox)fvo.getControlByName("typesCBX");
            ValComboBox tablesCBX      = (ValComboBox)fvo.getControlByName("tablesCBX");
            ValComboBox formatterCBX   = (ValComboBox)fvo.getControlByName("formatterCBX");
            ValComboBox fieldsCBX      = (ValComboBox)fvo.getControlByName("fieldsCBX");
            ValSpinner sizeLimitSp     = (ValSpinner)fvo.getControlByName("sizeLimit");
            
            int typeIndex = pickList.getType();
            typesCBX.getComboBox().setSelectedIndex(typeIndex);

            String tableName = pickList.getTableName();
            if (StringUtils.isNotEmpty(tableName))
            {
                tablesCBX.getComboBox().setSelectedIndex(getIndexInModel(tablesCBX, tableName));
                
                String fieldName = pickList.getFieldName();
                if (StringUtils.isNotEmpty(fieldName))
                {
                    fieldsCBX.getComboBox().setSelectedIndex(getIndexInModel(fieldsCBX, fieldName));
                }
                
                String formatter = pickList.getFormatter();
                if (StringUtils.isNotEmpty(formatter))
                {
                    formatterCBX.getComboBox().setSelectedIndex(getIndexInModel(formatterCBX, formatter));
                }
            }
            
            if (pickList.getSizeLimit() == -1)
            {
                sizeLimitSp.setEnabled(false);
                sizeLimitSp.setValue(sizeLimitSp.getMinValue());
                
            } else
            {
                sizeLimitSp.setEnabled(true);
            }
            
            typeSelected(fvo);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeFormFill(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void beforeFormFill(Viewable viewable)
    {
        // TODO Auto-generated method stub
        super.beforeFormFill(viewable);
        
        //adjustForm((FormViewObj)viewable);
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        // TODO Auto-generated method stub
        super.formShutdown();
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        //return super.processBusinessRules(dataObj);
        return STATUS.OK;
    }

}
