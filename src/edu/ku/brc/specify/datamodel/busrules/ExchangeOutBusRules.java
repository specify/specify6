/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.ExchangeOut;

import javax.swing.*;
import java.awt.*;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 3, 2009
 *
 */
public class ExchangeOutBusRules extends AttachmentOwnerBaseBusRules
{

    public ExchangeOutBusRules()
    {
        super(ExchangeOut.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(Object newDataObj)
    {
        ((ExchangeOut)newDataObj).setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);

        if (!(viewable instanceof FormViewObj) || dataObj == null)
        {
            return;
        }

        ExchangeOut exchangeOut = (ExchangeOut)dataObj;

        DBTableInfo divisionTI = DBTableIdMgr.getInstance().getInfoById(Division.getClassTableId());
        FormViewObj fvo = (FormViewObj)viewable;
        JLabel label = (JLabel)fvo.getLabelById("divLabel");
        if (label != null)
        {
            label.setText(divisionTI.getTitle()+":");
        }

        Component divComp = fvo.getControlById("divcbx");
        if (divComp instanceof ValComboBox)
        {
            ValComboBox cbx = (ValComboBox)divComp;
            DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
            model.removeAllElements();
            model.addElement(exchangeOut.getDivision());
            cbx.getComboBox().setSelectedIndex(0);

        } else if (divComp instanceof JTextField)
        {
            JTextField tf = (JTextField)divComp;
            tf.setText(exchangeOut.getDivision().getName());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();

        if (!(dataObj instanceof ExchangeOut))
        {
            return STATUS.Error;
        }

        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("exchangeOutNumber",
                (FormDataObjIFace)dataObj,
                ExchangeOut.class,
                "exchangeOutId");

        return duplicateNumberStatus;
    }

}
