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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpViewSetObj;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2008
 *
 */
public class PickListBusRules extends BaseBusRules implements FormPaneAdjusterIFace
{
    //private static final Logger log  = Logger.getLogger(PickListBusRules.class);
    
    /**
     * 
     */
    public PickListBusRules()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace#adjustForm(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    @Override
    public void adjustForm(final FormViewObj fvo)
    {
        final ValComboBox formatterCBX = (ValComboBox)fvo.getControlByName("formatterCBX");
        final ValComboBox tablesCBX    = (ValComboBox)fvo.getControlByName("tablesCBX");
        final ValComboBox fieldsCBX    = (ValComboBox)fvo.getControlByName("fieldsCBX");
        final ValComboBox typesCBX     = (ValComboBox)fvo.getControlByName("typesCBX");
        final ValCheckBox readOnlyChk  = (ValCheckBox)fvo.getControlByName("readOnly");
        
        tablesCBX.getComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tableSelected(fvo);
            }
        });
        
        typesCBX.getComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                typeSelected(fvo);
            }
        });
        
        readOnlyChk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!readOnlyChk.isSelected() && formViewObj.getDataObj() != null)
                {
                    adjustSizeSpinner();
                }
            }
        });
        
        fieldsCBX.setEnabled(false);
        formatterCBX.setEnabled(false);
        
        fillTableNameCBX(tablesCBX);
        
        DefaultComboBoxModel model = (DefaultComboBoxModel)typesCBX.getComboBox().getModel();
        model.removeAllElements();
        String[] types = {getResourceString("PL_ITEMS"), getResourceString("PL_TABLE"), getResourceString("PLTABLEFIELD")};
        for (String title : types)
        {
            model.addElement(title);
        }
    }
    
    /**
     * This is an adjustment for PickLists with items (only)
     */
    protected void adjustSizeSpinner()
    {
        ValSpinner  sizeLimitSp = (ValSpinner)formViewObj.getControlByName("sizeLimit");
        if (sizeLimitSp != null)
        {
            PickListIFace pickList = (PickListIFace)formViewObj.getDataObj();
            if (pickList.getReadOnly())
            {
                sizeLimitSp.setRange(0, 0, 0);
                
            } else if (formViewObj != null && pickList.getType() == PickListIFace.PL_WITH_ITEMS)
            {
                int           min         = Math.max(pickList.getNumItems(), 0);
                int           max         = AppPreferences.getRemote().getInt("PL_MAX_ITEMS", 500);
                Integer       val         = pickList.getSizeLimit();
                formViewObj.getValidator().setHasChanged(true);
                
                if (min > max)
                {
                    max = min;
                }
    
                if (val == null || val == -1 || val < min)
                {
                    val = min;
                }
                sizeLimitSp.setRange(min, max, val);
            }
        }
    } 
    
    /**
     * 
     */
    private void tableSelected(final FormViewObj fvo)
    {
        ValComboBox formatterCBX  = (ValComboBox)fvo.getControlByName("formatterCBX");
        ValComboBox tablesCBX     = (ValComboBox)fvo.getControlByName("tablesCBX");
        ValComboBox fieldsCBX     = (ValComboBox)fvo.getControlByName("fieldsCBX");
        
        fvo.getValidator().setHasChanged(true);
        tablesCBX.setChanged(true);

        //int typeIndex = typesCBX.getComboBox().getSelectedIndex();
        
        String noneStr = getResourceString("NONE");
        
        PickList pickList = (PickList)fvo.getDataObj();
        if (pickList == null)
        {
            return;
        }
        
        JComboBox   tableCbx = tablesCBX.getComboBox();
        DBTableInfo tblInfo  = (DBTableInfo)tableCbx.getSelectedItem();
        if (tblInfo != null)
        {
            if (tblInfo.getName().equals(noneStr))
            {
                pickList.setTableName(null);
                pickList.setFieldName(null);
                pickList.setFormatter(null);
                
            } else
            {
                pickList.setTableName(tblInfo.getName());
                
                DefaultComboBoxModel fldModel = (DefaultComboBoxModel)fieldsCBX.getComboBox().getModel();
                fldModel.removeAllElements();
                
                for (DBFieldInfo fi : tblInfo.getFields())
                {
                    if (fi.getDataClass() == String.class)
                    {
                        fldModel.addElement(fi);
                    }
                }
                
                Vector<DataObjSwitchFormatter> list = new Vector<DataObjSwitchFormatter>();
                for (DataObjSwitchFormatter fmt : DataObjFieldFormatMgr.getInstance().getFormatters())
                {
                    if (fmt.getDataClass() == tblInfo.getClassObj())
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
    private void fillTableNameCBX(final ValComboBox tablesCBX)
    {
        if (tablesCBX.getComboBox().getModel().getSize() == 0)
        {
            DefaultComboBoxModel tblModel = (DefaultComboBoxModel)tablesCBX.getComboBox().getModel();
            String               noneStr  = getResourceString("NONE");
            DBTableInfo          none     = new DBTableInfo(-1, PickList.class.getName(), "none", "", "");
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
     * Returns the index in the model for the string value.
     * @param vcbx the combobox
     * @param value the value
     * @return the index in the model for the string value
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
                if (ti.getName().equals(value))
                {
                    return i;
                }
                
            } else if (obj instanceof DBFieldInfo)
            {
                DBFieldInfo fi = (DBFieldInfo)obj;
                if (fi.getName().equals(value))
                {
                    return i;
                }
                
            } else if (obj instanceof DataObjSwitchFormatter)
            {
                DataObjSwitchFormatter fmt = (DataObjSwitchFormatter)obj;
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
     * 
     */
    private static void typeSelected(final FormViewObj fvo)
    {
        fvo.getValidator().setHasChanged(true);

        ValComboBox formatterCBX  = (ValComboBox)fvo.getControlByName("formatterCBX");
        ValComboBox tablesCBX     = (ValComboBox)fvo.getControlByName("tablesCBX");
        ValComboBox fieldsCBX     = (ValComboBox)fvo.getControlByName("fieldsCBX");
        ValComboBox typesCBX      = (ValComboBox)fvo.getControlByName("typesCBX");
        ValSpinner  sizeLimitSp   = (ValSpinner)fvo.getControlByName("sizeLimit");
        ValCheckBox readOnlyChk   = (ValCheckBox)fvo.getControlByName("readOnly");

        MultiView pickListItemsMV = (MultiView)fvo.getControlByName("pickListItems");
        
        typesCBX.setChanged(true);
        
        FormDataObjIFace dataObj = (FormDataObjIFace)fvo.getDataObj();
        boolean        isEditing = dataObj != null && dataObj.getId() != null;
        PickListIFace  pickList  = (PickListIFace)fvo.getDataObj();
        
        boolean fullEditIsOK = !isEditing || !pickList.isSystem();
        
        int typeIndex = typesCBX.getComboBox().getSelectedIndex();
        //log.debug("Type: "+typeIndex);
        switch (typeIndex) 
        {
            case 0: // Item
                tablesCBX.setEnabled(false);
                fieldsCBX.setEnabled(false);
                formatterCBX.setEnabled(false);
                sizeLimitSp.setEnabled(true);
                pickListItemsMV.setVisible(true);
                readOnlyChk.setEnabled(true);
                Object val = sizeLimitSp.getValue();
                if (val != null && val instanceof Integer && ((Integer)val) == -1)
                {
                    sizeLimitSp.setValue(50);
                }
                break;
                
            case 1: // Table
                tablesCBX.setEnabled(fullEditIsOK);
                fieldsCBX.setEnabled(false);
                formatterCBX.setEnabled(true);
                pickListItemsMV.setVisible(false);
                readOnlyChk.setEnabled(false);
                sizeLimitSp.setEnabled(false);
                sizeLimitSp.setValue(-1);
               
                fieldsCBX.getComboBox().setSelectedIndex(-1);
                break;
                
            case 2: // TableField
                tablesCBX.setEnabled(true);
                fieldsCBX.setEnabled(true);
                formatterCBX.setEnabled(true);
                pickListItemsMV.setVisible(false);
                readOnlyChk.setEnabled(false);
                
                //sizeLimitSp.setValue(-1);
                sizeLimitSp.setEnabled(false);
                fieldsCBX.getComboBox().setSelectedIndex(-1);
                break;
                
            default:
                break;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        PickList    pickList = (PickList)dataObj;
        if (pickList != null)
        {
            ValComboBox typesCBX       = (ValComboBox)formViewObj.getControlByName("typesCBX");
            ValComboBox tablesCBX      = (ValComboBox)formViewObj.getControlByName("tablesCBX");
            ValComboBox formatterCBX   = (ValComboBox)formViewObj.getControlByName("formatterCBX");
            ValComboBox fieldsCBX      = (ValComboBox)formViewObj.getControlByName("fieldsCBX");
            ValSpinner  sizeLimitSp    = (ValSpinner)formViewObj.getControlByName("sizeLimit");
            
            int fieldsSelectedIndex = -1;
            int fmtsSelectedIndex   = -1;
            
            int typeIndex = pickList.getType();
            if (typesCBX != null && typesCBX.getComboBox() != null)
            {
                if (typesCBX.getComboBox().getModel().getSize() > 0)
                {
                    typesCBX.getComboBox().setSelectedIndex(typeIndex);
                }

                String tableName = pickList.getTableName();
                if (StringUtils.isNotEmpty(tableName))
                {
                    tablesCBX.getComboBox().setSelectedIndex(getIndexInModel(tablesCBX, tableName));
                    
                    String fieldName = pickList.getFieldName();
                    if (StringUtils.isNotEmpty(fieldName))
                    {
                        fieldsSelectedIndex = getIndexInModel(fieldsCBX, fieldName);
                        //fieldsCBX.getComboBox().setSelectedIndex();
                    }
                    
                    String formatter = pickList.getFormatter();
                    if (StringUtils.isNotEmpty(formatter))
                    {
                        fmtsSelectedIndex = getIndexInModel(formatterCBX, formatter);
                    }
                }
                
                if (pickList.getSizeLimit() == -1)
                {
                    sizeLimitSp.setEnabled(false);
                    sizeLimitSp.setValue(sizeLimitSp.getMinValue());
                    
                } else
                {
                    boolean plWithItems = typeIndex == PickListIFace.PL_WITH_ITEMS;
                    sizeLimitSp.setEnabled(plWithItems);
                    if (plWithItems)
                    {
                        adjustSizeSpinner();
                    }
                }
                
                typeSelected(formViewObj);
                
                typesCBX.setEnabled(!pickList.getIsSystem());
            }
            
            fieldsCBX.getComboBox().setSelectedIndex(fieldsSelectedIndex);
            formatterCBX.getComboBox().setSelectedIndex(fmtsSelectedIndex);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        PickList pl = (PickList)dataObj;
        
        ValComboBox typesCBX      = (ValComboBox)formViewObj.getControlByName("typesCBX");
        ValComboBox formatterCBX  = (ValComboBox)formViewObj.getControlByName("formatterCBX");
        ValComboBox tablesCBX     = (ValComboBox)formViewObj.getControlByName("tablesCBX");
        ValComboBox fieldsCBX     = (ValComboBox)formViewObj.getControlByName("fieldsCBX");
        ValSpinner  sizeLimitSp   = (ValSpinner)formViewObj.getControlByName("sizeLimit");
        
        Integer val = (Integer)sizeLimitSp.getValue();
        pl.setSizeLimit(val);
        
        int index = typesCBX.getComboBox().getSelectedIndex();
        pl.setType((byte)index);
        
        DBTableInfo            ti   = (DBTableInfo)tablesCBX.getValue();
        DBFieldInfo            fi   = (DBFieldInfo)fieldsCBX.getValue();
        DataObjSwitchFormatter dofw = (DataObjSwitchFormatter)formatterCBX.getValue();
        
        pl.setTableName(ti != null ? ti.getName() : null);
        pl.setFieldName(fi != null ? fi.getName() : null);
        pl.setFormatter(dofw != null ? dofw.getName() : null);
        
        super.beforeSave(dataObj, session);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(final Object dataObj)
    {
        return ((PickList)dataObj).getIsSystem();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        PickList pickList = (PickList)dataObj;
        
        if (!pickList.getIsSystem())
        {
            // Check all the forms here
            // This is cheap and a hack because we should probably parse the XML
            // instead of just doing a text search
            
            String searchText = "picklist=\""+pickList.getName()+"\"";
            
            for (SpAppResourceDir appDir : ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getSpAppResourceList())
            {
                for (SpViewSetObj viewSet :appDir.getSpViewSets())
                {
                    String xml = viewSet.getDataAsString();
                    if (StringUtils.contains(xml, searchText))
                    {
                        if (deletable != null)
                        {
                            deletable.doDeleteDataObj(dataObj, session, false);
                        }
                        return;
                    }
                }
            }
            super.okToDelete(dataObj, session, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        PickList pickList = (PickList)dataObj;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            reasonList.clear();
            
            String   sql  = QueryAdjusterForDomain.getInstance().adjustSQL("FROM PickList WHERE name = '"+ pickList.getName()+"' AND collectionId = COLLID");
            PickList dbPL = (PickList)session.getData(sql);
            //log.debug("["+dbPL.getId()+"]["+pickList.getId()+"]");
            if (dbPL != null && (pickList.getId() == null || !dbPL.getId().equals(pickList.getId())))
            {
                reasonList.add(getLocalizedMessage("PL_DUPLICATE_NAME", pickList.getName()));
                return STATUS.Error;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListBusRules.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return super.processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        PickListIFace rs = (PickListIFace)dataObj;
        if (rs.getType() == PickListIFace.PL_WITH_ITEMS)
        {
            adjustSizeSpinner();
        }
        return super.afterSaveCommit(dataObj, session);
    }


}
