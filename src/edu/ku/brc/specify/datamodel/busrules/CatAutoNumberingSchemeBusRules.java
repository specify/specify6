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
