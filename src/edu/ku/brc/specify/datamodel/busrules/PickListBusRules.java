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
        
        tablesCBX.getComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                tableSelected(fvo, tablesCBX, fieldsCBX, formatterCBX);
            }
        });
        
        fieldsCBX.setEnabled(false);
        formatterCBX.setEnabled(false);
        
        PickListBusRules.fillTableNameCBX(tablesCBX);
        
    }
    
    /**
     * @param fvo
     * @param tablesCBX
     * @param fieldsCBX
     * @param formatterCBX
     */
    private static void tableSelected(final FormViewObj fvo,
                                      final ValComboBox tablesCBX,
                                      final ValComboBox fieldsCBX,
                                      final ValComboBox formatterCBX)
    {
        String noneStr = getResourceString("None");
        
        PickList pickList = (PickList)fvo.getDataObj();
        if (pickList == null)
        {
            return;
        }
        
        JComboBox   tableCbx  = tablesCBX.getComboBox();
        String      tableName = (String)tableCbx.getSelectedItem();
        if (tableName != null)
        {
            if (tableName.equals(noneStr))
            {
                pickList.setTableName(null);
                pickList.setFieldName(null);
                pickList.setFormatter(null);
                
            } else
            {
                
                //String currentName = (String)tablesCBX.getComboBox().getSelectedItem();
                DBTableInfo ti = null;
                for (DBTableInfo tblInfo : DBTableIdMgr.getInstance().getTables())
                {
                    if (tblInfo.getTitle().equals(tableName))
                    {
                        ti = tblInfo;
                        break;
                    }
                }
                
                if (ti != null)
                {
                    //if (StringUtils.isEmpty(currentName) || !currentName.equals(ti.getTitle()))
                    {
                        pickList.setTableName(ti.getName());
                        
                        DefaultComboBoxModel fldModel = (DefaultComboBoxModel)fieldsCBX.getComboBox().getModel();
                        fldModel.removeAllElements();
                        
                        for (DBFieldInfo fi : ti.getFields())
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
                            if (fmt.getDataClass() == ti.getClassObj())
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
            tblModel.addElement(UIRegistry.getResourceString("None"));
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                if (DataModelObjBase.class.isAssignableFrom(ti.getClassObj()) && 
                        !ti.getName().startsWith("sp") && 
                        !ti.getName().startsWith("workbench") && 
                        !ti.getName().startsWith("user") && 
                        !ti.getName().startsWith("appres"))
                {
                    tblModel.addElement(ti.getTitle());
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
            if (obj instanceof DBFieldInfo)
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
     * @param tablesCBX
     * @param value
     * @return
     */
    private int getTableIndexInModel(final ValComboBox tablesCBX, final String value)
    {
        String noneStr = getResourceString("None");
        if (value.equals(noneStr))
        {
            return 0;
        }
        
        DBTableInfo tblInfo = null;
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            System.out.println("["+ti.getTitle()+"]["+value+"]");
            if (ti.getName().equals(value))
            {
                tblInfo = ti;
                break;
            }
        }
        
        if (tblInfo != null)
        {
            DefaultComboBoxModel model = (DefaultComboBoxModel)tablesCBX.getComboBox().getModel();
            for (int i=0;i<model.getSize();i++)
            {
                Object obj = model.getElementAt(i);
                System.out.println("["+tblInfo.getName()+"]["+obj.toString()+"]");
                if (tblInfo.getTitle().equals(obj.toString()))
                {
                    return i;
                }
            }
        }
        return -1;
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
        
            ValComboBox formatterCBX = (ValComboBox)fvo.getControlByName("formatterCBX");
            ValComboBox tablesCBX    = (ValComboBox)fvo.getControlByName("tablesCBX");
            ValComboBox fieldsCBX    = (ValComboBox)fvo.getControlByName("fieldsCBX");
            
            String tableName = pickList.getTableName();
            if (StringUtils.isNotEmpty(tableName))
            {
                tablesCBX.getComboBox().setSelectedIndex(getTableIndexInModel(tablesCBX, tableName));
                
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
            
            ValSpinner sizeLimitSp = (ValSpinner)fvo.getControlByName("sizeLimit");
            if (pickList.getSizeLimit() == -1)
            {
                sizeLimitSp.setEnabled(false);
                sizeLimitSp.setValue(sizeLimitSp.getMinValue());
                
            } else
            {
                sizeLimitSp.setEnabled(true);
            }
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
        // TODO Auto-generated method stub
        return false;
    }

}
