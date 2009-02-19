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

import java.awt.Component;

import javax.swing.JCheckBox;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2008
 *
 */
public class CatAutoNumberingSchemeBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public CatAutoNumberingSchemeBusRules()
    {
        super(AutoNumberingScheme.class);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        Component comp = formViewObj.getControlById("2");
        if (comp != null && comp instanceof JCheckBox)
        {
            JCheckBox   cbx = (JCheckBox)comp;
            DBTableInfo ti  = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
            DBFieldInfo fi  = ti.getFieldByColumnName("CatalogNumber");
            UIFieldFormatterIFace fmt = fi.getFormatter();
            if (fmt != null)
            {
                cbx.setEnabled(false);
                cbx.setSelected(fmt.isNumeric());
            } else
            {
                cbx.setVisible(false);
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
    }
    
}
